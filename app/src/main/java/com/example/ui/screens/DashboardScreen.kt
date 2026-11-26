package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ComingSoonNotice
import com.example.model.FeatureCatalog
import com.example.model.ToolCategory
import com.example.model.UtilityToolItem
import com.example.ui.components.BottomFeatureCardsRow
import com.example.ui.components.ToolSectionCard
import com.example.ui.theme.DashboardBackground

@Composable
fun DashboardScreen(
    selectedCategory: ToolCategory,
    onToolClicked: (UtilityToolItem) -> Unit,
    onShowComingSoon: (ComingSoonNotice) -> Unit,
    modifier: Modifier = Modifier
) {
    // Item-level and section-level filtering required by Stage 1 Specification
    val visibleSections = FeatureCatalog.sections.mapNotNull { section ->
        val matchingItems = section.items.filter {
            selectedCategory == ToolCategory.ALL ||
                selectedCategory in it.categories
        }
        if (matchingItems.isEmpty()) {
            null
        } else {
            section.copy(items = matchingItems)
        }
    }

    val matchingOtherUtilities = FeatureCatalog.otherUtilitiesItems.filter {
        selectedCategory == ToolCategory.ALL ||
            selectedCategory in it.categories
    }

    val showSlideshow = FeatureCatalog.isSlideshowVisible(selectedCategory)
    val showFileManager = FeatureCatalog.isFileManagerVisible(selectedCategory)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(DashboardBackground)
            .testTag("dashboard_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_sections_list"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            items(
                items = visibleSections,
                key = { it.id }
            ) { filteredSection ->
                ToolSectionCard(
                    section = filteredSection,
                    onToolClicked = onToolClicked
                )
            }

            if (showSlideshow || matchingOtherUtilities.isNotEmpty() || showFileManager) {
                item(key = "bottom_feature_cards_row") {
                    BottomFeatureCardsRow(
                        showSlideshow = showSlideshow,
                        otherUtilitiesItems = matchingOtherUtilities,
                        showFileManager = showFileManager,
                        onToolClicked = onToolClicked,
                        onShowComingSoon = onShowComingSoon
                    )
                }
            }
        }
    }
}
