package com.hoabh.chanko.presentation.screens.chat.viewmodel

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NewChatMode {
    FREE,
    ITEM
}

data class NewChatUiState(
    val selectedMode: NewChatMode = NewChatMode.FREE,
    val items: List<CooldownItem> = emptyList(),
    val filteredItems: List<CooldownItem> = emptyList(),
    val selectedItem: CooldownItem? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canStartChat: Boolean
        get() = when (selectedMode) {
            NewChatMode.FREE -> true
            NewChatMode.ITEM -> selectedItem != null
        }

    val ctaButtonText: String
        get() = when (selectedMode) {
            NewChatMode.FREE -> "Bắt đầu trò chuyện"
            NewChatMode.ITEM -> {
                val item = selectedItem
                if (item != null) "Trò chuyện về ${item.name}" else "Chọn một món đồ"
            }
        }
}

sealed interface NewChatUiEffect {
    data class NavigateToChatDetail(
        val threadId: String? = null,
        val contextMode: String, // "FREE" or "ITEM"
        val itemId: String? = null,
        val itemName: String? = null,
        val itemPrice: Long? = null
    ) : NewChatUiEffect
    data object NavigateToMyItems : NewChatUiEffect
    data object Close : NewChatUiEffect
}

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : BaseViewModel<NewChatUiState, NewChatUiEffect>(NewChatUiState(isLoading = true)) {

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            itemRepository.getActiveItems()
                .catch { e ->
                    updateState { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { items ->
                    updateState { state ->
                        val filtered = filterItems(items, state.searchQuery)
                        state.copy(
                            items = items,
                            filteredItems = filtered,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun selectMode(mode: NewChatMode) {
        updateState { it.copy(selectedMode = mode) }
    }

    fun updateSearchQuery(query: String) {
        updateState { state ->
            state.copy(
                searchQuery = query,
                filteredItems = filterItems(state.items, query)
            )
        }
    }

    fun selectItem(item: CooldownItem) {
        updateState { it.copy(selectedItem = item) }
    }

    fun startChat() {
        val state = currentState
        when (state.selectedMode) {
            NewChatMode.FREE -> {
                sendEffect(
                    NewChatUiEffect.NavigateToChatDetail(
                        contextMode = "FREE"
                    )
                )
            }
            NewChatMode.ITEM -> {
                val item = state.selectedItem ?: return
                sendEffect(
                    NewChatUiEffect.NavigateToChatDetail(
                        contextMode = "ITEM",
                        itemId = item.id.toString(),
                        itemName = item.name,
                        itemPrice = item.price
                    )
                )
            }
        }
    }

    fun onNavigateToMyItems() {
        sendEffect(NewChatUiEffect.NavigateToMyItems)
    }

    fun onClose() {
        sendEffect(NewChatUiEffect.Close)
    }

    private fun filterItems(items: List<CooldownItem>, query: String): List<CooldownItem> {
        if (query.isBlank()) return items
        return items.filter { it.name.contains(query, ignoreCase = true) }
    }
}
