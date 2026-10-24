package com.hoabh.chanko.presentation.screens.itemdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.myitems.ItemDisplayStatus
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * DETAIL-06: Sticky Primary CTA + More Options vùng đáy có nền surface, insets an toàn.
 */
@Composable
fun StickyBottomActionBar(
    primaryCtaText: String,
    displayStatus: ItemDisplayStatus,
    onPrimaryCtaClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg, vertical = CandySpace.md)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrimaryCtaClick,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (displayStatus == ItemDisplayStatus.ReadyForReview) CandyColors.primary else CandyColors.primarySoft,
                    contentColor = if (displayStatus == ItemDisplayStatus.ReadyForReview) CandyColors.textPrimary else CandyColors.primaryInk
                )
            ) {
                Text(
                    text = primaryCtaText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onMoreOptionsClick),
                shape = RoundedCornerShape(16.dp),
                color = CandyColors.background
            ) {
                Box(
                    modifier = Modifier.padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = stringResource(R.string.item_detail_more_options),
                        tint = CandyColors.textPrimary
                    )
                }
            }
        }
    }
}
