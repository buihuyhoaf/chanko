package com.hoabh.chanko.presentation.features.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import com.hoabh.chanko.ui.theme.ChatTokens

/**
 * ChatComposerBar v2.2:
 * - Tối thiểu 56dp, co giãn tối đa 6 dòng.
 * - Tích hợp imePadding và navigationBarsPadding.
 * - Nút gửi có touch target >= 48dp.
 * - Hiển thị banner giải thích khi thread ở trạng thái chỉ đọc (ReadOnlyReason).
 */
@Composable
fun ChatComposerBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    isEnabled: Boolean = true,
    readOnlyReason: ReadOnlyReason = ReadOnlyReason.NONE,
    isStreaming: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isReadOnly = readOnlyReason != ReadOnlyReason.NONE
    val canSend = isEnabled && !isReadOnly && !isStreaming && text.isNotBlank()

    Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Read-only indicator banner
            if (isReadOnly) {
                val reasonText = when (readOnlyReason) {
                    ReadOnlyReason.ARCHIVED -> "Cuộc trò chuyện này đã được lưu trữ (chỉ đọc)"
                    ReadOnlyReason.PRIMARY_DELETED -> "Món đồ chính đã bị xóa khỏi hệ thống (chỉ đọc)"
                    ReadOnlyReason.OFFLINE -> "Đang ngoại tuyến, chỉ có thể đọc lịch sử"
                    ReadOnlyReason.NONE -> ""
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChatTokens.composerInputBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reasonText,
                        fontSize = 12.sp,
                        color = ChatTokens.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Input box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isReadOnly || !isEnabled) ChatTokens.disabledBg else ChatTokens.coolingActiveBg)
                        .border(
                            1.dp,
                            if (isReadOnly || !isEnabled) ChatTokens.disabledBorder else ChatTokens.refBorder,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = if (isReadOnly) "Đoạn chat ở chế độ chỉ đọc…" else "Nhập tin nhắn cho Chanko…",
                            fontSize = 15.sp,
                            color = ChatTokens.secondary
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        enabled = isEnabled && !isReadOnly && !isStreaming,
                        maxLines = 6,
                        textStyle = TextStyle(
                            fontFamily = ChatTokens.chatNunitoFamily,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = ChatTokens.body
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send button with >= 48dp touch target
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (canSend) ChatTokens.primaryTextAndCTA else ChatTokens.sendDisabled)
                        .semantics {
                            role = Role.Button
                            contentDescription = if (canSend) "Gửi tin nhắn" else "Nút gửi đang vô hiệu hóa"
                        }
                        .clickable(enabled = canSend, onClick = onSendClicked),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (canSend) R.drawable.chat_send_selected else R.drawable.chat_send_disabled
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
