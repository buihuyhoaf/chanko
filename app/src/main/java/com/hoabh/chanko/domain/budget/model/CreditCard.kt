package com.hoabh.chanko.domain.budget.model

import java.text.Normalizer

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
         * Kiểm tra tính hợp lệ của tên thẻ.
         */
        fun validateName(name: String): CardNameValidationResult {
            val normalized = normalizeName(name)
            return when {
                normalized.isEmpty() -> CardNameValidationResult.Empty
                normalized.length > MAX_NAME_LENGTH -> CardNameValidationResult.TooLong
                else -> CardNameValidationResult.Valid(normalized)
            }
        }
    }
}

sealed interface CardNameValidationResult {
    data class Valid(val normalizedName: String) : CardNameValidationResult
    data object Empty : CardNameValidationResult
    data object TooLong : CardNameValidationResult
}
