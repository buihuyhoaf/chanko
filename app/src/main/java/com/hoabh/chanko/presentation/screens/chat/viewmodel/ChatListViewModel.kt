package com.hoabh.chanko.presentation.screens.chat.viewmodel

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ItemContextStatus
import com.hoabh.chanko.domain.chat.model.ThreadFilter
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val activeThreads: List<ChatThread> = emptyList(),
    val archivedThreads: List<ChatThread> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ThreadFilter = ThreadFilter.ALL,
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
            // Filter by search query
            if (searchQuery.isNotBlank()) {
                val query = searchQuery.trim().lowercase()
                list = list.filter {
                    it.title.lowercase().contains(query) ||
                        (it.linkedItemName?.lowercase()?.contains(query) == true) ||
                        (it.previewText?.lowercase()?.contains(query) == true)
                }
            }
            // Filter by status chips
            list = when (selectedFilter) {
                ThreadFilter.ALL -> list
                ThreadFilter.CONSIDERING -> list.filter { it.itemStatus == ItemContextStatus.CONSIDERING }
                ThreadFilter.READY -> list.filter { it.itemStatus == ItemContextStatus.READY }
                ThreadFilter.PURCHASED -> list.filter { it.itemStatus == ItemContextStatus.PURCHASED }
                ThreadFilter.ARCHIVED -> list.filter { it.isArchived }
            }
            return list
        }
}

sealed interface ChatListUiEffect {
    data class NavigateToDetail(val threadId: String) : ChatListUiEffect
    data object NavigateToNewChat : ChatListUiEffect
    data class NavigateToItemDetail(val itemId: String) : ChatListUiEffect
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
                chatRepository.getActiveThreads(),
                chatRepository.getArchivedThreads()
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

    fun onFilterSelected(filter: ThreadFilter) {
        updateState {
            if (filter == ThreadFilter.ARCHIVED) {
                it.copy(selectedFilter = filter, showArchived = true)
            } else {
                it.copy(selectedFilter = filter, showArchived = false)
            }
        }
    }

    fun toggleShowArchived(showArchived: Boolean) {
        updateState {
            it.copy(
                showArchived = showArchived,
                selectedFilter = if (showArchived) ThreadFilter.ARCHIVED else ThreadFilter.ALL
            )
        }
    }

    // Modal Actions
    fun openOptions(thread: ChatThread) {
        updateState { it.copy(threadForOptions = thread) }
    }

    fun closeOptions() {
        updateState { it.copy(threadForOptions = null) }
    }

    fun openRenameDialog(thread: ChatThread) {
        updateState { it.copy(threadForOptions = null, threadForRename = thread) }
    }

    fun closeRenameDialog() {
        updateState { it.copy(threadForRename = null) }
    }

    fun confirmRename(newTitle: String) {
        val target = currentState.threadForRename ?: return
        closeRenameDialog()
        viewModelScope.launch {
            chatRepository.renameThread(target.id, newTitle)
        }
    }

    fun openArchiveDialog(thread: ChatThread) {
        updateState { it.copy(threadForOptions = null, threadForArchive = thread) }
    }

    fun closeArchiveDialog() {
        updateState { it.copy(threadForArchive = null) }
    }

    fun confirmArchive() {
        val target = currentState.threadForArchive ?: return
        closeArchiveDialog()
        viewModelScope.launch {
            if (target.isArchived) {
                chatRepository.restoreThread(target.id)
            } else {
                chatRepository.archiveThread(target.id)
            }
        }
    }

    fun openDeleteDialog(thread: ChatThread) {
        updateState { it.copy(threadForOptions = null, threadForDelete = thread) }
    }

    fun closeDeleteDialog() {
        updateState { it.copy(threadForDelete = null) }
    }

    fun confirmDelete() {
        val target = currentState.threadForDelete ?: return
        closeDeleteDialog()
        viewModelScope.launch {
            chatRepository.deleteThread(target.id)
        }
    }
}
