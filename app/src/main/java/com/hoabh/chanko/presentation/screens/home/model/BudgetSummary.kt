package com.hoabh.chanko.presentation.screens.home.model

data class DailyPurchaseSummary(
    val purchaseCount: Int,
    val totalAmount: Long
)

data class BudgetSummary(
    val totalBudget: Long,
    val totalSpent: Long
) {
    val remaining: Long
        get() = totalBudget - totalSpent

    val progressPercent: Int
        get() = if (totalBudget > 0L) {
            ((totalSpent.toDouble() / totalBudget) * 100).toInt()
        } else {
            0
        }

    val progressRatio: Float
        get() = if (totalBudget > 0L) {
            (totalSpent.toFloat() / totalBudget).coerceIn(0f, 1f)
        } else {
            0f
        }
}
