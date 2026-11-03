package com.hoabh.chanko.presentation.base

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UiTextTest {

    @Test
    fun dynamicString_returnsCorrectValue() {
        val text = UiText.DynamicString("Xin chào")
        assertEquals("Xin chào", text.value)
    }

    @Test
    fun emptyUiText_returnsEmptyString() {
        val empty = UiText.Empty
        assertEquals("", (empty as UiText.DynamicString).value)
    }

    @Test
    fun stringResource_holdsResIdAndArgs() {
        val res = UiText.StringResource(12345, "arg1", 100)
        assertEquals(12345, res.resId)
        assertEquals(listOf("arg1", 100), res.args)
    }

    @Test
    fun stringResource_equalityAndHashCode() {
        val res1 = UiText.StringResource(12345, "arg1")
        val res2 = UiText.StringResource(12345, "arg1")
        val res3 = UiText.StringResource(12345, "arg2")
        val res4 = UiText.StringResource(67890, "arg1")

        assertEquals(res1, res2)
        assertEquals(res1.hashCode(), res2.hashCode())
        assertNotEquals(res1, res3)
        assertNotEquals(res1, res4)
    }
}
