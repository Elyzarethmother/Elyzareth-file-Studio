package com.example

import com.example.storage.DuplicateFileGuard
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUtilityRegressionTest {
    @Test
    fun duplicateGuard_preservesExtension() {
        assertEquals(
            "backup (2).zip",
            DuplicateFileGuard.resolveUniqueDisplayName(
                setOf("backup.zip", "backup (1).zip"),
                "backup.zip"
            )
        )
    }

    @Test
    fun duplicateGuard_handlesNamesWithoutExtension() {
        assertEquals(
            "backup (1)",
            DuplicateFileGuard.resolveUniqueDisplayName(setOf("backup"), "backup")
        )
    }

    @Test
    fun duplicateGuard_doesNotRenameUnusedName() {
        assertEquals(
            "new.zip",
            DuplicateFileGuard.resolveUniqueDisplayName(emptySet(), "new.zip")
        )
    }
}
