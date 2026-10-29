package com.hoabh.chanko.presentation.screens.budget

sealed interface BudgetUiEffect {
    data class NavigateToSharedItemDetail(
        val itemId: String,
        val routeId: Int = itemId.trim().removePrefix("item_").toIntOrNull() ?: -1
    ) : BudgetUiEffect
    data class ShowSnackbar(val message: String) : BudgetUiEffect
}
