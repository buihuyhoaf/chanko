package com.hoabh.chanko.data.chat.repository

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatRepositoryImplTest {

    private lateinit var fakeRepository: FakeChatRepository
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setUp() {
        fakeRepository = FakeChatRepository()
        repository = ChatRepositoryImpl(fakeRepository)
    }

    @Test
    fun getThreads_returnsSeededThreads() = runTest {
        val threads = repository.getThreads().first()
        assertTrue(threads.isNotEmpty())
        assertEquals(2, threads.size)
    }

    @Test
    fun sendMessage_appendsMessageSuccessfully() = runTest {
        val activeThreads = repository.getThreads().first()
        val targetId = activeThreads.first().id

        val result = repository.sendMessage(
            threadId = targetId,
            text = "Xin chào Chanko",
            idempotencyKey = "key_send_1",
            clientMessageId = "msg_user_send_1"
        )

        assertTrue(result.isSuccess)
        val msg = result.getOrNull()
        assertNotNull(msg)
        assertEquals("Xin chào Chanko", msg?.content)

        val messages = repository.getMessages(targetId).first()
        assertTrue(messages.any { it.id == "msg_user_send_1" })
    }

    @Test
    fun archiveAndRestoreThread_persistsState() = runTest {
        val activeThreads = repository.getThreads().first()
        val targetId = activeThreads.first().id

        repository.archiveThread(targetId)
        val archived = repository.getThreadById(targetId).first()
        assertEquals(ReadOnlyReason.ARCHIVED, archived?.readOnlyReason)

        repository.restoreThread(targetId)
        val restored = repository.getThreadById(targetId).first()
        assertEquals(ReadOnlyReason.NONE, restored?.readOnlyReason)
    }
}
