package com.hoabh.chanko.domain.budget.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Value object và tiện ích số học cho tiền tệ VND trong phân hệ Ngân sách.
 * - Chỉ sử dụng số nguyên [Long], tuyệt đối không dùng Float/Double để tránh sai số.
 * - Định dạng chuẩn tiếng Việt: 5000000 -> "5.000.000đ"
 * - Hạn mức tối đa hỗ trợ: 999.999.999.999đ (12 chữ số)
 * - Hạn mức tối thiểu hợp lệ khi nhập ngân sách: 1đ
 */
data class MoneyVnd(val amount: Long) : Comparable<MoneyVnd> {

    init {
        require(amount >= 0L) { "Số tiền VND không được âm: $amount" }
    }

    val formatted: String get() = format(amount)

    operator fun plus(other: MoneyVnd): MoneyVnd = MoneyVnd(amount + other.amount)

    operator fun minus(other: MoneyVnd): Long = amount - other.amount

    override fun compareTo(other: MoneyVnd): Int = amount.compareTo(other.amount)

    override fun toString(): String = formatted

    companion object {
        const val MAX_BUDGET_AMOUNT: Long = 999_999_999_999L
        const val MIN_BUDGET_AMOUNT: Long = 1L

        val ZERO = MoneyVnd(0L)

        private val symbols = DecimalFormatSymbols(Locale.GERMAN).apply {
            groupingSeparator = '.'
        }
        private val decimalFormat = DecimalFormat("#,###", symbols)

        /**
         * Định dạng số nguyên Long thành chuỗi tiền tệ VND có hậu tố "đ".
         * Ví dụ: 5000000 -> "5.000.000đ", 0 -> "0đ"
         */
        fun format(amount: Long): String {
            return if (amount == 0L) {
                "0đ"
            } else {
                "${decimalFormat.format(amount)}đ"
            }
        }

        /**
         * Định dạng số nguyên Long không có hậu tố "đ" (phục vụ TextField hiển thị nhóm nghìn).
         * Ví dụ: 5000000 -> "5.000.000"
         */
        fun formatWithoutSymbol(amount: Long): String {
            return if (amount == 0L) "0" else decimalFormat.format(amount)
        }

        /**
         * Chuỗi đọc cho TalkBack: không dùng ký hiệu "đ" (dễ bị đọc sai thành chữ "đê").
         * Ví dụ: 5000000 -> "5.000.000 đồng", -500000 -> "âm 500.000 đồng".
         */
        fun spoken(amount: Long): String {
            val abs = if (amount < 0) -amount else amount
            val prefix = if (amount < 0) "âm " else ""
            return "$prefix${formatWithoutSymbol(abs)} đồng"
        }

        /** Kiểm tra hạn mức hợp lệ cho command/repository. */
        fun isValidBudget(amount: Long): Boolean = amount in MIN_BUDGET_AMOUNT..MAX_BUDGET_AMOUNT

        /**
         * Parse chuỗi ký tự người dùng nhập thành số nguyên VND.
         * Chấp nhận chuỗi số thuần túy ("5000000") hoặc có dấu chấm phân cách nhóm nghìn hợp lệ ("5.000.000").
         * Chặn số âm, số thập phân (dùng dấu phẩy hoặc chấm không đủ 3 số), ký tự lạ hoặc vượt hạn mức.
         */
        fun parse(input: String): Long? {
            val trimmed = input.trim().replace("đ", "").trim()
            if (trimmed.isBlank()) return null
            if (trimmed.contains(",")) return null // Chặn thập phân dạng phẩy

            if (trimmed.contains(".")) {
                val parts = trimmed.split(".")
                if (parts.any { it.isEmpty() || !it.all { char -> char.isDigit() } }) return null
                if (parts.first().length !in 1..3) return null
                for (i in 1 until parts.size) {
                    if (parts[i].length != 3) return null
                }
            } else {
                if (!trimmed.all { it.isDigit() }) return null
            }

            val clean = trimmed.replace(".", "")
            val value = clean.toLongOrNull() ?: return null
            if (value < MIN_BUDGET_AMOUNT || value > MAX_BUDGET_AMOUNT) return null
            return value
        }
    }
}
