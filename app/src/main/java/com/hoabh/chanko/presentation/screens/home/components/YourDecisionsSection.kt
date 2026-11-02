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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.DecisionStatus
import com.hoabh.chanko.presentation.screens.home.DecisionUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

private val OuterSectionCardRadius = 24.dp
private val AttentionTileRadius = 18.dp
private val ThumbnailSize = 56.dp
private val ThumbnailRadius = 14.dp
private val ChevronIconSize = 18.dp
private val StatusIconSize = 14.dp

@Immutable
data class AttentionItemStatusStyle(
    val pillBackground: Color,
    val pillText: Color,
    val icon: ChankoIconId
)

fun resolveAttentionItemStyle(
    status: DecisionStatus,
    isTimerExpiredLocally: Boolean = false
): AttentionItemStatusStyle = when {
    isTimerExpiredLocally -> AttentionItemStatusStyle(
        pillBackground = CandyColors.decisionWaitingPillBg,
        pillText = CandyColors.decisionWaitingPillInk,
        icon = ChankoIconId.ITM_04
    )
    status == DecisionStatus.Ready -> AttentionItemStatusStyle(
        pillBackground = CandyColors.backgroundPink,
        pillText = CandyColors.globalSheetSaveIconInk,
        icon = ChankoIconId.ITM_05
    )
    status == DecisionStatus.Waiting -> AttentionItemStatusStyle(
        pillBackground = CandyColors.decisionWaitingPillBg,
        pillText = CandyColors.decisionWaitingPillInk,
        icon = ChankoIconId.ITM_04
    )
    status == DecisionStatus.Considering -> AttentionItemStatusStyle(
        pillBackground = CandyColors.decisionWaitingPillBg,
        pillText = CandyColors.decisionWaitingPillInk,
        icon = ChankoIconId.ITM_12
    )
    status == DecisionStatus.Saved -> AttentionItemStatusStyle(
        pillBackground = CandyColors.decisionConsideringPillBg,
        pillText = CandyColors.decisionConsideringPillInk,
        icon = ChankoIconId.ITM_13
    )
    else -> AttentionItemStatusStyle(
        pillBackground = CandyColors.decisionReadyPillBg,
        pillText = CandyColors.decisionReadyPillInk,
        icon = ChankoIconId.ITM_06
    )
}

/**
 * Khu vực 3 — Những món cần chú ý (Candy Pop 3D v1.0 — AC-07..AC-10, HOME-STATE-01, HOME-EDGE-04):
 * - Khung card sáng bo góc lớn (`24dp`) bao quanh cả header và các hàng món đồ theo đúng mockup được duyệt.
 * - Header: Icon ngôi sao lấp lánh + "Những món cần chú ý" + nút "Tất cả ›" (>= 48dp).
 * - Tối đa 3 món, ưu tiên món cần hành động / đến hạn xem lại.
 * - Giá thiếu hiển thị đúng "Chưa có giá" (không coi là 0đ).
 * - На màn hình hẹp (< 360dp) hoặc font lớn (>= 1.35f), status pill tự động chuyển xuống dưới nội dung để không cắt chữ.
 * - Giữ empty state nhỏ gọn trong card khi có 0 món (HOME-STATE-01).
 */
@Composable
fun HomeAttentionItemsSection(
    sectionState: SectionState<List<DecisionUi>>,
    totalCount: Int,
    onDecisionClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenViewAll: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(OuterSectionCardRadius),
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
            // 1. Header inside enclosing card
            AttentionItemsHeader(
                canOpenViewAll = canOpenViewAll,
                onViewAllClick = onViewAllClick
            )

            // 2. Content
            when (sectionState) {
                SectionState.Loading -> AttentionItemsLoadingState()
                is SectionState.Empty -> AttentionItemsEmptyState(message = sectionState.message)
                is SectionState.Error -> AttentionItemsErrorState(
                    message = sectionState.message,
                    canRetry = sectionState.canRetry,
                    onRetryClick = onRetryClick
                )
                is SectionState.Content -> {
                    if (sectionState.data.isEmpty()) {
                        AttentionItemsEmptyState()
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                        ) {
                            sectionState.data.take(3).forEach { decision ->
                                key(decision.id) {
                                    HomeAttentionItemTile(
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

/**
 * Alias tương thích với HomeScreen.
 */
@Composable
fun YourDecisionsSection(
    sectionState: SectionState<List<DecisionUi>>,
    totalCount: Int,
    onDecisionClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenViewAll: Boolean = true
) {
    HomeAttentionItemsSection(
        sectionState = sectionState,
        totalCount = totalCount,
        onDecisionClick = onDecisionClick,
        onViewAllClick = onViewAllClick,
        onRetryClick = onRetryClick,
        modifier = modifier,
        canOpenViewAll = canOpenViewAll
    )
}

@Composable
private fun AttentionItemsHeader(
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
            ChankoIcon(
                id = ChankoIconId.ITM_01,
                contentDescription = null,
                size = 24.dp
            )

            Text(
                text = stringResource(R.string.home_decisions_title),
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (canOpenViewAll) {
            val viewAllText = stringResource(R.string.home_decisions_view_all_compact)
            Row(
                modifier = Modifier
                    .heightIn(min = CandySize.touchTarget)
                    .clip(CandyShape.chip)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.home_decisions_view_all),
                        onClick = onViewAllClick
                    )
                    .padding(horizontal = CandySpace.sm, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = viewAllText,
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.brandPurple
                )
                ChankoIcon(
                    id = ChankoIconId.ACT_18,
                    contentDescription = null,
                    size = 14.dp
                )
            }
        }
    }
}

/**
 * Một hàng món đồ trong "Những món cần chú ý" (AC-10, AC-20, HOME-EDGE-04):
 * - Toàn bộ tile là 1 action semantic mở ItemDetailScreen(itemId); chevron chỉ là affordance thị giác.
 * - Khi cửa sổ rộng (>= 340dp) và font bình thường: [Thumbnail] [Tên + Giá] [Status Pill] [Chevron].
 * - Khi cửa sổ hẹp (< 340dp) hoặc font lớn (>= 1.35f): Status Pill tự động chuyển xuống dưới Giá để không cắt chữ.
 */
@Composable
fun HomeAttentionItemTile(
    decision: DecisionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.35f
    val style = resolveAttentionItemStyle(
        status = decision.status,
        isTimerExpiredLocally = decision.isTimerExpiredLocally
    )

    val pillLabel = when {
        decision.isTimerExpiredLocally -> stringResource(R.string.home_decision_status_expired)
        decision.status == DecisionStatus.Ready -> stringResource(R.string.home_decision_status_ready)
        else -> decision.statusLabel
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CandySize.touchTarget)
            .clip(RoundedCornerShape(AttentionTileRadius))
            .semantics(mergeDescendants = true) {}
            .clickable(
                role = Role.Button,
                onClickLabel = decision.title,
                onClick = onClick
            ),
        shape = RoundedCornerShape(AttentionTileRadius),
        color = Color.White,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder),
        shadowElevation = 1.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md)
        ) {
            val stackStatusBelow = maxWidth < 310.dp || isLargeFont

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail: 56x56dp, radius 14dp, stable placeholder when missing
                Box(
                    modifier = Modifier
                        .size(ThumbnailSize)
                        .clip(RoundedCornerShape(ThumbnailRadius))
                        .background(CandyColors.primarySoft.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (decision.thumbnailResId != null) {
                        Image(
                            painter = painterResource(id = decision.thumbnailResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ChankoIcon(
                            id = ChankoIconId.ITM_03,
                            contentDescription = null,
                            size = 28.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(CandySpace.md))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = decision.title,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = decision.priceFormatted,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (decision.priceMinor == null) {
                            CandyColors.textSecondary
                        } else {
                            CandyColors.textPrimary
                        }
                    )

                    if (stackStatusBelow) {
                        StatusPillBadge(
                            label = pillLabel,
                            style = style
                        )
                    }
                }

                if (!stackStatusBelow) {
                    Spacer(modifier = Modifier.width(CandySpace.sm))
                    StatusPillBadge(
                        label = pillLabel,
                        style = style
                    )
                }

                Spacer(modifier = Modifier.width(CandySpace.sm))

                ChankoIcon(
                    id = ChankoIconId.ACT_18,
                    contentDescription = null,
                    size = ChevronIconSize
                )
            }
        }
    }
}

@Composable
private fun StatusPillBadge(
    label: String,
    style: AttentionItemStatusStyle,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CandyShape.chip,
        color = style.pillBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ChankoIcon(
                id = style.icon,
                contentDescription = null,
                size = StatusIconSize
            )
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = style.pillText
            )
        }
    }
}

/**
 * Empty state nhỏ gọn bên trong section Những món cần chú ý (HOME-STATE-01, AC-09).
 */
@Composable
private fun AttentionItemsEmptyState(
    message: String = ""
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AttentionTileRadius),
        color = CandyColors.primarySoft.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message.ifBlank { stringResource(R.string.home_decisions_empty_title) },
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.textPrimary
            )
            Text(
                text = stringResource(R.string.home_decisions_empty_subtitle),
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = CandyColors.textSecondary
            )
        }
    }
}

@Composable
private fun AttentionItemsLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        repeat(2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(AttentionTileRadius),
                color = CandyColors.primarySoft.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, CandyColors.primarySoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(CandySpace.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(ThumbnailSize)
                            .clip(RoundedCornerShape(ThumbnailRadius))
                            .background(Color.White.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(CandySpace.md))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.6f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttentionItemsErrorState(
    message: String,
    canRetry: Boolean,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AttentionTileRadius),
        color = CandyColors.primarySoft.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message.ifBlank { stringResource(R.string.home_section_error_decisions) },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = CandyColors.textSecondary,
                modifier = Modifier.weight(1f)
            )
            if (canRetry) {
                Spacer(modifier = Modifier.width(CandySpace.sm))
                val retryLabel = stringResource(R.string.home_section_retry)
                Surface(
                    modifier = Modifier
                        .heightIn(min = CandySize.touchTarget)
                        .clip(CandyShape.chip)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = retryLabel,
                            onClick = onRetryClick
                        ),
                    shape = CandyShape.chip,
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = retryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.brandPurple
                        )
                    }
                }
            }
        }
    }
}
