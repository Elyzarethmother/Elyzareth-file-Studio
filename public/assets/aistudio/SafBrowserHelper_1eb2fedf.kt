package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile

/**
 * sizeBytes is nullable because some DocumentsProviders do not expose a size.
 * A missing size must not be represented as zero (which looks like an empty file).
 */
data class SafFileInfo(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long?,
    val mimeType: String?,
    val isDirectory: Boolean = false
)

object SafBrowserHelper {
    fun queryFileInfo(context: Context, uri: Uri): SafFileInfo {
        val app = context.applicationContext
        val document = DocumentFile.fromSingleUri(app, uri)
        var name = document?.name ?: "unknown_file"
        var size: Long? = null
        val mime = app.contentResolver.getType(uri)

        app.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex).takeIf { it >= 0L }
                }
            }
        }

        return SafFileInfo(
            uri = uri,
            displayName = name,
            sizeBytes = size,
            mimeType = mime,
            isDirectory = document?.isDirectory == true
        )
    }

    fun listTreeContents(context: Context, treeUri: Uri): List<SafFileInfo> {
        val app = context.applicationContext
        val directory = DocumentFile.fromTreeUri(app, treeUri)
            ?: return emptyList()
        return directory.listFiles().map { child ->
            queryFileInfo(app, child.uri).copy(
                displayName = child.name ?: "unnamed",
                isDirectory = child.isDirectory
            )
        }
    }
}
