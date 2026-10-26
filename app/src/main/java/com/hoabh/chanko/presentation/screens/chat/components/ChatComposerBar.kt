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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.ui.theme.CandyColors

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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isItemBound) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CandyColors.backgroundLavender)
                    .clickable(onClick = onPickItemClicked),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Gắn món đồ",
                    tint = CandyColors.brandPurple
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(CandyColors.background)
                .border(1.dp, CandyColors.divider, RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp),
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

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClicked,
            enabled = isEnabled && text.isNotBlank(),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) CandyColors.brandPurple else CandyColors.divider)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Gửi",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
