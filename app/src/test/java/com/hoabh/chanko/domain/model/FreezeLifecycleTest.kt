package com.hoabh.chanko.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class FreezeLifecycleTest {

    private val startedAt = 10_000_000L
    private val duration = TimeUnit.DAYS.toMillis(3) // 3 days
    private val lockedPrice = 500_000L

    private val freezeState = FreezeState(
        lockedPrice = lockedPrice,
        startedAtMillis = startedAt,
        durationMillis = duration,
        isRefrozen = false,
        rewardClaimed = false
    )

    @Test
    fun testInitialProperties() {
        assertEquals(lockedPrice, freezeState.lockedPrice)
        assertEquals(startedAt, freezeState.startedAtMillis)
        assertEquals(duration, freezeState.durationMillis)
        assertEquals(startedAt + duration, freezeState.endMillis)
        assertFalse(freezeState.isRefrozen)
        assertFalse(freezeState.rewardClaimed)
    }

    @Test
    fun testRemainingMillisAndCompletion() {
        val midPoint = startedAt + (duration / 2)
        assertEquals(duration / 2, freezeState.remainingMillis(midPoint))
        assertFalse(freezeState.isComplete(midPoint))

        val endPoint = startedAt + duration
        assertEquals(0L, freezeState.remainingMillis(endPoint))
        assertTrue(freezeState.isComplete(endPoint))

        val afterEnd = endPoint + 10_000L
        assertEquals(0L, freezeState.remainingMillis(afterEnd))
        assertTrue(freezeState.isComplete(afterEnd))
    }

    @Test
    fun testResolveStatusTransition() {
        // When active and time not reached -> still FREEZE_ACTIVE
        val midPoint = startedAt + (duration / 2)
        assertEquals(
            ItemFreezeStatus.FREEZE_ACTIVE,
            freezeState.resolveStatus(midPoint, ItemFreezeStatus.FREEZE_ACTIVE)
        )

        // When active and time reached -> transitions to READY_FOR_REVIEW
        val endPoint = startedAt + duration
        assertEquals(
            ItemFreezeStatus.READY_FOR_REVIEW,
            freezeState.resolveStatus(endPoint, ItemFreezeStatus.FREEZE_ACTIVE)
        )

        // Terminal or decided statuses do not change automatically
        assertEquals(
            ItemFreezeStatus.UNFROZEN_EARLY,
            freezeState.resolveStatus(endPoint, ItemFreezeStatus.UNFROZEN_EARLY)
        )
        assertEquals(
            ItemFreezeStatus.BOUGHT,
            freezeState.resolveStatus(endPoint, ItemFreezeStatus.BOUGHT)
        )
        assertEquals(
            ItemFreezeStatus.PASSED,
            freezeState.resolveStatus(endPoint, ItemFreezeStatus.PASSED)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativePriceThrows() {
        FreezeState(lockedPrice = -1L, startedAtMillis = 1000L, durationMillis = 1000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testZeroOrNegativeDurationThrows() {
        FreezeState(lockedPrice = 1000L, startedAtMillis = 1000L, durationMillis = 0L)
    }
}
