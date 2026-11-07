package com.hoabh.chanko.domain.budget.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Các hàm tiện ích tính toán ngân sách và quy tắc chuyển chu kỳ (Home v2.2 - BC22-11..BC22-20).
 */
object BudgetCalculation {

    data class BudgetMetrics(
        val limitMinor: Long?,
        val spentMinor: Long?,
        val remainingMinor: Long?,
        val overMinor: Long?,
        val isOverBudget: Boolean,
        val clampedProgress: Float,
        val realPercentInt: Int?,
        val realPercentText: String?
    )

    /**
     * Tính tổng chi tiêu: Pay Now và Credit cùng tính theo ngày mua thực tế (BC22-11).
     */
    fun calculateTotalSpent(payNowSpentMinor: Long, creditSpentMinor: Long): Long {
        return payNowSpentMinor + creditSpentMinor
    }

    /**
     * Tính toán chỉ số ngân sách:
     * - Thanh tiến trình clamp trong [0.0f, 1.0f] (BC22-13).
     * - Nhãn hiển thị tỷ lệ thật (ví dụ 110%) khi spent > limit (BC22-13).
     * - Số tiền vượt = max(spent - limit, 0).
     */
    fun calculateMetrics(limitMinor: Long?, spentMinor: Long?): BudgetMetrics {
        if (limitMinor == null || spentMinor == null || limitMinor <= 0L) {
            val isOver = if (limitMinor != null && spentMinor != null) spentMinor > limitMinor else false
            return BudgetMetrics(
                limitMinor = limitMinor,
                spentMinor = spentMinor,
                remainingMinor = if (limitMinor != null && spentMinor != null) limitMinor - spentMinor else null,
                overMinor = if (isOver && limitMinor != null && spentMinor != null) spentMinor - limitMinor else null,
                isOverBudget = isOver,
                clampedProgress = if (isOver) 1.0f else 0.0f,
                realPercentInt = null,
                realPercentText = null
            )
        }

        val remaining = limitMinor - spentMinor
        val isOver = spentMinor > limitMinor
        val over = if (isOver) spentMinor - limitMinor else 0L

        // Tỷ lệ phần trăm làm tròn gần nhất HALF_UP
        val percent = BigDecimal.valueOf(spentMinor).movePointRight(2)
            .divide(BigDecimal.valueOf(limitMinor), 0, RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.valueOf(0), BigDecimal.valueOf(Int.MAX_VALUE.toLong()))
            .toInt()

        val ratio = spentMinor.toFloat() / limitMinor.toFloat()
        val clamped = ratio.coerceIn(0.0f, 1.0f)

        return BudgetMetrics(
            limitMinor = limitMinor,
            spentMinor = spentMinor,
            remainingMinor = remaining,
            overMinor = if (isOver) over else null,
            isOverBudget = isOver,
            clampedProgress = clamped,
            realPercentInt = percent,
            realPercentText = "$percent%"
        )
    }

    /**
     * Thực hiện chuyển chu kỳ (Rollover) idempotent (BC22-19, BC22-20):
     * - [current] cũ -> trở thành [previous] với trạng thái [BudgetCycleStatus.Closed] (giữ nguyên identity & theme).
     * - [next] cũ -> trở thành [current] mới với trạng thái [BudgetCycleStatus.Active] (bảo toàn identity, theme và hạn mức đã cấu hình).
     * - [newNext] -> trở thành [next] mới (status [BudgetCycleStatus.UpcomingUnconfigured] hoặc Configured).
     * - Số dư thuộc về kỳ cũ, KHÔNG chuyển tiếp số dư (no balance carry-over).
     */
    fun performRollover(
        currentWindow: TriCycleWindow,
        newNextCycle: BudgetCycle
    ): TriCycleWindow {
        val oldCurrent = currentWindow.current
        val oldNext = currentWindow.next

        val newPrevious = oldCurrent.copy(
            isCurrent = false,
            status = BudgetCycleStatus.Closed
        )

        val newCurrent = oldNext.copy(
            isCurrent = true,
            status = BudgetCycleStatus.Active
        )

        return TriCycleWindow(
            previous = newPrevious,
            current = newCurrent,
            next = newNextCycle,
            lastSyncedAt = System.currentTimeMillis()
        )
    }

    /**
     * Tính tổng hạn mức ngân sách đã thiết lập trong năm (AC05).
     * Chỉ cộng các tháng đã có cấu hình (limitMinor > 0), tuyệt đối không tính nháp hoặc gợi ý.
     */
    fun calculateAnnualConfiguredTotal(limits: List<Long?>): Long {
        return limits.filterNotNull().filter { it > 0L }.sum()
    }
}
