package com.hoabh.chanko.e2e

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.data.notification.NotificationDispatcher
import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.usecase.ItemPurchaseBudgetSyncUseCase
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.FreezeState
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PaymentType
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.policy.FreezeDurationPolicy
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import com.hoabh.chanko.domain.rewards.FreezeRewardDispatcher
import com.hoabh.chanko.domain.rewards.FreezeRewardPolicy
import com.hoabh.chanko.domain.rewards.MascotEvent
import com.hoabh.chanko.domain.rewards.MascotReactionPolicy
import com.hoabh.chanko.domain.rewards.ResourceBalance
import com.hoabh.chanko.domain.rewards.RetroactivePurchaseHandler
import com.hoabh.chanko.domain.rewards.UnfreezeCostCalculator
import com.hoabh.chanko.presentation.base.OfflineWriteGate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Kiểm thử tích hợp toàn diện E2E (End-to-End) cho hệ sinh thái Chanko Core:
 * 1. Add Item -> Quick Check -> Mandatory Freeze -> Price Lock.
 * 2. Countdown -> Expire -> Ready for Review -> Auto Reward (Daily Cap) -> Mascot Auto Level-Up.
 * 3. Early Unfreeze Flow -> Cost calculation -> Non-negative balances -> Neutral Mascot Reaction.
 * 4. Retroactive Late Purchase -> Reward clawback -> Retroactive fee.
 * 5. Purchase Confirmation -> Cycle Budget deduction -> Offline Gate safety.
 */
class ChankoCoreE2ETest {

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeItemRepository: MockItemRepository
    private lateinit var fakePurchaseRepository: MockPurchaseRepository
    private lateinit var offlineWriteGate: OfflineWriteGate
    private lateinit var syncUseCase: ItemPurchaseBudgetSyncUseCase
    private lateinit var notificationDispatcher: NotificationDispatcher

    private class MockItemRepository : ItemRepository {
        private val itemFlow = MutableStateFlow<CooldownItem?>(null)
        val items = mutableMapOf<Int, CooldownItem>()
        var lastStatus: ItemStatus? = null

        fun setItem(item: CooldownItem) {
            items[item.id] = item
            itemFlow.value = item
            lastStatus = item.status
        }

        override fun getItemById(id: Int): Flow<CooldownItem?> = MutableStateFlow(items[id])
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {
            lastStatus = status
            items[id] = items[id]?.copy(status = status) ?: return
            itemFlow.value = items[id]
        }

        override fun getActiveItems() = MutableStateFlow(items.values.filter { it.status == ItemStatus.COOLING_DOWN })
        override fun getItemsByStatus(status: ItemStatus) = MutableStateFlow(items.values.filter { it.status == status })
        override fun getArchivedItems() = MutableStateFlow(emptyList<CooldownItem>())
        override suspend fun insertItem(item: CooldownItem) {
            setItem(item)
        }
        override suspend fun updateItem(item: CooldownItem) {
            setItem(item)
        }
        override suspend fun setArchived(id: Int, isArchived: Boolean) {}
        override suspend fun deleteItem(item: CooldownItem) {
            items.remove(item.id)
        }
        override fun getTotalSavedAmount() = MutableStateFlow(0L)
    }

    private class MockPurchaseRepository : PurchaseRepository {
        private val purchases = mutableMapOf<String, PurchaseTransaction>()

        override fun getPurchasesByItemId(itemId: Int): Flow<List<PurchaseTransaction>> =
            MutableStateFlow(purchases.values.filter { it.itemId == itemId })

        override fun getPurchaseById(id: String): Flow<PurchaseTransaction?> =
            MutableStateFlow(purchases[id])

        override suspend fun recordPurchase(purchase: PurchaseTransaction) {
            purchases[purchase.id] = purchase
        }

        override suspend fun updatePurchase(purchase: PurchaseTransaction) {
            purchases[purchase.id] = purchase
        }

        override suspend fun deletePurchase(purchaseId: String) {
            purchases.remove(purchaseId)
        }
    }

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeItemRepository = MockItemRepository()
        fakePurchaseRepository = MockPurchaseRepository()
        offlineWriteGate = OfflineWriteGate()
        syncUseCase = ItemPurchaseBudgetSyncUseCase(
            itemRepository = fakeItemRepository,
            purchaseRepository = fakePurchaseRepository,
            budgetRepository = fakeBudgetRepository
        )
        notificationDispatcher = NotificationDispatcher()
    }

    @Test
    fun fullHappyPath_itemAdditionToFreezeToRewardToPurchase() = runTest {
        val now = 1_700_000_000_000L
        val itemPrice = 1_500_000L // Tier 1m - 5m: 7 days

        // 1. Thêm sản phẩm & Tính toán thời gian Freeze bắt buộc
        val durationMillis = FreezeDurationPolicy.calculateDurationMillis(itemPrice)
        assertEquals(TimeUnit.DAYS.toMillis(7), durationMillis)

        val item = CooldownItem(
            id = 101,
            name = "Giày thể thao chạy bộ",
            price = itemPrice,
            addedTimestamp = now,
            cooldownDurationMillis = durationMillis,
            reason = "Chạy bộ mỗi sáng",
            status = ItemStatus.COOLING_DOWN
        )
        fakeItemRepository.insertItem(item)

        assertFalse(item.canDecide(now))
        assertEquals(durationMillis, item.remainingMillis(now))

        // 2. Thời gian chờ kết thúc -> Sẵn sàng xem xét
        val finishTime = now + durationMillis + 1_000L
        assertTrue(item.canDecide(finishTime))
        assertEquals(0L, item.remainingMillis(finishTime))

        // 3. Phân bổ phần thưởng Freeze tự động & giới hạn 1 lần/ngày
        val freezeState = FreezeState(
            lockedPrice = item.price,
            startedAtMillis = now,
            durationMillis = durationMillis,
            isRefrozen = false,
            rewardClaimed = false
        )
        val initialBalance = ResourceBalance(hatBalance = 10L, pateBalance = 80L, currentLevel = 1)
        val dispatchResult = FreezeRewardDispatcher.dispatchDailyFreezeRewards(
            candidates = listOf(Pair(item, freezeState)),
            currentBalance = initialBalance,
            alreadyClaimedFreezeRewardToday = false
        )
        assertEquals(101, dispatchResult.rewardedItemId)
        assertTrue(dispatchResult.grantedHat > 0)
        assertTrue(dispatchResult.grantedPate > 0)

        // Tính thưởng dựa theo giá đã khóa
        val reward = FreezeRewardPolicy.calculateReward(item.price)
        assertTrue(reward.hatReward > 0)
        assertTrue(reward.pateReward > 0)

        // 4. Mascot tự động nhận Pate và Level Up
        val newBalance = dispatchResult.updatedBalance
        // Ban đầu 80 Pate + thưởng >= 20 Pate -> đủ 100 Pate để lên Level 2
        assertTrue(newBalance.pateBalance >= 100L)
        assertEquals(2, newBalance.currentLevel)

        val levelUpReaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.LevelUp(newLevel = newBalance.currentLevel)
        )
        assertTrue(levelUpReaction.isCelebratory)

        // 5. Xác nhận mua món đồ -> Ghi nhận ngân sách chu kỳ hiện tại
        val initialSnapshot = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first()
        val initialSpent = initialSnapshot.spentMinor

        val purchase = PurchaseTransaction(
            id = "tx_101",
            itemId = 101,
            actualUnitPrice = item.price,
            quantity = 1,
            paymentType = PaymentType.PAY_NOW,
            purchasedTimestamp = finishTime
        )
        val syncResult = syncUseCase.recordItemPurchase(purchase, isOnline = true)
        assertTrue(syncResult.isSuccess)

        // Ngân sách chu kỳ hiện tại bị trừ chính xác số tiền món đồ
        val updatedSnapshot = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first()
        assertEquals(initialSpent + item.price, updatedSnapshot.spentMinor)
        assertEquals(ItemStatus.BOUGHT, fakeItemRepository.lastStatus)
    }

    @Test
    fun earlyUnfreezePath_calculatesPenalty_balancesNeverNegative_neutralReaction() {
        val totalDuration = TimeUnit.DAYS.toMillis(3)
        val lockedPrice = 600_000L

        // Sau 1 ngày (còn 2 ngày)
        val remaining = TimeUnit.DAYS.toMillis(2)

        val fee = UnfreezeCostCalculator.calculateFee(
            lockedPriceVnd = lockedPrice,
            remainingMillis = remaining,
            totalDurationMillis = totalDuration
        )
        assertTrue(fee.hatFee > 0)
        assertTrue(fee.pateFee > 0)

        // Người dùng có số dư ít hơn số phí phạt -> Ledger chặn sàn ở 0, level giữ nguyên
        val startingBalance = ResourceBalance(hatBalance = 5L, pateBalance = 10L, currentLevel = 3)
        val debitedBalance = startingBalance.debit(fee.hatFee, fee.pateFee)

        assertEquals(0L, debitedBalance.hatBalance)
        assertEquals(0L, debitedBalance.pateBalance)
        assertEquals(3, debitedBalance.currentLevel) // Không bị giáng cấp

        // Phản ứng Mascot phải trung lập
        val unfreezeReaction = MascotReactionPolicy.resolveReaction(
            MascotEvent.EarlyUnfreeze(fee.hatFee, fee.pateFee)
        )
        assertFalse(unfreezeReaction.isCelebratory)
        assertEquals(com.hoabh.chanko.domain.chat.model.MascotEmotion.NEUTRAL, unfreezeReaction.emotion)
    }

    @Test
    fun retroactiveLatePurchase_clawsBackFreezeReward_appliesRetroactiveFee() {
        val freezeStart = 1_700_000_000_000L
        val totalDuration = TimeUnit.DAYS.toMillis(7)
        val lockedPrice = 2_000_000L

        val freezeState = FreezeState(
            lockedPrice = lockedPrice,
            startedAtMillis = freezeStart,
            durationMillis = totalDuration,
            rewardClaimed = true
        )

        // Người dùng thực tế mua vào ngày thứ 2 (khi còn 5 ngày Freeze)
        val actualPurchaseTime = freezeStart + TimeUnit.DAYS.toMillis(2)

        val startingBalance = ResourceBalance(hatBalance = 150L, pateBalance = 100L, currentLevel = 2)
        val adjustment = RetroactivePurchaseHandler.processPurchase(
            currentBalance = startingBalance,
            freezeState = freezeState,
            actualPurchaseTimestamp = actualPurchaseTime,
            alreadyUnfrozenEarly = false
        )

        // Thu hồi thưởng Freeze
        assertTrue(adjustment.clawedBackRewardHat > 0)
        assertTrue(adjustment.clawedBackRewardPate > 0)
        // Áp dụng phí phạt Unfreeze tại thời điểm mua thực tế
        assertTrue(adjustment.appliedUnfreezeFeeHat > 0)
        assertTrue(adjustment.appliedUnfreezeFeePate > 0)

        // Tạo thông báo điều chỉnh hồi tố
        val noti = notificationDispatcher.buildRetroactiveAdjustmentNotification(
            hatDeducted = adjustment.appliedUnfreezeFeeHat,
            pateDeducted = adjustment.appliedUnfreezeFeePate,
            itemName = "Đồng hồ thông minh"
        )
        assertTrue(noti.body.contains("Đồng hồ thông minh"))
    }

    @Test
    fun offlineGateSafety_blocksPurchaseWhenOffline_allowsWhenOnline() = runTest {
        val item = CooldownItem(
            id = 202,
            name = "Loa Bluetooth",
            price = 500_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 3 * 86_400_000L,
            reason = "Nghe nhạc",
            status = ItemStatus.COOLING_DOWN
        )
        fakeItemRepository.insertItem(item)

        val purchase = PurchaseTransaction(
            id = "tx_202",
            itemId = 202,
            actualUnitPrice = 500_000L,
            quantity = 1,
            paymentType = PaymentType.CREDIT,
            purchasedTimestamp = System.currentTimeMillis()
        )

        // Khi offline: Thao tác ghi bị chặn, trả về BudgetOfflineException
        val offlineAttempt = syncUseCase.recordItemPurchase(purchase, isOnline = false)
        assertTrue(offlineAttempt.isFailure)
        assertTrue(offlineAttempt.exceptionOrNull() is BudgetOfflineException)

        // Khi online: Thành công
        val onlineAttempt = syncUseCase.recordItemPurchase(purchase, isOnline = true)
        assertTrue(onlineAttempt.isSuccess)
    }
}
