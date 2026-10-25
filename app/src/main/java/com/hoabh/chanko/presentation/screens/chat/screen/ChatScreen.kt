package com.hoabh.chanko.presentation.screens.chat.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatViewModel

/**
 * ChatScreen:
 * Composable bao ngoài hỗ trợ điều hướng giữa [ChatEmptyScreen] (S01 Empty Hero)
 * và [ChatThreadScreen] (S02–S11 Thread Conversation).
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToThread: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.thread != null || uiState.messages.isNotEmpty()) {
        ChatThreadScreen(
            threadId = uiState.thread?.id ?: "",
            viewModel = viewModel,
            onNavigateBack = {
                viewModel.startNewChat()
            },
            onNavigateToNewChat = {
                viewModel.startNewChat()
            },
            modifier = modifier
        )
    } else {
        ChatEmptyScreen(
            viewModel = viewModel,
            onNavigateToThread = { threadId ->
                if (onNavigateToThread != null) {
                    onNavigateToThread(threadId)
                } else {
                    viewModel.selectThread(threadId)
                }
            },
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}
