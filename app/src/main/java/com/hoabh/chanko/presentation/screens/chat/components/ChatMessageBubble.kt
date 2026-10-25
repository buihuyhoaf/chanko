package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.DecisionVerdict
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.ui.theme.CandyColors

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    when (message.sender) {
        MessageSender.USER -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(0.2f))
                Box(
                    modifier = Modifier
                        .weight(0.8f, fill = false)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                        .background(CandyColors.brandPurple)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        MessageSender.CHANKO -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.85f, fill = false)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(Color.White)
                        .border(1.5.dp, CandyColors.divider, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = message.text,
                            color = CandyColors.textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        val verdict = message.verdict
                        if (verdict != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            DecisionVerdictCard(verdict = verdict)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(0.15f))
            }
        }
        MessageSender.SYSTEM -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEDE6F8))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
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

@Composable
fun DecisionVerdictCard(
    verdict: DecisionVerdict,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, text, title) = when (verdict) {
        DecisionVerdict.BUY -> Quad(
            CandyColors.readySurface,
            CandyColors.readyAction,
            "Món đồ này hợp lý với ngân sách hiện tại của bạn.",
            "QUYẾT ĐỊNH: NÊN MUA (BUY)"
        )
        DecisionVerdict.WAIT -> Quad(
            CandyColors.calendarSurface,
            CandyColors.brandPurple,
            "Cần thêm thời gian cân nhắc để tránh mua sắm bốc đồng.",
            "QUYẾT ĐỊNH: NÊN CHỜ (WAIT)"
        )
        DecisionVerdict.SKIP -> Quad(
            CandyColors.speechSurface,
            CandyColors.error,
            "Hãy chuyển số tiền này vào Két tiết kiệm để hạ nhiệt!",
            "QUYẾT ĐỊNH: BỎ QUA (SKIP)"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = borderColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = CandyColors.textPrimary
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
