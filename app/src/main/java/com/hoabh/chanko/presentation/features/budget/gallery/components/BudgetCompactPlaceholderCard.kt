package com.hoabh.chanko.presentation.features.budget.gallery.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.MonthlyBudgetOverview
import com.hoabh.chanko.ui.theme.CandyBudgetThemes

/**
 * Thẻ rỗng (Missing placeholder) cho tháng tương lai chưa thiết lập ngân sách (v3):
 * - Viền đứt nét (dashed border)
 * - Nền trung tính mềm mại
 * - Nút "+ Thiết lập" CTA
 */
@Composable
fun BudgetCompactPlaceholderCard(
    overview: MonthlyBudgetOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(CandyBudgetThemes.compactCardRadius)
    val spokenText = stringResource(
        R.string.budget_compact_spoken_not_configured,
        overview.monthNumberText,
        overview.yearText
    )

    val strokeColor = CandyBudgetThemes.placeholderBorder
    val bgColor = CandyBudgetThemes.placeholderBackground

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CandyBudgetThemes.compactAspect)
            .defaultMinSize(minHeight = CandyBudgetThemes.touchTargetMin)
            .clip(cardShape)
            .background(bgColor)
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .semantics(mergeDescendants = true) {
                contentDescription = spokenText
                role = Role.Button
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.5.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            val corner = CornerRadius(CandyBudgetThemes.compactCardRadius.toPx(), CandyBudgetThemes.compactCardRadius.toPx())

            drawRoundRect(
                color = strokeColor,
                cornerRadius = corner,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = dashEffect
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CandyBudgetThemes.compactPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Month Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = overview.monthNumberText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyBudgetThemes.placeholderInk
                )
            }

            // Body: "Chưa thiết lập" & CTA Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.budget_compact_not_configured),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = CandyBudgetThemes.placeholderInk.copy(alpha = 0.7f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(CandyBudgetThemes.placeholderCtaFill)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.budget_compact_setup_button),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyBudgetThemes.placeholderCtaText
                    )
                }
            }
        }
    }
}
