package com.hoabh.chanko.presentation.screens.chat.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.LaunchedEffect
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
import com.hoabh.chanko.presentation.screens.chat.components.ArchivedBanner
import com.hoabh.chanko.presentation.screens.chat.components.ChatComposerBar
import com.hoabh.chanko.presentation.screens.chat.components.ChatErrorBanner
import com.hoabh.chanko.presentation.screens.chat.components.ChatHistorySheet
import com.hoabh.chanko.presentation.screens.chat.components.ChatMascotHeader
import com.hoabh.chanko.presentation.screens.chat.components.ChatMessageBubble
import com.hoabh.chanko.presentation.screens.chat.components.ChatOfflineBanner
import com.hoabh.chanko.presentation.screens.chat.components.ChatThinkingIndicator
import com.hoabh.chanko.presentation.screens.chat.components.ThreadOptionsSheet
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatViewModel
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * S02–S11 — Chat Thread Screen
 * Màn hình chi tiết cuộc trò chuyện (Thread conversation).
 * Hiển thị toàn bộ lịch sử tin nhắn, phản biện của Chanko, thẻ thẩm định Verdict Card,
 * và các tùy chọn chuyển đổi chế độ / lưu trữ / xóa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    threadId: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(threadId) {
        if (threadId.isNotBlank()) {
            viewModel.selectThread(threadId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val activeThreads by viewModel.activeThreads.collectAsState()
    val archivedThreads by viewModel.archivedThreads.collectAsState()

    val itemPickerSheetState = rememberModalBottomSheetState()
    val historySheetState = rememberModalBottomSheetState()
    val optionsSheetState = rememberModalBottomSheetState()

    val isArchived = uiState.thread?.isArchived == true

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

                // Nút tạo cuộc trò chuyện mới (về S01 Empty)
                IconButton(onClick = onNavigateToNewChat) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Cuộc trò chuyện mới",
                        tint = CandyColors.brandPurple
                    )
                }

                // Nút mở Lịch sử trò chuyện (S04)
                IconButton(onClick = { viewModel.openHistorySheet() }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Lịch sử trò chuyện",
                        tint = CandyColors.brandPurple
                    )
                }

                // Nút mở Tùy chọn (S09 - Archive / Delete)
                IconButton(onClick = { viewModel.openOptionsSheet() }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn",
                        tint = CandyColors.mutedInk
                    )
                }
            }
        },
        bottomBar = {
            if (!isArchived) {
                ChatComposerBar(
                    text = uiState.inputText,
                    onTextChanged = viewModel::onTextChanged,
                    onSendClicked = { viewModel.sendMessage() },
                    onPickItemClicked = viewModel::openItemPicker,
                    isEnabled = !uiState.isSending,
                    isItemBound = uiState.thread?.mode == ThreadMode.ITEM
                )
            } else {
                ArchivedBanner(
                    onRestoreClicked = { viewModel.restoreCurrentThread() },
                    modifier = Modifier.padding(12.dp)
                )
            }
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

            // Header Mascot Presenter (Đổi theo 8 biểu cảm chính thức)
            ChatMascotHeader(
                mode = uiState.thread?.mode ?: ThreadMode.FREE,
                linkedItemName = uiState.thread?.linkedItemName,
                emotion = uiState.emotion
            )

            // S06 Offline Banner
            if (uiState.isOffline) {
                Spacer(modifier = Modifier.height(8.dp))
                ChatOfflineBanner()
            }

            // S08 Error Banner
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ChatErrorBanner(
                    errorMessage = uiState.error ?: "Đã có lỗi xảy ra",
                    onRetryClicked = { viewModel.retryLastAction() }
                )
            }

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

                // S07 Thinking / Loading Status khi bot đang phản hồi
                if (uiState.isSending) {
                    item(key = "thinking_indicator") {
                        ChatThinkingIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    // S05 — Item Picker BottomSheet
    if (uiState.showItemPicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeItemPicker() },
            sheetState = itemPickerSheetState,
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

                val sampleItems = listOf(
                    Triple("item_phone_01", "Điện thoại Flagship", 18_000_000L),
                    Triple("item_jacket_01", "Áo khoác mùa đông", 1_200_000L),
                    Triple("item_headphone_01", "Tai nghe chống ồn", 3_500_000L)
                )

                sampleItems.forEach { (id, name, price) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CandyColors.background)
                            .border(1.dp, CandyColors.divider, RoundedCornerShape(14.dp))
                            .clickable { viewModel.bindItem(id, name, price) }
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                color = CandyColors.textPrimary
                            )
                            Text(
                                text = "%,d ₫".format(price),
                                fontSize = 12.sp,
                                color = CandyColors.mutedInk
                            )
                        }
                    }
                }
            }
        }
    }

    // S04 — Chat History BottomSheet
    if (uiState.showHistorySheet) {
        ChatHistorySheet(
            activeThreads = activeThreads,
            archivedThreads = archivedThreads,
            currentThreadId = uiState.thread?.id,
            onSelectThread = { selectedThreadId ->
                viewModel.closeHistorySheet()
                viewModel.selectThread(selectedThreadId)
            },
            onDismiss = { viewModel.closeHistorySheet() },
            sheetState = historySheetState
        )
    }

    // S09 — Thread Options BottomSheet
    if (uiState.showOptionsSheet) {
        ThreadOptionsSheet(
            isArchived = isArchived,
            onArchiveClicked = { viewModel.archiveCurrentThread() },
            onRestoreClicked = { viewModel.restoreCurrentThread() },
            onDeleteClicked = { viewModel.openDeleteDialog() },
            onDismiss = { viewModel.closeOptionsSheet() },
            sheetState = optionsSheetState
        )
    }

    // S10 — Confirm Delete Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = {
                Text(
                    text = "Xoá cuộc trò chuyện?",
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Lịch sử trò chuyện này sẽ bị xoá vĩnh viễn. Quyết định thẩm định (nếu có) trên món đồ của bạn vẫn được giữ nguyên an toàn.",
                    fontSize = 14.sp,
                    color = CandyColors.mutedInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmDelete {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.error
                    )
                ) {
                    Text("Xoá vĩnh viễn", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteDialog() }) {
                    Text("Hủy", color = CandyColors.textPrimary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
