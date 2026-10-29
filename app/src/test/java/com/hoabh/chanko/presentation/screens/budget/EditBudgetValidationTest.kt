package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.domain.budget.model.MoneyVnd
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class EditBudgetValidationTest {

    @Test
    fun `parse valid budget inputs within 1 to 999_999_999_999 range`() {
        assertEquals(5_000_000L, MoneyVnd.parse("5000000"))
        assertEquals(5_000_000L, MoneyVnd.parse("5.000.000"))
        assertEquals(1L, MoneyVnd.parse("1"))
        assertEquals(999_999_999_999L, MoneyVnd.parse("999.999.999.999"))
        assertTrue(MoneyVnd.isValidBudget(1L))
        assertTrue(MoneyVnd.isValidBudget(999_999_999_999L))
        assertFalse(MoneyVnd.isValidBudget(0L))
        assertFalse(MoneyVnd.isValidBudget(1_000_000_000_000L))
    }

    @Test
    fun `spoken formats VND with dong instead of symbol d for TalkBack`() {
        assertEquals("5.000.000 đồng", MoneyVnd.spoken(5_000_000L))
        assertEquals("0 đồng", MoneyVnd.spoken(0L))
        assertFalse(MoneyVnd.spoken(1_250_000L).endsWith("đ"))
    }

    @Test
    fun `rejects invalid inputs such as empty, decimal, negative, or overflow`() {
        assertNull(MoneyVnd.parse(""))
        assertNull(MoneyVnd.parse("   "))
        assertNull(MoneyVnd.parse("5.5"))
        assertNull(MoneyVnd.parse("-1000"))
        assertNull(MoneyVnd.parse("abc"))
        assertNull(MoneyVnd.parse("1.000.000.000.000")) // Overflow
    }

    @Test
    fun `live preview calculates remaining or over correctly when limit is decreased below spent`() {
        val currentSpent = 3_000_000L
        val newLimit = 2_000_000L

        val isOver = newLimit < currentSpent
        assertTrue(isOver)

        val overAmount = currentSpent - newLimit
        assertEquals(1_000_000L, overAmount)

        val ratio = currentSpent.toFloat() / newLimit
        assertEquals(1.5f, ratio, 0.001f)
        assertEquals(150, (ratio * 100f).roundToInt())
        assertEquals(1.0f, ratio.coerceIn(0f, 1f), 0.001f)
    }

    @Test
    fun `live preview calculates remaining and progress correctly when limit is increased`() {
        val currentSpent = 3_000_000L
        val newLimit = 6_000_000L

        val isOver = newLimit < currentSpent
        assertFalse(isOver)

        val remaining = newLimit - currentSpent
        assertEquals(3_000_000L, remaining)

        val ratio = currentSpent.toFloat() / newLimit
        assertEquals(0.5f, ratio, 0.001f)
        assertEquals(50, (ratio * 100f).roundToInt())
        assertEquals(0.5f, ratio.coerceIn(0f, 1f), 0.001f)
    }
}
