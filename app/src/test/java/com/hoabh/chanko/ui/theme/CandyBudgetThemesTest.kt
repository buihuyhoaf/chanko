package com.hoabh.chanko.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CandyBudgetThemesTest {

    @Test
    fun verify_all_12_themes_exist_with_correct_names_and_ids() {
        assertEquals(12, CandyColors.BudgetThemes.allThemes.size)
        val expectedNames = listOf(
            "Tangerine", "Lavender", "Mint", "Strawberry", "Sky", "Lemon",
            "Peach", "Grape", "Aqua", "Rose", "Pistachio", "Blueberry"
        )
        CandyColors.BudgetThemes.allThemes.forEachIndexed { index, theme ->
            assertEquals(index + 1, theme.id) // 1..12
            assertEquals(expectedNames[index], theme.name)
            assertNotNull(theme.light)
            assertNotNull(theme.main)
            assertNotNull(theme.dark)
            assertEquals(CandyBudgetThemes.textDark, theme.foreground)
        }
    }

    @Test
    fun verify_floorMod_theme_cycling() {
        // Ordinal 0 -> Tangerine (ID 1), 11 -> Blueberry (ID 12), 12 -> Tangerine (ID 1)
        assertEquals("Tangerine", CandyBudgetThemes.getTheme(0).name)
        assertEquals(1, CandyBudgetThemes.getTheme(0).id)
        assertEquals("Blueberry", CandyBudgetThemes.getTheme(11).name)
        assertEquals(12, CandyBudgetThemes.getTheme(11).id)
        assertEquals("Tangerine", CandyBudgetThemes.getTheme(12).name)
        assertEquals("Lavender", CandyBudgetThemes.getTheme(13).name)

        // Negative ordinals
        assertEquals("Blueberry", CandyBudgetThemes.getTheme(-1).name)
        assertEquals("Pistachio", CandyBudgetThemes.getTheme(-2).name)
    }

    @Test
    fun verify_placeholder_and_semantic_tokens() {
        assertNotNull(CandyBudgetThemes.placeholderBackground)
        assertNotNull(CandyBudgetThemes.placeholderBorder)
        assertNotNull(CandyBudgetThemes.placeholderInk)
        assertNotNull(CandyBudgetThemes.placeholderCtaFill)
        assertNotNull(CandyBudgetThemes.placeholderCtaText)
        assertNotNull(CandyBudgetThemes.placeholderWave)
        assertNotNull(CandyBudgetThemes.placeholderBadgeBg)
        assertNotNull(CandyBudgetThemes.coralWarning)
        assertNotNull(CandyBudgetThemes.textDark)
        assertNotNull(CandyBudgetThemes.monthBadgeBg)
    }

    @Test
    fun verify_layout_tokens() {
        assertEquals(216.dp, CandyBudgetThemes.cardMinHeight)
        assertEquals(28.dp, CandyBudgetThemes.cardRadius)
        assertEquals(20.dp, CandyBudgetThemes.contentPadding)
        assertEquals(16.dp, CandyBudgetThemes.pagerSpacing)
        assertEquals(16.dp, CandyBudgetThemes.peek)
        assertEquals(48.dp, CandyBudgetThemes.touchTargetMin)
        assertEquals(32.sp, CandyBudgetThemes.amountTextSize)
        assertEquals(16.sp, CandyBudgetThemes.labelTextSize)
        assertEquals(24.sp, CandyBudgetThemes.monthTextSize)
    }

    @Test
    fun verify_gradient_brush_creation() {
        val brush0 = CandyBudgetThemes.getGradientBrush(0)
        assertNotNull(brush0)
        val brush12 = CandyBudgetThemes.getGradientBrush(12)
        assertNotNull(brush12)
    }
}
