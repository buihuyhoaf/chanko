package com.hoabh.chanko.presentation.screens.budget.sheets

/**
 * Các điểm đến của Single ModalBottomSheet Host trong phân hệ Ngân sách (v2.1).
 * Duy nhất 2 Bottom Sheet:
 * 1. CyclePicker (C02 - Chọn chu kỳ ngân sách)
 * 2. EditBudget (C04 - Sửa ngân sách & xem trước)
 */
sealed interface BudgetSheetDestination {
    /** C02: Sheet chọn chu kỳ */
    data object CyclePicker : BudgetSheetDestination

    /** C04: Sheet sửa ngân sách & xem trước */
    data class EditBudget(val currentLimit: Long) : BudgetSheetDestination
}
