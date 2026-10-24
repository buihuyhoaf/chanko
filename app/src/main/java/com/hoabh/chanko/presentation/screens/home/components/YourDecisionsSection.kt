package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.DecisionStatus
import com.hoabh.chanko.presentation.screens.home.DecisionUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme
import com.hoabh.chanko.ui.theme.Nunito

private val AttentionTileRadius = 24.dp
private val AttentionTileBorderWidth = 2.dp
private val ThumbnailSize = 72.dp
private val ThumbnailRadius = 16.dp
private val ChevronIconSize = 20.dp
private val StatusIconSize = 14.dp

@Immutable
data class AttentionItemStatusStyle(
    val background: Color,
    val border: Color,
    val accent: Color,
    val icon: ImageVector
)

fun resolveAttentionItemStyle(status: DecisionStatus): AttentionItemStatusStyle = when (status) {
    DecisionStatus.Ready -> AttentionItemStatusStyle(
        background = CandyColors.attentionTileReadyBg,
        border = CandyColors.attentionTileReadyBorder,
        accent = CandyColors.attentionTileReadyAccent,
        icon = Icons.Rounded.CheckCircle
    )
    DecisionStatus.Waiting -> AttentionItemStatusStyle(
        background = CandyColors.attentionTileWaitingBg,
        border = CandyColors.attentionTileWaitingBorder,
        accent = CandyColors.attentionTileWaitingAccent,
        icon = Icons.Rounded.Schedule
    )
    DecisionStatus.Considering -> AttentionItemStatusStyle(
        background = CandyColors.attentionTileConsideringBg,
        border = CandyColors.attentionTileConsideringBorder,
        accent = CandyColors.attentionTileConsideringAccent,
        icon = Icons.Rounded.Visibility
    )
    DecisionStatus.Saved -> AttentionItemStatusStyle(
        background = CandyColors.attentionTileSavedBg,
        border = CandyColors.attentionTileSavedBorder,
        accent = CandyColors.attentionTileSavedAccent,
        icon = Icons.Rounded.BookmarkBorder
    )
}

/**
 * HOME-05 — Home Attention Items Section ("Những món cần chú ý"):
 * - No outer enclosing white card; items sit directly on the screen background.
 * - Header: Sparkles icon in lavender container + "Những món cần chú ý" + "Tất cả ›".
 * - Item list: Up to 3 standalone Candy Item Tiles, stacked vertically with 12dp spacing.
 * - Semantic tile colors: Ready (mint), Waiting (sky blue), Considering (lavender), Saved (warm peach).
 * - Full thumbnail (72x72, 16dp radius), title (max 2 lines), price, and status in a compact row.
 */
@Composable
fun HomeAttentionItemsSection(
    sectionState: SectionState<List<DecisionUi>>,
    totalCount: Int,
    onDecisionClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    onSaveFirstItemClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenViewAll: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // 1. Header
        AttentionItemsHeader(
            canOpenViewAll = canOpenViewAll,
            onViewAllClick = onViewAllClick
        )

        // 2. Content
        when (sectionState) {
            SectionState.Loading -> AttentionItemsLoadingState()
            is SectionState.Empty -> AttentionItemsEmptyState(onSaveFirstItemClick = onSaveFirstItemClick)
            is SectionState.Error -> AttentionItemsErrorState(
                message = sectionState.message,
                canRetry = sectionState.canRetry,
                onRetryClick = onRetryClick
            )
            is SectionState.Content -> {
                if (sectionState.data.isEmpty()) {
                    AttentionItemsEmptyState(onSaveFirstItemClick = onSaveFirstItemClick)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
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

/**
 * Backward compatibility alias for [HomeAttentionItemsSection].
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
    HomeAttentionItemsSection(
        sectionState = sectionState,
        totalCount = totalCount,
        onDecisionClick = onDecisionClick,
        onViewAllClick = onViewAllClick,
        onSaveFirstItemClick = onSaveFirstItemClick,
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
        // Left: Sparkles icon in lavender container + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CandyColors.primarySoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = CandyColors.brandPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = stringResource(R.string.home_decisions_title),
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.textPrimary
            )
        }

        // Right: "Tất cả ›"
        if (canOpenViewAll) {
            val viewAllText = stringResource(R.string.home_decisions_view_all_compact)
            Row(
                modifier = Modifier
                    .heightIn(min = CandySize.touchTarget)
                    .clip(CandyShape.chip)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = viewAllText,
                        onClick = onViewAllClick
                    )
                    .padding(horizontal = CandySpace.xs, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewAllText,
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.brandPurple
                )
            }
        }
    }
}

/**
 * Individual Candy Item Tile:
 * [Product image (72x72)] [Title (max 2 lines) + Price + Status] [Chevron]
 */
@Composable
fun HomeAttentionItemTile(
    decision: DecisionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveStatus = if (decision.isTimerExpiredLocally) {
        DecisionStatus.Ready
    } else {
        decision.status
    }
    val style = resolveAttentionItemStyle(effectiveStatus)

    val displayStatusText = when (effectiveStatus) {
        DecisionStatus.Ready -> stringResource(R.string.home_decision_status_ready)
        DecisionStatus.Waiting -> {
            if (!decision.secondaryText.isNullOrBlank()) {
                "${decision.statusLabel} · ${decision.secondaryText}"
            } else {
                decision.statusLabel
            }
        }
        DecisionStatus.Considering -> decision.statusLabel
        DecisionStatus.Saved -> decision.statusLabel
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AttentionTileRadius))
            .clickable(
                role = Role.Button,
                onClickLabel = decision.title,
                onClick = onClick
            ),
        shape = RoundedCornerShape(AttentionTileRadius),
        color = style.background,
        border = BorderStroke(AttentionTileBorderWidth, style.border),
        shadowElevation = CandyElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail: 72x72dp, radius 16dp, background white
            Box(
                modifier = Modifier
                    .size(ThumbnailSize)
                    .clip(RoundedCornerShape(ThumbnailRadius))
                    .background(Color.White),
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
                    Icon(
                        imageVector = Icons.Rounded.ShoppingBag,
                        contentDescription = null,
                        tint = style.accent.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(CandySpace.md))

            // Text Column: Title + Price + Status
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = decision.title,
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
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
                    color = CandyColors.textPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = style.icon,
                        contentDescription = null,
                        tint = style.accent,
                        modifier = Modifier.size(StatusIconSize)
                    )
                    Text(
                        text = displayStatusText,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = style.accent
                    )
                }
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = style.accent.copy(alpha = 0.85f),
                modifier = Modifier.size(ChevronIconSize)
            )
        }
    }
}

@Composable
private fun AttentionItemsEmptyState(
    onSaveFirstItemClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CandyColors.attentionTileConsideringBg,
        border = BorderStroke(1.5.dp, CandyColors.attentionTileConsideringBorder),
        shadowElevation = CandyElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_decisions_empty_title),
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.textPrimary
                )
                Text(
                    text = stringResource(R.string.home_decisions_empty_subtitle),
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = CandyColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            val ctaLabel = stringResource(R.string.home_decisions_empty_cta)
            Surface(
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clip(CandyShape.chip)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = ctaLabel,
                        onClick = onSaveFirstItemClick
                    ),
                shape = CandyShape.chip,
                color = Color.White
            ) {
                Text(
                    text = ctaLabel,
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.brandPurple,
                    modifier = Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.xs)
                )
            }
        }
    }
}

@Composable
private fun AttentionItemsLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        repeat(2) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                shape = RoundedCornerShape(AttentionTileRadius),
                color = CandyColors.primarySoft.copy(alpha = 0.35f),
                border = BorderStroke(1.5.dp, CandyColors.primarySoft)
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
        shape = RoundedCornerShape(20.dp),
        color = CandyColors.attentionTileSavedBg,
        border = BorderStroke(1.5.dp, CandyColors.attentionTileSavedBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = Nunito,
                    fontSize = 13.sp,
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
                        .heightIn(min = 36.dp)
                        .clip(CandyShape.chip)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = retryText,
                            onClick = onRetryClick
                        ),
                    shape = CandyShape.chip,
                    color = Color.White
                ) {
                    Text(
                        text = retryText,
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.brandPurple,
                        modifier = Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.xs)
                    )
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

private val previewItemReady = DecisionUi(
    id = "p_ready",
    title = "Áo khoác vintage",
    priceMinor = 680_000L,
    priceFormatted = "680.000 ₫",
    status = DecisionStatus.Ready,
    statusLabel = "Sẵn sàng xem lại"
)

private val previewItemWaiting1 = DecisionUi(
    id = "p_wait1",
    title = "Sony WH-1000XM5",
    priceMinor = 7_490_000L,
    priceFormatted = "7.490.000 ₫",
    status = DecisionStatus.Waiting,
    statusLabel = "Đang chờ",
    secondaryText = "Còn 18 giờ 24 phút",
    remainingSeconds = 66_240L
)

private val previewItemWaiting2 = DecisionUi(
    id = "p_wait2",
    title = "Keychron K2",
    priceMinor = 1_850_000L,
    priceFormatted = "1.850.000 ₫",
    status = DecisionStatus.Waiting,
    statusLabel = "Đang chờ",
    secondaryText = "Còn 6 giờ 10 phút",
    remainingSeconds = 22_200L
)

private val previewItemConsidering = DecisionUi(
    id = "p_consider",
    title = "Màn hình LG UltraFine 4K 27 inch",
    priceMinor = 9_200_000L,
    priceFormatted = "9.200.000 ₫",
    status = DecisionStatus.Considering,
    statusLabel = "Đang cân nhắc"
)

private val previewItemSaved = DecisionUi(
    id = "p_saved",
    title = "Bình giữ nhiệt Stanley Aerolight",
    priceMinor = 850_000L,
    priceFormatted = "850.000 ₫",
    status = DecisionStatus.Saved,
    statusLabel = "Đã lưu"
)

private val previewItemLongName = DecisionUi(
    id = "p_long",
    title = "Bàn phím cơ Custom HHKB Professional HYBRID Type-S Snow Limited Edition kết nối Bluetooth",
    priceMinor = 8_500_000L,
    priceFormatted = "8.500.000 ₫",
    status = DecisionStatus.Waiting,
    statusLabel = "Đang chờ",
    secondaryText = "Còn 12 giờ 30 phút",
    remainingSeconds = 45_000L
)

@Preview(name = "1 Ready + 2 Waiting", showBackground = true, widthDp = 393)
@Composable
private fun HomeAttentionItemsPreview1Ready2Waiting() {
    ChankoTheme {
        HomeAttentionItemsSection(
            sectionState = SectionState.Content(listOf(previewItemReady, previewItemWaiting1, previewItemWaiting2)),
            totalCount = 3,
            onDecisionClick = {},
            onViewAllClick = {},
            onSaveFirstItemClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "3 Waiting", showBackground = true, widthDp = 393)
@Composable
private fun HomeAttentionItemsPreview3Waiting() {
    ChankoTheme {
        HomeAttentionItemsSection(
            sectionState = SectionState.Content(listOf(previewItemWaiting1, previewItemWaiting2, previewItemLongName)),
            totalCount = 3,
            onDecisionClick = {},
            onViewAllClick = {},
            onSaveFirstItemClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Considering + Saved", showBackground = true, widthDp = 393)
@Composable
private fun HomeAttentionItemsPreviewConsideringSaved() {
    ChankoTheme {
        HomeAttentionItemsSection(
            sectionState = SectionState.Content(listOf(previewItemConsidering, previewItemSaved)),
            totalCount = 2,
            onDecisionClick = {},
            onViewAllClick = {},
            onSaveFirstItemClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Empty State", showBackground = true, widthDp = 393)
@Composable
private fun HomeAttentionItemsPreviewEmpty() {
    ChankoTheme {
        HomeAttentionItemsSection(
            sectionState = SectionState.Empty("Chưa có món đồ nào đang chờ"),
            totalCount = 0,
            onDecisionClick = {},
            onViewAllClick = {},
            onSaveFirstItemClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Long Product Name", showBackground = true, widthDp = 393)
@Composable
private fun HomeAttentionItemsPreviewLongName() {
    ChankoTheme {
        HomeAttentionItemsSection(
            sectionState = SectionState.Content(listOf(previewItemLongName, previewItemReady)),
            totalCount = 2,
            onDecisionClick = {},
            onViewAllClick = {},
            onSaveFirstItemClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Large Font (2.0x)", showBackground = true, widthDp = 393, fontScale = 2.0f)
@Composable
private fun HomeAttentionItemsPreviewLargeFont() {
    ChankoTheme {
        HomeAttentionItemsSection(
            sectionState = SectionState.Content(listOf(previewItemReady, previewItemWaiting1)),
            totalCount = 2,
            onDecisionClick = {},
            onViewAllClick = {},
            onSaveFirstItemClick = {},
            onRetryClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}
