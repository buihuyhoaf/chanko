package com.hoabh.chanko

import androidx.lifecycle.SavedStateHandle
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.ActionState
import com.hoabh.chanko.presentation.ItemsState
import com.hoabh.chanko.presentation.OverviewViewModel
import com.hoabh.chanko.presentation.screens.additem.AddItemViewModel
import com.hoabh.chanko.presentation.screens.itemdetail.ItemViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun invalidInputDoesNotReachRepository() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = AddItemViewModel(repository, SavedStateHandle())
        model.add("  ", 100, 24, "")
        runCurrent()
        assertEquals(ActionState.ERROR, model.action.value)
        assertNull(repository.inserted)
    }

    @Test fun saveFailureCanBeRetriedAndDuplicateSubmitIsIgnored() = runTest(dispatcher) {
        val repository = FakeRepository().apply { failWrite = true }
        val model = AddItemViewModel(repository, SavedStateHandle())
        model.add(" Tai nghe ", 500_000, 24, " Làm việc ")
        runCurrent()
        assertEquals(ActionState.ERROR, model.action.value)
        repository.failWrite = false
        model.add(" Tai nghe ", 500_000, 24, " Làm việc ")
        model.add("Khác", 100, 1, "")
        runCurrent()
        assertEquals(ActionState.DONE, model.action.value)
        assertEquals("Tai nghe", repository.inserted?.name)
        assertEquals("Làm việc", repository.inserted?.reason)
        assertEquals(86_400_000L, repository.inserted?.cooldownDurationMillis)
        assertEquals(1, repository.insertCount)
    }

    @Test fun overviewCollectsDataAndExposesReadFailure() = runTest(dispatcher) {
        val repository = FakeRepository()
        val model = OverviewViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.items.collect() }
        runCurrent()
        assertTrue(model.items.value is ItemsState.Content)
        repository.rows.value = listOf(repository.example)
        runCurrent()
        assertEquals(1, (model.items.value as ItemsState.Content).active.size)
        val failed = OverviewViewModel(FakeRepository().apply { failRead = true })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { failed.items.collect() }
        runCurrent()
        assertEquals(ItemsState.Error, failed.items.value)
    }

    @Test fun decisionUsesNavigationIdAndReportsWriteFailure() = runTest(dispatcher) {
        val repository = FakeRepository().apply { failWrite = true }
        val model = ItemViewModel(repository, SavedStateHandle(mapOf("itemId" to 42)))
        model.decide(ItemStatus.PASSED)
        runCurrent()
        assertEquals(ActionState.ERROR, model.action.value)
        repository.failWrite = false
        model.decide(ItemStatus.PASSED)
        runCurrent()
        assertEquals(42, repository.decidedId)
        assertEquals(ActionState.DONE, model.action.value)
    }
}

private class FakeRepository : ItemRepository {
    val example = CooldownItem(42, "Tai nghe", 500_000, addedTimestamp = 0,
        cooldownDurationMillis = 1000, reason = "")
    val rows = MutableStateFlow<List<CooldownItem>>(emptyList())
    var failWrite = false
    var failRead = false
    var inserted: CooldownItem? = null
    var insertCount = 0
    var decidedId: Int? = null
    override fun getActiveItems(): Flow<List<CooldownItem>> = flow {
        if (failRead) error("Read failed")
        emitAll(rows.map { list -> list.filter { it.status == ItemStatus.COOLING_DOWN } })
    }
    override fun getItemsByStatus(status: ItemStatus) = rows.map { list -> list.filter { it.status == status } }
    override fun getItemById(id: Int) = rows.map { list -> list.find { it.id == id } }
    override fun getTotalSavedAmount() = rows.map { list -> list.filter { it.status == ItemStatus.PASSED }.sumOf { it.price } }
    override suspend fun insertItem(item: CooldownItem) {
        if (failWrite) error("Write failed")
        inserted = item
        insertCount++
    }
    override suspend fun updateItem(item: CooldownItem) = Unit
    override suspend fun deleteItem(item: CooldownItem) = Unit
    override suspend fun updateItemStatus(id: Int, status: ItemStatus) {
        if (failWrite) error("Write failed")
        decidedId = id
    }
}
