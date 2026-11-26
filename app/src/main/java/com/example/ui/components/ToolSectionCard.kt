package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BorderClear
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComingSoonNotice
import com.example.model.SectionHeaderStyle
import com.example.model.ToolIconType
import com.example.model.ToolSection
import com.example.model.UtilityToolItem
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.DocConversionHeaderEnd
import com.example.ui.theme.DocConversionHeaderStart
import com.example.ui.theme.DocConversionIconIndigo
import com.example.ui.theme.FileManagerAccentAmber
import com.example.ui.theme.FileManagerHeaderBg
import com.example.ui.theme.ImageStudioHeaderEnd
import com.example.ui.theme.ImageStudioHeaderStart
import com.example.ui.theme.ImageStudioIconGreen
import com.example.ui.theme.OtherUtilitiesHeaderBg
import com.example.ui.theme.OtherUtilitiesIconBlue
import com.example.ui.theme.PdfToolsHeaderEnd
import com.example.ui.theme.PdfToolsHeaderStart
import com.example.ui.theme.PdfToolsIconRed
import com.example.ui.theme.ScanSectionHeaderEnd
import com.example.ui.theme.ScanSectionHeaderStart
import com.example.ui.theme.ScanSectionIconTeal
import com.example.ui.theme.SlideshowAccentOrange
import com.example.ui.theme.SlideshowButtonBg
import com.example.ui.theme.SlideshowCardBg
import com.example.ui.theme.SlideshowCardBorder
import com.example.ui.theme.TextDarkSlate
import com.example.ui.theme.TextMutedSlate

@Composable
fun ToolSectionCard(
    section: ToolSection,
    onToolClicked: (UtilityToolItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val (headerBrush, iconVector, iconTint) = when (section.headerStyle) {
        SectionHeaderStyle.SCAN_CAPTURE -> Triple(
            Brush.horizontalGradient(listOf(ScanSectionHeaderStart, ScanSectionHeaderEnd)),
            Icons.Default.CameraAlt,
            ScanSectionIconTeal
        )
        SectionHeaderStyle.IMAGE_STUDIO -> Triple(
            Brush.horizontalGradient(listOf(ImageStudioHeaderStart, ImageStudioHeaderEnd)),
            Icons.Default.Image,
            ImageStudioIconGreen
        )
        SectionHeaderStyle.DOCUMENT_CONVERSION -> Triple(
            Brush.horizontalGradient(listOf(DocConversionHeaderStart, DocConversionHeaderEnd)),
            Icons.AutoMirrored.Filled.CompareArrows,
            DocConversionIconIndigo
        )
        SectionHeaderStyle.PDF_TOOLS -> Triple(
            Brush.horizontalGradient(listOf(PdfToolsHeaderStart, PdfToolsHeaderEnd)),
            Icons.Default.PictureAsPdf,
            PdfToolsIconRed
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_card_${section.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderLight, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            // Pastel Section Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(headerBrush)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = section.title,
                    color = TextDarkSlate,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                ComingSoonPillBadge()
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Row of Tool Items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                section.items.forEach { item ->
                    UtilityToolItemTile(
                        item = item,
                        onClick = { onToolClicked(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun UtilityToolItemTile(
    item: UtilityToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(68.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("tool_card_${item.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(item.badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.iconType.toImageVector(),
                contentDescription = item.title.replace('\n', ' '),
                tint = item.iconTint,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.title,
            color = TextDarkSlate,
            fontSize = 10.sp,
            lineHeight = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (item.subtitle != null) {
            Text(
                text = item.subtitle,
                color = TextMutedSlate,
                fontSize = 8.5.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BottomFeatureCardsRow(
    showSlideshow: Boolean,
    otherUtilitiesItems: List<UtilityToolItem>,
    showFileManager: Boolean,
    onToolClicked: (UtilityToolItem) -> Unit,
    onShowComingSoon: (ComingSoonNotice) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showSlideshow && otherUtilitiesItems.isEmpty() && !showFileManager) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_feature_cards_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showSlideshow) {
            SlideshowStudioCard(
                onClick = {
                    onShowComingSoon(
                        ComingSoonNotice(
                            id = "slideshow_studio",
                            title = "Slideshow Studio — Auto Play",
                            stageLabel = "Deferred Stage Feature — Coming Soon",
                            description = "Auto-play presentation for PDF, PPT, JPG, PNG, and BMP with adjustable 3–6s delay is gated as Coming Soon in Stage 1."
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        if (otherUtilitiesItems.isNotEmpty()) {
            OtherUtilitiesCard(
                items = otherUtilitiesItems,
                onToolClicked = onToolClicked,
                modifier = Modifier.weight(1f)
            )
        }

        if (showFileManager) {
            FileManagerCard(
                onClick = {
                    onShowComingSoon(
                        ComingSoonNotice(
                            id = "file_manager_saf",
                            title = "File Manager & SAF Storage",
                            stageLabel = "Stage 2 — File Manager & SAF",
                            description = "Storage Access Framework (SAF) folder grants, file browsing, and Downloads management are scheduled for Stage 2. No fake storage actions are executed in Stage 1."
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SlideshowStudioCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("card_slideshow_studio"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlideshowCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SlideshowCardBorder, RoundedCornerShape(14.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Slideshow,
                    contentDescription = null,
                    tint = SlideshowAccentOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Slideshow Studio",
                    color = TextDarkSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Slideshow Settings",
                    tint = SlideshowAccentOrange,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = SlideshowAccentOrange,
                modifier = Modifier.size(26.dp)
            )

            Text(
                text = "Auto Play",
                color = TextDarkSlate,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "PDF / PPT / JPG / PNG / BMP",
                color = TextMutedSlate,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SlideshowButtonBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF7A4B00),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Start Slideshow",
                    color = Color(0xFF4A2E00),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextMutedSlate,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Delay: 3 – 6s • Coming Soon",
                    color = TextMutedSlate,
                    fontSize = 7.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OtherUtilitiesCard(
    items: List<UtilityToolItem>,
    onToolClicked: (UtilityToolItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("card_other_utilities"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderLight, RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(OtherUtilitiesHeaderBg)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = OtherUtilitiesIconBlue,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Other Utilities",
                    color = TextDarkSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    Column(
                        modifier = Modifier
                            .width(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(role = Role.Button) { onToolClicked(item) }
                            .padding(vertical = 3.dp)
                            .testTag("tool_card_${item.id}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(item.badgeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.iconType.toImageVector(),
                                contentDescription = item.title.replace('\n', ' '),
                                tint = item.iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.title,
                            color = TextDarkSlate,
                            fontSize = 8.5.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileManagerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("card_file_manager"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderLight, RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FileManagerHeaderBg)
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = FileManagerAccentAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "File Manager",
                    color = TextDarkSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Browse • Manage • History",
                color = TextMutedSlate,
                fontSize = 8.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3DC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = FileManagerAccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Internal Storage",
                        color = TextDarkSlate,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Coming Soon (Stage 2)",
                        color = TextMutedSlate,
                        fontSize = 8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFD9E3E8))
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Coming Soon",
                    tint = TextMutedSlate,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ComingSoonPillBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Coming Soon",
            color = TextMutedSlate,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun ToolIconType.toImageVector(): ImageVector = when (this) {
    ToolIconType.DOCUMENT_SCANNER -> Icons.Default.DocumentScanner
    ToolIconType.ID_CARDS -> Icons.Default.Badge
    ToolIconType.EXTRACT_TEXT_OCR -> Icons.Default.CropFree
    ToolIconType.ID_PHOTO_MAKER -> Icons.Default.AccountBox
    ToolIconType.PHOTO_TRANSLATION -> Icons.Default.Translate
    ToolIconType.WHITEBOARD -> Icons.Default.Tv
    ToolIconType.TIMESTAMP_CAMERA -> Icons.Default.CameraAlt
    ToolIconType.RESIZE_COMPRESS -> Icons.Default.Compress
    ToolIconType.AUTO_ENHANCE -> Icons.Default.WbSunny
    ToolIconType.AUTO_ORIENTATION -> Icons.AutoMirrored.Filled.RotateRight
    ToolIconType.FORMAT_CONVERT -> Icons.Default.Image
    ToolIconType.BATCH_PROCESSING -> Icons.Default.Collections
    ToolIconType.RESTORE_PHOTO -> Icons.Default.AutoFixHigh
    ToolIconType.IMAGE_TO_PDF -> Icons.Default.PictureAsPdf
    ToolIconType.PDF_TO_IMAGES -> Icons.Default.PhotoFilter
    ToolIconType.PDF_TO_TEXT -> Icons.Default.TextFields
    ToolIconType.TO_WORD -> Icons.Default.Description
    ToolIconType.TO_EXCEL -> Icons.Default.GridOn
    ToolIconType.TO_PPT -> Icons.Default.Slideshow
    ToolIconType.TXT_DOCX -> Icons.AutoMirrored.Filled.CompareArrows
    ToolIconType.MERGE_PDF -> Icons.Default.Layers
    ToolIconType.SPLIT_PDF -> Icons.AutoMirrored.Filled.CallSplit
    ToolIconType.EXTRACT_PAGES -> Icons.Default.ContentCut
    ToolIconType.REORDER_PAGES -> Icons.Default.SwapVert
    ToolIconType.COMPRESS_PDF -> Icons.Default.Compress
    ToolIconType.LOCK_UNLOCK -> Icons.Default.Lock
    ToolIconType.PDF_TO_LONG_IMAGE -> Icons.Default.ViewAgenda
    ToolIconType.SIGN -> Icons.Default.Create
    ToolIconType.WATERMARK -> Icons.Default.WaterDrop
    ToolIconType.SMART_ERASE -> Icons.Default.AutoFixHigh
    ToolIconType.ERASE_MARKS -> Icons.Default.BorderClear
}
