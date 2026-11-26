# Elyzareth File Utility — targeted correction set

This folder contains **individual replacement files**, not a ZIP archive.

## Replacement map

Copy each file to the same relative path in the Android project:

1. `app/src/main/java/com/example/ui/ElyzarethApp.kt`
2. `app/src/main/java/com/example/storage/SafBrowserHelper.kt`
3. `app/src/main/java/com/example/storage/FileSaver.kt`
4. `app/src/main/java/com/example/ui/screens/PlaceholderScreens.kt`
5. `app/src/main/res/xml/file_paths.xml`
6. `app/src/main/AndroidManifest.xml`
7. `app/src/test/java/com/example/FileUtilityCorrectionTest.kt` (new test file)

## What was corrected

- The single-document action explicitly uses `ActivityResultContracts.OpenDocument`, which launches Android's Storage Access Framework document picker (`ACTION_OPEN_DOCUMENT`), not the photo picker or `GetContent`.
- The document picker requests `*/*` so ZIP archives are not excluded by a MIME allow-list. The selected document MIME type is queried after selection.
- Folder selection uses `ACTION_OPEN_DOCUMENT_TREE` through `StartActivityForResult`, captures the actual returned Intent flags, and persists only the read/write flags Android actually granted.
- The active folder URI and UI state are updated only after persistable permission acquisition succeeds.
- SAF file sizes are nullable when the provider does not report a size. Unknown size is no longer misrepresented as zero bytes.
- Download and SAF saves reopen the saved content URI and verify the byte count. Failed writes are deleted rather than left as partial files.
- Legacy Android 9-and-lower Downloads writes request the required runtime storage permission.
- FileProvider paths are narrowed to the public Downloads directory, which is the only path used by the legacy save implementation.
- File list labels distinguish directories, zero-byte files, and unavailable sizes.

## Scope boundary

This patch enables **selecting and inspecting** a ZIP file. It does not implement ZIP extraction, archive browsing, malware scanning, or importing files contained inside an archive. Those require a separate approved feature.

This patch does not change the dashboard layout, category filters, four-tab navigation, Coming Soon gates, or feature catalogue.

## Verification status and required device checks

The uploaded project did not contain `gradle-wrapper.jar`; only `gradle-wrapper.properties` was present. No Gradle executable or Android SDK build environment was available in this execution environment, so an Android build and emulator/device run could not be completed here.

After applying the files, verify on a physical Android device:

1. Open Elyzareth → File Manager → Open Doc.
2. Confirm Android's **system document picker** opens (not Camera/Photo/Video).
3. Browse Downloads and select a `.zip` file.
4. Confirm filename, MIME type and URI appear in the selected-file inspection panel.
5. Grant a folder and confirm its contents load.
6. Deny/cancel folder selection and confirm the app does not claim that access was granted.
7. Save the sample file and confirm it exists in Downloads with the expected byte count.
8. Repeat on Android 10+; test Android 9 or lower separately if legacy support is required.

A successful Gradle build plus physical-device picker verification is still required before marking the patch PASS.
