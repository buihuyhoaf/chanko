package com.hoabh.chanko.domain.budget.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetCycleTest {

    @Test
    fun verify_budget_cycle_properties_and_theme_mapping() {
        val cycle0 = BudgetCycle(
            id = "c0",
            label = "Tháng 10/2026",
            startDate = "01/10/2026",
            endDate = "31/10/2026",
            isCurrent = true,
            anchorDay = 1,
            ordinal = 0,
            status = BudgetCycleStatus.Active
        )
        assertEquals(0, cycle0.themeIndex)
        assertEquals("01/10/2026 - 31/10/2026", cycle0.dateRangeText)
        assertEquals(BudgetCycleStatus.Active, cycle0.status)

        val cycle12 = cycle0.copy(id = "c12", ordinal = 12)
        assertEquals(0, cycle12.themeIndex)

        val cycle13 = cycle0.copy(id = "c13", ordinal = 13)
        assertEquals(1, cycle13.themeIndex)
    }

    @Test
    fun verify_tri_cycle_window_with_all_three_slots() {
        val prev = BudgetCycle("p", "Kỳ trước", "01/09/2026", "30/09/2026", false, 1, 0, BudgetCycleStatus.Closed)
        val curr = BudgetCycle("c", "Kỳ này", "01/10/2026", "31/10/2026", true, 1, 1, BudgetCycleStatus.Active)
        val next = BudgetCycle("n", "Kỳ sau", "01/11/2026", "30/11/2026", false, 1, 2, BudgetCycleStatus.UpcomingConfigured, configuredLimitMinor = 6_000_000L)

        val window = TriCycleWindow(previous = prev, current = curr, next = next)
        assertTrue(window.hasPrevious)
        assertEquals(3, window.slots.size)
        assertEquals(listOf(prev, curr, next), window.slots)
    }

    @Test
    fun verify_tri_cycle_window_without_previous_slot() {
        val curr = BudgetCycle("c", "Kỳ này", "01/10/2026", "31/10/2026", true, 1, 0, BudgetCycleStatus.Active)
        val next = BudgetCycle("n", "Kỳ sau", "01/11/2026", "30/11/2026", false, 1, 1, BudgetCycleStatus.UpcomingUnconfigured)

        val window = TriCycleWindow(previous = null, current = curr, next = next)
        assertFalse(window.hasPrevious)
        assertNull(window.previous)
        assertEquals(2, window.slots.size)
        assertEquals(listOf(curr, next), window.slots)
    }

    @Test
    fun verify_boundary_calculation_standard_month() {
        val boundary = BudgetCycleBoundaryCalculator.calculateBoundary(year = 2026, month = 10, anchorDay = 1)
        assertEquals("01/10/2026", boundary.startDateText)
        assertEquals("31/10/2026", boundary.endDateText)
    }

    @Test
    fun verify_boundary_calculation_leap_year_anchor_31() {
        // Year 2024 is a leap year (February has 29 days)
        // Jan: 31/01/2024 -> 28/02/2024 (because Feb starts 29/02/2024)
        val janBoundary = BudgetCycleBoundaryCalculator.calculateBoundary(year = 2024, month = 1, anchorDay = 31)
        assertEquals("31/01/2024", janBoundary.startDateText)
        assertEquals("28/02/2024", janBoundary.endDateText)

        // Feb: 29/02/2024 -> 30/03/2024 (because Mar starts 31/03/2024)
        val febBoundary = BudgetCycleBoundaryCalculator.calculateBoundary(year = 2024, month = 2, anchorDay = 31)
        assertEquals("29/02/2024", febBoundary.startDateText)
        assertEquals("30/03/2024", febBoundary.endDateText)
    }
}
