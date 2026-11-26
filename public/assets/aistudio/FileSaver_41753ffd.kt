package com.example.storage

import android.content.ContentResolver
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
    fun extractPersistableGrantFlags(resultData: Intent?): Int {
        val rawFlags = resultData?.flags ?: return 0
        val mask = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        return rawFlags and mask
    }

    fun takeGrantedFolderPermission(
        context: Context,
        treeUri: Uri,
        grantedFlags: Int
    ): Boolean {
        val mask = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val flags = grantedFlags and mask
        val hasPersistableGrant =
            (grantedFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) != 0
        if (flags == 0 || !hasPersistableGrant) return false
        return try {
            context.applicationContext.contentResolver
                .takePersistableUriPermission(treeUri, flags)
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
        require(desiredDisplayName.isNotBlank())
        require(mimeType.isNotBlank())
        val app = context.applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveMediaStoreQPlus(app, desiredDisplayName, mimeType, bytes)
        } else {
            val authority = fileProviderAuthority ?: "${app.packageName}.fileprovider"
            saveLegacyAtomic(
                app, desiredDisplayName, mimeType, bytes, authority
            )
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
            resolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
                out.flush()
            } ?: error("Unable to open output stream for $candidate.")

            val published = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            check(resolver.update(uri, published, null, null) > 0) {
                "Failed to publish completed file $candidate."
            }

            val actualName = queryActualName(resolver, uri)
                ?.takeIf { it.isNotBlank() }
                ?: error("Unable to verify final MediaStore display name.")
            val actualSize = queryActualSize(resolver, uri)
                ?: error("Unable to verify persisted MediaStore file size.")
            check(actualSize == bytes.size.toLong()) {
                "Persisted size mismatch for $actualName: expected ${bytes.size}, got $actualSize."
            }
            return SavedFileRecord(uri, actualName, actualSize)
        } catch (error: Exception) {
            runCatching { resolver.delete(uri, null, null) }
            throw error
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryNames(
        resolver: ContentResolver,
        collection: Uri
    ): Set<String> {
        val names = mutableSetOf<String>()
        resolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            val index = cursor.getColumnIndexOrThrow(
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            while (cursor.moveToNext()) cursor.getString(index)?.let(names::add)
        }
        return names
    }

    private fun queryActualName(
        resolver: ContentResolver,
        uri: Uri
    ): String? = resolver.query(
        uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (index >= 0 && !cursor.isNull(index)) cursor.getString(index) else null
        } else null
    }

    private fun queryActualSize(
        resolver: ContentResolver,
        uri: Uri
    ): Long? = resolver.query(
        uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            if (index >= 0 && !cursor.isNull(index)) {
                val value = cursor.getLong(index)
                if (value >= 0L) value else null
            } else null
        } else null
    }

    @Suppress("DEPRECATION")
    private fun saveLegacyAtomic(
        app: Context,
        desiredName: String,
        mimeType: String,
        bytes: ByteArray,
        authority: String
    ): SavedFileRecord {
        val dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        check(dir.exists() || dir.mkdirs()) { "Cannot access Downloads directory." }

        val unique = DuplicateFileGuard.resolveUniqueDisplayName(
            dir.list()?.toSet().orEmpty(), desiredName
        )
        val finalFile = File(dir, unique)
        val temp = File(dir, ".$unique.${System.nanoTime()}.tmp")

        var committed = false
        try {
            FileOutputStream(temp).use { stream ->
                stream.write(bytes)
                stream.flush()
                stream.fd.sync()
            }
            check(!finalFile.exists()) { "Target already exists: $unique" }
            check(temp.renameTo(finalFile)) { "Atomic rename failed for $unique." }
            committed = true

            val verifiedSize = finalFile.length()
            check(finalFile.exists() && verifiedSize == bytes.size.toLong()) {
                "Persisted legacy file verification failed for $unique: expected ${bytes.size}, got $verifiedSize."
            }

            val contentUri = FileProvider.getUriForFile(app, authority, finalFile)
            return SavedFileRecord(contentUri, finalFile.name, verifiedSize)
        } catch (error: Exception) {
            if (temp.exists()) runCatching { temp.delete() }
            if (committed && finalFile.exists()) runCatching { finalFile.delete() }
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
        require(desiredDisplayName.isNotBlank())
        require(mimeType.isNotBlank())
        val app = context.applicationContext
        val tree = DocumentFile.fromTreeUri(app, treeUri)
        require(tree != null && tree.canWrite()) {
            "Selected folder is not writable."
        }

        val existing = tree.listFiles().mapNotNull { it.name }.toSet()
        val unique = DuplicateFileGuard.resolveUniqueDisplayName(existing, desiredDisplayName)
        val file = tree.createFile(mimeType, unique)
            ?: error("Failed to create $unique in selected folder.")

        return try {
            app.contentResolver.openOutputStream(file.uri)?.use { out ->
                out.write(bytes)
                out.flush()
            } ?: error("Unable to open output stream for ${file.uri}.")

            val refreshed = DocumentFile.fromSingleUri(app, file.uri) ?: file
            val finalName = refreshed.name?.takeIf { it.isNotBlank() }
                ?: error("Unable to verify saved SAF display name.")
            val actualSize = queryActualSize(app.contentResolver, refreshed.uri)
                ?: refreshed.length().takeIf { it >= 0L }
                ?: error("Unable to verify persisted SAF file size.")
            check(actualSize == bytes.size.toLong()) {
                "Persisted SAF size mismatch for $finalName: expected ${bytes.size}, got $actualSize."
            }
            SavedFileRecord(refreshed.uri, finalName, actualSize)
        } catch (error: Exception) {
            runCatching { file.delete() }
            throw error
        }
    }
}
