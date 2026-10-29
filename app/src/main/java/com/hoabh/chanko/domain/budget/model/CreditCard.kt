package com.hoabh.chanko.domain.budget.model

import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

/**
 * Metadata thẻ tín dụng (Domain Model).
 * Tuyệt đối không lưu số thẻ, ngày hết hạn hay CVV.
 * Chỉ lưu chuỗi tên 1–40 ký tự để phân loại chi tiêu.
 */
data class CreditCard(
    val id: String,
    val name: String,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 40

        /**
         * Chuẩn hóa tên thẻ: Trim khoảng trắng đầu/cuối và áp dụng Unicode NFC.
         */
        fun normalizeName(raw: String): String {
            return Normalizer.normalize(raw.trim(), Normalizer.Form.NFC)
        }

        /**
         * Đếm số grapheme cluster (ký tự người dùng nhìn thấy), không phải UTF-16 code unit.
         * Ví dụ: emoji ghép "👨‍👩‍👧" = 1, "Thẻ" (NFC) = 3.
         */
        fun graphemeCount(text: String): Int {
            if (text.isEmpty()) return 0
            val iterator = BreakIterator.getCharacterInstance()
            iterator.setText(text)
            var count = 0
            while (iterator.next() != BreakIterator.DONE) count++
            return count
        }

        /**
         * Kiểm tra tính hợp lệ của tên thẻ: 1–40 grapheme sau trim + NFC.
         */
        fun validateName(name: String): CardNameValidationResult {
            val normalized = normalizeName(name)
            return when {
                normalized.isEmpty() -> CardNameValidationResult.Empty
                graphemeCount(normalized) > MAX_NAME_LENGTH -> CardNameValidationResult.TooLong
                else -> CardNameValidationResult.Valid(normalized)
            }
        }

        /** So trùng case-insensitive sau trim/NFC. */
        fun isSameName(a: String, b: String): Boolean =
            normalizeName(a).lowercase(Locale.ROOT) == normalizeName(b).lowercase(Locale.ROOT)
    }
}

sealed interface CardNameValidationResult {
    data class Valid(val normalizedName: String) : CardNameValidationResult
    data object Empty : CardNameValidationResult
    data object TooLong : CardNameValidationResult
}
