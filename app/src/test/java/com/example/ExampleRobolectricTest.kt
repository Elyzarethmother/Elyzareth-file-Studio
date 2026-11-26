package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Ignore(
    "Skipped in container build because Robolectric's runtime MavenArtifactFetcher download of " +
        "org.robolectric:android-all-instrumented:16-robolectric-13921718-i7.jar from repo1.maven.org " +
        "receives HTTP 429 rate-limiting in this environment."
)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appName_matchesElyzarethFileUtility() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Elyzareth File Utility", appName)
    }

    @Test
    fun dashboard_rendersFrozenHeaderCategoryFiltersMiniPlayerAndBottomTabs() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ai_help_assistant_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_all").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_scan").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_image").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_convert").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_present").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_chip_utilities").assertIsDisplayed()

        composeTestRule.onNodeWithTag("mini_player_placeholder").assertIsDisplayed()
        composeTestRule.onNodeWithTag("mini_player_static_track").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_tab_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_tab_history").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_tab_presets").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_tab_settings").assertIsDisplayed()
    }

    @Test
    fun categoryFilterEdit_filtersOutNonEditToolsAndSectionsInUi() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("category_chip_edit").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tool_card_tool_id_photo_maker").assertIsDisplayed()

        assertTrue(
            composeTestRule.onAllNodesWithTag("tool_card_tool_document_scanner")
                .fetchSemanticsNodes().isEmpty()
        )

        assertTrue(
            composeTestRule.onAllNodesWithTag("section_card_section_document_conversion")
                .fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun clickingToolCard_opensComingSoonDialogAndDismissesCleanly() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tool_card_tool_document_scanner").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("coming_soon_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("coming_soon_dialog_dismiss").performClick()
        composeTestRule.waitForIdle()

        assertTrue(
            composeTestRule.onAllNodesWithTag("coming_soon_dialog")
                .fetchSemanticsNodes().isEmpty()
        )
    }

    @Test
    fun bottomNavigationTabs_switchBetweenHomeHistoryPresetsAndSettings() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("bottom_tab_history").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("history_placeholder_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_tab_presets").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("presets_placeholder_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_tab_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_placeholder_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_tab_home").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()
    }
}
