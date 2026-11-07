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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

/**
 * Thẻ tháng nhỏ (Compact Monthly Card) trên lưới 2 cột Budget Gallery v3 (04-COMPOSE-GUIDE.md).
 * - Aspect ratio: ~1.6
 * - Bo góc 20dp, padding 12dp
 * - Tương tác chuẩn: min touch target 48dp
 * - Palette 12 tháng native gradient
 * - Hiển thị mascot 36dp ở góc thẻ, tự động ẩn khi font hệ thống >= 150%
 */
@Composable
fun MonthlyBudgetCompactCard(
    overview: MonthlyBudgetOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (overview.setupState == BudgetSetupState.Missing && overview.temporalRole == BudgetTemporalRole.Future) {
        BudgetCompactPlaceholderCard(
            overview = overview,
            onClick = onClick,
            modifier = modifier
        )
        return
    }

    val theme = CandyBudgetThemes.forMonth(overview.themeMonth)
    val gradientBrush = CandyBudgetThemes.getGradientBrushForMonth(overview.themeMonth)
    val cardShape = RoundedCornerShape(CandyBudgetThemes.compactCardRadius)

    val fontScale = LocalDensity.current.fontScale
    val showMascot = fontScale < 1.5f

    val mascotResId = when {
        overview.isOverBudget -> R.drawable.budget_card_cat_concerned
        overview.temporalRole == BudgetTemporalRole.Past || overview.temporalRole == BudgetTemporalRole.Future -> R.drawable.budget_card_cat_sleepy
        else -> R.drawable.budget_card_cat_happy
    }

    val spokenText = buildString {
        append("Tháng ${overview.monthNumberText} năm ${overview.yearText}. ")
        when (overview.temporalRole) {
            BudgetTemporalRole.Past -> {
                if (overview.isOverBudget) {
                    append("Vượt ngân sách ${overview.overMinor?.let { MoneyVnd.spoken(it) } ?: ""}. ")
                } else {
                    append("Còn lại cuối kỳ ${overview.remainingMinor?.let { MoneyVnd.spoken(it) } ?: ""}. ")
                }
            }
            BudgetTemporalRole.Current -> {
                if (overview.isOverBudget) {
                    append("Vượt ngân sách ${overview.overMinor?.let { MoneyVnd.spoken(it) } ?: ""}. ")
                } else {
                    append("Ngân sách còn lại ${overview.remainingMinor?.let { MoneyVnd.spoken(it) } ?: ""}. ")
                }
                overview.usagePercent?.let { append("Đã dùng $it phần trăm. ") }
            }
            BudgetTemporalRole.Future -> {
                append("Ngân sách dự kiến ${overview.limitMinor?.let { MoneyVnd.spoken(it) } ?: ""}. Chưa bắt đầu. ")
            }
        }
        append("Chạm để xem chi tiết.")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CandyBudgetThemes.compactAspect)
            .defaultMinSize(minHeight = CandyBudgetThemes.touchTargetMin)
            .shadow(elevation = 2.dp, shape = cardShape)
            .clip(cardShape)
            .background(gradientBrush)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.5f), shape = cardShape)
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
                // Month badge pill "MM"
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

                // Decorative Chip (Vector drawn, non-interactive)
                CompactDecorativeChip(
                    modifier = Modifier
                        .size(width = 24.dp, height = 18.dp)
                        .clearAndSetSemantics { }
                )
            }

            // Body: Data label, Amount, and Status Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = if (showMascot) 38.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when (overview.temporalRole) {
                    BudgetTemporalRole.Past -> {
                        if (overview.isOverBudget) {
                            Text(
                                text = "Vượt",
                                fontSize = CandyBudgetThemes.compactLabelTextSize,
                                fontWeight = FontWeight.SemiBold,
                                color = CandyBudgetThemes.coralWarning,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = MoneyVnd.format(overview.overMinor ?: 0L),
                                fontSize = CandyBudgetThemes.compactAmountTextSize,
                                fontWeight = FontWeight.Bold,
                                color = CandyBudgetThemes.coralWarning,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "Còn lại cuối kỳ",
                                fontSize = CandyBudgetThemes.compactLabelTextSize,
                                color = CandyBudgetThemes.textDark.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = MoneyVnd.format(overview.remainingMinor ?: 0L),
                                fontSize = CandyBudgetThemes.compactAmountTextSize,
                                fontWeight = FontWeight.Bold,
                                color = CandyBudgetThemes.textDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    BudgetTemporalRole.Current -> {
                        if (overview.isOverBudget) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vượt ngân sách",
                                    fontSize = CandyBudgetThemes.compactLabelTextSize,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CandyBudgetThemes.coralWarning,
                                    maxLines = 1
                                )
                                overview.usagePercent?.let { percent ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(CandyBudgetThemes.coralWarning)
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$percent%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = MoneyVnd.format(overview.overMinor ?: 0L),
                                fontSize = CandyBudgetThemes.compactAmountTextSize,
                                fontWeight = FontWeight.Bold,
                                color = CandyBudgetThemes.coralWarning,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Còn lại",
                                    fontSize = CandyBudgetThemes.compactLabelTextSize,
                                    color = CandyBudgetThemes.textDark.copy(alpha = 0.75f),
                                    maxLines = 1
                                )
                                overview.usagePercent?.let { percent ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color.White.copy(alpha = 0.6f))
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$percent%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CandyBudgetThemes.textDark
                                        )
                                    }
                                }
                            }
                            Text(
                                text = MoneyVnd.format(overview.remainingMinor ?: 0L),
                                fontSize = CandyBudgetThemes.compactAmountTextSize,
                                fontWeight = FontWeight.Bold,
                                color = CandyBudgetThemes.textDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    BudgetTemporalRole.Future -> {
                        Text(
                            text = "Ngân sách dự kiến",
                            fontSize = CandyBudgetThemes.compactLabelTextSize,
                            color = CandyBudgetThemes.textDark.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = MoneyVnd.format(overview.limitMinor ?: 0L),
                                fontSize = CandyBudgetThemes.compactAmountTextSize,
                                fontWeight = FontWeight.Bold,
                                color = CandyBudgetThemes.textDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Chưa bắt đầu",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CandyBudgetThemes.textDark.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
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
