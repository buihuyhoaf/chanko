package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity

sealed interface BudgetUiEvent {
    // Navigation to Sheets / Routes
    data object OpenCyclePicker : BudgetUiEvent
    data object OpenWalletSettings : BudgetUiEvent
    data object OpenEditBudget : BudgetUiEvent
    data object OpenAdjustmentHistory : BudgetUiEvent

    // Cycle selection
    data class SelectCycle(val cycleId: String) : BudgetUiEvent

    // C09 Accordion toggle
    data class ToggleHistoryAccordion(val isExpanded: Boolean? = null) : BudgetUiEvent

    // Draft updates
    data class UpdateEditBudgetDraft(val text: String) : BudgetUiEvent

    // Mutations
    data class SubmitBudgetLimit(val newLimitMinor: Long) : BudgetUiEvent

    // Item & Activity taps
    data class NotableItemClicked(val itemId: String) : BudgetUiEvent
    data class ActivityItemClicked(val activity: BudgetActivity) : BudgetUiEvent

    // Sheet Host Backstack & Dirty Handling
    data object DismissSheet : BudgetUiEvent
    data object RequestCloseCurrentSheet : BudgetUiEvent
    data class BackPressedInSheet(val isDirty: Boolean = false) : BudgetUiEvent
    data object ConfirmDiscardChanges : BudgetUiEvent
    data object CancelDiscardChanges : BudgetUiEvent

    // Recovery & Retry
    data object Retry : BudgetUiEvent
    data object ReloadConflict : BudgetUiEvent
}
