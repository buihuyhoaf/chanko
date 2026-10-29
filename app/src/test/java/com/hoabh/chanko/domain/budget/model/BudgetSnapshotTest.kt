package com.hoabh.chanko.domain.budget.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetSnapshotTest {

    @Test
    fun `standard within-budget snapshot calculates metrics correctly`() {
        val snapshot = BudgetSnapshot(
            cycleId = "c1",
            budgetMinor = 5_000_000L,
            spentMinor = 3_000_000L
        )

        assertEquals(2_000_000L, snapshot.remainingMinor)
        assertEquals(0L, snapshot.overMinor)
        assertFalse(snapshot.isOverBudget)
        assertFalse(snapshot.isZeroSpend)
        assertEquals(0.6f, snapshot.ratio, 0.001f)
        assertEquals(60, snapshot.percentageInt)
        assertEquals(0.6f, snapshot.visualProgress, 0.001f)
        assertEquals("5.000.000đ", snapshot.budgetFormatted)
        assertEquals("3.000.000đ", snapshot.spentFormatted)
        assertEquals("2.000.000đ", snapshot.remainingFormatted)
    }

    @Test
    fun `C10 over-budget snapshot caps visualProgress at 100 percent and reports accurate over percentage`() {
        val snapshot = BudgetSnapshot(
            cycleId = "c1",
            budgetMinor = 5_000_000L,
            spentMinor = 5_500_000L
        )

        assertEquals(-500_000L, snapshot.remainingMinor)
        assertEquals(500_000L, snapshot.overMinor)
        assertTrue(snapshot.isOverBudget)
        assertEquals(1.1f, snapshot.ratio, 0.001f)
        assertEquals(110, snapshot.percentageInt)
        assertEquals(1.0f, snapshot.visualProgress, 0.001f)
        assertEquals("500.000đ", snapshot.overFormatted)
    }

    @Test
    fun `C13 zero-spend snapshot calculates metrics correctly without fake transactions`() {
        val snapshot = BudgetSnapshot(
            cycleId = "c1",
            budgetMinor = 5_000_000L,
            spentMinor = 0L
        )

        assertEquals(5_000_000L, snapshot.remainingMinor)
        assertEquals(0L, snapshot.overMinor)
        assertFalse(snapshot.isOverBudget)
        assertTrue(snapshot.isZeroSpend)
        assertEquals(0f, snapshot.ratio, 0.001f)
        assertEquals(0, snapshot.percentageInt)
        assertEquals(0f, snapshot.visualProgress, 0.001f)
    }

    @Test
    fun `zero budget guard avoids division by zero`() {
        val snapshot = BudgetSnapshot(
            cycleId = "c1",
            budgetMinor = 0L,
            spentMinor = 0L
        )

        assertEquals(0f, snapshot.ratio, 0.001f)
        assertEquals(0, snapshot.percentageInt)
        assertEquals(0f, snapshot.visualProgress, 0.001f)
    }

    @Test
    fun `percentageInt rounds half-up to nearest integer`() {
        // 1 / 3 = 33.333% -> 33
        val snap1 = BudgetSnapshot("c1", 3_000_000L, 1_000_000L)
        assertEquals(33, snap1.percentageInt)

        // 2 / 3 = 66.666% -> 67
        val snap2 = BudgetSnapshot("c1", 3_000_000L, 2_000_000L)
        assertEquals(67, snap2.percentageInt)
    }
}
