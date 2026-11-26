package com.example.model

enum class ToolCategory(
    val id: String,
    val displayName: String,
    val testTag: String
) {
    ALL(
        id = "all",
        displayName = "All Tools",
        testTag = "category_chip_all"
    ),
    SCAN(
        id = "scan",
        displayName = "Scan",
        testTag = "category_chip_scan"
    ),
    IMAGE(
        id = "image",
        displayName = "Image",
        testTag = "category_chip_image"
    ),
    CONVERT(
        id = "convert",
        displayName = "Convert",
        testTag = "category_chip_convert"
    ),
    EDIT(
        id = "edit",
        displayName = "Edit",
        testTag = "category_chip_edit"
    ),
    PRESENT(
        id = "present",
        displayName = "Present",
        testTag = "category_chip_present"
    ),
    UTILITIES(
        id = "utilities",
        displayName = "Utilities",
        testTag = "category_chip_utilities"
    )
}
