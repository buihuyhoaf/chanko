package com.hoabh.chanko.presentation.features.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.CoolingOffInfo
import com.hoabh.chanko.domain.chat.model.CoolingOffStatus
import com.hoabh.chanko.domain.chat.util.MonotonicClockCalculator
import com.hoabh.chanko.ui.theme.ChatTokens

/**
 * CoolingOffIndicator: Hiển thị thời gian hạ nhiệt bắt buộc (Mandatory Cooling-off).
 * Đồng bộ với hợp đồng CONTRACT-SYNC.md.
 */
@Composable
fun CoolingOffIndicator(
    coolingOffInfo: CoolingOffInfo?,
    modifier: Modifier = Modifier
) {
    if (coolingOffInfo == null) return

    val statusText = when (coolingOffInfo.status) {
        CoolingOffStatus.ELAPSED -> "Sẵn sàng xem xét"
        CoolingOffStatus.ACTIVE -> MonotonicClockCalculator.formatRemaining(coolingOffInfo.remainingMillis)
        CoolingOffStatus.UNKNOWN -> "Chưa có dữ liệu hạ nhiệt"
    }

    val progressColor = when (coolingOffInfo.status) {
        CoolingOffStatus.ELAPSED -> ChatTokens.coolingElapsedText
        CoolingOffStatus.ACTIVE -> ChatTokens.primary
        CoolingOffStatus.UNKNOWN -> ChatTokens.indicatorUnknown
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ChatTokens.grayIconBg)
            .padding(12.dp)
            .semantics {
                contentDescription = "Thời gian hạ nhiệt bắt buộc: $statusText"
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.chat_cooling_default),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Hạ nhiệt bắt buộc:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ChatTokens.secondary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = statusText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
        }

        if (coolingOffInfo.status == CoolingOffStatus.ACTIVE) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { coolingOffInfo.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = ChatTokens.indicatorTrack
            )
        }
    }
}
