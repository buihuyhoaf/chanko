package com.hoabh.chanko.presentation.screens.chat.viewmodel

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.chat.ChatUiEffect
import com.hoabh.chanko.presentation.screens.chat.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel<ChatUiState, ChatUiEffect>(ChatUiState()) {

    val activeThreads: StateFlow<List<ChatThread>> = chatRepository.getActiveThreads()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val archivedThreads: StateFlow<List<ChatThread>> = chatRepository.getArchivedThreads()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var observeThreadJob: Job? = null
    private var observeMessagesJob: Job? = null

    private fun calculateEmotion(
        thread: ChatThread?,
        messages: List<ChatMessage>,
        isSending: Boolean,
        error: String?
    ): MascotEmotion {
        return if (thread?.isArchived == true) {
            MascotEmotion.SLEEPING
        } else if (isSending) {
            MascotEmotion.THINKING
        } else if (error != null) {
            MascotEmotion.CONCERNED
        } else {
            messages.lastOrNull { it.sender == MessageSender.CHANKO }?.emotion
                ?: when (thread?.mode) {
                    ThreadMode.ITEM -> MascotEmotion.THINKING
                    else -> MascotEmotion.CURIOUS
                }
        }
    }

    private fun updateChatState(reducer: (ChatUiState) -> ChatUiState) {
        updateState { oldState ->
            val newState = reducer(oldState)
            newState.copy(
                emotion = calculateEmotion(
                    thread = newState.thread,
                    messages = newState.messages,
                    isSending = newState.isSending,
                    error = newState.error
                )
            )
        }
    }

    fun initThread(
        threadId: String?,
        contextMode: String? = null,
        itemId: String? = null,
        itemName: String? = null,
        itemPrice: Long? = null
    ) {
        if (!threadId.isNullOrBlank() && threadId != "new") {
            selectThread(threadId)
        } else {
            // Khởi tạo draft state (chưa gửi)
            observeThreadJob?.cancel()
            observeMessagesJob?.cancel()

            val mode = contextMode ?: "FREE"
            val quickReplies = if (mode == "ITEM") {
                listOf(
                    "Món này có thực sự cần thiết không?",
                    "Có lựa chọn nào rẻ hơn không?",
                    "Nếu đợi thêm 30 ngày thì sao?"
                )
            } else {
                listOf(
                    "Làm sao để kiềm chế mua sắm bốc đồng?",
                    "Quy tắc 72 giờ là gì?",
                    "Cách phân biệt muốn và cần?"
                )
            }

            val welcomeMessage = if (mode == "ITEM" && !itemName.isNullOrBlank()) {
                ChatMessage(
                    id = "draft_welcome",
                    threadId = "",
                    sender = MessageSender.CHANKO,
                    text = "Chào bạn! Mình thấy bạn đang cân nhắc \"$itemName\". Hãy cùng Chanko phân tích xem món này có thực sự cần thiết không nhé!",
                    timestamp = System.currentTimeMillis(),
                    emotion = MascotEmotion.CURIOUS
                )
            } else {
                ChatMessage(
                    id = "draft_welcome",
                    threadId = "",
                    sender = MessageSender.CHANKO,
                    text = "Chào bạn, mình là Chanko! Hãy chia sẻ với mình những băn khoăn về chi tiêu của bạn nhé.",
                    timestamp = System.currentTimeMillis(),
                    emotion = MascotEmotion.CURIOUS
                )
            }

            updateChatState {
                it.copy(
                    thread = null,
                    messages = listOf(welcomeMessage),
                    draftMode = mode,
                    draftItemId = itemId,
                    draftItemName = itemName,
                    draftItemPrice = itemPrice,
                    quickReplies = quickReplies,
                    error = null
                )
            }
        }
    }

    fun startNewChat() {
        initThread(null, "FREE")
    }

    fun selectThread(threadId: String) {
        observeThreadJob?.cancel()
        observeMessagesJob?.cancel()

        observeThreadJob = viewModelScope.launch {
            chatRepository.getThreadById(threadId).collect { thread ->
                updateChatState { state ->
                    val quickReplies = if (thread?.mode == ThreadMode.ITEM) {
                        listOf(
                            "Món này có thực sự cần thiết không?",
                            "Có lựa chọn nào rẻ hơn không?",
                            "Nếu đợi thêm 30 ngày thì sao?"
                        )
                    } else {
                        listOf(
                            "Làm sao để kiềm chế mua sắm bốc đồng?",
                            "Quy tắc 72 giờ là gì?",
                            "Cách phân biệt muốn và cần?"
                        )
                    }
                    state.copy(
                        thread = thread,
                        quickReplies = quickReplies
                    )
                }
            }
        }

        observeMessagesJob = viewModelScope.launch {
            chatRepository.getMessages(threadId).collect { messages ->
                updateChatState { it.copy(messages = messages) }
            }
        }
    }

    fun onTextChanged(newText: String) {
        updateChatState { it.copy(inputText = newText) }
    }

    /**
     * Hợp đồng Quick Replies: Chạm vào chip chỉ điền nội dung vào ô composer, không tự động gửi.
     */
    fun fillComposer(quickReplyText: String) {
        onTextChanged(quickReplyText)
    }

    fun sendMessage(onThreadCreated: ((String) -> Unit)? = null) {
        val text = currentState.inputText.trim()
        if (text.isEmpty()) return

        val isDraft = currentState.thread == null
        val targetThreadId = currentState.thread?.id ?: ""
        val draftMode = currentState.draftMode
        val draftItemId = currentState.draftItemId
        val draftItemName = currentState.draftItemName
        val draftItemPrice = currentState.draftItemPrice

        val optimisticUserMsg = ChatMessage(
            id = "draft_user_msg_${System.currentTimeMillis()}",
            threadId = targetThreadId,
            sender = MessageSender.USER,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        updateChatState {
            it.copy(
                inputText = "",
                isSending = true,
                isChankoTyping = true,
                messages = if (isDraft) it.messages + optimisticUserMsg else it.messages
            )
        }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(targetThreadId, text)
            updateChatState { it.copy(isSending = false, isChankoTyping = false) }
            if (result.isSuccess) {
                val sentMsg = result.getOrNull()
                if (sentMsg != null) {
                    if (isDraft) {
                        val newThreadId = sentMsg.threadId
                        // Nếu là draft mode ITEM: liên kết ngay món đồ vào thread vừa tạo
                        if (draftMode == "ITEM" && !draftItemId.isNullOrBlank()) {
                            chatRepository.bindExistingItem(
                                newThreadId,
                                draftItemId,
                                draftItemName ?: "",
                                draftItemPrice ?: 0L
                            )
                        }
                        selectThread(newThreadId)
                        onThreadCreated?.invoke(newThreadId)
                    }
                }
            } else {
                updateChatState { it.copy(error = "Không thể gửi tin nhắn") }
            }
        }
    }

    fun sendPrompt(prompt: String, onThreadCreated: ((String) -> Unit)? = null) {
        onTextChanged(prompt)
        sendMessage(onThreadCreated)
    }

    fun bindItem(itemId: String, itemName: String, itemPriceVnd: Long) {
        val currentId = currentState.thread?.id ?: return
        updateChatState { it.copy(showItemPicker = false, isLoading = true) }
        viewModelScope.launch {
            chatRepository.bindExistingItem(currentId, itemId, itemName, itemPriceVnd)
            updateChatState { it.copy(isLoading = false) }
        }
    }

    fun renameThread(newTitle: String) {
        val currentId = currentState.thread?.id ?: return
        if (newTitle.isBlank()) return
        updateChatState { it.copy(showRenameDialog = false) }
        viewModelScope.launch {
            chatRepository.renameThread(currentId, newTitle.trim())
        }
    }

    fun requestVerdict() {
        val currentId = currentState.thread?.id ?: return
        updateChatState { it.copy(isLoading = true) }
        viewModelScope.launch {
            chatRepository.requestVerdict(currentId)
            updateChatState { it.copy(isLoading = false) }
        }
    }

    fun retryLastAction() {
        updateChatState { it.copy(error = null) }
        if (currentState.inputText.isNotBlank()) {
            sendMessage()
        }
    }

    fun dismissError() {
        updateChatState { it.copy(error = null) }
    }

    fun openItemPicker() { updateChatState { it.copy(showItemPicker = true) } }
    fun closeItemPicker() { updateChatState { it.copy(showItemPicker = false) } }

    fun openRenameDialog() {
        updateChatState { it.copy(showOptionsSheet = false, showRenameDialog = true) }
    }
    fun closeRenameDialog() { updateChatState { it.copy(showRenameDialog = false) } }

    fun openArchiveDialog() {
        updateChatState { it.copy(showOptionsSheet = false, showArchiveDialog = true) }
    }
    fun closeArchiveDialog() { updateChatState { it.copy(showArchiveDialog = false) } }

    fun openDeleteDialog() {
        updateChatState { it.copy(showOptionsSheet = false, showDeleteDialog = true) }
    }
    fun closeDeleteDialog() { updateChatState { it.copy(showDeleteDialog = false) } }

    fun openHistorySheet() { updateChatState { it.copy(showHistorySheet = true) } }
    fun closeHistorySheet() { updateChatState { it.copy(showHistorySheet = false) } }

    fun openOptionsSheet() { updateChatState { it.copy(showOptionsSheet = true) } }
    fun closeOptionsSheet() { updateChatState { it.copy(showOptionsSheet = false) } }

    fun archiveCurrentThread() {
        val currentId = currentState.thread?.id ?: return
        updateChatState { it.copy(showArchiveDialog = false, showOptionsSheet = false) }
        viewModelScope.launch {
            chatRepository.archiveThread(currentId)
        }
    }

    fun restoreCurrentThread() {
        val currentId = currentState.thread?.id ?: return
        updateChatState { it.copy(showOptionsSheet = false) }
        viewModelScope.launch {
            chatRepository.restoreThread(currentId)
        }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        val currentId = currentState.thread?.id ?: return
        updateChatState { it.copy(showDeleteDialog = false) }
        viewModelScope.launch {
            chatRepository.deleteThread(currentId)
            observeThreadJob?.cancel()
            observeMessagesJob?.cancel()
            updateChatState {
                it.copy(
                    thread = null,
                    messages = emptyList()
                )
            }
            onDeleted()
        }
    }
}
