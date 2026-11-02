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
    fun `DecisionRanker AC-09 ranks Ready then Waiting then Considering then Saved and takes top 3`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved"),
                RawDecisionCandidate("W1", "Món đồ minh họa W1", 680_000L, "Waiting", remainingSeconds = 3600L),
                RawDecisionCandidate("R1", "Món đồ minh họa R1", 680_000L, "Ready"),
                RawDecisionCandidate("C1", "Món đồ minh họa C1", 680_000L, "Considering"),
                RawDecisionCandidate("R2", "Món đồ minh họa R2", 680_000L, "Ready")
            )
        )

        assertEquals(listOf("R1", "R2", "W1"), result.topDecisions.map { it.id })
        assertEquals(5, result.totalValidCount)
        assertEquals(2, result.reviewCount)
        assertEquals(1, result.waitingCount)
    }

    @Test
    fun `DecisionRanker AC-09 preserves upstream order when all items are Ready and caps at 3`() {
        val result = DecisionRanker.rankAndSelectTop(
            (1..5).map { idx ->
                RawDecisionCandidate("R$idx", "Món đồ R$idx", 680_000L, "Ready")
            }
        )

        assertEquals(listOf("R1", "R2", "R3"), result.topDecisions.map { it.id })
        assertEquals(5, result.totalValidCount)
        assertEquals(5, result.reviewCount)
    }

    @Test
    fun `DecisionRanker AC-07 and AC-08 moves expired cooling-off item to Review without changing verdict to BUY and without double counting`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate(
                    id = "W_EXPIRED",
                    title = "Bình giữ nhiệt 500ml",
                    priceMinor = 350_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = 0L,
                    verdict = DecisionVerdict.WAIT
                ),
                RawDecisionCandidate(
                    id = "W_ACTIVE",
                    title = "Bàn phím cơ",
                    priceMinor = 1_850_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = 3600L,
                    verdict = DecisionVerdict.WAIT
                )
            )
        )

        // AC-07: Chuyển sang nhóm Cần xem lại (Ready), nhãn "Vừa hết thời gian chờ", nhưng verdict giữ nguyên WAIT (không thành BUY)
        val expiredItem = result.topDecisions.first { it.id == "W_EXPIRED" }
        assertEquals(DecisionStatus.Ready, expiredItem.status)
        assertEquals("Vừa hết thời gian chờ", expiredItem.statusLabel)
        assertEquals(DecisionVerdict.WAIT, expiredItem.verdict)
        assertFalse(expiredItem.verdict == DecisionVerdict.BUY)
        assertTrue(expiredItem.isTimerExpiredLocally)

        // AC-08: Không tính trùng itemId giữa nhóm Đang chờ (Waiting) và Cần xem lại (Review)
        assertEquals(listOf("W_EXPIRED"), result.reviewItems.map { it.id })
        assertEquals(listOf("W_ACTIVE"), result.waitingItems.map { it.id })
        assertEquals(1, result.reviewCount)
        assertEquals(1, result.waitingCount)
        assertTrue(result.reviewItems.map { it.id }.intersect(result.waitingItems.map { it.id }.toSet()).isEmpty())
    }

    @Test
    fun `DecisionRanker HOME-EDGE-04 formats null price as Chua co gia and 0 price as 0 VND`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("NULL_PRICE", "Món chưa có giá", null, "Ready"),
                RawDecisionCandidate("ZERO_PRICE", "Quà tặng 0đ", 0L, "Ready")
            )
        )

        val nullPriceItem = result.topDecisions.first { it.id == "NULL_PRICE" }
        assertNull(nullPriceItem.priceMinor)
        assertEquals("Chưa có giá", nullPriceItem.priceFormatted)

        val zeroPriceItem = result.topDecisions.first { it.id == "ZERO_PRICE" }
        assertEquals(0L, zeroPriceItem.priceMinor)
        assertEquals("0 ₫", zeroPriceItem.priceFormatted)
    }

    @Test
    fun `DecisionRanker separates Skipped items into PauseJar group and excludes them from active Attention top 3`() {
        val result = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate("R1", "Tai nghe", 1_290_000L, "Ready"),
                RawDecisionCandidate("SK1", "Ốp lưng đã bỏ qua", 150_000L, "Skipped"),
                RawDecisionCandidate("SK2", "Đèn bàn đã bỏ qua", 420_000L, "Skipped")
            )
        )

        assertEquals(listOf("R1"), result.topDecisions.map { it.id })
        assertEquals(1, result.totalValidCount)
        assertEquals(2, result.skippedCount)
        assertEquals(listOf("SK1", "SK2"), result.skippedItems.map { it.id })
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
    fun `BudgetCalculator AC-11 computes remaining and Da chi percentage label`() {
        val budget = BudgetCalculator.compute(
            cycleLabel = "01/10 – 31/10 (còn 12 ngày)",
            spentMinor = 2_400_000L,
            limitMinor = 3_000_000L
        )

        assertEquals(600_000L, budget.remainingMinor)
        assertEquals(0L, budget.overMinor)
        assertEquals(80, budget.percentageInt)
        assertEquals(0.8f, budget.visualProgress, 0.001f)
        assertEquals(BudgetStateKind.Warning, budget.stateKind)
        assertEquals("2.400.000 ₫", budget.spentFormatted)
        assertEquals("3.000.000 ₫", budget.limitFormatted)
        assertEquals("600.000 ₫", budget.remainingFormatted)
        assertEquals("Đã chi 80%", budget.progressRightLabel)
    }

    @Test
    fun `BudgetCalculator AC-11 handles OverBudget with explicit over amount and clamped progress`() {
        val budget = BudgetCalculator.compute(
            cycleLabel = "01/10 – 31/10 (còn 12 ngày)",
            spentMinor = 3_600_000L,
            limitMinor = 3_000_000L
        )

        assertEquals(-600_000L, budget.remainingMinor)
        assertEquals(600_000L, budget.overMinor)
        assertEquals(120, budget.percentageInt)
        assertEquals(1.0f, budget.visualProgress, 0.001f)
        assertEquals(BudgetStateKind.OverBudget, budget.stateKind)
        assertEquals("Đã chi 120%", budget.progressRightLabel)
        assertTrue(budget.badgeText.contains("Vượt hạn mức"))
        assertTrue(budget.isOverBudget)
    }

    @Test
    fun `BudgetCalculator S07 and S08 guard against zero and null limits without dividing by zero`() {
        val s07 = BudgetCalculator.compute(
            cycleLabel = "01/10 – 31/10",
            spentMinor = 100_000L,
            limitMinor = 0L
        )
        assertEquals(-100_000L, s07.remainingMinor)
        assertNull(s07.ratio)
        assertEquals(BudgetStateKind.Zero, s07.stateKind)

        val s08 = BudgetCalculator.compute(
            cycleLabel = "01/10 – 31/10",
            spentMinor = null,
            limitMinor = null
        )
        assertNull(s08.remainingMinor)
        assertNull(s08.ratio)
        assertEquals(BudgetStateKind.Unknown, s08.stateKind)
        assertFalse(s08.spentFormatted.contains("0 ₫"))
    }

    @Test
    fun `CalendarDateResolver S19 respects injected timezone boundary`() {
        val (month, day) = CalendarDateResolver.resolve(
            nowIso = "2026-10-24T17:30:00Z",
            zoneIdString = "Asia/Ho_Chi_Minh"
        )
        assertEquals("OCT", month)
        assertEquals("25", day)
    }

    @Test
    fun `HomeCurrencyFormatter never truncates large VND amounts and handles null as Chua co gia`() {
        assertEquals("12.345.678 ₫", HomeCurrencyFormatter.formatVnd(12_345_678L))
        assertEquals("Chưa có giá", HomeCurrencyFormatter.formatNullableVnd(null))
        assertEquals("0 ₫", HomeCurrencyFormatter.formatNullableVnd(0L))
    }
}
