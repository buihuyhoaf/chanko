package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun BudgetReadOnlyBanner(
    isOnline: Boolean,
    lastSyncTimeText: String?,
    isPastCycle: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
    ) {
        // 1. Offline Banner (C11)
        if (!isOnline) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyColors.budgetWarningBadgeBg)
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                ChankoIcon(
                    id = ChankoIconId.SYS_10,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.budget_state_offline_banner),
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CandyColors.budgetWarningBadgeInk
                    )
                    if (!lastSyncTimeText.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.budget_state_last_sync_format, lastSyncTimeText),
                            fontFamily = Nunito,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = CandyColors.budgetWarningBadgeInk.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 2. Past-Cycle Badge (C12)
        if (isPastCycle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CandyColors.decisionSavedPillBg)
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                Text(
                    text = "🔒",
                    fontSize = 12.sp
                )
                Text(
                    text = stringResource(R.string.budget_state_past_badge),
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.decisionSavedPillInk
                )
            }
        }
    }
}
