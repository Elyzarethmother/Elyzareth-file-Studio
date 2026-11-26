package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BottomNavTab
import com.example.model.ComingSoonNotice
import com.example.storage.FileSaver
import com.example.storage.SafBrowserHelper
import com.example.ui.components.BottomNavigationBar
import com.example.ui.components.ComingSoonDialog
import com.example.ui.components.DashboardHeader
import com.example.ui.components.MiniPlayerPlaceholder
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PresetsPlaceholderScreen
import com.example.ui.screens.SettingsPlaceholderScreen
import com.example.ui.theme.DashboardBackground
import com.example.ui.theme.TealHeaderBottom

private class RawOpenDocumentContract : ActivityResultContract<Array<String>, Intent?>() {
    override fun createIntent(context: Context, input: Array<String>): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, input)
            type = "*/*"
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? =
        if (resultCode == Activity.RESULT_OK) intent else null
}

private class RawOpenDocumentTreeContract : ActivityResultContract<Uri?, Intent?>() {
    override fun createIntent(context: Context, input: Uri?): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            if (input != null) {
                putExtra("android.provider.extra.INITIAL_URI", input)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? =
        if (resultCode == Activity.RESULT_OK) intent else null
}

@Composable
fun ElyzarethApp(
    viewModel: ElyzarethAppViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showFileManagerOverlay by remember { mutableStateOf(false) }

    val sampleContentText = "Elyzareth File Utility V1.0 — Stage 2 SAF Sample Document Content.\n" +
        "Verified timestamp: ${System.currentTimeMillis()}"
    val sampleFileName = "elyzareth_sample_document.txt"
    val sampleMimeType = "text/plain"

    fun executeSave(targetTreeUri: Uri?) {
        val bytes = sampleContentText.toByteArray(Charsets.UTF_8)
        try {
            val record = if (targetTreeUri != null) {
                FileSaver.saveToTreeUri(
                    context = context,
                    treeUri = targetTreeUri,
                    desiredDisplayName = sampleFileName,
                    mimeType = sampleMimeType,
                    bytes = bytes
                )
            } else {
                FileSaver.saveToDownloads(
                    context = context,
                    desiredDisplayName = sampleFileName,
                    mimeType = sampleMimeType,
                    bytes = bytes
                )
            }
            val destinationLabel = if (targetTreeUri != null) {
                "Saved to SAF Folder"
            } else {
                "Saved to Downloads"
            }
            viewModel.recordSavedFile(
                title = destinationLabel,
                fileName = record.displayName,
                sizeBytes = record.sizeBytes,
                uri = record.uri
            )
        } catch (e: Exception) {
            viewModel.recordHistory(
                title = "File Save Failed",
                details = e.localizedMessage ?: "Unknown storage error."
            )
            viewModel.showComingSoonNotice(
                ComingSoonNotice(
                    id = "save_error",
                    title = "File Save Failed",
                    stageLabel = "Storage Error",
                    description = e.localizedMessage ?: "Unknown storage error."
                )
            )
        }
    }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            executeSave(targetTreeUri = null)
        } else {
            viewModel.recordHistory(
                title = "Legacy Storage Permission Denied",
                details = "WRITE_EXTERNAL_STORAGE required for API 24–28 Downloads save."
            )
            viewModel.showComingSoonNotice(
                ComingSoonNotice(
                    id = "legacy_perm_denied",
                    title = "Permission Required",
                    stageLabel = "API 24–28 Storage",
                    description = "Storage permission was not granted for saving to Downloads."
                )
            )
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = RawOpenDocumentContract()
    ) { resultIntent: Intent? ->
        val uri = resultIntent?.data
        if (uri != null) {
            val info = SafBrowserHelper.queryFileInfo(context, uri)
            viewModel.selectFile(info)
        } else {
            viewModel.recordHistory(
                title = "Document Selection Cancelled",
                details = "No document URI returned."
            )
        }
    }

    val documentTreeLauncher = rememberLauncherForActivityResult(
        contract = RawOpenDocumentTreeContract()
    ) { resultIntent: Intent? ->
        val treeUri = resultIntent?.data
        if (treeUri != null) {
            val grantedFlags = FileSaver.extractPersistableGrantFlags(resultIntent)
            val persisted = FileSaver.takeGrantedFolderPermission(context, treeUri, grantedFlags)
            if (persisted) {
                viewModel.onFolderPermissionGranted(context, treeUri, grantedFlags)
            } else {
                viewModel.recordHistory(
                    title = "SAF Folder Permission Not Persisted",
                    details = "Folder selected (${treeUri.lastPathSegment ?: treeUri}), but persistable grant failed or had no read/write flags.",
                    uri = treeUri
                )
            }
        } else {
            viewModel.recordHistory(
                title = "Folder Selection Cancelled",
                details = "No folder URI returned."
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DashboardBackground)
            ) {
                MiniPlayerPlaceholder(
                    onShowComingSoon = viewModel::showComingSoonNotice
                )
                BottomNavigationBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { tab ->
                        showFileManagerOverlay = false
                        viewModel.selectTab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TealHeaderBottom)
                .padding(innerPadding)
        ) {
            DashboardHeader(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    showFileManagerOverlay = false
                    viewModel.selectCategory(category)
                },
                onShowComingSoon = viewModel::showComingSoonNotice
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (showFileManagerOverlay && uiState.selectedTab == BottomNavTab.HOME) {
                    FileManagerScreen(
                        currentTreeUri = uiState.currentTreeUri,
                        treeFiles = uiState.treeFiles,
                        selectedFile = uiState.selectedFile,
                        onOpenDocumentPicker = { documentPickerLauncher.launch(arrayOf("*/*")) },
                        onOpenDocumentTree = { documentTreeLauncher.launch(uiState.currentTreeUri) },
                        onSelectFile = viewModel::selectFile,
                        onSaveSampleFile = {
                            val activeTree = uiState.currentTreeUri
                            if (activeTree != null) {
                                executeSave(targetTreeUri = activeTree)
                            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    executeSave(targetTreeUri = null)
                                } else {
                                    legacyPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            } else {
                                executeSave(targetTreeUri = null)
                            }
                        },
                        onBackToHome = { showFileManagerOverlay = false }
                    )
                } else {
                    when (uiState.selectedTab) {
                        BottomNavTab.HOME -> {
                            DashboardScreen(
                                selectedCategory = uiState.selectedCategory,
                                onToolClicked = { item ->
                                    if (item.id == "tool_file_manager" || item.id.contains("file_manager")) {
                                        showFileManagerOverlay = true
                                    } else {
                                        viewModel.onToolClicked(item)
                                    }
                                },
                                onShowComingSoon = viewModel::showComingSoonNotice
                            )
                        }
                        BottomNavTab.HISTORY -> {
                            HistoryScreen(
                                historyItems = uiState.historyItems,
                                onBackToHome = { viewModel.selectTab(BottomNavTab.HOME) }
                            )
                        }
                        BottomNavTab.PRESETS -> {
                            PresetsPlaceholderScreen(
                                onBackToHome = { viewModel.selectTab(BottomNavTab.HOME) }
                            )
                        }
                        BottomNavTab.SETTINGS -> {
                            SettingsPlaceholderScreen(
                                onBackToHome = { viewModel.selectTab(BottomNavTab.HOME) }
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.activeComingSoonNotice?.let { notice ->
        ComingSoonDialog(
            notice = notice,
            onDismiss = viewModel::dismissComingSoonNotice
        )
    }
}
