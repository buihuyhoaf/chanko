package com.hoabh.chanko.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val thread: ChatThread? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isOffline: Boolean = false,
    val emotion: MascotEmotion = MascotEmotion.NEUTRAL,
    val showItemPicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showHistorySheet: Boolean = false,
    val showOptionsSheet: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    // null biểu thị trạng thái S01 (Empty Screen) khi vừa mở hoặc bắt đầu đoạn chat mới
    private val _currentThreadId = MutableStateFlow<String?>(null)
    private val _inputText = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _isSending = MutableStateFlow(false)
    private val _showItemPicker = MutableStateFlow(false)
    private val _showDeleteDialog = MutableStateFlow(false)
    private val _showHistorySheet = MutableStateFlow(false)
    private val _showOptionsSheet = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val activeThreads: StateFlow<List<ChatThread>> = chatRepository.getActiveThreads()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val archivedThreads: StateFlow<List<ChatThread>> = chatRepository.getArchivedThreads()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val currentThreadFlow = _currentThreadId.combine(
        combine(activeThreads, archivedThreads) { active, archived -> active + archived }
    ) { id, list ->
        if (id == null) null else list.find { it.id == id }
    }

    private val messagesFlow = MutableStateFlow<List<ChatMessage>>(emptyList())

    // Gom nhóm trạng thái dialog / sheet
    private data class DialogState(
        val showItemPicker: Boolean,
        val showDeleteDialog: Boolean,
        val showHistorySheet: Boolean,
        val showOptionsSheet: Boolean,
        val error: String?
    )

    private val dialogStateFlow = combine(
        _showItemPicker,
        _showDeleteDialog,
        _showHistorySheet,
        _showOptionsSheet,
        _error
    ) { picker, dialog, history, options, err ->
        DialogState(picker, dialog, history, options, err)
    }

    val uiState: StateFlow<ChatUiState> = combine(
        currentThreadFlow,
        messagesFlow,
        _inputText,
        _isSending,
        dialogStateFlow
    ) { thread, messages, text, sending, dialogs ->
        val currentEmotion = if (thread?.isArchived == true) {
            MascotEmotion.SLEEPING
        } else if (sending) {
            MascotEmotion.THINKING
        } else if (dialogs.error != null) {
            MascotEmotion.CONCERNED
        } else {
            messages.lastOrNull { it.sender == MessageSender.CHANKO }?.emotion
                ?: when (thread?.mode) {
                    ThreadMode.ITEM -> MascotEmotion.THINKING
                    else -> MascotEmotion.CURIOUS
                }
        }

        ChatUiState(
            thread = thread,
            messages = messages,
            inputText = text,
            isLoading = false,
            isSending = sending,
            isOffline = false,
            emotion = currentEmotion,
            showItemPicker = dialogs.showItemPicker,
            showDeleteDialog = dialogs.showDeleteDialog,
            showHistorySheet = dialogs.showHistorySheet,
            showOptionsSheet = dialogs.showOptionsSheet,
            error = dialogs.error
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatUiState())

    init {
        // Mặc định khởi đầu ở S01 Empty, không load thread nào
        messagesFlow.value = emptyList()
    }

    fun startNewChat() {
        _currentThreadId.value = null
        messagesFlow.value = emptyList()
        _error.value = null
    }

    fun selectThread(threadId: String) {
        _currentThreadId.value = threadId
        loadThread(threadId)
    }

    private fun loadThread(threadId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(threadId).collect {
                messagesFlow.value = it
            }
        }
    }

    fun onTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        _inputText.value = ""
        _isSending.value = true

        val targetThreadId = _currentThreadId.value ?: ""

        viewModelScope.launch {
            val result = chatRepository.sendMessage(targetThreadId, text)
            _isSending.value = false
            if (result.isSuccess) {
                val sentMsg = result.getOrNull()
                if (sentMsg != null && _currentThreadId.value == null) {
                    // Chuyển từ S01 Empty sang thread mới tạo (S02)
                    _currentThreadId.value = sentMsg.threadId
                    loadThread(sentMsg.threadId)
                }
            } else {
                _error.value = "Không thể gửi tin nhắn"
            }
        }
    }

    fun bindItem(itemId: String, itemName: String, itemPriceVnd: Long) {
        _showItemPicker.value = false
        val currentId = _currentThreadId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.bindExistingItem(currentId, itemId, itemName, itemPriceVnd)
            _isLoading.value = false
        }
    }

    fun requestVerdict() {
        val currentId = _currentThreadId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.requestVerdict(currentId)
            _isLoading.value = false
        }
    }

    fun retryLastAction() {
        _error.value = null
        if (_inputText.value.isNotBlank()) {
            sendMessage()
        }
    }

    fun dismissError() {
        _error.value = null
    }

    fun openItemPicker() { _showItemPicker.value = true }
    fun closeItemPicker() { _showItemPicker.value = false }

    fun openDeleteDialog() {
        _showOptionsSheet.value = false
        _showDeleteDialog.value = true
    }
    fun closeDeleteDialog() { _showDeleteDialog.value = false }

    fun openHistorySheet() { _showHistorySheet.value = true }
    fun closeHistorySheet() { _showHistorySheet.value = false }

    fun openOptionsSheet() { _showOptionsSheet.value = true }
    fun closeOptionsSheet() { _showOptionsSheet.value = false }

    fun archiveCurrentThread() {
        val currentId = _currentThreadId.value ?: return
        _showOptionsSheet.value = false
        viewModelScope.launch {
            chatRepository.archiveThread(currentId)
        }
    }

    fun restoreCurrentThread() {
        val currentId = _currentThreadId.value ?: return
        _showOptionsSheet.value = false
        viewModelScope.launch {
            chatRepository.restoreThread(currentId)
        }
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        val currentId = _currentThreadId.value ?: return
        _showDeleteDialog.value = false
        viewModelScope.launch {
            chatRepository.deleteThread(currentId)
            _currentThreadId.value = null
            messagesFlow.value = emptyList()
            onDeleted()
        }
    }
}
