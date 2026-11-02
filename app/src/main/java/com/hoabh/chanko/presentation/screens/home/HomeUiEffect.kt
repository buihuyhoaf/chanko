package com.hoabh.chanko.presentation.screens.home

/**
 * Bộ lọc danh sách món đồ khi điều hướng từ Home / Contextual Sheet sang màn hình MyItems (AC-06).
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
    data class NavigateToDecisionDetail(val id: String) : HomeUiEffect
    data class NavigateToMyItems(
        val filter: HomeMyItemsFilter = HomeMyItemsFilter.ALL
    ) : HomeUiEffect
}
