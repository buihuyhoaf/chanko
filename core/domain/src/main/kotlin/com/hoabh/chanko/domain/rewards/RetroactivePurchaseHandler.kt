package com.hoabh.chanko.domain.rewards

import com.hoabh.chanko.domain.model.FreezeState

data class RetroactiveAdjustmentResult(
    val previousBalance: ResourceBalance,
    val updatedBalance: ResourceBalance,
    val clawedBackRewardHat: Long,
    val clawedBackRewardPate: Long,
    val appliedUnfreezeFeeHat: Long,
    val appliedUnfreezeFeePate: Long,
    val auditLog: String
)

/**
 * Xử lý khai báo mua sản phẩm trong hoặc sau Freeze:
 * - Nếu người dùng mua trong lúc Freeze: tính chi phí Unfreeze tại thời điểm mua thực tế.
 * - Nếu khai báo mua trễ sau khi đã nhận thưởng Freeze không hợp lệ: thu hồi thưởng + phạt Unfreeze hồi tố + ghi audit log.
 * - Không trừ hai lần nếu đã chủ động Unfreeze trước đó.
 */
object RetroactivePurchaseHandler {

    fun processPurchase(
        currentBalance: ResourceBalance,
        freezeState: FreezeState,
        actualPurchaseTimestamp: Long,
        alreadyUnfrozenEarly: Boolean = false
    ): RetroactiveAdjustmentResult {
        // Đã unfreeze trước đó -> không phạt trùng lặp
        if (alreadyUnfrozenEarly) {
            return RetroactiveAdjustmentResult(
                previousBalance = currentBalance,
                updatedBalance = currentBalance,
                clawedBackRewardHat = 0L,
                clawedBackRewardPate = 0L,
                appliedUnfreezeFeeHat = 0L,
                appliedUnfreezeFeePate = 0L,
                auditLog = "Đã Unfreeze sớm trước đó, không áp dụng trừ phạt lặp."
            )
        }

        val wasBoughtDuringFreeze = actualPurchaseTimestamp < freezeState.endMillis

        if (!wasBoughtDuringFreeze) {
            // Mua sau khi Freeze kết thúc hợp lệ -> Không trừ phạt, giữ nguyên thưởng
            return RetroactiveAdjustmentResult(
                previousBalance = currentBalance,
                updatedBalance = currentBalance,
                clawedBackRewardHat = 0L,
                clawedBackRewardPate = 0L,
                appliedUnfreezeFeeHat = 0L,
                appliedUnfreezeFeePate = 0L,
                auditLog = "Mua hàng sau khi đã kết thúc Freeze hợp lệ. Giữ nguyên tài nguyên."
            )
        }

        // Mua trong thời gian Freeze:
        // 1. Thu hồi thưởng Freeze nếu đã nhận không hợp lệ
        val clawbackHat = if (freezeState.rewardClaimed) {
            FreezeRewardPolicy.calculateReward(freezeState.lockedPrice).hatReward
        } else 0L

        val clawbackPate = if (freezeState.rewardClaimed) {
            FreezeRewardPolicy.calculateReward(freezeState.lockedPrice).pateReward
        } else 0L

        // 2. Tính phí Unfreeze tại thời điểm mua thực tế
        val remainingAtPurchase = (freezeState.endMillis - actualPurchaseTimestamp).coerceIn(0L, freezeState.durationMillis)
        val unfreezeFee = UnfreezeCostCalculator.calculateFee(
            lockedPriceVnd = freezeState.lockedPrice,
            remainingMillis = remainingAtPurchase,
            totalDurationMillis = freezeState.durationMillis
        )

        // 3. Khấu trừ tổng vào số dư (chặn sàn ở 0, Level không giảm)
        val totalHat = clawbackHat + unfreezeFee.hatFee
        val totalPate = clawbackPate + unfreezeFee.pateFee
        val updatedBalance = currentBalance.debit(totalHat, totalPate)

        val log = "Khai báo mua hàng hồi tố tại timestamp $actualPurchaseTimestamp. " +
                "Thu hồi thưởng: -$clawbackHat Hạt, -$clawbackPate Pate. " +
                "Phạt Unfreeze thời điểm mua: -${unfreezeFee.hatFee} Hạt, -${unfreezeFee.pateFee} Pate."

        return RetroactiveAdjustmentResult(
            previousBalance = currentBalance,
            updatedBalance = updatedBalance,
            clawedBackRewardHat = clawbackHat,
            clawedBackRewardPate = clawbackPate,
            appliedUnfreezeFeeHat = unfreezeFee.hatFee,
            appliedUnfreezeFeePate = unfreezeFee.pateFee,
            auditLog = log
        )
    }
}
