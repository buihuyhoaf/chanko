package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * S06 — Offline Connectivity Banner:
 * Hiển thị thanh thông báo màu vàng/cam khi mất kết nối mạng.
 * Vô hiệu hóa tính năng gửi/bind theo hợp đồng A4 (chỉ cho phép đọc offline cache).
 */
@Composable
fun ChatOfflineBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFF3CD))
            .border(1.dp, CandyColors.budgetWarning, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.chanko_relaxed),
                contentDescription = "Chanko Relaxed",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Ngoại tuyến — Chỉ đọc lịch sử",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Text(
                    text = "Không thể gửi tin nhắn mới khi mất mạng",
                    fontSize = 10.sp,
                    color = CandyColors.mutedInk
                )
            }
        }
    }
}

/**
 * Bong bóng chỉ báo trạng thái Chanko đang gõ / suy nghĩ câu trả lời (Bouncing Dots Animation).
 * Phong cách Candy Pop 3D: Mascot avatar 32dp + Bong bóng trắng bo góc + 3 chấm tròn tím nảy tuần tự.
 */
@Composable
fun ChankoTypingBubble(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ChankoTypingDots")

    // Chấm 1 nảy từ 0ms đến 600ms
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 0
                -6f at 300 using FastOutSlowInEasing
                0f at 600 using FastOutSlowInEasing
                0f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot1Offset"
    )

    // Chấm 2 nảy lệch pha 150ms
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 0
                0f at 150
                -6f at 450 using FastOutSlowInEasing
                0f at 750 using FastOutSlowInEasing
                0f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot2Offset"
    )

    // Chấm 3 nảy lệch pha 300ms
    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 0
                0f at 300
                -6f at 600 using FastOutSlowInEasing
                0f at 900 using FastOutSlowInEasing
                0f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot3Offset"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar mascot Chanko suy nghĩ
        Image(
            painter = painterResource(id = R.drawable.chanko_thinking),
            contentDescription = "Chanko đang suy nghĩ",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Bong bóng typing với 3 chấm nảy
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomEnd = 18.dp,
                        bottomStart = 18.dp
                    )
                )
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = CandyColors.divider,
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomEnd = 18.dp,
                        bottomStart = 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer { translationY = offset1.dp.toPx() }
                        .background(CandyColors.brandPurple, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer { translationY = offset2.dp.toPx() }
                        .background(CandyColors.brandPurple, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer { translationY = offset3.dp.toPx() }
                        .background(CandyColors.brandPurple, CircleShape)
                )
            }
        }
    }
}

/**
 * S07 — Loading / Thinking Status:
 * Hiển thị trạng thái Chanko đang phân tích / suy nghĩ (chanko_thinking) kèm hiệu ứng quay nhẹ.
 */
@Composable
fun ChatThinkingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Color.White)
                .border(1.dp, CandyColors.divider, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.chanko_thinking),
                    contentDescription = "Chanko Thinking",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = CandyColors.brandPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chanko đang suy nghĩ...",
                    fontSize = 12.sp,
                    color = CandyColors.mutedInk
                )
            }
        }
    }
}

/**
 * S08 — Error / Unknown Outcome Banner:
 * Hiển thị lỗi kèm hình chú mèo Chanko Concerned và nút Thử lại (Reconcile / Retry).
 */
@Composable
fun ChatErrorBanner(
    errorMessage: String,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CandyColors.speechSurface)
            .border(1.dp, CandyColors.error, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.chanko_concerned),
                contentDescription = "Chanko Concerned",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Không thể hoàn thành",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.error
                )
                Text(
                    text = errorMessage,
                    fontSize = 11.sp,
                    color = CandyColors.textPrimary
                )
            }
            Button(
                onClick = onRetryClicked,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Thử lại",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
