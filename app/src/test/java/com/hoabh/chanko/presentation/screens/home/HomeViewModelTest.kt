package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.screens.home.data.InMemoryHomeSpendingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fixedToday = LocalDate.of(2026, 10, 18)
    private val fixedNowMillis = 100_000L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialContent_loadsAndFiltersReadyAndCoolingItems() = runTest(testDispatcher) {
        val readyItem = CooldownItem(
            id = 1,
            name = "Áo khoác vintage",
            price = 680_000L,
            addedTimestamp = 50_000L,
            cooldownDurationMillis = 50_000L, // remaining = 0 -> canDecide = true
            reason = ""
        )
        val coolingItem = CooldownItem(
            id = 2,
            name = "Sony WH-1000XM5",
            price = 7_490_000L,
            addedTimestamp = 80_000L,
            cooldownDurationMillis = 50_000L, // remaining = 30_000 -> canDecide = false
            reason = ""
        )

        val fakeRepo = FakeHomeItemRepository(listOf(readyItem, coolingItem))
        val spendingRepo = InMemoryHomeSpendingRepository()

        val viewModel = HomeViewModel(fakeRepo, spendingRepo).apply {
            todayProvider = { fixedToday }
            nowMillisProvider = { fixedNowMillis }
            anchorDay = 15
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue("State should be Content, but was $state", state is HomeUiState.Content)
        val content = state as HomeUiState.Content

        assertEquals(1, content.readyItems.size)
        assertEquals("Áo khoác vintage", content.readyItems.first().name)

        assertEquals(1, content.coolingItems.size)
        assertEquals("Sony WH-1000XM5", content.coolingItems.first().name)

        assertEquals(fixedToday, content.selectedDate)
        assertEquals(31, content.calendarDays.size)
        assertEquals(com.hoabh.chanko.presentation.screens.home.model.MascotAdvice.Warning(80), content.mascotAdvice)
    }

    @Test
    fun selectDate_updatesSelectedDayAndTransactions() = runTest(testDispatcher) {
        val fakeRepo = FakeHomeItemRepository(emptyList())
        val spendingRepo = InMemoryHomeSpendingRepository()

        val viewModel = HomeViewModel(fakeRepo, spendingRepo).apply {
            todayProvider = { fixedToday }
            nowMillisProvider = { fixedNowMillis }
            anchorDay = 15
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        val newDate = LocalDate.of(2026, 10, 16)
        viewModel.onEvent(HomeUiEvent.SelectDate(newDate))
        runCurrent()

        val content = viewModel.uiState.value as HomeUiState.Content
        assertEquals(newDate, content.selectedDate)
        assertEquals(1, content.selectedDateTransactions?.purchaseCount)
        assertEquals(1_000_000L, content.selectedDateTransactions?.totalAmount)
    }

    @Test
    fun jumpToToday_resetsSelectionToToday() = runTest(testDispatcher) {
        val fakeRepo = FakeHomeItemRepository(emptyList())
        val spendingRepo = InMemoryHomeSpendingRepository()

        val viewModel = HomeViewModel(fakeRepo, spendingRepo).apply {
            todayProvider = { fixedToday }
            nowMillisProvider = { fixedNowMillis }
            anchorDay = 15
        }

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        // Select another date
        viewModel.onEvent(HomeUiEvent.SelectDate(LocalDate.of(2026, 10, 20)))
        runCurrent()
        assertEquals(LocalDate.of(2026, 10, 20), (viewModel.uiState.value as HomeUiState.Content).selectedDate)

        // Jump to today
        viewModel.onEvent(HomeUiEvent.JumpToToday)
        runCurrent()
        assertEquals(fixedToday, (viewModel.uiState.value as HomeUiState.Content).selectedDate)
    }

    @Test
    fun repositoryError_emitsErrorState() = runTest(testDispatcher) {
        val errorRepo = object : ItemRepository {
            override fun getActiveItems(): Flow<List<CooldownItem>> = flow { error("Database offline") }
            override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = flow { emit(emptyList()) }
            override fun getItemById(id: Int): Flow<CooldownItem?> = flow { emit(null) }
            override suspend fun insertItem(item: CooldownItem) = Unit
            override suspend fun updateItem(item: CooldownItem) = Unit
            override suspend fun updateItemStatus(id: Int, status: ItemStatus) = Unit
            override suspend fun deleteItem(item: CooldownItem) = Unit
            override fun getTotalSavedAmount(): Flow<Long> = flow { emit(0L) }
        }

        val viewModel = HomeViewModel(errorRepo, InMemoryHomeSpendingRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        runCurrent()

        assertEquals(HomeUiState.Error, viewModel.uiState.value)
    }
}

private class FakeHomeItemRepository(items: List<CooldownItem>) : ItemRepository {
    private val flowItems = MutableStateFlow(items)

    override fun getActiveItems(): Flow<List<CooldownItem>> = flowItems
    override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> = flowItems
    override fun getItemById(id: Int): Flow<CooldownItem?> = MutableStateFlow(flowItems.value.find { it.id == id })
    override suspend fun insertItem(item: CooldownItem) = Unit
    override suspend fun updateItem(item: CooldownItem) = Unit
    override suspend fun updateItemStatus(id: Int, status: ItemStatus) = Unit
    override suspend fun deleteItem(item: CooldownItem) = Unit
    override fun getTotalSavedAmount(): Flow<Long> = MutableStateFlow(0L)
}
