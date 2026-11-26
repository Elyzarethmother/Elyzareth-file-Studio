package com.example.scanner

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.OutputStream

/** Builds a multi-page PDF from captured JPEG pages. All bitmap work is bounded to page size. */
object ScannerPdfExporter {
    fun writePdf(contentResolver: ContentResolver, pageUris: List<Uri>, output: OutputStream) {
        require(pageUris.isNotEmpty()) { "Capture at least one page before exporting." }
        val document = PdfDocument()
        try {
            pageUris.forEachIndexed { index, uri ->
                val bitmap = contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: throw IllegalStateException("Unable to open scanned page ${index + 1}.")
                try {
                    val maxSide = 2200
                    val scale = minOf(1f, maxSide.toFloat() / maxOf(bitmap.width, bitmap.height))
                    val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
                    val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
                    val pageBitmap = if (width == bitmap.width && height == bitmap.height) bitmap
                        else Bitmap.createScaledBitmap(bitmap, width, height, true)
                    try {
                        val pageInfo = PdfDocument.PageInfo.Builder(width, height, index + 1).create()
                        val page = document.startPage(pageInfo)
                        page.canvas.drawBitmap(pageBitmap, 0f, 0f, null)
                        document.finishPage(page)
                    } finally {
                        if (pageBitmap !== bitmap) pageBitmap.recycle()
                    }
                } finally { bitmap.recycle() }
            }
            document.writeTo(output)
        } finally { document.close() }
    }
}
