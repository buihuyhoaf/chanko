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
 * Hỗ trợ khóa dismiss khi đang lưu (isSubmitting) và kiểm tra bản nháp bẩn (isCurrentSheetDirty).
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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { !state.isSubmitting }
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            if (!state.isSubmitting) {
                onEvent(BudgetUiEvent.RequestCloseCurrentSheet)
            }
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
                    offlineCachedCycleIds = state.offlineCachedCycleIds,
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
                    disabledReason = state.writeDisabledReason,
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
                    draftText = state.editBudgetDraft,
                    onDraftChange = { text -> onEvent(BudgetUiEvent.UpdateEditBudgetDraft(text)) },
                    canWrite = state.canWrite,
                    disabledReason = state.writeDisabledReason,
                    isSubmitting = state.isSubmitting,
                    inlineError = state.editBudgetInlineError,
                    hasConflict = state.hasEditBudgetConflict,
                    onReloadConflict = { onEvent(BudgetUiEvent.ReloadConflict) },
                    onSave = { newLimit -> onEvent(BudgetUiEvent.SubmitBudgetLimit(newLimit)) },
                    onCancel = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) },
                    onClose = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) }
                )
            }

            BudgetSheetDestination.CreditCardManage -> {
                CreditCardManageSheet(
                    cards = state.creditCards,
                    canWrite = state.canWrite,
                    disabledReason = state.writeDisabledReason,
                    isSubmitting = state.isSubmitting,
                    onAddCardClick = { onEvent(BudgetUiEvent.OpenAddCreditCard) },
                    onRenameCardClick = { card -> onEvent(BudgetUiEvent.OpenRenameCreditCard(card)) },
                    onDeleteCardClick = { card -> onEvent(BudgetUiEvent.RequestDeleteCreditCard(card)) },
                    onClose = {
                        onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
                    }
                )
            }

            BudgetSheetDestination.AddCreditCard -> {
                AddCreditCardSheet(
                    draftText = state.addCardDraft,
                    onDraftChange = { text -> onEvent(BudgetUiEvent.UpdateAddCardDraft(text)) },
                    canWrite = state.canWrite,
                    disabledReason = state.writeDisabledReason,
                    isSubmitting = state.isSubmitting,
                    inlineError = state.addCardInlineError,
                    existingCardNames = state.creditCards.map { it.name },
                    onSave = { name -> onEvent(BudgetUiEvent.SubmitAddCreditCard(name)) },
                    onCancel = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) },
                    onClose = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) }
                )
            }

            is BudgetSheetDestination.RenameCreditCard -> {
                val isCardAvailable = state.creditCards.any { it.id == destination.card.id && !it.isArchived }
                RenameCreditCardSheet(
                    card = destination.card,
                    draftText = state.renameCardDraft,
                    onDraftChange = { text -> onEvent(BudgetUiEvent.UpdateRenameCardDraft(text)) },
                    isCardAvailable = isCardAvailable,
                    canWrite = state.canWrite,
                    disabledReason = state.writeDisabledReason,
                    isSubmitting = state.isSubmitting,
                    inlineError = state.renameCardInlineError,
                    existingCardNames = state.creditCards.map { it.name },
                    onSave = { newName -> onEvent(BudgetUiEvent.SubmitRenameCreditCard(destination.card.id, newName)) },
                    onCancel = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) },
                    onClose = { isDirty -> onEvent(BudgetUiEvent.BackPressedInSheet(isDirty)) }
                )
            }

            BudgetSheetDestination.AdjustmentHistory -> {
                AdjustmentHistorySheet(
                    adjustments = state.adjustments,
                    cycleRange = state.selectedCycle?.dateRangeText ?: "",
                    onClose = {
                        onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
                    }
                )
            }
        }
    }
}
