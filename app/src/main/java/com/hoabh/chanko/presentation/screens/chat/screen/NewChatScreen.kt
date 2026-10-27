package com.hoabh.chanko.presentation.screens.chat.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatMode
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatUiEffect
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatUiState
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatViewModel
import com.hoabh.chanko.ui.theme.CandyColors
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NewChatScreen(
    viewModel: NewChatViewModel,
    onNavigateToChatDetail: (contextMode: String, itemId: String?, itemName: String?, itemPrice: Long?) -> Unit,
    onNavigateToMyItems: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is NewChatUiEffect.NavigateToChatDetail -> {
                    onNavigateToChatDetail(
                        effect.contextMode,
                        effect.itemId,
                        effect.itemName,
                        effect.itemPrice
                    )
                }
                is NewChatUiEffect.NavigateToMyItems -> onNavigateToMyItems()
                is NewChatUiEffect.Close -> onClose()
            }
        }
    }

    NewChatContent(
        state = state,
        onSelectMode = viewModel::selectMode,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onSelectItem = viewModel::selectItem,
        onStartChat = viewModel::startChat,
        onNavigateToMyItems = viewModel::onNavigateToMyItems,
        onClose = viewModel::onClose,
        modifier = modifier
    )
}

@Composable
fun NewChatContent(
    state: NewChatUiState,
    onSelectMode: (NewChatMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectItem: (CooldownItem) -> Unit,
    onStartChat: () -> Unit,
    onNavigateToMyItems: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CandyColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            NewChatHeader(onClose = onClose)

            // Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Card 1: Trò chuyện tự do
                ModeSelectionCard(
                    title = "Trò chuyện tự do",
                    description = "Hỏi đáp, tâm sự chi tiêu, nhận lời khuyên can chung từ Chanko.",
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CandyColors.backgroundLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.SYS_01,
                                contentDescription = null,
                                size = 26.dp
                            )
                        }
                    },
                    isSelected = state.selectedMode == NewChatMode.FREE,
                    onClick = { onSelectMode(NewChatMode.FREE) }
                )

                // Card 2: Về một món đồ
                ModeSelectionCard(
                    title = "Về một món đồ",
                    description = "Thẩm định một món đồ trong danh sách để Chanko phân tích kỹ lưỡng.",
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFECE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.ITM_03,
                                contentDescription = null,
                                size = 26.dp
                            )
                        }
                    },
                    isSelected = state.selectedMode == NewChatMode.ITEM,
                    onClick = { onSelectMode(NewChatMode.ITEM) }
                )

                // Inline Item Picker (chỉ xuất hiện khi chọn mode ITEM)
                AnimatedVisibility(
                    visible = state.selectedMode == NewChatMode.ITEM,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    InlineItemPickerSection(
                        items = state.filteredItems,
                        allItemsCount = state.items.size,
                        selectedItem = state.selectedItem,
                        searchQuery = state.searchQuery,
                        isLoading = state.isLoading,
                        onSearchQueryChange = onSearchQueryChange,
                        onSelectItem = onSelectItem,
                        onNavigateToMyItems = onNavigateToMyItems
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fixed Footer CTA (Tuyệt đối không có composer)
            NewChatFixedFooter(
                ctaText = state.ctaButtonText,
                isEnabled = state.canStartChat,
                onStartChat = onStartChat
            )
        }
    }
}

@Composable
private fun NewChatHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(48.dp)
        ) {
            ChankoIcon(
                id = ChankoIconId.ACT_03,
                contentDescription = "Đóng"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "Đoạn chat mới",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Text(
                text = "Chọn cách bắt đầu",
                fontSize = 13.sp,
                color = CandyColors.mutedInk
            )
        }
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) CandyColors.brandPurple else CandyColors.divider
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val backgroundColor = if (isSelected) CandyColors.backgroundLavender.copy(alpha = 0.35f) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = CandyColors.mutedInk
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = CandyColors.brandPurple,
                unselectedColor = CandyColors.divider
            )
        )
    }
}

@Composable
private fun InlineItemPickerSection(
    items: List<CooldownItem>,
    allItemsCount: Int,
    selectedItem: CooldownItem?,
    searchQuery: String,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectItem: (CooldownItem) -> Unit,
    onNavigateToMyItems: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Chọn món đồ muốn thẩm định",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = CandyColors.textPrimary
        )

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Tìm món đồ...", fontSize = 14.sp, color = CandyColors.mutedInk) },
            leadingIcon = {
                ChankoIcon(
                    id = ChankoIconId.ACT_07,
                    contentDescription = null,
                    size = 20.dp
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CandyColors.brandPurple,
                unfocusedBorderColor = CandyColors.divider,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = CandyColors.brandPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, CandyColors.divider, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (allItemsCount == 0) {
                            "Chưa có món đồ nào đang cân nhắc"
                        } else {
                            "Không tìm thấy món đồ phù hợp"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CandyColors.mutedInk
                    )

                    if (allItemsCount == 0) {
                        TextButton(onClick = onNavigateToMyItems) {
                            Text(
                                text = "Xem danh sách món đồ",
                                color = CandyColors.brandPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            ChankoIcon(
                                id = ChankoIconId.ACT_18,
                                contentDescription = null,
                                size = 16.dp
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isChecked = selectedItem?.id == item.id
                    val itemBorderColor = if (isChecked) CandyColors.brandPurple else CandyColors.divider
                    val itemBg = if (isChecked) CandyColors.backgroundLavender.copy(alpha = 0.25f) else Color.White

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, itemBorderColor, RoundedCornerShape(16.dp))
                            .background(itemBg)
                            .clickable { onSelectItem(item) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CandyColors.backgroundLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.ITM_03,
                                contentDescription = null,
                                size = 22.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandyColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "%,d ₫".format(item.price),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CandyColors.brandPurple
                            )
                        }

                        RadioButton(
                            selected = isChecked,
                            onClick = { onSelectItem(item) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CandyColors.brandPurple,
                                unselectedColor = CandyColors.divider
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChatFixedFooter(
    ctaText: String,
    isEnabled: Boolean,
    onStartChat: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onStartChat,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.brandPurple,
                    disabledContainerColor = CandyColors.divider
                )
            ) {
                Text(
                    text = ctaText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) Color.White else CandyColors.mutedInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
