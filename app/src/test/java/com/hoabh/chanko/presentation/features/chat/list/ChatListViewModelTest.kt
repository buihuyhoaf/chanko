package com.hoabh.chanko.presentation.features.chat.list

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ChatListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var viewModel: ChatListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeChatRepository()
        viewModel = ChatListViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsActiveThreads() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showArchived)
        assertTrue(state.threads.isNotEmpty())
        assertEquals(2, state.threads.size) // 2 active threads seeded
        assertEquals(2, state.filteredThreads.size)
    }

    @Test
    fun searchQuery_filtersThreads() = runTest {
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("bàn phím")
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredThreads.size)
        assertEquals("Bàn phím mẫu C", state.filteredThreads.first().title)
    }

    @Test
    fun toggleArchived_switchesToArchivedList() = runTest {
        advanceUntilIdle()

        viewModel.onToggleArchived(true)
        val state = viewModel.uiState.value
        assertTrue(state.showArchived)
        assertEquals(0, state.filteredThreads.size) // initially 0 archived
    }

    @Test
    fun archiveAndRestore_updatesThreadState() = runTest {
        advanceUntilIdle()
        val target = viewModel.uiState.value.threads.first()

        viewModel.onRequestArchive(target)
        assertEquals(target.id, viewModel.uiState.value.threadForArchive?.id)

        viewModel.onConfirmArchive()
        advanceUntilIdle()

        // Active threads should decrease
        assertEquals(1, viewModel.uiState.value.activeThreads.size)
        // Archived threads should increase
        assertEquals(1, viewModel.uiState.value.archivedThreads.size)

        // Restore
        val archivedTarget = viewModel.uiState.value.archivedThreads.first()
        viewModel.onRestoreThread(archivedTarget)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.activeThreads.size)
        assertEquals(0, viewModel.uiState.value.archivedThreads.size)
    }

    @Test
    fun renameThread_updatesTitle() = runTest {
        advanceUntilIdle()
        val target = viewModel.uiState.value.threads.first()

        viewModel.onRequestRename(target)
        viewModel.onConfirmRename("Tai nghe cao cấp mới")
        advanceUntilIdle()

        val updated = viewModel.uiState.value.threads.first { it.id == target.id }
        assertEquals("Tai nghe cao cấp mới", updated.title)
    }

    @Test
    fun deleteThread_removesFromList() = runTest {
        advanceUntilIdle()
        val target = viewModel.uiState.value.threads.first()

        viewModel.onRequestDelete(target)
        viewModel.onConfirmDelete()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.activeThreads.size)
        assertFalse(viewModel.uiState.value.activeThreads.any { it.id == target.id })
    }
}
