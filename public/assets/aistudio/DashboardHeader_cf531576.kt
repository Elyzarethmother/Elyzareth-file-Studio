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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComingSoonNotice
import com.example.model.ToolCategory
import com.example.ui.theme.AiBannerBorder
import com.example.ui.theme.AiBannerGradientEnd
import com.example.ui.theme.AiBannerGradientStart
import com.example.ui.theme.AiBannerIconTeal
import com.example.ui.theme.CategoryChipSelectedBg
import com.example.ui.theme.CategoryChipSelectedIcon
import com.example.ui.theme.CategoryChipSelectedText
import com.example.ui.theme.CategoryChipUnselectedBg
import com.example.ui.theme.CategoryChipUnselectedBorder
import com.example.ui.theme.GoldVersionBadge
import com.example.ui.theme.TealAccentCyan
import com.example.ui.theme.TealHeaderBottom
import com.example.ui.theme.TealHeaderMid
import com.example.ui.theme.TealHeaderTop
import com.example.ui.theme.TextDarkSlate
import com.example.ui.theme.TextMutedSlate

@Composable
fun DashboardHeader(
    selectedCategory: ToolCategory,
    onCategorySelected: (ToolCategory) -> Unit,
    onShowComingSoon: (ComingSoonNotice) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(TealHeaderTop, TealHeaderMid, TealHeaderBottom)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Brand Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElyzarethBrandLogo()

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Elyzareth File Utility",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "V1.0",
                        color = GoldVersionBadge,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Image • Document • Scan • Convert • Edit • Present",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = {
                    onShowComingSoon(
                        ComingSoonNotice(
                            id = "header_search",
                            title = "Global Tool & File Search",
                            stageLabel = "Coming Soon",
                            description = "Search across tools and indexed local documents is not active in Stage 1."
                        )
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("header_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Tools",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    onShowComingSoon(
                        ComingSoonNotice(
                            id = "header_more_menu",
                            title = "Quick Options Menu",
                            stageLabel = "Coming Soon",
                            description = "Additional header menu actions are gated as Coming Soon in Stage 1."
                        )
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("header_more_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Help Assistant Banner (Visual only in Stage 1)
        AiHelpAssistantBanner(
            onClick = {
                onShowComingSoon(
                    ComingSoonNotice(
                        id = "ai_help_assistant",
                        title = "AI Help Assistant — Powered by Google Gemini",
                        stageLabel = "Stage 6 — Coming Soon",
                        description = "The AI Help Assistant banner is visual-only in Stage 1. Guidance-only Gemini integration is scheduled for Stage 6."
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Seven Category Filters
        CategoryFilterRow(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )
    }
}

@Composable
private fun ElyzarethBrandLogo() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF43D5F0), Color(0xFF007D91))
                )
            )
            .border(1.dp, Color(0xFF8BE9FA), RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFF073B44),
            modifier = Modifier.size(34.dp)
        )
        Text(
            text = "E",
            color = GoldVersionBadge,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

@Composable
private fun AiHelpAssistantBanner(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(AiBannerGradientStart, AiBannerGradientEnd)
                )
            )
            .border(1.dp, AiBannerBorder, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("ai_help_assistant_banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Help Assistant",
            tint = AiBannerIconTeal,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = TextDarkSlate
                        )
                    ) {
                        append("AI Help Assistant")
                    }
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = TextDarkSlate
                        )
                    ) {
                        append(" — Powered by Google Gemini")
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ask about image size, compression, file conversion, or any feature.",
                color = TextMutedSlate,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF008E9B).copy(alpha = 0.14f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Coming Soon",
                color = Color(0xFF006978),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Coming Soon",
            tint = AiBannerIconTeal,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: ToolCategory,
    onCategorySelected: (ToolCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("category_filter_row"),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ToolCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            CategoryFilterChipCard(
                category = category,
                isSelected = isSelected,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryFilterChipCard(
    category: ToolCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    if (isSelected) CategoryChipSelectedBg else CategoryChipUnselectedBg
                )
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color.White else CategoryChipUnselectedBorder,
                    shape = RoundedCornerShape(13.dp)
                )
                .semantics { selected = isSelected }
                .clickable(role = Role.Tab, onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 6.dp)
                .testTag(category.testTag),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.iconVector(),
                contentDescription = category.displayName,
                tint = if (isSelected) CategoryChipSelectedIcon else Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = category.displayName,
                color = if (isSelected) CategoryChipSelectedText else Color.White,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Box(
            modifier = Modifier
                .width(38.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(if (isSelected) TealAccentCyan else Color.Transparent)
        )
    }
}

private fun ToolCategory.iconVector(): ImageVector = when (this) {
    ToolCategory.ALL -> Icons.Default.GridView
    ToolCategory.SCAN -> Icons.Default.PhotoCamera
    ToolCategory.IMAGE -> Icons.Default.Image
    ToolCategory.CONVERT -> Icons.Default.SwapHoriz
    ToolCategory.EDIT -> Icons.Default.Edit
    ToolCategory.PRESENT -> Icons.Default.Slideshow
    ToolCategory.UTILITIES -> Icons.Default.WorkOutline
}
