package com.hoabh.chanko.data.chat.fake

import com.hoabh.chanko.domain.chat.model.ChatGenerationEvent
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeChatRepositoryTest {

    private lateinit var repository: FakeChatRepository

    @Before
    fun setUp() {
        repository = FakeChatRepository()
    }

    @Test
    fun getThreads_returnsSeededThreadsSortedByUpdatedAtDesc() = runBlocking {
        val threads = repository.getThreads().first()
        assertTrue(threads.isNotEmpty())
        assertEquals(2, threads.size)
        assertTrue(threads[0].updatedAtEpochMillis >= threads[1].updatedAtEpochMillis)
    }

    @Test
    fun findActiveThreadForProduct_returnsMatchingThread() = runBlocking {
        val thread = repository.findActiveThreadForProduct("p_headphones")
        assertNotNull(thread)
        assertEquals("p_headphones", thread?.primaryProduct?.productId)

        val nonExistent = repository.findActiveThreadForProduct("p_unknown")
        assertNull(nonExistent)
    }

    @Test
    fun sendFirstMessage_createsNewThreadAndMessageAtomically() = runBlocking {
        val newProduct = ProductSnapshot(
            productId = "p_mouse",
            name = "Chuột không dây",
            price = 990_000L
        )
        val draft = ChatThread(
            id = "t_draft_new",
            title = "Chuột không dây",
            primaryProduct = newProduct,
            updatedAtEpochMillis = System.currentTimeMillis()
        )

        val result = repository.sendFirstMessage(
            draftThread = draft,
            firstMessageText = "Chuột này dùng văn phòng ổn không Chanko?",
            idempotencyKey = "key_123",
            clientMessageId = "msg_user_1"
        )

        assertTrue(result.isSuccess)
        val createdThread = result.getOrThrow()
        assertEquals("t_draft_new", createdThread.id)

        // Verify thread is in list
        val allThreads = repository.getThreads().first()
        assertTrue(allThreads.any { it.id == "t_draft_new" })

        // Verify message was stored
        val messages = repository.getMessages("t_draft_new").first()
        assertEquals(1, messages.size)
        assertEquals("Chuột này dùng văn phòng ổn không Chanko?", messages[0].content)
    }

    @Test
    fun addReference_blocksDuplicateAndPrimary() = runBlocking {
        val threadId = "t_01" // primary is p_headphones

        // Try adding primary product as reference -> fails (EDGE-07)
        val primaryResult = repository.addReference(threadId, "p_headphones")
        assertTrue(primaryResult.isFailure)

        // Add valid reference
        val validResult = repository.addReference(threadId, "p_watch")
        assertTrue(validResult.isSuccess)
        val threadWithRef = validResult.getOrThrow()
        assertTrue(threadWithRef.referenceProducts.any { it.productId == "p_watch" })

        // Try adding same reference again -> fails (EDGE-07)
        val duplicateResult = repository.addReference(threadId, "p_watch")
        assertTrue(duplicateResult.isFailure)
    }

    @Test
    fun removeReference_removesActiveLinkPreservingHistory() = runBlocking {
        val threadId = "t_02" // primary is p_keyboard, reference is p_chair
        val messagesBefore = repository.getMessages(threadId).first()
        assertTrue(messagesBefore.isNotEmpty())

        val result = repository.removeReference(threadId, "p_chair")
        assertTrue(result.isSuccess)
        val updatedThread = result.getOrThrow()
        assertFalse(updatedThread.referenceProducts.any { it.productId == "p_chair" })

        // Message history still intact
        val messagesAfter = repository.getMessages(threadId).first()
        assertEquals(messagesBefore.size, messagesAfter.size)
    }

    @Test
    fun archiveAndRestoreThread_updatesReadOnlyState() = runBlocking {
        val threadId = "t_01"

        repository.archiveThread(threadId)
        val archived = repository.getThreadById(threadId).first()
        assertTrue(archived?.isArchived == true)
        assertEquals(ReadOnlyReason.ARCHIVED, archived?.readOnlyReason)

        repository.restoreThread(threadId)
        val restored = repository.getThreadById(threadId).first()
        assertFalse(restored?.isArchived == true)
        assertEquals(ReadOnlyReason.NONE, restored?.readOnlyReason)
    }

    @Test
    fun cancelGeneration_cancelsStreamingGracefully() = runBlocking {
        val threadId = "t_01"
        val genId = "gen_test_cancel"

        repository.cancelGeneration(threadId, genId)
        val events = repository.streamAiResponse(threadId, genId).first()
        // Started emitted first
        assertTrue(events is ChatGenerationEvent.Started)
    }
}
