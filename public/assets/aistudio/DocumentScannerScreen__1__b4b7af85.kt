package com.example.scanner

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun DocumentScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var capture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val pages = remember { mutableStateListOf<Uri>() }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            busy = true
            scope.launch {
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri, "w")?.use { out ->
                            ScannerPdfExporter.writePdf(context.contentResolver, pages.toList(), out)
                        } ?: error("Unable to create PDF output stream.")
                    }
                }
                busy = false
                message = result.fold({ "PDF saved successfully." }, { "PDF export failed: ${it.localizedMessage ?: "Unknown error"}" })
            }
        }
    }

    LaunchedEffect(Unit) { if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    DisposableEffect(hasPermission, previewView, lifecycleOwner) {
        if (hasPermission && previewView != null) {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            val listener = Runnable {
                runCatching {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView!!.surfaceProvider) }
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setJpegQuality(95)
                        .build()
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    capture = imageCapture
                }.onFailure { message = "Camera unavailable: ${it.localizedMessage ?: "Check camera access."}" }
            }
            providerFuture.addListener(listener, executor)
            onDispose { runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }; capture = null }
        } else onDispose { }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
            Text("Document Scanner", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text("${pages.size} pages", style = MaterialTheme.typography.labelLarge)
        }
        if (hasPermission) {
            AndroidView(
                factory = { ctx -> PreviewView(ctx).also { it.scaleType = PreviewView.ScaleType.FILL_CENTER; previewView = it } },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            )
        } else {
            Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Camera permission is required to scan documents.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Grant camera permission") }
            }
        }
        if (pages.isNotEmpty()) {
            LazyRow(contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(pages) { index, _ ->
                    AssistChip(onClick = { }, label = { Text("Page ${index + 1}") }, trailingIcon = {
                        IconButton(onClick = { pages.removeAt(index) }) { Icon(Icons.Default.Delete, contentDescription = "Remove page ${index + 1}") }
                    })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { pages.clear(); message = "All pages removed." }, enabled = pages.isNotEmpty() && !busy, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Clear") }
            FilledTonalButton(
                onClick = {
                    val imageCapture = capture ?: return@FilledTonalButton
                    busy = true
                    val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture.takePicture(options, executor, object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                            pages.add(Uri.fromFile(file)); busy = false; message = "Page ${pages.size} captured."
                        }
                        override fun onError(exception: ImageCaptureException) { busy = false; message = "Capture failed: ${exception.localizedMessage}"; file.delete() }
                    })
                }, enabled = hasPermission && capture != null && !busy, modifier = Modifier.weight(1.2f)
            ) { Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(6.dp)); Text("Capture") }
            Button(onClick = { pdfLauncher.launch("Elyzareth_Scan_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.pdf") }, enabled = pages.isNotEmpty() && !busy, modifier = Modifier.weight(1f)) { Icon(Icons.Default.PictureAsPdf, null); Spacer(Modifier.width(4.dp)); Text("PDF") }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        message?.let { Text(it, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall) }
        Text("Capture pages in order, remove unwanted pages, then export one PDF. Images remain in app cache until cleared by Android.", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
