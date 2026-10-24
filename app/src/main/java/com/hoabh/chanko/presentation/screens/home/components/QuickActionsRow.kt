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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val QuickActionIconCircleSize = 36.dp
private val QuickActionInnerIconSize = 20.dp
private val QuickActionChevronSize = 18.dp
private val QuickActionMinHeight = 68.dp

/**
 * HOME-03 — Quick Actions (v2 Image-Aligned):
 * - Hai hành động trực tiếp (Direct Actions) xuất hiện ngay dưới Compact Living Room:
 *   1. `Quick Check`: Thẻ nền tím gradient, icon kính lúp trong vòng tròn trắng, `"Quick Check"` / `"Hỏi Chanko trước khi mua"`.
 *   2. `Save an Item`: Thẻ nền sáng lavender, icon `+` trắng trong vòng tròn tím, `"Save an Item"` / `"Lưu để xem lại sau"`.
 * - Tự động chuyển sang bố cục xếp dọc trên màn hẹp (`<= 340dp`) hoặc khi `fontScale >= 1.45f` để không cắt chữ.
 */
@Composable
fun QuickActionsRow(
    onQuickCheckClick: () -> Unit,
    onSaveItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.45f

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stackVertically = maxWidth < 340.dp || isLargeFont

        if (stackVertically) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                QuickCheckActionCard(
                    onClick = onQuickCheckClick,
                    modifier = Modifier.fillMaxWidth()
                )
                SaveAnItemActionCard(
                    onClick = onSaveItemClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickCheckActionCard(
                    onClick = onQuickCheckClick,
                    modifier = Modifier.weight(1f)
                )
                SaveAnItemActionCard(
                    onClick = onSaveItemClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickCheckActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(R.string.home_quick_check_title)
    val subtitle = stringResource(R.string.home_quick_check_subtitle)
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            CandyColors.quickCheckGradientStart,
            CandyColors.quickCheckGradientEnd
        )
    )

    Surface(
        modifier = modifier
            .heightIn(min = QuickActionMinHeight)
            .clip(CandyShape.smallCard)
            .clickable(
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            ),
        shape = CandyShape.smallCard,
        color = CandyColors.brandPurple,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(horizontal = CandySpace.md, vertical = CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(QuickActionIconCircleSize)
                    .clip(CircleShape)
                    .background(CandyColors.onReadyAction),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = CandyColors.brandPurple,
                    modifier = Modifier.size(QuickActionInnerIconSize)
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.onReadyAction,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CandyColors.onReadyAction.copy(alpha = 0.92f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = CandyColors.onReadyAction.copy(alpha = 0.9f),
                modifier = Modifier.size(QuickActionChevronSize)
            )
        }
    }
}

@Composable
private fun SaveAnItemActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(R.string.home_save_item_title)
    val subtitle = stringResource(R.string.home_save_item_subtitle)

    Surface(
        modifier = modifier
            .heightIn(min = QuickActionMinHeight)
            .clip(CandyShape.smallCard)
            .clickable(
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            ),
        shape = CandyShape.smallCard,
        color = CandyColors.saveItemSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.saveItemBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.md, vertical = CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(QuickActionIconCircleSize)
                    .clip(CircleShape)
                    .background(CandyColors.brandPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = CandyColors.onReadyAction,
                    modifier = Modifier.size(QuickActionInnerIconSize)
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CandyColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = CandyColors.textSecondary,
                modifier = Modifier.size(QuickActionChevronSize)
            )
        }
    }
}
