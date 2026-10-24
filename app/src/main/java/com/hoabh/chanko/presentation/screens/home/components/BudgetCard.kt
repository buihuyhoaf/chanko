package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.BudgetStateKind
import com.hoabh.chanko.presentation.screens.home.BudgetUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val ProgressTrackHeight = 10.dp
private val VerticalDividerHeight = 36.dp
private val DetailsChevronSize = 16.dp

/**
 * HOME-04 — Budget Snapshot Card (v2 Image-Aligned):
 * - Header: Tiêu đề chu kỳ (`"Ngân sách tháng 10 – 11/2026"`) + pill `"Chi tiết >"`.
 * - Cột số tiền: `"Đã chi"` (`2.400.000 ₫ / 3.000.000 ₫`), `"Còn lại"` (`600.000 ₫`) và badge `"Đã tiêu 80%"`.
 * - Thanh tiến độ: Thanh tím bo tròn + nhãn phần trăm bên phải (`"80%"`).
 * - Tuyệt đối không dùng dấu ba chấm cắt ngắn số tiền (S15); tự động xếp dọc trên màn hẹp hoặc font lớn.
 * - Hỗ trợ đầy đủ Loading, Error (có nút Thử lại — S13), Zero (S07), Unknown (S08) và OverBudget (S06).
 */
@Composable
fun BudgetCard(
    sectionState: SectionState<BudgetUi>,
    onDetailsClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenDetails: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CandyShape.card,
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
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
            is SectionState.Content -> BudgetContent(
                budget = sectionState.data,
                canOpenDetails = canOpenDetails,
                onDetailsClick = onDetailsClick
            )
        }
    }
}

@Composable
private fun BudgetContent(
    budget: BudgetUi,
    canOpenDetails: Boolean,
    onDetailsClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.4f

    val (badgeBg, badgeInk) = when (budget.stateKind) {
        BudgetStateKind.Normal -> CandyColors.budgetNormalBadgeBg to CandyColors.budgetNormalBadgeInk
        BudgetStateKind.Warning -> CandyColors.budgetWarningBadgeBg to CandyColors.budgetWarningBadgeInk
        BudgetStateKind.Reached -> CandyColors.budgetReachedBadgeBg to CandyColors.budgetReachedBadgeInk
        BudgetStateKind.OverBudget -> CandyColors.budgetOverBadgeBg to CandyColors.budgetOverBadgeInk
        BudgetStateKind.Zero,
        BudgetStateKind.Unknown -> CandyColors.primarySoft to CandyColors.primaryInk
    }

    val remainingColor = if (budget.isOverBudget) {
        CandyColors.budgetOverBadgeInk
    } else {
        CandyColors.brandPurple
    }

    val progressBarFillColor = if (budget.isOverBudget) {
        CandyColors.overBudget
    } else {
        CandyColors.brandPurple
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CandySpace.lg),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Row 1: Cycle Title + "Chi tiết >" Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = budget.cycleLabel,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            if (canOpenDetails) {
                Spacer(modifier = Modifier.width(CandySpace.sm))
                val detailsLabel = stringResource(R.string.home_budget_details_cta)
                Surface(
                    modifier = Modifier
                        .heightIn(min = 32.dp)
                        .clip(CandyShape.chip)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = detailsLabel,
                            onClick = onDetailsClick
                        ),
                    shape = CandyShape.chip,
                    color = CandyColors.primarySoft
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = CandySpace.md,
                            vertical = CandySpace.xs
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = detailsLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.primaryInk
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = CandyColors.primaryInk,
                            modifier = Modifier.size(DetailsChevronSize)
                        )
                    }
                }
            }
        }

        // Row 2: Spent / Remaining Columns + Status Badge (Adaptive for narrow 320dp & fontScale 2.0)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useStackedAmounts = maxWidth < 330.dp || isLargeFont

            if (useStackedAmounts) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                ) {
                    SpentMetricColumn(budget = budget)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RemainingMetricColumn(
                            budget = budget,
                            remainingColor = remainingColor
                        )
                        BudgetStatusPill(
                            text = budget.badgeText,
                            backgroundColor = badgeBg,
                            contentColor = badgeInk
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpentMetricColumn(
                        budget = budget,
                        modifier = Modifier.weight(1.25f)
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = CandySpace.sm)
                            .width(1.dp)
                            .height(VerticalDividerHeight)
                            .background(CandyColors.divider)
                    )

                    RemainingMetricColumn(
                        budget = budget,
                        remainingColor = remainingColor,
                        modifier = Modifier.weight(0.85f)
                    )

                    Spacer(modifier = Modifier.width(CandySpace.xs))

                    BudgetStatusPill(
                        text = budget.badgeText,
                        backgroundColor = badgeBg,
                        contentColor = badgeInk
                    )
                }
            }
        }

        // Row 3: Rounded Progress Bar + Percentage Label on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(ProgressTrackHeight)
                    .clip(CircleShape)
                    .background(CandyColors.progressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = budget.visualProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(progressBarFillColor)
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            Text(
                text = budget.progressRightLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.textSecondary
            )
        }
    }
}

@Composable
private fun SpentMetricColumn(
    budget: BudgetUi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.home_budget_spent_label),
            style = MaterialTheme.typography.labelSmall,
            color = CandyColors.textSecondary
        )
        // Never use ellipsis for money amounts (S15)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
        ) {
            Text(
                text = budget.spentFormatted,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.textPrimary
            )
            if (budget.limitMinor != null && budget.limitMinor > 0L) {
                Text(
                    text = "/ ${budget.limitFormatted}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CandyColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun RemainingMetricColumn(
    budget: BudgetUi,
    remainingColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.home_budget_remaining_label),
            style = MaterialTheme.typography.labelSmall,
            color = CandyColors.textSecondary
        )
        Text(
            text = budget.remainingFormatted,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = remainingColor
        )
    }
}

@Composable
private fun BudgetStatusPill(
    text: String,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = CandyShape.chip,
        color = backgroundColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = contentColor,
            modifier = Modifier.padding(
                horizontal = CandySpace.sm,
                vertical = CandySpace.xs
            )
        )
    }
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
            modifier = Modifier.size(20.dp),
            color = CandyColors.brandPurple,
            strokeWidth = 2.dp
        )
        Text(
            text = stringResource(R.string.home_section_loading),
            style = MaterialTheme.typography.bodySmall,
            color = CandyColors.textSecondary
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
            color = CandyColors.textSecondary,
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
                color = CandyColors.primarySoft
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
                        color = CandyColors.primaryInk
                    )
                }
            }
        }
    }
}
