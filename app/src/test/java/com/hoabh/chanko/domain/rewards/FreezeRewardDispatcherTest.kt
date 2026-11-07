package com.hoabh.chanko.domain.rewards

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.FreezeState
import com.hoabh.chanko.domain.model.ItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.TimeUnit

class FreezeRewardDispatcherTest {

    private val startedAt = 1000L
    private val duration = TimeUnit.DAYS.toMillis(3)

    private fun mockCandidate(id: Int, price: Long, isRefrozen: Boolean = false, rewardClaimed: Boolean = false): Pair<CooldownItem, FreezeState> {
        val item = CooldownItem(
            id = id,
            name = "Item $id",
            price = price,
            addedTimestamp = startedAt,
            cooldownDurationMillis = duration,
            reason = "Reason",
            status = ItemStatus.COOLING_DOWN
        )
        val state = FreezeState(
            lockedPrice = price,
            startedAtMillis = startedAt,
            durationMillis = duration,
            isRefrozen = isRefrozen,
            rewardClaimed = rewardClaimed
        )
        return Pair(item, state)
    }

    @Test
    fun testSelectsHighestRewardWhenMultipleCompleteSameDay() {
        val initialBalance = ResourceBalance(hatBalance = 0L, pateBalance = 0L)
        val candidate1 = mockCandidate(id = 1, price = 500_000L) // reward: 25 Hat, 12 Pate
        val candidate2 = mockCandidate(id = 2, price = 2_000_000L) // reward: 100 Hat, 50 Pate
        val candidate3 = mockCandidate(id = 3, price = 1_000_000L) // reward: 50 Hat, 25 Pate

        val result = FreezeRewardDispatcher.dispatchDailyFreezeRewards(
            candidates = listOf(candidate1, candidate2, candidate3),
            currentBalance = initialBalance,
            alreadyClaimedFreezeRewardToday = false
        )

        // Selects candidate2 with 2_000_000 VND
        assertEquals(2, result.rewardedItemId)
        assertEquals(100L, result.grantedHat)
        assertEquals(50L, result.grantedPate)
        assertEquals(100L, result.updatedBalance.hatBalance)
        assertEquals(50L, result.updatedBalance.pateBalance)
        assertEquals(2, result.skippedItemsCount)
    }

    @Test
    fun testIgnoresRefrozenItemForReward() {
        val initialBalance = ResourceBalance(hatBalance = 0L, pateBalance = 0L)
        // Item 1 is expensive but isRefrozen = true
        val candidate1 = mockCandidate(id = 1, price = 5_000_000L, isRefrozen = true)
        val candidate2 = mockCandidate(id = 2, price = 500_000L, isRefrozen = false)

        val result = FreezeRewardDispatcher.dispatchDailyFreezeRewards(
            candidates = listOf(candidate1, candidate2),
            currentBalance = initialBalance,
            alreadyClaimedFreezeRewardToday = false
        )

        // Candidate 2 is selected because Candidate 1 was refrozen
        assertEquals(2, result.rewardedItemId)
        assertEquals(25L, result.grantedHat)
    }

    @Test
    fun testWhenAlreadyClaimedTodayNoRewardIsGranted() {
        val initialBalance = ResourceBalance(hatBalance = 50L, pateBalance = 50L)
        val candidate = mockCandidate(id = 1, price = 1_000_000L)

        val result = FreezeRewardDispatcher.dispatchDailyFreezeRewards(
            candidates = listOf(candidate),
            currentBalance = initialBalance,
            alreadyClaimedFreezeRewardToday = true
        )

        assertNull(result.rewardedItemId)
        assertEquals(0L, result.grantedHat)
        assertEquals(initialBalance, result.updatedBalance)
    }
}
