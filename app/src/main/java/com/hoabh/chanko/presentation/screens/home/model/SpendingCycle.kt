package com.hoabh.chanko.presentation.screens.home.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SpendingCycle(
    val anchorDay: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val nextCycleStartDate: LocalDate
) {
    val totalDays: Int
        get() = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)).toInt()

    val dates: List<LocalDate> by lazy {
        val list = mutableListOf<LocalDate>()
        var curr = startDate
        while (!curr.isAfter(endDate)) {
            list.add(curr)
            curr = curr.plusDays(1)
        }
        list
    }

    /**
     * 7 weekday labels starting from the weekday of [startDate].
     * E.g. if startDate is Thursday, returns ["T5", "T6", "T7", "CN", "T2", "T3", "T4"].
     */
    val weekdayHeaders: List<String> by lazy {
        calculateWeekdayHeaders(startDate.dayOfWeek)
    }

    companion object {
        fun calculateWeekdayHeaders(startDayOfWeek: DayOfWeek): List<String> {
            val headers = mutableListOf<String>()
            var day = startDayOfWeek
            for (i in 0 until 7) {
                headers.add(toVietnameseShortName(day))
                day = day.plus(1)
            }
            return headers
        }

        fun toVietnameseShortName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
            DayOfWeek.MONDAY -> "T2"
            DayOfWeek.TUESDAY -> "T3"
            DayOfWeek.WEDNESDAY -> "T4"
            DayOfWeek.THURSDAY -> "T5"
            DayOfWeek.FRIDAY -> "T6"
            DayOfWeek.SATURDAY -> "T7"
            DayOfWeek.SUNDAY -> "CN"
        }

        fun create(anchorDay: Int, referenceDate: LocalDate = LocalDate.now()): SpendingCycle {
            val clampedAnchor = anchorDay.coerceIn(1, 31)
            val currentMonthLen = referenceDate.lengthOfMonth()
            val effectiveAnchor = minOf(clampedAnchor, currentMonthLen)

            val startDate: LocalDate
            val endDate: LocalDate

            if (referenceDate.dayOfMonth >= effectiveAnchor) {
                startDate = referenceDate.withDayOfMonth(effectiveAnchor)
                val nextMonth = startDate.plusMonths(1)
                val nextAnchor = minOf(clampedAnchor, nextMonth.lengthOfMonth())
                endDate = nextMonth.withDayOfMonth(nextAnchor).minusDays(1)
            } else {
                val prevMonth = referenceDate.minusMonths(1)
                val prevAnchor = minOf(clampedAnchor, prevMonth.lengthOfMonth())
                startDate = prevMonth.withDayOfMonth(prevAnchor)
                endDate = referenceDate.withDayOfMonth(effectiveAnchor).minusDays(1)
            }
            val nextCycleStartDate = endDate.plusDays(1)
            return SpendingCycle(
                anchorDay = clampedAnchor,
                startDate = startDate,
                endDate = endDate,
                nextCycleStartDate = nextCycleStartDate
            )
        }
    }
}
