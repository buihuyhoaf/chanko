package com.hoabh.chanko.presentation.features.chat.newchat

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.presentation.features.chat.components.formatVndPrice
import com.hoabh.chanko.presentation.features.chat.components.getProductThumbnailResId
import com.hoabh.chanko.ui.theme.ChatTokens

@Composable
fun NewChatScreen(
    viewModel: NewChatViewModel,
    onNavigateToChatDetail: (threadId: String?, productId: String?, productName: String?, productPrice: Long?, thumbnailUrl: String?) -> Unit,
    onNavigateToMyItems: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NewChatUiEffect.NavigateToExistingChat -> {
                    onNavigateToChatDetail(effect.threadId, null, null, null, null)
                }
                is NewChatUiEffect.NavigateToNewDraftChat -> {
                    onNavigateToChatDetail(
                        null,
                        effect.productId,
                        effect.productName,
                        effect.productPrice,
                        effect.thumbnailUrl
                    )
                }
                is NewChatUiEffect.NavigateToMyItems -> onNavigateToMyItems()
                is NewChatUiEffect.Close -> onClose()
            }
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Đóng màn hình tạo đoạn chat mới"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chat_close_default),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Bắt đầu cuộc trò chuyện",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTokens.body
                )
            }

            // Subtitle & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Chọn món đồ bạn muốn thẩm định hoặc tâm sự cùng Chanko:",
                    fontSize = 13.sp,
                    color = ChatTokens.secondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = { Text("Tìm món đồ trong danh sách…", fontSize = 14.sp, color = ChatTokens.secondary) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.chat_search_default),
                            contentDescription = "Tìm kiếm",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = ChatTokens.primaryTextAndCTA,
                        unfocusedBorderColor = ChatTokens.divider
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Product List or Empty State (NC-GAP-02, NC-GAP-03)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ChatTokens.primaryTextAndCTA)
                    }
                } else if (state.filteredProducts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (state.searchQuery.isNotEmpty())
                                "Không tìm thấy món đồ phù hợp"
                            else
                                "Chưa có món đồ nào trong danh sách",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChatTokens.body
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hãy thêm món đồ bạn đang cân nhắc mua để Chanko giúp bạn hạ nhiệt nhé!",
                            fontSize = 13.sp,
                            color = ChatTokens.secondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = viewModel::onNavigateToMyItems,
                            colors = ButtonDefaults.buttonColors(containerColor = ChatTokens.primaryTextAndCTA),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(text = "Thêm món đồ ngay", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 16.dp
                        )
                    ) {
                        items(state.filteredProducts, key = { it.productId }) { product ->
                            val isSelected = state.selectedProduct?.productId == product.productId
                            ProductSelectionCard(
                                product = product,
                                isSelected = isSelected,
                                onClick = { viewModel.selectProduct(product) }
                            )
                        }
                    }
                }
            }

            // Bottom CTA Button (min 56dp)
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = viewModel::startChat,
                        enabled = state.canStartChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChatTokens.primaryTextAndCTA,
                            disabledContainerColor = ChatTokens.divider
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    ) {
                        Text(
                            text = if (state.selectedProduct != null)
                                "Trò chuyện về ${state.selectedProduct!!.name}"
                            else
                                "Chọn một món đồ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.canStartChat) Color.White else ChatTokens.secondary
                        )
                    }
                }
            }
        }
    }

    // Dialog: History Found (CHAT-02, NC-GAP-01)
    if (state.showHistoryDialog) {
        val activeThread = state.existingActiveThread
        val archivedCount = state.archivedThreads.size
        AlertDialog(
            onDismissRequest = viewModel::onDismissHistoryDialog,
            title = {
                Text(
                    text = "Món đồ đã có cuộc trò chuyện",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ChatTokens.body
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bạn đã có đoạn chat '${activeThread?.title ?: ""}' đang hoạt động về món đồ này.",
                        fontSize = 14.sp,
                        color = ChatTokens.body,
                        lineHeight = 20.sp
                    )
                    if (archivedCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "(Ngoài ra có $archivedCount đoạn chat về món này trong kho lưu trữ)",
                            fontSize = 12.sp,
                            color = ChatTokens.secondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onContinueExistingChat,
                    colors = ButtonDefaults.buttonColors(containerColor = ChatTokens.primaryTextAndCTA),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tiếp tục đoạn chat cũ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCreateNewThreadAnyway) {
                    Text("Tạo đoạn chat mới", color = ChatTokens.primaryTextAndCTA)
                }
            }
        )
    }
}

/**
 * ProductSelectionCard: Thẻ chọn 1 sản phẩm chính (NC-GAP-02) với semantics RadioButton.
 * Card min 88dp, thumbnail 64dp.
 */
@Composable
fun ProductSelectionCard(
    product: ProductSnapshot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ChatTokens.backgroundStart else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ChatTokens.primary else ChatTokens.disabledBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "${product.name}, giá ${formatVndPrice(product.price)}"
            }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val thumbRes = product.thumbnailResId ?: getProductThumbnailResId(product.thumbnailUrl)
            Image(
                painter = painterResource(id = thumbRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChatTokens.grayIconBg)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTokens.body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatVndPrice(product.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatTokens.primaryTextAndCTA
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ChatTokens.primaryTextAndCTA,
                    unselectedColor = ChatTokens.radioUnselected
                )
            )
        }
    }
}
