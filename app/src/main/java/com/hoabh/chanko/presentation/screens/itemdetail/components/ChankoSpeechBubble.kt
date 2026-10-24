package com.hoabh.chanko.presentation.screens.itemdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.ui.mascot.ChankoMascot
import com.hoabh.chanko.ui.mascot.MascotPresentationState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * DETAIL-04: Chanko nhỏ bên cạnh contextual speech bubble, nằm dưới status / cooling card,
 * tách biệt hoàn toàn khỏi media container của sản phẩm.
 */
@Composable
fun ChankoSpeechBubble(
    speechText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small mascot slot: 48-72dp reference
        ChankoMascot(
            state = MascotPresentationState.Curious,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.width(CandySpace.sm))

        // Contextual Speech Bubble
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(Color.White)
                .padding(horizontal = CandySpace.md, vertical = CandySpace.sm)
        ) {
            Text(
                text = speechText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = CandyColors.textPrimary
            )
        }
    }
}
