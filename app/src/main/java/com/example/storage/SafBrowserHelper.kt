package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile

/**
 * Providers may omit SIZE. Null means unknown; zero means a known empty file.
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
        var sizeBytes: Long? = null
        val mimeType = app.contentResolver.getType(uri)

        app.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    sizeBytes = cursor.getLong(sizeIndex).takeIf { it >= 0L }
                }
            }
        }

        // DocumentFile.length() returning zero can mean "unknown"; only use
        // positive values as a fallback when the provider omitted SIZE.
        if (sizeBytes == null && document?.isFile == true) {
            document.length().takeIf { it > 0L }?.let { sizeBytes = it }
        }

        return SafFileInfo(
            uri = uri,
            displayName = name,
            sizeBytes = sizeBytes,
            mimeType = mimeType,
            isDirectory = document?.isDirectory == true
        )
    }

    fun listTreeContents(context: Context, treeUri: Uri): List<SafFileInfo> {
        val app = context.applicationContext
        val directory = DocumentFile.fromTreeUri(app, treeUri) ?: return emptyList()
        return directory.listFiles().map { child ->
            val info = queryFileInfo(app, child.uri)
            info.copy(
                displayName = child.name ?: info.displayName,
                isDirectory = child.isDirectory,
                sizeBytes = if (child.isDirectory) null else info.sizeBytes
            )
        }
    }
}
