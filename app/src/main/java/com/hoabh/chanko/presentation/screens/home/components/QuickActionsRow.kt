package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme

private val QuickActionIconSize = 26.dp
private val QuickActionBorderWidth = 2.dp

/**
 * HOME-03 — Home Quick Actions (Candy Pop 3D):
 * - Two action cards side by side sharing equal width and height:
 *   1. "Hỏi Chanko" / "Cân nhắc mua" (Message/Question icon, purple background)
 *   2. "Lưu món đồ" / "Để sau nhé" (Bookmark Plus icon, pink background)
 * - Icon on left, text column on right.
 * - Equal height in row layout via [IntrinsicSize.Min].
 * - Responsive vertical stack reflow on narrow screens (< 340dp) or large font scale (>= 1.45f).
 * - No text ellipsis; multiline text is allowed without clipping.
 */
@Composable
fun HomeQuickActions(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(CandySpace.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickCheckActionCard(
                    onClick = onQuickCheckClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SaveAnItemActionCard(
                    onClick = onSaveItemClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Alias for [HomeQuickActions] to preserve existing callers.
 */
@Composable
fun QuickActionsRow(
    onQuickCheckClick: () -> Unit,
    onSaveItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeQuickActions(
        onQuickCheckClick = onQuickCheckClick,
        onSaveItemClick = onSaveItemClick,
        modifier = modifier
    )
}

@Composable
private fun QuickCheckActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuickActionCard(
        title = stringResource(R.string.home_action_ask_title),
        subtitle = stringResource(R.string.home_action_ask_subtitle),
        icon = Icons.AutoMirrored.Rounded.HelpOutline,
        backgroundColor = CandyColors.quickActionAskBg,
        borderColor = CandyColors.quickActionAskBorder,
        contentColor = CandyColors.quickActionAskText,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SaveAnItemActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuickActionCard(
        title = stringResource(R.string.home_action_save_title),
        subtitle = stringResource(R.string.home_action_save_subtitle),
        icon = Icons.Rounded.BookmarkAdd,
        backgroundColor = CandyColors.quickActionSaveBg,
        borderColor = CandyColors.quickActionSaveBorder,
        contentColor = CandyColors.quickActionSaveText,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    borderColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = CandySize.touchTarget)
            .clip(CandyShape.card)
            .clickable(
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            ),
        shape = CandyShape.card,
        color = backgroundColor,
        border = BorderStroke(QuickActionBorderWidth, borderColor),
        shadowElevation = CandyElevation.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(QuickActionIconSize)
            )

            Spacer(modifier = Modifier.width(CandySpace.sm))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = contentColor.copy(alpha = 0.92f)
                )
            }
        }
    }
}

@Preview(name = "Home Quick Actions - Default (Font 1.0x)", showBackground = true, widthDp = 393)
@Composable
private fun HomeQuickActionsPreviewDefault() {
    ChankoTheme {
        HomeQuickActions(
            onQuickCheckClick = {},
            onSaveItemClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Quick Actions - Large Font (Font 2.0x)", showBackground = true, widthDp = 393, fontScale = 2.0f)
@Composable
private fun HomeQuickActionsPreviewLargeFont() {
    ChankoTheme {
        HomeQuickActions(
            onQuickCheckClick = {},
            onSaveItemClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}

@Preview(name = "Home Quick Actions - Narrow Screen (320dp)", showBackground = true, widthDp = 320)
@Composable
private fun HomeQuickActionsPreviewNarrowScreen() {
    ChankoTheme {
        HomeQuickActions(
            onQuickCheckClick = {},
            onSaveItemClick = {},
            modifier = Modifier.padding(CandySpace.screen)
        )
    }
}
