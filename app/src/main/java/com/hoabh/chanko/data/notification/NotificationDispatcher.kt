package com.hoabh.chanko.data.notification

import com.hoabh.chanko.domain.notification.ChankoNotification
import com.hoabh.chanko.domain.notification.DailyEmailSummary
import com.hoabh.chanko.domain.notification.NotificationAction
import com.hoabh.chanko.domain.notification.NotificationType
import java.util.UUID

/**
 * Bộ điều phối tạo thông báo Push Notification và tổng hợp Daily Email.
 *
 * Tuân thủ nghiêm ngặt nguyên tắc:
 * 1. Chủ động hỏi tình trạng mua hàng để người dùng tự phản tư (không bắt buộc mua).
 * 2. Nhắc nhở Daily Check-in.
 * 3. Báo hoàn thành thời gian chờ Cooling-off để xem xét lại nhu cầu.
 * 4. Thông báo nhận thưởng Hạt/Pate và Chanko Level Up.
 * 5. Tuyệt đối không dùng thông báo để kích thích, hối thúc hay khuyến khích mua sắm.
 */
class NotificationDispatcher {

    companion object {
        const val ACTION_NOT_BOUGHT = "action_not_bought"
        const val ACTION_BOUGHT = "action_bought"
        const val ACTION_LATER = "action_later"

        private val FORBIDDEN_IMPULSE_PHRASES = listOf(
            "mua ngay", "chớp lấy", "cơ hội", "giảm giá",
            "deal hot", "deal sốc", "khuyến mãi", "sale",
            "kẻo lỡ", "nhanh tay", "đừng bỏ lỡ"
        )
    }

    /**
     * Thông báo chủ động hỏi thăm tình trạng mua sắm của các món đồ đã thêm.
     */
    fun buildPurchaseInquiryNotification(activeItemsCount: Int): ChankoNotification? {
        if (activeItemsCount <= 0) return null

        val notification = ChankoNotification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.PURCHASE_INQUIRY,
            title = "Kiểm tra quyết định mua sắm cùng Chanko",
            body = "Bạn có mua món đồ nào trong danh sách gần đây chưa? Hãy cùng Chanko ghi nhận nhé.",
            actions = listOf(
                NotificationAction(ACTION_NOT_BOUGHT, "Chưa mua gì cả"),
                NotificationAction(ACTION_BOUGHT, "Đã mua món đồ"),
                NotificationAction(ACTION_LATER, "Để sau")
            )
        )
        validateNoImpulseNudge(notification)
        return notification
    }

    /**
     * Nhắc nhở Daily Check-in vào khung giờ tối.
     */
    fun buildDailyCheckInReminder(): ChankoNotification {
        val notification = ChankoNotification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.DAILY_CHECKIN_REMINDER,
            title = "Nhắc nhở Daily Check-in hôm nay",
            body = "Dành 10 giây để xác nhận bạn đã kiên định không mua sắm ngoài dự kiến và nhận Hạt cùng Pate nhé!"
        )
        validateNoImpulseNudge(notification)
        return notification
    }

    /**
     * Thông báo món đồ đã hoàn thành thời gian chờ và sẵn sàng xem xét lại nhu cầu.
     */
    fun buildFreezeCompletedNotification(itemName: String, itemId: String): ChankoNotification {
        val notification = ChankoNotification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.FREEZE_COMPLETED,
            title = "Món đồ đã sẵn sàng xem xét",
            body = "Thời gian hạ nhiệt cho món \"$itemName\" đã hoàn thành. Bạn còn muốn sở hữu món này không?",
            targetItemId = itemId
        )
        validateNoImpulseNudge(notification)
        return notification
    }

    /**
     * Thông báo nhận thưởng Hạt và Pate khi kết thúc thời gian chờ hợp lệ.
     */
    fun buildRewardGrantedNotification(
        hatEarned: Long,
        pateEarned: Long,
        itemName: String
    ): ChankoNotification {
        val notification = ChankoNotification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.REWARD_GRANTED,
            title = "Thưởng Cooling-off hoàn thành",
            body = "Bạn đã nhận được +$hatEarned Hạt và +$pateEarned Pate cho món \"$itemName\"!"
        )
        validateNoImpulseNudge(notification)
        return notification
    }

    /**
     * Thông báo điều chỉnh tài nguyên hồi tố khi ghi nhận mua đồ muộn.
     */
    fun buildRetroactiveAdjustmentNotification(
        hatDeducted: Long,
        pateDeducted: Long,
        itemName: String
    ): ChankoNotification {
        val notification = ChankoNotification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.RETROACTIVE_ADJUSTMENT,
            title = "Điều chỉnh tài nguyên hồi tố",
            body = "Giao dịch mua \"$itemName\" trong thời gian chờ đã được ghi nhận. Hệ thống đã khấu trừ $hatDeducted Hạt và $pateDeducted Pate."
        )
        validateNoImpulseNudge(notification)
        return notification
    }

    /**
     * Thông báo Chanko lên cấp độ mới.
     */
    fun buildLevelUpNotification(newLevel: Int): ChankoNotification {
        val notification = ChankoNotification(
            id = UUID.randomUUID().toString(),
            type = NotificationType.LEVEL_UP,
            title = "Chanko đã lớn hơn rồi!",
            body = "Chúc mừng! Chanko đã chính thức đạt Cấp độ $newLevel nhờ sự kiên trì tích lũy Pate của bạn."
        )
        validateNoImpulseNudge(notification)
        return notification
    }

    /**
     * Tạo bản tin tổng hợp Daily Email.
     */
    fun generateDailyEmailSummary(
        recipientEmail: String,
        dateFormatted: String,
        frozenItemsCount: Int,
        readyItemsCount: Int,
        currentHatBalance: Long,
        currentPateBalance: Long,
        chankoLevel: Int,
        budgetRemainingVnd: Long
    ): DailyEmailSummary {
        val subject = "Bản tin Chanko ngày $dateFormatted: Tình hình hạ nhiệt & Ngân sách"
        val bodyHtml = """
            <html>
            <body>
                <h2>Bản tin Chanko hàng ngày ($dateFormatted)</h2>
                <p>Chào bạn, dưới đây là tổng quan tình trạng chi tiêu và Chanko hôm nay:</p>
                <ul>
                    <li><b>Đang Cooling-off:</b> $frozenItemsCount món đồ</li>
                    <li><b>Sẵn sàng xem xét:</b> $readyItemsCount món đồ</li>
                    <li><b>Ngân sách khả dụng còn lại:</b> ${budgetRemainingVnd}₫</li>
                    <li><b>Tài nguyên tích lũy:</b> $currentHatBalance Hạt | $currentPateBalance Pate</li>
                    <li><b>Cấp độ Chanko:</b> Level $chankoLevel</li>
                </ul>
                <p>Hãy mở Chanko khi bạn rảnh để xem lại các quyết định mua sắm của mình nhé.</p>
            </body>
            </html>
        """.trimIndent()

        return DailyEmailSummary(
            recipientEmail = recipientEmail,
            subject = subject,
            dateFormatted = dateFormatted,
            frozenItemsCount = frozenItemsCount,
            readyItemsCount = readyItemsCount,
            currentHatBalance = currentHatBalance,
            currentPateBalance = currentPateBalance,
            chankoLevel = chankoLevel,
            budgetRemainingVnd = budgetRemainingVnd,
            bodyHtml = bodyHtml
        )
    }

    private fun validateNoImpulseNudge(notification: ChankoNotification) {
        val fullText = "${notification.title} ${notification.body}".lowercase()
        for (phrase in FORBIDDEN_IMPULSE_PHRASES) {
            if (fullText.contains(phrase)) {
                throw IllegalStateException("Vi phạm nguyên tắc: Thông báo chứa từ ngữ kích thích mua sắm '$phrase'")
            }
        }
    }
}
