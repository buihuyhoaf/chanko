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
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.BudgetStateKind
import com.hoabh.chanko.presentation.screens.home.BudgetUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.presentation.screens.home.domain.BudgetCalculator
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme
import com.hoabh.chanko.ui.theme.Nunito

private val SnapshotCardRadius = 32.dp
private val SnapshotBorderWidth = 2.dp
private val SpendingPanelRadius = 20.dp
private val ProgressBarHeight = 12.dp
private val DetailsIconSize = 14.dp
private val CalendarIconSize = 14.dp

/**
 * HOME-04 — Home Budget Snapshot (Candy Pop 3D):
 * 1. Chu kỳ ngân sách hiện tại (Header: "Ngân sách của bạn", icon calendar + ngày thực tế, nút "Chi tiết ↗").
 * 2. Số tiền còn lại (Main section: "Còn lại" / "Vượt hạn mức", số tiền lớn 28sp Nunito ExtraBold).
 * 3. Mascot Chanko 3D (Mascot Chanko 96–112dp, biểu cảm theo trạng thái ngân sách).
 * 4. Spending section (Panel trắng bo góc: "Đã chi", số tiền đã chi, badge phần trăm, thanh progress capsule 12dp).
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
                    Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.home_budget_details_cta),
                        onClick = onDetailsClick
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(SnapshotCardRadius),
        color = CandyColors.budgetSnapshotBg,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(SnapshotBorderWidth, CandyColors.budgetSnapshotBorder)
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
                canOpenDetails = canOpenDetails,
                onDetailsClick = onDetailsClick
            )
        }
    }
}

/**
 * Alias for [HomeBudgetSnapshot] to maintain backward compatibility with HomeScreen and existing callers.
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
    canOpenDetails: Boolean,
    onDetailsClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.4f

    val isOver = budget.isOverBudget
    val remainingColor = if (isOver) CandyColors.budgetOverBadgeInk else CandyColors.budgetSnapshotAmount
    val progressBarFillColor = if (isOver) CandyColors.overBudget else CandyColors.budgetSnapshotProgressFill

    val mascotRes = when (budget.stateKind) {
        BudgetStateKind.Normal -> R.drawable.mascot_sleepy
        BudgetStateKind.Warning,
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
        // 1. Header: "Ngân sách của bạn" + cycle date & "Chi tiết" pill
        SnapshotHeader(
            cycleDateText = cycleDateText,
            canOpenDetails = canOpenDetails,
            onDetailsClick = onDetailsClick
        )

        // 2. Main section: "Còn lại" / Amount (Left) + Mascot 3D (Right)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useCompactMascot = maxWidth < 330.dp || isLargeFont
            val mascotSize = if (useCompactMascot) 80.dp else 104.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Text(
                        text = if (isOver) {
                            stringResource(R.string.home_budget_over_label)
                        } else {
                            stringResource(R.string.home_budget_remaining_label)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CandyColors.budgetSnapshotSecondaryText
                    )
                    Text(
                        text = budget.remainingFormatted,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
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

        // 3. Spending section: White rounded panel
        SpendingPanel(
            budget = budget,
            progressBarFillColor = progressBarFillColor
        )
    }
}

@Composable
private fun SnapshotHeader(
    cycleDateText: String,
    canOpenDetails: Boolean,
    onDetailsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.home_budget_header_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.textPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ChankoIcon(
                    id = ChankoIconId.BUD_05,
                    contentDescription = null,
                    size = CalendarIconSize
                )
                Text(
                    text = cycleDateText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CandyColors.budgetSnapshotSecondaryText
                )
            }
        }

        if (canOpenDetails) {
            Spacer(modifier = Modifier.width(CandySpace.sm))
            val detailsLabel = stringResource(R.string.home_budget_details_cta)
            Surface(
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clip(CandyShape.chip)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = detailsLabel,
                        onClick = onDetailsClick
                    ),
                shape = CandyShape.chip,
                color = Color.White,
                shadowElevation = CandyElevation.low
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = CandySpace.md,
                        vertical = CandySpace.xs
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Text(
                        text = detailsLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.budgetSnapshotAmount
                    )
                    ChankoIcon(
                        id = ChankoIconId.ACT_14,
                        contentDescription = null,
                        size = DetailsIconSize
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingPanel(
    budget: BudgetUi,
    progressBarFillColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SpendingPanelRadius),
        color = CandyColors.budgetSnapshotSpendingPanel,
        shadowElevation = CandyElevation.low
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: "Đã chi" + Amount (Left) & Percentage badge (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Text(
                        text = stringResource(R.string.home_budget_spent_label),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CandyColors.budgetSnapshotSecondaryText
                    )
                    Text(
                        text = budget.spentFormatted,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.textPrimary
                    )
                }

                BudgetPercentageBadge(
                    text = budget.progressRightLabel,
                    stateKind = budget.stateKind
                )
            }

            // Row 2: Capsule progress bar (12dp height, no duplicate % label)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ProgressBarHeight)
                    .clip(CircleShape)
                    .background(CandyColors.budgetSnapshotProgressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = budget.visualProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(progressBarFillColor)
                )
            }
        }
    }
}

@Composable
private fun BudgetPercentageBadge(
    text: String,
    stateKind: BudgetStateKind
) {
    val (badgeBg, badgeText) = when (stateKind) {
        BudgetStateKind.OverBudget -> CandyColors.budgetOverBadgeBg to CandyColors.budgetOverBadgeInk
        BudgetStateKind.Warning,
        BudgetStateKind.Reached -> CandyColors.budgetSnapshotBadgeBg to CandyColors.budgetSnapshotBadgeText
        BudgetStateKind.Normal -> CandyColors.budgetSnapshotBadgeBg to CandyColors.budgetSnapshotBadgeText
        BudgetStateKind.Zero,
        BudgetStateKind.Unknown -> CandyColors.primarySoft to CandyColors.primaryInk
    }

    Surface(
        shape = CandyShape.chip,
        color = badgeBg
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = badgeText,
            modifier = Modifier.padding(
                horizontal = CandySpace.sm,
                vertical = 4.dp
            )
        )
    }
}

private fun cleanCycleDateRange(rawCycleLabel: String): String {
    return rawCycleLabel
        .removePrefix("Ngân sách tháng ")
        .removePrefix("Ngân sách ")
        .trim()
        .ifBlank { "15/10 – 14/11/2026" }
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
                    modifier = Modifier.padding(horizontal = CandySpace.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = retryText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.budgetSnapshotAmount
                    )
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Home Budget Snapshot - Normal (< 80%)", showBackground = true, widthDp = 393)
@Composable
private fun HomeBudgetSnapshotPreviewNormal() {
    ChankoTheme {
        HomeBudgetSnapshot(
            sectionState = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = "15/10 – 14/11/2026",
                    spentMinor = 1_500_000L,
                    limitMinor = 3_000_000L
                )
            ),
            onDetailsClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Budget Snapshot - Warning (80%)", showBackground = true, widthDp = 393)
@Composable
private fun HomeBudgetSnapshotPreviewWarning() {
    ChankoTheme {
        HomeBudgetSnapshot(
            sectionState = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = "15/10 – 14/11/2026",
                    spentMinor = 2_400_000L,
                    limitMinor = 3_000_000L
                )
            ),
            onDetailsClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Budget Snapshot - Reached (100%)", showBackground = true, widthDp = 393)
@Composable
private fun HomeBudgetSnapshotPreviewReached() {
    ChankoTheme {
        HomeBudgetSnapshot(
            sectionState = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = "15/10 – 14/11/2026",
                    spentMinor = 3_000_000L,
                    limitMinor = 3_000_000L
                )
            ),
            onDetailsClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Budget Snapshot - Over Budget (120%)", showBackground = true, widthDp = 393)
@Composable
private fun HomeBudgetSnapshotPreviewOverBudget() {
    ChankoTheme {
        HomeBudgetSnapshot(
            sectionState = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = "15/10 – 14/11/2026",
                    spentMinor = 3_600_000L,
                    limitMinor = 3_000_000L
                )
            ),
            onDetailsClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Budget Snapshot - Large Font (2.0x)", showBackground = true, widthDp = 393, fontScale = 2.0f)
@Composable
private fun HomeBudgetSnapshotPreviewLargeFont() {
    ChankoTheme {
        HomeBudgetSnapshot(
            sectionState = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = "15/10 – 14/11/2026",
                    spentMinor = 2_400_000L,
                    limitMinor = 3_000_000L
                )
            ),
            onDetailsClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Budget Snapshot - Narrow Screen (320dp)", showBackground = true, widthDp = 320)
@Composable
private fun HomeBudgetSnapshotPreviewNarrowScreen() {
    ChankoTheme {
        HomeBudgetSnapshot(
            sectionState = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = "15/10 – 14/11/2026",
                    spentMinor = 2_400_000L,
                    limitMinor = 3_000_000L
                )
            ),
            onDetailsClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}
