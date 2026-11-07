package com.hoabh.chanko.domain.budget.model

/**
 * Trạng thái chu kỳ ngân sách (Home v2.2 - BC22-04, BC22-07, BC22-10).
 */
enum class BudgetCycleStatus {
    /** Kỳ quá khứ đã kết thúc (chỉ đọc, giữ nguyên màu theme gốc) */
    Closed,

    /** Kỳ hiện tại đang diễn ra */
    Active,

    /** Kỳ tiếp theo đã được thiết lập hạn mức dự kiến */
    UpcomingConfigured,

    /** Kỳ tiếp theo chưa thiết lập hạn mức (thẻ trung tính placeholder viền đứt nét) */
    UpcomingUnconfigured
}
