package com.hoabh.chanko.domain.rewards

import kotlin.math.round

data class UnfreezeFee(
    val hatFee: Long,
    val pateFee: Long
)

/**
 * Bộ tính chi phí Unfreeze sớm theo nguyên tắc đã chốt:
 * Chi phí ∝ Giá sản phẩm * (Thời gian còn lại / Tổng thời gian Freeze)
 *
 * - Sản phẩm càng đắt, chi phí Unfreeze càng cao.
 * - Unfreeze càng sớm, chi phí càng cao.
 * - Chi phí giảm dần theo thời gian và bằng 0 khi Freeze kết thúc.
 */
object UnfreezeCostCalculator {
    const val DEFAULT_VND_PER_HAT_FEE: Long = 10_000L
    const val DEFAULT_VND_PER_PATE_FEE: Long = 20_000L

    fun calculateFee(
        lockedPriceVnd: Long,
        remainingMillis: Long,
        totalDurationMillis: Long,
        vndPerHat: Long = DEFAULT_VND_PER_HAT_FEE,
        vndPerPate: Long = DEFAULT_VND_PER_PATE_FEE
    ): UnfreezeFee {
        if (remainingMillis <= 0L || totalDurationMillis <= 0L || lockedPriceVnd <= 0L) {
            return UnfreezeFee(hatFee = 0L, pateFee = 0L)
        }

        val fraction = (remainingMillis.toDouble() / totalDurationMillis.toDouble()).coerceIn(0.0, 1.0)
        val baseHat = maxOf(1.0, lockedPriceVnd.toDouble() / vndPerHat.toDouble())
        val basePate = maxOf(1.0, lockedPriceVnd.toDouble() / vndPerPate.toDouble())

        val hatFee = round(baseHat * fraction).toLong().coerceAtLeast(0L)
        val pateFee = round(basePate * fraction).toLong().coerceAtLeast(0L)

        return UnfreezeFee(hatFee = hatFee, pateFee = pateFee)
    }
}
