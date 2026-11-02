package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.GreetingPeriod
import com.hoabh.chanko.presentation.screens.home.HeaderUiModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val HeaderControlVisualSize = 42.dp

/**
 * Khu vực 1 — Header (AC-03, AC-16, HOME-EDGE-05):
 * - Lời chào theo buổi kèm biểu tượng mặt trời/mặt trăng (`"Chào buổi sáng, Hoa ☀️"`).
 * - Phụ đề ấm áp (`"Hôm nay mình cùng Chanko mua sắm có suy nghĩ nhé 💜"`).
 * - Nút Avatar tròn tím (`CandyColors.brandPurple`) mở Profile & Settings, vùng chạm >= 48dp.
 * - Khi offline: hiển thị badge ngoại tuyến kèm thời điểm đồng bộ thực tế (`lastSyncedAtText`).
 */
@Composable
fun HomeHeader(
    header: HeaderUiModel,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    canOpenProfile: Boolean = true
) {
    val greetingText = header.customGreeting ?: resolveGreetingText(header)
    val greetingEmoji = when (header.greetingPeriod) {
        GreetingPeriod.MORNING -> " ☀️"
        GreetingPeriod.AFTERNOON -> " 🌤️"
        GreetingPeriod.EVENING -> " 🌙"
    }
    val subtitleText = header.customSubtitle ?: stringResource(R.string.home_header_subtitle)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CandySize.touchTarget),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { heading() },
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                Text(
                    text = "$greetingText$greetingEmoji",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = CandyColors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            // Profile Avatar Button in Purple Circle (Touch target >= 48dp, AC-03 & AC-20)
            if (canOpenProfile) {
                val profileCd = stringResource(R.string.home_cd_profile)
                Box(
                    modifier = Modifier
                        .size(CandySize.touchTarget)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = profileCd,
                            onClick = onProfileClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(HeaderControlVisualSize)
                            .clip(CircleShape)
                            .background(CandyColors.brandPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = header.avatarInitial,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = CandyColors.onReadyAction
                        )
                    }
                }
            }
        }

        if (header.isOffline) {
            val offlineText = if (!header.lastSyncedAtText.isNullOrBlank()) {
                stringResource(R.string.home_header_offline_badge_with_sync, header.lastSyncedAtText)
            } else {
                stringResource(R.string.home_header_offline_badge)
            }
            Surface(
                shape = CandyShape.chip,
                color = CandyColors.primarySoft
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
