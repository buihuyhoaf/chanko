package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.CreditCard

sealed interface BudgetUiEvent {
    // Navigation to Sheets
    data object OpenCyclePicker : BudgetUiEvent
    data object OpenWalletSettings : BudgetUiEvent
    data object OpenEditBudget : BudgetUiEvent
    data object OpenCreditCardManage : BudgetUiEvent
    data object OpenAddCreditCard : BudgetUiEvent
    data class OpenRenameCreditCard(val card: CreditCard) : BudgetUiEvent
    data object OpenAdjustmentHistory : BudgetUiEvent

    // Cycle selection
    data class SelectCycle(val cycleId: String) : BudgetUiEvent

    // Mutations
    data class SubmitBudgetLimit(val newLimitMinor: Long) : BudgetUiEvent
    data class SubmitAddCreditCard(val name: String) : BudgetUiEvent
    data class SubmitRenameCreditCard(val cardId: String, val newName: String) : BudgetUiEvent
    data class RequestDeleteCreditCard(val card: CreditCard) : BudgetUiEvent
    data object ConfirmDeleteCreditCard : BudgetUiEvent
    data object DismissDeleteCreditCardDialog : BudgetUiEvent

    // Item & Activity taps
    data class NotableItemClicked(val itemId: String) : BudgetUiEvent
    data class ActivityItemClicked(val activity: BudgetActivity) : BudgetUiEvent

    // Sheet Host Backstack & Dirty Handling
    data object DismissSheet : BudgetUiEvent
    data class BackPressedInSheet(val isDirty: Boolean) : BudgetUiEvent
    data object ConfirmDiscardChanges : BudgetUiEvent
    data object CancelDiscardChanges : BudgetUiEvent

    // Recovery & Retry
    data object Retry : BudgetUiEvent
    data object ReloadConflict : BudgetUiEvent
}
