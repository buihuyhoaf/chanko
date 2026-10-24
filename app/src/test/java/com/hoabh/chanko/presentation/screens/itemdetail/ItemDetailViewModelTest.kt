package com.hoabh.chanko.presentation.screens.itemdetail

import androidx.lifecycle.SavedStateHandle
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import com.hoabh.chanko.presentation.screens.myitems.ItemDisplayStatus
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
class ItemDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeItemRepository
    private lateinit var fakePurchaseRepository: FakePurchaseRepository
    private lateinit var viewModel: ItemDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeItemRepository()
        fakePurchaseRepository = FakePurchaseRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadItem_coolingDown_displaysCorrectState() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val item = CooldownItem(
            id = 42,
            name = "Màn hình cong 34 inch",
            price = 12_000_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 7 * 86_400_000L, // 7 ngày
            reason = "Muốn làm việc đa nhiệm tốt hơn",
            status = ItemStatus.COOLING_DOWN
        )
        fakeRepository.setItem(item)

        val savedStateHandle = SavedStateHandle(mapOf("itemId" to 42))
        viewModel = ItemDetailViewModel(fakeRepository, fakePurchaseRepository, savedStateHandle)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.itemNotFound)
        assertEquals(42, state.id)
        assertEquals("Màn hình cong 34 inch", state.name)
        assertEquals(ItemDisplayStatus.CoolingOff, state.displayStatus)
        assertTrue(state.remainingMillis > 0)
        assertTrue(state.decisionJourney.isNotEmpty())
    }

    @Test
    fun skipWaiting_triggersConfirmationAndUpdatesToReady() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val item = CooldownItem(
            id = 42,
            name = "Màn hình cong 34 inch",
            price = 12_000_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 7 * 86_400_000L,
            reason = "Thích",
            status = ItemStatus.COOLING_DOWN
        )
        fakeRepository.setItem(item)

        val savedStateHandle = SavedStateHandle(mapOf("itemId" to 42))
        viewModel = ItemDetailViewModel(fakeRepository, fakePurchaseRepository, savedStateHandle)
        runCurrent()

        // 1. Yêu cầu Skip Waiting -> mở confirmation dialog
        viewModel.onEvent(ItemDetailUiEvent.RequestSkipWaiting)
        assertTrue(viewModel.uiState.value.isSkipWaitingConfirmDialogOpen)

        // 2. Xác nhận Skip Waiting (GAP-02) -> Kết thúc chờ, chuyển sang Ready for Review
        viewModel.onEvent(ItemDetailUiEvent.ConfirmSkipWaiting)
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isSkipWaitingConfirmDialogOpen)
        assertEquals(ItemDisplayStatus.ReadyForReview, state.displayStatus)
        assertEquals(0L, state.remainingMillis)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        override fun getPurchasesByItemId(itemId: Int): Flow<List<PurchaseTransaction>> =
            MutableStateFlow(emptyList())

        override fun getPurchaseById(id: String): Flow<PurchaseTransaction?> =
            MutableStateFlow(null)

        override suspend fun recordPurchase(purchase: PurchaseTransaction) {}
        override suspend fun updatePurchase(purchase: PurchaseTransaction) {}
        override suspend fun deletePurchase(purchaseId: String) {}
    }

    private class FakeItemRepository : ItemRepository {
        private val itemFlow = MutableStateFlow<CooldownItem?>(null)

        fun setItem(item: CooldownItem?) {
            itemFlow.value = item
        }

        override fun getItemById(id: Int): Flow<CooldownItem?> = itemFlow.asStateFlow()

        override fun getActiveItems(): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override suspend fun insertItem(item: CooldownItem) {}
        override suspend fun updateItem(item: CooldownItem) {}
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {}
        override suspend fun deleteItem(item: CooldownItem) {}
        override fun getTotalSavedAmount(): Flow<Long> = MutableStateFlow(0L)
    }
}
