package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetDestination

/**
 * Trạng thái UI đa trục cho phân hệ Ngân sách (C01–C13).
 */
data class BudgetUiState(
    val selectedCycle: BudgetCycle? = null,
    val availableCycles: List<BudgetCycle> = emptyList(),
    val snapshot: BudgetSnapshot? = null,
    val paymentMethods: List<PaymentMethodBreakdown> = emptyList(),
    val notableItems: List<NotableItem> = emptyList(),
    val recentActivities: List<BudgetActivity> = emptyList(),
    val adjustments: List<BudgetAdjustment> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val isOnline: Boolean = true,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val lastSyncTimeText: String? = null,
    val currentSheet: BudgetSheetDestination? = null,
    val sheetBackStack: List<BudgetSheetDestination> = emptyList(),
    val isDiscardDialogOpen: Boolean = false,
    val cardToDelete: CreditCard? = null
) {
    val isCurrentCycle: Boolean get() = selectedCycle?.isCurrent == true

    /**
     * Quyền ghi tập trung (Centralized Reducer):
     * Chỉ cho phép ghi khi: Đang online AND Thuộc chu kỳ hiện tại AND Không loading AND Không submitting.
     */
    val canWrite: Boolean get() = isOnline && isCurrentCycle && !isLoading && !isSubmitting

    val isOverBudget: Boolean get() = snapshot?.isOverBudget == true

    val isZeroSpend: Boolean get() = snapshot?.isZeroSpend == true
}
