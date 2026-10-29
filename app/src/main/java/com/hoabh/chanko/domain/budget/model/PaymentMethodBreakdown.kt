package com.hoabh.chanko.domain.budget.model

/**
 * Phân bổ chi tiêu theo phương thức thanh toán trong chu kỳ (Domain Model).
 */
data class PaymentMethodBreakdown(
    val id: String,
    val name: String,
    val spentMinor: Long
) {
    val spentFormatted: String get() = MoneyVnd.format(spentMinor)
}

/**
 * Món đồ đáng chú ý đã lưu cần cân nhắc trong chu kỳ (Domain Model).
 */
data class NotableItem(
    val itemId: String,
    val name: String,
    val priceMinor: Long,
    val thumbnailResId: Int? = null
) {
    val priceFormatted: String get() = MoneyVnd.format(priceMinor)
}
