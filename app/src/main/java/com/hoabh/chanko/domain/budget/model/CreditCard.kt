package com.hoabh.chanko.domain.budget.model

import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

/**
 * 6 màu Candy Swatches chuẩn v2.1
 */
enum class CardColor(val hex: String, val displayName: String, val colorLong: Long) {
    PURPLE("#7447FF", "Tím", 0xFF7447FF),
    PINK("#FF64A4", "Hồng", 0xFFFF64A4),
    BLUE("#4D96FF", "Xanh dương", 0xFF4D96FF),
    MINT("#34CBA0", "Bạc hà", 0xFF34CBA0),
    YELLOW("#FFD363", "Vàng", 0xFFFFD363),
    ORANGE("#FF9C55", "Cam", 0xFFFF9C55)
}

/**
 * Metadata thẻ tín dụng (Domain Model).
 * Tuyệt đối không lưu số thẻ, ngày hết hạn hay CVV.
 * Chỉ lưu nhãn tên và màu sắc để phân loại chi tiêu.
 */
data class CreditCard(
    val id: String,
    val name: String,
    val color: CardColor = CardColor.PURPLE,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 40
        const val UI_MAX_NAME_LENGTH: Int = 30
        const val RESERVED_PAY_NOW: String = "pay now"

        /**
         * Chuẩn hóa tên thẻ: Trim khoảng trắng đầu/cuối và áp dụng Unicode NFC.
         */
        fun normalizeName(raw: String): String {
            return Normalizer.normalize(raw.trim(), Normalizer.Form.NFC)
        }

        /**
         * Đếm số grapheme cluster (ký tự người dùng nhìn thấy), không phải UTF-16 code unit.
         * Ví dụ: emoji ghép "👨‍👩‍👧" = 1, "Thẻ" (NFC) = 3, "Techcombank" = 11.
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
         * Kiểm tra tính hợp lệ của tên thẻ (Domain level: 1–40 grapheme).
         */
        fun validateName(name: String): CardNameValidationResult {
            val normalized = normalizeName(name)
            return when {
                normalized.isEmpty() -> CardNameValidationResult.Empty
                graphemeCount(normalized) > MAX_NAME_LENGTH -> CardNameValidationResult.TooLong
                else -> CardNameValidationResult.Valid(normalized)
            }
        }

        /** Validation riêng cho thêm thẻ: không có trạng thái NoChange. */
        fun validateAdd(rawName: String, activeNames: List<String>): AddCardValidationResult {
            val normalized = normalizeName(rawName)
            return when {
                normalized.isEmpty() -> AddCardValidationResult.Empty
                graphemeCount(normalized) > UI_MAX_NAME_LENGTH -> AddCardValidationResult.TooLong
                normalized.lowercase(Locale.ROOT) == RESERVED_PAY_NOW -> AddCardValidationResult.ReservedPayNow
                activeNames.any { isSameName(it, normalized) } -> AddCardValidationResult.Duplicate
                else -> AddCardValidationResult.Valid(normalized)
            }
        }

        /**
         * Kiểm tra tên thẻ cho UI Dialog Đổi tên thẻ C07 (UI level: 1–30 grapheme, no-change, duplicate, reserved).
         */
        fun validateRename(
            rawDraft: String,
            originalName: String,
            otherActiveCardNames: List<String>
        ): RenameValidationResult {
            val normalized = normalizeName(rawDraft)
            val normalizedOriginal = normalizeName(originalName)

            if (normalized.isEmpty()) {
                return RenameValidationResult.Empty
            }

            if (graphemeCount(normalized) > UI_MAX_NAME_LENGTH) {
                return RenameValidationResult.TooLong
            }

            if (normalized.lowercase(Locale.ROOT) == RESERVED_PAY_NOW) {
                return RenameValidationResult.ReservedPayNow
            }

            // No-change check: Exact normalized string equality (case-sensitive)
            if (normalized == normalizedOriginal) {
                return RenameValidationResult.NoChange
            }

            // Case-only change of self (e.g., "techcombank" -> "Techcombank") is allowed.
            // Check if matches any OTHER active card names (case-insensitive)
            val isDuplicateOther = otherActiveCardNames.any { isSameName(it, normalized) }
            if (isDuplicateOther) {
                return RenameValidationResult.Duplicate
            }

            return RenameValidationResult.Valid(normalized)
        }

        /**
         * Kiểm tra nếu tên thẻ hiện hữu vượt quá giới hạn mới 30 ký tự (Legacy card).
         */
        fun isLegacyOverLimit(name: String): Boolean =
            graphemeCount(normalizeName(name)) > UI_MAX_NAME_LENGTH

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

sealed interface RenameValidationResult {
    data class Valid(val normalizedName: String) : RenameValidationResult
    data object Empty : RenameValidationResult
    data object TooLong : RenameValidationResult
    data object Duplicate : RenameValidationResult
    data object ReservedPayNow : RenameValidationResult
    data object NoChange : RenameValidationResult
}

sealed interface AddCardValidationResult {
    data class Valid(val normalizedName: String) : AddCardValidationResult
    data object Empty : AddCardValidationResult
    data object TooLong : AddCardValidationResult
    data object Duplicate : AddCardValidationResult
    data object ReservedPayNow : AddCardValidationResult
}
