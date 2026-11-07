package com.hoabh.chanko.domain.rewards

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceLedgerTest {

    @Test
    fun testInitialBalanceZero() {
        val balance = ResourceBalance()
        assertEquals(0L, balance.hatBalance)
        assertEquals(0L, balance.pateBalance)
        assertEquals(1, balance.currentLevel)
        assertEquals(1, balance.highestLevelReached)
    }

    @Test
    fun testCreditIncreasesBalanceAndLevel() {
        val balance = ResourceBalance()
        // Credit 50 Hạt and 150 Pate -> hits Level 2 (requires 100 Pate)
        val credited = balance.credit(hat = 50, pate = 150)
        assertEquals(50L, credited.hatBalance)
        assertEquals(150L, credited.pateBalance)
        assertEquals(2, credited.currentLevel)
        assertEquals(2, credited.highestLevelReached)
    }

    @Test
    fun testDebitReducesBalanceWithoutGoingNegative() {
        val balance = ResourceBalance(hatBalance = 30L, pateBalance = 20L, currentLevel = 2, highestLevelReached = 2)
        // Debit 50 Hạt and 100 Pate (more than current balances)
        val debited = balance.debit(hat = 50, pate = 100)
        assertEquals(0L, debited.hatBalance)
        assertEquals(0L, debited.pateBalance)
        // Level never decreases even when Pate drops
        assertEquals(2, debited.currentLevel)
        assertEquals(2, debited.highestLevelReached)
    }

    @Test
    fun testDebitWhenZeroDoesNotThrowAndStaysZero() {
        val balance = ResourceBalance(hatBalance = 0L, pateBalance = 0L, currentLevel = 3, highestLevelReached = 3)
        val debited = balance.debit(hat = 100, pate = 100)
        assertEquals(0L, debited.hatBalance)
        assertEquals(0L, debited.pateBalance)
        assertEquals(3, debited.currentLevel)
    }

    @Test
    fun testNegativeInputToCreditIsIgnored() {
        val balance = ResourceBalance(hatBalance = 10L, pateBalance = 10L)
        val credited = balance.credit(hat = -5, pate = -5)
        assertEquals(10L, credited.hatBalance)
        assertEquals(10L, credited.pateBalance)
    }
}
