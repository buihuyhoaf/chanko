package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.model.BudgetConflictException
import com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException
import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.model.BudgetPastCycleException
import com.hoabh.chanko.domain.budget.model.InvalidBudgetAmountException
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
    fun `addCreditCard adds card and rejects duplicate case-insensitively`() = runTest {
        val addResult = repository.addCreditCard("Thẻ Shinhan")
        assertTrue(addResult.isSuccess)

        val cards = repository.getCreditCards().first()
        assertTrue(cards.any { it.name == "Thẻ Shinhan" })

        // Trùng tên không phân biệt hoa thường
        val dupResult = repository.addCreditCard("thẻ shinhan")
        assertTrue(dupResult.isFailure)
        assertEquals("Tên thẻ đã tồn tại", dupResult.exceptionOrNull()?.message)
    }

    @Test
    fun `archiveCreditCard marks card as archived and removes from active list`() = runTest {
        val initialCards = repository.getCreditCards().first()
        val targetCard = initialCards[0]

        val archiveResult = repository.archiveCreditCard(targetCard.id)
        assertTrue(archiveResult.isSuccess)

        val remainingCards = repository.getCreditCards().first()
        assertFalse(remainingCards.any { it.id == targetCard.id })

        // Thử xóa lại thẻ đã lưu trữ
        val reArchiveResult = repository.archiveCreditCard(targetCard.id)
        assertTrue(reArchiveResult.isFailure)
        assertEquals("Thẻ đã được xóa", reArchiveResult.exceptionOrNull()?.message)
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
}
