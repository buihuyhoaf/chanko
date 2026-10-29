package com.hoabh.chanko.domain.budget.model

enum class BudgetActivityType {
    PURCHASE_RECORDED,
    EXPENSE_CORRECTED,
    BUDGET_ADJUSTED
}

/**
 * Biến động ảnh hưởng đến ngân sách trong chu kỳ (Domain Model).
 * Chỉ lưu các sự kiện làm thay đổi số tiền (không đưa WAIT/SKIP/lưu món đơn thuần).
 */
data class BudgetActivity(
    val id: String,
    val cycleId: String,
    val title: String,
    val amountChangeMinor: Long,
    val occurredAt: String,
    val itemId: String? = null,
    val type: BudgetActivityType = BudgetActivityType.PURCHASE_RECORDED
) {
    val amountFormatted: String
        get() {
            val prefix = if (amountChangeMinor > 0) "+" else ""
            return "$prefix${MoneyVnd.format(amountChangeMinor)}"
        }
}
