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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.DecisionVerdict
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.ui.theme.CandyColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Bong bóng tin nhắn Chat with Chanko theo chuẩn CHAT-UX-REV-01:
 * - User bubble: Chữ tối tương phản cao trên nền lavender dịu (tối đa 82% chiều ngang).
 * - Chanko bubble: Avatar 32dp bên ngoài, bong bóng nền trắng, tên "Chanko" và thời gian.
 * - Decision Card: Render trung tính (Neutral BUY, mascot Neutral, nút Xem món đồ).
 */
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onViewItemClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(message.timestamp))

    when (message.sender) {
        MessageSender.USER -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(modifier = Modifier.weight(0.18f))
                Column(
                    modifier = Modifier.weight(0.82f, fill = false),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp))
                            .background(Color(0xFFEDE6F8))
                            .border(1.dp, Color(0xFFDDD2F2), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = message.text,
                            color = Color(0xFF241B35),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = CandyColors.mutedInk,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
        MessageSender.CHANKO -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar Chanko 32dp
                Image(
                    painter = painterResource(id = R.drawable.chanko_neutral),
                    contentDescription = "Chanko",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Chanko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyColors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "· $timeString",
                            fontSize = 11.sp,
                            color = CandyColors.mutedInk
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                            .background(Color.White)
                            .border(1.5.dp, CandyColors.divider, RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = message.text,
                                color = CandyColors.textPrimary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            val verdict = message.verdict
                            if (verdict != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DecisionVerdictCard(
                                    verdict = verdict,
                                    onViewItemClick = onViewItemClick
                                )
                            }
                        }
                    }
                }
            }
        }
        MessageSender.SYSTEM -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEDE6F8))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message.text,
                        color = CandyColors.primaryInk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Thẻ nhận định của Chanko theo chuẩn trung tính (Neutral BUY):
 * Không pháo hoa, không màu xanh lá chiến thắng, dùng mascot tĩnh Neutral.
 */
@Composable
fun DecisionVerdictCard(
    verdict: DecisionVerdict,
    onViewItemClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (verdictLabel, subtitle) = when (verdict) {
        DecisionVerdict.BUY -> Pair(
            "BUY · Có thể cân nhắc mua",
            "Món đồ này có thể cân nhắc nếu bạn thực sự cần thiết."
        )
        DecisionVerdict.WAIT -> Pair(
            "WAIT · Có thể chờ thêm",
            "Hãy để hết thời gian hạ nhiệt để đánh giá khách quan hơn."
        )
        DecisionVerdict.SKIP -> Pair(
            "SKIP · Có thể bỏ qua",
            "Món đồ chưa thực sự cần thiết lúc này, nên giữ lại ngân sách."
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFAF7FF))
            .border(1.5.dp, CandyColors.divider, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nhận định của Chanko",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CandyColors.mutedInk
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = verdictLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF241B35)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = CandyColors.textSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mascot tĩnh Neutral (72dp)
                Image(
                    painter = painterResource(id = R.drawable.chanko_neutral),
                    contentDescription = "Chanko Neutral",
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Cơ sở: Hãy kiểm tra lại nhu cầu thực tế và ngân sách khả dụng trước khi quyết định.",
                fontSize = 12.sp,
                color = CandyColors.mutedInk,
                lineHeight = 16.sp
            )

            if (onViewItemClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onViewItemClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.brandPurple
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Xem món đồ",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
