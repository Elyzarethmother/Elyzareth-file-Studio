# Elyzareth File Utility — project-specific correction set

This is a collection of individual replacement files, not a ZIP archive.

## Source of truth

These replacements were prepared against the user's latest upload:
`elyzareth-file-utility (3).zip`

They preserve the existing `ElyzarethAppViewModel`, `SafFileInfo` call sites, Compose screen APIs, dashboard, categories, four-tab navigation and Coming Soon gates.

## Files to replace

Copy each file into the project at the same relative path:

1. `app/src/main/java/com/example/ui/ElyzarethApp.kt`
2. `app/src/main/java/com/example/storage/FileSaver.kt`
3. `app/src/main/java/com/example/storage/SafBrowserHelper.kt`
4. `app/src/main/java/com/example/ui/screens/PlaceholderScreens.kt`
5. `app/src/main/AndroidManifest.xml`
6. `app/src/main/res/xml/file_paths.xml`

Add this new test:
7. `app/src/test/java/com/example/FileUtilityRegressionTest.kt`

## Corrections

- `Open Doc` now launches `ACTION_OPEN_DOCUMENT` through `StartActivityForResult`, requests `*/*`, and reads the actual returned URI and grant flags. This is the Android Storage Access Framework document picker, not the Photo Picker.
- The result URI's read permission is persisted only when Android explicitly grants a persistable read permission.
- `Grant Folder` launches `ACTION_OPEN_DOCUMENT_TREE` and persists only the read/write permissions and persistability flag actually returned by Android. UI state is updated only after permission persistence succeeds.
- Folder-load and document-inspection failures are surfaced through the existing Coming Soon notice mechanism.
- Unknown file size is represented as `null`; a known zero-byte file remains `0`. Directory status is tracked separately.
- MediaStore and SAF writes are read back through their content URIs and byte counts are verified before success is reported. Failed partial files are deleted.
- Legacy Downloads writes use a temporary file, sync, verify, atomic rename, and final size check.
- Android 9 and lower request `WRITE_EXTERNAL_STORAGE` at runtime before the legacy Downloads write. The manifest permission is capped at API 28.
- FileProvider is restricted to the public `Download/` directory used by the legacy save path.
- Existing screen layout and navigation are unchanged.

## Scope

This patch enables selecting and inspecting ZIP archives as documents. It does not implement ZIP extraction, archive browsing, or import of files contained inside an archive.

The Android Studio / Google AI Studio chat attachment picker is separate from Elyzareth's in-app document picker. This code only changes the installed Elyzareth app.

## Verification limitation

The supplied source project has neither `gradlew` nor `gradle/wrapper/gradle-wrapper.jar`. Therefore, the Android build cannot be run using the project's wrapper as supplied. No device test is claimed here.

After applying the replacements, run `./gradlew :app:testDebugUnitTest assembleDebug` in the configured Android build environment, then physically test:
1. Open Doc opens Android's system document picker.
2. A ZIP in Downloads can be selected and its name/MIME/URI are displayed.
3. Grant Folder works and lists contents.
4. Cancelled/denied folder permission does not change the active folder.
5. Save File creates a complete sample file in Downloads.
6. Repeating Save File does not overwrite the earlier file.
7. Android 9 and lower prompt for legacy write permission; Android 10+ does not.
