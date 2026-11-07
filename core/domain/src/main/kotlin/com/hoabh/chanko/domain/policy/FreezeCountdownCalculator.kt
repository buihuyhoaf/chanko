package com.hoabh.chanko.domain.policy

import java.util.concurrent.TimeUnit

/**
 * Bộ công cụ tính toán thời gian đếm ngược và tỷ lệ hoàn thành chu kỳ Freeze.
 * Hoạt động độc lập trên mốc thời gian Epoch millis (UTC).
 */
object FreezeCountdownCalculator {

    /**
     * Tính số mili-giây còn lại đến khi kết thúc Freeze tại mốc [now].
     * Kết quả được chặn sàn tại 0L và chặn trần tại [durationMillis].
     */
    fun calculateRemainingMillis(startedAtMillis: Long, durationMillis: Long, now: Long): Long {
        if (durationMillis <= 0L) return 0L
        val endMillis = startedAtMillis + durationMillis
        return (endMillis - now).coerceIn(0L, durationMillis)
    }

    /**
     * Tính tỷ lệ thời gian còn lại [0.0f, 1.0f] = (remainingMillis / durationMillis).
     * Bằng 1.0f khi vừa bắt đầu và giảm dần về 0.0f khi hoàn tất Freeze.
     */
    fun calculateRemainingFraction(startedAtMillis: Long, durationMillis: Long, now: Long): Float {
        if (durationMillis <= 0L) return 0.0f
        val remaining = calculateRemainingMillis(startedAtMillis, durationMillis, now)
        return (remaining.toDouble() / durationMillis.toDouble()).toFloat().coerceIn(0.0f, 1.0f)
    }

    /**
     * Kiểm tra chu kỳ Freeze đã hoàn thành tại mốc [now] hay chưa.
     */
    fun isFreezeComplete(startedAtMillis: Long, durationMillis: Long, now: Long): Boolean {
        if (durationMillis <= 0L) return true
        return now >= (startedAtMillis + durationMillis)
    }

    /**
     * Định dạng chuỗi thời gian còn lại thân thiện bằng Tiếng Việt.
     */
    fun formatRemainingTimeVi(remainingMillis: Long): String {
        if (remainingMillis <= 0L) {
            return "Đã sẵn sàng xem xét"
        }

        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

        return when {
            days > 0 -> {
                if (hours > 0) "Còn $days ngày $hours giờ" else "Còn $days ngày"
            }
            hours > 0 -> {
                if (minutes > 0) "Còn $hours giờ $minutes phút" else "Còn $hours giờ"
            }
            minutes > 0 -> {
                "Còn $minutes phút"
            }
            else -> {
                "Còn dưới 1 phút"
            }
        }
    }
}
