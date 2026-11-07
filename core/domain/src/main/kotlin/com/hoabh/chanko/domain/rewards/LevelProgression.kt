package com.hoabh.chanko.domain.rewards

/**
 * Giao diện bảng tiến trình Cấp độ (Level) của Mascot Chanko.
 */
interface LevelProgressionTable {
    fun calculateLevel(totalPate: Long): Int
    fun requiredPateForLevel(level: Int): Long
    fun pateProgressInLevel(totalPate: Long): Pair<Long, Long>
    fun calculateProgressFraction(totalPate: Long): Float
}

/**
 * Bảng tiến trình Level mặc định của Chanko.
 * Cấp độ tăng dần theo lũy tiến Pate.
 */
object DefaultLevelProgressionTable : LevelProgressionTable {
    private val LEVEL_THRESHOLDS = longArrayOf(
        0L,       // Level 1: 0 Pate
        100L,     // Level 2: 100 Pate
        250L,     // Level 3: 250 Pate
        500L,     // Level 4: 500 Pate
        900L,     // Level 5: 900 Pate
        1400L,    // Level 6: 1400 Pate
        2000L,    // Level 7: 2000 Pate
        3000L,    // Level 8: 3000 Pate
        4500L,    // Level 9: 4500 Pate
        6500L     // Level 10: 6500 Pate
    )

    override fun calculateLevel(totalPate: Long): Int {
        val pate = totalPate.coerceAtLeast(0L)
        for (i in LEVEL_THRESHOLDS.indices.reversed()) {
            if (pate >= LEVEL_THRESHOLDS[i]) {
                return i + 1
            }
        }
        return 1
    }

    override fun requiredPateForLevel(level: Int): Long {
        val idx = (level - 1).coerceIn(0, LEVEL_THRESHOLDS.size - 1)
        return LEVEL_THRESHOLDS[idx]
    }

    override fun pateProgressInLevel(totalPate: Long): Pair<Long, Long> {
        val currentLevel = calculateLevel(totalPate)
        if (currentLevel >= LEVEL_THRESHOLDS.size) {
            val base = LEVEL_THRESHOLDS.last()
            return Pair(totalPate - base, 0L)
        }
        val currentThreshold = LEVEL_THRESHOLDS[currentLevel - 1]
        val nextThreshold = LEVEL_THRESHOLDS[currentLevel]
        val currentInLevel = (totalPate - currentThreshold).coerceAtLeast(0L)
        val neededInLevel = nextThreshold - currentThreshold
        return Pair(currentInLevel, neededInLevel)
    }

    override fun calculateProgressFraction(totalPate: Long): Float {
        val (currentInLevel, neededInLevel) = pateProgressInLevel(totalPate)
        if (neededInLevel <= 0L) return 1.0f
        return (currentInLevel.toFloat() / neededInLevel.toFloat()).coerceIn(0.0f, 1.0f)
    }
}
