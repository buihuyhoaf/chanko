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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

private val HeaderAvatarVisualSize = 52.dp

/**
 * Khu vực 1 — Header (Home v2.0 — AC-02, AC-20, AC-31):
 * - Dòng lời chào theo buổi ("Chào buổi tối 🌙").
 * - Nút Profile tròn bên phải dùng icon chuẩn `SYS_03` mở Profile & Settings gộp (AC-02).
 * - Khi offline: hiển thị badge ngoại tuyến kèm thời điểm đồng bộ (`lastSyncedAtText` — AC-20).
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
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
                    fontSize = 24.sp,
                    lineHeight = 30.sp
                ),
                color = CandyColors.HomeV2.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.width(CandySpace.sm))

            // Profile & Settings Button dùng icon Profile chuẩn (SYS_03) để di chuyển qua màn hình Profile (AC-02 & AC-31)
            if (canOpenProfile) {
                val profileCd = stringResource(R.string.home_cd_profile)
                Surface(
                    modifier = Modifier
                        .size(HeaderAvatarVisualSize)
                        .shadow(elevation = 4.dp, shape = CircleShape)
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
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(CandyColors.primarySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.SYS_03,
                            contentDescription = profileCd,
                            size = 28.dp
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
