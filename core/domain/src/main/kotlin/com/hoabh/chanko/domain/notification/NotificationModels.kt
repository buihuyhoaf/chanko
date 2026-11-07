package com.hoabh.chanko.domain.notification

import java.util.UUID

/**
 * Các loại thông báo Push Notification của Chanko.
 *
 * Nguyên tắc đã chốt:
 * - Chủ động hỏi xem người dùng đã mua sản phẩm nào hay chưa (tự phản tư).
 * - Nhắc Daily Check-in.
 * - Báo sản phẩm hoàn thành Freeze và sẵn sàng xem xét.
 * - Thông báo thưởng Hạt/Pate, thay đổi hồi tố và Level Up.
 * - Tuyệt đối không dùng thông báo để khuyến khích mua sắm.
 */
enum class NotificationType {
    PURCHASE_INQUIRY,
    DAILY_CHECKIN_REMINDER,
    FREEZE_COMPLETED,
    REWARD_GRANTED,
    RETROACTIVE_ADJUSTMENT,
    LEVEL_UP
}

/**
 * Hành động tương tác nhanh trực tiếp trên thông báo.
 */
data class NotificationAction(
    val actionId: String,
    val titleVi: String
)

/**
 * Dữ liệu payload của một thông báo Chanko.
 */
data class ChankoNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val body: String,
    val targetItemId: String? = null,
    val actions: List<NotificationAction> = emptyList(),
    val scheduledTimeMillis: Long = System.currentTimeMillis()
)

/**
 * Bản tin tổng hợp Daily Email.
 */
data class DailyEmailSummary(
    val recipientEmail: String,
    val subject: String,
    val dateFormatted: String,
    val frozenItemsCount: Int,
    val readyItemsCount: Int,
    val currentHatBalance: Long,
    val currentPateBalance: Long,
    val chankoLevel: Int,
    val budgetRemainingVnd: Long,
    val bodyHtml: String
)
