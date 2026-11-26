# Document Scanner — ZIP5 implementation

Implemented: CameraX live preview, camera permission request, full-quality JPEG capture to app cache, multi-page capture list, page removal, and PDF export using Android PdfDocument via Storage Access Framework CreateDocument.

Not implemented in this closure: automatic document-edge detection, perspective correction, OCR, image filters, crop handles, and persistent scan history. These must not be represented as completed.

## Studio build checks
1. Sync Gradle and run `:app:assembleDebug`.
2. Run `:app:testDebugUnitTest`.
3. Install on a physical device; grant camera permission; capture 3 pages; remove page 2; export PDF; open PDF and verify pages 1 and 2 remain in order.
4. Deny camera permission and verify the permission explanation/retry button.
5. Test camera unavailable and PDF save cancellation.

CameraX versions are sourced from the existing version catalog (1.5.0). No additional camera libraries were introduced.
