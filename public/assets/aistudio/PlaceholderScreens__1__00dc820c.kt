package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HistoryItem
import com.example.storage.SafFileInfo
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.DashboardBackground
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextDarkSlate
import com.example.ui.theme.TextMutedSlate

@Composable
fun FileManagerScreen(
    currentTreeUri: Uri?,
    treeFiles: List<SafFileInfo>,
    selectedFile: SafFileInfo?,
    onOpenDocumentPicker: () -> Unit,
    onOpenDocumentTree: () -> Unit,
    onSelectFile: (SafFileInfo) -> Unit,
    onSaveSampleFile: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(DashboardBackground)
            .padding(12.dp)
            .testTag("file_manager_screen")
    ) {
        // Top Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderLight, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "File Manager & SAF Storage",
                    color = TextDarkSlate,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentTreeUri != null) {
                        "Active Folder: ${currentTreeUri.lastPathSegment ?: currentTreeUri}"
                    } else {
                        "Browse documents or grant folder access via SAF."
                    },
                    color = TextMutedSlate,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenDocumentPicker,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_pick_document")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open Doc", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenDocumentTree,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_grant_folder")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Grant Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSaveSampleFile,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_sample")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area: Selected File Details or Tree File List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, CardBorderLight, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                if (selectedFile != null) {
                    Text(
                        text = "Selected File Inspection",
                        color = TextDarkSlate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Name: ${selectedFile.displayName}", color = TextDarkSlate, fontSize = 12.sp)
                    Text(
                        text = "Size: ${selectedFile.sizeBytes?.let { "$it bytes" } ?: if (selectedFile.isDirectory) "Directory" else "Unavailable"}",
                        color = TextMutedSlate,
                        fontSize = 12.sp
                    )
                    Text(text = "MIME: ${selectedFile.mimeType ?: "Unknown"}", color = TextMutedSlate, fontSize = 12.sp)
                    Text(text = "URI: ${selectedFile.uri}", color = TextMutedSlate, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Text(
                    text = if (treeFiles.isNotEmpty()) "Folder Contents (${treeFiles.size})" else "Directory Contents (Select a folder above)",
                    color = TextDarkSlate,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (treeFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No files loaded. Tap 'Grant Folder' to inspect a directory or 'Open Doc' to pick a single file.",
                            color = TextMutedSlate,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("saf_file_list"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(treeFiles, key = { it.uri.toString() }) { file ->
                            SafFileRow(
                                file = file,
                                onClick = { onSelectFile(file) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SafFileRow(
    file: SafFileInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .background(DashboardBackground.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            tint = TealPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.displayName,
                color = TextDarkSlate,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${file.sizebytes_or_summary()} • ${file.mimeType ?: "file"}",
                color = TextMutedSlate,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun SafFileInfo.sizebytes_or_summary(): String = when {
    isDirectory -> "Directory"
    sizeBytes == null -> "Size unavailable"
    sizeBytes == 0L -> "0 bytes"
    sizeBytes < 1024L -> "$sizeBytes bytes"
    else -> "${sizeBytes / 1024} KB"
}

@Composable
fun HistoryScreen(
    historyItems: List<HistoryItem>,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(DashboardBackground)
            .padding(12.dp)
            .testTag("history_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Operation History Log",
                color = TextDarkSlate,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (historyItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = TextMutedSlate,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No operations recorded yet.",
                            color = TextMutedSlate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "SAF file imports and saves in Stage 2 will log here.",
                            color = TextMutedSlate.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("history_items_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(historyItems, key = { it.id }) { item ->
                        HistoryCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderLight, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.operationTitle,
                    color = TextDarkSlate,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = item.timestampFormatted,
                    color = TextMutedSlate,
                    fontSize = 10.5.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.details,
                color = TextMutedSlate,
                fontSize = 12.sp
            )
            if (item.sizeBytes != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Size: ${item.sizeBytes} bytes",
                    color = TealPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PresetsPlaceholderScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)
    StageOneSecondaryPlaceholder(
        screenTestTag = "presets_placeholder_screen",
        icon = Icons.Outlined.StarOutline,
        title = "Presets — Coming Soon",
        stageBadge = "Stage 3 Image & PDF Compression Presets",
        description = "Compression target presets and ID Photo dimension templates are scheduled for Stage 3. Stage 1 and Stage 2 display no invented preset items."
    )
}

@Composable
fun SettingsPlaceholderScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToHome)
    StageOneSecondaryPlaceholder(
        screenTestTag = "settings_placeholder_screen",
        icon = Icons.Outlined.Settings,
        title = "Settings — Coming Soon",
        stageBadge = "Stage 2 Storage & App Configuration",
        description = "Default SAF output directories and storage permissions configuration options are gated as Coming Soon."
    )
}

@Composable
private fun StageOneSecondaryPlaceholder(
    screenTestTag: String,
    icon: ImageVector,
    title: String,
    stageBadge: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(DashboardBackground)
            .padding(20.dp)
            .testTag(screenTestTag),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderLight, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = TealPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealPrimary.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Coming Soon • $stageBadge",
                        color = TealPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    color = TextDarkSlate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    color = TextMutedSlate,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
