package com.hoabh.chanko.domain.model

/**
 * Trạng thái của sản phẩm trong vòng đời Cooling-off (Freeze) và xem xét.
 */
enum class ItemFreezeStatus {
    /** Đang trong thời gian Cooling-off bắt buộc do Chanko ấn định. */
    FREEZE_ACTIVE,

    /** Đã hết thời gian Freeze, sẵn sàng để người dùng xem xét quyết định (không tự động mua). */
    READY_FOR_REVIEW,

    /** Người dùng đã chủ động Unfreeze sớm (chịu chi phí Hạt & Pate). */
    UNFROZEN_EARLY,

    /** Người dùng xác nhận đã mua sản phẩm. */
    BOUGHT,

    /** Người dùng quyết định bỏ qua sản phẩm. */
    PASSED
}

/**
 * Lưu trữ trạng thái và dữ liệu khóa bất biến của chu kỳ Freeze cho một sản phẩm.
 *
 * @property lockedPrice Giá sản phẩm được khóa cố định tại thời điểm bắt đầu Freeze (VNĐ).
 * @property startedAtMillis Mốc thời gian tuyệt đối (Epoch timestamp UTC) khi bắt đầu Freeze.
 * @property durationMillis Tổng thời lượng Freeze được ấn định (mili-giây).
 * @property isRefrozen Đánh dấu món đồ đã trải qua Freeze lại hay chưa (để chặn trùng thưởng).
 * @property rewardClaimed Đánh dấu phần thưởng hoàn thành Freeze đã được ghi nhận hay chưa.
 */
data class FreezeState(
    val lockedPrice: Long,
    val startedAtMillis: Long,
    val durationMillis: Long,
    val isRefrozen: Boolean = false,
    val rewardClaimed: Boolean = false
) {
    init {
        require(lockedPrice >= 0) { "lockedPrice không được âm: $lockedPrice" }
        require(startedAtMillis >= 0) { "startedAtMillis không hợp lệ: $startedAtMillis" }
        require(durationMillis > 0) { "durationMillis phải lớn hơn 0: $durationMillis" }
    }

    /** Mốc thời gian kết thúc Freeze */
    val endMillis: Long get() = startedAtMillis + durationMillis

    /** Kiểm tra xem chu kỳ Freeze đã hoàn thành tại mốc thời gian [now] hay chưa */
    fun isComplete(now: Long): Boolean = now >= endMillis

    /** Thời gian còn lại tính bằng mili-giây tại mốc [now], chặn sàn ở 0 */
    fun remainingMillis(now: Long): Long = (endMillis - now).coerceIn(0L, durationMillis)

    /**
     * Xác định trạng thái tự động dựa trên mốc thời gian hiện tại [now]
     * và trạng thái cơ sở [currentStatus].
     */
    fun resolveStatus(now: Long, currentStatus: ItemFreezeStatus): ItemFreezeStatus {
        return when (currentStatus) {
            ItemFreezeStatus.FREEZE_ACTIVE -> {
                if (isComplete(now)) ItemFreezeStatus.READY_FOR_REVIEW else ItemFreezeStatus.FREEZE_ACTIVE
            }
            ItemFreezeStatus.READY_FOR_REVIEW,
            ItemFreezeStatus.UNFROZEN_EARLY,
            ItemFreezeStatus.BOUGHT,
            ItemFreezeStatus.PASSED -> currentStatus
        }
    }
}
