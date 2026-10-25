package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.ui.theme.CandyColors

@Composable
fun ChatMascotHeader(
    mode: ThreadMode,
    linkedItemName: String?,
    emotion: MascotEmotion,
    modifier: Modifier = Modifier
) {
    val mascotRes = when (emotion) {
        MascotEmotion.CONCERNED -> R.drawable.chanko_concerned
        MascotEmotion.CURIOUS -> R.drawable.chanko_curious
        MascotEmotion.ENCOURAGING -> R.drawable.chanko_encouraging
        MascotEmotion.NEUTRAL -> R.drawable.chanko_neutral
        MascotEmotion.RELAXED -> R.drawable.chanko_relaxed
        MascotEmotion.SLEEPING -> R.drawable.chanko_sleeping
        MascotEmotion.THINKING -> R.drawable.chanko_thinking
        MascotEmotion.WELCOME -> R.drawable.chanko_welcome
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CandyColors.backgroundLavender)
            .border(2.dp, CandyColors.brandPurple.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, CandyColors.brandPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = mascotRes),
                    contentDescription = "Chanko Mascot",
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (mode == ThreadMode.ITEM && !linkedItemName.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CandyColors.yellow)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MÓN ĐỒ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = linkedItemName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyColors.textPrimary
                        )
                    }
                } else {
                    Text(
                        text = "Trò chuyện tự do cùng Chanko",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyColors.textPrimary
                    )
                }
            }

            // Prototype Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEDE6F8))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PROTOTYPE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CandyColors.brandPurple
                )
            }
        }
    }
}
