package com.hoabh.chanko.presentation.screens.chat

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.data.chat.repository.ChatRepositoryImpl
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.presentation.screens.chat.viewmodel.ChatViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeChatDao: FakeChatDao
    private lateinit var fakeDataSource: FakeChatDataSource
    private lateinit var repository: ChatRepositoryImpl
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeChatDao = FakeChatDao()
        fakeDataSource = FakeChatDataSource()
        repository = ChatRepositoryImpl(fakeChatDao, fakeDataSource)
        viewModel = ChatViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isS01Empty() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.thread)
        assertTrue(state.messages.isEmpty())
    }

    @Test
    fun selectThread_loadsThreadAndMessages() = runTest {
        advanceUntilIdle()

        viewModel.selectThread("thread_free_1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.thread)
        assertEquals("thread_free_1", state.thread?.id)
        assertEquals(ThreadMode.FREE, state.thread?.mode)
        assertTrue(state.messages.isNotEmpty())
    }

    @Test
    fun initThread_withExistingThread_loadsMessagesAndQuickReplies() = runTest {
        viewModel.initThread("thread_item_keyboard")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.thread)
        assertEquals("thread_item_keyboard", state.thread?.id)
        assertEquals(ThreadMode.ITEM, state.thread?.mode)
        assertTrue(state.messages.isNotEmpty())
        assertTrue(state.quickReplies.isNotEmpty())
    }

    @Test
    fun initThread_withDraftItem_setsItemContextAndWelcome() = runTest {
        viewModel.initThread(
            threadId = null,
            contextMode = "ITEM",
            itemId = "item_123",
            itemName = "Bàn phím cơ",
            itemPrice = 2_500_000L
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("ITEM", state.draftMode)
        assertEquals("item_123", state.draftItemId)
        assertEquals("Bàn phím cơ", state.draftItemName)
        assertEquals(1, state.messages.size)
        assertTrue(state.messages[0].text.contains("Bàn phím cơ"))
        assertTrue(state.quickReplies.contains("Món này có thực sự cần thiết không?"))
    }

    @Test
    fun fillComposer_updatesInputTextOnly_doesNotSend() = runTest {
        viewModel.initThread("thread_free_1")
        advanceUntilIdle()

        val initialMessageCount = viewModel.uiState.value.messages.size

        viewModel.fillComposer("Quy tắc 72 giờ là gì?")
        val state = viewModel.uiState.value

        assertEquals("Quy tắc 72 giờ là gì?", state.inputText)
        assertEquals(initialMessageCount, state.messages.size)
        assertFalse(state.isSending)
    }

    @Test
    fun sendMessage_triggersChankoTyping_thenReceivesResponse() = runTest {
        viewModel.initThread("thread_free_1")
        advanceUntilIdle()

        viewModel.onTextChanged("Chanko có đang nghe không?")
        viewModel.sendMessage()

        // Ngay khi gọi sendMessage: isSending = true, isChankoTyping = true, inputText bị xóa trắng
        val typingState = viewModel.uiState.value
        assertEquals("", typingState.inputText)
        assertTrue(typingState.isSending)
        assertTrue(typingState.isChankoTyping)

        // Chạy qua thời gian trễ typing 1.5s (advanceUntilIdle)
        advanceUntilIdle()

        // Sau khi hoàn tất: isSending = false, isChankoTyping = false, nhận được phản hồi của Chanko
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isSending)
        assertFalse(finalState.isChankoTyping)
        assertTrue(finalState.messages.any { it.text.contains("Chanko đã nghe: \"Chanko có đang nghe không?\"") })
    }

    @Test
    fun sendMessage_inDraftItemMode_createsThreadAndBindsItem() = runTest {
        viewModel.initThread(
            threadId = null,
            contextMode = "ITEM",
            itemId = "item_999",
            itemName = "Tai nghe",
            itemPrice = 1_500_000L
        )
        advanceUntilIdle()

        viewModel.onTextChanged("Chanko ơi có nên mua tai nghe này không?")
        viewModel.sendMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.thread)
        assertEquals(ThreadMode.ITEM, state.thread?.mode)
        assertEquals("item_999", state.thread?.linkedItemId)
    }

    @Test
    fun renameThread_updatesThreadTitle() = runTest {
        viewModel.initThread("thread_free_1")
        advanceUntilIdle()

        viewModel.renameThread("Tâm sự tài chính cuối tháng")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Tâm sự tài chính cuối tháng", state.thread?.title)
    }

    @Test
    fun bindItem_switchesToItemMode_onSameThread() = runTest {
        advanceUntilIdle()

        viewModel.selectThread("thread_free_1")
        advanceUntilIdle()

        val originalThreadId = viewModel.uiState.value.thread?.id
        val oldMessageCount = viewModel.uiState.value.messages.size

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
        assertTrue(state.messages.size >= oldMessageCount)
    }

    @Test
    fun archiveAndRestoreThread_togglesArchivedState() = runTest {
        viewModel.initThread("thread_item_keyboard")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.thread?.isArchived == true)

        viewModel.archiveCurrentThread()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.thread?.isArchived == true)

        viewModel.restoreCurrentThread()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.thread?.isArchived == true)
    }

    @Test
    fun confirmDelete_removesThread() = runTest {
        advanceUntilIdle()

        viewModel.selectThread("thread_free_1")
        advanceUntilIdle()

        var deletedCalled = false
        viewModel.confirmDelete {
            deletedCalled = true
        }
        advanceUntilIdle()

        assertTrue(deletedCalled)
    }
}
