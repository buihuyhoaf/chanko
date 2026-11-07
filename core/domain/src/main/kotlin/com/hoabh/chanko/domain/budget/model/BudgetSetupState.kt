package com.hoabh.chanko.domain.budget.model

/**
 * Trạng thái cấu hình ngân sách của chu kỳ (Budget v3):
 * - [Configured]: Đã thiết lập hạn mức hợp lệ (> 0 VND)
 * - [Missing]: Chưa thiết lập (dashed card trên lưới gallery, nút CTA "+ Thiết lập")
 * - [OverBudget]: Chi vượt hạn mức (giữ palette tháng, đổi badge/số tiền sang Coral warning)
 */
enum class BudgetSetupState {
    Configured,
    Missing,
    OverBudget
}
