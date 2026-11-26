package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.example.ui.screens.PresetsPlaceholderScreen
import com.example.ui.screens.SettingsPlaceholderScreen
import com.example.ui.theme.DashboardBackground
import com.example.ui.theme.TealHeaderBottom

@Composable
fun ElyzarethApp(
    viewModel: ElyzarethAppViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    // OpenDocument uses ACTION_OPEN_DOCUMENT (the system document picker), not the
    // Photo Picker or GetContent. A single wildcard avoids provider-specific ZIP MIME
    // filtering; the selected document's actual MIME type is inspected after selection.
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val info = SafBrowserHelper.queryFileInfo(context, uri)
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

    // Capture the actual result Intent so SAF persistence uses only permissions
    // granted by Android for this exact tree URI.
    val documentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { resultIntent ->
                resultIntent.data?.let { treeUri ->
                    val grantedFlags = resultIntent.flags and (
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    val persisted = FileSaver.takeGrantedFolderPermission(
                        context = context,
                        treeUri = treeUri,
                        grantedFlags = grantedFlags
                    )
                    if (persisted) {
                        viewModel.updateTreeUri(context, treeUri)
                        viewModel.selectTab(BottomNavTab.HISTORY)
                    } else {
                        viewModel.showComingSoonNotice(
                            ComingSoonNotice(
                                id = "folder_permission_error",
                                title = "Folder Access Not Granted",
                                stageLabel = "SAF Permission Error",
                                description = "Android did not grant a persistable read/write permission for this folder. Please select the folder again."
                            )
                        )
                    }
                }
            }
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
                    id = "legacy_storage_permission_denied",
                    title = "Storage Permission Required",
                    stageLabel = "Storage Permission",
                    description = "Android storage permission was denied. The sample file was not saved."
                )
            )
        }
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
                                onOpenDocumentPicker = {
                                    documentPickerLauncher.launch(arrayOf("*/*"))
                                },
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

    uiState.activeComingSoonNotice?.let { notice ->
        ComingSoonDialog(
            notice = notice,
            onDismiss = viewModel::dismissComingSoonNotice
        )
    }
}
