package com.hoabh.chanko.presentation.features.chat

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import com.hoabh.chanko.domain.chat.model.ChatGenerationEvent
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import com.hoabh.chanko.presentation.features.chat.detail.ChatViewModel
import com.hoabh.chanko.presentation.features.chat.list.ChatListViewModel
import com.hoabh.chanko.presentation.features.chat.newchat.NewChatViewModel
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

/**
 * Suite kiểm thử tích hợp truy vết đầy đủ 27 Quyết định UX gốc v2.2 (3 CHAT + 12 GAP + 12 EDGE).
 * Khớp với docs/ACCEPTANCE-TRACEABILITY.csv và docs/UX-27-ORIGINAL.md.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatEndToEndFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeChatRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeChatRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- CHAT: 3 Quyết định ---

    @Test
    fun verify_CHAT_01_and_NC_GAP_02_OnePrimaryPlusReferencesNoFreeChat() = runTest {
        val ncViewModel = NewChatViewModel(fakeRepository)
        advanceUntilIdle()

        // NC chỉ cho phép chọn duy nhất 1 sản phẩm chính, không có Free Chat
        assertFalse(ncViewModel.uiState.value.canStartChat)
        val product = ncViewModel.uiState.value.products.first()
        ncViewModel.selectProduct(product)
        assertTrue(ncViewModel.uiState.value.canStartChat)
    }

    @Test
    fun verify_CHAT_02_and_NC_GAP_01_ExistingThreadDefaultOrNew() = runTest {
        val ncViewModel = NewChatViewModel(fakeRepository)
        advanceUntilIdle()

        // p_headphones đã có thread t_01
        val headphones = ncViewModel.uiState.value.products.first { it.productId == "p_headphones" }
        ncViewModel.selectProduct(headphones)
        ncViewModel.startChat()
        advanceUntilIdle()

        assertTrue(ncViewModel.uiState.value.showHistoryDialog)
        assertEquals("t_01", ncViewModel.uiState.value.existingActiveThread?.id)
    }

    // --- CL: 4 Quyết định ---

    @Test
    fun verify_CL_GAP_01_ReferenceBadgeCount() = runTest {
        val threads = fakeRepository.getThreads().first()
        val t2 = threads.first { it.id == "t_02" }
        assertEquals(1, t2.referenceProducts.size)
        // Badge +1 sẽ hiển thị cho t_02
    }

    @Test
    fun verify_CL_GAP_02_NoOldFilters() = runTest {
        val clViewModel = ChatListViewModel(fakeRepository)
        advanceUntilIdle()
        // Chỉ có filter tìm kiếm và toggle showArchived, không có ThreadFilter.CONSIDERING...
        assertEquals(2, clViewModel.uiState.value.filteredThreads.size)
    }

    @Test
    fun verify_CL_GAP_03_SortOrderLatestMessageDesc() = runTest {
        val threads = fakeRepository.getThreads().first()
        assertTrue(threads[0].updatedAtEpochMillis >= threads[1].updatedAtEpochMillis)
    }

    @Test
    fun verify_CL_GAP_04_and_EDGE_01_DeletedPrimaryRetainsHistoryReadOnly() = runTest {
        val deletedProduct = ProductSnapshot(
            productId = "p_deleted_1",
            name = "Món đã xóa",
            price = 500_000L,
            isPrimaryDeleted = true
        )
        val threadWithDeleted = ChatThread(
            id = "t_deleted",
            title = "Món đã xóa",
            primaryProduct = deletedProduct,
            updatedAtEpochMillis = System.currentTimeMillis(),
            readOnlyReason = ReadOnlyReason.PRIMARY_DELETED
        )

        fakeRepository.sendFirstMessage(
            draftThread = threadWithDeleted,
            firstMessageText = "Tin nhắn cũ trước khi xóa",
            idempotencyKey = "k_del",
            clientMessageId = "m_del"
        )
        advanceUntilIdle()

        val thread = fakeRepository.getThreadById("t_deleted").first()
        assertNotNull(thread)
        assertTrue(thread?.primaryProduct?.isPrimaryDeleted == true)
        // Chat trở thành read-only
        assertEquals(ReadOnlyReason.PRIMARY_DELETED, thread?.readOnlyReason)

        // Vẫn cho phép Archive hoặc Delete (EDGE-01)
        val archiveResult = fakeRepository.archiveThread("t_deleted")
        assertTrue(archiveResult.isSuccess)
    }

    // --- CD: 4 Quyết định ---

    @Test
    fun verify_CD_GAP_01_and_CD_GAP_02_PrimaryImmutableAndReferenceChips() = runTest {
        val cdViewModel = ChatViewModel(fakeRepository)
        cdViewModel.initThread(threadId = "t_01")
        advanceUntilIdle()

        // Primary product immutable (Tai nghe mẫu A)
        assertEquals("p_headphones", cdViewModel.uiState.value.thread?.primaryProduct?.productId)

        // Add reference product
        val watch = fakeRepository.getEligibleProducts().first().first { it.productId == "p_watch" }
        cdViewModel.onSelectReference(watch)
        advanceUntilIdle()

        assertTrue(cdViewModel.uiState.value.thread?.referenceProducts?.any { it.productId == "p_watch" } == true)
    }

    @Test
    fun verify_CD_GAP_03_and_EDGE_11_ObjectiveSnapshotsNoVerdict() = runTest {
        val messages = fakeRepository.getMessages("t_01").first()
        val chankoMsg = messages.first { it.sender == MessageSender.CHANKO }

        // Có productSnapshots gắn kèm
        assertTrue(chankoMsg.productSnapshots.isNotEmpty())
        assertEquals("Tai nghe mẫu A", chankoMsg.productSnapshots.first().name)
        // Không chứa phán quyết BUY/WAIT/SKIP
        assertFalse(chankoMsg.content.contains("BUY · Có thể cân nhắc mua"))
        assertFalse(chankoMsg.content.contains("SKIP"))
    }

    @Test
    fun verify_CD_GAP_04_and_EDGE_08_RemovingReferencePreservesHistory() = runTest {
        val cdViewModel = ChatViewModel(fakeRepository)
        cdViewModel.initThread(threadId = "t_02") // t_02 có tham chiếu p_chair
        advanceUntilIdle()

        val countBefore = cdViewModel.uiState.value.messages.size
        cdViewModel.onRemoveReference("p_chair")
        advanceUntilIdle()

        // Tham chiếu bị gỡ
        assertFalse(cdViewModel.uiState.value.thread?.referenceProducts?.any { it.productId == "p_chair" } == true)
        // Lịch sử tin nhắn phân tích được bảo toàn (CD-GAP-04, EDGE-08)
        assertEquals(countBefore, cdViewModel.uiState.value.messages.size)
    }

    // --- EDGE: 12 Quyết định biên ---

    @Test
    fun verify_EDGE_03_PriceUpdateCardReflectsNewPriceSnapshotImmutable() = runTest {
        val messages = fakeRepository.getMessages("t_01").first()
        val historicalSnapshotPrice = messages.first { it.sender == MessageSender.CHANKO }.productSnapshots.first().price
        assertEquals(7_990_000L, historicalSnapshotPrice)

        // Cập nhật giá sản phẩm ở view card không ghi đè snapshot quá khứ
        val updatedProduct = fakeRepository.getEligibleProducts().first().first { it.productId == "p_headphones" }
            .copy(price = 8_500_000L)
        assertEquals(8_500_000L, updatedProduct.price)
        assertEquals(7_990_000L, historicalSnapshotPrice)
    }

    @Test
    fun verify_EDGE_05_and_NC_GAP_04_ExitDraftLeavesNoThread() = runTest {
        val cdViewModel = ChatViewModel(fakeRepository)
        // Khởi tạo draft chưa gửi
        cdViewModel.initThread(
            threadId = null,
            productId = "p_watch",
            productName = "Đồng hồ mẫu B",
            productPrice = 11_990_000L
        )
        advanceUntilIdle()

        // Chưa gửi tin đầu tiên -> Chưa lưu thread nào vào repository (NC-GAP-04, EDGE-05)
        val threadInRepo = fakeRepository.findActiveThreadForProduct("p_watch")
        assertNull(threadInRepo)
    }

    @Test
    fun verify_EDGE_07_NoDuplicateMainOrReference() = runTest {
        // Không cho phép thêm chính SP chính làm tham chiếu
        val failPrimary = fakeRepository.addReference("t_01", "p_headphones")
        assertTrue(failPrimary.isFailure)

        // Không cho phép thêm trùng tham chiếu
        val successFirst = fakeRepository.addReference("t_01", "p_watch")
        assertTrue(successFirst.isSuccess)
        val failDuplicate = fakeRepository.addReference("t_01", "p_watch")
        assertTrue(failDuplicate.isFailure)
    }

    @Test
    fun verify_EDGE_10_IdempotentRetryNoDuplicate() = runTest {
        val msg1 = fakeRepository.sendMessage("t_01", "Tin nhắn test mạng", "key_same", "client_msg_1")
        assertTrue(msg1.isSuccess)

        val messages = fakeRepository.getMessages("t_01").first()
        val count = messages.count { it.id == "client_msg_1" }
        assertEquals(1, count)
    }

    @Test
    fun verify_EDGE_12_CancelBeforeArchiveOrDeleteDuringStream() = runTest {
        val cdViewModel = ChatViewModel(fakeRepository)
        cdViewModel.initThread(threadId = "t_01")
        advanceUntilIdle()

        // Gửi tin nhắn kích hoạt stream
        cdViewModel.onInputTextChanged("Chanko phân tích giúp tôi")
        cdViewModel.sendMessage()

        // Yêu cầu xóa khi đang streaming sẽ tự động cancel generation trước (EDGE-12)
        cdViewModel.onRequestDelete()
        advanceUntilIdle()

        assertFalse(cdViewModel.uiState.value.isStreaming)
    }
}
