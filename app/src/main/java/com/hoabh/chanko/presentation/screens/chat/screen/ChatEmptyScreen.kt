package com.hoabh.chanko.presentation.screens.chat.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.presentation.screens.chat.components.ChatComposerBar
import com.hoabh.chanko.presentation.screens.chat.components.ChatEmptyHero
import com.hoabh.chanko.presentation.screens.chat.components.ChatHistorySheet
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatViewModel
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * S01 — Chat Empty Screen
 * Màn hình bắt đầu khi vào tính năng Chat with Chanko.
 * Hiển thị linh vật Chanko chào mừng, 3 câu hỏi gợi ý (Starter Prompts),
 * và khung nhập tin nhắn ban đầu.
 * Khi click gợi ý hoặc gửi tin nhắn, điều hướng sang [ChatThreadScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatEmptyScreen(
    viewModel: ChatViewModel,
    onNavigateToThread: (threadId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeThreads by viewModel.activeThreads.collectAsState()
    val archivedThreads by viewModel.archivedThreads.collectAsState()
    val historySheetState = rememberModalBottomSheetState()

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
                    text = "Chat with Chanko",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Nút mở Lịch sử trò chuyện (S04)
                IconButton(onClick = { viewModel.openHistorySheet() }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Lịch sử trò chuyện",
                        tint = CandyColors.brandPurple
                    )
                }
            }
        },
        bottomBar = {
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
                isItemBound = false
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
            ChatEmptyHero(
                onPromptClick = { prompt ->
                    viewModel.sendPrompt(prompt) { threadId ->
                        onNavigateToThread(threadId)
                    }
                },
                modifier = Modifier.weight(1f)
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
