package com.hoabh.chanko.presentation.features.budget.gallery.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.BudgetSetupState
import com.hoabh.chanko.domain.budget.model.BudgetTemporalRole
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.domain.budget.model.MonthlyBudgetOverview
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Thẻ tháng nhỏ (Compact Monthly Card) trên lưới 2 cột Budget Gallery v3:
 * - Aspect ratio: ~1.6, bo góc 20dp, padding 12dp
 * - Palette 12 tháng native gradient
 * - Viền nổi bật nhận diện tháng hiện tại
 * - Thẻ chưa thiết lập: hiển thị button "+ Thiết lập" trên nền thẻ tín dụng bình thường
 * - Thẻ đã thiết lập: chỉ hiển thị số tiền đã chi tiêu và text "Đã chi tiêu"
 * - Hiển thị mascot 36dp ở góc thẻ, tự động ẩn khi font hệ thống >= 150%
 */
@Composable
fun MonthlyBudgetCompactCard(
    overview: MonthlyBudgetOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = CandyBudgetThemes.getGradientBrushForMonth(overview.themeMonth)
    val cardShape = RoundedCornerShape(CandyBudgetThemes.compactCardRadius)

    val fontScale = LocalDensity.current.fontScale
    val showMascot = fontScale < 1.5f

    val isCurrentMonth = overview.temporalRole == BudgetTemporalRole.Current
    val isMissingSetup = overview.setupState == BudgetSetupState.Missing

    val mascotResId = when {
        isMissingSetup -> R.drawable.budget_card_cat_sleepy
        overview.isOverBudget -> R.drawable.budget_card_cat_concerned
        overview.temporalRole == BudgetTemporalRole.Past || overview.temporalRole == BudgetTemporalRole.Future -> R.drawable.budget_card_cat_sleepy
        else -> R.drawable.budget_card_cat_happy
    }

    val spokenText = buildString {
        append("Tháng ${overview.monthNumberText} năm ${overview.yearText}. ")
        if (isCurrentMonth) append("Tháng hiện tại. ")
        if (isMissingSetup) {
            append("Chưa thiết lập ngân sách. Chạm để thiết lập.")
        } else {
            append("Đã chi tiêu ${MoneyVnd.spoken(overview.actualSpentMinor ?: 0L)}. Chạm để xem chi tiết.")
        }
    }

    val cardBorder = if (isCurrentMonth) {
        Modifier.border(width = 2.5.dp, color = CandyColors.brandPurple, shape = cardShape)
    } else {
        Modifier.border(width = 1.dp, color = Color.White.copy(alpha = 0.5f), shape = cardShape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CandyBudgetThemes.compactAspect)
            .defaultMinSize(minHeight = CandyBudgetThemes.touchTargetMin)
            .shadow(elevation = 2.dp, shape = cardShape)
            .clip(cardShape)
            .background(gradientBrush)
            .then(cardBorder)
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .semantics(mergeDescendants = true) {
                contentDescription = spokenText
                role = Role.Button
            }
            .padding(CandyBudgetThemes.compactPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Month chip pill & Decorative Gold Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month badge pill "MM" (+ optional "Hiện tại" badge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                            color = CandyBudgetThemes.textDark
                        )
                    }

                    if (isCurrentMonth) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CandyColors.brandPurple)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Hiện tại",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Decorative Chip (Vector drawn, non-interactive)
                CompactDecorativeChip(
                    modifier = Modifier
                        .size(width = 24.dp, height = 18.dp)
                        .clearAndSetSemantics { }
                )
            }

            // Body Area:
            if (isMissingSetup) {
                // Thẻ chưa thiết lập: hiển thị button "+ Thiết lập"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(
                            role = Role.Button,
                            onClick = onClick
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Thiết lập",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CandyColors.brandPurple
                    )
                }
            } else {
                // Thẻ đã thiết lập: chỉ hiển thị số tiền đã chi tiêu và text "Đã chi tiêu"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = if (showMascot) 38.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Đã chi tiêu",
                        fontFamily = Nunito,
                        fontSize = CandyBudgetThemes.compactLabelTextSize,
                        color = CandyBudgetThemes.textDark.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = MoneyVnd.format(overview.actualSpentMinor ?: 0L),
                        fontFamily = Nunito,
                        fontSize = CandyBudgetThemes.compactAmountTextSize,
                        fontWeight = FontWeight.Bold,
                        color = CandyBudgetThemes.textDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showMascot) {
            Image(
                painter = painterResource(id = mascotResId),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
internal fun CompactDecorativeChip(modifier: Modifier = Modifier) {
    val chipFill = CandyColors.HomeV2.chipGold
    val chipStroke = CandyColors.HomeV2.chipGoldStroke
    Canvas(modifier = modifier) {
        val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        drawRoundRect(
            color = chipFill,
            cornerRadius = corner
        )
        drawRoundRect(
            color = chipStroke,
            cornerRadius = corner,
            style = Stroke(width = 1.dp.toPx())
        )
        val w = size.width
        val h = size.height
        drawLine(
            color = chipStroke,
            start = Offset(w * 0.33f, 0f),
            end = Offset(w * 0.33f, h),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = chipStroke,
            start = Offset(w * 0.67f, 0f),
            end = Offset(w * 0.67f, h),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = chipStroke,
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 0.8.dp.toPx()
        )
    }
}
