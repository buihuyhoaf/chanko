package com.hoabh.chanko.data.notification

import com.hoabh.chanko.domain.notification.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationDispatcherTest {

    private lateinit var dispatcher: NotificationDispatcher

    @Before
    fun setUp() {
        dispatcher = NotificationDispatcher()
    }

    @Test
    fun purchaseInquiry_withActiveItems_returnsNotificationWithThreeQuickActions() {
        val notification = dispatcher.buildPurchaseInquiryNotification(activeItemsCount = 3)
        assertNotNull(notification)
        assertEquals(NotificationType.PURCHASE_INQUIRY, notification!!.type)
        assertEquals(3, notification.actions.size)
        assertEquals(NotificationDispatcher.ACTION_NOT_BOUGHT, notification.actions[0].actionId)
        assertEquals(NotificationDispatcher.ACTION_BOUGHT, notification.actions[1].actionId)
        assertEquals(NotificationDispatcher.ACTION_LATER, notification.actions[2].actionId)
        assertTrue(notification.body.contains("Bạn có mua món đồ nào"))
    }

    @Test
    fun purchaseInquiry_withZeroActiveItems_returnsNull() {
        val notification = dispatcher.buildPurchaseInquiryNotification(activeItemsCount = 0)
        assertNull(notification)
    }

    @Test
    fun dailyCheckInReminder_returnsValidNotification() {
        val notification = dispatcher.buildDailyCheckInReminder()
        assertEquals(NotificationType.DAILY_CHECKIN_REMINDER, notification.type)
        assertTrue(notification.body.contains("kiên định không mua sắm"))
    }

    @Test
    fun freezeCompleted_includesItemNameAndTargetId() {
        val notification = dispatcher.buildFreezeCompletedNotification(
            itemName = "Bàn phím cơ Keychron",
            itemId = "item_123"
        )
        assertEquals(NotificationType.FREEZE_COMPLETED, notification.type)
        assertEquals("item_123", notification.targetItemId)
        assertTrue(notification.body.contains("Bàn phím cơ Keychron"))
        assertTrue(notification.title.contains("sẵn sàng"))
    }

    @Test
    fun rewardGranted_includesHatAndPateDetails() {
        val notification = dispatcher.buildRewardGrantedNotification(
            hatEarned = 20,
            pateEarned = 20,
            itemName = "Chuột Logitech"
        )
        assertEquals(NotificationType.REWARD_GRANTED, notification.type)
        assertTrue(notification.body.contains("+20 Hạt"))
        assertTrue(notification.body.contains("+20 Pate"))
        assertTrue(notification.body.contains("Chuột Logitech"))
    }

    @Test
    fun retroactiveAdjustment_reportsCorrectDeduction() {
        val notification = dispatcher.buildRetroactiveAdjustmentNotification(
            hatDeducted = 15,
            pateDeducted = 10,
            itemName = "Áo khoác gió"
        )
        assertEquals(NotificationType.RETROACTIVE_ADJUSTMENT, notification.type)
        assertTrue(notification.body.contains("Áo khoác gió"))
        assertTrue(notification.body.contains("15 Hạt"))
        assertTrue(notification.body.contains("10 Pate"))
    }

    @Test
    fun levelUp_celebratesLevelIncrease() {
        val notification = dispatcher.buildLevelUpNotification(newLevel = 5)
        assertEquals(NotificationType.LEVEL_UP, notification.type)
        assertTrue(notification.title.contains("lớn hơn"))
        assertTrue(notification.body.contains("Cấp độ 5"))
    }

    @Test
    fun dailyEmailSummary_generatesAccurateReportHtml() {
        val email = dispatcher.generateDailyEmailSummary(
            recipientEmail = "user@example.com",
            dateFormatted = "07/11/2026",
            frozenItemsCount = 2,
            readyItemsCount = 1,
            currentHatBalance = 150L,
            currentPateBalance = 320L,
            chankoLevel = 3,
            budgetRemainingVnd = 1_500_000L
        )
        assertEquals("user@example.com", email.recipientEmail)
        assertTrue(email.subject.contains("07/11/2026"))
        assertTrue(email.bodyHtml.contains("2 món đồ"))
        assertTrue(email.bodyHtml.contains("1 món đồ"))
        assertTrue(email.bodyHtml.contains("1500000₫"))
        assertTrue(email.bodyHtml.contains("150 Hạt"))
        assertTrue(email.bodyHtml.contains("Level 3"))
    }
}
