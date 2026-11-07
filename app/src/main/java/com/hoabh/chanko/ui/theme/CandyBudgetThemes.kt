package com.hoabh.chanko.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quản lý 12 Candy Gradient Themes cho chu kỳ ngân sách và thẻ trung tính (Home v2.2 - BC22-08, BC22-09, BC22-10).
 * Theo design-tokens.json:
 * Linear gradient sRGB 3 điểm: startNormalized: [0, 0] -> endNormalized: [1, 1], positions: [0, 0.5, 1]
 * (Light -> Main -> Dark).
 */
object CandyBudgetThemes {

    // Layout Tokens (design-tokens.json)
    val cardMinHeight = 216.dp
    val cardRadius = 28.dp
    val contentPadding = 20.dp
    val pagerSpacing = 16.dp
    val peek = 16.dp
    val touchTargetMin = 48.dp
    val amountTextSize = 32.sp
    val labelTextSize = 16.sp
    val monthTextSize = 24.sp

    // Budget v3 Compact Card Tokens (04-COMPOSE-GUIDE.md)
    val compactCardRadius = 20.dp
    val compactAmountTextSize = 16.sp
    val compactLabelTextSize = 12.sp
    val compactPadding = 12.dp
    val compactAspect = 1.6f

    fun getTheme(ordinal: Int): CandyColors.BudgetThemes.GradientTheme {
        return CandyColors.BudgetThemes.forOrdinal(ordinal)
    }

    /**
     * Map tháng 01..12 trực tiếp tới palette 01..12 theo Budget v3 (04-COMPOSE-GUIDE.md).
     */
    fun forMonth(month: Int): CandyColors.BudgetThemes.GradientTheme {
        val safeMonth = month.coerceIn(1, 12)
        return CandyColors.BudgetThemes.allThemes[safeMonth - 1]
    }

    /**
     * Tạo Brush gradient tuyến tính 3 điểm (Light -> Main -> Dark)
     * chuẩn hóa theo design-tokens.json.
     */
    fun getGradientBrush(ordinal: Int): Brush {
        val theme = getTheme(ordinal)
        return Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to theme.light,
                0.5f to theme.main,
                1.0f to theme.dark
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 650f)
        )
    }

    /**
     * Tạo Brush gradient cho thẻ tháng theo số tháng (1..12).
     */
    fun getGradientBrushForMonth(month: Int): Brush {
        val theme = forMonth(month)
        return Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to theme.light,
                0.5f to theme.main,
                1.0f to theme.dark
            ),
            start = Offset(0f, 0f),
            end = Offset(450f, 280f)
        )
    }

    // Neutral Placeholder Tokens (design-tokens.json)
    val placeholderBackground: Color get() = CandyColors.BudgetThemes.placeholderBg
    val placeholderBorder: Color get() = CandyColors.BudgetThemes.placeholderBorder
    val placeholderInk: Color get() = CandyColors.BudgetThemes.placeholderInk
    val placeholderCtaFill: Color get() = CandyColors.BudgetThemes.placeholderCtaFill
    val placeholderCtaText: Color get() = CandyColors.BudgetThemes.placeholderCtaText
    val placeholderWave: Color get() = CandyColors.BudgetThemes.placeholderWave
    val placeholderBadgeBg: Color get() = CandyColors.BudgetThemes.placeholderBadgeBg

    // Semantic tokens (design-tokens.json)
    val textDark: Color get() = CandyColors.BudgetThemes.textDark
    val monthBadgeBg: Color get() = CandyColors.BudgetThemes.monthBadgeBg
    val coralWarning: Color get() = CandyColors.BudgetThemes.coral
}
