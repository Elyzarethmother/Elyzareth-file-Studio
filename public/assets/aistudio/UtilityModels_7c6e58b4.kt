package com.example.model

import androidx.compose.ui.graphics.Color

enum class ToolIconType {
    DOCUMENT_SCANNER,
    ID_CARDS,
    EXTRACT_TEXT_OCR,
    ID_PHOTO_MAKER,
    PHOTO_TRANSLATION,
    WHITEBOARD,
    TIMESTAMP_CAMERA,
    RESIZE_COMPRESS,
    AUTO_ENHANCE,
    AUTO_ORIENTATION,
    FORMAT_CONVERT,
    BATCH_PROCESSING,
    RESTORE_PHOTO,
    IMAGE_TO_PDF,
    PDF_TO_IMAGES,
    PDF_TO_TEXT,
    TO_WORD,
    TO_EXCEL,
    TO_PPT,
    TXT_DOCX,
    MERGE_PDF,
    SPLIT_PDF,
    EXTRACT_PAGES,
    REORDER_PAGES,
    COMPRESS_PDF,
    LOCK_UNLOCK,
    PDF_TO_LONG_IMAGE,
    SIGN,
    WATERMARK,
    SMART_ERASE,
    ERASE_MARKS
}

enum class SectionHeaderStyle {
    SCAN_CAPTURE,
    IMAGE_STUDIO,
    DOCUMENT_CONVERSION,
    PDF_TOOLS
}

data class ComingSoonNotice(
    val id: String,
    val title: String,
    val stageLabel: String,
    val description: String
)

data class UtilityToolItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconType: ToolIconType,
    val badgeColor: Color,
    val iconTint: Color,
    val categories: Set<ToolCategory>,
    val stageLabel: String,
    val comingSoonDescription: String,
    val isComingSoon: Boolean = true
) {
    init {
        require(ToolCategory.ALL in categories) {
            "UtilityToolItem '$id' must explicitly include ToolCategory.ALL in categories."
        }
    }
}

data class ToolSection(
    val id: String,
    val title: String,
    val headerStyle: SectionHeaderStyle,
    val items: List<UtilityToolItem>
)

object FeatureCatalog {

    val sections: List<ToolSection> = listOf(
        ToolSection(
            id = "section_scan_capture",
            title = "Scan & Capture",
            headerStyle = SectionHeaderStyle.SCAN_CAPTURE,
            items = listOf(
                UtilityToolItem(
                    id = "tool_document_scanner",
                    title = "Document\nScanner",
                    iconType = ToolIconType.DOCUMENT_SCANNER,
                    badgeColor = Color(0xFFD8F0FA),
                    iconTint = Color(0xFF0288D1),
                    categories = setOf(ToolCategory.ALL, ToolCategory.SCAN),
                    stageLabel = "Deferred Stage Feature",
                    comingSoonDescription = "Camera document edge detection and perspective capture are gated as Coming Soon in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_id_cards",
                    title = "ID Cards",
                    iconType = ToolIconType.ID_CARDS,
                    badgeColor = Color(0xFFD9EBFC),
                    iconTint = Color(0xFF1976D2),
                    categories = setOf(ToolCategory.ALL, ToolCategory.SCAN),
                    stageLabel = "Deferred Stage Feature",
                    comingSoonDescription = "Dual-side ID card capture and layout alignment are gated as Coming Soon in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_extract_text_ocr",
                    title = "Extract\nText (OCR)",
                    iconType = ToolIconType.EXTRACT_TEXT_OCR,
                    badgeColor = Color(0xFFDCEBFB),
                    iconTint = Color(0xFF1565C0),
                    categories = setOf(ToolCategory.ALL, ToolCategory.SCAN, ToolCategory.CONVERT),
                    stageLabel = "Stage 5 — PDF Tools & OCR",
                    comingSoonDescription = "Optical Character Recognition (OCR) text extraction is scheduled for Stage 5 and does not execute in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_id_photo_maker",
                    title = "ID Photo\nMaker",
                    iconType = ToolIconType.ID_PHOTO_MAKER,
                    badgeColor = Color(0xFFEDE2FA),
                    iconTint = Color(0xFF7B1FA2),
                    categories = setOf(
                        ToolCategory.ALL,
                        ToolCategory.SCAN,
                        ToolCategory.IMAGE,
                        ToolCategory.EDIT
                    ),
                    stageLabel = "Stage 3 — Image Studio & ID Photo Maker",
                    comingSoonDescription = "ID Photo Maker dimension presets and target-KB compression are scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_photo_translation",
                    title = "Photo\nTranslation",
                    iconType = ToolIconType.PHOTO_TRANSLATION,
                    badgeColor = Color(0xFFD9ECFA),
                    iconTint = Color(0xFF0277BD),
                    categories = setOf(ToolCategory.ALL, ToolCategory.SCAN, ToolCategory.CONVERT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "Captured image translation is deferred and gated as Coming Soon with no network or OCR execution in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_whiteboard",
                    title = "Whiteboard",
                    iconType = ToolIconType.WHITEBOARD,
                    badgeColor = Color(0xFFDCEEFB),
                    iconTint = Color(0xFF039BE5),
                    categories = setOf(ToolCategory.ALL, ToolCategory.SCAN, ToolCategory.PRESENT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "Whiteboard glare cleanup and presentation capture are gated as Coming Soon in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_timestamp_camera",
                    title = "Timestamp\nCamera",
                    iconType = ToolIconType.TIMESTAMP_CAMERA,
                    badgeColor = Color(0xFFEDE1FA),
                    iconTint = Color(0xFF6A1B9A),
                    categories = setOf(ToolCategory.ALL, ToolCategory.SCAN, ToolCategory.UTILITIES),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "Timestamp watermark camera capture is gated as Coming Soon in Stage 1."
                )
            )
        ),
        ToolSection(
            id = "section_image_studio",
            title = "Image Studio",
            headerStyle = SectionHeaderStyle.IMAGE_STUDIO,
            items = listOf(
                UtilityToolItem(
                    id = "tool_resize_compress",
                    title = "Resize &\nCompress",
                    iconType = ToolIconType.RESIZE_COMPRESS,
                    badgeColor = Color(0xFFD7F5E8),
                    iconTint = Color(0xFF2E7D32),
                    categories = setOf(ToolCategory.ALL, ToolCategory.IMAGE, ToolCategory.EDIT),
                    stageLabel = "Stage 3 — Image Studio",
                    comingSoonDescription = "Two-pass bounded decoding and target-KB JPEG compression are scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_auto_enhance",
                    title = "Auto Enhance\n(Brightness)",
                    iconType = ToolIconType.AUTO_ENHANCE,
                    badgeColor = Color(0xFFFFF3D6),
                    iconTint = Color(0xFFF57C00),
                    categories = setOf(ToolCategory.ALL, ToolCategory.IMAGE, ToolCategory.EDIT),
                    stageLabel = "Stage 3 — Image Studio",
                    comingSoonDescription = "Auto brightness and contrast enhancement are scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_auto_orientation",
                    title = "Auto\nOrientation",
                    iconType = ToolIconType.AUTO_ORIENTATION,
                    badgeColor = Color(0xFFD8F5E6),
                    iconTint = Color(0xFF2E7D32),
                    categories = setOf(ToolCategory.ALL, ToolCategory.IMAGE, ToolCategory.EDIT),
                    stageLabel = "Stage 3 — Image Studio",
                    comingSoonDescription = "EXIF orientations 1–8 normalization is scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_format_convert",
                    title = "Format\nConvert",
                    iconType = ToolIconType.FORMAT_CONVERT,
                    badgeColor = Color(0xFFD7F4E3),
                    iconTint = Color(0xFF1B873F),
                    categories = setOf(ToolCategory.ALL, ToolCategory.IMAGE, ToolCategory.CONVERT),
                    stageLabel = "Stage 3 — Image Studio",
                    comingSoonDescription = "JPEG, PNG, and WebP format conversion is scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_batch_processing",
                    title = "Batch\nProcessing",
                    iconType = ToolIconType.BATCH_PROCESSING,
                    badgeColor = Color(0xFFD4F5E2),
                    iconTint = Color(0xFF1B873F),
                    categories = setOf(
                        ToolCategory.ALL,
                        ToolCategory.IMAGE,
                        ToolCategory.EDIT,
                        ToolCategory.UTILITIES
                    ),
                    stageLabel = "Stage 3 — Image Studio",
                    comingSoonDescription = "Cancellation-safe multi-image batch compression is scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_restore_photo",
                    title = "Restore\nPhoto",
                    iconType = ToolIconType.RESTORE_PHOTO,
                    badgeColor = Color(0xFFEDE1FA),
                    iconTint = Color(0xFF6A1B9A),
                    categories = setOf(ToolCategory.ALL, ToolCategory.IMAGE, ToolCategory.EDIT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "Photo restoration is deferred and gated as Coming Soon in Stage 1."
                )
            )
        ),
        ToolSection(
            id = "section_document_conversion",
            title = "Document Conversion",
            headerStyle = SectionHeaderStyle.DOCUMENT_CONVERSION,
            items = listOf(
                UtilityToolItem(
                    id = "tool_image_to_pdf",
                    title = "Image to\nPDF",
                    subtitle = "(Compressed)",
                    iconType = ToolIconType.IMAGE_TO_PDF,
                    badgeColor = Color(0xFFFDE0E0),
                    iconTint = Color(0xFFD32F2F),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT, ToolCategory.IMAGE),
                    stageLabel = "Stage 3 — Image-to-PDF Engine",
                    comingSoonDescription = "Staged multi-image to compressed PDF generation is scheduled for Stage 3."
                ),
                UtilityToolItem(
                    id = "tool_pdf_to_images",
                    title = "PDF to\nImages",
                    iconType = ToolIconType.PDF_TO_IMAGES,
                    badgeColor = Color(0xFFFCE2E4),
                    iconTint = Color(0xFFC62828),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT, ToolCategory.IMAGE),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "Rendering PDF pages to image files is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_pdf_to_text",
                    title = "PDF to\nText",
                    iconType = ToolIconType.PDF_TO_TEXT,
                    badgeColor = Color(0xFFE7E2FC),
                    iconTint = Color(0xFF5E35B1),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT),
                    stageLabel = "Stage 5 — PDF Tools & OCR",
                    comingSoonDescription = "Extracting plain text from PDF documents is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_to_word",
                    title = "To Word\n(DOCX)",
                    iconType = ToolIconType.TO_WORD,
                    badgeColor = Color(0xFFDCEBFA),
                    iconTint = Color(0xFF1565C0),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "DOCX document conversion is deferred and gated as Coming Soon in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_to_excel",
                    title = "To Excel\n(XLSX)",
                    iconType = ToolIconType.TO_EXCEL,
                    badgeColor = Color(0xFFD9F4E4),
                    iconTint = Color(0xFF2E7D32),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "XLSX table conversion is deferred and gated as Coming Soon in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_to_ppt",
                    title = "To PPT\n(PPTX)",
                    iconType = ToolIconType.TO_PPT,
                    badgeColor = Color(0xFFFEE5D9),
                    iconTint = Color(0xFFE65100),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT, ToolCategory.PRESENT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "PPTX presentation export is deferred and gated as Coming Soon in Stage 1."
                ),
                UtilityToolItem(
                    id = "tool_txt_docx",
                    title = "TXT\n↔ DOCX",
                    iconType = ToolIconType.TXT_DOCX,
                    badgeColor = Color(0xFFD9EEFA),
                    iconTint = Color(0xFF0277BD),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "Bidirectional TXT and DOCX conversion is gated as Coming Soon in Stage 1."
                )
            )
        ),
        ToolSection(
            id = "section_pdf_tools",
            title = "PDF Tools",
            headerStyle = SectionHeaderStyle.PDF_TOOLS,
            items = listOf(
                UtilityToolItem(
                    id = "tool_merge_pdf",
                    title = "Merge\nPDF",
                    iconType = ToolIconType.MERGE_PDF,
                    badgeColor = Color(0xFFFDE0E2),
                    iconTint = Color(0xFFD32F2F),
                    categories = setOf(ToolCategory.ALL, ToolCategory.EDIT, ToolCategory.UTILITIES),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "Combining multiple PDF files is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_split_pdf",
                    title = "Split\nPDF",
                    iconType = ToolIconType.SPLIT_PDF,
                    badgeColor = Color(0xFFFDE0E2),
                    iconTint = Color(0xFFD32F2F),
                    categories = setOf(ToolCategory.ALL, ToolCategory.EDIT, ToolCategory.UTILITIES),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "Splitting PDF documents by page range is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_extract_pages",
                    title = "Extract\nPages",
                    iconType = ToolIconType.EXTRACT_PAGES,
                    badgeColor = Color(0xFFFDE0E2),
                    iconTint = Color(0xFFD32F2F),
                    categories = setOf(ToolCategory.ALL, ToolCategory.EDIT),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "Extracting selected PDF pages is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_reorder_pages",
                    title = "Reorder\nPages",
                    iconType = ToolIconType.REORDER_PAGES,
                    badgeColor = Color(0xFFE7E2FC),
                    iconTint = Color(0xFF5E35B1),
                    categories = setOf(ToolCategory.ALL, ToolCategory.EDIT),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "Interactive PDF page reordering is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_compress_pdf",
                    title = "Compress\nPDF",
                    subtitle = "(Reduce Size)",
                    iconType = ToolIconType.COMPRESS_PDF,
                    badgeColor = Color(0xFFFDE2DD),
                    iconTint = Color(0xFFD84315),
                    categories = setOf(ToolCategory.ALL, ToolCategory.EDIT, ToolCategory.UTILITIES),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "PDF size reduction is scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_lock_unlock",
                    title = "Lock /\nUnlock",
                    iconType = ToolIconType.LOCK_UNLOCK,
                    badgeColor = Color(0xFFFEE5D9),
                    iconTint = Color(0xFFE65100),
                    categories = setOf(ToolCategory.ALL, ToolCategory.UTILITIES),
                    stageLabel = "Stage 5 — PDF Tools",
                    comingSoonDescription = "PDF security lock and unlock utilities are scheduled for Stage 5."
                ),
                UtilityToolItem(
                    id = "tool_pdf_to_long_image",
                    title = "PDF to\nLong Image",
                    iconType = ToolIconType.PDF_TO_LONG_IMAGE,
                    badgeColor = Color(0xFFE7E2FC),
                    iconTint = Color(0xFF5E35B1),
                    categories = setOf(ToolCategory.ALL, ToolCategory.CONVERT, ToolCategory.IMAGE),
                    stageLabel = "Deferred Feature — Coming Soon",
                    comingSoonDescription = "Stitching PDF pages into a continuous vertical image is gated as Coming Soon in Stage 1."
                )
            )
        )
    )

    val otherUtilitiesItems: List<UtilityToolItem> = listOf(
        UtilityToolItem(
            id = "tool_sign",
            title = "Sign",
            iconType = ToolIconType.SIGN,
            badgeColor = Color(0xFFD9EEFA),
            iconTint = Color(0xFF0277BD),
            categories = setOf(ToolCategory.ALL, ToolCategory.EDIT, ToolCategory.UTILITIES),
            stageLabel = "Deferred Feature — Coming Soon",
            comingSoonDescription = "Digital signature overlay is deferred and gated as Coming Soon in Stage 1."
        ),
        UtilityToolItem(
            id = "tool_watermark",
            title = "Watermark",
            iconType = ToolIconType.WATERMARK,
            badgeColor = Color(0xFFE7E2FC),
            iconTint = Color(0xFF5E35B1),
            categories = setOf(ToolCategory.ALL, ToolCategory.EDIT, ToolCategory.UTILITIES),
            stageLabel = "Deferred Feature — Coming Soon",
            comingSoonDescription = "Document and image watermarking is deferred and gated as Coming Soon in Stage 1."
        ),
        UtilityToolItem(
            id = "tool_smart_erase",
            title = "Smart\nErase",
            iconType = ToolIconType.SMART_ERASE,
            badgeColor = Color(0xFFD7F5E8),
            iconTint = Color(0xFF2E7D32),
            categories = setOf(
                ToolCategory.ALL,
                ToolCategory.EDIT,
                ToolCategory.IMAGE,
                ToolCategory.UTILITIES
            ),
            stageLabel = "Deferred Feature — Coming Soon",
            comingSoonDescription = "Smart object and blemish removal is deferred and gated as Coming Soon in Stage 1."
        ),
        UtilityToolItem(
            id = "tool_erase_marks",
            title = "Erase\nMarks",
            iconType = ToolIconType.ERASE_MARKS,
            badgeColor = Color(0xFFD9EEFA),
            iconTint = Color(0xFF0277BD),
            categories = setOf(ToolCategory.ALL, ToolCategory.EDIT, ToolCategory.UTILITIES),
            stageLabel = "Deferred Feature — Coming Soon",
            comingSoonDescription = "Handwriting and annotation mark cleanup is deferred and gated as Coming Soon in Stage 1."
        )
    )

    val slideshowStudioCategories: Set<ToolCategory> = setOf(
        ToolCategory.ALL,
        ToolCategory.PRESENT
    )

    val fileManagerCategories: Set<ToolCategory> = setOf(
        ToolCategory.ALL,
        ToolCategory.UTILITIES
    )

    fun filterItemsForCategory(
        items: List<UtilityToolItem>,
        selectedCategory: ToolCategory
    ): List<UtilityToolItem> = items.filter {
        selectedCategory == ToolCategory.ALL || selectedCategory in it.categories
    }

    fun filterSectionsForCategory(
        selectedCategory: ToolCategory,
        sourceSections: List<ToolSection> = sections
    ): List<ToolSection> = sourceSections.mapNotNull { section ->
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

    fun filterOtherUtilitiesForCategory(
        selectedCategory: ToolCategory,
        items: List<UtilityToolItem> = otherUtilitiesItems
    ): List<UtilityToolItem> = items.filter {
        selectedCategory == ToolCategory.ALL ||
            selectedCategory in it.categories
    }

    fun isSlideshowVisible(selectedCategory: ToolCategory): Boolean =
        selectedCategory == ToolCategory.ALL || selectedCategory in slideshowStudioCategories

    fun isFileManagerVisible(selectedCategory: ToolCategory): Boolean =
        selectedCategory == ToolCategory.ALL || selectedCategory in fileManagerCategories
}
