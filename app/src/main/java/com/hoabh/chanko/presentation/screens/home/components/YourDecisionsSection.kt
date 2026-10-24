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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.DecisionStatus
import com.hoabh.chanko.presentation.screens.home.DecisionUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val SectionIconBoxSize = 28.dp
private val SectionIconSize = 16.dp
private val DecisionThumbnailSize = 48.dp
private val DecisionThumbnailShape = RoundedCornerShape(14.dp)
private val DecisionRowShape = RoundedCornerShape(18.dp)
private val StatusPillIconSize = 13.dp
private val ChevronIconSize = 18.dp

/**
 * HOME-05 — Your Decisions Section (`"Những món cần chú ý"`, v2 Image-Aligned):
 * - Header: Icon checklist tím + `"Những món cần chú ý"` + count badge tròn (`3`) + `"Xem tất cả >"`.
 * - Danh sách hiển thị tối đa 3 `DecisionRow` đã được sắp xếp ổn định `Ready -> Waiting -> Considering -> Saved`.
 * - Hỗ trợ thumbnail mẫu (`S21`) và fallback icon khi `thumbnailResId == null` (`S15`).
 * - Hỗ trợ trạng thái `Loading`, `Empty` (`S05` kèm CTA lưu món đồ) và `Error` (`S13`).
 */
@Composable
fun YourDecisionsSection(
    sectionState: SectionState<List<DecisionUi>>,
    totalCount: Int,
    onDecisionClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onSaveFirstItemClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenViewAll: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CandyShape.card,
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            // Section Header
            DecisionsHeaderRow(
                totalCount = totalCount,
                canOpenViewAll = canOpenViewAll,
                onViewAllClick = onViewAllClick
            )

            when (sectionState) {
                SectionState.Loading -> DecisionsLoadingState()
                is SectionState.Empty -> DecisionsEmptyState(
                    onSaveFirstItemClick = onSaveFirstItemClick
                )
                is SectionState.Error -> DecisionsErrorState(
                    message = sectionState.message,
                    canRetry = sectionState.canRetry,
                    onRetryClick = onRetryClick
                )
                is SectionState.Content -> {
                    if (sectionState.data.isEmpty()) {
                        DecisionsEmptyState(onSaveFirstItemClick = onSaveFirstItemClick)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                        ) {
                            sectionState.data.take(3).forEach { decision ->
                                key(decision.id) {
                                    DecisionRow(
                                        decision = decision,
                                        onClick = { onDecisionClick(decision.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecisionsHeaderRow(
    totalCount: Int,
    canOpenViewAll: Boolean,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(SectionIconBoxSize)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CandyColors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.FactCheck,
                    contentDescription = null,
                    tint = CandyColors.brandPurple,
                    modifier = Modifier.size(SectionIconSize)
                )
            }

            Text(
                text = stringResource(R.string.home_decisions_title),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (totalCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = CandyColors.primarySoft
                ) {
                    Text(
                        text = totalCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CandyColors.primaryInk,
                        modifier = Modifier.padding(
                            horizontal = CandySpace.sm,
                            vertical = 2.dp
                        )
                    )
                }
            }
        }

        if (canOpenViewAll) {
            val viewAllLabel = stringResource(R.string.home_decisions_view_all)
            Row(
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .clip(CandyShape.chip)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = viewAllLabel,
                        onClick = onViewAllClick
                    )
                    .padding(horizontal = CandySpace.xs, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewAllLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.primaryInk
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CandyColors.primaryInk,
                    modifier = Modifier.size(ChevronIconSize)
                )
            }
        }
    }
}

@Composable
fun DecisionRow(
    decision: DecisionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.4f

    val rowBg = when (decision.status) {
        DecisionStatus.Ready -> CandyColors.decisionReadyRowBg
        DecisionStatus.Waiting -> CandyColors.decisionWaitingRowBg
        DecisionStatus.Considering -> CandyColors.decisionConsideringRowBg
        DecisionStatus.Saved -> CandyColors.decisionSavedRowBg
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(DecisionRowShape)
            .clickable(
                role = Role.Button,
                onClickLabel = decision.title,
                onClick = onClick
            ),
        shape = DecisionRowShape,
        color = rowBg
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md)
        ) {
            val stackStatusBelow = maxWidth < 310.dp || isLargeFont

            if (stackStatusBelow) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DecisionThumbnail(thumbnailResId = decision.thumbnailResId)
                        Spacer(modifier = Modifier.width(CandySpace.md))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = decision.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = CandyColors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Never clip money with ellipsis (S15)
                            Text(
                                text = decision.priceFormatted,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CandyColors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = CandyColors.textSecondary,
                            modifier = Modifier.size(ChevronIconSize)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DecisionStatusBadge(status = decision.status, label = decision.statusLabel)
                        if (!decision.secondaryText.isNullOrBlank()) {
                            Text(
                                text = decision.secondaryText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = CandyColors.textSecondary
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DecisionThumbnail(thumbnailResId = decision.thumbnailResId)

                    Spacer(modifier = Modifier.width(CandySpace.md))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = decision.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = CandyColors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Never clip money with ellipsis (S15)
                        Text(
                            text = decision.priceFormatted,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(CandySpace.sm))

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        DecisionStatusBadge(status = decision.status, label = decision.statusLabel)
                        if (!decision.secondaryText.isNullOrBlank()) {
                            Text(
                                text = decision.secondaryText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = CandyColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(CandySpace.xs))

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = CandyColors.textSecondary,
                        modifier = Modifier.size(ChevronIconSize)
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionThumbnail(
    thumbnailResId: Int?
) {
    Box(
        modifier = Modifier
            .size(DecisionThumbnailSize)
            .clip(DecisionThumbnailShape)
            .background(CandyColors.primarySoft),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailResId != null) {
            Image(
                painter = painterResource(id = thumbnailResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.ShoppingBag,
                contentDescription = null,
                tint = CandyColors.brandPurple,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DecisionStatusBadge(
    status: DecisionStatus,
    label: String
) {
    val (pillBg, pillInk, icon) = when (status) {
        DecisionStatus.Ready -> Triple(
            CandyColors.decisionReadyPillBg,
            CandyColors.decisionReadyPillInk,
            Icons.Rounded.CheckCircle
        )
        DecisionStatus.Waiting -> Triple(
            CandyColors.decisionWaitingPillBg,
            CandyColors.decisionWaitingPillInk,
            Icons.Rounded.Schedule
        )
        DecisionStatus.Considering -> Triple(
            CandyColors.decisionConsideringPillBg,
            CandyColors.decisionConsideringPillInk,
            Icons.Rounded.Visibility
        )
        DecisionStatus.Saved -> Triple(
            CandyColors.decisionSavedPillBg,
            CandyColors.decisionSavedPillInk,
            Icons.Rounded.BookmarkBorder
        )
    }

    Surface(
        shape = CandyShape.chip,
        color = pillBg
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = CandySpace.sm,
                vertical = CandySpace.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = pillInk,
                modifier = Modifier.size(StatusPillIconSize)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = pillInk
            )
        }
    }
}

@Composable
private fun DecisionsEmptyState(
    onSaveFirstItemClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CandySpace.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        Text(
            text = stringResource(R.string.home_decisions_empty_title),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = CandyColors.textPrimary
        )
        Text(
            text = stringResource(R.string.home_decisions_empty_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = CandyColors.textSecondary
        )
        val ctaLabel = stringResource(R.string.home_decisions_empty_cta)
        Surface(
            modifier = Modifier
                .heightIn(min = CandySize.touchTarget)
                .clip(CandyShape.chip)
                .clickable(
                    role = Role.Button,
                    onClickLabel = ctaLabel,
                    onClick = onSaveFirstItemClick
                ),
            shape = CandyShape.chip,
            color = CandyColors.primarySoft
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = CandySpace.lg,
                    vertical = CandySpace.sm
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ctaLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.primaryInk
                )
            }
        }
    }
}

@Composable
private fun DecisionsLoadingState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CandySpace.sm),
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
private fun DecisionsErrorState(
    message: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
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
