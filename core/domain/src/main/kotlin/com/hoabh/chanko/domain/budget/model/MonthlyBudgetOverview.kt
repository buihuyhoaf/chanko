package com.hoabh.chanko.domain.budget.model

import java.time.YearMonth

/**
 * Dữ liệu tổng quan của một tháng dùng cho thẻ nhỏ trên lưới 2 cột Budget Gallery (v3).
 */
data class MonthlyBudgetOverview(
    val yearMonth: YearMonth,
    val temporalRole: BudgetTemporalRole,
    val setupState: BudgetSetupState,
    val limitMinor: Long?,
    val actualSpentMinor: Long?,
    val remainingMinor: Long?,
    val overMinor: Long?,
    val usagePercent: Int?,
    val isOverBudget: Boolean = false,
    val themeMonth: Int = yearMonth.monthValue,
    val canEdit: Boolean = false,
    val themeOrdinal: Int = yearMonth.monthValue - 1
) {
    val isConfigured: Boolean
        get() = setupState != BudgetSetupState.Missing && limitMinor != null && limitMinor > 0L

    val monthNumberText: String
        get() = "%02d".format(yearMonth.monthValue)

    val yearText: String
        get() = yearMonth.year.toString()
}
