package com.hoabh.chanko.presentation.screens.budget

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : BaseViewModel<BudgetUiState, BudgetUiEffect>(BudgetUiState()) {

    private var cycleDataJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        observeNetwork()
        observeAvailableCycles()
        observeCreditCards()
    }

    private fun observeNetwork() {
        repository.isOnlineFlow()
            .onEach { online ->
                val timeText = if (online) null else timeFormat.format(Date())
                updateState { it.copy(isOnline = online, lastSyncTimeText = timeText) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAvailableCycles() {
        repository.getAvailableCycles()
            .onEach { cycles ->
                updateState { state ->
                    val selected = state.selectedCycle ?: cycles.firstOrNull { it.isCurrent } ?: cycles.firstOrNull()
                    state.copy(availableCycles = cycles, selectedCycle = selected)
                }
                currentState.selectedCycle?.let { loadCycleData(it) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCreditCards() {
        repository.getCreditCards()
            .onEach { cards ->
                updateState { it.copy(creditCards = cards) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCycleData(cycle: BudgetCycle) {
        cycleDataJob?.cancel()
        cycleDataJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }

            // Lắng nghe snapshot
            launch {
                repository.getBudgetSnapshot(cycle.id)
                    .catch { error ->
                        updateState { it.copy(isLoading = false, errorMessage = error.message) }
                    }
                    .collect { snapshot ->
                        updateState { it.copy(snapshot = snapshot, isLoading = false) }
                    }
            }

            // Lắng nghe payment breakdown
            launch {
                repository.getPaymentBreakdown(cycle.id)
                    .collect { breakdown ->
                        updateState { it.copy(paymentMethods = breakdown) }
                    }
            }

            // Lắng nghe notable items
            launch {
                repository.getNotableItems(cycle.id)
                    .collect { items ->
                        updateState { it.copy(notableItems = items) }
                    }
            }

            // Lắng nghe activities
            launch {
                repository.getRecentActivities(cycle.id)
                    .collect { activities ->
                        updateState { it.copy(recentActivities = activities) }
                    }
            }

            // Lắng nghe adjustments
            launch {
                repository.getAdjustments(cycle.id)
                    .collect { adjustments ->
                        updateState { it.copy(adjustments = adjustments) }
                    }
            }
        }
    }

    fun onEvent(event: BudgetUiEvent) {
        when (event) {
            BudgetUiEvent.OpenCyclePicker -> navigateToSheet(BudgetSheetDestination.CyclePicker)
            BudgetUiEvent.OpenWalletSettings -> navigateToSheet(BudgetSheetDestination.WalletSettings)
            BudgetUiEvent.OpenEditBudget -> {
                val currentLimit = currentState.snapshot?.budgetMinor ?: 0L
                navigateToSheet(BudgetSheetDestination.EditBudget(currentLimit))
            }
            BudgetUiEvent.OpenCreditCardManage -> navigateToSheet(BudgetSheetDestination.CreditCardManage)
            BudgetUiEvent.OpenAddCreditCard -> navigateToSheet(BudgetSheetDestination.AddCreditCard)
            is BudgetUiEvent.OpenRenameCreditCard -> navigateToSheet(BudgetSheetDestination.RenameCreditCard(event.card))
            BudgetUiEvent.OpenAdjustmentHistory -> navigateToSheet(BudgetSheetDestination.AdjustmentHistory)

            is BudgetUiEvent.SelectCycle -> {
                val cycle = currentState.availableCycles.firstOrNull { it.id == event.cycleId }
                if (cycle != null) {
                    updateState {
                        it.copy(
                            selectedCycle = cycle,
                            currentSheet = null,
                            sheetBackStack = emptyList()
                        )
                    }
                    loadCycleData(cycle)
                }
            }

            is BudgetUiEvent.SubmitBudgetLimit -> submitBudgetLimit(event.newLimitMinor)
            is BudgetUiEvent.SubmitAddCreditCard -> submitAddCreditCard(event.name)
            is BudgetUiEvent.SubmitRenameCreditCard -> submitRenameCreditCard(event.cardId, event.newName)
            is BudgetUiEvent.RequestDeleteCreditCard -> updateState { it.copy(cardToDelete = event.card) }
            BudgetUiEvent.ConfirmDeleteCreditCard -> confirmDeleteCreditCard()
            BudgetUiEvent.DismissDeleteCreditCardDialog -> updateState { it.copy(cardToDelete = null) }

            is BudgetUiEvent.NotableItemClicked -> sendEffect(BudgetUiEffect.NavigateToSharedItemDetail(event.itemId))
            is BudgetUiEvent.ActivityItemClicked -> {
                if (event.activity.type == BudgetActivityType.BUDGET_ADJUSTED) {
                    navigateToSheet(BudgetSheetDestination.AdjustmentHistory)
                } else if (!event.activity.itemId.isNullOrBlank()) {
                    sendEffect(BudgetUiEffect.NavigateToSharedItemDetail(event.activity.itemId))
                }
            }

            BudgetUiEvent.DismissSheet -> {
                updateState { it.copy(currentSheet = null, sheetBackStack = emptyList()) }
            }

            is BudgetUiEvent.BackPressedInSheet -> {
                if (event.isDirty) {
                    updateState { it.copy(isDiscardDialogOpen = true) }
                } else {
                    popSheet()
                }
            }

            BudgetUiEvent.ConfirmDiscardChanges -> {
                updateState { it.copy(isDiscardDialogOpen = false) }
                popSheet()
            }

            BudgetUiEvent.CancelDiscardChanges -> {
                updateState { it.copy(isDiscardDialogOpen = false) }
            }

            BudgetUiEvent.Retry -> {
                currentState.selectedCycle?.let { loadCycleData(it) }
            }

            BudgetUiEvent.ReloadConflict -> {
                currentState.selectedCycle?.let { loadCycleData(it) }
            }
        }
    }

    private fun navigateToSheet(destination: BudgetSheetDestination) {
        updateState { state ->
            val stack = state.currentSheet?.let { state.sheetBackStack + it } ?: state.sheetBackStack
            state.copy(currentSheet = destination, sheetBackStack = stack)
        }
    }

    private fun popSheet() {
        updateState { state ->
            if (state.sheetBackStack.isNotEmpty()) {
                val next = state.sheetBackStack.last()
                state.copy(
                    currentSheet = next,
                    sheetBackStack = state.sheetBackStack.dropLast(1)
                )
            } else {
                state.copy(currentSheet = null, sheetBackStack = emptyList())
            }
        }
    }

    private fun submitBudgetLimit(newLimitMinor: Long) {
        val cycle = currentState.selectedCycle ?: return
        val currentSnap = currentState.snapshot ?: return

        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true) }
            val result = repository.updateBudgetLimit(cycle.id, newLimitMinor, currentSnap.revision)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                popSheet()
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã cập nhật ngân sách"))
            }.onFailure { error ->
                sendEffect(BudgetUiEffect.ShowSnackbar(error.message ?: "Chưa lưu được ngân sách"))
            }
        }
    }

    private fun submitAddCreditCard(name: String) {
        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true) }
            val result = repository.addCreditCard(name)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                popSheet()
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã thêm thẻ"))
            }.onFailure { error ->
                sendEffect(BudgetUiEffect.ShowSnackbar(error.message ?: "Chưa thêm được thẻ"))
            }
        }
    }

    private fun submitRenameCreditCard(cardId: String, newName: String) {
        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true) }
            val result = repository.renameCreditCard(cardId, newName)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                popSheet()
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã đổi tên thẻ"))
            }.onFailure { error ->
                sendEffect(BudgetUiEffect.ShowSnackbar(error.message ?: "Chưa đổi được tên thẻ"))
            }
        }
    }

    private fun confirmDeleteCreditCard() {
        val card = currentState.cardToDelete ?: return
        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true) }
            val result = repository.archiveCreditCard(card.id)
            updateState { it.copy(isSubmitting = false, cardToDelete = null) }

            result.onSuccess {
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã xóa thẻ"))
            }.onFailure { error ->
                sendEffect(BudgetUiEffect.ShowSnackbar(error.message ?: "Chưa xóa được thẻ"))
            }
        }
    }
}
