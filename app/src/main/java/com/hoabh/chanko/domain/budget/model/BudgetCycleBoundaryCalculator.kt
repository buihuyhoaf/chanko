package com.hoabh.chanko.domain.budget.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Tính toán ranh giới chu kỳ ngân sách theo ngày neo (1..31) (Home v2.2 Spec Mục 7).
 * Khoảng nghiệp vụ: [start, nextStart). Nhãn hiển thị endDate = nextStart - 1 ngày.
 */
object BudgetCycleBoundaryCalculator {

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    data class CycleBoundary(
        val startDateText: String,
        val endDateText: String,
        val startLocalDate: LocalDate,
        val endInclusiveLocalDate: LocalDate
    )

    fun calculateBoundary(year: Int, month: Int, anchorDay: Int): CycleBoundary {
        require(anchorDay in 1..31) { "anchorDay must be in 1..31, found $anchorDay" }
        require(month in 1..12) { "month must be in 1..12, found $month" }

        val firstOfThisMonth = LocalDate.of(year, month, 1)
        val daysInThisMonth = firstOfThisMonth.lengthOfMonth()
        val startDay = min(anchorDay, daysInThisMonth)
        val startDate = LocalDate.of(year, month, startDay)

        val firstOfNextMonth = firstOfThisMonth.plusMonths(1)
        val daysInNextMonth = firstOfNextMonth.lengthOfMonth()
        val nextStartDay = min(anchorDay, daysInNextMonth)
        val nextStartDate = LocalDate.of(firstOfNextMonth.year, firstOfNextMonth.monthValue, nextStartDay)

        val endInclusiveDate = nextStartDate.minusDays(1)

        return CycleBoundary(
            startDateText = startDate.format(formatter),
            endDateText = endInclusiveDate.format(formatter),
            startLocalDate = startDate,
            endInclusiveLocalDate = endInclusiveDate
        )
    }
}
