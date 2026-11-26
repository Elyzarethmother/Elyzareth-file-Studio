package com.example.ui

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
        // If File Manager card is tapped, switch to HISTORY tab or File Manager view
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

    fun updateTreeUri(context: android.content.Context, treeUri: Uri) {
        val appContext = context.applicationContext
        val files = SafBrowserHelper.listTreeContents(appContext, treeUri)

        // Persist the URI string separately from Android's persisted URI grant.
        // The grant preserves access; this preference lets the app find it again.
        appContext.getSharedPreferences(TREE_PREFS, android.content.Context.MODE_PRIVATE)
            .edit()
            .putString(TREE_URI_KEY, treeUri.toString())
            .apply()

        _uiState.update {
            it.copy(
                currentTreeUri = treeUri,
                treeFiles = files
            )
        }
        recordHistory(
            title = "SAF Folder Granted",
            details = "Accessed tree URI: ${treeUri.lastPathSegment ?: treeUri}",
            uri = treeUri
        )
    }

    /** Restore the last selected folder only while its persisted read/write grant exists. */
    fun restoreTreeUri(context: android.content.Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(
            TREE_PREFS,
            android.content.Context.MODE_PRIVATE
        )
        val savedUri = prefs.getString(TREE_URI_KEY, null)?.let(Uri::parse) ?: return
        val permission = appContext.contentResolver.persistedUriPermissions
            .firstOrNull { it.uri == savedUri && it.isReadPermission && it.isWritePermission }

        if (permission == null) {
            prefs.edit().remove(TREE_URI_KEY).apply()
            _uiState.update { it.copy(currentTreeUri = null, treeFiles = emptyList()) }
            return
        }

        try {
            val files = SafBrowserHelper.listTreeContents(appContext, savedUri)
            _uiState.update {
                it.copy(currentTreeUri = savedUri, treeFiles = files)
            }
        } catch (_: SecurityException) {
            prefs.edit().remove(TREE_URI_KEY).apply()
            _uiState.update { it.copy(currentTreeUri = null, treeFiles = emptyList()) }
        } catch (_: IllegalArgumentException) {
            prefs.edit().remove(TREE_URI_KEY).apply()
            _uiState.update { it.copy(currentTreeUri = null, treeFiles = emptyList()) }
        }
    }

    private companion object {
        const val TREE_PREFS = "elyzareth_file_utility_storage"
        const val TREE_URI_KEY = "selected_tree_uri"
    }

    fun selectFile(file: SafFileInfo) {
        _uiState.update { it.copy(selectedFile = file) }
        recordHistory(
            title = "File Inspected / Opened",
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
