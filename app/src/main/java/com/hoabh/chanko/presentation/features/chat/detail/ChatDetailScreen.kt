package com.hoabh.chanko.presentation.features.chat.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.MessageStatus
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.presentation.features.chat.components.ArchiveConfirmationDialog
import com.hoabh.chanko.presentation.features.chat.components.ChatComposerBar
import com.hoabh.chanko.presentation.features.chat.components.ChatMessageBubble
import com.hoabh.chanko.presentation.features.chat.components.DeleteConfirmationDialog
import com.hoabh.chanko.presentation.features.chat.components.ProductContextCard
import com.hoabh.chanko.presentation.features.chat.components.ReferenceChipsRow
import com.hoabh.chanko.presentation.features.chat.components.RenameThreadDialog
import com.hoabh.chanko.presentation.features.chat.components.ThreadOptionsSheet
import com.hoabh.chanko.presentation.features.chat.components.formatVndPrice
import com.hoabh.chanko.presentation.features.chat.components.getProductThumbnailResId
import com.hoabh.chanko.ui.theme.ChatTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: ChatViewModel,
    threadId: String?,
    productId: String? = null,
    productName: String? = null,
    productPrice: Long? = null,
    thumbnailUrl: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToItemDetail: (productId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(threadId, productId) {
        viewModel.initThread(
            threadId = threadId,
            productId = productId,
            productName = productName,
            productPrice = productPrice,
            thumbnailUrl = thumbnailUrl
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ChatUiEffect.NavigateToItemDetail -> onNavigateToItemDetail(effect.productId)
                is ChatUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    // Auto-scroll to bottom on new message or stream chunk
    LaunchedEffect(state.messages.size, state.streamingChunk) {
        val totalCount = state.messages.size + if (state.isStreaming && state.streamingChunk.isNotBlank()) 1 else 0
        if (totalCount > 0) {
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ChatTokens.screenBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top App Bar (min 56dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = viewModel::onBackClicked)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Quay lại danh sách"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chat_back_default),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Avatar Chanko
                Image(
                    painter = painterResource(id = R.drawable.chanko_chat_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(ChatTokens.avatarChatDp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.thread?.title ?: state.draftProduct?.name ?: "Chanko Chat",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChatTokens.body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (state.isStreaming) "Chanko đang soạn phản hồi…" else "Trợ lý chi tiêu khách quan",
                        fontSize = 12.sp,
                        color = if (state.isStreaming) ChatTokens.primaryTextAndCTA else ChatTokens.secondary
                    )
                }

                // Options menu button (only for persisted threads)
                if (state.thread != null) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(onClick = viewModel::onOpenOptions)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Tùy chọn cuộc trò chuyện"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chat_more_default),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Product Context Header (CD-GAP-01, CD-GAP-02)
            val currentProduct = state.currentProduct
            if (currentProduct != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    ProductContextCard(
                        product = currentProduct,
                        onClick = { viewModel.onProductCardClicked(currentProduct.productId) }
                    )
                }
            }

            // 3. Reference Chips Row (CD-GAP-01, CD-GAP-04, EDGE-09)
            if (state.thread != null) {
                ReferenceChipsRow(
                    referenceProducts = state.thread!!.referenceProducts,
                    onAddReferenceClick = viewModel::onAddReferenceClicked,
                    onRemoveReferenceClick = viewModel::onRemoveReference,
                    isLocked = state.isStreaming || state.isReadOnly,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Message Timeline (LazyColumn)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        ChatMessageBubble(
                            message = message,
                            onProductClick = viewModel::onProductCardClicked
                        )
                    }

                    // Streaming Partial Assistant Response
                    if (state.isStreaming && state.streamingChunk.isNotBlank()) {
                        item(key = "streaming_bubble") {
                            ChatMessageBubble(
                                message = ChatMessage(
                                    id = "m_streaming",
                                    threadId = state.thread?.id ?: "",
                                    sender = MessageSender.CHANKO,
                                    content = state.streamingChunk,
                                    createdAtEpochMillis = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }

            // 5. Quick Replies Chips (Horizontal scroll, fills composer only)
            if (!state.isReadOnly && !state.isStreaming && state.quickReplies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.quickReplies.forEach { reply ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ChatTokens.coolingActiveBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ChatTokens.coolingActiveBorder),
                            modifier = Modifier
                                .clickable { viewModel.onQuickReplyClicked(reply) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Gợi ý: $reply"
                                }
                        ) {
                            Text(
                                text = reply,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ChatTokens.primaryTextAndCTA,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 6. Composer Bar (min 56dp, max 6 lines, imePadding)
            ChatComposerBar(
                text = state.inputText,
                onTextChanged = viewModel::onInputTextChanged,
                onSendClicked = viewModel::sendMessage,
                isEnabled = !state.isSending,
                readOnlyReason = state.readOnlyReason,
                isStreaming = state.isStreaming
            )
        }
    }

    // Modal BottomSheet: Reference Picker (EDGE-07)
    if (state.showReferencePicker) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissReferencePicker,
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Chọn món đồ để so sánh",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTokens.body
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Chanko sẽ đưa ra nhận xét khách quan cho từng sản phẩm:",
                    fontSize = 13.sp,
                    color = ChatTokens.secondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (state.eligibleReferences.isEmpty()) {
                    Text(
                        text = "Không còn món đồ nào khác để so sánh.",
                        fontSize = 14.sp,
                        color = ChatTokens.secondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.eligibleReferences, key = { it.productId }) { refProduct ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ChatTokens.snapshotCardBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ChatTokens.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSelectReference(refProduct) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    val thumbRes = refProduct.thumbnailResId
                                        ?: getProductThumbnailResId(refProduct.thumbnailUrl)
                                    Image(
                                        painter = painterResource(id = thumbRes),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = refProduct.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ChatTokens.body
                                        )
                                        Text(
                                            text = formatVndPrice(refProduct.price),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = ChatTokens.primaryTextAndCTA
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal BottomSheet: Options
    if (state.showOptionsSheet && state.thread != null) {
        val sheetState = rememberModalBottomSheetState()
        ThreadOptionsSheet(
            isArchived = state.thread!!.isArchived,
            onArchiveClicked = viewModel::onRequestArchive,
            onRestoreClicked = viewModel::onRestoreThread,
            onDeleteClicked = viewModel::onRequestDelete,
            onRenameClicked = viewModel::onRequestRename,
            onViewItemClicked = { state.currentProduct?.productId?.let(onNavigateToItemDetail) },
            onDismiss = viewModel::onDismissOptions,
            sheetState = sheetState
        )
    }

    // Rename Dialog
    if (state.showRenameDialog && state.thread != null) {
        RenameThreadDialog(
            initialTitle = state.thread!!.title,
            onConfirm = viewModel::onConfirmRename,
            onDismiss = viewModel::onDismissRename
        )
    }

    // Archive Dialog
    if (state.showArchiveDialog) {
        ArchiveConfirmationDialog(
            onConfirm = viewModel::onConfirmArchive,
            onDismiss = viewModel::onDismissArchive
        )
    }

    // Delete Dialog
    if (state.showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = viewModel::onConfirmDelete,
            onDismiss = viewModel::onDismissDelete
        )
    }
}
