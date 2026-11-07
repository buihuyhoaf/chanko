package com.hoabh.chanko.domain.policy

import java.util.concurrent.TimeUnit

/**
 * Chính sách ấn định thời gian Cooling-off (Freeze) tự động của Chanko.
 *
 * Nguyên tắc:
 * - Chanko tự động quyết định thời gian chờ dựa trên giá trị món đồ.
 * - Người dùng không thể chỉnh sửa hoặc bỏ qua thời gian Freeze.
 * - Món đồ càng đắt, thời gian cần tự vấn và làm nguội cảm xúc bốc đồng càng dài.
 */
object FreezeDurationPolicy {
    val DURATION_TIER_1: Long = TimeUnit.DAYS.toMillis(1)   // 24 giờ cho < 200k
    val DURATION_TIER_2: Long = TimeUnit.DAYS.toMillis(3)   // 72 giờ cho 200k - 1tr
    val DURATION_TIER_3: Long = TimeUnit.DAYS.toMillis(7)   // 168 giờ cho 1tr - 5tr
    val DURATION_TIER_4: Long = TimeUnit.DAYS.toMillis(14)  // 336 giờ cho > 5tr

    const val THRESHOLD_LOW_VND: Long = 200_000L
    const val THRESHOLD_MID_VND: Long = 1_000_000L
    const val THRESHOLD_HIGH_VND: Long = 5_000_000L

    /**
     * Tính toán thời gian Freeze (mili-giây) dựa trên giá sản phẩm (VNĐ).
     */
    fun calculateDurationMillis(priceVnd: Long): Long {
        val price = priceVnd.coerceAtLeast(0L)
        return when {
            price < THRESHOLD_LOW_VND -> DURATION_TIER_1
            price < THRESHOLD_MID_VND -> DURATION_TIER_2
            price <= THRESHOLD_HIGH_VND -> DURATION_TIER_3
            else -> DURATION_TIER_4
        }
    }

    /**
     * Trả về số ngày Freeze tương ứng để hiển thị trên UI.
     */
    fun calculateDurationDays(priceVnd: Long): Int {
        val millis = calculateDurationMillis(priceVnd)
        return TimeUnit.MILLISECONDS.toDays(millis).toInt()
    }
}
