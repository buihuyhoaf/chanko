package com.hoabh.chanko.domain.rewards

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class UnfreezeCostCalculatorTest {

    private val price = 1_000_000L // 1 triệu VNĐ -> baseHat = 100, basePate = 50
    private val totalDuration = TimeUnit.DAYS.toMillis(3)

    @Test
    fun testZeroCostWhenFreezeCompleted() {
        val fee = UnfreezeCostCalculator.calculateFee(
            lockedPriceVnd = price,
            remainingMillis = 0L,
            totalDurationMillis = totalDuration
        )
        assertEquals(0L, fee.hatFee)
        assertEquals(0L, fee.pateFee)
    }

    @Test
    fun testFullCostWhenUnfreezingAtStart() {
        val fee = UnfreezeCostCalculator.calculateFee(
            lockedPriceVnd = price,
            remainingMillis = totalDuration,
            totalDurationMillis = totalDuration
        )
        assertEquals(100L, fee.hatFee)
        assertEquals(50L, fee.pateFee)
    }

    @Test
    fun testHalfCostWhenUnfreezingAtMidPoint() {
        val fee = UnfreezeCostCalculator.calculateFee(
            lockedPriceVnd = price,
            remainingMillis = totalDuration / 2,
            totalDurationMillis = totalDuration
        )
        assertEquals(50L, fee.hatFee)
        assertEquals(25L, fee.pateFee)
    }

    @Test
    fun testHigherPriceResultsInHigherCost() {
        val feeLow = UnfreezeCostCalculator.calculateFee(500_000L, totalDuration, totalDuration)
        val feeHigh = UnfreezeCostCalculator.calculateFee(5_000_000L, totalDuration, totalDuration)
        assertEquals(50L, feeLow.hatFee)
        assertEquals(500L, feeHigh.hatFee)
    }
}
