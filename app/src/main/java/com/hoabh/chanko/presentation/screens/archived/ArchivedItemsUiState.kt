package com.hoabh.chanko.presentation.screens.archived

import androidx.compose.runtime.Immutable
import com.hoabh.chanko.presentation.screens.myitems.MyItemCardUi

@Immutable
data class ArchivedItemsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val items: List<MyItemCardUi> = emptyList()
)

sealed interface ArchivedItemsUiEvent {
    data class SearchQueryChanged(val query: String) : ArchivedItemsUiEvent
    data class RestoreItem(val itemId: Int) : ArchivedItemsUiEvent
    data class ItemClicked(val itemId: Int) : ArchivedItemsUiEvent
}

sealed interface ArchivedItemsUiEffect {
    data object NavigateBack : ArchivedItemsUiEffect
    data class NavigateToDetail(val itemId: Int) : ArchivedItemsUiEffect
    data class ShowToast(val message: String) : ArchivedItemsUiEffect
}
