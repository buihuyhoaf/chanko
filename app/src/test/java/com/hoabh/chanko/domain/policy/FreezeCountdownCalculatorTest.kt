package com.hoabh.chanko.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class FreezeCountdownCalculatorTest {

    private val startedAt = 100_000_000L
    private val duration = TimeUnit.DAYS.toMillis(3) // 72 hours = 259,200,000 ms

    @Test
    fun testRemainingMillisAtStart() {
        val remaining = FreezeCountdownCalculator.calculateRemainingMillis(startedAt, duration, startedAt)
        assertEquals(duration, remaining)
        val fraction = FreezeCountdownCalculator.calculateRemainingFraction(startedAt, duration, startedAt)
        assertEquals(1.0f, fraction, 0.0001f)
        assertFalse(FreezeCountdownCalculator.isFreezeComplete(startedAt, duration, startedAt))
    }

    @Test
    fun testRemainingMillisAtMidPoint() {
        val midPoint = startedAt + (duration / 2)
        val remaining = FreezeCountdownCalculator.calculateRemainingMillis(startedAt, duration, midPoint)
        assertEquals(duration / 2, remaining)
        val fraction = FreezeCountdownCalculator.calculateRemainingFraction(startedAt, duration, midPoint)
        assertEquals(0.5f, fraction, 0.0001f)
        assertFalse(FreezeCountdownCalculator.isFreezeComplete(startedAt, duration, midPoint))
    }

    @Test
    fun testRemainingMillisAtEnd() {
        val endPoint = startedAt + duration
        val remaining = FreezeCountdownCalculator.calculateRemainingMillis(startedAt, duration, endPoint)
        assertEquals(0L, remaining)
        val fraction = FreezeCountdownCalculator.calculateRemainingFraction(startedAt, duration, endPoint)
        assertEquals(0.0f, fraction, 0.0001f)
        assertTrue(FreezeCountdownCalculator.isFreezeComplete(startedAt, duration, endPoint))
    }

    @Test
    fun testRemainingMillisPastEnd() {
        val pastEnd = startedAt + duration + 50_000_000L
        val remaining = FreezeCountdownCalculator.calculateRemainingMillis(startedAt, duration, pastEnd)
        assertEquals(0L, remaining)
        val fraction = FreezeCountdownCalculator.calculateRemainingFraction(startedAt, duration, pastEnd)
        assertEquals(0.0f, fraction, 0.0001f)
        assertTrue(FreezeCountdownCalculator.isFreezeComplete(startedAt, duration, pastEnd))
    }

    @Test
    fun testFormatRemainingTimeVi() {
        assertEquals("Đã sẵn sàng xem xét", FreezeCountdownCalculator.formatRemainingTimeVi(0L))
        assertEquals("Đã sẵn sàng xem xét", FreezeCountdownCalculator.formatRemainingTimeVi(-1000L))

        // 2 days 5 hours
        val millis2d5h = TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(5)
        assertEquals("Còn 2 ngày 5 giờ", FreezeCountdownCalculator.formatRemainingTimeVi(millis2d5h))

        // 2 days 0 hours
        val millis2d = TimeUnit.DAYS.toMillis(2)
        assertEquals("Còn 2 ngày", FreezeCountdownCalculator.formatRemainingTimeVi(millis2d))

        // 5 hours 15 minutes
        val millis5h15m = TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(15)
        assertEquals("Còn 5 giờ 15 phút", FreezeCountdownCalculator.formatRemainingTimeVi(millis5h15m))

        // 5 hours 0 minutes
        val millis5h = TimeUnit.HOURS.toMillis(5)
        assertEquals("Còn 5 giờ", FreezeCountdownCalculator.formatRemainingTimeVi(millis5h))

        // 45 minutes
        val millis45m = TimeUnit.MINUTES.toMillis(45)
        assertEquals("Còn 45 phút", FreezeCountdownCalculator.formatRemainingTimeVi(millis45m))

        // 30 seconds
        val millis30s = TimeUnit.SECONDS.toMillis(30)
        assertEquals("Còn dưới 1 phút", FreezeCountdownCalculator.formatRemainingTimeVi(millis30s))
    }
}
