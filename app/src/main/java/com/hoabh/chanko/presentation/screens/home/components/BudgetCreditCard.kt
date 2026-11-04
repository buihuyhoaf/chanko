package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.BudgetCreditCardUi
import com.hoabh.chanko.presentation.screens.home.BudgetCardRole
import com.hoabh.chanko.presentation.screens.home.BudgetCardSlotUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

private val CreditCardCornerShape = RoundedCornerShape(24.dp)
private val ProgressTrackCornerShape = RoundedCornerShape(999.dp)
private val PercentPillCornerShape = RoundedCornerShape(999.dp)
private val CreditCardMinHeight = 196.dp

/**
 * Khu vực 2 — Budget Credit Card 3D Candy Orange (Home v2.0 — AC-03..AC-09, AC-18..AC-22, AC-31):
 * - Thiết kế thẻ mua sắm 3D Candy Orange (#FFB665 -> #F59145), có dấu chân + CHANKO SHOPPING BUDGET,
 *   chip vàng EMV, sóng contactless và mèo 3D trang trí (tất cả phần trang trí đều tắt focus TalkBack).
 * - Không có số thẻ, CVV hay logo ngân hàng thật (AC-05).
 * - Toàn thẻ mở `BudgetScreen` một lần duy nhất; là lối vào Budget duy nhất trên Home (AC-04).
 * - Khi vượt ngân sách (`spent > limit`): giữ nguyên nền cam, hiển thị cảnh báo/số vượt bằng màu Coral (#B83248),
 *   thanh tiến trình clamp trong miền `0..1` (AC-06).
 * - Hỗ trợ đầy đủ: `NoBudget` (AC-07), `Error + Retry cục bộ` (AC-08), `MissingPrice` (AC-09),
 *   `Skeleton Loading` (AC-18) và `OfflineNoCache` (AC-21).
 */
@Composable
fun BudgetCreditCard(
    budgetState: SectionState<BudgetCreditCardUi>,
    onCardClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (budgetState) {
        SectionState.Loading -> {
            BudgetCreditCardSkeleton(modifier = modifier)
        }

        is SectionState.OfflineNoCache -> {
            BudgetCreditCardStatusFrame(
                title = stringResource(R.string.home_budget_offline_no_cache_title),
                subtitle = stringResource(R.string.home_budget_offline_no_cache_subtitle),
                actionLabel = null,
                onCardClick = onCardClick,
                onActionClick = onCardClick,
                modifier = modifier
            )
        }

        is SectionState.Error -> {
            BudgetCreditCardStatusFrame(
                title = budgetState.message.ifBlank {
                    stringResource(R.string.home_section_error_budget)
                },
                subtitle = stringResource(R.string.home_budget_offline_no_cache_subtitle),
                actionLabel = stringResource(R.string.home_section_retry),
                onCardClick = onCardClick,
                onActionClick = onRetryClick,
                modifier = modifier
            )
        }

        is SectionState.Empty -> {
            BudgetCreditCardStatusFrame(
                title = stringResource(R.string.home_budget_no_budget_title),
                subtitle = stringResource(R.string.home_budget_no_budget_subtitle),
                actionLabel = stringResource(R.string.home_budget_no_budget_cta),
                onCardClick = onCardClick,
                onActionClick = onCardClick,
                modifier = modifier
            )
        }

        is SectionState.Content -> {
            val budget = budgetState.data
            if (!budget.isConfigured) {
                BudgetCreditCardStatusFrame(
                    title = stringResource(R.string.home_budget_no_budget_title),
                    subtitle = stringResource(R.string.home_budget_no_budget_subtitle),
                    actionLabel = stringResource(R.string.home_budget_no_budget_cta),
                    onCardClick = onCardClick,
                    onActionClick = onCardClick,
                    modifier = modifier
                )
            } else {
                BudgetCreditCardContent(
                    budget = budget,
                    onClick = onCardClick,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun BudgetCreditCardContent(
    budget: BudgetCreditCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingLabel = stringResource(R.string.home_budget_remaining_label)
    val spentSummaryText = stringResource(
        R.string.home_budget_spent_summary,
        budget.spentFormatted,
        budget.limitFormatted
    )
    val fullSummaryDescription = buildString {
        append("$remainingLabel: ${budget.remainingFormatted}. ")
        budget.overWarningFormatted?.let { append("$it. ") }
        budget.missingPriceNote?.let { append("$it. ") }
        append("$spentSummaryText, tiến độ ${budget.progressPercentText}. ")
        append("Chu kỳ ${budget.cycleLabel}.")
    }
    val cardCd = stringResource(R.string.home_cd_budget_card, fullSummaryDescription)

    val cardBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                CandyColors.HomeV2.candyOrangeHighlight,
                CandyColors.HomeV2.candyOrange,
                CandyColors.HomeV2.candyOrangeShadow
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CreditCardMinHeight)
            .shadow(
                elevation = 8.dp,
                shape = CreditCardCornerShape,
                ambientColor = CandyColors.HomeV2.candyOrangeShadow,
                spotColor = CandyColors.HomeV2.candyOrangeShadow
            )
            .clip(CreditCardCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                role = Role.Button
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = budget.visualProgress.coerceIn(0f, 1f),
                    range = 0f..1f
                )
            },
        shape = CreditCardCornerShape,
        color = CandyColors.HomeV2.candyOrange,
        border = BorderStroke(1.5.dp, CandyColors.HomeV2.candyOrangeHighlight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBrush)
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md)
        ) {
            // Mèo Chanko trang trí bên phải thẻ lấy từ bộ resource mascot_xxx (chỉ trang trí — không nhận focus TalkBack AC-31)
            Image(
                painter = painterResource(
                    id = if (budget.isOverBudget) {
                        R.drawable.mascot_worried
                    } else {
                        R.drawable.mascot_happy
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(96.dp)
                    .offset(x = 6.dp, y = 4.dp)
                    .clearAndSetSemantics {}
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                // 1. Hàng trên: Dấu chân + CHANKO SHOPPING BUDGET | Chip vàng + Contactless
                CreditCardTopBar()

                Spacer(modifier = Modifier.height(CandySpace.xs))

                // 2. Nhãn "Ngân sách còn lại" + Số tiền 32sp ExtraBold
                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = CandyColors.HomeV2.textPrimary
                )

                Text(
                    text = budget.remainingFormatted,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        lineHeight = 38.sp
                    ),
                    color = if (budget.isOverBudget) {
                        CandyColors.HomeV2.coral
                    } else {
                        CandyColors.HomeV2.textPrimary
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Cảnh báo vượt ngân sách bằng màu Coral trên nền cam (AC-06)
                if (budget.overWarningFormatted != null) {
                    Surface(
                        shape = PercentPillCornerShape,
                        color = CandyColors.HomeV2.coralSoft,
                        border = BorderStroke(1.dp, CandyColors.HomeV2.coral)
                    ) {
                        Text(
                            text = budget.overWarningFormatted,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.HomeV2.coral,
                            modifier = Modifier.padding(
                                horizontal = CandySpace.sm,
                                vertical = 2.dp
                            )
                        )
                    }
                }

                // Chú thích dữ liệu giá chưa đầy đủ khi có món thiếu giá (AC-09)
                if (budget.missingPriceNote != null) {
                    Surface(
                        shape = PercentPillCornerShape,
                        color = CandyColors.HomeV2.candyOrangeTrack
                    ) {
                        Text(
                            text = budget.missingPriceNote,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.HomeV2.textPrimary,
                            modifier = Modifier.padding(
                                horizontal = CandySpace.sm,
                                vertical = 2.dp
                            )
                        )
                    }
                }

                // 3. Thanh tiến trình capsule (0..1) + Pill phần trăm (53%)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(ProgressTrackCornerShape)
                            .background(CandyColors.HomeV2.candyOrangeTrack)
                            .border(
                                width = 1.dp,
                                color = CandyColors.HomeV2.candyOrangeHighlight,
                                shape = ProgressTrackCornerShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(budget.visualProgress.coerceIn(0f, 1f))
                                .clip(ProgressTrackCornerShape)
                                .background(
                                    if (budget.isOverBudget) {
                                        CandyColors.HomeV2.coral
                                    } else {
                                        CandyColors.HomeV2.candyOrangeProgressFill
                                    }
                                )
                        )
                    }

                    Surface(
                        shape = PercentPillCornerShape,
                        color = CandyColors.HomeV2.candyOrangeTrack,
                        border = BorderStroke(1.dp, CandyColors.HomeV2.candyOrangeHighlight)
                    ) {
                        Text(
                            text = budget.progressPercentText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = if (budget.isOverBudget) {
                                CandyColors.HomeV2.coral
                            } else {
                                CandyColors.HomeV2.textPrimary
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 4. Dòng "Đã chi 2.650.000đ / 5.000.000đ" & Chu kỳ "01/11 – 30/11"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_01,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = spentSummaryText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = CandyColors.HomeV2.textPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_05,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = budget.cycleLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = CandyColors.HomeV2.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditCardTopBar(
    badge: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bên trái: Dấu chân trắng + CHANKO / SHOPPING BUDGET + Huy hiệu trạng thái
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            DecorativePawMark(modifier = Modifier.size(28.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_budget_brand_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.home_budget_brand_subtitle),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }
            badge?.invoke()
        }

        // Bên phải: Chip vàng EMV + Sóng Contactless
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            DecorativeGoldChip(modifier = Modifier.size(width = 36.dp, height = 26.dp))
            DecorativeContactlessWaves(modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun DecorativePawMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Đệm chân chính
        drawOval(
            color = Color.White,
            topLeft = Offset(w * 0.22f, h * 0.44f),
            size = Size(w * 0.56f, h * 0.44f)
        )
        // 4 ngón chân tròn
        drawCircle(color = Color.White, radius = w * 0.11f, center = Offset(w * 0.18f, h * 0.34f))
        drawCircle(color = Color.White, radius = w * 0.12f, center = Offset(w * 0.38f, h * 0.18f))
        drawCircle(color = Color.White, radius = w * 0.12f, center = Offset(w * 0.62f, h * 0.18f))
        drawCircle(color = Color.White, radius = w * 0.11f, center = Offset(w * 0.82f, h * 0.34f))
    }
}

@Composable
private fun DecorativeGoldChip(modifier: Modifier = Modifier) {
    val chipFill = CandyColors.HomeV2.chipGold
    val chipStroke = CandyColors.HomeV2.chipGoldStroke
    Canvas(modifier = modifier) {
        val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        drawRoundRect(
            color = chipFill,
            cornerRadius = corner
        )
        drawRoundRect(
            color = chipStroke,
            cornerRadius = corner,
            style = Stroke(width = 1.2.dp.toPx())
        )
        // Đường chia mạch chip EMV
        val w = size.width
        val h = size.height
        drawLine(
            color = chipStroke,
            start = Offset(w * 0.33f, 0f),
            end = Offset(w * 0.33f, h),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = chipStroke,
            start = Offset(w * 0.67f, 0f),
            end = Offset(w * 0.67f, h),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = chipStroke,
            start = Offset(0f, h * 0.5f),
            end = Offset(w, h * 0.5f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun DecorativeContactlessWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        val radii = listOf(0.28f, 0.50f, 0.72f, 0.94f)
        radii.forEach { ratio ->
            val d = size.minDimension * ratio
            val topLeft = Offset((size.width - d) / 2f - 4.dp.toPx(), (size.height - d) / 2f)
            drawArc(
                color = Color.White,
                startAngle = -42f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(d, d),
                style = stroke
            )
        }
    }
}

@Composable
internal fun BudgetCreditCardStatusFrame(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                CandyColors.HomeV2.candyOrangeHighlight,
                CandyColors.HomeV2.candyOrange,
                CandyColors.HomeV2.candyOrangeShadow
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CreditCardMinHeight)
            .shadow(elevation = 6.dp, shape = CreditCardCornerShape)
            .clip(CreditCardCornerShape)
            .clickable(role = Role.Button, onClick = onCardClick),
        shape = CreditCardCornerShape,
        color = CandyColors.HomeV2.candyOrange,
        border = BorderStroke(1.5.dp, CandyColors.HomeV2.candyOrangeHighlight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBrush)
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            CreditCardTopBar()

            Spacer(modifier = Modifier.height(CandySpace.xs))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.HomeV2.textPrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CandyColors.HomeV2.textPrimary
            )

            if (actionLabel != null) {
                Button(
                    onClick = onActionClick,
                    shape = PercentPillCornerShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.HomeV2.textPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun BudgetCreditCardSkeleton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(CreditCardMinHeight)
            .clearAndSetSemantics {},
        shape = CreditCardCornerShape,
        color = CandyColors.HomeV2.candyOrangeTrack,
        border = BorderStroke(1.dp, CandyColors.HomeV2.candyOrangeHighlight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 24.dp)
                        .clip(PercentPillCornerShape)
                        .background(CandyColors.HomeV2.candyOrangeHighlight)
                )
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 24.dp)
                        .clip(PercentPillCornerShape)
                        .background(CandyColors.HomeV2.candyOrangeHighlight)
                )
            }

            Box(
                modifier = Modifier
                    .size(width = 180.dp, height = 36.dp)
                    .clip(PercentPillCornerShape)
                    .background(CandyColors.HomeV2.candyOrangeHighlight)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(PercentPillCornerShape)
                    .background(CandyColors.HomeV2.candyOrangeHighlight)
            )

            Box(
                modifier = Modifier
                    .size(width = 150.dp, height = 16.dp)
                    .clip(PercentPillCornerShape)
                    .background(CandyColors.HomeV2.candyOrangeHighlight)
            )
        }
    }
}

/**
 * Composable hiển thị từng slot thẻ trên Carousel v2.2 (Home v2.2 - BC22-01..BC22-22).
 */
@Composable
fun BudgetCreditCardSlotView(
    slot: BudgetCardSlotUi,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (slot) {
        is BudgetCardSlotUi.UpcomingPlaceholderCard -> {
            BudgetPlaceholderCard(
                cycleLabel = slot.cycleLabel,
                dateRangeText = slot.dateRangeText,
                onClick = onCardClick,
                role = slot.role,
                modifier = modifier
            )
        }

        is BudgetCardSlotUi.CurrentEmptyCard -> {
            BudgetPlaceholderCard(
                cycleLabel = slot.cycleLabel,
                dateRangeText = slot.dateRangeText,
                onClick = onCardClick,
                role = slot.role,
                modifier = modifier
            )
        }

        is BudgetCardSlotUi.UpcomingConfiguredCard -> {
            UpcomingConfiguredCardContent(
                slot = slot,
                onClick = onCardClick,
                modifier = modifier
            )
        }

        is BudgetCardSlotUi.ClosedCard -> {
            ClosedCardContent(
                slot = slot,
                onClick = onCardClick,
                modifier = modifier
            )
        }

        is BudgetCardSlotUi.ActiveCard -> {
            ActiveCardContent(
                slot = slot,
                onClick = onCardClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ActiveCardContent(
    slot: BudgetCardSlotUi.ActiveCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingLabel = stringResource(R.string.home_budget_remaining_label)
    val spentSummaryText = stringResource(
        R.string.home_budget_spent_summary,
        slot.spentFormatted,
        slot.limitFormatted
    )
    val cardCd = buildString {
        append("Thẻ ngân sách đang diễn ra, ${slot.cycleLabel}. ")
        append("Hạn mức: ${slot.limitFormatted}, đã chi: ${slot.spentFormatted}. ")
        if (slot.isOverBudget) {
            append("Vượt ngân sách: ${slot.overFormatted}. ")
        } else {
            append("Còn lại: ${slot.remainingFormatted}. ")
        }
        append("Tiến độ: ${slot.realPercentText}. Chạm để xem chi tiết.")
    }

    val cardBrush = remember(slot.themeOrdinal) {
        CandyBudgetThemes.getGradientBrush(slot.themeOrdinal)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CreditCardMinHeight)
            .shadow(
                elevation = 8.dp,
                shape = CreditCardCornerShape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(CreditCardCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                role = Role.Button
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = slot.clampedProgress,
                    range = 0f..1f
                )
            },
        shape = CreditCardCornerShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBrush)
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md)
        ) {
            Image(
                painter = painterResource(
                    id = if (slot.isOverBudget) {
                        R.drawable.mascot_worried
                    } else {
                        R.drawable.mascot_happy
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(96.dp)
                    .offset(x = 6.dp, y = 4.dp)
                    .clearAndSetSemantics {}
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                CreditCardTopBar(badge = { BudgetCycleBadge(role = BudgetCardRole.Current) })

                Spacer(modifier = Modifier.height(CandySpace.xs))

                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = if (slot.isOverBudget) {
                        stringResource(R.string.home_budget_over_warning, slot.overFormatted.orEmpty())
                    } else {
                        slot.remainingFormatted
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (slot.isOverBudget) 24.sp else 32.sp,
                        lineHeight = 36.sp
                    ),
                    color = if (slot.isOverBudget) CandyBudgetThemes.coralWarning else Color.White
                )

                // Thanh tiến trình
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(ProgressTrackCornerShape)
                            .background(Color.White.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(slot.clampedProgress)
                                .clip(ProgressTrackCornerShape)
                                .background(
                                    if (slot.isOverBudget) {
                                        CandyBudgetThemes.coralWarning
                                    } else {
                                        Color.White
                                    }
                                )
                        )
                    }

                    Surface(
                        shape = PercentPillCornerShape,
                        color = Color.White.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = slot.realPercentText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = if (slot.isOverBudget) CandyBudgetThemes.coralWarning else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_01,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = spentSummaryText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_05,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = "${slot.cycleLabel} (${slot.dateRangeText})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ClosedCardContent(
    slot: BudgetCardSlotUi.ClosedCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingLabel = stringResource(R.string.home_budget_remaining_label)
    val spentSummaryText = stringResource(
        R.string.home_budget_spent_summary,
        slot.spentFormatted,
        slot.limitFormatted
    )
    val cardCd = buildString {
        append("Thẻ ngân sách đã kết thúc, ${slot.cycleLabel}. ")
        append("Hạn mức: ${slot.limitFormatted}, đã chi: ${slot.spentFormatted}. ")
        if (slot.isOverBudget) {
            append("Vượt ngân sách: ${slot.overFormatted}. ")
        } else {
            append("Còn lại: ${slot.remainingFormatted}. ")
        }
        append("Tiến độ: ${slot.realPercentText}. Chạm để xem chi tiết.")
    }

    val cardBrush = remember(slot.themeOrdinal) {
        CandyBudgetThemes.getGradientBrush(slot.themeOrdinal)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CreditCardMinHeight)
            .shadow(
                elevation = 6.dp,
                shape = CreditCardCornerShape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(CreditCardCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                role = Role.Button
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = slot.clampedProgress,
                    range = 0f..1f
                )
            },
        shape = CreditCardCornerShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBrush)
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                CreditCardTopBar(badge = { BudgetCycleBadge(role = BudgetCardRole.Previous) })

                Spacer(modifier = Modifier.height(CandySpace.xs))

                Text(
                    text = remainingLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = if (slot.isOverBudget) {
                        stringResource(R.string.home_budget_over_warning, slot.overFormatted.orEmpty())
                    } else {
                        slot.remainingFormatted
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (slot.isOverBudget) 24.sp else 32.sp,
                        lineHeight = 36.sp
                    ),
                    color = if (slot.isOverBudget) CandyBudgetThemes.coralWarning else Color.White
                )

                // Thanh tiến trình
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(ProgressTrackCornerShape)
                            .background(Color.White.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(slot.clampedProgress)
                                .clip(ProgressTrackCornerShape)
                                .background(
                                    if (slot.isOverBudget) {
                                        CandyBudgetThemes.coralWarning
                                    } else {
                                        Color.White
                                    }
                                )
                        )
                    }

                    Surface(
                        shape = PercentPillCornerShape,
                        color = Color.White.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = slot.realPercentText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = if (slot.isOverBudget) CandyBudgetThemes.coralWarning else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_01,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = spentSummaryText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_05,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = "${slot.cycleLabel} (${slot.dateRangeText})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingConfiguredCardContent(
    slot: BudgetCardSlotUi.UpcomingConfiguredCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardCd = "Thẻ ngân sách sắp tới, ${slot.cycleLabel}. Hạn mức dự kiến: ${slot.limitFormatted}. Chạm để xem hoặc chỉnh sửa."

    val cardBrush = remember(slot.themeOrdinal) {
        CandyBudgetThemes.getGradientBrush(slot.themeOrdinal)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CreditCardMinHeight)
            .shadow(
                elevation = 6.dp,
                shape = CreditCardCornerShape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(CreditCardCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                role = Role.Button
            },
        shape = CreditCardCornerShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = cardBrush)
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                CreditCardTopBar(badge = { BudgetCycleBadge(role = BudgetCardRole.NextConfigured) })

                Spacer(modifier = Modifier.height(CandySpace.sm))

                Column {
                    Text(
                        text = stringResource(R.string.home_budget_upcoming_limit_label),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = slot.limitFormatted,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            lineHeight = 38.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(CandySpace.md))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ITM_05,
                        contentDescription = null,
                        size = 16.dp
                    )
                    Text(
                        text = "${slot.cycleLabel} (${slot.dateRangeText})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
