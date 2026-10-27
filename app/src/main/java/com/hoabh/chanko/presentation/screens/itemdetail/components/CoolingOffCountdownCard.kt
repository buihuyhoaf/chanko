package com.hoabh.chanko.presentation.screens.itemdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.myitems.ItemDisplayStatus
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * DETAIL-07: Cooling Off countdown nổi bật.
 */
@Composable
fun CoolingOffCountdownCard(
    displayStatus: ItemDisplayStatus,
    remainingFormatted: String,
    modifier: Modifier = Modifier
) {
    val isReady = displayStatus == ItemDisplayStatus.ReadyForReview
    val containerBg = if (isReady) CandyColors.readySurface else CandyColors.blue.copy(alpha = 0.15f)
    val accentColor = if (isReady) CandyColors.readyAction else Color(0xFF1B5A8A)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = containerBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                ChankoIcon(
                    id = if (isReady) ChankoIconId.ITM_05 else ChankoIconId.ITM_04,
                    contentDescription = null,
                    size = 24.dp
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isReady) {
                        stringResource(R.string.item_detail_countdown_ready)
                    } else {
                        stringResource(R.string.item_detail_countdown_title)
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = remainingFormatted,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.item_detail_countdown_sub),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = CandyColors.textSecondary
                )
            }
        }
    }
}
