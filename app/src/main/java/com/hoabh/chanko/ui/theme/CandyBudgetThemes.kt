package com.hoabh.chanko.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Quản lý 12 Candy Gradient Themes cho chu kỳ ngân sách và thẻ trung tính (Home v2.2 - BC22-08, BC22-09, BC22-10).
 * Thẻ sử dụng dải màu Candy Pop 3D chuẩn: Main -> Dark theo góc xiên (Offset(0f, 0f) -> Offset(900f, 650f))
 * kết hợp viền sáng Light và bóng đổ ấm cùng tone màu.
 */
object CandyBudgetThemes {

    fun getTheme(ordinal: Int): CandyColors.BudgetThemes.GradientTheme {
        return CandyColors.BudgetThemes.forOrdinal(ordinal)
    }

    /**
     * Tạo Brush gradient góc xiên chuẩn Chanko (Offset(0f, 0f) -> Offset(900f, 650f))
     * từ màu chính (main) sang màu đậm (dark), mang lại cảm giác mượt mà và bóng bẩy 3D.
     */
    fun getGradientBrush(ordinal: Int): Brush {
        val theme = getTheme(ordinal)
        return Brush.linearGradient(
            colors = listOf(theme.main, theme.dark),
            start = Offset(0f, 0f),
            end = Offset(900f, 650f)
        )
    }

    // Neutral Placeholder Tokens (BC22-05)
    val placeholderBackground: Color get() = CandyColors.BudgetThemes.placeholderBg
    val placeholderBorder: Color get() = CandyColors.BudgetThemes.placeholderBorder
    val placeholderInk: Color get() = CandyColors.BudgetThemes.placeholderInk

    // Cảnh báo vượt ngân sách (BC22-13)
    val coralWarning: Color get() = CandyColors.BudgetThemes.coral
}
