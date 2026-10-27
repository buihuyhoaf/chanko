package com.hoabh.chanko.presentation.screens.chat.screen

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ItemContextStatus
import com.hoabh.chanko.domain.chat.model.ThreadFilter
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.presentation.screens.chat.components.ArchiveConfirmationDialog
import com.hoabh.chanko.presentation.screens.chat.components.DeleteConfirmationDialog
import com.hoabh.chanko.presentation.screens.chat.components.RenameThreadDialog
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatListViewModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn hình CL — Chat List (Danh sách cuộc trò chuyện theo chuẩn CHAT-UX-REV-01):
 * - Quản lý hội thoại kiểu Messenger: Header, Subtitle, Hero mascot, Search bar, Filter chips.
 * - Danh sách Thread Cards với thumbnail, tiêu đề, đoạn xem trước, thời gian và tag.
 * - Nút "+ Đoạn chat mới" cố định ở đáy an toàn.
 * - TUYỆT ĐỐI KHÔNG CÓ Ô GÕ TIN NHẮN (COMPOSER) TẠI ĐÂY.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onNavigateToDetail: (threadId: String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToItemDetail: (itemId: String) -> Unit,
    modifier: Modifier = Modifier,
    contentBottomPadding: Dp = 0.dp
) {
    val uiState by viewModel.uiState.collectAsState()
    var isHeaderMenuExpanded by remember { mutableStateOf(false) }
    val effectiveBottomPadding = if (contentBottomPadding > 0.dp) {
        contentBottomPadding
    } else {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Header & More Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.lg, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.showArchived) "Cuộc trò chuyện đã lưu trữ" else "Chat với Chanko",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = CandyColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cùng suy nghĩ kỹ hơn trước mỗi quyết định",
                        fontSize = 13.sp,
                        color = CandyColors.mutedInk
                    )
                }

                Box {
                    IconButton(onClick = { isHeaderMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Tùy chọn danh sách",
                            tint = CandyColors.textPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = isHeaderMenuExpanded,
                        onDismissRequest = { isHeaderMenuExpanded = false }
                    ) {
                        if (!uiState.showArchived) {
                            DropdownMenuItem(
                                text = { Text("Xem mục đã lưu trữ") },
                                onClick = {
                                    isHeaderMenuExpanded = false
                                    viewModel.toggleShowArchived(true)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Xem cuộc trò chuyện đang hoạt động") },
                                onClick = {
                                    isHeaderMenuExpanded = false
                                    viewModel.toggleShowArchived(false)
                                }
                            )
                        }
                    }
                }
            }

            // 2. Danh sách cuộn chứa Hero, Search, Filters và Threads
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = effectiveBottomPadding + 80.dp) // Tránh bị nút FAB che khi cuộn xuống cuối
            ) {
                // Hero Banner
                item(key = "hero_banner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = CandySpace.lg, vertical = 10.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFF0E7FF),
                                        Color(0xFFFCEAF4)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chào bạn!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CandyColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Đang phân vân món đồ nào? Hãy chia sẻ để Chanko giúp bạn hạ nhiệt và đánh giá khách quan nhé!",
                                    fontSize = 13.sp,
                                    color = CandyColors.textSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.chanko_welcome),
                                contentDescription = "Chanko Hero",
                                modifier = Modifier.size(96.dp)
                            )
                        }
                    }
                }

                // Search Bar
                item(key = "search_bar") {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Tìm cuộc trò chuyện…", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Tìm kiếm",
                                tint = CandyColors.mutedInk
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Xóa",
                                        tint = CandyColors.mutedInk
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = CandyColors.brandPurple,
                            unfocusedBorderColor = CandyColors.divider
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = CandySpace.lg, vertical = 6.dp)
                    )
                }

                // Filter Chips (Cuộn ngang)
                item(key = "filter_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CandySpace.lg, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Pair(ThreadFilter.ALL, "Tất cả"),
                            Pair(ThreadFilter.CONSIDERING, "Đang cân nhắc"),
                            Pair(ThreadFilter.READY, "Chờ xem lại"),
                            Pair(ThreadFilter.PURCHASED, "Đã mua")
                        ).forEach { (filter, label) ->
                            val isSelected = uiState.selectedFilter == filter && !uiState.showArchived
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onFilterSelected(filter) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CandyColors.brandPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = CandyColors.textPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) Color.Transparent else CandyColors.divider,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }

                // Danh sách Threads
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
                                    "Không tìm thấy cuộc trò chuyện phù hợp"
                                else
                                    "Chưa có cuộc trò chuyện nào",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandyColors.textPrimary
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Text("Xóa tìm kiếm", color = CandyColors.brandPurple)
                                }
                            }
                        }
                    }
                } else {
                    items(threads, key = { it.id }) { thread ->
                        ChatThreadRow(
                            thread = thread,
                            onClick = { onNavigateToDetail(thread.id) },
                            onOptionsClick = { viewModel.openOptions(thread) },
                            modifier = Modifier.padding(horizontal = CandySpace.lg, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3. Floating Action Button "+ Đoạn chat mới" nổi phía trên Bottom Bar
        ExtendedFloatingActionButton(
            onClick = onNavigateToNewChat,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color.White,
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
            containerColor = CandyColors.brandPurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = CandySpace.lg, bottom = effectiveBottomPadding + CandySpace.md)
        )
    }

    // Modal BottomSheet: Tùy chọn trên từng Thread Row
    val threadForOptions = uiState.threadForOptions
    if (threadForOptions != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeOptions() },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = threadForOptions.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Đổi tên
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.openRenameDialog(threadForOptions) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = CandyColors.brandPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Đổi tên cuộc trò chuyện", fontSize = 15.sp, color = CandyColors.textPrimary)
                }

                // 2. Lưu trữ / Khôi phục
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.openArchiveDialog(threadForOptions) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (threadForOptions.isArchived) Icons.Rounded.Refresh else Icons.Rounded.Archive
                    val label = if (threadForOptions.isArchived) "Khôi phục cuộc trò chuyện" else "Lưu trữ cuộc trò chuyện"
                    Icon(icon, contentDescription = null, tint = CandyColors.brandPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, fontSize = 15.sp, color = CandyColors.textPrimary)
                }

                // 3. Xem món đồ (nếu có linkedItemId)
                val linkedItemId = threadForOptions.linkedItemId
                if (linkedItemId != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.closeOptions()
                                onNavigateToItemDetail(linkedItemId)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = CandyColors.brandPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Xem chi tiết món đồ", fontSize = 15.sp, color = CandyColors.textPrimary)
                    }
                }

                // 4. Xóa
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.openDeleteDialog(threadForOptions) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = CandyColors.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Xóa cuộc trò chuyện", fontSize = 15.sp, color = CandyColors.error)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs
    val threadForRename = uiState.threadForRename
    if (threadForRename != null) {
        RenameThreadDialog(
            initialTitle = threadForRename.title,
            onConfirm = { viewModel.confirmRename(it) },
            onDismiss = { viewModel.closeRenameDialog() }
        )
    }

    val threadForArchive = uiState.threadForArchive
    if (threadForArchive != null) {
        ArchiveConfirmationDialog(
            onConfirm = { viewModel.confirmArchive() },
            onDismiss = { viewModel.closeArchiveDialog() }
        )
    }

    val threadForDelete = uiState.threadForDelete
    if (threadForDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.closeDeleteDialog() }
        )
    }
}

/**
 * Thẻ một cuộc trò chuyện kiểu Messenger
 */
@Composable
fun ChatThreadRow(
    thread: ChatThread,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(thread.updatedAt))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, CandyColors.divider, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail 56dp
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0E7FF)),
                contentAlignment = Alignment.Center
            ) {
                if (thread.mode == ThreadMode.ITEM) {
                    Image(
                        painter = painterResource(id = R.drawable.chanko_neutral),
                        contentDescription = "Item Chat",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "Free Chat",
                        tint = CandyColors.brandPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Nội dung chính
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = thread.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CandyColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = CandyColors.mutedInk
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = thread.previewText ?: "Nhấn để tiếp tục trò chuyện...",
                    fontSize = 13.sp,
                    color = CandyColors.mutedInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tag trạng thái
                val (tagLabel, tagBg, tagText) = when {
                    thread.isArchived -> Triple("Đã lưu trữ", Color(0xFFEEEEEE), Color(0xFF666666))
                    thread.itemStatus == ItemContextStatus.CONSIDERING -> Triple("Đang cân nhắc", Color(0xFFFFF7E6), Color(0xFFD48806))
                    thread.itemStatus == ItemContextStatus.READY -> Triple("Chờ xem lại", Color(0xFFE6FFFB), Color(0xFF08979C))
                    thread.itemStatus == ItemContextStatus.PURCHASED -> Triple("Đã mua", Color(0xFFF6FFED), Color(0xFF389E0D))
                    else -> Triple("Tự do", Color(0xFFF0E7FF), CandyColors.brandPurple)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tagLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagText
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nút More
            IconButton(
                onClick = onOptionsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Tùy chọn",
                    tint = CandyColors.mutedInk
                )
            }
        }
    }
}
