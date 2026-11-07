package com.hoabh.chanko.domain.chat.util

import com.hoabh.chanko.domain.chat.model.CoolingOffStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoolingOffCalculatorTest {

    @Test
    fun calculate_whenInvalidTimestamps_returnsUnknown() {
        val info1 = MonotonicClockCalculator.calculate(
            startedAtEpochMillis = 0L,
            durationMillis = 86400000L,
            currentEpochMillis = 100000L
        )
        assertEquals(CoolingOffStatus.UNKNOWN, info1.status)
        assertEquals(0L, info1.remainingMillis)
        assertEquals(0f, info1.progress, 0.001f)

        val info2 = MonotonicClockCalculator.calculate(
            startedAtEpochMillis = 1000L,
            durationMillis = -100L,
            currentEpochMillis = 100000L
        )
        assertEquals(CoolingOffStatus.UNKNOWN, info2.status)
    }

    @Test
    fun calculate_whenActive_returnsCorrectRemainingAndProgress() {
        val start = 1_000_000L
        val duration = 100_000L // 100s
        val now = 1_025_000L   // 25s passed -> 25% progress

        val info = MonotonicClockCalculator.calculate(
            startedAtEpochMillis = start,
            durationMillis = duration,
            currentEpochMillis = now
        )

        assertEquals(CoolingOffStatus.ACTIVE, info.status)
        assertEquals(75_000L, info.remainingMillis)
        assertEquals(0.25f, info.progress, 0.001f)
    }

    @Test
    fun calculate_whenElapsed_returnsElapsedAndZeroRemaining() {
        val start = 1_000_000L
        val duration = 100_000L
        val now = 1_150_000L // past end

        val info = MonotonicClockCalculator.calculate(
            startedAtEpochMillis = start,
            durationMillis = duration,
            currentEpochMillis = now
        )

        assertEquals(CoolingOffStatus.ELAPSED, info.status)
        assertEquals(0L, info.remainingMillis)
        assertEquals(1.0f, info.progress, 0.001f)
    }

    @Test
    fun formatRemaining_formatsVietnameseStrings() {
        assertEquals("Sẵn sàng xem xét", MonotonicClockCalculator.formatRemaining(0L))
        assertEquals("Còn lại 2 ngày 5 giờ", MonotonicClockCalculator.formatRemaining(2 * 86400000L + 5 * 3600000L))
        assertEquals("Còn lại 3 giờ 15 phút", MonotonicClockCalculator.formatRemaining(3 * 3600000L + 15 * 60000L))
        assertEquals("Còn lại 45 phút", MonotonicClockCalculator.formatRemaining(45 * 60000L))
        assertEquals("Còn lại dưới 1 phút", MonotonicClockCalculator.formatRemaining(30000L))
    }
}
