package com.example

import com.example.storage.DuplicateFileGuard
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUtilityCorrectionTest {
    @Test
    fun duplicateGuard_keepsOriginalNameWhenAvailable() {
        assertEquals(
            "archive.zip",
            DuplicateFileGuard.resolveUniqueDisplayName(emptySet(), "archive.zip")
        )
    }

    @Test
    fun duplicateGuard_addsNumberBeforeExtension() {
        assertEquals(
            "archive (2).zip",
            DuplicateFileGuard.resolveUniqueDisplayName(
                setOf("archive.zip", "archive (1).zip"),
                "archive.zip"
            )
        )
    }

    @Test
    fun duplicateGuard_handlesNamesWithoutExtension() {
        assertEquals(
            "archive (1)",
            DuplicateFileGuard.resolveUniqueDisplayName(setOf("archive"), "archive")
        )
    }
}
