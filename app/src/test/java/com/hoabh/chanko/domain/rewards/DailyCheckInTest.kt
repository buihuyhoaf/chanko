package com.hoabh.chanko.domain.rewards

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyCheckInTest {

    private val useCase = DailyCheckInUseCase(fixedHatReward = 10L, fixedPateReward = 10L)

    @Test
    fun testSuccessfulCheckInAwardsFixedRewards() {
        val initial = ResourceBalance(hatBalance = 20L, pateBalance = 20L)
        val result = useCase.execute(
            currentBalance = initial,
            hasCheckedInToday = false,
            confirmedNoImpulseBuying = true
        )

        assertTrue(result.success)
        assertEquals(10L, result.grantedHat)
        assertEquals(10L, result.grantedPate)
        assertEquals(30L, result.updatedBalance.hatBalance)
        assertEquals(30L, result.updatedBalance.pateBalance)
    }

    @Test
    fun testCheckInFailsWithoutConfirmation() {
        val initial = ResourceBalance(hatBalance = 20L, pateBalance = 20L)
        val result = useCase.execute(
            currentBalance = initial,
            hasCheckedInToday = false,
            confirmedNoImpulseBuying = false
        )

        assertFalse(result.success)
        assertEquals(0L, result.grantedHat)
        assertEquals(initial, result.updatedBalance)
    }

    @Test
    fun testDuplicateCheckInSameDayFails() {
        val initial = ResourceBalance(hatBalance = 20L, pateBalance = 20L)
        val result = useCase.execute(
            currentBalance = initial,
            hasCheckedInToday = true,
            confirmedNoImpulseBuying = true
        )

        assertFalse(result.success)
        assertEquals(0L, result.grantedHat)
        assertEquals(initial, result.updatedBalance)
    }
}
