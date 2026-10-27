package com.hoabh.chanko.presentation.screens.chat.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.presentation.screens.chat.components.ArchivedBanner
import com.hoabh.chanko.presentation.screens.chat.components.ChatComposerBar
import com.hoabh.chanko.presentation.screens.chat.components.ChatErrorBanner
import com.hoabh.chanko.presentation.screens.chat.components.ChatMessageBubble
import com.hoabh.chanko.presentation.screens.chat.components.ChatOfflineBanner
import com.hoabh.chanko.presentation.screens.chat.components.ChankoTypingBubble
import com.hoabh.chanko.presentation.screens.chat.components.DeleteConfirmationDialog
import com.hoabh.chanko.presentation.screens.chat.components.RenameThreadDialog
import com.hoabh.chanko.presentation.screens.chat.components.SameThreadItemPickerSheet
import com.hoabh.chanko.presentation.screens.chat.components.ThreadOptionsSheet
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatViewModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Màn hình CD — Chat Detail (Chi tiết cuộc trò chuyện)
 * Nơi DUY NHẤT trong tính năng Chat with Chanko có ô gõ tin nhắn (Composer bar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    threadId: String?,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToItemDetail: (itemId: String) -> Unit,
    modifier: Modifier = Modifier,
    contextMode: String? = null,
    itemId: String? = null,
    itemName: String? = null,
    itemPrice: Long? = null
) {
    LaunchedEffect(threadId, contextMode, itemId) {
        viewModel.initThread(
            threadId = threadId,
            contextMode = contextMode,
            itemId = itemId,
            itemName = itemName,
            itemPrice = itemPrice
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val optionsSheetState = rememberModalBottomSheetState()
    val itemPickerSheetState = rememberModalBottomSheetState()

    // Tự động cuộn xuống tin nhắn mới nhất hoặc khi Chanko đang gõ
    LaunchedEffect(uiState.messages.size, uiState.isChankoTyping) {
        val totalCount = uiState.messages.size + if (uiState.isChankoTyping) 1 else 0
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    val isArchived = uiState.thread?.isArchived == true
    val isItemMode = uiState.thread?.mode == ThreadMode.ITEM || uiState.draftMode == "ITEM"
    val linkedItemId = uiState.thread?.linkedItemId ?: uiState.draftItemId
    val linkedItemName = uiState.thread?.linkedItemName ?: uiState.draftItemName

    Scaffold(
        topBar = {
            ChatDetailTopBar(
                title = when {
                    !linkedItemName.isNullOrBlank() -> linkedItemName
                    uiState.thread != null -> uiState.thread?.title ?: "Đoạn chat"
                    else -> "Trò chuyện tự do"
                },
                subtitle = when {
                    !linkedItemId.isNullOrBlank() -> "Nhấn để xem chi tiết món đồ"
                    isItemMode -> "Đang thẩm định món đồ"
                    else -> "Chanko đang lắng nghe"
                },
                isItemMode = isItemMode,
                canOpenItem = !linkedItemId.isNullOrBlank(),
                onOpenItem = {
                    if (!linkedItemId.isNullOrBlank()) {
                        onNavigateToItemDetail(linkedItemId)
                    }
                },
                onNavigateBack = onNavigateBack,
                onMoreClick = {
                    if (uiState.thread != null) {
                        viewModel.openOptionsSheet()
                    }
                },
                hasThread = uiState.thread != null
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = Color.White,
                shadowElevation = CandyElevation.low
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Quick Replies row (chỉ hiện khi chưa archive và có gợi ý)
                    if (!isArchived && uiState.quickReplies.isNotEmpty()) {
                        QuickRepliesRow(
                            quickReplies = uiState.quickReplies,
                            onReplyClick = viewModel::fillComposer
                        )
                    }

                    // Composer Bar (hoặc ArchivedBanner)
                    if (!isArchived) {
                        ChatComposerBar(
                            text = uiState.inputText,
                            onTextChanged = viewModel::onTextChanged,
                            onSendClicked = { viewModel.sendMessage() },
                            onPickItemClicked = viewModel::openItemPicker,
                            isEnabled = !uiState.isSending,
                            isItemBound = isItemMode
                        )
                    } else {
                        ArchivedBanner(
                            onRestoreClicked = { viewModel.restoreCurrentThread() },
                            modifier = Modifier.padding(CandySpace.md)
                        )
                    }
                }
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
            // Provenance Bar (khi thread đã liên kết món đồ)
            if (!linkedItemName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ProvenanceBar(
                    itemName = linkedItemName,
                    onClick = {
                        if (!linkedItemId.isNullOrBlank()) {
                            onNavigateToItemDetail(linkedItemId)
                        }
                    }
                )
            }

            // Offline Banner
            if (uiState.isOffline) {
                Spacer(modifier = Modifier.height(8.dp))
                ChatOfflineBanner()
            }

            // Error Banner
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ChatErrorBanner(
                    errorMessage = uiState.error ?: "Đã có lỗi xảy ra",
                    onRetryClicked = { viewModel.retryLastAction() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeline tin nhắn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        onViewItemClick = {
                            if (!linkedItemId.isNullOrBlank()) {
                                onNavigateToItemDetail(linkedItemId)
                            }
                        }
                    )
                }

                if (uiState.isChankoTyping) {
                    item(key = "chanko_typing_bubble") {
                        ChankoTypingBubble()
                    }
                }
            }
        }
    }

    // Sheet: Tùy chọn cuộc trò chuyện
    if (uiState.showOptionsSheet && uiState.thread != null) {
        ThreadOptionsSheet(
            isArchived = isArchived,
            onArchiveClicked = { viewModel.archiveCurrentThread() },
            onRestoreClicked = { viewModel.restoreCurrentThread() },
            onDeleteClicked = { viewModel.openDeleteDialog() },
            onRenameClicked = { viewModel.openRenameDialog() },
            onViewItemClicked = if (!linkedItemId.isNullOrBlank()) {
                {
                    viewModel.closeOptionsSheet()
                    onNavigateToItemDetail(linkedItemId)
                }
            } else null,
            onDismiss = { viewModel.closeOptionsSheet() },
            sheetState = optionsSheetState
        )
    }

    // Dialog: Đổi tên cuộc trò chuyện
    if (uiState.showRenameDialog && uiState.thread != null) {
        RenameThreadDialog(
            initialTitle = uiState.thread?.title ?: "",
            onConfirm = { newTitle -> viewModel.renameThread(newTitle) },
            onDismiss = { viewModel.closeRenameDialog() }
        )
    }

    // Dialog: Xóa cuộc trò chuyện
    if (uiState.showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.confirmDelete(onDeleted = onNavigateBack)
            },
            onDismiss = { viewModel.closeDeleteDialog() }
        )
    }

    // Sheet: Chọn món đồ để liên kết cùng thread (Same-thread item bind)
    if (uiState.showItemPicker) {
        SameThreadItemPickerSheet(
            onDismiss = { viewModel.closeItemPicker() },
            sheetState = itemPickerSheetState,
            onConfirmBind = { itemId, itemName, priceVnd ->
                viewModel.bindItem(itemId, itemName, priceVnd)
            }
        )
    }
}

@Composable
private fun ChatDetailTopBar(
    title: String,
    subtitle: String,
    isItemMode: Boolean,
    canOpenItem: Boolean,
    onOpenItem: () -> Unit,
    onNavigateBack: () -> Unit,
    onMoreClick: () -> Unit,
    hasThread: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = CandyColors.textPrimary
                )
            }

            // Thumbnail / Avatar tròn 40dp
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CandyColors.backgroundLavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isItemMode) Icons.Rounded.ShoppingBag else Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = CandyColors.brandPurple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = canOpenItem, onClick = onOpenItem)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (canOpenItem) CandyColors.brandPurple else CandyColors.mutedInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (hasThread) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn",
                        tint = CandyColors.mutedInk
                    )
                }
            }
        }
    }
}

@Composable
private fun ProvenanceBar(
    itemName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3EDFD))
            .border(1.dp, CandyColors.brandPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Link,
            contentDescription = null,
            tint = CandyColors.brandPurple,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Đã liên kết với $itemName",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = CandyColors.brandPurple,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Hàng chip Quick Replies: Chỉ điền vào ô composer (FillComposer), không tự động gửi
 */
@Composable
private fun QuickRepliesRow(
    quickReplies: List<String>,
    onReplyClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = CandySpace.lg, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickReplies) { reply ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyColors.background)
                    .border(1.dp, CandyColors.divider, RoundedCornerShape(16.dp))
                    .clickable { onReplyClick(reply) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = reply,
                    fontSize = 12.sp,
                    color = CandyColors.textPrimary
                )
            }
        }
    }
}
