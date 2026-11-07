package com.hoabh.chanko.domain.budget.model

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
