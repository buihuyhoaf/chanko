package com.hoabh.chanko.domain.budget.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyVndTest {

    @Test
    fun `format VND correctly formats zero and large numbers`() {
        assertEquals("0đ", MoneyVnd.format(0L))
        assertEquals("5.000.000đ", MoneyVnd.format(5_000_000L))
        assertEquals("123.456.789đ", MoneyVnd.format(123_456_789L))
        assertEquals("999.999.999.999đ", MoneyVnd.format(999_999_999_999L))
    }

    @Test
    fun `formatWithoutSymbol formats number with dots but no symbol`() {
        assertEquals("0", MoneyVnd.formatWithoutSymbol(0L))
        assertEquals("5.000.000", MoneyVnd.formatWithoutSymbol(5_000_000L))
    }

    @Test
    fun `parse valid inputs returns correct Long`() {
        assertEquals(5_000_000L, MoneyVnd.parse("5000000"))
        assertEquals(5_000_000L, MoneyVnd.parse("5.000.000"))
        assertEquals(5_000_000L, MoneyVnd.parse(" 5.000.000đ "))
        assertEquals(1L, MoneyVnd.parse("1"))
        assertEquals(999_999_999_999L, MoneyVnd.parse("999.999.999.999"))
    }

    @Test
    fun `parse invalid inputs returns null`() {
        assertNull(MoneyVnd.parse(""))
        assertNull(MoneyVnd.parse("   "))
        assertNull(MoneyVnd.parse("abc"))
        assertNull(MoneyVnd.parse("5m"))
        assertNull(MoneyVnd.parse("-5000000"))
        // Overflow > 999.999.999.999đ
        assertNull(MoneyVnd.parse("1.000.000.000.000"))
    }

    @Test
    fun `creating MoneyVnd with negative amount throws IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            MoneyVnd(-1000L)
        }
    }
}
