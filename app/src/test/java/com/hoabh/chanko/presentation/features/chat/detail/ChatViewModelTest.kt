package com.hoabh.chanko.presentation.features.chat.detail

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeChatRepository()
        viewModel = ChatViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initThread_withExistingThreadId_loadsThreadAndMessages() = runTest {
        viewModel.initThread(threadId = "t_01")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.thread)
        assertEquals("t_01", state.thread?.id)
        assertEquals("Tai nghe mẫu A", state.thread?.primaryProduct?.name)
        assertTrue(state.messages.isNotEmpty())
    }

    @Test
    fun initThread_withDraftProduct_setsUpDraftWithoutPersisting() = runTest {
        viewModel.initThread(
            threadId = null,
            productId = "p_watch",
            productName = "Đồng hồ mẫu B",
            productPrice = 11_990_000L
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.thread) // Not persisted yet (NC-GAP-04)
        assertNotNull(state.draftProduct)
        assertEquals("p_watch", state.draftProduct?.productId)
        assertEquals(1, state.messages.size) // Welcome message
    }

    @Test
    fun sendMessage_inDraftMode_createsThreadAndPersists() = runTest {
        viewModel.initThread(
            threadId = null,
            productId = "p_watch",
            productName = "Đồng hồ mẫu B",
            productPrice = 11_990_000L
        )
        advanceUntilIdle()

        viewModel.onInputTextChanged("Món này đeo đi làm được không?")
        viewModel.sendMessage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.thread) // Created thread
        assertEquals("Đồng hồ mẫu B", state.thread?.primaryProduct?.name)
        assertTrue(state.messages.any { it.content == "Món này đeo đi làm được không?" })
    }

    @Test
    fun addAndRemoveReference_updatesReferencesPreservingHistory() = runTest {
        viewModel.initThread(threadId = "t_01")
        advanceUntilIdle()

        val initialMessagesCount = viewModel.uiState.value.messages.size

        // Add reference
        val watch = fakeRepository.getEligibleProducts().first().first { it.productId == "p_watch" }
        viewModel.onSelectReference(watch)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.thread?.referenceProducts?.any { it.productId == "p_watch" } == true)

        // Remove reference
        viewModel.onRemoveReference("p_watch")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.thread?.referenceProducts?.any { it.productId == "p_watch" } == true)
        // Historical messages preserved (CD-GAP-04, EDGE-08)
        assertTrue(viewModel.uiState.value.messages.size >= initialMessagesCount)
    }

    @Test
    fun archiveAndRestoreThread_updatesReadOnlyState() = runTest {
        viewModel.initThread(threadId = "t_01")
        advanceUntilIdle()

        viewModel.onRequestArchive()
        viewModel.onConfirmArchive()
        advanceUntilIdle()

        assertEquals(ReadOnlyReason.ARCHIVED, viewModel.uiState.value.readOnlyReason)
        assertTrue(viewModel.uiState.value.isReadOnly)

        viewModel.onRestoreThread()
        advanceUntilIdle()

        assertEquals(ReadOnlyReason.NONE, viewModel.uiState.value.readOnlyReason)
        assertFalse(viewModel.uiState.value.isReadOnly)
    }

    @Test
    fun renameThread_updatesTitle() = runTest {
        viewModel.initThread(threadId = "t_01")
        advanceUntilIdle()

        viewModel.onRequestRename()
        viewModel.onConfirmRename("Tai nghe nghe nhạc chuyên nghiệp")
        advanceUntilIdle()

        assertEquals("Tai nghe nghe nhạc chuyên nghiệp", viewModel.uiState.value.thread?.title)
    }
}
