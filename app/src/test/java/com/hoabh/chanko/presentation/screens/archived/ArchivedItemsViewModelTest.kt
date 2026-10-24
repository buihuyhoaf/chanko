package com.hoabh.chanko.presentation.screens.archived

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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchivedItemsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeItemRepository
    private lateinit var viewModel: ArchivedItemsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeItemRepository()
        viewModel = ArchivedItemsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadArchivedItems_displaysAndFiltersByQuery() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val item1 = CooldownItem(
            id = 1,
            name = "Máy ảnh film",
            price = 3_000_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 5000L,
            reason = "Chụp ảnh du lịch",
            status = ItemStatus.PASSED,
            isArchived = true
        )
        val item2 = CooldownItem(
            id = 2,
            name = "Loa bluetooth",
            price = 1_500_000L,
            addedTimestamp = now,
            cooldownDurationMillis = 5000L,
            reason = "Nghe nhạc chill",
            status = ItemStatus.BOUGHT,
            isArchived = true
        )

        fakeRepository.setArchived(listOf(item1, item2))
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.items.size)

        // Search "máy ảnh"
        viewModel.onEvent(ArchivedItemsUiEvent.SearchQueryChanged("máy ảnh"))
        runCurrent()

        val filteredState = viewModel.uiState.value
        assertEquals(1, filteredState.items.size)
        assertEquals("Máy ảnh film", filteredState.items.first().title)
    }

    @Test
    fun restoreItem_callsRepository() = runTest(testDispatcher) {
        val item = CooldownItem(
            id = 1,
            name = "Máy ảnh film",
            price = 3_000_000L,
            addedTimestamp = 1000L,
            cooldownDurationMillis = 5000L,
            reason = "Test",
            status = ItemStatus.PASSED,
            isArchived = true
        )
        fakeRepository.setArchived(listOf(item))
        runCurrent()

        viewModel.onEvent(ArchivedItemsUiEvent.RestoreItem(1))
        runCurrent()

        assertEquals(1, fakeRepository.restoredItemId)
        assertEquals(false, fakeRepository.lastArchivedState)
    }

    private class FakeItemRepository : ItemRepository {
        private val archivedFlow = MutableStateFlow<List<CooldownItem>>(emptyList())
        var restoredItemId: Int? = null
        var lastArchivedState: Boolean? = null

        fun setArchived(items: List<CooldownItem>) {
            archivedFlow.value = items
        }

        override fun getArchivedItems(): Flow<List<CooldownItem>> = archivedFlow.asStateFlow()

        override suspend fun setArchived(id: Int, isArchived: Boolean) {
            restoredItemId = id
            lastArchivedState = isArchived
            archivedFlow.value = archivedFlow.value.filter { it.id != id }
        }

        override fun getActiveItems(): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override fun getItemById(id: Int): Flow<CooldownItem?> = MutableStateFlow(null)
        override suspend fun insertItem(item: CooldownItem) {}
        override suspend fun updateItem(item: CooldownItem) {}
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {}
        override suspend fun deleteItem(item: CooldownItem) {}
        override fun getTotalSavedAmount(): Flow<Long> = MutableStateFlow(0L)
    }
}
