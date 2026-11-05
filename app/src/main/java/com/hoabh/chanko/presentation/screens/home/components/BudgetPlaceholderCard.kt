package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.hoabh.chanko.presentation.screens.home.BudgetCardRole
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandySpace

private val PlaceholderCornerShape = RoundedCornerShape(CandyBudgetThemes.cardRadius)
private val PlaceholderMinHeight = CandyBudgetThemes.cardMinHeight

/**
 * Thẻ Trung Tính Placeholder cho Chu Kỳ Tiếp Theo Chưa Cấu Hình (Home v2.2 - BC22-05, BC-CARD-LABEL-01, BC-CARD-CONTENT-01):
 * - Nền trắng ngà/xám pastel (#FBF9FD), viền nét đứt native (#82758E, dash = 6dp, gap = 4dp).
 * - Góc trên bên phải có Month Chip pill MM (01..12).
 * - Tiêu đề: "Chu kỳ tiếp theo", phụ đề: "Chưa thiết lập ngân sách".
 * - Nút CTA dạng pill "+ Thiết lập ngân sách" nền tím #6326A8, chữ trắng.
 */
@Composable
fun BudgetPlaceholderCard(
    monthLabel: String,
    cycleLabel: String = "",
    dateRangeText: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: BudgetCardRole = BudgetCardRole.NextPlaceholder
) {
    val borderColor = CandyBudgetThemes.placeholderBorder
    val bgColor = CandyBudgetThemes.placeholderBackground
    val ctaFill = CandyBudgetThemes.placeholderCtaFill
    val ctaText = CandyBudgetThemes.placeholderCtaText
    val titleText = stringResource(R.string.home_budget_placeholder_title)
    val subtitleText = stringResource(R.string.home_budget_placeholder_unconfigured_subtitle)
    val ctaLabel = stringResource(R.string.home_budget_placeholder_cta)

    val cardCd = buildString {
        append("$titleText, ")
        if (monthLabel.isNotBlank()) append("tháng $monthLabel. ")
        append("$subtitleText. ")
        append("Chạm để $ctaLabel.")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PlaceholderMinHeight)
            .clip(PlaceholderCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                this.role = Role.Button
            },
        shape = PlaceholderCornerShape,
        color = bgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CandyBudgetThemes.contentPadding)
        ) {
            // Viền nét đứt native Compose (dash = 6.dp, gap = 4.dp, radius = 28.dp)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val dash = 6.dp.toPx()
                val gap = 4.dp.toPx()
                val radiusPx = CandyBudgetThemes.cardRadius.toPx()
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CandySpace.xs),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TopBar: Brand trang trí bên trái + Month Chip pill bên phải
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_budget_brand_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = CandyBudgetThemes.textDark
                            )
                            Text(
                                text = stringResource(R.string.home_budget_brand_subtitle),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.4.sp
                                ),
                                color = CandyBudgetThemes.placeholderBorder
                            )
                        }
                    }

                    if (monthLabel.isNotBlank()) {
                        BudgetMonthChip(monthLabel = monthLabel)
                    }
                }

                // 2. Nội dung trung tâm: Tiêu đề + phụ đề
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CandySpace.xs)
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = CandyBudgetThemes.textDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = CandyBudgetThemes.placeholderBorder
                    )
                }

                // 3. Nút CTA dạng pill tím #6326A8, chữ trắng (BC-CARD-CONTENT-01)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(999.dp),
                    color = ctaFill,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ctaLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = ctaText
                        )
                    }
                }
            }
        }
    }
}
