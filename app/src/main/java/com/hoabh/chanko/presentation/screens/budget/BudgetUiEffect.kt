package com.hoabh.chanko.presentation.screens.budget

sealed interface BudgetUiEffect {
    data class NavigateToSharedItemDetail(val itemId: String) : BudgetUiEffect
    data class ShowSnackbar(val message: String) : BudgetUiEffect
}
