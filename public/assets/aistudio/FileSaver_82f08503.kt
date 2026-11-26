package com.example.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream

object DuplicateFileGuard {
    fun resolveUniqueDisplayName(
        existingNames: Set<String>,
        desiredName: String
    ): String {
        require(desiredName.isNotBlank()) { "desiredName must not be blank." }
        if (desiredName !in existingNames) return desiredName

        val dot = desiredName.lastIndexOf('.')
        val base = if (dot > 0) desiredName.substring(0, dot) else desiredName
        val extension = if (dot > 0) desiredName.substring(dot) else ""
        var index = 1
        while (true) {
            val candidate = "$base ($index)$extension"
            if (candidate !in existingNames) return candidate
            index++
        }
    }
}

data class SavedFileRecord(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long
)

object FileSaver {
    fun takeGrantedFolderPermission(
        context: Context,
        treeUri: Uri,
        grantedFlags: Int
    ): Boolean {
        val permissionMask = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val persistableFlags = grantedFlags and permissionMask
        if (persistableFlags == 0) return false

        return try {
            context.applicationContext.contentResolver
                .takePersistableUriPermission(treeUri, persistableFlags)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    fun saveToDownloads(
        context: Context,
        desiredDisplayName: String,
        mimeType: String,
        bytes: ByteArray,
        fileProviderAuthority: String? = null
    ): SavedFileRecord {
        require(desiredDisplayName.isNotBlank()) { "File name must not be blank." }
        require(mimeType.isNotBlank()) { "MIME type must not be blank." }

        val app = context.applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveMediaStoreQPlus(app, desiredDisplayName, mimeType, bytes)
        } else {
            val authority = fileProviderAuthority ?: "${app.packageName}.fileprovider"
            saveLegacyAtomic(app, desiredDisplayName, mimeType, bytes, authority)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveMediaStoreQPlus(
        app: Context,
        desiredName: String,
        mimeType: String,
        bytes: ByteArray
    ): SavedFileRecord {
        val resolver = app.contentResolver
        val collection = MediaStore.Downloads.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
        val candidate = DuplicateFileGuard.resolveUniqueDisplayName(
            queryNames(resolver, collection), desiredName
        )
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, candidate)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: error("Failed to create file in Downloads.")

        try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: error("Unable to open output stream for $candidate.")

            val actualName = queryActualName(resolver, uri)
                ?: error("Unable to verify final MediaStore display name.")
            val verifiedSize = verifyReadableSize(app, uri, bytes.size.toLong())
            check(verifiedSize == bytes.size.toLong()) {
                "Saved file size mismatch: expected ${bytes.size}, found $verifiedSize."
            }

            val publish = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            check(resolver.update(uri, publish, null, null) > 0) {
                "Failed to publish completed file $candidate."
            }
            return SavedFileRecord(uri, actualName, verifiedSize)
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryNames(
        resolver: android.content.ContentResolver,
        collection: Uri
    ): Set<String> {
        val names = mutableSetOf<String>()
        resolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) cursor.getString(index)?.let(names::add)
        }
        return names
    }

    private fun queryActualName(
        resolver: android.content.ContentResolver,
        uri: Uri
    ): String? = resolver.query(
        uri,
        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (index >= 0 && !cursor.isNull(index)) cursor.getString(index) else null
        } else null
    }

    /**
     * Reads the saved document back through its content URI. This is more reliable
     * than trusting a provider's SIZE column, which may be absent or stale.
     */
    private fun verifyReadableSize(
        context: Context,
        uri: Uri,
        expectedSize: Long
    ): Long {
        var total = 0L
        context.contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) total += count
            }
        } ?: error("Unable to reopen saved file for verification.")
        check(total == expectedSize) {
            "Saved file verification failed: expected $expectedSize bytes, read $total bytes."
        }
        return total
    }

    @Suppress("DEPRECATION")
    private fun saveLegacyAtomic(
        app: Context,
        desiredName: String,
        mimeType: String,
        bytes: ByteArray,
        authority: String
    ): SavedFileRecord {
        val downloads = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        check(downloads.exists() || downloads.mkdirs()) {
            "Cannot access Downloads directory."
        }

        val uniqueName = DuplicateFileGuard.resolveUniqueDisplayName(
            downloads.list()?.toSet().orEmpty(),
            desiredName
        )
        val finalFile = File(downloads, uniqueName)
        val temporaryFile = File(downloads, ".$uniqueName.${System.nanoTime()}.tmp")
        var committed = false

        try {
            FileOutputStream(temporaryFile).use { output ->
                output.write(bytes)
                output.flush()
                output.fd.sync()
            }
            check(temporaryFile.length() == bytes.size.toLong()) {
                "Temporary file size verification failed."
            }
            check(!finalFile.exists()) { "Target already exists: $uniqueName" }
            check(temporaryFile.renameTo(finalFile)) {
                "Atomic rename failed for $uniqueName."
            }
            committed = true
            check(finalFile.isFile && finalFile.length() == bytes.size.toLong()) {
                "Final Downloads file verification failed."
            }

            val contentUri = FileProvider.getUriForFile(app, authority, finalFile)
            return SavedFileRecord(contentUri, finalFile.name, finalFile.length())
        } catch (error: Exception) {
            if (temporaryFile.exists()) temporaryFile.delete()
            if (committed && finalFile.exists()) finalFile.delete()
            throw error
        }
    }

    fun saveToTreeUri(
        context: Context,
        treeUri: Uri,
        desiredDisplayName: String,
        mimeType: String,
        bytes: ByteArray
    ): SavedFileRecord {
        require(desiredDisplayName.isNotBlank()) { "File name must not be blank." }
        require(mimeType.isNotBlank()) { "MIME type must not be blank." }

        val app = context.applicationContext
        val tree = DocumentFile.fromTreeUri(app, treeUri)
        require(tree != null && tree.canWrite()) {
            "Selected folder is not writable or its permission has expired."
        }

        val existingNames = tree.listFiles().mapNotNull { it.name }.toSet()
        val uniqueName = DuplicateFileGuard.resolveUniqueDisplayName(
            existingNames,
            desiredDisplayName
        )
        val file = tree.createFile(mimeType, uniqueName)
            ?: error("Failed to create $uniqueName in selected folder.")

        return try {
            app.contentResolver.openOutputStream(file.uri, "w")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: error("Unable to open output stream for ${file.uri}.")

            val finalName = file.name
                ?: error("Unable to verify saved SAF display name.")
            val verifiedSize = verifyReadableSize(app, file.uri, bytes.size.toLong())
            check(verifiedSize == bytes.size.toLong()) {
                "SAF saved file size mismatch: expected ${bytes.size}, found $verifiedSize."
            }
            SavedFileRecord(file.uri, finalName, verifiedSize)
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }
}
