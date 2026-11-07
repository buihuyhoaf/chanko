package com.hoabh.chanko.domain.budget.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetCalculationTest {

    @Test
    fun verify_total_spent_aggregates_pay_now_and_credit() {
        val total = BudgetCalculation.calculateTotalSpent(
            payNowSpentMinor = 2_000_000L,
            creditSpentMinor = 1_500_000L
        )
        assertEquals(3_500_000L, total)
    }

    @Test
    fun verify_metrics_when_under_budget() {
        val metrics = BudgetCalculation.calculateMetrics(
            limitMinor = 5_000_000L,
            spentMinor = 2_500_000L
        )
        assertFalse(metrics.isOverBudget)
        assertEquals(2_500_000L, metrics.remainingMinor)
        assertNull(metrics.overMinor)
        assertEquals(0.5f, metrics.clampedProgress, 0.001f)
        assertEquals(50, metrics.realPercentInt)
        assertEquals("50%", metrics.realPercentText)
    }

    @Test
    fun verify_metrics_when_over_budget_exact_example_from_spec() {
        // Spec example: Limit 5.000.000đ and Spent 5.500.000đ -> clamped 100%, real 110%, over 500.000đ
        val metrics = BudgetCalculation.calculateMetrics(
            limitMinor = 5_000_000L,
            spentMinor = 5_500_000L
        )
        assertTrue(metrics.isOverBudget)
        assertEquals(-500_000L, metrics.remainingMinor)
        assertEquals(500_000L, metrics.overMinor)
        assertEquals(1.0f, metrics.clampedProgress, 0.001f)
        assertEquals(110, metrics.realPercentInt)
        assertEquals("110%", metrics.realPercentText)
    }

    @Test
    fun verify_metrics_when_null_or_zero_budget() {
        val metrics = BudgetCalculation.calculateMetrics(
            limitMinor = null,
            spentMinor = 1_000_000L
        )
        assertNull(metrics.realPercentInt)
        assertNull(metrics.realPercentText)
    }

    @Test
    fun verify_rollover_preserves_identity_and_shifts_roles() {
        val oldPrev = BudgetCycle("p", "Kỳ 09", "01/09", "30/09", false, 1, 0, BudgetCycleStatus.Closed)
        val oldCurr = BudgetCycle("c", "Kỳ 10", "01/10", "31/10", true, 1, 1, BudgetCycleStatus.Active)
        val oldNext = BudgetCycle("n", "Kỳ 11", "01/11", "30/11", false, 1, 2, BudgetCycleStatus.UpcomingConfigured, 6_000_000L)
        val window = TriCycleWindow(previous = oldPrev, current = oldCurr, next = oldNext)

        val newNext = BudgetCycle("n2", "Kỳ 12", "01/12", "31/12", false, 1, 3, BudgetCycleStatus.UpcomingUnconfigured)

        val rolledWindow = BudgetCalculation.performRollover(window, newNext)

        // Old current becomes closed previous with ordinal 1 (preserves theme)
        assertEquals("c", rolledWindow.previous?.id)
        assertEquals(1, rolledWindow.previous?.ordinal)
        assertEquals(BudgetCycleStatus.Closed, rolledWindow.previous?.status)
        assertFalse(rolledWindow.previous?.isCurrent ?: true)

        // Old next becomes active current with ordinal 2 (preserves theme and configured limit)
        assertEquals("n", rolledWindow.current.id)
        assertEquals(2, rolledWindow.current.ordinal)
        assertEquals(BudgetCycleStatus.Active, rolledWindow.current.status)
        assertTrue(rolledWindow.current.isCurrent)
        assertEquals(6_000_000L, rolledWindow.current.configuredLimitMinor)

        // New next is unconfigured upcoming with ordinal 3
        assertEquals("n2", rolledWindow.next.id)
        assertEquals(3, rolledWindow.next.ordinal)
        assertEquals(BudgetCycleStatus.UpcomingUnconfigured, rolledWindow.next.status)
    }

    @Test
    fun verify_annual_configured_total_sums_only_valid_configured_months() {
        val limits = listOf(6_000_000L, 6_000_000L, 6_000_000L, null, 0L)
        val total = BudgetCalculation.calculateAnnualConfiguredTotal(limits)
        assertEquals(18_000_000L, total)
    }
}
