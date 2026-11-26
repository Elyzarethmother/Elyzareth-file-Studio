# ELYZARETH FILE UTILITY V1.0 — FINAL IMPLEMENTATION HANDOVER TO ARGON 4
**Governing Rules:** Implementation Contract v1.1 | Existing GitHub Architecture Authoritative | UI & Scope Frozen

Inspect the relevant existing repository files while implementing—do NOT conduct a separate discovery exercise or ask unnecessary questions. Implement **Engineering Reference Pack Rev 4** and the approved technical corrections against the existing GitHub repository.

### STRICT IMPLEMENTATION BOUNDARY
- **Existing GitHub architecture is authoritative:** Inspect existing package names, Gradle files (`minSdk`, `compileSdk`, `targetSdk`, Kotlin, AGP), manifest declarations, `FileProvider` setup, and existing modules first. Do not replace working code unnecessarily or upgrade dependencies/versions arbitrarily. Distinguish between defects in Rev 4 and defects actually present in the repository.
- **Frozen UI and scope remain unchanged:** Keep the approved Elyzareth File Utility V1.0 dashboard UI, teal branding, section hierarchy, bottom navigation, and mini-player placement above bottom navigation. Keep unimplemented Phase 2 and Phase 3 features clearly gated as **Coming Soon**—no fake functionality or placeholder success messages.
- **Implement, don't invent:** No new features, no UI redesign, no additional modules, no architectural experiments, and no unsolicited improvements. Only apply necessary corrections and integrations.
- **Evidence-based reporting only:** Never mark a feature `PASS` based solely on compilation. Report exact changed files, dependency/manifest changes, build/test evidence, and any unresolved issues.

---

### APPROVED CORRECTIONS TO APPLY WITH REV 4
1. **Media3 Controller Connection (`MusicSessionManager.connectController`):** Use a cancellation-safe continuation pattern supported by the repository's existing `kotlinx.coroutines` version—ensuring single resumption (no double-resume or race on cancellation) and releasing any `ListenableFuture` and any created `MediaController` when cancelled or no longer needed.
2. **Mini-Player State & Ticker:** Keep `MiniPlayerUiState` (`isPlaying`, `isBuffering`, `currentTitle`, `artworkUri`, `positionMs`, `durationMs`), `seekTo`, and clean controller/listener release, and wire a lifecycle-aware position refresh while visible and playing (stopping when paused, hidden, or released).
3. **Storage & PDF Staging:** Keep `FileProvider` mandatory on API < 29 (`Uri.fromFile` prohibited), use actual picker-granted flags for SAF persistable permissions, verify actual persisted names/sizes, and ensure staged PDF/save failures clean up temporary and partial outputs (including any destination partially created in `onCommitTempFile`).
4. **Memory & Cancellation Safety:** Keep two-pass upward power-of-two subsampling, post-decode max-side enforcement, all 8 EXIF orientations, iterative target-KB compression with honest `targetMet`/failure reporting, bitmap cleanup in `finally`, and cooperative cancellation (`currentCoroutineContext().ensureActive()` + `onStepCheck`) without swallowing `CancellationException`.

---

### REQUIRED REPORT AFTER EXECUTION
Return:
1. Changed filenames and brief rationale.
2. Any dependency or manifest changes with exact versions/entries.
3. Build results and test/device findings (marking unverified items honestly rather than claiming PASS from compilation alone).
4. Any unresolved issues or repository conflicts.

---

### ATTACHED TECHNICAL AUDIT SUMMARY (FOR REFERENCE DURING IMPLEMENTATION)
- **Coroutines / Media3 (`FAIL` on raw `tryResume`/`completeResume` token plumbing in Rev 4):** Adapt to the repository's `kotlinx.coroutines` version using a race-free single-resumption pattern and clean `MediaController` / `ListenableFuture` release on cancellation.
- **Image Decoding & Compression (`PARTIAL`):** Keep upward power-of-two subsampling, all 8 EXIF transforms, parameter validation, strict `Bitmap.compress` check, and `finally` bitmap cleanup; benchmark batch execution on 10–30 MB / 20+ image sets on device.
- **Image-to-PDF (`PARTIAL`):** Propagate `onStepCheck` across steps, stage via `writePdfStaged`, ensure `onCommitTempFile` cleans up partial outputs on failure, and verify 10–20 page memory behavior on device.
- **Storage (`PARTIAL`):** Keep `FileProvider` on API < 29 (no `Uri.fromFile`), pass actual granted flags to `takeGrantedFolderPermission`, verify actual saved names/sizes, and test SAF/Downloads edge cases on device.
- **Android 15 Foreground Service & Notifications (`NEEDS REPOSITORY VERIFICATION`):** Verify manifest `mediaPlayback` foreground service declaration, permissions, and runtime notification permission behavior on physical Android 15.

---

### ENGINEERING REFERENCE PACK REV 4 (SOURCE SPECIFICATION)

```text
ELYZARETH FILE UTILITY V1.0
ENGINEERING REFERENCE PACK REV 4 — FINAL TECHNICAL FREEZE
ARGON 4 IMPLEMENTATION HANDOVER
====================================================

STATUS
------
Architecture: FROZEN
UI: FROZEN
Scope: FROZEN
Implementation: AUTHORIZED IN PHASES
Engineering reference: APPROVED WITH THE EXPLICIT CORRECTIONS AND GATES BELOW
Physical-device acceptance: NOT YET VERIFIED

IMPORTANT HANDOVER RULE
-----------------------
This is an engineering reference pack, not a substitute for the actual Android
repository. Argon 4 MUST inspect the existing repository first, preserve its
package names and compatible dependency versions, and integrate these files
without replacing unrelated working code. Do not invent Gradle/AGP/Kotlin
versions. Do not redesign the approved UI.

The code below is organized as separate Kotlin files. Add the correct package
declaration for the inspected repository at the top of each file.

DEPENDENCIES TO VERIFY AGAINST THE EXISTING PROJECT
---------------------------------------------------
- androidx.exifinterface:exifinterface
- androidx.documentfile:documentfile
- androidx.core:core-ktx (FileProvider, ContextCompat)
- androidx.media3:media3-common
- androidx.media3:media3-exoplayer
- androidx.media3:media3-session
- kotlinx-coroutines-core / kotlinx-coroutines-android
- Guava ListenableFuture (normally transitively available through Media3)

Do not upgrade dependencies merely to match this document. Confirm API
availability against the repository's actual Media3 and coroutines versions.

====================================================
FILE 1 — image/ImageCompression.kt
====================================================

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.max

data class CompressionOutcome(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val quality: Int,
    val targetMet: Boolean,
    val reason: String? = null
)

object ExifOrientation {
    fun read(context: Context, uri: Uri): Int =
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

    fun applyAndRecycleSource(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> return source
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    setRotate(180f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    setRotate(90f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    setRotate(270f)
                    postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
                else -> return source
            }
        }
        val transformed = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
        if (transformed !== source && !source.isRecycled) source.recycle()
        return transformed
    }
}

object SafeImageDecoder {
    fun computeInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        if (srcWidth <= 0 || srcHeight <= 0 || reqWidth <= 0 || reqHeight <= 0) return 1
        val maxRatio = max(
            srcWidth.toFloat() / reqWidth.toFloat(),
            srcHeight.toFloat() / reqHeight.toFloat()
        )
        if (maxRatio <= 1f) return 1
        val targetCeil = ceil(maxRatio.toDouble()).toInt()
        var sample = 1
        while (sample < targetCeil) sample *= 2
        return max(1, sample)
    }

    fun decodeBounded(
        context: Context,
        uri: Uri,
        maxSidePx: Int = 2048
    ): Bitmap {
        require(maxSidePx > 0) { "maxSidePx must be positive." }
        val appContext = context.applicationContext
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        appContext.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) {
            "Invalid or unreadable image."
        }

        val exif = ExifOrientation.read(appContext, uri)
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = computeInSampleSize(
                bounds.outWidth, bounds.outHeight, maxSidePx, maxSidePx
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        var decoded: Bitmap? = null
        var oriented: Bitmap? = null
        try {
            decoded = appContext.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: error("Failed to decode bitmap stream.")

            oriented = ExifOrientation.applyAndRecycleSource(decoded, exif)
            if (oriented === decoded) decoded = null

            var longest = max(oriented.width, oriented.height)
            while (longest > maxSidePx) {
                val scale = maxSidePx.toFloat() / longest.toFloat()
                val w = max(1, (oriented.width * scale).toInt())
                val h = max(1, (oriented.height * scale).toInt())
                val resized = Bitmap.createScaledBitmap(oriented, w, h, true)
                if (resized !== oriented && !oriented.isRecycled) oriented.recycle()
                oriented = resized
                longest = max(oriented.width, oriented.height)
            }

            check(max(oriented.width, oriented.height) <= maxSidePx) {
                "Decoded bitmap exceeded maxSidePx ($maxSidePx)."
            }
            val result = oriented
            oriented = null
            return result
        } finally {
            if (decoded != null && !decoded.isRecycled) decoded.recycle()
            if (oriented != null && !oriented.isRecycled) oriented.recycle()
        }
    }
}

object SmartJpegCompressor {
    private fun encodeStrict(bitmap: Bitmap, quality: Int): ByteArray {
        val q = quality.coerceIn(1, 100)
        val out = ByteArrayOutputStream()
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, q, out)) {
            "Bitmap.compress(JPEG, quality=$q) returned false."
        }
        return out.toByteArray()
    }

    fun compressToTargetMaxBytes(
        context: Context,
        uri: Uri,
        maxBytes: Int,
        initialMaxSidePx: Int = 2048,
        minSidePx: Int = 240,
        minQuality: Int = 25,
        onStepCheck: (() -> Unit)? = null
    ): CompressionOutcome {
        require(maxBytes > 0) { "maxBytes must be positive." }
        require(initialMaxSidePx > 0) { "initialMaxSidePx must be positive." }
        require(minSidePx in 64..initialMaxSidePx) {
            "minSidePx must be between 64 and initialMaxSidePx."
        }
        require(minQuality in 10..90) { "minQuality must be in 10..90." }

        val appContext = context.applicationContext
        var bitmap = SafeImageDecoder.decodeBounded(appContext, uri, initialMaxSidePx)
        try {
            while (true) {
                onStepCheck?.invoke()
                var low = minQuality
                var high = 90
                var best: ByteArray? = null
                var bestQuality = minQuality

                while (low <= high) {
                    onStepCheck?.invoke()
                    val mid = (low + high) / 2
                    val candidate = encodeStrict(bitmap, mid)
                    if (candidate.size <= maxBytes) {
                        best = candidate
                        bestQuality = mid
                        low = mid + 1
                    } else {
                        high = mid - 1
                    }
                }

                if (best != null) {
                    return CompressionOutcome(
                        best, bitmap.width, bitmap.height, bestQuality, true
                    )
                }

                onStepCheck?.invoke()
                val longest = max(bitmap.width, bitmap.height)
                if (longest <= minSidePx) {
                    val fallback = encodeStrict(bitmap, minQuality)
                    val met = fallback.size <= maxBytes
                    return CompressionOutcome(
                        bytes = fallback,
                        width = bitmap.width,
                        height = bitmap.height,
                        quality = minQuality,
                        targetMet = met,
                        reason = if (!met) {
                            "Reached minimum dimension ($minSidePx px) and quality " +
                                "($minQuality%) limits without meeting $maxBytes bytes."
                        } else null
                    )
                }

                val nextLongest = max(minSidePx, (longest * 0.85f).toInt())
                val scale = nextLongest.toFloat() / longest.toFloat()
                val nextW = max(1, (bitmap.width * scale).toInt())
                val nextH = max(1, (bitmap.height * scale).toInt())
                val next = Bitmap.createScaledBitmap(bitmap, nextW, nextH, true)
                if (next !== bitmap && !bitmap.isRecycled) bitmap.recycle()
                bitmap = next
            }
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }
}

====================================================
FILE 2 — image/BatchImageProcessor.kt
====================================================

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class BatchProgress(
    val completed: Int,
    val total: Int,
    val currentUri: Uri?,
    val lastResult: Result<CompressionOutcome>? = null,
    val isFinished: Boolean = false
)

class BatchImageProcessor(context: Context) {
    private val appContext = context.applicationContext

    fun processBatch(
        uris: List<Uri>,
        maxBytes: Int,
        initialMaxSidePx: Int = 2048
    ): Flow<BatchProgress> = flow {
        val total = uris.size
        if (total == 0) {
            emit(BatchProgress(0, 0, null, null, isFinished = true))
            return@flow
        }

        for ((index, uri) in uris.withIndex()) {
            val jobContext = currentCoroutineContext()
            jobContext.ensureActive()

            val result = try {
                val outcome = SmartJpegCompressor.compressToTargetMaxBytes(
                    context = appContext,
                    uri = uri,
                    maxBytes = maxBytes,
                    initialMaxSidePx = initialMaxSidePx,
                    onStepCheck = { jobContext.ensureActive() }
                )
                jobContext.ensureActive()
                Result.success(outcome)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Result.failure(error)
            }

            jobContext.ensureActive()
            emit(
                BatchProgress(
                    completed = index + 1,
                    total = total,
                    currentUri = uri,
                    lastResult = result,
                    isFinished = index + 1 == total
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}

====================================================
FILE 3 — pdf/ImageToPdfEngine.kt
====================================================

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.min

data class PdfGenerationReport(
    val bytesWritten: Long,
    val pageCount: Int,
    val allImageTargetsMet: Boolean,
    val missedTargetIndices: List<Int>
)

/**
 * Caller owns outputStream lifecycle. This method writes and flushes but does
 * not close the stream. onStepCheck should throw CancellationException when
 * the coroutine/job is cancelled.
 */
object ImageToPdfEngine {
    fun writePdfFromImages(
        context: Context,
        imageUris: List<Uri>,
        outputStream: OutputStream,
        pageWidthPt: Int = 595,
        pageHeightPt: Int = 842,
        marginPt: Float = 24f,
        perImageMaxBytes: Int = 512 * 1024,
        onStepCheck: (() -> Unit)? = null
    ): PdfGenerationReport {
        require(imageUris.isNotEmpty()) { "At least one image is required." }
        require(pageWidthPt > 0 && pageHeightPt > 0) {
            "Page dimensions must be positive."
        }
        require(marginPt >= 0f) { "marginPt must not be negative." }
        require(
            pageWidthPt - 2f * marginPt >= 1f &&
                pageHeightPt - 2f * marginPt >= 1f
        ) { "Margins leave no drawable page area." }
        require(perImageMaxBytes > 0) { "perImageMaxBytes must be positive." }

        val appContext = context.applicationContext
        val missed = mutableListOf<Int>()
        val document = PdfDocument()

        try {
            imageUris.forEachIndexed { index, uri ->
                onStepCheck?.invoke()
                val compressed = SmartJpegCompressor.compressToTargetMaxBytes(
                    context = appContext,
                    uri = uri,
                    maxBytes = perImageMaxBytes,
                    initialMaxSidePx = 1600,
                    onStepCheck = onStepCheck
                )
                onStepCheck?.invoke()
                if (!compressed.targetMet) missed.add(index)

                val bitmap = BitmapFactory.decodeByteArray(
                    compressed.bytes, 0, compressed.bytes.size
                ) ?: error("Failed to decode compressed image at index $index.")

                // Bitmap cleanup begins immediately after decode. This also
                // covers startPage() failure, not only draw failures.
                try {
                    onStepCheck?.invoke()
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        pageWidthPt, pageHeightPt, index + 1
                    ).create()
                    val page = document.startPage(pageInfo)
                    try {
                        onStepCheck?.invoke()
                        val availableW = pageWidthPt - 2f * marginPt
                        val availableH = pageHeightPt - 2f * marginPt
                        val scale = min(
                            availableW / bitmap.width,
                            availableH / bitmap.height
                        )
                        val drawW = bitmap.width * scale
                        val drawH = bitmap.height * scale
                        val left = marginPt + (availableW - drawW) / 2f
                        val top = marginPt + (availableH - drawH) / 2f
                        page.canvas.drawBitmap(
                            bitmap, null,
                            RectF(left, top, left + drawW, top + drawH),
                            null
                        )
                    } finally {
                        document.finishPage(page)
                    }
                } finally {
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
            }

            onStepCheck?.invoke()
            val counting = NonClosingCountingOutputStream(outputStream)
            document.writeTo(counting)
            outputStream.flush()

            return PdfGenerationReport(
                bytesWritten = counting.bytesWritten,
                pageCount = imageUris.size,
                allImageTargetsMet = missed.isEmpty(),
                missedTargetIndices = missed.toList()
            )
        } finally {
            document.close()
        }
    }

    /**
     * Creates a cache/staging PDF, fsyncs it, then calls onCommitTempFile.
     * The callback MUST synchronously move or copy the temp file to its final
     * destination before returning. The temp file is deleted after callback
     * completion, and on any failure/cancellation.
     */
    fun writePdfStaged(
        context: Context,
        imageUris: List<Uri>,
        stagingDir: File,
        pageWidthPt: Int = 595,
        pageHeightPt: Int = 842,
        marginPt: Float = 24f,
        perImageMaxBytes: Int = 512 * 1024,
        onStepCheck: (() -> Unit)? = null,
        onCommitTempFile: (tempFile: File, report: PdfGenerationReport) -> Unit
    ): PdfGenerationReport {
        check(stagingDir.exists() || stagingDir.mkdirs()) {
            "Unable to prepare PDF staging directory."
        }
        val temp = File.createTempFile("elyzareth_pdf_", ".tmp", stagingDir)
        return try {
            val report = FileOutputStream(temp).use { stream ->
                val result = writePdfFromImages(
                    context = context,
                    imageUris = imageUris,
                    outputStream = stream,
                    pageWidthPt = pageWidthPt,
                    pageHeightPt = pageHeightPt,
                    marginPt = marginPt,
                    perImageMaxBytes = perImageMaxBytes,
                    onStepCheck = onStepCheck
                )
                stream.fd.sync()
                result
            }
            onStepCheck?.invoke()
            onCommitTempFile(temp, report)
            report
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    private class NonClosingCountingOutputStream(
        private val delegate: OutputStream
    ) : OutputStream() {
        var bytesWritten: Long = 0L
            private set

        override fun write(b: Int) {
            delegate.write(b)
            bytesWritten++
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            delegate.write(b, off, len)
            bytesWritten += len
        }

        override fun flush() = delegate.flush()
        override fun close() = Unit
    }
}

====================================================
FILE 4 — storage/FileSaver.kt
====================================================

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
    fun takeGrantedFolderPermission(
        context: Context,
        treeUri: Uri,
        grantedFlags: Int
    ): Boolean {
        val mask = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val flags = grantedFlags and mask
        if (flags == 0) return false
        return try {
            context.applicationContext.contentResolver
                .takePersistableUriPermission(treeUri, flags)
            true
        } catch (_: SecurityException) {
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
            require(!fileProviderAuthority.isNullOrBlank()) {
                "A configured FileProvider authority is mandatory on API 24–28."
            }
            saveLegacyAtomic(
                app, desiredDisplayName, mimeType, bytes, fileProviderAuthority
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
                ?: error("Unable to verify final MediaStore display name.")
            val actualSize = queryActualSize(resolver, uri) ?: bytes.size.toLong()
            return SavedFileRecord(uri, actualName, actualSize)
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
        resolver: android.content.ContentResolver,
        uri: Uri
    ): String? = resolver.query(
        uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (index >= 0) cursor.getString(index) else null
        } else null
    }

    private fun queryActualSize(
        resolver: android.content.ContentResolver,
        uri: Uri
    ): Long? = resolver.query(
        uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            if (index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else null
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

            val contentUri = FileProvider.getUriForFile(app, authority, finalFile)
            return SavedFileRecord(contentUri, finalFile.name, finalFile.length())
        } catch (error: Exception) {
            if (temp.exists()) temp.delete()
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
            val finalName = file.name ?: error("Unable to verify saved SAF display name.")
            SavedFileRecord(file.uri, finalName, bytes.size.toLong())
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }
}

NOTE — STORAGE PERMISSIONS AND FILEPROVIDER
-------------------------------------------
- API 24–28: request WRITE_EXTERNAL_STORAGE at runtime where required by the
  app's target SDK and legacy storage model. Do not request it on API 29+ for
  this Downloads implementation.
- API 33+: request POST_NOTIFICATIONS only if the app's notification UX needs
  notification permission. Media-session/foreground-service notification
  behavior must still comply with the target SDK.
- ACTION_OPEN_DOCUMENT_TREE: pass the result Intent's actual flags to
  takeGrantedFolderPermission(). Do not fabricate read/write grants.
- FileProvider authority must be `${applicationId}.fileprovider` or another
  exact configured authority. Add a provider and paths XML matching the real
  package and file locations. Do not use Uri.fromFile().
- MediaStore name pre-query is not an atomic uniqueness lock. Concurrent saves
  can race; inspect the returned persisted display name and report it. If the
  provider changes a name, return the actual name, not the requested name.

Example manifest fragment (merge with the existing manifest; do not replace it):

<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

Example res/xml/file_paths.xml (scope paths narrowly; adjust to actual app needs):

<paths>
    <external-path name="public_downloads" path="Download/" />
    <cache-path name="shared_cache" path="." />
</paths>

====================================================
FILE 5 — audio/MusicPlayerService.kt
====================================================

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

data class MiniPlayerUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentTitle: String? = null,
    val artworkUri: Uri? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

class MusicPlayerService : MediaSessionService() {
    private var session: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        val attributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val exo = ExoPlayer.Builder(this)
            .setAudioAttributes(attributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player = exo
        session = MediaSession.Builder(this, exo).build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val activePlayer = player
        if (activePlayer == null ||
            !activePlayer.playWhenReady ||
            activePlayer.mediaItemCount == 0
        ) stopSelf()
    }

    override fun onDestroy() {
        session?.release()
        session = null
        player?.release()
        player = null
        super.onDestroy()
    }
}

object MusicSessionManager {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun connectController(
        context: Context,
        serviceClass: Class<out MediaSessionService> = MusicPlayerService::class.java
    ): MediaController = suspendCancellableCoroutine { continuation ->
        val app = context.applicationContext
        val token = SessionToken(app, ComponentName(app, serviceClass))
        val future: ListenableFuture<MediaController> =
            MediaController.Builder(app, token).buildAsync()

        continuation.invokeOnCancellation {
            MediaController.releaseFuture(future)
        }

        future.addListener({
            try {
                val controller = future.get()
                val token = continuation.tryResume(controller) { _, value, _ ->
                    value.release()
                }
                if (token != null) {
                    continuation.completeResume(token)
                } else {
                    controller.release()
                }
            } catch (error: Exception) {
                val failureToken = continuation.tryResumeWithException(error)
                if (failureToken != null) {
                    continuation.completeResume(failureToken)
                } else {
                    MediaController.releaseFuture(future)
                }
            }
        }, ContextCompat.getMainExecutor(app))
    }

    fun releaseController(controller: MediaController?) {
        controller?.release()
    }
}

class MiniPlayerController(context: Context) {
    private val appContext = context.applicationContext
    private var controller: MediaController? = null

    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = sync()
        override fun onPlaybackStateChanged(playbackState: Int) = sync()
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = sync()
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) = sync()
    }

    private fun sync() {
        val c = controller ?: return
        val item = c.currentMediaItem
        val rawDuration = c.duration
        val duration = if (rawDuration < 0L) 0L else rawDuration
        _uiState.value = MiniPlayerUiState(
            isPlaying = c.isPlaying,
            isBuffering = c.playbackState == Player.STATE_BUFFERING,
            currentTitle = item?.mediaMetadata?.title?.toString(),
            artworkUri = item?.mediaMetadata?.artworkUri,
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = duration
        )
    }

    // Call from a lifecycle-aware ticker while the mini-player is visible.
    // StateFlow alone does not emit position updates continuously.
    fun refreshProgress() = sync()

    suspend fun bind() {
        if (controller != null) {
            sync()
            return
        }
        val connected = MusicSessionManager.connectController(appContext)
        connected.addListener(listener)
        controller = connected
        sync()
    }

    fun setPlaylist(
        items: List<MediaItem>,
        startIndex: Int = 0,
        playWhenReady: Boolean = true
    ) {
        val c = controller ?: return
        require(items.isNotEmpty())
        require(startIndex in items.indices)
        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.playWhenReady = playWhenReady
        sync()
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
        sync()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs.coerceAtLeast(0L))
        sync()
    }

    fun next() {
        controller?.seekToNextMediaItem()
        sync()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
        sync()
    }

    fun release() {
        controller?.removeListener(listener)
        MusicSessionManager.releaseController(controller)
        controller = null
        _uiState.value = MiniPlayerUiState()
    }
}

====================================================
MANIFEST / ANDROID 15 INTEGRATION GATE
====================================================

Merge the following service declaration into AndroidManifest.xml. Keep the
application's existing theme, activities, providers and permissions.

<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application ...>
    <service
        android:name=".audio.MusicPlayerService"
        android:exported="true"
        android:foregroundServiceType="mediaPlayback">
        <intent-filter>
            <action android:name="androidx.media3.session.MediaSessionService" />
        </intent-filter>
    </service>
</application>

Important:
- Confirm service class's fully qualified package name.
- Confirm targetSdk-specific foreground service start restrictions.
- POST_NOTIFICATIONS runtime prompt is separate from foreground-service
  eligibility and notification display requirements.
- Add READ_MEDIA_AUDIO only if the app directly browses shared audio on API 33+;
  SAF selection can avoid broad media-library access.
- For Android 12+ Bluetooth/headset controls, verify the Media3 session
  notification and media button behavior on the actual device.
- Audio focus and headphone-disconnect pause are configured in ExoPlayer.
- Phone-call interruption behavior must be tested, not assumed from compilation.

====================================================
ARGON 4 REQUIRED INTEGRATION INSTRUCTIONS
====================================================

1. Inspect the actual repository before edits:
   - applicationId and package structure
   - minSdk, targetSdk, compileSdk
   - AGP, Gradle, Kotlin versions
   - Compose/UI architecture
   - existing Media3, coroutines, ExifInterface, DocumentFile dependencies
   - existing FileProvider authority and paths
   - current manifest service/permission declarations

2. Preserve the frozen UI:
   - approved dashboard hierarchy, cards, colors, branding and navigation
   - docked mini-player above bottom navigation
   - no redesign and no replacement with a generic control-panel layout
   - all not-yet-implemented Phase 2/3 tiles must show Coming Soon, not fake
     success or non-functional click handlers

3. Integrate code in isolated modules. Keep image, PDF, storage, OCR and audio
   failures isolated. PDF/image processing must not stop background music.

4. Do not call Bitmap.compress() cancellable mid-native-call. The callback
   provides cooperative cancellation before/after each compression attempt.
   Cancellation latency is bounded by the current native bitmap operation.

5. Storage:
   - Enforce FileProvider authority on API 24–28; never fall back to Uri.fromFile.
   - Configure provider paths narrowly.
   - Request legacy WRITE_EXTERNAL_STORAGE only when applicable to the actual
     target SDK/storage model.
   - Pass actual picker-granted flags to takeGrantedFolderPermission.
   - MediaStore duplicate pre-check is not an atomic lock; return actual name.
   - Test SAF write failures, provider permission and cleanup.

6. PDF:
   - onStepCheck must be wired to the coroutine Job's ensureActive().
   - onCommitTempFile must synchronously commit the staged file. If the final
     destination is SAF/MediaStore, copy through its ContentResolver stream
     and only report success after complete write/close/verification.
   - Temp file is deleted after callback; callback must not retain its path.
   - Large PDF memory/output behavior must be measured on device.
   - Note: PdfDocument retains page data until writeTo; one-page-at-a-time
     drawing does not prove constant total memory usage.

7. Mini-player:
   - StateFlow position does not tick by itself. Add lifecycle-aware periodic
     refresh while the mini-player is visible (e.g. 250–500 ms) and stop ticker
     when not needed.
   - Bind/release controller with screen/service lifecycle.
   - Verify continuation cancellation APIs compile against installed
     kotlinx-coroutines version. The tryResume cancellation callback must
     release a controller that cannot be delivered.
   - Confirm notification, lock-screen controls, audio focus and interruption.

8. Build and test after each phase. Do not mark a feature PASS because the
   project compiles. Report PASS / PARTIAL / FAIL / NOT IMPLEMENTED with
   evidence and exact test environment.

====================================================
IMPLEMENTATION PHASES — CONTRACT V1.1
====================================================

PHASE 1
- Dashboard/navigation and frozen UI
- File Manager and SAF import
- Coming Soon gates for deferred tools

PHASE 2
- Image Studio: resize/compress, batch, format conversion, preview/report
- ID Photo Maker
- Image-to-PDF

PHASE 3
- Mini-player shell and Media3 background playback

PHASE 4
- PDF tools and OCR

PHASE 5
- Gemini Help Assistant (guidance only; no automatic uploads or destructive
  actions)

PHASE 6
- Integration testing, APK packaging and acceptance report

Deferred/Coming Soon:
- TXT/PDF/DOCX/XLSX/PPTX conversion where not yet implemented
- PDF long-image
- Slideshow Studio
- Smart Erase, watermark, digital signature, restoration, translation
- Gemini structured actions

====================================================
ACCEPTANCE TEST MATRIX — PHYSICAL AND AUTOMATED
====================================================

IMAGE
- 10–30 MB night photo: decode, process, preview and save without OOM.
- 20+ image batch: progress, cancellation, partial failures and cleanup.
- EXIF orientations 1–8: verify visual orientation including mirrored cases.
- Compression presets: 50–100 KB, 100–200 KB, 200–500 KB, 500 KB–1 MB.
- Impossible 8 KB target: report targetMet=false and reason; never claim success.
- Confirm original source files remain byte-for-byte unchanged.

PDF
- 10–20 images: verify page count, order, readability and output size.
- Cancel during compression, decode, page draw and before final write.
- Simulate output write failure and commit failure; no partial final file.
- Confirm temporary file cleanup.
- Confirm per-image target misses are surfaced to the user.

STORAGE
- API 29+ MediaStore Downloads: actual persisted name and size verified.
- SAF: read-only grant, write-only grant, read/write grant, revoked grant.
- API 24–28: permission denial, successful save, FileProvider URI access.
- Duplicate names and concurrent save collision behavior.
- Confirm no raw file:// URI escapes the app.

AUDIO — ANDROID 15 PHYSICAL DEVICE
- Playback continues while navigating between app screens.
- Playback continues while image/PDF work runs without memory pressure.
- Screen off, notification controls, lock screen, next/previous and seek.
- Headphone disconnect pauses playback.
- Phone-call interruption and audio focus regain.
- Notification permission denied/granted behavior.
- Service teardown and controller release without leaks.

UI
- Compare against approved frozen UI reference.
- Mini-player remains docked above bottom navigation.
- Coming Soon items do not execute fake actions.
- No layout redesign.

====================================================
FINAL SIGN-OFF BOUNDARY
====================================================

This Rev 4 pack resolves the identified Rev 3 design gaps at reference level,
subject to the corrections and integration notes in this document. It does
NOT establish that the app compiles, that the existing repository is correctly
integrated, or that Android 15 device behavior has passed.

GPT REVIEW STATUS:
- Reference architecture: ACCEPTABLE FOR ARGON IMPLEMENTATION.
- PDF: cooperative checks and staging defined; callback contract is mandatory.
- Storage: FileProvider is mandatory on legacy branch; SAF and actual name
  verification defined.
- Media3: cancellation-safe handoff intended; compile against actual coroutine
  version and test lifecycle. Position ticker must be implemented by caller.
- Physical device: PENDING.
- Production release: NOT SIGNED OFF.

Argon 4 must return:
1. Changed-file list and rationale.
2. Dependency/manifest changes with exact versions.
3. Build output and test results.
4. Unit/instrumentation test evidence.
5. Android 15 physical-device test matrix with PASS/PARTIAL/FAIL.
6. Debug APK; release APK only when signing configuration is available.
7. Any deviations from this frozen contract, explicitly listed.

END OF ENGINEERING REFERENCE PACK REV 4
```