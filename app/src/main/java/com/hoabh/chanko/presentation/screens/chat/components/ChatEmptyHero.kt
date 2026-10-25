package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * S01 — Empty Hero State:
 * Hiển thị linh vật Chanko Welcome (200x208dp) ở giữa và các gợi ý Starter Prompts
 * khi phiên trò chuyện chưa có tin nhắn nào.
 */
@Composable
fun ChatEmptyHero(
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chanko_welcome),
            contentDescription = "Chanko Welcome",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chào bạn! Tôi là Chanko.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CandyColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Hãy chia sẻ món đồ bạn đang định mua,\ntôi sẽ giúp bạn hạ nhiệt và đánh giá khách quan!",
            fontSize = 13.sp,
            color = CandyColors.mutedInk,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        val prompts = listOf(
            "Tôi vừa thấy một món đồ rất thích...",
            "Làm sao để biết tôi thực sự cần món này?",
            "Quy đổi món đồ này ra bao nhiêu giờ làm việc?"
        )

        prompts.forEach { prompt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.5.dp, CandyColors.divider, RoundedCornerShape(16.dp))
                    .clickable { onPromptClick(prompt) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CandyColors.brandPurple)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = prompt,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = CandyColors.textPrimary
                    )
                }
            }
        }
    }
}
