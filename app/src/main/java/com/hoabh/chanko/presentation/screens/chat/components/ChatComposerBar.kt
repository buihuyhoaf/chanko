package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun ChatComposerBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onPickItemClicked: () -> Unit,
    isEnabled: Boolean = true,
    isItemBound: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isItemBound) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CandyColors.backgroundLavender)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Gắn món đồ",
                        onClick = onPickItemClicked
                    ),
                contentAlignment = Alignment.Center
            ) {
                ChankoIcon(
                    id = ChankoIconId.ACT_01,
                    contentDescription = "Gắn món đồ",
                    size = 20.dp
                )
            }
            Spacer(modifier = Modifier.width(CandySpace.sm))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(CandyColors.background)
                .border(1.dp, CandyColors.divider, RoundedCornerShape(22.dp))
                .padding(horizontal = CandySpace.lg),
            contentAlignment = Alignment.CenterStart
        ) {
            if (text.isEmpty()) {
                Text(
                    text = if (isItemBound) "Hỏi Chanko về món này..." else "Tâm sự về chi tiêu...",
                    color = CandyColors.textSecondary,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                enabled = isEnabled,
                textStyle = TextStyle(
                    color = CandyColors.textPrimary,
                    fontSize = 14.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.width(CandySpace.sm))

        val isSendActive = isEnabled && text.isNotBlank()
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSendActive) CandyColors.brandPurple else CandyColors.divider)
                .clickable(
                    enabled = isSendActive,
                    role = Role.Button,
                    onClickLabel = "Gửi tin nhắn",
                    onClick = onSendClicked
                ),
            contentAlignment = Alignment.Center
        ) {
            ChankoIcon(
                id = ChankoIconId.SYS_02,
                contentDescription = "Gửi",
                enabled = isSendActive,
                size = 18.dp
            )
        }
    }
}
