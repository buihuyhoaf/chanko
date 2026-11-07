package com.hoabh.chanko.domain.rewards

data class FreezeReward(
    val hatReward: Long,
    val pateReward: Long
)

/**
 * Chính sách trao thưởng khi hoàn thành Freeze:
 * - Tỷ lệ thuận theo giá sản phẩm (không dùng khoảng giá cố định).
 * - Không quy đổi thành tiền mặt.
 */
object FreezeRewardPolicy {
    const val DEFAULT_VND_PER_HAT_REWARD: Long = 20_000L
    const val DEFAULT_VND_PER_PATE_REWARD: Long = 40_000L
    const val MIN_HAT_REWARD: Long = 5L
    const val MIN_PATE_REWARD: Long = 5L

    fun calculateReward(
        lockedPriceVnd: Long,
        vndPerHat: Long = DEFAULT_VND_PER_HAT_REWARD,
        vndPerPate: Long = DEFAULT_VND_PER_PATE_REWARD
    ): FreezeReward {
        if (lockedPriceVnd <= 0L) {
            return FreezeReward(MIN_HAT_REWARD, MIN_PATE_REWARD)
        }
        val hat = maxOf(MIN_HAT_REWARD, lockedPriceVnd / vndPerHat)
        val pate = maxOf(MIN_PATE_REWARD, lockedPriceVnd / vndPerPate)
        return FreezeReward(hatReward = hat, pateReward = pate)
    }
}
