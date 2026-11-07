package com.hoabh.chanko.domain.rewards

import java.util.UUID

/**
 * Loại giao dịch tài nguyên trong sổ cái Hạt & Pate.
 */
enum class ResourceTransactionType {
    /** Thưởng Daily Check-in xác nhận không mua sắm ngoài dự kiến. */
    CHECKIN_REWARD,

    /** Thưởng hoàn thành chu kỳ Freeze hợp lệ. */
    FREEZE_REWARD,

    /** Khấu trừ chi phí khi người dùng chủ động Unfreeze sớm hoặc mua trong Freeze. */
    EARLY_UNFREEZE_FEE,

    /** Thu hồi thưởng Freeze không hợp lệ khi phát hiện mua muộn hồi tố. */
    RETROACTIVE_CLAWBACK,

    /** Cấp tài nguyên khởi tạo tài khoản mới. */
    INITIAL_GRANT
}

/**
 * Bản ghi giao dịch trong sổ cái tài nguyên.
 */
data class ResourceEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: ResourceTransactionType,
    val hatDelta: Long,
    val pateDelta: Long,
    val description: String = ""
)

/**
 * Trạng thái số dư tài nguyên Hạt và Pate của người dùng và Chanko.
 *
 * Nguyên tắc bất biến đã chốt:
 * - Hạt (Resource) và Pate (EXP) không bao giờ âm (chặn sàn ở 0).
 * - Vẫn cho phép Unfreeze khi tài khoản không đủ tài nguyên.
 * - Pate tích lũy làm tăng Level của Chanko.
 * - Chanko tự lên Level khi đủ Pate.
 * - Level đã đạt không bị hạ cấp khi trừ Pate (Monotonic non-decreasing).
 * - Không quy đổi Hạt/Pate ra tiền mặt thật.
 */
data class ResourceBalance(
    val hatBalance: Long = 0L,
    val pateBalance: Long = 0L,
    val currentLevel: Int = 1,
    val highestLevelReached: Int = currentLevel
) {
    init {
        require(hatBalance >= 0L) { "Số dư Hạt không được âm: $hatBalance" }
        require(pateBalance >= 0L) { "Số dư Pate không được âm: $pateBalance" }
        require(currentLevel >= 1) { "Level Chanko tối thiểu là 1: $currentLevel" }
        require(highestLevelReached >= currentLevel) { "highestLevelReached phải >= currentLevel" }
    }

    /**
     * Cộng tài nguyên thưởng và tự động nâng cấp Level nếu đủ điều kiện Pate.
     */
    fun credit(
        hat: Long,
        pate: Long,
        progressionTable: LevelProgressionTable = DefaultLevelProgressionTable
    ): ResourceBalance {
        val newHat = (hatBalance + hat.coerceAtLeast(0L)).coerceAtLeast(0L)
        val newPate = (pateBalance + pate.coerceAtLeast(0L)).coerceAtLeast(0L)
        val calculatedLevel = progressionTable.calculateLevel(newPate)
        val newLevel = maxOf(currentLevel, calculatedLevel)
        return copy(
            hatBalance = newHat,
            pateBalance = newPate,
            currentLevel = newLevel,
            highestLevelReached = maxOf(highestLevelReached, newLevel)
        )
    }

    /**
     * Trừ tài nguyên (Unfreeze sớm hoặc thu hồi).
     * Áp dụng nguyên tắc không âm và không giảm Level đã đạt.
     */
    fun debit(hat: Long, pate: Long): ResourceBalance {
        val newHat = (hatBalance - hat.coerceAtLeast(0L)).coerceAtLeast(0L)
        val newPate = (pateBalance - pate.coerceAtLeast(0L)).coerceAtLeast(0L)
        return copy(
            hatBalance = newHat,
            pateBalance = newPate,
            currentLevel = currentLevel,
            highestLevelReached = highestLevelReached
        )
    }
}
