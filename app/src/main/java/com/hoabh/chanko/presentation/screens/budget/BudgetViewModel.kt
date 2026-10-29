package com.hoabh.chanko.presentation.screens.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.data.budget.InMemorySharedItemResolver
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.BudgetConflictException
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException
import com.hoabh.chanko.domain.budget.model.CreditCardUnavailableException
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import com.hoabh.chanko.domain.budget.repository.SharedItemResolver
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
    private val repository: BudgetRepository,
    private val sharedItemResolver: SharedItemResolver,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<BudgetUiState, BudgetUiEffect>(
    BudgetUiState(
        selectedCycleId = savedStateHandle.get<String>(KEY_SELECTED_CYCLE_ID),
        editBudgetDraft = savedStateHandle.get<String>(KEY_EDIT_BUDGET_DRAFT) ?: "",
        addCardDraft = savedStateHandle.get<String>(KEY_ADD_CARD_DRAFT) ?: "",
        renameCardDraft = savedStateHandle.get<String>(KEY_RENAME_CARD_DRAFT) ?: ""
    )
) {

    /** Constructor phụ phục vụ unit test truyền trực tiếp repository. */
    constructor(
        repository: BudgetRepository
    ) : this(
        repository = repository,
        sharedItemResolver = InMemorySharedItemResolver(),
        savedStateHandle = SavedStateHandle()
    )

    companion object {
        const val KEY_SELECTED_CYCLE_ID = "selected_cycle_id"
        const val KEY_EDIT_BUDGET_DRAFT = "edit_budget_draft"
        const val KEY_ADD_CARD_DRAFT = "add_card_draft"
        const val KEY_RENAME_CARD_DRAFT = "rename_card_draft"
    }

    private var cycleDataJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())

    init {
        observeNetwork()
        observeOfflineCachedCycles()
        observeAvailableCycles()
        observeCreditCards()
    }

    private fun formatDataTimestamp(epochMillis: Long?): String? {
        if (epochMillis == null || epochMillis <= 0L) return null
        return timeFormat.format(Date(epochMillis))
    }

    private fun observeNetwork() {
        repository.isOnlineFlow()
            .onEach { online ->
                updateState { state ->
                    val syncText = if (online) null else formatDataTimestamp(state.dataUpdatedAt)
                    state.copy(isOnline = online, lastSyncTimeText = syncText)
                }
                // Khi mất mạng hoặc có mạng lại, kiểm tra lại dữ liệu của chu kỳ đang chọn
                currentState.selectedCycle?.let { cycle ->
                    if (!online && cycle.id !in currentState.offlineCachedCycleIds) {
                        loadCycleData(cycle)
                    } else if (online && currentState.errorMessage != null) {
                        loadCycleData(cycle)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeOfflineCachedCycles() {
        repository.getOfflineCachedCycleIds()
            .onEach { cachedIds ->
                updateState { it.copy(offlineCachedCycleIds = cachedIds) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAvailableCycles() {
        repository.getAvailableCycles()
            .onEach { cycles ->
                val restoredCycleId = savedStateHandle.get<String>(KEY_SELECTED_CYCLE_ID)
                    ?: currentState.selectedCycleId
                    ?: currentState.selectedCycle?.id

                val selected = cycles.firstOrNull { it.id == restoredCycleId }
                    ?: cycles.firstOrNull { it.isCurrent }
                    ?: cycles.firstOrNull()

                val previousCycleId = currentState.selectedCycle?.id
                if (selected != null) {
                    savedStateHandle[KEY_SELECTED_CYCLE_ID] = selected.id
                }

                updateState { state ->
                    state.copy(
                        availableCycles = cycles,
                        selectedCycleId = selected?.id,
                        selectedCycle = selected
                    )
                }

                if (selected != null && (selected.id != previousCycleId || currentState.snapshot == null)) {
                    loadCycleData(selected)
                }
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

    /**
     * Tải nguyên tử (atomic) toàn bộ dữ liệu của một chu kỳ.
     * Xoá ngay dữ liệu của kỳ cũ khi đổi kỳ để không bao giờ trộn số liệu giữa các kỳ
     * hoặc che khuất lỗi tải kỳ mới dưới snapshot kỳ cũ (C02, AC02/AC06).
     */
    private fun loadCycleData(cycle: BudgetCycle) {
        cycleDataJob?.cancel()
        val isSwitchingCycle = currentState.snapshot != null && currentState.snapshot?.cycleId != cycle.id

        updateState { state ->
            state.copy(
                isLoading = true,
                errorMessage = null,
                isOfflineNoCache = false,
                snapshot = if (isSwitchingCycle) null else state.snapshot,
                paymentMethods = if (isSwitchingCycle) emptyList() else state.paymentMethods,
                notableItems = if (isSwitchingCycle) emptyList() else state.notableItems,
                recentActivities = if (isSwitchingCycle) emptyList() else state.recentActivities,
                adjustments = if (isSwitchingCycle) emptyList() else state.adjustments
            )
        }

        cycleDataJob = viewModelScope.launch {
            repository.getCycleOverview(cycle.id)
                .catch { error ->
                    val isNoCache = error is BudgetNoOfflineCacheException
                    val message = when (error) {
                        is BudgetNoOfflineCacheException -> "Chưa có dữ liệu để xem ngoại tuyến"
                        else -> error.message ?: "Chưa tải được dữ liệu ngân sách"
                    }
                    updateState { state ->
                        if (state.selectedCycle?.id != cycle.id) {
                            state
                        } else {
                            state.copy(
                                snapshot = null,
                                paymentMethods = emptyList(),
                                notableItems = emptyList(),
                                recentActivities = emptyList(),
                                adjustments = emptyList(),
                                isLoading = false,
                                isOfflineNoCache = isNoCache,
                                errorMessage = message
                            )
                        }
                    }
                }
                .collect { overview ->
                    updateState { state ->
                        if (state.selectedCycle?.id != cycle.id || overview.cycleId != cycle.id) {
                            state
                        } else {
                            val syncText = if (state.isOnline) {
                                null
                            } else {
                                formatDataTimestamp(overview.dataUpdatedAt)
                            }
                            state.copy(
                                snapshot = overview.snapshot,
                                paymentMethods = overview.paymentMethods,
                                notableItems = overview.notableItems,
                                recentActivities = overview.recentActivities,
                                adjustments = overview.adjustments,
                                dataUpdatedAt = overview.dataUpdatedAt,
                                lastSyncTimeText = syncText,
                                isLoading = false,
                                isOfflineNoCache = false,
                                errorMessage = null
                            )
                        }
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
                val currentRevision = currentState.snapshot?.revision
                val initialText = if (currentLimit > 0L) MoneyVnd.formatWithoutSymbol(currentLimit) else ""
                savedStateHandle[KEY_EDIT_BUDGET_DRAFT] = initialText
                updateState {
                    it.copy(
                        editBudgetInitialText = initialText,
                        editBudgetDraft = initialText,
                        editBudgetBaseRevision = currentRevision,
                        editBudgetInlineError = null,
                        hasEditBudgetConflict = false
                    )
                }
                navigateToSheet(BudgetSheetDestination.EditBudget(currentLimit))
            }
            BudgetUiEvent.OpenCreditCardManage -> navigateToSheet(BudgetSheetDestination.CreditCardManage)
            BudgetUiEvent.OpenAddCreditCard -> {
                savedStateHandle[KEY_ADD_CARD_DRAFT] = ""
                updateState {
                    it.copy(
                        addCardDraft = "",
                        addCardInlineError = null
                    )
                }
                navigateToSheet(BudgetSheetDestination.AddCreditCard)
            }
            is BudgetUiEvent.OpenRenameCreditCard -> {
                savedStateHandle[KEY_RENAME_CARD_DRAFT] = event.card.name
                updateState {
                    it.copy(
                        renameCardInitialText = event.card.name,
                        renameCardDraft = event.card.name,
                        renameCardInlineError = null
                    )
                }
                navigateToSheet(BudgetSheetDestination.RenameCreditCard(event.card))
            }
            BudgetUiEvent.OpenAdjustmentHistory -> navigateToSheet(BudgetSheetDestination.AdjustmentHistory)

            is BudgetUiEvent.SelectCycle -> {
                if (currentState.isSubmitting) return
                // Khi đang chọn đúng chu kỳ hiện tại: chỉ đóng sheet
                if (event.cycleId == currentState.selectedCycle?.id) {
                    updateState { it.copy(currentSheet = null, sheetBackStack = emptyList()) }
                    return
                }
                // Khi ngoại tuyến, chặn chọn chu kỳ chưa được cache (C02)
                if (!currentState.isOnline && event.cycleId !in currentState.offlineCachedCycleIds) {
                    sendEffect(BudgetUiEffect.ShowSnackbar("Chưa có dữ liệu ngoại tuyến"))
                    return
                }
                val cycle = currentState.availableCycles.firstOrNull { it.id == event.cycleId }
                if (cycle != null) {
                    savedStateHandle[KEY_SELECTED_CYCLE_ID] = cycle.id
                    updateState {
                        it.copy(
                            selectedCycleId = cycle.id,
                            selectedCycle = cycle,
                            currentSheet = null,
                            sheetBackStack = emptyList()
                        )
                    }
                    loadCycleData(cycle)
                }
            }

            is BudgetUiEvent.UpdateEditBudgetDraft -> {
                savedStateHandle[KEY_EDIT_BUDGET_DRAFT] = event.text
                updateState {
                    it.copy(
                        editBudgetDraft = event.text,
                        editBudgetInlineError = if (it.hasEditBudgetConflict) it.editBudgetInlineError else null
                    )
                }
            }

            is BudgetUiEvent.UpdateAddCardDraft -> {
                savedStateHandle[KEY_ADD_CARD_DRAFT] = event.text
                updateState {
                    it.copy(
                        addCardDraft = event.text,
                        addCardInlineError = null
                    )
                }
            }

            is BudgetUiEvent.UpdateRenameCardDraft -> {
                savedStateHandle[KEY_RENAME_CARD_DRAFT] = event.text
                updateState {
                    it.copy(
                        renameCardDraft = event.text,
                        renameCardInlineError = null
                    )
                }
            }

            is BudgetUiEvent.SubmitBudgetLimit -> submitBudgetLimit(event.newLimitMinor)
            is BudgetUiEvent.SubmitAddCreditCard -> submitAddCreditCard(event.name)
            is BudgetUiEvent.SubmitRenameCreditCard -> submitRenameCreditCard(event.cardId, event.newName)
            is BudgetUiEvent.RequestDeleteCreditCard -> {
                updateState {
                    it.copy(
                        cardToDelete = event.card,
                        deleteCardInlineError = null
                    )
                }
            }
            BudgetUiEvent.ConfirmDeleteCreditCard -> confirmDeleteCreditCard()
            BudgetUiEvent.DismissDeleteCreditCardDialog -> {
                if (currentState.isSubmitting) return
                updateState { it.copy(cardToDelete = null, deleteCardInlineError = null) }
            }

            is BudgetUiEvent.NotableItemClicked -> handleOpenSharedItemDetail(event.itemId)
            is BudgetUiEvent.ActivityItemClicked -> {
                if (event.activity.type == BudgetActivityType.BUDGET_ADJUSTED) {
                    navigateToSheet(BudgetSheetDestination.AdjustmentHistory)
                } else if (!event.activity.itemId.isNullOrBlank()) {
                    handleOpenSharedItemDetail(event.activity.itemId)
                }
            }

            BudgetUiEvent.DismissSheet -> {
                if (currentState.isSubmitting) return
                if (currentState.isCurrentSheetDirty) {
                    updateState { it.copy(isDiscardDialogOpen = true) }
                } else {
                    updateState { it.copy(currentSheet = null, sheetBackStack = emptyList()) }
                }
            }

            BudgetUiEvent.RequestCloseCurrentSheet -> {
                if (currentState.isSubmitting) return
                if (currentState.isCurrentSheetDirty) {
                    updateState { it.copy(isDiscardDialogOpen = true) }
                } else {
                    popSheet()
                }
            }

            is BudgetUiEvent.BackPressedInSheet -> {
                if (currentState.isSubmitting) return
                val dirty = event.isDirty || currentState.isCurrentSheetDirty
                if (dirty) {
                    updateState { it.copy(isDiscardDialogOpen = true) }
                } else {
                    popSheet()
                }
            }

            BudgetUiEvent.ConfirmDiscardChanges -> {
                if (currentState.isSubmitting) return
                clearCurrentFormDraft()
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
                handleReloadConflict()
            }
        }
    }

    private fun handleOpenSharedItemDetail(itemId: String) {
        viewModelScope.launch {
            val routeId = sharedItemResolver.resolveRouteId(itemId)
            if (routeId != null) {
                sendEffect(BudgetUiEffect.NavigateToSharedItemDetail(itemId = itemId, routeId = routeId))
            } else {
                sendEffect(BudgetUiEffect.ShowSnackbar("Món đồ chưa có dữ liệu ngoại tuyến"))
            }
        }
    }

    private fun handleReloadConflict() {
        val cycle = currentState.selectedCycle ?: return
        loadCycleData(cycle)
        val latestLimit = currentState.snapshot?.budgetMinor ?: 0L
        val latestRevision = currentState.snapshot?.revision
        val latestText = if (latestLimit > 0L) MoneyVnd.formatWithoutSymbol(latestLimit) else ""
        savedStateHandle[KEY_EDIT_BUDGET_DRAFT] = latestText
        updateState { state ->
            val updatedSheet = if (state.currentSheet is BudgetSheetDestination.EditBudget) {
                BudgetSheetDestination.EditBudget(latestLimit)
            } else {
                state.currentSheet
            }
            state.copy(
                currentSheet = updatedSheet,
                editBudgetInitialText = latestText,
                editBudgetDraft = latestText,
                editBudgetBaseRevision = latestRevision,
                editBudgetInlineError = null,
                hasEditBudgetConflict = false
            )
        }
        sendEffect(BudgetUiEffect.ShowSnackbar("Đã tải lại ngân sách mới nhất. Vui lòng kiểm tra trước khi lưu."))
    }

    private fun clearCurrentFormDraft() {
        when (currentState.currentSheet) {
            is BudgetSheetDestination.EditBudget -> {
                savedStateHandle[KEY_EDIT_BUDGET_DRAFT] = ""
                updateState {
                    it.copy(
                        editBudgetDraft = "",
                        editBudgetInitialText = "",
                        editBudgetInlineError = null,
                        hasEditBudgetConflict = false
                    )
                }
            }
            BudgetSheetDestination.AddCreditCard -> {
                savedStateHandle[KEY_ADD_CARD_DRAFT] = ""
                updateState { it.copy(addCardDraft = "", addCardInlineError = null) }
            }
            is BudgetSheetDestination.RenameCreditCard -> {
                savedStateHandle[KEY_RENAME_CARD_DRAFT] = ""
                updateState {
                    it.copy(
                        renameCardDraft = "",
                        renameCardInitialText = "",
                        renameCardInlineError = null
                    )
                }
            }
            else -> Unit
        }
    }

    private fun navigateToSheet(destination: BudgetSheetDestination) {
        if (currentState.isSubmitting) return
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
            updateState {
                it.copy(
                    isSubmitting = true,
                    editBudgetInlineError = null
                )
            }
            val expectedRevision = currentState.editBudgetBaseRevision ?: currentSnap.revision
            val result = repository.updateBudgetLimit(cycle.id, newLimitMinor, expectedRevision)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                clearCurrentFormDraft()
                popSheet()
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã cập nhật ngân sách"))
            }.onFailure { error ->
                if (error is BudgetConflictException) {
                    val conflictMsg = "Ngân sách đã thay đổi. Tải lại để chỉnh sửa."
                    updateState {
                        it.copy(
                            hasEditBudgetConflict = true,
                            editBudgetInlineError = conflictMsg
                        )
                    }
                    sendEffect(BudgetUiEffect.ShowSnackbar(conflictMsg))
                } else {
                    val saveErrMsg = "Chưa lưu được ngân sách. Vui lòng thử lại."
                    updateState {
                        it.copy(
                            hasEditBudgetConflict = false,
                            editBudgetInlineError = saveErrMsg
                        )
                    }
                    sendEffect(BudgetUiEffect.ShowSnackbar(saveErrMsg))
                }
            }
        }
    }

    private fun submitAddCreditCard(name: String) {
        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true, addCardInlineError = null) }
            val result = repository.addCreditCard(name)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                clearCurrentFormDraft()
                popSheet()
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã thêm thẻ"))
            }.onFailure { error ->
                val msg = error.message ?: "Chưa thêm được thẻ. Vui lòng thử lại."
                updateState { it.copy(addCardInlineError = "Chưa thêm được thẻ. Vui lòng thử lại.") }
                sendEffect(BudgetUiEffect.ShowSnackbar(msg))
            }
        }
    }

    private fun submitRenameCreditCard(cardId: String, newName: String) {
        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true, renameCardInlineError = null) }
            val result = repository.renameCreditCard(cardId, newName)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                clearCurrentFormDraft()
                popSheet()
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã đổi tên thẻ"))
            }.onFailure { error ->
                val inlineMsg = if (error is CreditCardUnavailableException) {
                    "Thẻ không còn khả dụng"
                } else {
                    "Chưa đổi được tên thẻ. Vui lòng thử lại."
                }
                updateState { it.copy(renameCardInlineError = inlineMsg) }
                sendEffect(BudgetUiEffect.ShowSnackbar(inlineMsg))
            }
        }
    }

    private fun confirmDeleteCreditCard() {
        val card = currentState.cardToDelete ?: return
        if (!currentState.canWrite) return

        viewModelScope.launch {
            updateState { it.copy(isSubmitting = true, deleteCardInlineError = null) }
            val result = repository.archiveCreditCard(card.id)
            updateState { it.copy(isSubmitting = false) }

            result.onSuccess {
                updateState { it.copy(cardToDelete = null, deleteCardInlineError = null) }
                sendEffect(BudgetUiEffect.ShowSnackbar("Đã xóa thẻ"))
            }.onFailure { error ->
                if (error is CreditCardUnavailableException) {
                    // Thẻ đã bị archive trước đó -> đóng dialog và báo "Thẻ đã được xóa" (C08)
                    updateState { it.copy(cardToDelete = null, deleteCardInlineError = null) }
                    sendEffect(BudgetUiEffect.ShowSnackbar("Thẻ đã được xóa"))
                } else {
                    // Lỗi lưu: giữ dialog mở và hiện lỗi inline để người dùng thử lại (C08)
                    val errMsg = "Chưa xóa được thẻ. Vui lòng thử lại."
                    updateState { it.copy(deleteCardInlineError = errMsg) }
                    sendEffect(BudgetUiEffect.ShowSnackbar(errMsg))
                }
            }
        }
    }
}
