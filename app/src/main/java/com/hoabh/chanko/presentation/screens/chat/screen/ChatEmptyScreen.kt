package com.hoabh.chanko.presentation.screens.chat.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.chat.components.ChatComposerBar
import com.hoabh.chanko.presentation.screens.chat.components.ChatEmptyHero
import com.hoabh.chanko.presentation.screens.chat.components.ChatHistorySheet
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatViewModel
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * S01 — Chat Empty Screen
 * Màn hình bắt đầu khi vào tính năng Chat with Chanko (Tab Chat trên BottomBar).
 * Hiển thị Header chuẩn theo phong cách màn Món đồ (MyItems),
 * linh vật Chanko chào mừng, 3 câu hỏi gợi ý (Starter Prompts),
 * và khung nhập tin nhắn ban đầu.
 * Khi click gợi ý hoặc gửi tin nhắn, điều hướng sang [ChatThreadScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEmptyScreen(
    viewModel: ChatViewModel,
    onNavigateToThread: (threadId: String) -> Unit,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeThreads by viewModel.activeThreads.collectAsState()
    val archivedThreads by viewModel.archivedThreads.collectAsState()
    val historySheetState = rememberModalBottomSheetState()
    var isMenuExpanded by remember { mutableStateOf(false) }

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
            // 1. Top App Bar (Thiết kế đồng bộ giống Screen Món đồ)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.chat_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = CandyColors.textPrimary
                )

                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.chat_history_menu),
                            tint = CandyColors.textPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.chat_history_menu)) },
                            onClick = {
                                isMenuExpanded = false
                                viewModel.openHistorySheet()
                            }
                        )
                    }
                }
            }

            // Content Body: Empty Hero
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                ChatEmptyHero(
                    onPromptClick = { prompt ->
                        viewModel.sendPrompt(prompt) { threadId ->
                            onNavigateToThread(threadId)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom: Composer Bar (đặt trên BottomBar để không bị che khuất)
            ChatComposerBar(
                text = uiState.inputText,
                onTextChanged = viewModel::onTextChanged,
                onSendClicked = {
                    viewModel.sendMessage { threadId ->
                        onNavigateToThread(threadId)
                    }
                },
                onPickItemClicked = {},
                isEnabled = !uiState.isSending,
                isItemBound = false,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        }
    }

    // S04 — Chat History BottomSheet
    if (uiState.showHistorySheet) {
        ChatHistorySheet(
            activeThreads = activeThreads,
            archivedThreads = archivedThreads,
            currentThreadId = uiState.thread?.id,
            onSelectThread = { threadId ->
                viewModel.closeHistorySheet()
                onNavigateToThread(threadId)
            },
            onDismiss = { viewModel.closeHistorySheet() },
            sheetState = historySheetState
        )
    }
}
