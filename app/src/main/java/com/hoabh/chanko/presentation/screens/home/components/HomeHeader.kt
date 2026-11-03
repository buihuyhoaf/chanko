package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.GreetingPeriod
import com.hoabh.chanko.presentation.screens.home.HeaderUiModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme

/**
 * Hằng số kích thước và token cho HomeHeader hỗ trợ Collapsing & Sticky.
 */
object HomeHeaderTokens {
    val AvatarExpandedSize = 52.dp
    val AvatarCollapsedSize = 38.dp
    val CollapsedMinHeight = 56.dp
    val CollapseScrollRange = 96.dp
}

/**
 * Hàm tính toán tỷ lệ co giãn (collapseFraction) chuẩn hóa trong đoạn [0f, 1f].
 */
fun calculateCollapseFraction(scrollOffsetPx: Int, maxCollapseRangePx: Float): Float {
    if (maxCollapseRangePx <= 0f) return 0f
    return (scrollOffsetPx / maxCollapseRangePx).coerceIn(0f, 1f)
}

/**
 * Khu vực 1 — Header (Home v2.0 — AC-02, AC-20, AC-31) hỗ trợ Collapsing & Sticky:
 * - Khi mở rộng (`collapseFraction == 0f`):
 *   + Lời chào buổi lớn 24sp ("Chào buổi tối 🌙").
 *   + Avatar tròn 52dp viền tím mở Profile & Settings (AC-02).
 *   + Dải banner ngoại tuyến đầy đủ (khi offline — AC-20).
 * - Khi thu gọn (`collapseFraction > 0f` đến `1f`):
 *   + Tiêu đề co lại 18sp và giới hạn 1 dòng.
 *   + Avatar thu gọn mượt mà xuống 38dp.
 *   + Banner dài mờ dần; thay vào đó là badge ngoại tuyến mini ngay cạnh Avatar (duy trì chỉ báo offline).
 *   + Nền kính mờ (`CandyColors.HomeV2.headerFrostedSurface`) che chắn nội dung cuộn bên dưới.
 */
@Composable
fun HomeHeader(
    header: HeaderUiModel,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseFraction: Float = 0f,
    canOpenProfile: Boolean = true
) {
    val clampedFraction = collapseFraction.coerceIn(0f, 1f)
    val greetingText = header.customGreeting ?: resolveGreetingText(header)
    val greetingEmoji = when (header.greetingPeriod) {
        GreetingPeriod.MORNING -> " ☀️"
        GreetingPeriod.AFTERNOON -> " 🌤️"
        GreetingPeriod.EVENING -> " 🌙"
    }

    val avatarVisualSize = lerp(
        HomeHeaderTokens.AvatarExpandedSize,
        HomeHeaderTokens.AvatarCollapsedSize,
        clampedFraction
    )
    val iconSize = lerp(28.dp, 20.dp, clampedFraction)
    val avatarElevation = lerp(4.dp, 1.dp, clampedFraction)
    val greetingFontSize = lerp(24.sp, 18.sp, clampedFraction)
    val greetingLineHeight = lerp(30.sp, 24.sp, clampedFraction)

    val surfaceAlpha = (clampedFraction * 1.25f).coerceIn(0f, 0.96f)
    val bottomDividerAlpha = (clampedFraction * 0.8f).coerceIn(0f, 0.4f)
    val verticalSpacing = lerp(CandySpace.xs, 0.dp, clampedFraction)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CandyColors.HomeV2.headerFrostedSurface.copy(alpha = surfaceAlpha)
            )
            .drawBehind {
                if (bottomDividerAlpha > 0f) {
                    drawLine(
                        color = CandyColors.HomeV2.headerBorderDivider.copy(alpha = bottomDividerAlpha),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            },
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CandySize.touchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$greetingText$greetingEmoji",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = greetingFontSize,
                    lineHeight = greetingLineHeight
                ),
                color = CandyColors.HomeV2.textPrimary,
                maxLines = if (clampedFraction > 0.5f) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.width(CandySpace.sm))

            // Chỉ báo ngoại tuyến mini khi Header ở trạng thái thu gọn (Collapsed)
            if (header.isOffline && clampedFraction > 0.35f) {
                val compactAlpha = ((clampedFraction - 0.35f) / 0.65f).coerceIn(0f, 1f)
                val offlineCompactLabel = stringResource(R.string.home_header_offline_compact)
                Surface(
                    shape = CandyShape.chip,
                    color = CandyColors.primarySoft.copy(alpha = compactAlpha),
                    modifier = Modifier
                        .graphicsLayer { alpha = compactAlpha }
                        .padding(end = CandySpace.xs)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = CandySpace.sm, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.SYS_10,
                            contentDescription = null,
                            size = 12.dp
                        )
                        Text(
                            text = offlineCompactLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = CandyColors.primaryInk
                        )
                    }
                }
            }

            // Profile & Settings Button dùng icon Profile chuẩn (SYS_03)
            if (canOpenProfile) {
                val profileCd = stringResource(R.string.home_cd_profile)
                Surface(
                    modifier = Modifier
                        .size(avatarVisualSize)
                        .shadow(elevation = avatarElevation, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = profileCd,
                            onClick = onProfileClick
                        ),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, CandyColors.homeCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(lerp(4.dp, 2.dp, clampedFraction))
                            .clip(CircleShape)
                            .background(CandyColors.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.SYS_03,
                            contentDescription = profileCd,
                            size = iconSize
                        )
                    }
                }
            }
        }

        // Dải banner ngoại tuyến đầy đủ (ẩn dần khi cuộn xuống)
        if (header.isOffline && clampedFraction < 0.85f) {
            val bannerAlpha = (1f - clampedFraction * 1.4f).coerceIn(0f, 1f)
            if (bannerAlpha > 0f) {
                val offlineText = if (!header.lastSyncedAtText.isNullOrBlank()) {
                    stringResource(R.string.home_header_offline_badge_with_sync, header.lastSyncedAtText)
                } else {
                    stringResource(R.string.home_header_offline_badge)
                }
                Surface(
                    shape = CandyShape.chip,
                    color = CandyColors.primarySoft,
                    modifier = Modifier.graphicsLayer { alpha = bannerAlpha }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = CandySpace.md,
                            vertical = CandySpace.xs
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.SYS_10,
                            contentDescription = null,
                            size = 16.dp
                        )
                        Text(
                            text = offlineText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = CandyColors.primaryInk
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveGreetingText(header: HeaderUiModel): String {
    return if (header.hasName) {
        when (header.greetingPeriod) {
            GreetingPeriod.MORNING -> stringResource(R.string.home_greeting_morning_named, header.displayName)
            GreetingPeriod.AFTERNOON -> stringResource(R.string.home_greeting_afternoon_named, header.displayName)
            GreetingPeriod.EVENING -> stringResource(R.string.home_greeting_evening_named, header.displayName)
        }
    } else {
        when (header.greetingPeriod) {
            GreetingPeriod.MORNING -> stringResource(R.string.home_greeting_morning_anonymous)
            GreetingPeriod.AFTERNOON -> stringResource(R.string.home_greeting_afternoon_anonymous)
            GreetingPeriod.EVENING -> stringResource(R.string.home_greeting_evening_anonymous)
        }
    }
}

@Preview(name = "Expanded Online", showBackground = true)
@Composable
private fun HomeHeaderExpandedOnlinePreview() {
    ChankoTheme {
        HomeHeader(
            header = HeaderUiModel(
                greetingPeriod = GreetingPeriod.EVENING,
                userName = "Hoa",
                isOffline = false
            ),
            onProfileClick = {},
            collapseFraction = 0f
        )
    }
}

@Preview(name = "Expanded Offline", showBackground = true)
@Composable
private fun HomeHeaderExpandedOfflinePreview() {
    ChankoTheme {
        HomeHeader(
            header = HeaderUiModel(
                greetingPeriod = GreetingPeriod.MORNING,
                userName = "Hoa",
                isOffline = true,
                lastSyncedAtText = "10:30"
            ),
            onProfileClick = {},
            collapseFraction = 0f
        )
    }
}

@Preview(name = "Collapsed Offline", showBackground = true)
@Composable
private fun HomeHeaderCollapsedOfflinePreview() {
    ChankoTheme {
        HomeHeader(
            header = HeaderUiModel(
                greetingPeriod = GreetingPeriod.MORNING,
                userName = "Hoa",
                isOffline = true,
                lastSyncedAtText = "10:30"
            ),
            onProfileClick = {},
            collapseFraction = 1f
        )
    }
}
