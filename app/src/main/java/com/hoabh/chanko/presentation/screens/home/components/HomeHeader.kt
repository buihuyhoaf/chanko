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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
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
import com.hoabh.chanko.presentation.screens.home.GreetingPeriod
import com.hoabh.chanko.presentation.screens.home.HeaderUiModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val HeaderControlVisualSize = 40.dp
private val HeaderSettingsIconSize = 22.dp

/**
 * HOME-01 — Header:
 * - Hiển thị lời chào theo ngữ cảnh, hỗ trợ tên dài, tên trống và font scaling.
 * - Các control Settings và Avatar có vùng chạm độc lập >= 48dp và callback rõ ràng.
 */
@Composable
fun HomeHeader(
    header: HeaderUiModel,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greetingText = resolveGreetingText(header)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = CandySize.primaryButton),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { heading() },
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.home_header_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = CandyColors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            // Settings Button (Touch target >= 48dp)
            Box(
                modifier = Modifier
                    .size(CandySize.touchTarget)
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Button,
                        onClick = onSettingsClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(HeaderControlVisualSize),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = CandyElevation.low,
                    shadowElevation = CandyElevation.low
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.home_cd_settings),
                            tint = CandyColors.textPrimary,
                            modifier = Modifier.size(HeaderSettingsIconSize)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(CandySpace.xs))

            // Profile Avatar Button (Touch target >= 48dp)
            val profileContentDescription = stringResource(R.string.home_cd_profile)
            Box(
                modifier = Modifier
                    .size(CandySize.touchTarget)
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = profileContentDescription,
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
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (header.isOffline) {
            Surface(
                shape = CandyShape.chip,
                color = CandyColors.primarySoft
            ) {
                Text(
                    text = stringResource(R.string.home_header_offline_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = CandyColors.primaryInk,
                    modifier = Modifier.padding(
                        horizontal = CandySpace.md,
                        vertical = CandySpace.xs
                    )
                )
            }
        }
    }
}

@Composable
private fun resolveGreetingText(header: HeaderUiModel): String {
    return if (header.hasName) {
        val resId = when (header.greetingPeriod) {
            GreetingPeriod.MORNING -> R.string.home_greeting_morning_named
            GreetingPeriod.AFTERNOON -> R.string.home_greeting_afternoon_named
            GreetingPeriod.EVENING -> R.string.home_greeting_evening_named
        }
        stringResource(resId, header.displayName)
    } else {
        val resId = when (header.greetingPeriod) {
            GreetingPeriod.MORNING -> R.string.home_greeting_morning_anonymous
            GreetingPeriod.AFTERNOON -> R.string.home_greeting_afternoon_anonymous
            GreetingPeriod.EVENING -> R.string.home_greeting_evening_anonymous
        }
        stringResource(resId)
    }
}
