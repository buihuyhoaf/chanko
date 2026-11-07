package com.hoabh.chanko.domain.chat.util

import com.hoabh.chanko.domain.chat.model.CoolingOffInfo
import com.hoabh.chanko.domain.chat.model.CoolingOffStatus

/**
 * Tiện ích tính toán thời gian hạ nhiệt (Mandatory Cooling-off) theo chuẩn CONTRACT-SYNC.md.
 * remaining = max(0, end - now)
 * progress = clamp((now - start) / (end - start), 0, 1)
 */
object MonotonicClockCalculator {

    fun calculate(
        startedAtEpochMillis: Long,
        durationMillis: Long,
        currentEpochMillis: Long
    ): CoolingOffInfo {
        if (startedAtEpochMillis <= 0L || durationMillis <= 0L) {
            return CoolingOffInfo(
                startedAtEpochMillis = startedAtEpochMillis,
                durationMillis = durationMillis,
                remainingMillis = 0L,
                progress = 0f,
                status = CoolingOffStatus.UNKNOWN
            )
        }

        val endMillis = startedAtEpochMillis + durationMillis
        val remaining = maxOf(0L, endMillis - currentEpochMillis)
        val progress = ((currentEpochMillis - startedAtEpochMillis).toFloat() / durationMillis.toFloat())
            .coerceIn(0f, 1f)

        val status = if (remaining <= 0L) {
            CoolingOffStatus.ELAPSED
        } else {
            CoolingOffStatus.ACTIVE
        }

        return CoolingOffInfo(
            startedAtEpochMillis = startedAtEpochMillis,
            durationMillis = durationMillis,
            remainingMillis = remaining,
            progress = progress,
            status = status
        )
    }

    /**
     * Định dạng thời gian còn lại sang chuỗi tiếng Việt dễ hiểu.
     */
    fun formatRemaining(remainingMillis: Long): String {
        if (remainingMillis <= 0L) return "Sẵn sàng xem xét"

        val seconds = remainingMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "Còn lại ${days} ngày ${hours % 24} giờ"
            hours > 0 -> "Còn lại ${hours} giờ ${minutes % 60} phút"
            minutes > 0 -> "Còn lại ${minutes} phút"
            else -> "Còn lại dưới 1 phút"
        }
    }
}
