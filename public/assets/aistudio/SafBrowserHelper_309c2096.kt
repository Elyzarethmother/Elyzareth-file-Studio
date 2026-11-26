package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile

data class SafFileInfo(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long?,
    val mimeType: String?,
    val isDirectory: Boolean = false
)

object SafBrowserHelper {
    fun queryFileInfo(context: Context, uri: Uri): SafFileInfo {
        val resolver = context.applicationContext.contentResolver
        var name: String? = null
        var size: Long? = null
        val mime = runCatching { resolver.getType(uri) }.getOrNull()

        runCatching {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                        name = cursor.getString(nameIndex)?.takeIf(String::isNotBlank)
                    }
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        cursor.getLong(sizeIndex).takeIf { it >= 0L }?.let { size = it }
                    }
                }
            }
        }

        val doc = runCatching { DocumentFile.fromSingleUri(context.applicationContext, uri) }
            .getOrNull()
        val isDirectory = mime == DocumentsContract.Document.MIME_TYPE_DIR ||
            runCatching { doc?.isDirectory == true }.getOrDefault(false)

        if (name == null) {
            name = runCatching { doc?.name?.takeIf(String::isNotBlank) }.getOrNull()
        }
        if (size == null && !isDirectory) {
            val fallbackSize = runCatching { doc?.length() }.getOrNull()
            size = fallbackSize?.takeIf { it >= 0L }
        }

        return SafFileInfo(
            uri = uri,
            displayName = name ?: uri.lastPathSegment?.takeIf(String::isNotBlank) ?: "unknown_file",
            sizeBytes = if (isDirectory) null else size,
            mimeType = if (isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else mime,
            isDirectory = isDirectory
        )
    }

    fun listTreeContents(context: Context, treeUri: Uri): List<SafFileInfo> {
        val app = context.applicationContext
        val tree = runCatching { DocumentFile.fromTreeUri(app, treeUri) }.getOrNull()
            ?: return emptyList()
        val children = runCatching { tree.listFiles() }.getOrNull() ?: return emptyList()

        return children.mapNotNull { child ->
            runCatching {
                val uri = child.uri
                val isDirectory = child.isDirectory
                val reportedSize = if (isDirectory) null else child.length().takeIf { it >= 0L }
                SafFileInfo(
                    uri = uri,
                    displayName = child.name?.takeIf(String::isNotBlank)
                        ?: uri.lastPathSegment?.takeIf(String::isNotBlank)
                        ?: "unnamed",
                    sizeBytes = reportedSize,
                    mimeType = if (isDirectory) {
                        DocumentsContract.Document.MIME_TYPE_DIR
                    } else {
                        child.type
                    },
                    isDirectory = isDirectory
                )
            }.getOrNull()
        }
    }
}
