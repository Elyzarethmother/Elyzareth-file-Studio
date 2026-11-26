package com.example.model

enum class BottomNavTab(
    val route: String,
    val label: String,
    val testTag: String
) {
    HOME(
        route = "home",
        label = "Home",
        testTag = "bottom_tab_home"
    ),
    HISTORY(
        route = "history",
        label = "History",
        testTag = "bottom_tab_history"
    ),
    PRESETS(
        route = "presets",
        label = "Presets",
        testTag = "bottom_tab_presets"
    ),
    SETTINGS(
        route = "settings",
        label = "Settings",
        testTag = "bottom_tab_settings"
    )
}
