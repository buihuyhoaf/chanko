package com.hoabh.chanko.presentation.screens.chat

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.data.chat.repository.ChatRepositoryImpl
import com.hoabh.chanko.domain.chat.model.ThreadMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDataSource: FakeChatDataSource
    private lateinit var repository: ChatRepositoryImpl
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDataSource = FakeChatDataSource()
        repository = ChatRepositoryImpl(fakeDataSource)
        viewModel = ChatViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsActiveThreadsAndMessages() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.thread)
        assertEquals("thread_free_1", state.thread?.id)
        assertEquals(ThreadMode.FREE, state.thread?.mode)
        assertTrue(state.messages.isNotEmpty())
    }

    @Test
    fun sendMessage_addsUserAndBotMessages() = runTest {
        advanceUntilIdle()

        viewModel.onTextChanged("Tôi có nên mua tai nghe này không?")
        viewModel.sendMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val lastUserMessage = state.messages.find { it.text == "Tôi có nên mua tai nghe này không?" }
        assertNotNull(lastUserMessage)

        val lastBotMessage = state.messages.lastOrNull()
        assertNotNull(lastBotMessage)
        assertEquals(com.hoabh.chanko.domain.chat.model.MessageSender.CHANKO, lastBotMessage?.sender)
    }

    @Test
    fun bindItem_switchesToItemMode_onSameThread() = runTest {
        advanceUntilIdle()

        val originalThreadId = viewModel.uiState.value.thread?.id

        viewModel.bindItem(
            itemId = "item_watch_01",
            itemName = "Đồng hồ thông minh",
            itemPriceVnd = 5_000_000L
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(originalThreadId, state.thread?.id)
        assertEquals(ThreadMode.ITEM, state.thread?.mode)
        assertEquals("Đồng hồ thông minh", state.thread?.linkedItemName)
        assertEquals(5_000_000L, state.thread?.linkedItemPriceVnd)
    }

    @Test
    fun confirmDelete_removesThread() = runTest {
        advanceUntilIdle()

        var deletedCalled = false
        viewModel.confirmDelete {
            deletedCalled = true
        }
        advanceUntilIdle()

        assertTrue(deletedCalled)
    }
}
