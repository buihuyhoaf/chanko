package com.hoabh.chanko.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R

val Nunito = FontFamily(
    Font(R.font.nunito_400, weight = FontWeight(400)),
    Font(R.font.nunito_500, weight = FontWeight(500)),
    Font(R.font.nunito_600, weight = FontWeight(600)),
    Font(R.font.nunito_700, weight = FontWeight(700)),
    Font(R.font.nunito_800, weight = FontWeight(800))
)

// Letter spacing 0 is Reference; sizes, weights and base line heights are UI-03.
private fun style(size: Int, height: Int, weight: Int) = TextStyle(
    fontFamily = Nunito, fontSize = size.sp, lineHeight = height.sp,
    fontWeight = FontWeight(weight), letterSpacing = 0.sp
)

val ChankoTypography = Typography(
    displayLarge = style(36, 44, 800),
    displayMedium = style(32, 40, 800),
    headlineLarge = style(28, 36, 800),
    headlineMedium = style(24, 32, 700),
    titleLarge = style(20, 28, 700),
    titleMedium = style(17, 24, 700),
    titleSmall = style(15, 22, 600),
    bodyLarge = style(16, 24, 400),
    bodyMedium = style(14, 20, 400),
    bodySmall = style(12, 16, 500),
    labelLarge = style(16, 24, 700),
    labelMedium = style(14, 20, 600),
    labelSmall = style(12, 16, 700),
    // Reference fallbacks for the two Material slots outside the approved 13.
    displaySmall = style(28, 36, 800),
    headlineSmall = style(20, 28, 700)
)

// Financial line heights are Reference (PV-04).
object FinancialTypography {
    val budgetSummary = style(28, 36, 800)
    val productPrice = style(20, 28, 700)
    val remainingBudget = style(18, 26, 700)
    val smallAmount = style(14, 20, 600)
    val countdown = style(24, 32, 800)
}
