package com.hoabh.chanko.data.budget

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
    fun `getAvailableCycles returns current and past cycles`() = runTest {
        val cycles = repository.getAvailableCycles().first()
        assertEquals(2, cycles.size)
        assertTrue(cycles[0].isCurrent)
        assertFalse(cycles[1].isCurrent)
    }

    @Test
    fun `getBudgetSnapshot returns default snapshot for current cycle`() = runTest {
        val snapshot = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertEquals(5_000_000L, snapshot.budgetMinor)
        assertEquals(3_000_000L, snapshot.spentMinor)
        assertEquals(2_000_000L, snapshot.remainingMinor)
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
    fun `updateBudgetLimit fails when offline`() = runTest {
        repository.setNetworkOnline(false)
        val result = repository.updateBudgetLimit(repository.currentCycleId, 6_000_000L, 1)
        assertTrue(result.isFailure)
        assertEquals("Kết nối mạng để chỉnh sửa", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateBudgetLimit fails on past cycle`() = runTest {
        val result = repository.updateBudgetLimit(repository.pastCycleId, 6_000_000L, 1)
        assertTrue(result.isFailure)
        assertEquals("Chỉ chỉnh sửa trong chu kỳ hiện tại", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateBudgetLimit fails on revision conflict`() = runTest {
        val result = repository.updateBudgetLimit(repository.currentCycleId, 6_000_000L, expectedRevision = 99)
        assertTrue(result.isFailure)
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
    fun `scenario switchers update snapshots as expected`() = runTest {
        repository.setScenarioOverBudget()
        val overSnap = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertTrue(overSnap.isOverBudget)
        assertEquals(500_000L, overSnap.overMinor)

        repository.setScenarioZeroSpend()
        val zeroSnap = repository.getBudgetSnapshot(repository.currentCycleId).first()
        assertTrue(zeroSnap.isZeroSpend)
        assertEquals(0L, zeroSnap.spentMinor)
    }
}
