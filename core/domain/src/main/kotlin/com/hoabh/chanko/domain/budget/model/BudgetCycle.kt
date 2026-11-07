package com.hoabh.chanko.domain.budget.model

/**
 * Chu kỳ ngân sách (Domain Model) nâng cấp cho Home v2.2 (BC22-01..BC22-10).
 *
 * @param id Định danh duy nhất của chu kỳ (ví dụ: "cycle_2026_10")
 * @param label Nhãn chu kỳ (ví dụ: "Tháng 10/2026")
 * @param startDate Ngày bắt đầu chu kỳ (ví dụ: "01/10/2026")
 * @param endDate Ngày kết thúc chu kỳ (ví dụ: "31/10/2026")
 * @param isCurrent Đánh dấu chu kỳ hiện tại (true) hay chu kỳ quá khứ đã kết thúc (false)
 * @param anchorDay Ngày neo bắt đầu chu kỳ (1..31, ví dụ: 1)
 * @param ordinal Số thứ tự chu kỳ bất biến theo tài khoản (bắt đầu từ 0)
 * @param status Trạng thái hoạt động của chu kỳ
 * @param configuredLimitMinor Hạn mức đã cấu hình trước cho kỳ tiếp theo (nếu có)
 */
data class BudgetCycle(
    val id: String,
    val label: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean,
    val anchorDay: Int = 1,
    val ordinal: Int = 0,
    val status: BudgetCycleStatus = if (isCurrent) BudgetCycleStatus.Active else BudgetCycleStatus.Closed,
    val configuredLimitMinor: Long? = null,
    val customLabelMonth: String? = null
) {
    /**
     * Chuỗi 2 chữ số tháng MM (01..12) cho Month Chip pill trên TopBar (BC-CARD-LABEL-01).
     */
    val labelMonth: String
        get() = customLabelMonth ?: run {
            val parts = startDate.split("/")
            if (parts.size >= 2) parts[1].padStart(2, '0') else "01"
        }

    val dateRangeText: String get() = "$startDate - $endDate"

    /**
     * ID theme gradient (1..12) theo design-tokens.json: floorMod(ordinal, 12) + 1
     */
    val themeId: Int get() = Math.floorMod(ordinal, 12) + 1

    /**
     * Vị trí theme gradient (0..11) trong bảng 12 Candy Gradient Themes (BC22-08).
     * Bất biến theo cycle identity và xoay vòng theo floorMod(ordinal, 12).
     */
    val themeIndex: Int get() = Math.floorMod(ordinal, 12)
}
