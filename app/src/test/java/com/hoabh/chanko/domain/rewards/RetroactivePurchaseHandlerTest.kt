package com.hoabh.chanko.domain.rewards

import com.hoabh.chanko.domain.model.FreezeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class RetroactivePurchaseHandlerTest {

    private val startedAt = 10_000_000L
    private val duration = TimeUnit.DAYS.toMillis(3)
    private val endMillis = startedAt + duration
    private val lockedPrice = 1_000_000L

    @Test
    fun testPurchaseAfterFreezeCompleteLeavesRewardUntouched() {
        val initialBalance = ResourceBalance(hatBalance = 200L, pateBalance = 150L, currentLevel = 2)
        val state = FreezeState(
            lockedPrice = lockedPrice,
            startedAtMillis = startedAt,
            durationMillis = duration,
            rewardClaimed = true
        )
        // Purchase happened after endMillis
        val result = RetroactivePurchaseHandler.processPurchase(
            currentBalance = initialBalance,
            freezeState = state,
            actualPurchaseTimestamp = endMillis + 5000L
        )

        assertEquals(initialBalance, result.updatedBalance)
        assertEquals(0L, result.clawedBackRewardHat)
        assertEquals(0L, result.appliedUnfreezeFeeHat)
    }

    @Test
    fun testAlreadyUnfrozenEarlyDoesNotDoubleCharge() {
        val initialBalance = ResourceBalance(hatBalance = 50L, pateBalance = 30L)
        val state = FreezeState(
            lockedPrice = lockedPrice,
            startedAtMillis = startedAt,
            durationMillis = duration
        )
        val result = RetroactivePurchaseHandler.processPurchase(
            currentBalance = initialBalance,
            freezeState = state,
            actualPurchaseTimestamp = startedAt + 1000L,
            alreadyUnfrozenEarly = true
        )

        assertEquals(initialBalance, result.updatedBalance)
        assertEquals(0L, result.appliedUnfreezeFeeHat)
        assertTrue(result.auditLog.contains("không áp dụng trừ phạt lặp"))
    }

    @Test
    fun testRetroactivePurchaseClawsBackRewardAndAppliesUnfreezeFee() {
        val initialBalance = ResourceBalance(hatBalance = 300L, pateBalance = 200L, currentLevel = 2)
        val state = FreezeState(
            lockedPrice = lockedPrice, // 1m VND -> reward = 50 Hat, 25 Pate
            startedAtMillis = startedAt,
            durationMillis = duration,
            rewardClaimed = true
        )
        // Actual purchase happened at midPoint of Freeze
        val purchaseTime = startedAt + (duration / 2)
        val result = RetroactivePurchaseHandler.processPurchase(
            currentBalance = initialBalance,
            freezeState = state,
            actualPurchaseTimestamp = purchaseTime
        )

        // Clawback: 50 Hat, 25 Pate
        assertEquals(50L, result.clawedBackRewardHat)
        assertEquals(25L, result.clawedBackRewardPate)

        // Fee at midPoint: 50 Hat, 25 Pate
        assertEquals(50L, result.appliedUnfreezeFeeHat)
        assertEquals(25L, result.appliedUnfreezeFeePate)

        // Total deduction = 100 Hat, 50 Pate
        assertEquals(200L, result.updatedBalance.hatBalance)
        assertEquals(150L, result.updatedBalance.pateBalance)
        // Level never decreases
        assertEquals(2, result.updatedBalance.currentLevel)
    }
}
