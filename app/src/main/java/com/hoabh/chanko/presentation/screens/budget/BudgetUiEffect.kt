package com.hoabh.chanko.presentation.screens.budget

sealed interface BudgetUiEffect {
    data class NavigateToSharedItemDetail(
        val itemId: String,
        val routeId: Int = itemId.trim().removePrefix("item_").toIntOrNull() ?: -1
    ) : BudgetUiEffect
    data class NavigateToWalletSettings(val expandHistory: Boolean = false) : BudgetUiEffect
    data class ShowSnackbarResource(@param:androidx.annotation.StringRes val messageRes: Int) : BudgetUiEffect
    data class ShowSnackbar(val message: String) : BudgetUiEffect
}
