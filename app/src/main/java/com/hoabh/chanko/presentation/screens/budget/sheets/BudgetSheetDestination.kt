package com.hoabh.chanko.presentation.screens.budget.sheets

import com.hoabh.chanko.domain.budget.model.CreditCard

/**
 * Các điểm đến của Single ModalBottomSheet Host trong phân hệ Ngân sách.
 */
sealed interface BudgetSheetDestination {
    /** C02: Sheet chọn chu kỳ */
    data object CyclePicker : BudgetSheetDestination

    /** C03: Sheet thiết lập ví */
    data object WalletSettings : BudgetSheetDestination

    /** C04: Sheet sửa ngân sách & xem trước */
    data class EditBudget(val currentLimit: Long) : BudgetSheetDestination

    /** C05: Sheet quản lý thẻ tín dụng */
    data object CreditCardManage : BudgetSheetDestination

    /** C06: Sheet thêm thẻ tín dụng */
    data object AddCreditCard : BudgetSheetDestination

    /** C07: Sheet đổi tên thẻ tín dụng */
    data class RenameCreditCard(val card: CreditCard) : BudgetSheetDestination

    /** C09: Sheet lịch sử điều chỉnh hạn mức */
    data object AdjustmentHistory : BudgetSheetDestination
}
