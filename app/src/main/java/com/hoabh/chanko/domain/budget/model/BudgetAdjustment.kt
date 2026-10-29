package com.hoabh.chanko.domain.budget.model

/**
 * Lịch sử điều chỉnh hạn mức ngân sách của một chu kỳ (Domain Model).
 */
data class BudgetAdjustment(
    val id: String,
    val cycleId: String,
    val oldLimitMinor: Long,
    val newLimitMinor: Long,
    val adjustedAt: String
) {
    val diffMinor: Long get() = newLimitMinor - oldLimitMinor
    val isIncrease: Boolean get() = diffMinor >= 0L

    val oldLimitFormatted: String get() = MoneyVnd.format(oldLimitMinor)
    val newLimitFormatted: String get() = MoneyVnd.format(newLimitMinor)

    val diffFormatted: String
        get() {
            val prefix = if (isIncrease) "+ " else "- "
            return "$prefix${MoneyVnd.format(kotlin.math.abs(diffMinor))}"
        }
}
