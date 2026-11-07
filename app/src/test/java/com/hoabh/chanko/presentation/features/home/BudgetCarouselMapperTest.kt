package com.hoabh.chanko.presentation.features.home

import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetCycleStatus
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.TriCycleWindow
import com.hoabh.chanko.presentation.features.home.mapper.BudgetCarouselMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetCarouselMapperTest {

    @Test
    fun map_returns_three_slots_when_previous_exists() {
        val prev = BudgetCycle("p", "Tháng 09/2026", "01/09/2026", "30/09/2026", false, 1, 1, BudgetCycleStatus.Closed)
        val curr = BudgetCycle("c", "Tháng 10/2026", "01/10/2026", "31/10/2026", true, 1, 2, BudgetCycleStatus.Active)
        val next = BudgetCycle("n", "Tháng 11/2026", "01/11/2026", "30/11/2026", false, 1, 3, BudgetCycleStatus.UpcomingUnconfigured)

        val window = TriCycleWindow(previous = prev, current = curr, next = next)
        val snapshots = mapOf(
            "p" to BudgetSnapshot("p", 4_000_000L, 3_500_000L),
            "c" to BudgetSnapshot("c", 5_000_000L, 5_500_000L) // OverBudget
        )

        val carousel = BudgetCarouselMapper.map(window, snapshots)

        assertEquals(3, carousel.slots.size)
        assertTrue(carousel.hasPrevious)
        assertEquals("c", carousel.currentCycleKey)
        assertEquals("c", carousel.selectedSlotKey)

        // Previous
        val prevSlot = carousel.slots[0] as BudgetCardSlotUi.ClosedCard
        assertEquals(BudgetCardRole.Previous, prevSlot.role)
        assertEquals("Tháng 09/2026", prevSlot.cycleLabel)
        assertEquals("09", prevSlot.monthLabel)
        assertEquals(1, prevSlot.themeOrdinal)
        assertFalse(prevSlot.isOverBudget)

        // Current (Over budget)
        val currSlot = carousel.slots[1] as BudgetCardSlotUi.ActiveCard
        assertEquals(BudgetCardRole.Current, currSlot.role)
        assertEquals("Tháng 10/2026", currSlot.cycleLabel)
        assertEquals("10", currSlot.monthLabel)
        assertEquals(2, currSlot.themeOrdinal)
        assertTrue(currSlot.isOverBudget)
        assertEquals(1.0f, currSlot.clampedProgress, 0.001f)
        assertEquals("110%", currSlot.realPercentText)
        assertEquals(500_000L, currSlot.overMinor)

        // Next (Placeholder)
        val nextSlot = carousel.slots[2] as BudgetCardSlotUi.UpcomingPlaceholderCard
        assertEquals(BudgetCardRole.NextPlaceholder, nextSlot.role)
        assertEquals("Tháng 11/2026", nextSlot.cycleLabel)
        assertEquals("11", nextSlot.monthLabel)
        assertEquals(3, nextSlot.themeOrdinal)
    }

    @Test
    fun map_returns_two_slots_when_previous_is_null() {
        val curr = BudgetCycle("c", "Tháng 10/2026", "01/10/2026", "31/10/2026", true, 1, 0, BudgetCycleStatus.Active)
        val next = BudgetCycle("n", "Tháng 11/2026", "01/11/2026", "30/11/2026", false, 1, 1, BudgetCycleStatus.UpcomingConfigured, 6_000_000L)

        val window = TriCycleWindow(previous = null, current = curr, next = next)
        val snapshots = mapOf(
            "c" to BudgetSnapshot("c", 5_000_000L, 2_000_000L)
        )

        val carousel = BudgetCarouselMapper.map(window, snapshots)

        assertEquals(2, carousel.slots.size)
        assertFalse(carousel.hasPrevious)

        // Next configured
        val nextSlot = carousel.slots[1] as BudgetCardSlotUi.UpcomingConfiguredCard
        assertEquals(BudgetCardRole.NextConfigured, nextSlot.role)
        assertEquals(6_000_000L, nextSlot.limitMinor)
    }

    @Test
    fun map_returns_current_empty_when_no_budget() {
        val curr = BudgetCycle("c", "Tháng 10/2026", "01/10/2026", "31/10/2026", true, 1, 0, BudgetCycleStatus.Active)
        val next = BudgetCycle("n", "Tháng 11/2026", "01/11/2026", "30/11/2026", false, 1, 1, BudgetCycleStatus.UpcomingUnconfigured)

        val window = TriCycleWindow(previous = null, current = curr, next = next)
        val carousel = BudgetCarouselMapper.map(window, emptyMap())

        assertTrue(carousel.slots[0] is BudgetCardSlotUi.CurrentEmptyCard)
    }
}
