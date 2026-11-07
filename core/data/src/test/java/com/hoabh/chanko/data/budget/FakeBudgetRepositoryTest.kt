package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.model.BudgetConflictException
import com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException
import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.model.BudgetPastCycleException
import com.hoabh.chanko.domain.budget.model.BudgetSetupState
import com.hoabh.chanko.domain.budget.model.BudgetTemporalRole
import com.hoabh.chanko.domain.budget.model.InvalidBudgetAmountException
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

class FakeBudgetRepositoryTest {

    private lateinit var repository: FakeBudgetRepository

    @Before
    fun setUp() {
        repository = FakeBudgetRepository()
    }

    @Test
    fun `getAvailableCycles returns current, cached past, uncached past, and upcoming cycles`() = runTest {
        val cycles = repository.getAvailableCycles().first()
        assertEquals(4, cycles.size)
        assertTrue(cycles[0].isCurrent)
        assertFalse(cycles[1].isCurrent)
        assertFalse(cycles[2].isCurrent)
        assertFalse(cycles[3].isCurrent)

        val cachedIds = repository.getOfflineCachedCycleIds().first()
        assertTrue(repository.currentCycleId in cachedIds)
        assertTrue(repository.pastCycleId in cachedIds)
        assertFalse(repository.pastUncachedCycleId in cachedIds)
    }

    @Test
    fun `observeTriCycleWindow returns window with previous, current, and next slots`() = runTest {
        val window = repository.observeTriCycleWindow().first()
        assertTrue(window.hasPrevious)
        assertEquals(repository.pastCycleId, window.previous?.id)
        assertEquals("09", window.previous?.labelMonth)
        assertEquals(repository.currentCycleId, window.current.id)
        assertEquals("10", window.current.labelMonth)
        assertEquals(repository.nextCycleId, window.next.id)
        assertEquals("11", window.next.labelMonth)
        assertEquals(com.hoabh.chanko.domain.budget.model.BudgetCycleStatus.UpcomingUnconfigured, window.next.status)
    }

    @Test
    fun `observeTriCycleWindow without previous cycle omits previous slot`() = runTest {
        repository.setNoPreviousCycleForTesting()
        val window = repository.observeTriCycleWindow().first()
        assertFalse(window.hasPrevious)
        org.junit.Assert.assertNull(window.previous)
        assertEquals(2, window.slots.size)
    }

    @Test
    fun `setupNextCycleBudget saves limit for next cycle and updates status`() = runTest {
        val result = repository.setupNextCycleBudget(limitMinor = 7_000_000L)
        assertTrue(result.isSuccess)

        val window = repository.observeTriCycleWindow().first()
        assertEquals(com.hoabh.chanko.domain.budget.model.BudgetCycleStatus.UpcomingConfigured, window.next.status)
        assertEquals(7_000_000L, window.next.configuredLimitMinor)

        val snapshot = repository.getBudgetSnapshot(window.next.id).first()
        assertEquals(7_000_000L, snapshot.budgetMinor)
        assertEquals(0L, snapshot.spentMinor)
    }

    @Test
    fun `setupNextCycleBudget fails when offline or invalid amount`() = runTest {
        repository.setNetworkOnline(false)
        val offlineResult = repository.setupNextCycleBudget(limitMinor = 5_000_000L)
        assertTrue(offlineResult.isFailure)
        assertTrue(offlineResult.exceptionOrNull() is BudgetOfflineException)

        repository.setNetworkOnline(true)
        val invalidResult = repository.setupNextCycleBudget(limitMinor = 0L)
        assertTrue(invalidResult.isFailure)
        assertTrue(invalidResult.exceptionOrNull() is InvalidBudgetAmountException)
    }

    @Test
    fun `triggerRollover transitions roles and retains theme identity`() = runTest {
        // First configure next cycle
        repository.setupNextCycleBudget(limitMinor = 8_000_000L)

        // Then rollover
        val rolloverResult = repository.triggerRollover()
        assertTrue(rolloverResult.isSuccess)

        val window = repository.observeTriCycleWindow().first()
        // Old current (cycle_2026_10) is now previous with Closed status
        assertEquals(repository.currentCycleId, window.previous?.id)
        assertEquals(com.hoabh.chanko.domain.budget.model.BudgetCycleStatus.Closed, window.previous?.status)

        // Old next (cycle_2026_11) is now current with Active status and 8M budget
        assertEquals(repository.nextCycleId, window.current.id)
        assertEquals(com.hoabh.chanko.domain.budget.model.BudgetCycleStatus.Active, window.current.status)
        assertEquals(8_000_000L, window.current.configuredLimitMinor)

        val newCurrentSnapshot = repository.getBudgetSnapshot(window.current.id).first()
        assertEquals(8_000_000L, newCurrentSnapshot.budgetMinor)
        assertEquals(0L, newCurrentSnapshot.spentMinor)
    }

    @Test
    fun `getBudgetSnapshot returns default snapshot for current cycle`() = runTest {
        val snapshot = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertEquals(5_000_000L, snapshot.budgetMinor)
        assertEquals(3_000_000L, snapshot.spentMinor)
        assertEquals(2_000_000L, snapshot.remainingMinor)
    }

    @Test
    fun `getCycleOverview throws BudgetNoOfflineCacheException when offline and cycle not cached`() = runTest {
        repository.setNetworkOnline(false)
        try {
            repository.getCycleOverview(repository.pastUncachedCycleId).first()
            assertTrue("Expected BudgetNoOfflineCacheException", false)
        } catch (e: BudgetNoOfflineCacheException) {
            assertEquals("Chưa có dữ liệu để xem ngoại tuyến", e.message)
        }
    }

    @Test
    fun `updateBudgetLimit successfully updates limit, increments revision and logs adjustment`() = runTest {
        val initialSnap = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertEquals(1, initialSnap.revision)

        val result = repository.updateBudgetLimit(repository.currentCycleId, 6_000_000L, expectedRevision = 1)
        assertTrue(result.isSuccess)

        val updatedSnap = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertEquals(6_000_000L, updatedSnap.budgetMinor)
        assertEquals(2, updatedSnap.revision)

        val adjustments = repository.getAdjustments(repository.currentCycleId).first()
        assertEquals(2, adjustments.size)
        assertEquals(5_000_000L, adjustments[0].oldLimitMinor)
        assertEquals(6_000_000L, adjustments[0].newLimitMinor)

        val activities = repository.getRecentActivities(repository.currentCycleId).first()
        assertTrue(activities.any { it.title == "Tăng hạn mức ngân sách" })
    }

    @Test
    fun `updateBudgetLimit validates 1 to 999_999_999_999 range at repository level`() = runTest {
        val zeroResult = repository.updateBudgetLimit(repository.currentCycleId, 0L, 1)
        assertTrue(zeroResult.isFailure)
        assertTrue(zeroResult.exceptionOrNull() is InvalidBudgetAmountException)

        val overflowResult = repository.updateBudgetLimit(
            repository.currentCycleId,
            MoneyVnd.MAX_BUDGET_AMOUNT + 1L,
            1
        )
        assertTrue(overflowResult.isFailure)
        assertTrue(overflowResult.exceptionOrNull() is InvalidBudgetAmountException)
    }

    @Test
    fun `updateBudgetLimit fails when offline`() = runTest {
        repository.setNetworkOnline(false)
        val result = repository.updateBudgetLimit(repository.currentCycleId, 6_000_000L, 1)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BudgetOfflineException)
        assertEquals("Kết nối mạng để chỉnh sửa", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateBudgetLimit fails on past cycle`() = runTest {
        val result = repository.updateBudgetLimit(repository.pastCycleId, 6_000_000L, 1)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BudgetPastCycleException)
        assertEquals("Chỉ chỉnh sửa trong chu kỳ hiện tại", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateBudgetLimit fails on revision conflict`() = runTest {
        val result = repository.updateBudgetLimit(repository.currentCycleId, 6_000_000L, expectedRevision = 99)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BudgetConflictException)
        assertEquals("Ngân sách đã thay đổi. Tải lại để chỉnh sửa.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `scenario switchers update snapshots including combined past+offline+over and zero`() = runTest {
        repository.setScenarioOverBudget()
        val overSnap = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertTrue(overSnap.isOverBudget)
        assertEquals(500_000L, overSnap.overMinor)

        repository.setScenarioZeroSpend()
        val zeroSnap = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertTrue(zeroSnap.isZeroSpend)
        assertEquals(0L, zeroSnap.spentMinor)

        repository.setScenarioPastOfflineOver()
        assertFalse(repository.isOnlineFlow().first())
        val pastOverSnap = repository.getBudgetSnapshot(repository.pastCycleId).first()
        assertTrue(pastOverSnap.isOverBudget)
        assertEquals(500_000L, pastOverSnap.overMinor)

        repository.setScenarioPastOfflineZero()
        val pastZeroSnap = repository.getBudgetSnapshot(repository.pastCycleId).first()
        assertTrue(pastZeroSnap.isZeroSpend)
    }

    @Test
    fun `getYearGalleryOverview returns months from firstEstablishedMonth to 12 for 2026`() = runTest {
        val overview = repository.getYearGalleryOverview(2026).first()
        assertEquals(2026, overview.selectedYear)
        assertEquals(YearMonth.of(2026, 9), overview.firstEstablishedMonth)
        // AC01: First 09/2026, chọn 2026 -> chỉ 09, 10, 11, 12
        assertEquals(4, overview.months.size)
        assertEquals(listOf(9, 10, 11, 12), overview.months.map { it.yearMonth.monthValue })

        // AC05: Trước khi thiết lập 12 -> tổng 18tr, 3 tháng configured
        assertEquals(18_000_000L, overview.annualConfiguredTotalMinor)
        assertEquals(3, overview.configuredMonthsCount)

        // Month 12 is Future and Missing (no spent/remaining/usage)
        val month12 = overview.months.first { it.yearMonth.monthValue == 12 }
        assertEquals(BudgetTemporalRole.Future, month12.temporalRole)
        assertEquals(BudgetSetupState.Missing, month12.setupState)
        assertNull(month12.limitMinor)
        assertNull(month12.actualSpentMinor)
        assertNull(month12.remainingMinor)
        assertNull(month12.usagePercent)

        // AC08: Month 10 is over budget (limit 6tr, spent 6.2tr -> over 200k, usage 103%)
        val month10 = overview.months.first { it.yearMonth.monthValue == 10 }
        assertEquals(BudgetTemporalRole.Past, month10.temporalRole)
        assertEquals(BudgetSetupState.Configured, month10.setupState)
        assertEquals(6_000_000L, month10.limitMinor)
        assertEquals(6_200_000L, month10.actualSpentMinor)
        assertEquals(200_000L, month10.overMinor)
        assertEquals(-200_000L, month10.remainingMinor)
        assertTrue(month10.isOverBudget)
        assertEquals(103, month10.usagePercent)
    }

    @Test
    fun `getYearGalleryOverview returns all 12 months for 2027`() = runTest {
        val overview = repository.getYearGalleryOverview(2027).first()
        // AC02: Chọn 2027 -> 01..12
        assertEquals(12, overview.months.size)
        assertEquals((1..12).toList(), overview.months.map { it.yearMonth.monthValue })
    }

    @Test
    fun `setBudgetForMonth updates future month limit and refreshes annual total`() = runTest {
        // AC05: Sau khi lưu tháng 12 = 6tr -> tổng 24tr, 4 tháng configured
        val result = repository.setBudgetForMonth(YearMonth.of(2026, 12), 6_000_000L)
        assertTrue(result.isSuccess)

        val overview = repository.getYearGalleryOverview(2026).first()
        assertEquals(24_000_000L, overview.annualConfiguredTotalMinor)
        assertEquals(4, overview.configuredMonthsCount)

        val month12 = overview.months.first { it.yearMonth.monthValue == 12 }
        assertEquals(BudgetSetupState.Configured, month12.setupState)
        assertEquals(6_000_000L, month12.limitMinor)
        assertNull(month12.actualSpentMinor) // Future still has no actual spent
    }

    @Test
    fun `setBudgetForMonth fails when offline or invalid amount or past month`() = runTest {
        repository.setNetworkOnline(false)
        val offlineRes = repository.setBudgetForMonth(YearMonth.of(2026, 12), 6_000_000L)
        assertTrue(offlineRes.isFailure)
        assertTrue(offlineRes.exceptionOrNull() is BudgetOfflineException)

        repository.setNetworkOnline(true)
        val invalidRes = repository.setBudgetForMonth(YearMonth.of(2026, 12), 0L)
        assertTrue(invalidRes.isFailure)
        assertTrue(invalidRes.exceptionOrNull() is InvalidBudgetAmountException)

        val pastRes = repository.setBudgetForMonth(YearMonth.of(2026, 9), 6_000_000L)
        assertTrue(pastRes.isFailure)
        assertTrue(pastRes.exceptionOrNull() is BudgetPastCycleException)
    }
}
