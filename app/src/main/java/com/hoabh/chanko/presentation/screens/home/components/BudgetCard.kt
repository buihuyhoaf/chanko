package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.BudgetStateKind
import com.hoabh.chanko.presentation.screens.home.BudgetUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

private val SnapshotCardRadius = 24.dp
private val SnapshotBorderWidth = 1.dp
private val ProgressBarHeight = 14.dp
private val DetailsIconSize = 14.dp
private val WalletIconSize = 36.dp

/**
 * Khu vực 4 — Budget Snapshot ("Ngân sách mua sắm" — Candy Pop 3D v1.0, AC-11, AC-12, AC-13, HOME-BUD-01):
 * 1. Đặt sau "Những món cần chú ý", trước "Góc nhìn từ Chanko".
 * 2. Header: Icon ví (`BUD-01`), tiêu đề "Ngân sách mua sắm", khoảng chu kỳ, pill affordance "Chi tiết ›".
 * 3. Main section: "Còn lại" / "Vượt hạn mức", số tiền lớn `32sp ExtraBold`, mascot mèo trắng phụ bên phải.
 * 4. Thanh tiến trình capsule + nhãn "Đã chi XX%" bên phải.
 * 5. Toàn bộ card mở `BudgetScreen` với 1 action semantic duy nhất (không có nested clickable gây đọc lặp TalkBack — AC-12, AC-20).
 */
@Composable
fun HomeBudgetSnapshot(
    sectionState: SectionState<BudgetUi>,
    onDetailsClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenDetails: Boolean = true
) {
    val isCardClickable = canOpenDetails && sectionState is SectionState.Content

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SnapshotCardRadius))
            .then(
                if (isCardClickable) {
                    Modifier
                        .semantics(mergeDescendants = true) {}
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.home_budget_details_cta),
                            onClick = onDetailsClick
                        )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(SnapshotCardRadius),
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(SnapshotBorderWidth, CandyColors.homeCardBorder)
    ) {
        when (sectionState) {
            SectionState.Loading -> BudgetLoadingState()
            is SectionState.Empty -> BudgetErrorOrEmptyState(
                message = sectionState.message.ifBlank { stringResource(R.string.home_budget_unknown_value) },
                canRetry = false,
                onRetryClick = onRetryClick
            )
            is SectionState.Error -> BudgetErrorOrEmptyState(
                message = sectionState.message,
                canRetry = sectionState.canRetry,
                onRetryClick = onRetryClick
            )
            is SectionState.Content -> BudgetSnapshotContent(
                budget = sectionState.data,
                canOpenDetails = canOpenDetails
            )
        }
    }
}

/**
 * Alias tương thích với HomeScreen.
 */
@Composable
fun BudgetCard(
    sectionState: SectionState<BudgetUi>,
    onDetailsClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenDetails: Boolean = true
) {
    HomeBudgetSnapshot(
        sectionState = sectionState,
        onDetailsClick = onDetailsClick,
        onRetryClick = onRetryClick,
        modifier = modifier,
        canOpenDetails = canOpenDetails
    )
}

@Composable
private fun BudgetSnapshotContent(
    budget: BudgetUi,
    canOpenDetails: Boolean
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.4f

    val isOver = budget.isOverBudget
    val remainingColor = if (isOver) CandyColors.budgetOverBadgeInk else CandyColors.budgetSnapshotAmount
    val progressBarFillColor = if (isOver) CandyColors.overBudget else CandyColors.brandPurple

    val mascotRes = when (budget.stateKind) {
        BudgetStateKind.Normal,
        BudgetStateKind.Warning -> R.drawable.mascot_sleepy
        BudgetStateKind.Reached,
        BudgetStateKind.OverBudget -> R.drawable.mascot_worried
        BudgetStateKind.Zero,
        BudgetStateKind.Unknown -> R.drawable.mascot_sleepy
    }

    val cycleDateText = cleanCycleDateRange(budget.cycleLabel)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CandySpace.lg),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // 1. Header: Icon ví + "Ngân sách mua sắm" + chu kỳ & "Chi tiết ›" affordance pill
        SnapshotHeader(
            cycleDateText = cycleDateText,
            canOpenDetails = canOpenDetails
        )

        // 2. Main section: "Còn lại" / "Vượt hạn mức" + Số tiền 32sp ExtraBold + Mascot phụ
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useCompactMascot = maxWidth < 330.dp || isLargeFont
            val mascotSize = if (useCompactMascot) 72.dp else 92.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isOver) {
                            stringResource(R.string.home_budget_over_label)
                        } else {
                            stringResource(R.string.home_budget_remaining_label)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CandyColors.textSecondary
                    )
                    Text(
                        text = budget.remainingFormatted,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 32.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = remainingColor
                    )
                }

                Spacer(modifier = Modifier.width(CandySpace.sm))

                Image(
                    painter = painterResource(mascotRes),
                    contentDescription = null,
                    modifier = Modifier.size(mascotSize),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // 3. Progress bar capsule + "Đã chi XX%" bên phải theo đúng mockup Candy Pop 3D v1.0
        SpendingProgressRow(
            budget = budget,
            progressBarFillColor = progressBarFillColor,
            isLargeFont = isLargeFont
        )
    }
}

@Composable
private fun SnapshotHeader(
    cycleDateText: String,
    canOpenDetails: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            ChankoIcon(
                id = ChankoIconId.BUD_01,
                contentDescription = null,
                size = WalletIconSize
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_budget_header_title),
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary
                )
                Text(
                    text = cycleDateText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CandyColors.textSecondary
                )
            }
        }

        // "Chi tiết ›" là affordance thị giác nằm trong vùng chạm của toàn bộ BudgetCard (tránh nested clickable - AC-12)
        if (canOpenDetails) {
            Spacer(modifier = Modifier.width(CandySpace.sm))
            Surface(
                shape = CandyShape.chip,
                color = Color.White,
                border = BorderStroke(1.dp, CandyColors.homeCardBorder),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = CandySpace.md,
                        vertical = 6.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_budget_details_cta),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.brandPurple
                    )
                    ChankoIcon(
                        id = ChankoIconId.ACT_18,
                        contentDescription = null,
                        size = DetailsIconSize
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingProgressRow(
    budget: BudgetUi,
    progressBarFillColor: Color,
    isLargeFont: Boolean
) {
    if (isLargeFont) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProgressBarCapsule(
                progress = budget.visualProgress,
                fillColor = progressBarFillColor,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = budget.progressRightLabel,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.textPrimary
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            ProgressBarCapsule(
                progress = budget.visualProgress,
                fillColor = progressBarFillColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = budget.progressRightLabel,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.textPrimary
            )
        }
    }
}

@Composable
private fun ProgressBarCapsule(
    progress: Float,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(ProgressBarHeight)
            .clip(CircleShape)
            .background(CandyColors.budgetSnapshotProgressTrack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(fillColor)
        )
    }
}

private fun cleanCycleDateRange(rawCycleLabel: String): String {
    return rawCycleLabel
        .removePrefix("Ngân sách tháng ")
        .removePrefix("Ngân sách ")
        .trim()
        .ifBlank { "01/10 – 31/10 (còn 12 ngày)" }
}

@Composable
private fun BudgetLoadingState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CandySpace.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = CandyColors.budgetSnapshotAmount,
            strokeWidth = 2.dp
        )
        Text(
            text = stringResource(R.string.home_section_loading),
            style = MaterialTheme.typography.bodySmall,
            color = CandyColors.budgetSnapshotSecondaryText
        )
    }
}

@Composable
private fun BudgetErrorOrEmptyState(
    message: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CandySpace.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = CandyColors.budgetSnapshotSecondaryText,
            modifier = Modifier.weight(1f)
        )
        if (canRetry) {
            Spacer(modifier = Modifier.width(CandySpace.sm))
            val retryText = stringResource(R.string.home_section_retry)
            Surface(
                modifier = Modifier
                    .heightIn(min = CandySize.touchTarget)
                    .clip(CandyShape.chip)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = retryText,
                        onClick = onRetryClick
                    ),
                shape = CandyShape.chip,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier.padding(
                        horizontal = CandySpace.md,
                        vertical = CandySpace.xs
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = retryText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CandyColors.budgetSnapshotAmount
                    )
                }
            }
        }
    }
}
