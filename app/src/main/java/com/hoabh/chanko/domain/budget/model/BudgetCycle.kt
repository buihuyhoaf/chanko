package com.hoabh.chanko.domain.budget.model

/**
 * Chu kỳ ngân sách (Domain Model).
 * @param id Định danh duy nhất của chu kỳ (ví dụ: "cycle_2026_10")
 * @param label Nhãn chu kỳ (ví dụ: "Tháng 10/2026")
 * @param startDate Ngày bắt đầu chu kỳ (ví dụ: "01/10/2026")
 * @param endDate Ngày kết thúc chu kỳ (ví dụ: "31/10/2026")
 * @param isCurrent Đánh dấu chu kỳ hiện tại (true) hay chu kỳ quá khứ đã kết thúc (false)
 * @param anchorDay Ngày neo bắt đầu chu kỳ (chỉ đọc trong MVP, ví dụ: 1)
 */
data class BudgetCycle(
    val id: String,
    val label: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean,
    val anchorDay: Int = 1
) {
    val dateRangeText: String get() = "$startDate - $endDate"
}
