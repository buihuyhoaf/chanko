package com.hoabh.chanko.presentation.screens.chat.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * Hộp thoại Đổi tên cuộc trò chuyện (CHAT-UX-REV-01 Rename Dialog)
 */
@Composable
fun RenameThreadDialog(
    initialTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Đổi tên cuộc trò chuyện",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CandyColors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Nhập tên mới cho cuộc trò chuyện này:",
                    fontSize = 14.sp,
                    color = CandyColors.mutedInk
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên cuộc trò chuyện") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CandyColors.brandPurple,
                        cursorColor = CandyColors.brandPurple
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = title.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirm(trimmed)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = CandyColors.textPrimary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Hộp thoại xác nhận Lưu trữ cuộc trò chuyện
 */
@Composable
fun ArchiveConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lưu trữ cuộc trò chuyện?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CandyColors.textPrimary
            )
        },
        text = {
            Text(
                text = "Bạn vẫn có thể xem lại trong bộ lọc Đã lưu trữ. Việc lưu trữ cuộc trò chuyện không làm ảnh hưởng đến trạng thái của món đồ.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = CandyColors.mutedInk
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu trữ", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Giữ lại", color = CandyColors.textPrimary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Hộp thoại xác nhận Xóa cuộc trò chuyện
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xóa cuộc trò chuyện này?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CandyColors.textPrimary
            )
        },
        text = {
            Text(
                text = "Lịch sử trò chuyện này sẽ bị xóa vĩnh viễn. Toàn bộ lịch sử quyết định chính thức, đánh giá của món đồ và lịch sử mua sắm vẫn được giữ nguyên an toàn.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = CandyColors.mutedInk
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Xóa chat", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Giữ lại", color = CandyColors.textPrimary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * BottomSheet Liên kết món đồ cùng thread (Same-thread item bind)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SameThreadItemPickerSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    onConfirmBind: (itemId: String, itemName: String, priceVnd: Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Triple<String, String, Long>?>(null) }
    var isConfirming by remember { mutableStateOf(false) }

    val sampleItems = remember {
        listOf(
            Triple("item_phone_01", "Điện thoại Flagship", 18_000_000L),
            Triple("item_jacket_01", "Áo khoác mùa đông", 1_200_000L),
            Triple("item_headphone_01", "Tai nghe chống ồn", 3_500_000L),
            Triple("item_keyboard_01", "Bàn phím cơ Custom", 2_500_000L),
            Triple("item_watch_01", "Đồng hồ thông minh", 5_000_000L)
        )
    }

    val filteredItems = sampleItems.filter {
        it.second.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (!isConfirming) {
                Text(
                    text = "Liên kết với món đồ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Text(
                    text = "Toàn bộ tin nhắn hiện tại sẽ được giữ nguyên trong cùng cuộc trò chuyện.",
                    fontSize = 13.sp,
                    color = CandyColors.mutedInk,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm món đồ...") },
                    leadingIcon = {
                        ChankoIcon(
                            id = ChankoIconId.ACT_07,
                            contentDescription = "Tìm kiếm",
                            size = 20.dp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CandyColors.brandPurple,
                        cursorColor = CandyColors.brandPurple
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.height(240.dp)) {
                    items(filteredItems, key = { it.first }) { item ->
                        val isSelected = selectedItem?.first == item.first
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFF0E9FF) else Color(0xFFFAF8F5))
                                .border(
                                    1.5.dp,
                                    if (isSelected) CandyColors.brandPurple else CandyColors.divider,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedItem = item }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.second,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CandyColors.textPrimary
                                    )
                                    Text(
                                        text = "%,d ₫".format(item.third),
                                        fontSize = 13.sp,
                                        color = CandyColors.mutedInk
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isConfirming = true },
                    enabled = selectedItem != null,
                    colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (selectedItem != null) "Tiếp tục" else "Chọn một món đồ",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // Confirmation screen
                Text(
                    text = "Xác nhận liên kết",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Liên kết cuộc trò chuyện này với \"${selectedItem?.second}\"?\n\nToàn bộ tin nhắn trước đây sẽ được giữ nguyên trong cuộc trò chuyện. Tin nhắn tự do cũ không trở thành lịch sử quyết định chính thức.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { isConfirming = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Quay lại", color = CandyColors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val item = selectedItem
                            if (item != null) {
                                onConfirmBind(item.first, item.second, item.third)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Liên kết món đồ", color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
