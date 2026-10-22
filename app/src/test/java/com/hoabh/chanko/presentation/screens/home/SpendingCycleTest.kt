package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.model.BudgetSummary
import com.hoabh.chanko.presentation.screens.home.model.SpendingCycle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class SpendingCycleTest {

    @Test
    fun cycleWithAnchorDay15_October2026_createsCorrectDates() {
        val referenceDate = LocalDate.of(2026, 10, 18)
        val cycle = SpendingCycle.create(anchorDay = 15, referenceDate = referenceDate)

        assertEquals(LocalDate.of(2026, 10, 15), cycle.startDate)
        assertEquals(LocalDate.of(2026, 11, 14), cycle.endDate)
        assertEquals(LocalDate.of(2026, 11, 15), cycle.nextCycleStartDate)
        assertEquals(31, cycle.totalDays)
        assertEquals(31, cycle.dates.size)
    }

    @Test
    fun cycleWithAnchorDay15_NovemberBeginning_stillInOctoberCycle() {
        val referenceDate = LocalDate.of(2026, 11, 5)
        val cycle = SpendingCycle.create(anchorDay = 15, referenceDate = referenceDate)

        assertEquals(LocalDate.of(2026, 10, 15), cycle.startDate)
        assertEquals(LocalDate.of(2026, 11, 14), cycle.endDate)
    }

    @Test
    fun cycleBoundaryDays_startAndEnd_belongToSameCycle() {
        val cycleOnStart = SpendingCycle.create(anchorDay = 15, referenceDate = LocalDate.of(2026, 10, 15))
        assertEquals(LocalDate.of(2026, 10, 15), cycleOnStart.startDate)
        assertEquals(LocalDate.of(2026, 11, 14), cycleOnStart.endDate)

        val cycleOnEnd = SpendingCycle.create(anchorDay = 15, referenceDate = LocalDate.of(2026, 11, 14))
        assertEquals(LocalDate.of(2026, 10, 15), cycleOnEnd.startDate)
        assertEquals(LocalDate.of(2026, 11, 14), cycleOnEnd.endDate)

        val cycleNextDay = SpendingCycle.create(anchorDay = 15, referenceDate = LocalDate.of(2026, 11, 15))
        assertEquals(LocalDate.of(2026, 11, 15), cycleNextDay.startDate)
        assertEquals(LocalDate.of(2026, 12, 14), cycleNextDay.endDate)
    }

    @Test
    fun weekdayHeaders_startingThursday_returnsCorrectOrder() {
        // 15/10/2026 is a Thursday
        val headers = SpendingCycle.calculateWeekdayHeaders(DayOfWeek.THURSDAY)
        val expected = listOf("T5", "T6", "T7", "CN", "T2", "T3", "T4")
        assertEquals(expected, headers)
    }

    @Test
    fun weekdayHeaders_startingMonday_returnsStandardOrder() {
        val headers = SpendingCycle.calculateWeekdayHeaders(DayOfWeek.MONDAY)
        val expected = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        assertEquals(expected, headers)
    }

    @Test
    fun budgetSummary_calculations_correctAndSafe() {
        val summary = BudgetSummary(totalBudget = 3_000_000L, totalSpent = 2_400_000L)
        assertEquals(600_000L, summary.remaining)
        assertEquals(80, summary.progressPercent)
        assertEquals(0.8f, summary.progressRatio, 0.001f)

        // Over budget case
        val overSummary = BudgetSummary(totalBudget = 3_000_000L, totalSpent = 3_500_000L)
        assertEquals(-500_000L, overSummary.remaining)
        assertEquals(116, overSummary.progressPercent)
        assertEquals(1.0f, overSummary.progressRatio, 0.001f)

        // Zero budget case (division by zero safety)
        val zeroBudget = BudgetSummary(totalBudget = 0L, totalSpent = 0L)
        assertEquals(0L, zeroBudget.remaining)
        assertEquals(0, zeroBudget.progressPercent)
        assertEquals(0f, zeroBudget.progressRatio, 0.001f)
    }
}
