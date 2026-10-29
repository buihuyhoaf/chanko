package com.hoabh.chanko.domain.budget.model

/**
 * Lỗi có kiểu của phân hệ Ngân sách để ViewModel phân nhánh xử lý theo contract,
 * thay vì so sánh chuỗi thông báo.
 */
sealed class BudgetException(message: String) : Exception(message)

/** Ghi khi đang ngoại tuyến. */
class BudgetOfflineException : BudgetException("Kết nối mạng để chỉnh sửa")

/** Ghi vào chu kỳ đã kết thúc. */
class BudgetPastCycleException : BudgetException("Chỉ chỉnh sửa trong chu kỳ hiện tại")

/** Revision cơ sở đã cũ (C04 conflict). */
class BudgetConflictException : BudgetException("Ngân sách đã thay đổi. Tải lại để chỉnh sửa.")

/** Hạn mức ngoài khoảng 1–999.999.999.999đ (validation tầng command/repository). */
class InvalidBudgetAmountException : BudgetException("Số tiền không hợp lệ")

/** Chu kỳ chưa có cache khi ngoại tuyến (C13/no-cache). */
class BudgetNoOfflineCacheException : BudgetException("Chưa có dữ liệu để xem ngoại tuyến")

/** Lỗi tải dữ liệu chu kỳ (fixture load error). */
class BudgetLoadException : BudgetException("Chưa tải được dữ liệu ngân sách")

/** Lỗi lưu giả lập (fixture save error). */
class BudgetSaveException : BudgetException("Yêu cầu chưa hoàn tất")

/** Tên thẻ không hợp lệ (rỗng/quá giới hạn grapheme). */
class InvalidCardNameException(message: String = "Tên thẻ không hợp lệ") : BudgetException(message)

/** Tên thẻ dành riêng cho hệ thống (ví dụ Pay Now). */
class ReservedCardNameException(message: String = "Tên này dành cho Pay Now") : BudgetException(message)

/** Tên thẻ trùng (case-insensitive, sau trim/NFC). */
class DuplicateCardNameException : BudgetException("Tên thẻ đã tồn tại")

/** Thẻ đã bị archive hoặc không tồn tại. */
class CreditCardUnavailableException(
    message: String = "Thẻ không còn khả dụng"
) : BudgetException(message)
