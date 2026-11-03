package com.hoabh.chanko.presentation.screens.home

/**
 * Bộ lọc danh sách món đồ khi điều hướng sang màn hình MyItems.
 * Trên Home v2.0, nút "Xem tất cả" luôn truyền DEFAULT (HomeMyItemsFilter.ALL — AC-16).
 */
enum class HomeMyItemsFilter {
    ALL,
    WAITING,
    REVIEW,
    SKIPPED
}

sealed interface HomeUiEffect {
    data object NavigateToProfile : HomeUiEffect
    data object OpenAddItemFlow : HomeUiEffect
    data class ShowOfflineBlockedMessage(val message: String) : HomeUiEffect
    data object NavigateToBudgetDetails : HomeUiEffect
    data class NavigateToItemDetail(val itemId: String) : HomeUiEffect
    data class NavigateToMyItems(
        val filter: HomeMyItemsFilter = HomeMyItemsFilter.ALL
    ) : HomeUiEffect
}
