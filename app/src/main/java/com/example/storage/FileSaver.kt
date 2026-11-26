package com.example.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream

object DuplicateFileGuard {
    fun resolveUniqueDisplayName(existingNames: Set<String>, desiredName: String): String {
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

data class SavedFileRecord(val uri: Uri, val displayName: String, val sizeBytes: Long)

object FileSaver {
    private const val READ_WRITE_FLAGS =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    /** Persist only read/write grants explicitly returned by Android's picker. */
    fun takeGrantedFolderPermission(
        context: Context,
        treeUri: Uri,
        resultFlags: Int
    ): Boolean {
        val grantedFlags = resultFlags and READ_WRITE_FLAGS
        val readGranted = grantedFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
        val writeGranted = grantedFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0
        val persistableGranted =
            resultFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0
        // This screen browses the folder and supports writing a sample file, so
        // both capabilities must be granted before the folder becomes active.
        if (!readGranted || !writeGranted || !persistableGranted) return false
        return try {
            context.applicationContext.contentResolver
                .takePersistableUriPermission(treeUri, grantedFlags)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    /** Persist a selected document's read grant when the provider supports it. */
    fun takeGrantedDocumentReadPermission(
        context: Context,
        documentUri: Uri,
        resultFlags: Int
    ): Boolean {
        val readGranted = resultFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
        val persistableGranted =
            resultFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0
        if (!readGranted || !persistableGranted) return false
        return try {
            context.applicationContext.contentResolver.takePersistableUriPermission(
                documentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
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
            saveLegacyAtomic(app, desiredDisplayName, bytes, authority)
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
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uniqueName = DuplicateFileGuard.resolveUniqueDisplayName(
            queryNames(resolver, collection), desiredName
        )
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, uniqueName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: error("Failed to create file in Downloads.")

        try {
            resolver.openOutputStream(uri, "w")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: error("Unable to open output stream for $uniqueName.")

            val verifiedSize = verifyReadableSize(app, uri, bytes.size.toLong())
            val actualName = queryActualName(resolver, uri)
                ?: error("Unable to verify final MediaStore display name.")

            val publishValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            check(resolver.update(uri, publishValues, null, null) > 0) {
                "Failed to publish completed file $uniqueName."
            }
            return SavedFileRecord(uri, actualName, verifiedSize)
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryNames(resolver: android.content.ContentResolver, collection: Uri): Set<String> {
        val names = mutableSetOf<String>()
        resolver.query(
            collection, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) cursor.getString(index)?.let(names::add)
        }
        return names
    }

    private fun queryActualName(resolver: android.content.ContentResolver, uri: Uri): String? =
        resolver.query(
            uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (index >= 0 && !cursor.isNull(index)) cursor.getString(index) else null
            } else null
        }

    /** Read back the actual content URI; do not trust a missing/stale SIZE column. */
    private fun verifyReadableSize(context: Context, uri: Uri, expectedSize: Long): Long {
        var actualSize = 0L
        context.contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                if (count > 0) actualSize += count
            }
        } ?: error("Unable to reopen saved file for verification.")
        check(actualSize == expectedSize) {
            "Saved file size mismatch: expected $expectedSize bytes, read $actualSize bytes."
        }
        return actualSize
    }

    @Suppress("DEPRECATION")
    private fun saveLegacyAtomic(
        app: Context,
        desiredName: String,
        bytes: ByteArray,
        authority: String
    ): SavedFileRecord {
        val downloads = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        check(downloads.exists() || downloads.mkdirs()) { "Cannot access Downloads directory." }
        val uniqueName = DuplicateFileGuard.resolveUniqueDisplayName(
            downloads.list()?.toSet().orEmpty(), desiredName
        )
        val finalFile = File(downloads, uniqueName)
        val tempFile = File(downloads, ".$uniqueName.${System.nanoTime()}.tmp")
        var committed = false
        try {
            FileOutputStream(tempFile).use { output ->
                output.write(bytes)
                output.flush()
                output.fd.sync()
            }
            check(tempFile.length() == bytes.size.toLong()) {
                "Temporary Downloads file failed size verification."
            }
            check(!finalFile.exists()) { "Target already exists: $uniqueName" }
            check(tempFile.renameTo(finalFile)) { "Atomic rename failed for $uniqueName." }
            committed = true
            val actualSize = finalFile.length()
            check(finalFile.isFile && actualSize == bytes.size.toLong()) {
                "Final Downloads file failed size verification."
            }
            val uri = FileProvider.getUriForFile(app, authority, finalFile)
            return SavedFileRecord(uri, finalFile.name, actualSize)
        } catch (error: Exception) {
            if (tempFile.exists()) tempFile.delete()
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
        val existing = tree.listFiles().mapNotNull { it.name }.toSet()
        val uniqueName = DuplicateFileGuard.resolveUniqueDisplayName(existing, desiredDisplayName)
        val file = tree.createFile(mimeType, uniqueName)
            ?: error("Failed to create $uniqueName in selected folder.")
        return try {
            app.contentResolver.openOutputStream(file.uri, "w")?.use { output ->
                output.write(bytes)
                output.flush()
            } ?: error("Unable to open output stream for ${file.uri}.")
            val finalName = file.name ?: error("Unable to verify final SAF display name.")
            val verifiedSize = verifyReadableSize(app, file.uri, bytes.size.toLong())
            SavedFileRecord(file.uri, finalName, verifiedSize)
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }
}
