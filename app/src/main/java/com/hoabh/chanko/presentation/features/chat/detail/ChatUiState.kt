package com.hoabh.chanko.presentation.features.chat.detail

import androidx.compose.runtime.Immutable
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason

@Immutable
data class ChatUiState(
    val thread: ChatThread? = null,
    val draftProduct: ProductSnapshot? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingChunk: String = "",
    val activeGenerationId: String? = null,
    val isOffline: Boolean = false,
    val emotion: MascotEmotion = MascotEmotion.NEUTRAL,
    val showReferencePicker: Boolean = false,
    val eligibleReferences: List<ProductSnapshot> = emptyList(),
    val showRenameDialog: Boolean = false,
    val showArchiveDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showOptionsSheet: Boolean = false,
    val error: String? = null,
    val quickReplies: List<String> = listOf(
        "Món này có thực sự cần thiết không?",
        "Có lựa chọn nào rẻ hơn không?",
        "Nếu đợi thêm 30 ngày thì sao?"
    )
) {
    val currentProduct: ProductSnapshot?
        get() = thread?.primaryProduct ?: draftProduct

    val readOnlyReason: ReadOnlyReason
        get() = thread?.readOnlyReason ?: ReadOnlyReason.NONE

    val isReadOnly: Boolean
        get() = readOnlyReason != ReadOnlyReason.NONE
}

sealed interface ChatUiEffect {
    data class NavigateToItemDetail(val productId: String) : ChatUiEffect
    data object NavigateBack : ChatUiEffect
}
