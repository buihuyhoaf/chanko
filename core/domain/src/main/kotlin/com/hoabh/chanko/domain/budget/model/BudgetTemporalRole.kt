package com.hoabh.chanko.domain.budget.model

/**
 * Vai trò thời gian của một chu kỳ tháng (Budget v3 3-Axis State Matrix):
 * - [Past]: Tháng đã kết thúc trong quá khứ (Read-only, hiển thị số dư cuối kỳ hoặc vượt)
 * - [Current]: Tháng đang diễn ra (Hiển thị còn lại/vượt, tiến độ chi tiêu, cho phép chỉnh sửa khi online)
 * - [Future]: Tháng trong tương lai (Hiển thị dự kiến, không có thanh tiến độ/giao dịch)
 */
enum class BudgetTemporalRole {
    Past,
    Current,
    Future
}
