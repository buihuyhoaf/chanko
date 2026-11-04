package com.hoabh.chanko.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Quản lý 12 Candy Gradient Themes cho chu kỳ ngân sách và thẻ trung tính (Home v2.2 - BC22-08, BC22-09, BC22-10).
 * Gradient chạy từ Sáng (top-left) -> Chính (center) -> Đậm (bottom-right).
 */
object CandyBudgetThemes {

    fun getTheme(ordinal: Int): CandyColors.BudgetThemes.GradientTheme {
        return CandyColors.BudgetThemes.forOrdinal(ordinal)
    }

    fun getGradientBrush(ordinal: Int): Brush {
        val theme = getTheme(ordinal)
        return Brush.linearGradient(
            colors = listOf(theme.light, theme.main, theme.dark),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Neutral Placeholder Tokens (BC22-05)
    val placeholderBackground: Color get() = CandyColors.BudgetThemes.placeholderBg
    val placeholderBorder: Color get() = CandyColors.BudgetThemes.placeholderBorder
    val placeholderInk: Color get() = CandyColors.BudgetThemes.placeholderInk

    // Cảnh báo vượt ngân sách (BC22-13)
    val coralWarning: Color get() = CandyColors.BudgetThemes.coral
}
