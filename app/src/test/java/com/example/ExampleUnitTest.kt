package com.example

import com.example.model.BottomNavTab
import com.example.model.ComingSoonNotice
import com.example.model.FeatureCatalog
import com.example.model.ToolCategory
import com.example.ui.ElyzarethAppViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun allCatalogItems_explicitlyContainAllCategoryAndFunctionalCategory() {
        val allSectionItems = FeatureCatalog.sections.flatMap { it.items }
        val allItems = allSectionItems + FeatureCatalog.otherUtilitiesItems

        assertEquals(27, allSectionItems.size)
        assertEquals(4, FeatureCatalog.otherUtilitiesItems.size)
        assertTrue("Catalog must not be empty", allItems.isNotEmpty())

        allItems.forEach { item ->
            assertTrue(
                "Item ${item.id} must contain ToolCategory.ALL",
                ToolCategory.ALL in item.categories
            )
            assertTrue(
                "Item ${item.id} must declare at least one functional category besides ALL",
                item.categories.size >= 2
            )
            if (item.id == "tool_document_scanner") {
                assertFalse(
                    "Item ${item.id} is implemented in ZIP5 and must not be marked Coming Soon",
                    item.isComingSoon
                )
            } else {
                assertTrue(
                    "Item ${item.id} must be marked Coming Soon in Stage 1",
                    item.isComingSoon
                )
            }
        }
    }

    @Test
    fun editCategoryFilter_showsOnlyEditTaggedToolsAndHidesEmptySections() {
        val filteredSections = FeatureCatalog.filterSectionsForCategory(ToolCategory.EDIT)

        // Document Conversion has no EDIT-tagged items and must be hidden
        val sectionIds = filteredSections.map { it.id }
        assertFalse(
            "Document Conversion section should be hidden when EDIT is selected",
            "section_document_conversion" in sectionIds
        )

        // Every remaining item in every visible section must have ToolCategory.EDIT
        filteredSections.forEach { section ->
            assertTrue("Visible section ${section.id} must not be empty", section.items.isNotEmpty())
            section.items.forEach { item ->
                assertTrue(
                    "Item ${item.id} in section ${section.id} must be tagged EDIT",
                    ToolCategory.EDIT in item.categories
                )
            }
        }

        // In Scan & Capture, only ID Photo Maker is tagged EDIT
        val scanSection = filteredSections.first { it.id == "section_scan_capture" }
        assertEquals(listOf("tool_id_photo_maker"), scanSection.items.map { it.id })

        // Other utilities items filtered by EDIT must all contain EDIT
        val filteredOtherUtilities = FeatureCatalog.filterOtherUtilitiesForCategory(ToolCategory.EDIT)
        assertEquals(4, filteredOtherUtilities.size)
        filteredOtherUtilities.forEach { item ->
            assertTrue(ToolCategory.EDIT in item.categories)
        }
    }

    @Test
    fun scanCategoryFilter_hidesNonScanSectionsAndOtherUtilities() {
        val filteredSections = FeatureCatalog.filterSectionsForCategory(ToolCategory.SCAN)
        assertEquals(listOf("section_scan_capture"), filteredSections.map { it.id })
        assertEquals(7, filteredSections.first().items.size)

        val filteredOtherUtilities = FeatureCatalog.filterOtherUtilitiesForCategory(ToolCategory.SCAN)
        assertTrue(
            "Other Utilities has no SCAN items and must be empty",
            filteredOtherUtilities.isEmpty()
        )
        assertFalse(FeatureCatalog.isSlideshowVisible(ToolCategory.SCAN))
        assertFalse(FeatureCatalog.isFileManagerVisible(ToolCategory.SCAN))
    }

    @Test
    fun presentCategoryFilter_showsOnlyPresentItemsAndSlideshowCard() {
        val filteredSections = FeatureCatalog.filterSectionsForCategory(ToolCategory.PRESENT)
        val matchingIds = filteredSections.flatMap { it.items }.map { it.id }
        assertEquals(listOf("tool_whiteboard", "tool_to_ppt"), matchingIds)

        val filteredOtherUtilities = FeatureCatalog.filterOtherUtilitiesForCategory(ToolCategory.PRESENT)
        assertTrue(filteredOtherUtilities.isEmpty())
        assertTrue(FeatureCatalog.isSlideshowVisible(ToolCategory.PRESENT))
        assertFalse(FeatureCatalog.isFileManagerVisible(ToolCategory.PRESENT))
    }

    @Test
    fun viewModel_handlesTabNavigationCategorySelectionAndComingSoonGovernance() {
        val viewModel = ElyzarethAppViewModel()

        // Initial state
        assertEquals(BottomNavTab.HOME, viewModel.uiState.value.selectedTab)
        assertEquals(ToolCategory.ALL, viewModel.uiState.value.selectedCategory)
        assertNull(viewModel.uiState.value.activeComingSoonNotice)

        // Navigate across all 4 bottom tabs
        viewModel.selectTab(BottomNavTab.HISTORY)
        assertEquals(BottomNavTab.HISTORY, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(BottomNavTab.PRESETS)
        assertEquals(BottomNavTab.PRESETS, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(BottomNavTab.SETTINGS)
        assertEquals(BottomNavTab.SETTINGS, viewModel.uiState.value.selectedTab)

        // Selecting a category returns to HOME tab with the selected category active
        viewModel.selectCategory(ToolCategory.CONVERT)
        assertEquals(BottomNavTab.HOME, viewModel.uiState.value.selectedTab)
        assertEquals(ToolCategory.CONVERT, viewModel.uiState.value.selectedCategory)

        // Clicking a tool triggers a ComingSoonNotice without side effects (unless implemented)
        val sampleTool = FeatureCatalog.sections.first().items[1] // ID Cards (still Coming Soon)
        viewModel.onToolClicked(sampleTool)
        val activeNotice = viewModel.uiState.value.activeComingSoonNotice
        assertNotNull(activeNotice)
        assertEquals(sampleTool.id, activeNotice?.id)

        // Dismissing clears the notice
        viewModel.dismissComingSoonNotice()
        assertNull(viewModel.uiState.value.activeComingSoonNotice)

        // Custom ComingSoonNotice (e.g., AI banner or Mini-Player placeholder)
        val miniPlayerNotice = ComingSoonNotice(
            id = "mini_player_placeholder",
            title = "Elyzareth Music Mini-Player",
            stageLabel = "Stage 4",
            description = "Inactive placeholder"
        )
        viewModel.showComingSoonNotice(miniPlayerNotice)
        assertEquals("mini_player_placeholder", viewModel.uiState.value.activeComingSoonNotice?.id)
        viewModel.dismissComingSoonNotice()
        assertNull(viewModel.uiState.value.activeComingSoonNotice)
    }
}
