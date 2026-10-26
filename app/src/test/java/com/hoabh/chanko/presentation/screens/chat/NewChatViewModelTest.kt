package com.hoabh.chanko.presentation.screens.chat

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatMode
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatUiEffect
import com.hoabh.chanko.presentation.screens.chat.viewmodel.NewChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeItemRepository: FakeItemRepository
    private lateinit var viewModel: NewChatViewModel

    private val sampleItems = listOf(
        CooldownItem(
            id = 1,
            name = "Bàn phím cơ Custom",
            price = 2_500_000L,
            addedTimestamp = System.currentTimeMillis(),
            cooldownDurationMillis = 7 * 86_400_000L,
            reason = "Muốn gõ phím êm hơn",
            status = ItemStatus.COOLING_DOWN
        ),
        CooldownItem(
            id = 2,
            name = "Tai nghe chống ồn",
            price = 4_200_000L,
            addedTimestamp = System.currentTimeMillis(),
            cooldownDurationMillis = 5 * 86_400_000L,
            reason = "Tập trung làm việc",
            status = ItemStatus.COOLING_DOWN
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeItemRepository = FakeItemRepository()
        fakeItemRepository.setActiveItems(sampleItems)
        viewModel = NewChatViewModel(fakeItemRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsActiveItemsAndDefaultsToFreeMode() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(NewChatMode.FREE, state.selectedMode)
        assertEquals(2, state.items.size)
        assertTrue(state.canStartChat)
        assertEquals("Bắt đầu trò chuyện", state.ctaButtonText)
    }

    @Test
    fun selectMode_updatesModeAndCanStartChat() = runTest {
        advanceUntilIdle()

        viewModel.selectMode(NewChatMode.ITEM)
        val state = viewModel.uiState.value

        assertEquals(NewChatMode.ITEM, state.selectedMode)
        assertFalse(state.canStartChat)
        assertEquals("Chọn một món đồ", state.ctaButtonText)
    }

    @Test
    fun selectItem_enablesStartChatWithItemTitle() = runTest {
        advanceUntilIdle()

        viewModel.selectMode(NewChatMode.ITEM)
        viewModel.selectItem(sampleItems[0])
        val state = viewModel.uiState.value

        assertEquals(sampleItems[0], state.selectedItem)
        assertTrue(state.canStartChat)
        assertEquals("Trò chuyện về Bàn phím cơ Custom", state.ctaButtonText)
    }

    @Test
    fun searchQuery_filtersItems() = runTest {
        advanceUntilIdle()

        viewModel.updateSearchQuery("tai nghe")
        val state = viewModel.uiState.value

        assertEquals(1, state.filteredItems.size)
        assertEquals("Tai nghe chống ồn", state.filteredItems[0].name)
    }

    @Test
    fun startChat_inFreeMode_emitsNavigateEffectWithFreeContext() = runTest {
        advanceUntilIdle()

        var emittedEffect: NewChatUiEffect? = null
        val job = launch {
            emittedEffect = viewModel.effect.first()
        }

        viewModel.startChat()
        advanceUntilIdle()

        assertTrue(emittedEffect is NewChatUiEffect.NavigateToChatDetail)
        val navEffect = emittedEffect as NewChatUiEffect.NavigateToChatDetail
        assertEquals("FREE", navEffect.contextMode)
        assertEquals(null, navEffect.itemId)

        job.cancel()
    }

    @Test
    fun startChat_inItemMode_emitsNavigateEffectWithItemContext() = runTest {
        advanceUntilIdle()

        viewModel.selectMode(NewChatMode.ITEM)
        viewModel.selectItem(sampleItems[1])

        var emittedEffect: NewChatUiEffect? = null
        val job = launch {
            emittedEffect = viewModel.effect.first()
        }

        viewModel.startChat()
        advanceUntilIdle()

        assertTrue(emittedEffect is NewChatUiEffect.NavigateToChatDetail)
        val navEffect = emittedEffect as NewChatUiEffect.NavigateToChatDetail
        assertEquals("ITEM", navEffect.contextMode)
        assertEquals("2", navEffect.itemId)
        assertEquals("Tai nghe chống ồn", navEffect.itemName)
        assertEquals(4_200_000L, navEffect.itemPrice)

        job.cancel()
    }

    private class FakeItemRepository : ItemRepository {
        private val activeItemsFlow = MutableStateFlow<List<CooldownItem>>(emptyList())

        fun setActiveItems(items: List<CooldownItem>) {
            activeItemsFlow.value = items
        }

        override fun getActiveItems(): Flow<List<CooldownItem>> = activeItemsFlow.asStateFlow()
        override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override fun getArchivedItems(): Flow<List<CooldownItem>> = MutableStateFlow(emptyList())
        override fun getItemById(id: Int): Flow<CooldownItem?> = MutableStateFlow(null)
        override suspend fun insertItem(item: CooldownItem) {}
        override suspend fun updateItem(item: CooldownItem) {}
        override suspend fun updateItemStatus(id: Int, status: ItemStatus) {}
        override suspend fun setArchived(id: Int, isArchived: Boolean) {}
        override suspend fun deleteItem(item: CooldownItem) {}
        override fun getTotalSavedAmount(): Flow<Long> = MutableStateFlow(0L)
    }
}
