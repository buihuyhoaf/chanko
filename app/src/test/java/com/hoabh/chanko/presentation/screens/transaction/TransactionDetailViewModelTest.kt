package com.hoabh.chanko.presentation.screens.transaction

import androidx.lifecycle.SavedStateHandle
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PaymentType
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePurchaseRepo: FakePurchaseRepository
    private lateinit var fakeItemRepo: FakeItemRepository
    private lateinit var viewModel: TransactionDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePurchaseRepo = FakePurchaseRepository()
        fakeItemRepo = FakeItemRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadTransaction_displaysDetailsCorrectly() = runTest(testDispatcher) {
        val item = CooldownItem(
            id = 10,
            name = "Giày chạy bộ",
            price = 2_000_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 5000L,
            reason = "Chạy thể dục",
            status = ItemStatus.BOUGHT
        )
        val tx = PurchaseTransaction(
            id = "tx-123",
            itemId = 10,
            actualUnitPrice = 1_800_000L,
            quantity = 2,
            totalAmount = 3_600_000L,
            paymentType = PaymentType.PAY_NOW,
            purchasedTimestamp = 2000L,
            note = "Mua đợt sale"
        )
        fakeItemRepo.setItem(item)
        fakePurchaseRepo.setPurchase(tx)

        val handle = SavedStateHandle(mapOf("purchaseId" to "tx-123"))
        viewModel = TransactionDetailViewModel(fakePurchaseRepo, fakeItemRepo, handle)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isNotFound)
        assertEquals("tx-123", state.purchaseId)
        assertEquals("Giày chạy bộ", state.itemName)
        assertEquals(3_600_000L, state.totalAmount)
        assertEquals(2, state.quantity)
    }

    @Test
    fun editTransaction_updatesValuesAndTogglesMode() = runTest(testDispatcher) {
        val tx = PurchaseTransaction(
            id = "tx-123",
            itemId = 10,
            actualUnitPrice = 1_800_000L,
            quantity = 1,
            totalAmount = 1_800_000L,
            paymentType = PaymentType.PAY_NOW,
            purchasedTimestamp = 2000L
        )
        fakePurchaseRepo.setPurchase(tx)

        val handle = SavedStateHandle(mapOf("purchaseId" to "tx-123"))
        viewModel = TransactionDetailViewModel(fakePurchaseRepo, fakeItemRepo, handle)
        runCurrent()

        // Sửa giá và số lượng
        viewModel.onEvent(TransactionDetailUiEvent.UpdateFields(actualUnitPrice = 2_000_000L, quantity = 3, paymentType = PaymentType.CREDIT, note = "Cập nhật lại"))
        runCurrent()

        val updated = fakePurchaseRepo.lastUpdated
        assertEquals(2_000_000L, updated?.actualUnitPrice)
        assertEquals(3, updated?.quantity)
        assertEquals(6_000_000L, updated?.totalAmount)
        assertEquals(PaymentType.CREDIT, updated?.paymentType)
    }

    @Test
    fun deleteTransaction_dialogFlowAndRepositoryCall() = runTest(testDispatcher) {
        val tx = PurchaseTransaction(
            id = "tx-123",
            itemId = 10,
            actualUnitPrice = 1_800_000L,
            quantity = 1,
            totalAmount = 1_800_000L,
            paymentType = PaymentType.PAY_NOW,
            purchasedTimestamp = 2000L
        )
        fakePurchaseRepo.setPurchase(tx)

        val handle = SavedStateHandle(mapOf("purchaseId" to "tx-123"))
        viewModel = TransactionDetailViewModel(fakePurchaseRepo, fakeItemRepo, handle)
        runCurrent()

        viewModel.onEvent(TransactionDetailUiEvent.RequestDelete)
        assertTrue(viewModel.uiState.value.isDeleteDialogOpen)

        viewModel.onEvent(TransactionDetailUiEvent.ConfirmDelete)
        runCurrent()

        assertFalse(viewModel.uiState.value.isDeleteDialogOpen)
        assertEquals("tx-123", fakePurchaseRepo.deletedId)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        private val txFlow = MutableStateFlow<PurchaseTransaction?>(null)
        var lastUpdated: PurchaseTransaction? = null
        var deletedId: String? = null

        fun setPurchase(tx: PurchaseTransaction?) {
            txFlow.value = tx
        }

        override fun getPurchasesByItemId(itemId: Int): Flow<List<PurchaseTransaction>> =
            MutableStateFlow(listOfNotNull(txFlow.value))

        override fun getPurchaseById(id: String): Flow<PurchaseTransaction?> = txFlow.asStateFlow()

        override suspend fun recordPurchase(purchase: PurchaseTransaction) {
            txFlow.value = purchase
        }

        override suspend fun updatePurchase(purchase: PurchaseTransaction) {
            lastUpdated = purchase
            txFlow.value = purchase
        }

        override suspend fun deletePurchase(purchaseId: String) {
            deletedId = purchaseId
            txFlow.value = null
        }
    }

    private class FakeItemRepository : ItemRepository {
        private val itemFlow = MutableStateFlow<CooldownItem?>(null)

        fun setItem(item: CooldownItem?) {
            itemFlow.value = item
        }

        override fun getItemById(id: Int): Flow<CooldownItem?> = itemFlow.asStateFlow()
        override fun getArchivedItems(): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override suspend fun setArchived(id: Int, isArchived: Boolean) {}
        override fun getActiveItems(): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override suspend fun insertItem(item: CooldownItem) {}
        override suspend fun updateItem(item: CooldownItem) {}
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {}
        override suspend fun deleteItem(item: CooldownItem) {}
        override fun getTotalSavedAmount(): Flow<Long> = MutableStateFlow(0L)
    }
}
