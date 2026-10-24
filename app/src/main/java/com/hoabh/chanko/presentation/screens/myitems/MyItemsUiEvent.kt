package com.hoabh.chanko.presentation.screens.myitems

sealed interface MyItemsUiEvent {
    data class SearchQueryChanged(val query: String) : MyItemsUiEvent
    data class PrimaryFilterSelected(val filter: PrimaryFilter) : MyItemsUiEvent
    data class ActiveSubFilterSelected(val subFilter: ActiveSubFilter) : MyItemsUiEvent
    data class SortOptionSelected(val sortOption: ItemSortOption) : MyItemsUiEvent
    data class OverviewTileClicked(val tileType: OverviewTileType) : MyItemsUiEvent
    data object OpenFilterSheet : MyItemsUiEvent
    data object DismissFilterSheet : MyItemsUiEvent
    data object ResetFilters : MyItemsUiEvent
    data object ClearSearchAndFilters : MyItemsUiEvent
    data class ItemClicked(val itemId: Int) : MyItemsUiEvent
    data object ArchivedMenuClicked : MyItemsUiEvent
}

enum class OverviewTileType {
    ALL_ITEMS,
    COOLING_OFF,
    READY
}

sealed interface MyItemsUiEffect {
    data class NavigateToDetail(val itemId: Int) : MyItemsUiEffect
    data object NavigateToArchived : MyItemsUiEffect
}
