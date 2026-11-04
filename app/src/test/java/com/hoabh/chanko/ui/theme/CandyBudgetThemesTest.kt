package com.hoabh.chanko.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CandyBudgetThemesTest {

    @Test
    fun verify_all_12_themes_exist_with_correct_names_and_ids() {
        assertEquals(12, CandyColors.BudgetThemes.allThemes.size)
        val expectedNames = listOf(
            "Tangerine", "Lavender", "Mint", "Strawberry", "Sky Blue", "Lemon",
            "Peach", "Grape", "Aqua", "Rose", "Pistachio", "Blueberry"
        )
        CandyColors.BudgetThemes.allThemes.forEachIndexed { index, theme ->
            assertEquals(index, theme.id)
            assertEquals(expectedNames[index], theme.name)
            assertNotNull(theme.light)
            assertNotNull(theme.main)
            assertNotNull(theme.dark)
        }
    }

    @Test
    fun verify_floorMod_theme_cycling() {
        // Ordinal 0 -> Tangerine, 11 -> Blueberry, 12 -> Tangerine
        assertEquals("Tangerine", CandyBudgetThemes.getTheme(0).name)
        assertEquals("Blueberry", CandyBudgetThemes.getTheme(11).name)
        assertEquals("Tangerine", CandyBudgetThemes.getTheme(12).name)
        assertEquals("Lavender", CandyBudgetThemes.getTheme(13).name)

        // Negative ordinals
        assertEquals("Blueberry", CandyBudgetThemes.getTheme(-1).name)
        assertEquals("Pistachio", CandyBudgetThemes.getTheme(-2).name)
    }

    @Test
    fun verify_placeholder_and_coral_tokens() {
        assertNotNull(CandyBudgetThemes.placeholderBackground)
        assertNotNull(CandyBudgetThemes.placeholderBorder)
        assertNotNull(CandyBudgetThemes.placeholderInk)
        assertNotNull(CandyBudgetThemes.coralWarning)
    }

    @Test
    fun verify_gradient_brush_creation() {
        val brush0 = CandyBudgetThemes.getGradientBrush(0)
        assertNotNull(brush0)
        val brush12 = CandyBudgetThemes.getGradientBrush(12)
        assertNotNull(brush12)
    }
}
