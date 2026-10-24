package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.domain.BudgetCalculator
import com.hoabh.chanko.presentation.screens.home.domain.CalendarDateResolver
import com.hoabh.chanko.presentation.screens.home.domain.DecisionRanker
import com.hoabh.chanko.presentation.screens.home.domain.HomeCurrencyFormatter
import com.hoabh.chanko.presentation.screens.home.domain.RawDecisionCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDomainLogicTest {

    @Test
    fun `DecisionRanker S01 ranks Ready then Waiting then Considering then Saved and takes top 3`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved"),
                RawDecisionCandidate("W1", "Món đồ minh họa W1", 680_000L, "Waiting"),
                RawDecisionCandidate("R1", "Món đồ minh họa R1", 680_000L, "Ready"),
                RawDecisionCandidate("C1", "Món đồ minh họa C1", 680_000L, "Considering"),
                RawDecisionCandidate("R2", "Món đồ minh họa R2", 680_000L, "Ready")
            )
        )

        assertEquals(listOf("R1", "R2", "W1"), result.topDecisions.map { it.id })
        assertEquals(5, result.totalValidCount)
    }

    @Test
    fun `DecisionRanker S02 preserves upstream order when all items are Ready and caps at 3`() {
        val result = DecisionRanker.rankAndSelectTop(
            (1..5).map { idx ->
                RawDecisionCandidate("R$idx", "Món đồ R$idx", 680_000L, "Ready")
            }
        )

        assertEquals(listOf("R1", "R2", "R3"), result.topDecisions.map { it.id })
        assertEquals(5, result.totalValidCount)
    }

    @Test
    fun `DecisionRanker S03 preserves order within Waiting group after Ready`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("S1", "Saved 1", 680_000L, "Saved"),
                RawDecisionCandidate("C1", "Considering 1", 680_000L, "Considering"),
                RawDecisionCandidate("W1", "Waiting 1", 680_000L, "Waiting"),
                RawDecisionCandidate("W2", "Waiting 2", 680_000L, "Waiting"),
                RawDecisionCandidate("R1", "Ready 1", 680_000L, "Ready")
            )
        )

        assertEquals(listOf("R1", "W1", "W2"), result.topDecisions.map { it.id })
    }

    @Test
    fun `DecisionRanker S04 and S20 handle Saved only and Considering plus Saved`() {
        val s04 = DecisionRanker.rankAndSelectTop(
            listOf(RawDecisionCandidate("S1", "Saved 1", 680_000L, "Saved"))
        )
        assertEquals(listOf("S1"), s04.topDecisions.map { it.id })

        val s20 = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("S1", "Saved 1", 680_000L, "Saved"),
                RawDecisionCandidate("C1", "Considering 1", 680_000L, "Considering"),
                RawDecisionCandidate("C2", "Considering 2", 680_000L, "Considering"),
                RawDecisionCandidate("S2", "Saved 2", 680_000L, "Saved")
            )
        )
        assertEquals(listOf("C1", "C2", "S1"), s20.topDecisions.map { it.id })
    }

    @Test
    fun `DecisionRanker S12 keeps Waiting status when local countdown timer expires`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate(
                    id = "W1",
                    title = "Món đồ minh họa W1",
                    priceMinor = 680_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = -5L
                )
            )
        )

        val item = result.topDecisions.single()
        assertEquals(DecisionStatus.Waiting, item.status)
        assertTrue(item.isTimerExpiredLocally)
        assertFalse(item.secondaryText.orEmpty().contains("-"))
    }

    @Test
    fun `DecisionRanker S18 deduplicates by ID keeping first occurrence and drops unknown statuses`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("X", "Món đồ minh họa X", 680_000L, "Ready"),
                RawDecisionCandidate("X", "Món đồ minh họa X bản sao", 680_000L, "Waiting"),
                RawDecisionCandidate("U", "Món đồ minh họa U", 680_000L, "Unknown"),
                RawDecisionCandidate("   ", "ID rỗng", 680_000L, "Ready"),
                RawDecisionCandidate("V1", "   ", 680_000L, "Ready")
            )
        )

        assertEquals(listOf("X"), result.topDecisions.map { it.id })
        assertEquals(DecisionStatus.Ready, result.topDecisions.single().status)
        assertEquals(1, result.totalValidCount)
    }

    @Test
    fun `BudgetCalculator S01 and S21 compute 80 percent Warning state`() {
        val budget = BudgetCalculator.compute(
            cycleLabel = "Ngân sách tháng 10 – 11/2026",
            spentMinor = 2_400_000L,
            limitMinor = 3_000_000L
        )

        assertEquals(600_000L, budget.remainingMinor)
        assertEquals(80, budget.percentageInt)
        assertEquals(0.8f, budget.visualProgress, 0.001f)
        assertEquals(BudgetStateKind.Warning, budget.stateKind)
        assertEquals("2.400.000 ₫", budget.spentFormatted)
        assertEquals("3.000.000 ₫", budget.limitFormatted)
        assertEquals("600.000 ₫", budget.remainingFormatted)
        assertEquals("Đã tiêu 80%", budget.badgeText)
        assertEquals("80%", budget.progressRightLabel)
    }

    @Test
    fun `BudgetCalculator S06 handles OverBudget with negative remaining and clamped progress`() {
        val budget = BudgetCalculator.compute(
            cycleLabel = "Ngân sách tháng 10 – 11/2026",
            spentMinor = 3_600_000L,
            limitMinor = 3_000_000L
        )

        assertEquals(-600_000L, budget.remainingMinor)
        assertEquals(120, budget.percentageInt)
        assertEquals(1.0f, budget.visualProgress, 0.001f)
        assertEquals(BudgetStateKind.OverBudget, budget.stateKind)
        assertEquals("-600.000 ₫", budget.remainingFormatted)
        assertEquals("120%", budget.progressRightLabel)
        assertTrue(budget.isOverBudget)
    }

    @Test
    fun `BudgetCalculator S07 does not divide by zero when limitMinor is 0`() {
        val budget = BudgetCalculator.compute(
            cycleLabel = "Ngân sách tháng 10 – 11/2026",
            spentMinor = 100_000L,
            limitMinor = 0L
        )

        assertEquals(-100_000L, budget.remainingMinor)
        assertNull(budget.ratio)
        assertNull(budget.percentageInt)
        assertEquals(0f, budget.visualProgress, 0.001f)
        assertEquals(BudgetStateKind.Zero, budget.stateKind)
    }

    @Test
    fun `BudgetCalculator S08 does not fabricate 0 VND when budget is unknown`() {
        val budget = BudgetCalculator.compute(
            cycleLabel = "Ngân sách tháng 10 – 11/2026",
            spentMinor = null,
            limitMinor = null
        )

        assertNull(budget.remainingMinor)
        assertNull(budget.ratio)
        assertEquals(BudgetStateKind.Unknown, budget.stateKind)
        assertFalse(budget.spentFormatted.contains("0 ₫"))
        assertFalse(budget.remainingFormatted.contains("0 ₫"))
    }

    @Test
    fun `CalendarDateResolver S19 respects injected timezone boundary`() {
        // 2026-10-24T17:30:00Z is 2026-10-25T00:30:00+07:00 in Asia/Ho_Chi_Minh
        val (month, day) = CalendarDateResolver.resolve(
            nowIso = "2026-10-24T17:30:00Z",
            zoneIdString = "Asia/Ho_Chi_Minh"
        )
        assertEquals("OCT", month)
        assertEquals("25", day)
    }

    @Test
    fun `HomeCurrencyFormatter never truncates large VND amounts`() {
        assertEquals("12.345.678 ₫", HomeCurrencyFormatter.formatVnd(12_345_678L))
        assertEquals("-600.000 ₫", HomeCurrencyFormatter.formatVnd(-600_000L))
    }
}
