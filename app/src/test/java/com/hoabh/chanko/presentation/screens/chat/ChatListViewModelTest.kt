package com.hoabh.chanko.presentation.screens.chat

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.data.chat.repository.ChatRepositoryImpl
import com.hoabh.chanko.domain.chat.model.ThreadFilter
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatListViewModel
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
    private lateinit var fakeChatDao: FakeChatDao
    private lateinit var fakeDataSource: FakeChatDataSource
    private lateinit var repository: ChatRepositoryImpl
    private lateinit var viewModel: ChatListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeChatDao = FakeChatDao()
        fakeDataSource = FakeChatDataSource()
        repository = ChatRepositoryImpl(fakeChatDao, fakeDataSource)
        viewModel = ChatListViewModel(repository)
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
        assertEquals(4, state.threads.size) // 4 active threads seeded
    }

    @Test
    fun searchQuery_filtersThreads() = runTest {
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("bàn phím")
        val state = viewModel.uiState.value
        val filtered = state.filteredThreads

        assertEquals(1, filtered.size)
        assertTrue(filtered.first().title.contains("Bàn phím"))
    }

    @Test
    fun statusFilter_filtersCorrectly() = runTest {
        advanceUntilIdle()

        viewModel.onFilterSelected(ThreadFilter.CONSIDERING)
        val state = viewModel.uiState.value
        val filtered = state.filteredThreads

        assertEquals(1, filtered.size)
        assertEquals("Điện thoại Flagship", filtered.first().title)
    }

    @Test
    fun renameThread_updatesTitle() = runTest {
        advanceUntilIdle()

        val target = viewModel.uiState.value.threads.first()
        viewModel.openRenameDialog(target)
        viewModel.confirmRename("Điện thoại Flagship Pro Max")
        advanceUntilIdle()

        val updated = viewModel.uiState.value.threads.first { it.id == target.id }
        assertEquals("Điện thoại Flagship Pro Max", updated.title)
    }

    @Test
    fun deleteThread_removesFromList() = runTest {
        advanceUntilIdle()

        val initialCount = viewModel.uiState.value.threads.size
        val target = viewModel.uiState.value.threads.first()

        viewModel.openDeleteDialog(target)
        viewModel.confirmDelete()
        advanceUntilIdle()

        val newCount = viewModel.uiState.value.threads.size
        assertEquals(initialCount - 1, newCount)
        assertFalse(viewModel.uiState.value.threads.any { it.id == target.id })
    }

    @Test
    fun showArchived_displaysArchivedThreads() = runTest {
        advanceUntilIdle()

        viewModel.toggleShowArchived(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showArchived)
        assertEquals(1, state.threads.size)
        assertTrue(state.threads.first().isArchived)
    }
}
