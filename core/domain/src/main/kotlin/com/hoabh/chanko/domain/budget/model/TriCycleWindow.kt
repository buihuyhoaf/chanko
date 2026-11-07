package com.hoabh.chanko.domain.budget.model

/**
 * Cửa sổ 3 chu kỳ đại diện cho Carousel trên màn Home (Home v2.2 - BC22-01..BC22-04):
 * - [previous]: Kỳ liền trước nếu tồn tại (đã kết thúc, chỉ đọc). Null nếu tài khoản mới chưa có lịch sử.
 * - [current]: Kỳ hiện tại (đang diễn ra hoặc chưa có ngân sách). Luôn tồn tại.
 * - [next]: Kỳ tiếp theo (đã cấu hình hạn mức hoặc placeholder trung tính để thiết lập). Luôn tồn tại.
 */
data class TriCycleWindow(
    val previous: BudgetCycle?,
    val current: BudgetCycle,
    val next: BudgetCycle,
    val snapshots: Map<String, BudgetSnapshot> = emptyMap(),
    val lastSyncedAt: Long? = null
) {
    /**
     * Danh sách các slot hợp lệ hiển thị trên Carousel (tối đa 3 slots).
     * Nếu không có [previous], danh sách chỉ gồm 2 slots [current, next] (BC22-04).
     */
    val slots: List<BudgetCycle>
        get() = listOfNotNull(previous, current, next)

    val hasPrevious: Boolean
        get() = previous != null
}
