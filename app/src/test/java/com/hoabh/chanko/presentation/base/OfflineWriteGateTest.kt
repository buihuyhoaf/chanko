package com.hoabh.chanko.presentation.base

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineWriteGateTest {

    private val gate = OfflineWriteGate()

    @Test
    fun testDefaultOnline() {
        assertTrue(gate.isNetworkAvailable())
    }

    @Test
    fun testRunIfOnlineExecutesWhenOnline() {
        gate.setOnline(true)
        var executed = false
        val result = gate.runIfOnline {
            executed = true
            "Success"
        }
        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
        assertTrue(executed)
    }

    @Test
    fun testRunIfOnlineBlocksWhenOfflineWithVietnameseMessage() {
        gate.setOnline(false)
        var executed = false
        val result = gate.runIfOnline {
            executed = true
            "Success"
        }
        assertFalse(result.isSuccess)
        assertFalse(executed)
        val exception = result.exceptionOrNull()
        assertTrue(exception is OfflineBlockedException)
        assertTrue(exception!!.message!!.contains("Bạn đang ngoại tuyến"))
    }

    @Test
    fun testRestoringOnlineReEnablesExecution() {
        gate.setOnline(false)
        assertFalse(gate.isNetworkAvailable())

        gate.setOnline(true)
        assertTrue(gate.isNetworkAvailable())
        val result = gate.runIfOnline { 42 }
        assertTrue(result.isSuccess)
        assertEquals(42, result.getOrNull())
    }
}
