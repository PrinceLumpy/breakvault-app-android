// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.combogenerator

import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratedComboNameTest {

    private val format = "Generated Combo #%1\$d"

    @Test
    fun nextGeneratedComboName_startsAtOneWhenNoneMatch() {
        assertEquals(
            "Generated Combo #1",
            nextGeneratedComboName(listOf("Windmill flow", "2026-09-25 14:03:11"), format)
        )
    }

    @Test
    fun nextGeneratedComboName_usesHighestNumberIgnoringGapsAndRenames() {
        val names = listOf("Generated Combo #1", "Generated Combo #4", "Generated Combo #2 remix")
        assertEquals("Generated Combo #5", nextGeneratedComboName(names, format))
    }
}
