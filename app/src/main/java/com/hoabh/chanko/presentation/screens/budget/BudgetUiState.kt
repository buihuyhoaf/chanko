package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetDestination

/**
 * Trạng thái UI đa trục cho phân hệ Ngân sách (C01–C13).
 */
data class BudgetUiState(
    val selectedCycleId: String? = null,
    val selectedCycle: BudgetCycle? = null,
    val availableCycles: List<BudgetCycle> = emptyList(),
    val offlineCachedCycleIds: Set<String> = emptySet(),
    val snapshot: BudgetSnapshot? = null,
    val paymentMethods: List<PaymentMethodBreakdown> = emptyList(),
    val notableItems: List<NotableItem> = emptyList(),
    val recentActivities: List<BudgetActivity> = emptyList(),
    val adjustments: List<BudgetAdjustment> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val isOnline: Boolean = true,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isOfflineNoCache: Boolean = false,
    val errorMessage: String? = null,
    val dataUpdatedAt: Long? = null,
    val lastSyncTimeText: String? = null,
    val currentSheet: BudgetSheetDestination? = null,
    val sheetBackStack: List<BudgetSheetDestination> = emptyList(),
    val isDiscardDialogOpen: Boolean = false,
    val cardToDelete: CreditCard? = null,
    val deleteCardInlineError: String? = null,
    // C04 Edit Budget form state & conflict state
    val editBudgetDraft: String = "",
    val editBudgetInitialText: String = "",
    val editBudgetBaseRevision: Int? = null,
    val editBudgetInlineError: String? = null,
    val hasEditBudgetConflict: Boolean = false,
    // C06 Add Credit Card form state
    val addCardDraft: String = "",
    val addCardInlineError: String? = null,
    // C07 Rename Credit Card form state
    val renameCardDraft: String = "",
    val renameCardInitialText: String = "",
    val renameCardInlineError: String? = null
) {
    companion object {
        fun isBudgetDraftDirty(draft: String, initialText: String): Boolean {
            val initialAmount = MoneyVnd.parse(initialText)
            val amount = MoneyVnd.parse(draft)
            return if (amount != null && initialAmount != null) amount != initialAmount
            else draft.trim() != initialText.trim()
        }
    }

    val isCurrentCycle: Boolean get() = selectedCycle?.isCurrent == true

    /**
     * Quyền ghi tập trung (Centralized Reducer):
     * Chỉ cho phép ghi khi: Đang online AND Thuộc chu kỳ hiện tại AND Không loading AND Không submitting.
     */
    val canWrite: Boolean get() = isOnline && isCurrentCycle && !isLoading && !isSubmitting

    /** Lý do khóa chỉnh sửa hiển thị trên C03/C04/C05/C06/C07/C08 khi !canWrite. */
    val writeDisabledReason: String?
        get() = when {
            !isOnline -> "Kết nối mạng để chỉnh sửa"
            selectedCycle != null && !isCurrentCycle -> "Chỉ chỉnh sửa trong chu kỳ hiện tại"
            else -> null
        }

    val isOverBudget: Boolean get() = snapshot?.isOverBudget == true

    val isZeroSpend: Boolean get() = snapshot?.isZeroSpend == true

    /** Kiểm tra form đang mở có thay đổi chưa lưu hay không. */
    val isCurrentSheetDirty: Boolean
        get() = when (currentSheet) {
            is BudgetSheetDestination.EditBudget -> isBudgetDraftDirty(editBudgetDraft, editBudgetInitialText)
            BudgetSheetDestination.AddCreditCard -> addCardDraft.isNotBlank()
            is BudgetSheetDestination.RenameCreditCard ->
                CreditCard.normalizeName(renameCardDraft) != CreditCard.normalizeName(renameCardInitialText)
            else -> false
        }
}
