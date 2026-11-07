package com.hoabh.chanko.domain.rewards

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.FreezeState

data class FreezeRewardDispatchResult(
    val updatedBalance: ResourceBalance,
    val rewardedItemId: Int?,
    val grantedHat: Long,
    val grantedPate: Long,
    val skippedItemsCount: Int
)

/**
 * Điều phối thưởng Freeze tự động:
 * - Tối đa 1 phần thưởng Freeze mỗi ngày: nếu nhiều món hoàn thành cùng ngày, chọn phần thưởng cao nhất.
 * - Thưởng tỷ lệ thuận theo giá sản phẩm (không chia khoảng giá cố định).
 * - Sản phẩm Freeze lại (isRefrozen == true) không nhận thưởng lần hai.
 */
object FreezeRewardDispatcher {

    fun dispatchDailyFreezeRewards(
        candidates: List<Pair<CooldownItem, FreezeState>>,
        currentBalance: ResourceBalance,
        alreadyClaimedFreezeRewardToday: Boolean
    ): FreezeRewardDispatchResult {
        if (alreadyClaimedFreezeRewardToday || candidates.isEmpty()) {
            return FreezeRewardDispatchResult(
                updatedBalance = currentBalance,
                rewardedItemId = null,
                grantedHat = 0L,
                grantedPate = 0L,
                skippedItemsCount = candidates.size
            )
        }

        // Lọc các món hợp lệ (chưa nhận thưởng và không phải Freeze lại)
        val eligibleCandidates = candidates.filter { (_, state) ->
            !state.rewardClaimed && !state.isRefrozen
        }

        if (eligibleCandidates.isEmpty()) {
            return FreezeRewardDispatchResult(
                updatedBalance = currentBalance,
                rewardedItemId = null,
                grantedHat = 0L,
                grantedPate = 0L,
                skippedItemsCount = candidates.size
            )
        }

        // Chọn món có giá trị cao nhất
        val bestCandidate = eligibleCandidates.maxByOrNull { (_, state) -> state.lockedPrice }!!
        val reward = FreezeRewardPolicy.calculateReward(bestCandidate.second.lockedPrice)

        val newBalance = currentBalance.credit(reward.hatReward, reward.pateReward)

        return FreezeRewardDispatchResult(
            updatedBalance = newBalance,
            rewardedItemId = bestCandidate.first.id,
            grantedHat = reward.hatReward,
            grantedPate = reward.pateReward,
            skippedItemsCount = candidates.size - 1
        )
    }
}
