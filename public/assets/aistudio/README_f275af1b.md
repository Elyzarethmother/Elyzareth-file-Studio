# Elyzareth File Utility V1.0 — Stage 1 Foundation

## Overview

Elyzareth File Utility V1.0 (Stage 1) is the foundational Android application built with Kotlin and Jetpack Compose under Implementation Contract v1.1 and Engineering Reference Pack Rev 4.

Stage 1 delivers the frozen UI dashboard, teal branding palette, seven functional category filters with item-level and section-level filtering, four working bottom navigation tabs, honest Coming Soon governance for deferred stage tools, and the docked inactive mini-player visual placeholder.

## Stage 1 Scope & Governance

- **Implemented in Stage 1:**
  - Frozen dashboard hierarchy, teal header, and AI Help Assistant visual banner.
  - Seven interactive category filters (`All Tools`, `Scan`, `Image`, `Convert`, `Edit`, `Present`, `Utilities`) with strict item-level and section-level filtering.
  - Tool cards across `Scan & Capture`, `Image Studio`, `Document Conversion`, `PDF Tools`, and the bottom feature row (`Slideshow Studio`, `Other Utilities`, `File Manager`).
  - Coming Soon modal notices for all tools scheduled in Stages 2–6 without fake file processing, fake progress, or fake success messages.
  - Docked mini-player visual placeholder above the bottom navigation bar using a static neutral track.
  - Four bottom navigation tabs (`Home`, `History`, `Presets`, `Settings`) with honest Stage 1 placeholder screens for secondary tabs.
- **Deferred to Later Authorized Stages:**
  - Stage 2: File Manager and Storage Access Framework (SAF).
  - Stage 3: Image Studio, ID Photo Maker, and Image-to-PDF.
  - Stage 4: Media3 mini-player and background audio playback service.
  - Stage 5: PDF tools and OCR.
  - Stage 6: Gemini Help Assistant and final integration.

## Architecture & Package Layout

- `com.example.MainActivity`: Edge-to-edge Compose host activity.
- `com.example.model`:
  - `ToolCategory`: Enum defining the seven dashboard filter categories.
  - `BottomNavTab`: Enum defining the four bottom navigation destinations.
  - `UtilityModels`: `UtilityToolItem`, `ToolSection`, `ComingSoonNotice`, and `FeatureCatalog` containing the catalog and pure filtering functions.
- `com.example.ui`:
  - `ElyzarethApp`: Root scaffold coordinating the header, active tab screen, docked mini-player placeholder, bottom navigation bar, and Coming Soon dialog.
  - `ElyzarethAppViewModel`: StateFlow-backed UI state holder for tab selection, category selection, and active Coming Soon notices.
- `com.example.ui.components`:
  - `DashboardHeader`: Top branding bar, AI Help Assistant banner, and category filter strip.
  - `ToolSectionCard`: Section cards, tool tiles, and bottom feature cards.
  - `MiniPlayerPlaceholder`: Docked inactive mini-player shell with static 2dp track.
  - `BottomNavigationBar`: Bottom navigation bar for `Home`, `History`, `Presets`, and `Settings`.
  - `ComingSoonDialog`: Modal dialog explaining stage schedule and governance for tapped tools.
- `com.example.ui.screens`:
  - `DashboardScreen`: Scrollable dashboard applying item-level and section-level filtering.
  - `PlaceholderScreens`: Secondary tab placeholder screens (`History`, `Presets`, `Settings`).
- `com.example.ui.theme`:
  - `Color`, `Theme`, `Type`: Frozen Elyzareth V1.0 color palette, Material 3 theme, and typography.

## Build & Verification Instructions

In the cloud container environment, use the system `gradle` binary directly:

1. **Assemble Debug APK:**
   - Command: `gradle :app:assembleDebug`
   - Output APK: `app/build/outputs/apk/debug/app-debug.apk`
2. **Run Unit & Robolectric Tests:**
   - Command: `gradle :app:testDebugUnitTest`
