package com.hoabh.chanko.presentation.screens.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.screens.home.domain.HomeCurrencyFormatter
import com.hoabh.chanko.presentation.screens.myitems.ItemDisplayStatus
import com.hoabh.chanko.presentation.screens.myitems.MyItemCardUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchivedItemsUiState())
    val uiState: StateFlow<ArchivedItemsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ArchivedItemsUiEffect>()
    val effect: SharedFlow<ArchivedItemsUiEffect> = _effect.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        observeArchivedItems()
    }

    private fun observeArchivedItems() {
        viewModelScope.launch {
            combine(
                itemRepository.getArchivedItems(),
                _searchQuery
            ) { archivedItems, query ->
                val now = System.currentTimeMillis()
                val mapped = archivedItems.map { it.toUiModel(now) }
                val filtered = if (query.isBlank()) {
                    mapped
                } else {
                    val trimmed = query.trim().lowercase()
                    mapped.filter { it.title.lowercase().contains(trimmed) }
                }
                ArchivedItemsUiState(
                    isLoading = false,
                    searchQuery = query,
                    items = filtered
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: ArchivedItemsUiEvent) {
        when (event) {
            is ArchivedItemsUiEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is ArchivedItemsUiEvent.RestoreItem -> {
                viewModelScope.launch {
                    itemRepository.setArchived(event.itemId, isArchived = false)
                    _effect.emit(ArchivedItemsUiEffect.ShowToast("Đã khôi phục món đồ"))
                }
            }
            is ArchivedItemsUiEvent.ItemClicked -> {
                viewModelScope.launch {
                    _effect.emit(ArchivedItemsUiEffect.NavigateToDetail(event.itemId))
                }
            }
        }
    }

    private fun CooldownItem.toUiModel(now: Long): MyItemCardUi {
        val displayStatus = when (status) {
            ItemStatus.COOLING_DOWN -> {
                if (canDecide(now)) ItemDisplayStatus.ReadyForReview else ItemDisplayStatus.CoolingOff
            }
            ItemStatus.BOUGHT -> ItemDisplayStatus.Purchased
            ItemStatus.PASSED -> ItemDisplayStatus.Skipped
        }
        return MyItemCardUi(
            id = id,
            title = name,
            formattedPrice = HomeCurrencyFormatter.formatVnd(price),
            rawPrice = price,
            imageUri = imageUri,
            displayStatus = displayStatus,
            secondaryInfoText = "Đang lưu trữ",
            addedTimestamp = addedTimestamp
        )
    }
}
