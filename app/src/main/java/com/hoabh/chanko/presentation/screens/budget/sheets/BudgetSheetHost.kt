package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.hoabh.chanko.presentation.screens.budget.BudgetUiEvent
import com.hoabh.chanko.presentation.screens.budget.BudgetUiState
import kotlinx.coroutines.launch

/**
 * Host quản lý ModalBottomSheet duy nhất cho toàn bộ phân hệ Ngân sách.
 * Tuân thủ quy tắc: "Không stack nhiều sheet, host thay nội dung và duy trì backstack nội bộ".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSheetHost(
    destination: BudgetSheetDestination?,
    state: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (destination == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
        },
        sheetState = sheetState,
        modifier = modifier
    ) {
        when (destination) {
            BudgetSheetDestination.CyclePicker -> {
                CyclePickerSheet(
                    availableCycles = state.availableCycles,
                    selectedCycleId = state.selectedCycle?.id ?: "",
                    isOnline = state.isOnline,
                    onCycleSelect = { cycleId ->
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onEvent(BudgetUiEvent.SelectCycle(cycleId))
                        }
                    },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onEvent(BudgetUiEvent.DismissSheet)
                        }
                    }
                )
            }

            BudgetSheetDestination.WalletSettings -> {
                WalletSettingsSheet(
                    selectedCycle = state.selectedCycle,
                    currentSnapshot = state.snapshot,
                    canWrite = state.canWrite,
                    onEditBudgetClick = { onEvent(BudgetUiEvent.OpenEditBudget) },
                    onManageCardsClick = { onEvent(BudgetUiEvent.OpenCreditCardManage) },
                    onHistoryClick = { onEvent(BudgetUiEvent.OpenAdjustmentHistory) },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onEvent(BudgetUiEvent.DismissSheet)
                        }
                    }
                )
            }

            is BudgetSheetDestination.EditBudget -> {
                EditBudgetSheet(
                    currentLimit = destination.currentLimit,
                    currentSpent = state.snapshot?.spentMinor ?: 0L,
                    cycleRange = state.selectedCycle?.dateRangeText ?: "",
                    canWrite = state.canWrite,
                    isSubmitting = state.isSubmitting,
                    onSave = { newLimit -> onEvent(BudgetUiEvent.SubmitBudgetLimit(newLimit)) },
                    onCancel = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) }
                )
            }

            BudgetSheetDestination.CreditCardManage -> {
                CreditCardManageSheet(
                    cards = state.creditCards,
                    canWrite = state.canWrite,
                    onAddCardClick = { onEvent(BudgetUiEvent.OpenAddCreditCard) },
                    onRenameCardClick = { card -> onEvent(BudgetUiEvent.OpenRenameCreditCard(card)) },
                    onDeleteCardClick = { card -> onEvent(BudgetUiEvent.RequestDeleteCreditCard(card)) },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
                        }
                    }
                )
            }

            BudgetSheetDestination.AddCreditCard -> {
                AddCreditCardSheet(
                    canWrite = state.canWrite,
                    isSubmitting = state.isSubmitting,
                    existingCardNames = state.creditCards.map { it.name },
                    onSave = { name -> onEvent(BudgetUiEvent.SubmitAddCreditCard(name)) },
                    onCancel = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) }
                )
            }

            is BudgetSheetDestination.RenameCreditCard -> {
                RenameCreditCardSheet(
                    card = destination.card,
                    canWrite = state.canWrite,
                    isSubmitting = state.isSubmitting,
                    existingCardNames = state.creditCards.map { it.name },
                    onSave = { newName -> onEvent(BudgetUiEvent.SubmitRenameCreditCard(destination.card.id, newName)) },
                    onCancel = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) }
                )
            }

            BudgetSheetDestination.AdjustmentHistory -> {
                AdjustmentHistorySheet(
                    adjustments = state.adjustments,
                    cycleRange = state.selectedCycle?.dateRangeText ?: "",
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
                        }
                    }
                )
            }
        }
    }
}
