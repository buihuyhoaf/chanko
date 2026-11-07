package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.usecase.ItemPurchaseBudgetSyncUseCase
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PaymentType
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ItemPurchaseBudgetSyncTest {

    private lateinit var fakeBudgetRepository: FakeBudgetRepository
    private lateinit var fakeItemRepository: MockItemRepository
    private lateinit var fakePurchaseRepository: MockPurchaseRepository
    private lateinit var syncUseCase: ItemPurchaseBudgetSyncUseCase

    @Before
    fun setUp() {
        fakeBudgetRepository = FakeBudgetRepository()
        fakeItemRepository = MockItemRepository()
        fakePurchaseRepository = MockPurchaseRepository()
        syncUseCase = ItemPurchaseBudgetSyncUseCase(
            itemRepository = fakeItemRepository,
            purchaseRepository = fakePurchaseRepository,
            budgetRepository = fakeBudgetRepository
        )
    }

    @Test
    fun recordItemPurchase_payNow_deductsBudgetAndUpdatesItemStatus() = runTest {
        val item = CooldownItem(
            id = 10,
            name = "Máy pha cà phê",
            price = 1_500_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 7 * 86_400_000L,
            reason = "Uống cà phê sáng",
            status = ItemStatus.COOLING_DOWN
        )
        fakeItemRepository.setItem(item)

        val initialSnapshot = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first()
        val initialSpent = initialSnapshot.spentMinor

        val purchase = PurchaseTransaction(
            id = "tx_1",
            itemId = 10,
            actualUnitPrice = 1_500_000L,
            quantity = 1,
            paymentType = PaymentType.PAY_NOW,
            purchasedTimestamp = System.currentTimeMillis()
        )

        val result = syncUseCase.recordItemPurchase(purchase, isOnline = true)
        assertTrue(result.isSuccess)

        // Item status is now BOUGHT
        assertEquals(ItemStatus.BOUGHT, fakeItemRepository.lastStatus)

        // Budget spent increased by 1_500_000L
        val updatedSnapshot = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first()
        assertEquals(initialSpent + 1_500_000L, updatedSnapshot.spentMinor)
    }

    @Test
    fun recordItemPurchase_creditCard_alsoRecordsExpenseImmediately() = runTest {
        val item = CooldownItem(
            id = 11,
            name = "Bàn phím cơ",
            price = 800_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 3 * 86_400_000L,
            reason = "Gõ phím",
            status = ItemStatus.COOLING_DOWN
        )
        fakeItemRepository.setItem(item)

        val initialSpent = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first().spentMinor

        val purchase = PurchaseTransaction(
            id = "tx_2",
            itemId = 11,
            actualUnitPrice = 800_000L,
            quantity = 1,
            paymentType = PaymentType.CREDIT,
            purchasedTimestamp = System.currentTimeMillis()
        )

        val result = syncUseCase.recordItemPurchase(purchase, isOnline = true)
        assertTrue(result.isSuccess)

        val updatedSpent = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first().spentMinor
        assertEquals(initialSpent + 800_000L, updatedSpent)
    }

    @Test
    fun recordItemPurchase_nonExistentItem_fails() = runTest {
        val purchase = PurchaseTransaction(
            id = "tx_unknown",
            itemId = 999, // Không có trong Chanko
            actualUnitPrice = 500_000L,
            quantity = 1,
            purchasedTimestamp = System.currentTimeMillis()
        )

        val result = syncUseCase.recordItemPurchase(purchase, isOnline = true)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun recordItemPurchase_offline_blocksAndReturnsOfflineException() = runTest {
        val item = CooldownItem(
            id = 12,
            name = "Giày thể thao",
            price = 1_000_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 3 * 86_400_000L,
            reason = "Chạy bộ"
        )
        fakeItemRepository.setItem(item)

        val purchase = PurchaseTransaction(
            id = "tx_offline",
            itemId = 12,
            actualUnitPrice = 1_000_000L,
            quantity = 1,
            purchasedTimestamp = System.currentTimeMillis()
        )

        val result = syncUseCase.recordItemPurchase(purchase, isOnline = false)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BudgetOfflineException)
    }

    @Test
    fun refundPurchase_restoresBudget() = runTest {
        val item = CooldownItem(
            id = 13,
            name = "Chuột không dây",
            price = 400_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 3 * 86_400_000L,
            reason = "Làm việc",
            status = ItemStatus.BOUGHT
        )
        fakeItemRepository.setItem(item)

        val purchase = PurchaseTransaction(
            id = "tx_refund",
            itemId = 13,
            actualUnitPrice = 400_000L,
            quantity = 1,
            purchasedTimestamp = System.currentTimeMillis()
        )
        fakePurchaseRepository.recordPurchase(purchase)
        fakeBudgetRepository.recordItemExpense("13", "Chuột không dây", 400_000L, PaymentType.PAY_NOW)

        val beforeRefundSpent = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first().spentMinor

        val result = syncUseCase.refundPurchase("tx_refund", itemId = 13, isOnline = true)
        assertTrue(result.isSuccess)

        val afterRefundSpent = fakeBudgetRepository.getBudgetSnapshot(fakeBudgetRepository.currentCycleId).first().spentMinor
        assertEquals(beforeRefundSpent - 400_000L, afterRefundSpent)
        assertEquals(ItemStatus.COOLING_DOWN, fakeItemRepository.lastStatus)
    }

    private class MockItemRepository : ItemRepository {
        private val itemFlow = MutableStateFlow<CooldownItem?>(null)
        var lastStatus: ItemStatus? = null

        fun setItem(item: CooldownItem) {
            itemFlow.value = item
            lastStatus = item.status
        }

        override fun getItemById(id: Int): Flow<CooldownItem?> = itemFlow.asStateFlow()
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {
            lastStatus = status
            itemFlow.value = itemFlow.value?.copy(status = status)
        }

        override fun getActiveItems() = MutableStateFlow(emptyList<CooldownItem>())
        override fun getItemsByStatus(status: ItemStatus) = MutableStateFlow(emptyList<CooldownItem>())
        override fun getArchivedItems() = MutableStateFlow(emptyList<CooldownItem>())
        override suspend fun insertItem(item: CooldownItem) {}
        override suspend fun updateItem(item: CooldownItem) {}
        override suspend fun setArchived(id: Int, isArchived: Boolean) {}
        override suspend fun deleteItem(item: CooldownItem) {}
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
}
