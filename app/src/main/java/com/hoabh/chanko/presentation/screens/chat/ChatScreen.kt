package com.hoabh.chanko.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.presentation.screens.chat.components.ChatComposerBar
import com.hoabh.chanko.presentation.screens.chat.components.ChatMascotHeader
import com.hoabh.chanko.presentation.screens.chat.components.ChatMessageBubble
import com.hoabh.chanko.ui.theme.CandyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CandyColors.background)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = CandyColors.textPrimary
                    )
                }

                Text(
                    text = uiState.thread?.title ?: "Chat with Chanko",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { viewModel.openDeleteDialog() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa hội thoại",
                        tint = CandyColors.mutedInk
                    )
                }
            }
        },
        bottomBar = {
            ChatComposerBar(
                text = uiState.inputText,
                onTextChanged = viewModel::onTextChanged,
                onSendClicked = viewModel::sendMessage,
                onPickItemClicked = viewModel::openItemPicker,
                isEnabled = !uiState.isSending,
                isItemBound = uiState.thread?.mode == ThreadMode.ITEM
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CandyColors.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ChatMascotHeader(
                mode = uiState.thread?.mode ?: ThreadMode.FREE,
                linkedItemName = uiState.thread?.linkedItemName,
                emotion = uiState.emotion
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatMessageBubble(message = msg)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (uiState.showItemPicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeItemPicker() },
            sheetState = bottomSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Chọn món đồ để Chanko thẩm định",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Mẫu danh sách món đồ (Item Picker S05)
                val sampleItems = listOf(
                    Triple("item_phone_01", "Điện thoại Flagship", 18_000_000L),
                    Triple("item_jacket_01", "Áo khoác mùa đông", 1_200_000L),
                    Triple("item_headphone_01", "Tai nghe chống ồn", 3_500_000L)
                )

                sampleItems.forEach { (id, name, price) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CandyColors.background)
                            .border(1.dp, CandyColors.divider, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.bindItem(id, name, price)
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CandyColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${price}đ",
                                    fontSize = 12.sp,
                                    color = CandyColors.brandPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CandyColors.primarySoft)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Thẩm định",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CandyColors.brandPurple
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = {
                Text(
                    text = "Xóa đoạn hội thoại?",
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Đoạn chat này sẽ bị xóa khỏi lịch sử. Lưu ý: Các quyết định tài chính chính thức gắn với món đồ (nếu có) vẫn được bảo toàn nguyên vẹn.",
                    color = CandyColors.mutedInk,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete(onDeleted = onNavigateBack) },
                    colors = ButtonDefaults.buttonColors(containerColor = CandyColors.error)
                ) {
                    Text("Xóa", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteDialog() }) {
                    Text("Hủy", color = CandyColors.textPrimary)
                }
            }
        )
    }
}
