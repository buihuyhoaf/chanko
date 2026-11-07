package com.hoabh.chanko.domain.rewards

data class DailyCheckInResult(
    val success: Boolean,
    val updatedBalance: ResourceBalance,
    val grantedHat: Long = 0L,
    val grantedPate: Long = 0L,
    val message: String
)

/**
 * Xử lý Daily Check-in:
 * - Yêu cầu người dùng xác nhận không mua sắm ngoài dự kiến.
 * - Thưởng cố định mỗi ngày, không có streak multiplier.
 * - Tối đa 1 lần mỗi ngày dương lịch.
 */
class DailyCheckInUseCase(
    val fixedHatReward: Long = 10L,
    val fixedPateReward: Long = 10L
) {
    fun execute(
        currentBalance: ResourceBalance,
        hasCheckedInToday: Boolean,
        confirmedNoImpulseBuying: Boolean
    ): DailyCheckInResult {
        if (!confirmedNoImpulseBuying) {
            return DailyCheckInResult(
                success = false,
                updatedBalance = currentBalance,
                message = "Yêu cầu người dùng xác nhận không mua sắm để nhận thưởng."
            )
        }

        if (hasCheckedInToday) {
            return DailyCheckInResult(
                success = false,
                updatedBalance = currentBalance,
                message = "Bạn đã hoàn thành Check-in hôm nay rồi."
            )
        }

        val updatedBalance = currentBalance.credit(fixedHatReward, fixedPateReward)
        return DailyCheckInResult(
            success = true,
            updatedBalance = updatedBalance,
            grantedHat = fixedHatReward,
            grantedPate = fixedPateReward,
            message = "Check-in thành công! Bạn nhận được +$fixedHatReward Hạt và +$fixedPateReward Pate."
        )
    }
}
