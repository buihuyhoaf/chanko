package com.hoabh.chanko.domain.budget.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Ảnh chụp trạng thái ngân sách của một chu kỳ (Domain Model).
 * Tuân thủ quy chuẩn số học:
 * - remaining = budget - spent
 * - over = max(spent - budget, 0)
 * - visualProgress clamp [0f, 1f]
 * - percentageInt làm tròn gần nhất half-up theo SPEC Q3
 */
data class BudgetSnapshot(
    val cycleId: String,
    val budgetMinor: Long,
    val spentMinor: Long,
    val revision: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** Exact half-up label; Float is reserved for drawing the progress bar. */
        fun calculatePercentageInt(spentMinor: Long, budgetMinor: Long): Int {
            if (budgetMinor <= 0L) return 0
            return BigDecimal.valueOf(spentMinor).movePointRight(2)
                .divide(BigDecimal.valueOf(budgetMinor), 0, RoundingMode.HALF_UP)
                .coerceIn(BigDecimal.valueOf(Int.MIN_VALUE.toLong()), BigDecimal.valueOf(Int.MAX_VALUE.toLong()))
                .toInt()
        }
    }

    val remainingMinor: Long get() = budgetMinor - spentMinor

    val overMinor: Long get() = (spentMinor - budgetMinor).coerceAtLeast(0L)

    val isOverBudget: Boolean get() = spentMinor > budgetMinor

    val isZeroSpend: Boolean get() = spentMinor == 0L

    val ratio: Float
        get() = if (budgetMinor > 0L) spentMinor.toFloat() / budgetMinor else 0f

    val percentageInt: Int
        get() = calculatePercentageInt(spentMinor, budgetMinor)

    val visualProgress: Float
        get() = ratio.coerceIn(0f, 1f)

    val budgetFormatted: String get() = MoneyVnd.format(budgetMinor)
    val spentFormatted: String get() = MoneyVnd.format(spentMinor)
    val remainingFormatted: String get() = MoneyVnd.format(remainingMinor)
    val overFormatted: String get() = MoneyVnd.format(overMinor)
}
