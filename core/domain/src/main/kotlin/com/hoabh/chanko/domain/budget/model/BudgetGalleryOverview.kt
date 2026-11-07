package com.hoabh.chanko.domain.budget.model

import java.time.YearMonth

/**
 * Tổng quan toàn bộ các tháng trong năm được chọn cho Budget Gallery v3.
 */
data class BudgetGalleryOverview(
    val selectedYear: Int,
    val firstEstablishedMonth: YearMonth,
    val currentClockMonth: YearMonth,
    val annualConfiguredTotalMinor: Long,
    val configuredMonthsCount: Int,
    val months: List<MonthlyBudgetOverview>,
    val isOnline: Boolean,
    val lastSyncedAt: Long?
)
