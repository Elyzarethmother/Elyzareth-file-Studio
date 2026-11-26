package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.scanner.DocumentScannerScreen

@Composable
fun ElyzarethApp(
    viewModel: ElyzarethAppViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDocumentScanner by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Restore the previously selected SAF folder after process/app recreation.
    LaunchedEffect(context) {
        viewModel.restoreTreeUri(context)
    }

    val sampleContentText = "Elyzareth File Utility V1.0 — Stage 2 SAF Sample Document Content.\n" +
        "Verified timestamp: ${System.currentTimeMillis()}"
    val sampleFileName = "elyzareth_sample_document.txt"
    val sampleMimeType = "text/plain"

    val saveSampleFile: () -> Unit = {
        try {
            val record = FileSaver.saveToDownloads(
                context = context,
                desiredDisplayName = sampleFileName,
                mimeType = sampleMimeType,
                bytes = sampleContentText.toByteArray(Charsets.UTF_8)
            )
            viewModel.recordSavedFile(
                title = "Saved to Downloads",
                fileName = record.displayName,
                sizeBytes = record.sizeBytes,
                uri = record.uri
            )
        } catch (e: Exception) {
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

    val legacyWritePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveSampleFile()
        } else {
            viewModel.showComingSoonNotice(
                ComingSoonNotice(
                    id = "storage_permission_denied",
                    title = "Storage Permission Denied",
                    stageLabel = "Storage Permission",
                    description = "Android storage permission was denied. The file was not saved."
                )
            )
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultIntent = result.data
        val documentUri = resultIntent?.data
        if (result.resultCode == Activity.RESULT_OK && documentUri != null) {
            try {
                FileSaver.takeGrantedDocumentReadPermission(
                    context = context,
                    documentUri = documentUri,
                    resultFlags = resultIntent.flags
                )
                val info = SafBrowserHelper.queryFileInfo(context, documentUri)
                viewModel.selectFile(info)
                viewModel.selectTab(BottomNavTab.HISTORY)
            } catch (e: Exception) {
                viewModel.showComingSoonNotice(
                    ComingSoonNotice(
                        id = "document_read_error",
                        title = "Unable to Read Document",
                        stageLabel = "File Access Error",
                        description = e.localizedMessage ?: "The selected document could not be inspected."
                    )
                )
            }
        }
    }

    val documentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultIntent = result.data
        val treeUri = resultIntent?.data
        if (result.resultCode == Activity.RESULT_OK && treeUri != null) {
            val persisted = FileSaver.takeGrantedFolderPermission(
                context = context,
                treeUri = treeUri,
                resultFlags = resultIntent.flags
            )
            if (persisted) {
                try {
                    viewModel.updateTreeUri(context, treeUri)
                    viewModel.selectTab(BottomNavTab.HISTORY)
                } catch (e: Exception) {
                    viewModel.showComingSoonNotice(
                        ComingSoonNotice(
                            id = "folder_load_error",
                            title = "Unable to Load Folder",
                            stageLabel = "SAF Folder Error",
                            description = e.localizedMessage ?: "The selected folder could not be read."
                        )
                    )
                }
            } else {
                viewModel.showComingSoonNotice(
                    ComingSoonNotice(
                        id = "folder_permission_error",
                        title = "Folder Permission Not Persisted",
                        stageLabel = "SAF Permission Error",
                        description = "Android did not return a persistable read/write grant. Select the folder again using the system document picker."
                    )
                )
            }
        }
    }

    val onOpenDocument: () -> Unit = {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // Keep the picker unrestricted. Inspect the selected document's MIME
            // type after selection rather than filtering provider results here.
            type = "*/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        documentPickerLauncher.launch(intent)
    }

    val onOpenDocumentTree: () -> Unit = {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
        documentTreeLauncher.launch(intent)
    }

    val onSaveSampleFile: () -> Unit = {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            legacyWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            saveSampleFile()
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
                    onTabSelected = viewModel::selectTab
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
            if (showDocumentScanner) {
                DocumentScannerScreen(onBack = { showDocumentScanner = false })
            } else {
            DashboardHeader(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::selectCategory,
                onShowComingSoon = viewModel::showComingSoonNotice
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.selectedTab) {
                    BottomNavTab.HOME -> {
                        DashboardScreen(
                            selectedCategory = uiState.selectedCategory,
                            onToolClicked = { item ->
                                if (item.id == "tool_file_manager") {
                                    viewModel.selectTab(BottomNavTab.HISTORY)
                                } else if (item.id == "tool_document_scanner") {
                                    showDocumentScanner = true
                                } else {
                                    viewModel.onToolClicked(item)
                                }
                            },
                            onShowComingSoon = viewModel::showComingSoonNotice
                        )
                    }
                    BottomNavTab.HISTORY -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            FileManagerScreen(
                                currentTreeUri = uiState.currentTreeUri,
                                treeFiles = uiState.treeFiles,
                                selectedFile = uiState.selectedFile,
                                onOpenDocumentPicker = onOpenDocument,
                                onOpenDocumentTree = onOpenDocumentTree,
                                onSelectFile = viewModel::selectFile,
                                onSaveSampleFile = onSaveSampleFile,
                                onBackToHome = { viewModel.selectTab(BottomNavTab.HOME) }
                            )
                        }
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
