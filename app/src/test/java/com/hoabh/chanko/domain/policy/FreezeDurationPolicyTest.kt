package com.hoabh.chanko.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class FreezeDurationPolicyTest {

    @Test
    fun testUnder200kReturns1Day() {
        assertEquals(TimeUnit.DAYS.toMillis(1), FreezeDurationPolicy.calculateDurationMillis(0L))
        assertEquals(TimeUnit.DAYS.toMillis(1), FreezeDurationPolicy.calculateDurationMillis(50_000L))
        assertEquals(TimeUnit.DAYS.toMillis(1), FreezeDurationPolicy.calculateDurationMillis(199_999L))
        assertEquals(1, FreezeDurationPolicy.calculateDurationDays(199_999L))
    }

    @Test
    fun testFrom200kToUnder1mReturns3Days() {
        assertEquals(TimeUnit.DAYS.toMillis(3), FreezeDurationPolicy.calculateDurationMillis(200_000L))
        assertEquals(TimeUnit.DAYS.toMillis(3), FreezeDurationPolicy.calculateDurationMillis(500_000L))
        assertEquals(TimeUnit.DAYS.toMillis(3), FreezeDurationPolicy.calculateDurationMillis(999_999L))
        assertEquals(3, FreezeDurationPolicy.calculateDurationDays(500_000L))
    }

    @Test
    fun testFrom1mTo5mReturns7Days() {
        assertEquals(TimeUnit.DAYS.toMillis(7), FreezeDurationPolicy.calculateDurationMillis(1_000_000L))
        assertEquals(TimeUnit.DAYS.toMillis(7), FreezeDurationPolicy.calculateDurationMillis(3_500_000L))
        assertEquals(TimeUnit.DAYS.toMillis(7), FreezeDurationPolicy.calculateDurationMillis(5_000_000L))
        assertEquals(7, FreezeDurationPolicy.calculateDurationDays(3_500_000L))
    }

    @Test
    fun testAbove5mReturns14Days() {
        assertEquals(TimeUnit.DAYS.toMillis(14), FreezeDurationPolicy.calculateDurationMillis(5_000_001L))
        assertEquals(TimeUnit.DAYS.toMillis(14), FreezeDurationPolicy.calculateDurationMillis(10_000_000L))
        assertEquals(TimeUnit.DAYS.toMillis(14), FreezeDurationPolicy.calculateDurationMillis(50_000_000L))
        assertEquals(14, FreezeDurationPolicy.calculateDurationDays(10_000_000L))
    }

    @Test
    fun testNegativePriceHandledSafely() {
        assertEquals(TimeUnit.DAYS.toMillis(1), FreezeDurationPolicy.calculateDurationMillis(-100_000L))
    }
}
