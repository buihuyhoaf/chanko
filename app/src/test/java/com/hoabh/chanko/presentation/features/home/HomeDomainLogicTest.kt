package com.hoabh.chanko.presentation.features.home

import androidx.compose.ui.unit.dp
import com.hoabh.chanko.presentation.features.home.components.computeCenteredZigZagLayout
import com.hoabh.chanko.presentation.features.home.mapper.BudgetV2Calculator
import com.hoabh.chanko.presentation.features.home.mapper.HomeCurrencyFormatter
import com.hoabh.chanko.presentation.features.home.mapper.JourneyItemCandidate
import com.hoabh.chanko.presentation.features.home.mapper.JourneySelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bộ kiểm thử đơn vị cho Domain Logic của Home v2.0 (AC-03, AC-06..AC-12, AC-25).
 */
class HomeDomainLogicTest {

    // =========================================================================
    // 1. HomeCurrencyFormatter & Missing Price (AC-09)
    // =========================================================================

    @Test
    fun `formatNullableVnd returns Chua co gia when price is null and 0d when zero`() {
        assertEquals("Chưa có giá", HomeCurrencyFormatter.formatNullableVnd(null, compactUnit = true))
        assertEquals("0đ", HomeCurrencyFormatter.formatNullableVnd(0L, compactUnit = true))
        assertEquals("2.350.000đ", HomeCurrencyFormatter.formatVnd(2_350_000L, compactUnit = true))
        assertEquals("-650.000đ", HomeCurrencyFormatter.formatVnd(-650_000L, compactUnit = true))
    }

    // =========================================================================
    // 2. BudgetV2Calculator (AC-03, AC-06, AC-07, AC-09, AC-25)
    // =========================================================================

    @Test
    fun `AC-03 BudgetV2Calculator combines Pay Now and Credit into consistent remaining and percentage`() {
        val budget = BudgetV2Calculator.compute(
            cycleLabel = "01/11 – 30/11",
            payNowSpentMinor = 2_000_000L,
            creditSpentMinor = 650_000L,
            limitMinor = 5_000_000L
        )

        assertEquals(BudgetStateKind.Normal, budget.stateKind)
        assertEquals(2_650_000L, budget.spentMinor)
        assertEquals(5_000_000L, budget.limitMinor)
        assertEquals(2_350_000L, budget.remainingMinor)
        assertEquals(53, budget.percentageInt)
        assertEquals(0.53f, budget.visualProgress, 0.001f)
        assertEquals("2.350.000đ", budget.remainingFormatted)
        assertEquals("2.650.000đ", budget.spentFormatted)
        assertEquals("5.000.000đ", budget.limitFormatted)
        assertEquals("53%", budget.progressPercentText)
        assertNull(budget.overWarningFormatted)
    }

    @Test
    fun `AC-06 OverBudget keeps negative remaining, computes Coral over warning, and clamps progress to 0_1`() {
        val budget = BudgetV2Calculator.compute(
            cycleLabel = "01/11 – 30/11",
            payNowSpentMinor = 4_200_000L,
            creditSpentMinor = 1_450_000L,
            limitMinor = 5_000_000L
        )

        assertTrue(budget.isOverBudget)
        assertEquals(BudgetStateKind.OverBudget, budget.stateKind)
        assertEquals(-650_000L, budget.remainingMinor)
        assertEquals(650_000L, budget.overMinor)
        assertEquals("-650.000đ", budget.remainingFormatted)
        assertEquals("Vượt ngân sách 650.000đ", budget.overWarningFormatted)
        // Progress clamp về 0..1 chỉ cho thanh tiến trình
        assertEquals(1.0f, budget.visualProgress, 0.0001f)
        assertEquals("113%", budget.progressPercentText)
    }

    @Test
    fun `AC-07 NoBudget when limit is null or zero does not divide by zero or report fake 0d`() {
        val nullBudget = BudgetV2Calculator.compute(
            cycleLabel = "01/11 – 30/11",
            payNowSpentMinor = null,
            creditSpentMinor = null,
            limitMinor = null
        )
        assertFalse(nullBudget.isConfigured)
        assertEquals(BudgetStateKind.NoBudget, nullBudget.stateKind)
        assertNull(nullBudget.remainingMinor)
        assertEquals("Chưa thiết lập", nullBudget.remainingFormatted)

        val zeroLimitBudget = BudgetV2Calculator.compute(
            cycleLabel = "01/11 – 30/11",
            payNowSpentMinor = 500_000L,
            creditSpentMinor = 0L,
            limitMinor = 0L
        )
        assertFalse(zeroLimitBudget.isConfigured)
        assertEquals(BudgetStateKind.NoBudget, zeroLimitBudget.stateKind)
        assertEquals(0f, zeroLimitBudget.visualProgress, 0.0001f)
    }

    @Test
    fun `AC-09 MissingPrice marks budget note without treating missing price as zero`() {
        val budget = BudgetV2Calculator.compute(
            cycleLabel = "01/11 – 30/11",
            payNowSpentMinor = 2_650_000L,
            creditSpentMinor = 0L,
            limitMinor = 5_000_000L,
            hasMissingPriceItems = true
        )

        assertTrue(budget.hasMissingPriceItems)
        assertEquals("Chưa đủ dữ liệu giá", budget.missingPriceNote)
    }

    // =========================================================================
    // 3. JourneySelector (AC-10, AC-11, AC-12, AC-14, AC-25)
    // =========================================================================

    @Test
    fun `AC-10 Filtering excludes Archived items BEFORE taking top 5 newest`() {
        val candidates = listOf(
            JourneyItemCandidate("arch_newest", "Món lưu trữ mới nhất", 500_000L, "Saved", 900L, isArchived = true),
            JourneyItemCandidate("item_1", "Món 1", 500_000L, "Saved", 800L),
            JourneyItemCandidate("arch_second", "Món lưu trữ thứ hai", 500_000L, "Waiting", 750L, isArchived = true),
            JourneyItemCandidate("item_2", "Món 2", 500_000L, "Waiting", 700L),
            JourneyItemCandidate("item_3", "Món 3", 500_000L, "Ready", 600L),
            JourneyItemCandidate("item_4", "Món 4", 500_000L, "Considering", 500L),
            JourneyItemCandidate("item_5", "Món 5", 500_000L, "Purchased", 400L),
            JourneyItemCandidate("item_6", "Món 6", 500_000L, "Skipped", 300L)
        )

        val journey = JourneySelector.selectRecentJourney(candidates)

        assertEquals(5, journey.nodes.size)
        assertEquals(6, journey.totalActiveAndHistoryCount)
        assertEquals(
            listOf("item_5", "item_4", "item_3", "item_2", "item_1"),
            journey.nodes.map { it.itemId }
        )
    }

    @Test
    fun `AC-11 Stable ordering does not reorder by status and tie-breaks equal createdAt by itemId ASC`() {
        val sameTimestamp = 1_762_000_000_000L
        val candidates = listOf(
            JourneyItemCandidate("item_c", "Món C", 300_000L, "Ready", sameTimestamp),
            JourneyItemCandidate("item_a", "Món A", 300_000L, "Skipped", sameTimestamp),
            JourneyItemCandidate("item_b", "Món B", 300_000L, "Saved", sameTimestamp)
        )

        val initialJourney = JourneySelector.selectRecentJourney(candidates)
        assertEquals(listOf("item_a", "item_b", "item_c"), initialJourney.nodes.map { it.itemId })

        // Đổi status của item_a từ Skipped -> Ready và item_c từ Ready -> Waiting: thứ tự tuyệt đối không đổi
        val statusChangedCandidates = candidates.map {
            when (it.itemId) {
                "item_a" -> it.copy(statusRaw = "Ready")
                "item_c" -> it.copy(statusRaw = "Waiting")
                else -> it
            }
        }
        val afterStatusChange = JourneySelector.selectRecentJourney(statusChangedCandidates)
        assertEquals(listOf("item_a", "item_b", "item_c"), afterStatusChange.nodes.map { it.itemId })
    }

    @Test
    fun `AC-12 Count returns min(n, 5) nodes and n-1 connectors (0 connectors when n is 0 or 1)`() {
        for (n in 0..6) {
            val candidates = (1..n).map { idx ->
                JourneyItemCandidate(
                    itemId = "id_$idx",
                    title = "Món $idx",
                    priceMinor = 100_000L * idx,
                    statusRaw = "Saved",
                    createdAtEpochMillis = 1000L * idx
                )
            }
            val journey = JourneySelector.selectRecentJourney(candidates)
            val expectedNodes = n.coerceAtMost(5)
            val expectedConnectors = (expectedNodes - 1).coerceAtLeast(0)
            assertEquals(expectedNodes, journey.nodes.size)
            assertEquals(expectedConnectors, journey.connectorCount)
        }
    }

    @Test
    fun `AC-14 Valid statuses map accurately to 4 Cooling-off JourneyNodeStatus`() {
        val statuses = listOf("CoolingOff", "Ready", "Purchased", "Skipped")
        val candidates = statuses.mapIndexed { index, status ->
            JourneyItemCandidate(
                itemId = "item_$index",
                title = "Món $status",
                priceMinor = 200_000L,
                statusRaw = status,
                createdAtEpochMillis = 10_000L + index
            )
        }

        val journey = JourneySelector.selectRecentJourney(candidates, maxNodes = 4)
        assertEquals(
            listOf(
                JourneyNodeStatus.CoolingOff,
                JourneyNodeStatus.Ready,
                JourneyNodeStatus.Purchased,
                JourneyNodeStatus.Skipped
            ),
            journey.nodes.map { it.status }
        )
    }

    @Test
    fun `AC-14 legacy statuses map to CoolingOff`() {
        assertEquals(JourneyNodeStatus.CoolingOff, JourneySelector.parseStatus("Saved"))
        assertEquals(JourneyNodeStatus.CoolingOff, JourneySelector.parseStatus("Considering"))
        assertEquals(JourneyNodeStatus.CoolingOff, JourneySelector.parseStatus("Waiting"))
        assertEquals(JourneyNodeStatus.CoolingOff, JourneySelector.parseStatus("FreezeActive"))
    }

    @Test
    fun `AC-25 Cooling-off expired transitions Waiting to Ready without auto-converting verdict to BUY`() {
        val expiredCandidate = JourneyItemCandidate(
            itemId = "w_exp",
            title = "Giày chạy bộ",
            priceMinor = 1_500_000L,
            statusRaw = "Waiting",
            createdAtEpochMillis = 5000L,
            remainingSeconds = 0L,
            verdict = DecisionVerdict.BUY // Kể cả đầu vào lệch BUY thì khi hết cooling-off cũng không tự BUY
        )

        val journey = JourneySelector.selectRecentJourney(listOf(expiredCandidate))
        val node = journey.nodes.single()

        assertEquals(JourneyNodeStatus.Ready, node.status)
        assertTrue(node.isCoolingOffJustExpired)
        assertEquals(DecisionVerdict.WAIT, node.verdict)
    }

    @Test
    fun `Centered ZigZag layout keeps nodes centered and side mascots non-overlapping across 320dp to 412dp`() {
        listOf(296.dp, 360.dp, 380.dp).forEach { containerWidth ->
            val layout = computeCenteredZigZagLayout(maxWidth = containerWidth, nodeCount = 5)
            val nodeRadius = layout.nodeDiameter / 2

            // Đường trung tâm của cặp ziczac phải trùng với chính giữa màn hình (containerWidth / 2)
            val minCenterX = layout.nodeCenters.minOf { it.first }
            val maxCenterX = layout.nodeCenters.maxOf { it.first }
            val midPoint = (minCenterX + maxCenterX) / 2
            assertEquals(containerWidth.value / 2f, midPoint.value, 0.01f)

            // Mèo bên trái (0..mascotSize) và mèo bên phải (containerWidth - mascotSize..containerWidth)
            // không được chồng lấn vào biên ngoài của các node ziczac ở giữa
            val leftmostNodeEdge = minCenterX - nodeRadius
            val rightmostNodeEdge = maxCenterX + nodeRadius
            assertTrue(layout.mascotSize <= leftmostNodeEdge)
            assertTrue(containerWidth - layout.mascotSize >= rightmostNodeEdge)
        }
    }

    // =========================================================================
    // 6. Sticky Collapsing Header Helper
    // =========================================================================

    @Test
    fun `calculateCollapseFraction clamps fraction between 0 and 1 correctly`() {
        val maxScroll = 100f
        assertEquals(0f, com.hoabh.chanko.presentation.features.home.components.calculateCollapseFraction(0, maxScroll), 0.001f)
        assertEquals(0.5f, com.hoabh.chanko.presentation.features.home.components.calculateCollapseFraction(50, maxScroll), 0.001f)
        assertEquals(1f, com.hoabh.chanko.presentation.features.home.components.calculateCollapseFraction(100, maxScroll), 0.001f)
        assertEquals(1f, com.hoabh.chanko.presentation.features.home.components.calculateCollapseFraction(250, maxScroll), 0.001f)
        assertEquals(0f, com.hoabh.chanko.presentation.features.home.components.calculateCollapseFraction(-10, maxScroll), 0.001f)
        assertEquals(0f, com.hoabh.chanko.presentation.features.home.components.calculateCollapseFraction(50, 0f), 0.001f)
    }
}
