package com.hoabh.chanko.presentation.features.chat.list

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val activeThreads: List<ChatThread> = emptyList(),
    val archivedThreads: List<ChatThread> = emptyList(),
    val searchQuery: String = "",
    val showArchived: Boolean = false,
    val isLoading: Boolean = false,
    val threadForOptions: ChatThread? = null,
    val threadForRename: ChatThread? = null,
    val threadForArchive: ChatThread? = null,
    val threadForDelete: ChatThread? = null
) {
    val threads: List<ChatThread>
        get() = if (showArchived) archivedThreads else activeThreads

    val filteredThreads: List<ChatThread>
        get() {
            var list = threads
            if (searchQuery.isNotBlank()) {
                val query = searchQuery.trim().lowercase()
                list = list.filter {
                    it.title.lowercase().contains(query) ||
                        it.primaryProduct.name.lowercase().contains(query) ||
                        (it.previewText?.lowercase()?.contains(query) == true)
                }
            }
            return list.sortedWith(
                compareByDescending<ChatThread> { it.updatedAtEpochMillis }.thenByDescending { it.id }
            )
        }
}

sealed interface ChatListUiEffect {
    data class NavigateToDetail(val threadId: String) : ChatListUiEffect
    data object NavigateToNewChat : ChatListUiEffect
    data class NavigateToItemDetail(val productId: String) : ChatListUiEffect
}

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel<ChatListUiState, ChatListUiEffect>(ChatListUiState()) {

    init {
        observeThreads()
    }

    private fun observeThreads() {
        viewModelScope.launch {
            combine(
                chatRepository.getThreads(isArchived = false),
                chatRepository.getThreads(isArchived = true)
            ) { active, archived ->
                Pair(active, archived)
            }.collect { (active, archived) ->
                updateState { state ->
                    state.copy(
                        activeThreads = active,
                        archivedThreads = archived,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        updateState { it.copy(searchQuery = query) }
    }

    fun onToggleArchived(showArchived: Boolean) {
        updateState { it.copy(showArchived = showArchived) }
    }

    fun onThreadClicked(threadId: String) {
        sendEffect(ChatListUiEffect.NavigateToDetail(threadId))
    }

    fun onNewChatClicked() {
        sendEffect(ChatListUiEffect.NavigateToNewChat)
    }

    fun onOptionsClicked(thread: ChatThread) {
        updateState { it.copy(threadForOptions = thread) }
    }

    fun onDismissOptions() {
        updateState { it.copy(threadForOptions = null) }
    }

    fun onRequestRename(thread: ChatThread) {
        updateState { it.copy(threadForOptions = null, threadForRename = thread) }
    }

    fun onDismissRename() {
        updateState { it.copy(threadForRename = null) }
    }

    fun onConfirmRename(newTitle: String) {
        val target = currentState.threadForRename ?: return
        viewModelScope.launch {
            chatRepository.renameThread(target.id, newTitle)
            updateState { it.copy(threadForRename = null) }
        }
    }

    fun onRequestArchive(thread: ChatThread) {
        updateState { it.copy(threadForOptions = null, threadForArchive = thread) }
    }

    fun onDismissArchive() {
        updateState { it.copy(threadForArchive = null) }
    }

    fun onConfirmArchive() {
        val target = currentState.threadForArchive ?: return
        viewModelScope.launch {
            chatRepository.archiveThread(target.id)
            updateState { it.copy(threadForArchive = null) }
        }
    }

    fun onRestoreThread(thread: ChatThread) {
        viewModelScope.launch {
            chatRepository.restoreThread(thread.id)
            updateState { it.copy(threadForOptions = null) }
        }
    }

    fun onRequestDelete(thread: ChatThread) {
        updateState { it.copy(threadForOptions = null, threadForDelete = thread) }
    }

    fun onDismissDelete() {
        updateState { it.copy(threadForDelete = null) }
    }

    fun onConfirmDelete() {
        val target = currentState.threadForDelete ?: return
        viewModelScope.launch {
            chatRepository.deleteThread(target.id)
            updateState { it.copy(threadForDelete = null) }
        }
    }
}
