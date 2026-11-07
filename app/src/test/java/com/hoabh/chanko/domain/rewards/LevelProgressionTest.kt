package com.hoabh.chanko.domain.rewards

import org.junit.Assert.assertEquals
import org.junit.Test

class LevelProgressionTest {

    @Test
    fun testLevelCalculation() {
        assertEquals(1, DefaultLevelProgressionTable.calculateLevel(0L))
        assertEquals(1, DefaultLevelProgressionTable.calculateLevel(99L))
        assertEquals(2, DefaultLevelProgressionTable.calculateLevel(100L))
        assertEquals(2, DefaultLevelProgressionTable.calculateLevel(249L))
        assertEquals(3, DefaultLevelProgressionTable.calculateLevel(250L))
        assertEquals(4, DefaultLevelProgressionTable.calculateLevel(500L))
        assertEquals(10, DefaultLevelProgressionTable.calculateLevel(10_000L))
    }

    @Test
    fun testProgressFraction() {
        // Level 1: 0 to 100 Pate
        assertEquals(0.0f, DefaultLevelProgressionTable.calculateProgressFraction(0L), 0.001f)
        assertEquals(0.5f, DefaultLevelProgressionTable.calculateProgressFraction(50L), 0.001f)

        // Level 2: 100 to 250 Pate (needed = 150)
        // At 175 Pate -> currentInLevel = 75 -> 75 / 150 = 0.5f
        assertEquals(0.5f, DefaultLevelProgressionTable.calculateProgressFraction(175L), 0.001f)
    }

    @Test
    fun testMaxLevelProgressClampsTo1() {
        assertEquals(1.0f, DefaultLevelProgressionTable.calculateProgressFraction(7_000L), 0.001f)
    }
}
