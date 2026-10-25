package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
