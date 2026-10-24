package com.hoabh.chanko.presentation.screens.myitems.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.mascot.ChankoMascot
import com.hoabh.chanko.ui.mascot.MascotPresentationState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * ITEM-02: Compact 3D Chanko Hero
 * Thể hiện sự đồng hành nhẹ nhàng, kết hợp mascot Chanko và lời nhắn tĩnh tâm.
 */
@Composable
fun MyItemsHero(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        CandyColors.backgroundLavender,
                        CandyColors.backgroundPink
                    )
                )
            )
            .padding(CandySpace.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.my_items_hero_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 26.sp
                    ),
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(CandySpace.xs))
                Text(
                    text = stringResource(R.string.my_items_hero_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = CandyColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.width(CandySpace.sm))
            // Chanko mascot slot an toàn
            ChankoMascot(
                state = MascotPresentationState.Curious,
                modifier = Modifier
            )
        }
    }
}
