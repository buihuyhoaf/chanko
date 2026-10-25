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

    fun startNewChat() {
        observeThreadJob?.cancel()
        observeMessagesJob?.cancel()
        updateChatState {
            it.copy(
                thread = null,
                messages = emptyList(),
                error = null
            )
        }
    }

    fun selectThread(threadId: String) {
        observeThreadJob?.cancel()
        observeMessagesJob?.cancel()

        observeThreadJob = viewModelScope.launch {
            chatRepository.getThreadById(threadId).collect { thread ->
                updateChatState { it.copy(thread = thread) }
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

    fun sendMessage(onThreadCreated: ((String) -> Unit)? = null) {
        val text = currentState.inputText.trim()
        if (text.isEmpty()) return

        val targetThreadId = currentState.thread?.id ?: ""

        updateChatState {
            it.copy(
                inputText = "",
                isSending = true
            )
        }

        viewModelScope.launch {
            val result = chatRepository.sendMessage(targetThreadId, text)
            updateChatState { it.copy(isSending = false) }
            if (result.isSuccess) {
                val sentMsg = result.getOrNull()
                if (sentMsg != null) {
                    if (currentState.thread == null) {
                        // Chuyển từ S01 Empty sang thread mới tạo (S02)
                        selectThread(sentMsg.threadId)
                    }
                    onThreadCreated?.invoke(sentMsg.threadId)
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
        updateChatState { it.copy(showOptionsSheet = false) }
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
