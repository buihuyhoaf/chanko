package com.hoabh.chanko.presentation.screens.myitems

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyItemsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeItemRepository
    private lateinit var viewModel: MyItemsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeItemRepository()
        viewModel = MyItemsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun itemsLoaded_calculatesTilesAndMapsCorrectly() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val coolingItem = CooldownItem(
            id = 1,
            name = "Bàn phím cơ",
            price = 2_500_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 86_400_000L, // 1 ngày
            reason = "Muốn gõ êm hơn",
            status = ItemStatus.COOLING_DOWN
        )
        val readyItem = CooldownItem(
            id = 2,
            name = "Tai nghe chống ồn",
            price = 4_000_000L,
            addedTimestamp = now - 100_000_000L,
            cooldownDurationMillis = 86_400_000L, // Đã hết cooldown -> Ready
            reason = "Làm việc yên tĩnh",
            status = ItemStatus.COOLING_DOWN
        )
        val boughtItem = CooldownItem(
            id = 3,
            name = "Chuột không dây",
            price = 1_200_000L,
            addedTimestamp = now - 200_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Cần dùng ngay",
            status = ItemStatus.BOUGHT
        )

        fakeRepository.setActive(listOf(coolingItem, readyItem))
        fakeRepository.setBought(listOf(boughtItem))

        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(3, state.totalCount)
        assertEquals(1, state.coolingOffCount)
        assertEquals(1, state.readyCount)
        assertEquals(3, state.items.size)
    }

    @Test
    fun overviewTiles_filterItemsAppropriately() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val coolingItem = CooldownItem(
            id = 1,
            name = "Bàn phím cơ",
            price = 2_500_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 86_400_000L,
            reason = "Thích",
            status = ItemStatus.COOLING_DOWN
        )
        val readyItem = CooldownItem(
            id = 2,
            name = "Tai nghe chống ồn",
            price = 4_000_000L,
            addedTimestamp = now - 100_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Cần",
            status = ItemStatus.COOLING_DOWN
        )
        fakeRepository.setActive(listOf(coolingItem, readyItem))

        runCurrent()

        // Nhấn tile COOLING_OFF
        viewModel.onEvent(MyItemsUiEvent.OverviewTileClicked(OverviewTileType.COOLING_OFF))
        runCurrent()

        var state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(1, state.items.first().id)
        assertEquals(ItemDisplayStatus.CoolingOff, state.items.first().displayStatus)

        // Nhấn tile READY
        viewModel.onEvent(MyItemsUiEvent.OverviewTileClicked(OverviewTileType.READY))
        runCurrent()

        state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(2, state.items.first().id)
        assertEquals(ItemDisplayStatus.ReadyForReview, state.items.first().displayStatus)
    }

    @Test
    fun searchAndSort_updatesListCorrectly() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val itemA = CooldownItem(
            id = 1,
            name = "Áo khoác gió",
            price = 500_000L,
            addedTimestamp = now - 1000,
            cooldownDurationMillis = 86_400_000L,
            reason = "Trời lạnh",
            status = ItemStatus.COOLING_DOWN
        )
        val itemB = CooldownItem(
            id = 2,
            name = "Balo du lịch",
            price = 1_500_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 86_400_000L,
            reason = "Đi phượt",
            status = ItemStatus.COOLING_DOWN
        )
        fakeRepository.setActive(listOf(itemA, itemB))
        runCurrent()

        // Search "balo"
        viewModel.onEvent(MyItemsUiEvent.SearchQueryChanged("balo"))
        runCurrent()

        var state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Balo du lịch", state.items.first().title)

        // Clear search & Sort theo Giá thấp đến cao
        viewModel.onEvent(MyItemsUiEvent.SearchQueryChanged(""))
        viewModel.onEvent(MyItemsUiEvent.SortOptionSelected(ItemSortOption.PRICE_ASC))
        runCurrent()

        state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals(500_000L, state.items[0].rawPrice)
        assertEquals(1_500_000L, state.items[1].rawPrice)
    }

    private class FakeItemRepository : ItemRepository {
        private val activeFlow = MutableStateFlow<List<CooldownItem>>(emptyList())
        private val boughtFlow = MutableStateFlow<List<CooldownItem>>(emptyList())
        private val passedFlow = MutableStateFlow<List<CooldownItem>>(emptyList())

        fun setActive(items: List<CooldownItem>) {
            activeFlow.value = items
        }

        fun setBought(items: List<CooldownItem>) {
            boughtFlow.value = items
        }

        fun setPassed(items: List<CooldownItem>) {
            passedFlow.value = items
        }

        override fun getActiveItems(): Flow<List<CooldownItem>> = activeFlow.asStateFlow()

        override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = when (status) {
            ItemStatus.COOLING_DOWN -> activeFlow.asStateFlow()
            ItemStatus.BOUGHT -> boughtFlow.asStateFlow()
            ItemStatus.PASSED -> passedFlow.asStateFlow()
        }

        override fun getItemById(id: Int): Flow<CooldownItem?> {
            val all = activeFlow.value + boughtFlow.value + passedFlow.value
            return MutableStateFlow(all.find { it.id == id })
        }

        override fun getArchivedItems(): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override suspend fun setArchived(id: Int, isArchived: Boolean) {}

        override suspend fun insertItem(item: CooldownItem) {}
        override suspend fun updateItem(item: CooldownItem) {}
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {}
        override suspend fun deleteItem(item: CooldownItem) {}
        override fun getTotalSavedAmount(): Flow<Long> = MutableStateFlow(0L)
    }
}
