package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.model.BottomNavTab
import com.example.model.ComingSoonNotice
import com.example.model.HistoryItem
import com.example.model.ToolCategory
import com.example.model.UtilityToolItem
import com.example.storage.SafBrowserHelper
import com.example.storage.SafFileInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ElyzarethStage2UiState(
    val selectedTab: BottomNavTab = BottomNavTab.HOME,
    val selectedCategory: ToolCategory = ToolCategory.ALL,
    val activeComingSoonNotice: ComingSoonNotice? = null,
    val currentTreeUri: Uri? = null,
    val treeFiles: List<SafFileInfo> = emptyList(),
    val selectedFile: SafFileInfo? = null,
    val historyItems: List<HistoryItem> = emptyList()
)

class ElyzarethAppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ElyzarethStage2UiState())
    val uiState: StateFlow<ElyzarethStage2UiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())

    fun selectTab(tab: BottomNavTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectCategory(category: ToolCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedTab = BottomNavTab.HOME
            )
        }
    }

    fun onToolClicked(item: UtilityToolItem) {
        if (item.id == "tool_file_manager" || item.id.contains("file_manager")) {
            _uiState.update { it.copy(selectedTab = BottomNavTab.HISTORY) }
            return
        }

        _uiState.update {
            it.copy(
                activeComingSoonNotice = ComingSoonNotice(
                    id = item.id,
                    title = item.title.replace('\n', ' '),
                    stageLabel = item.stageLabel,
                    description = item.comingSoonDescription
                )
            )
        }
    }

    fun showComingSoonNotice(notice: ComingSoonNotice) {
        _uiState.update { it.copy(activeComingSoonNotice = notice) }
    }

    fun dismissComingSoonNotice() {
        _uiState.update { it.copy(activeComingSoonNotice = null) }
    }

    fun recordHistory(title: String, details: String, sizeBytes: Long? = null, uri: Uri? = null) {
        val newItem = HistoryItem(
            id = "hist_${System.currentTimeMillis()}_${(0..999).random()}",
            operationTitle = title,
            details = details,
            timestampFormatted = dateFormat.format(Date()),
            sizeBytes = sizeBytes,
            uriString = uri?.toString()
        )
        _uiState.update {
            it.copy(historyItems = listOf(newItem) + it.historyItems)
        }
    }

    fun onFolderPermissionGranted(context: Context, treeUri: Uri, grantedFlags: Int) {
        val files = SafBrowserHelper.listTreeContents(context, treeUri)
        _uiState.update {
            it.copy(
                currentTreeUri = treeUri,
                treeFiles = files
            )
        }
        recordHistory(
            title = "SAF Folder Permission Granted (flags=$grantedFlags)",
            details = "Persisted folder: ${treeUri.lastPathSegment ?: treeUri}",
            uri = treeUri
        )
    }

    fun selectFile(file: SafFileInfo) {
        _uiState.update { it.copy(selectedFile = file) }
        recordHistory(
            title = if (file.isDirectory) "SAF Folder Inspected" else "File Selected & Inspected",
            details = file.displayName,
            sizeBytes = file.sizeBytes,
            uri = file.uri
        )
    }

    fun recordSavedFile(title: String, fileName: String, sizeBytes: Long, uri: Uri) {
        recordHistory(
            title = title,
            details = fileName,
            sizeBytes = sizeBytes,
            uri = uri
        )
    }
}
