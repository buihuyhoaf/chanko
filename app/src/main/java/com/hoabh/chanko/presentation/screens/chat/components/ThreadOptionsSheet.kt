package com.hoabh.chanko.presentation.screens.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
 * S09 — Thread Options Sheet:
 * Menu tùy chọn (Archive / Delete) cho phiên hội thoại hiện tại.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadOptionsSheet(
    isArchived: Boolean,
    onArchiveClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Tùy chọn hội thoại",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isArchived) {
                // Tùy chọn: Lưu trữ (Archive)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onArchiveClicked() }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CandyColors.backgroundLavender),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Lưu trữ",
                            tint = CandyColors.brandPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Lưu trữ đoạn chat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CandyColors.textPrimary
                        )
                        Text(
                            text = "Chuyển vào kho lưu trữ (chế độ chỉ đọc)",
                            fontSize = 12.sp,
                            color = CandyColors.mutedInk
                        )
                    }
                }
            } else {
                // Tùy chọn: Khôi phục (Restore)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRestoreClicked() }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CandyColors.readySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Khôi phục",
                            tint = CandyColors.readyAction,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Khôi phục cuộc trò chuyện",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CandyColors.textPrimary
                        )
                        Text(
                            text = "Tiếp tục gửi tin nhắn và thẩm định",
                            fontSize = 12.sp,
                            color = CandyColors.mutedInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tùy chọn: Xóa (Delete)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDeleteClicked() }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CandyColors.speechSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = CandyColors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Xóa đoạn hội thoại",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CandyColors.error
                    )
                    Text(
                        text = "Xóa tin nhắn, giữ nguyên bản ghi quyết định món đồ",
                        fontSize = 12.sp,
                        color = CandyColors.mutedInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * S11 — Archived Banner:
 * Thanh cảnh báo khi thread đang ở trạng thái đã lưu trữ (Read-only) với chú mèo Chanko Sleeping.
 */
@Composable
fun ArchivedBanner(
    onRestoreClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3EDFD))
            .border(1.dp, CandyColors.brandPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.chanko_sleeping),
                contentDescription = "Chanko Sleeping",
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Đoạn chat đã được lưu trữ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.brandPurple
                )
                Text(
                    text = "Chế độ chỉ đọc (Read-only)",
                    fontSize = 11.sp,
                    color = CandyColors.mutedInk
                )
            }

            Button(
                onClick = onRestoreClicked,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Mở lại",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
