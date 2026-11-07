package com.hoabh.chanko.presentation.features.chat.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.CoolingOffStatus
import com.hoabh.chanko.presentation.features.chat.components.ArchiveConfirmationDialog
import com.hoabh.chanko.presentation.features.chat.components.DeleteConfirmationDialog
import com.hoabh.chanko.presentation.features.chat.components.RenameThreadDialog
import com.hoabh.chanko.presentation.features.chat.components.ThreadOptionsSheet
import com.hoabh.chanko.presentation.features.chat.components.formatVndPrice
import com.hoabh.chanko.presentation.features.chat.components.getProductThumbnailResId
import com.hoabh.chanko.ui.theme.ChatTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onNavigateToDetail: (threadId: String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToItemDetail: (productId: String) -> Unit,
    modifier: Modifier = Modifier,
    contentBottomPadding: Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val effectiveBottomPadding = maxOf(contentBottomPadding, navBottom)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ChatTokens.screenBg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(bottom = effectiveBottomPadding + 88.dp)
            ) {
                // 1. Header Hero Banner
                item(key = "hero_banner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ChatTokens.coolingActiveBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Trò chuyện cùng Chanko",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ChatTokens.body
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Thẩm định chi tiêu khách quan, hạ nhiệt mua sắm và so sánh sản phẩm thông minh.",
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = ChatTokens.secondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.chanko_chat_hero),
                                contentDescription = "Chanko Hero",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(108.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }

                // 2. Search Bar (min 52dp)
                item(key = "search_bar") {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Tìm cuộc trò chuyện…", fontSize = 14.sp, color = ChatTokens.secondary) },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.chat_search_default),
                                contentDescription = "Tìm kiếm",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.chat_close_default),
                                        contentDescription = "Xóa",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ChatTokens.primaryTextAndCTA,
                            unfocusedBorderColor = ChatTokens.divider
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // 3. Tab Chuyển đổi: Đang hoạt động / Kho lưu trữ (Bỏ filter chips cũ CL-GAP-02)
                item(key = "tabs_mode") {
                    TabRow(
                        selectedTabIndex = if (uiState.showArchived) 1 else 0,
                        containerColor = Color.Transparent,
                        contentColor = ChatTokens.primaryTextAndCTA,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (uiState.showArchived) 1 else 0]),
                                color = ChatTokens.primaryTextAndCTA
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Tab(
                            selected = !uiState.showArchived,
                            onClick = { viewModel.onToggleArchived(false) },
                            text = {
                                Text(
                                    text = "Đang hoạt động (${uiState.activeThreads.size})",
                                    fontWeight = if (!uiState.showArchived) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                        Tab(
                            selected = uiState.showArchived,
                            onClick = { viewModel.onToggleArchived(true) },
                            text = {
                                Text(
                                    text = "Kho lưu trữ (${uiState.archivedThreads.size})",
                                    fontWeight = if (uiState.showArchived) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                // 4. Danh sách Threads (CL-GAP-01, CL-GAP-03)
                val threads = uiState.filteredThreads
                if (threads.isEmpty()) {
                    item(key = "empty_state") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty())
                                    "Không tìm thấy đoạn chat phù hợp"
                                else if (uiState.showArchived)
                                    "Kho lưu trữ đang trống"
                                else
                                    "Chưa có cuộc trò chuyện nào",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChatTokens.body
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bắt đầu thẩm định một món đồ cùng Chanko nhé!",
                                fontSize = 13.sp,
                                color = ChatTokens.secondary
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Text("Xóa từ khóa tìm kiếm", color = ChatTokens.primaryTextAndCTA)
                                }
                            }
                        }
                    }
                } else {
                    items(threads, key = { it.id }) { thread ->
                        ChatThreadItem(
                            thread = thread,
                            onClick = { onNavigateToDetail(thread.id) },
                            onOptionsClick = { viewModel.onOptionsClicked(thread) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 5. Nút "+ Đoạn chat mới" cố định góc phải (touch target >= 48dp, height min 56dp)
            ExtendedFloatingActionButton(
                onClick = onNavigateToNewChat,
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.chat_new_chat_default),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                },
                text = {
                    Text(
                        text = "Đoạn chat mới",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                containerColor = ChatTokens.primaryTextAndCTA,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = effectiveBottomPadding + 16.dp)
                    .heightIn(min = 56.dp)
            )
        }
    }

    // Modal BottomSheet: Options
    val threadForOptions = uiState.threadForOptions
    if (threadForOptions != null) {
        androidx.compose.material3.ExperimentalMaterial3Api::class
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        ThreadOptionsSheet(
            isArchived = threadForOptions.isArchived,
            onArchiveClicked = { viewModel.onRequestArchive(threadForOptions) },
            onRestoreClicked = { viewModel.onRestoreThread(threadForOptions) },
            onDeleteClicked = { viewModel.onRequestDelete(threadForOptions) },
            onRenameClicked = { viewModel.onRequestRename(threadForOptions) },
            onViewItemClicked = { onNavigateToItemDetail(threadForOptions.primaryProduct.productId) },
            onDismiss = { viewModel.onDismissOptions() },
            sheetState = sheetState
        )
    }

    // Rename Dialog
    val threadForRename = uiState.threadForRename
    if (threadForRename != null) {
        RenameThreadDialog(
            initialTitle = threadForRename.title,
            onConfirm = { viewModel.onConfirmRename(it) },
            onDismiss = { viewModel.onDismissRename() }
        )
    }

    // Archive Dialog
    val threadForArchive = uiState.threadForArchive
    if (threadForArchive != null) {
        ArchiveConfirmationDialog(
            onConfirm = { viewModel.onConfirmArchive() },
            onDismiss = { viewModel.onDismissArchive() }
        )
    }

    // Delete Dialog
    val threadForDelete = uiState.threadForDelete
    if (threadForDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.onConfirmDelete() },
            onDismiss = { viewModel.onDismissDelete() }
        )
    }
}

/**
 * ChatThreadItem: Thẻ hiển thị một cuộc trò chuyện trên CL (CL-GAP-01, CL-GAP-03).
 */
@Composable
fun ChatThreadItem(
    thread: ChatThread,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(thread.updatedAtEpochMillis))
    val cooling = thread.primaryProduct.coolingOff
    val refCount = thread.referenceProducts.size

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatTokens.listCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Cuộc trò chuyện: ${thread.title}, ${thread.primaryProduct.name}, ${thread.previewText ?: ""}"
            }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Thumbnail 56dp + Reference Badge +N (CL-GAP-01)
            Box(modifier = Modifier.size(56.dp)) {
                val thumbRes = thread.primaryProduct.thumbnailResId
                    ?: getProductThumbnailResId(thread.primaryProduct.thumbnailUrl)
                Image(
                    painter = painterResource(id = thumbRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ChatTokens.grayIconBg)
                )

                // Badge +N (CL-GAP-01)
                if (refCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChatTokens.primaryTextAndCTA)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                            .semantics { contentDescription = "Kèm $refCount sản phẩm so sánh" }
                    ) {
                        Text(
                            text = "+$refCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = thread.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ChatTokens.body,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeString,
                        fontSize = 12.sp,
                        color = ChatTokens.secondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = thread.previewText ?: "Chưa có tin nhắn",
                    fontSize = 13.sp,
                    color = ChatTokens.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price and Cooling-off status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatVndPrice(thread.primaryProduct.price),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChatTokens.primaryTextAndCTA
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (thread.primaryProduct.isPrimaryDeleted) {
                        Text(
                            text = "Món chính đã xóa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChatTokens.alertRed
                        )
                    } else if (cooling != null) {
                        val coolingLabel = when (cooling.status) {
                            CoolingOffStatus.ELAPSED -> "Sẵn sàng xem xét"
                            CoolingOffStatus.ACTIVE -> "Đang hạ nhiệt"
                            CoolingOffStatus.UNKNOWN -> "Chưa rõ hạ nhiệt"
                        }
                        val coolingColor = when (cooling.status) {
                            CoolingOffStatus.ELAPSED -> ChatTokens.coolingElapsedText
                            CoolingOffStatus.ACTIVE -> ChatTokens.primaryTextAndCTA
                            CoolingOffStatus.UNKNOWN -> ChatTokens.secondary
                        }
                        Text(
                            text = "• $coolingLabel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = coolingColor
                        )
                    }
                }
            }

            // More Options Menu button (>= 48dp target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOptionsClick)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Tùy chọn cho đoạn chat ${thread.title}"
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
}
