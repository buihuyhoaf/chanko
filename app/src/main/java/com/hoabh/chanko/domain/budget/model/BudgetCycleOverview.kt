package com.hoabh.chanko.domain.budget.model

/**
 * Ảnh chụp nguyên tử (atomic) toàn bộ dữ liệu Overview của MỘT chu kỳ.
 * Mọi phần (summary, payment, notable, activity, adjustment) luôn cùng [cycleId],
 * tránh trộn số liệu giữa kỳ cũ và kỳ mới khi đổi chu kỳ (C02, AC02/AC06).
 *
 * @param dataUpdatedAt Thời điểm dữ liệu được cập nhật thực tế (epoch millis),
 * dùng cho nhãn "Cập nhật lần cuối" khi ngoại tuyến (C11).
 */
data class BudgetCycleOverview(
    val cycleId: String,
    val snapshot: BudgetSnapshot,
    val paymentMethods: List<PaymentMethodBreakdown>,
    val notableItems: List<NotableItem>,
    val recentActivities: List<BudgetActivity>,
    val adjustments: List<BudgetAdjustment>,
    val dataUpdatedAt: Long
)
