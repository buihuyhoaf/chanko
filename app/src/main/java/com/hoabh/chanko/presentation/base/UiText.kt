package com.hoabh.chanko.presentation.base

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Đại diện cho chuỗi văn bản trên giao diện người dùng.
 * Cho phép ViewModel đóng gói text (cố định từ resource hoặc dynamic từ server/người dùng)
 * mà không phụ thuộc trực tiếp vào Android [Context].
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @get:StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            return resId == other.resId && args == other.args
        }

        override fun hashCode(): Int {
            return 31 * resId + args.hashCode()
        }

        override fun toString(): String {
            return "StringResource(resId=$resId, args=$args)"
        }
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId, *args.toTypedArray())
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *args.toTypedArray())
    }

    companion object {
        val Empty: UiText = DynamicString("")
    }
}
