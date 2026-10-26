package com.hoabh.chanko.presentation.screens.chat

import androidx.compose.runtime.Immutable
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MascotEmotion

@Immutable
data class ChatUiState(
    val thread: ChatThread? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isChankoTyping: Boolean = false,
    val isOffline: Boolean = false,
    val emotion: MascotEmotion = MascotEmotion.NEUTRAL,
    val showItemPicker: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showArchiveDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showHistorySheet: Boolean = false,
    val showOptionsSheet: Boolean = false,
    val error: String? = null,
    val draftMode: String = "FREE",
    val draftItemId: String? = null,
    val draftItemName: String? = null,
    val draftItemPrice: Long? = null,
    val quickReplies: List<String> = emptyList()
)

sealed interface ChatUiEffect {
    data class NavigateToItemDetail(val itemId: String) : ChatUiEffect
    data object NavigateBack : ChatUiEffect
}
