package com.hoabh.chanko.data.chat.repository

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ThreadMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatRepositoryImplTest {

    private lateinit var fakeDao: FakeChatDao
    private lateinit var fakeDataSource: FakeChatDataSource
    private lateinit var repository: ChatRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeChatDao()
        fakeDataSource = FakeChatDataSource()
        repository = ChatRepositoryImpl(fakeDao, fakeDataSource)
    }

    @Test
    fun getActiveThreads_seedsInitialFixturesToDao() = runTest {
        assertEquals(0, fakeDao.getThreadCount())

        val activeThreads = repository.getActiveThreads().first()

        assertTrue(fakeDao.getThreadCount() > 0)
        assertEquals(4, activeThreads.size)
    }

    @Test
    fun sendMessage_persistsUserAndBotMessageToDao() = runTest {
        val result = repository.sendMessage("", "Xin chào Chanko")

        assertTrue(result.isSuccess)
        val botMsg = result.getOrNull()
        assertNotNull(botMsg)
        assertEquals(MessageSender.CHANKO, botMsg?.sender)

        val messages = repository.getMessages(botMsg!!.threadId).first()
        assertEquals(2, messages.size)
        assertEquals("Xin chào Chanko", messages[0].text)
        assertEquals(MessageSender.USER, messages[0].sender)
        assertEquals(botMsg.text, messages[1].text)
        assertEquals(MessageSender.CHANKO, messages[1].sender)
    }

    @Test
    fun archiveAndRestoreThread_persistsStateToDao() = runTest {
        val activeThreads = repository.getActiveThreads().first()
        val targetId = activeThreads.first().id

        repository.archiveThread(targetId)

        val activeAfterArchive = repository.getActiveThreads().first()
        assertFalse(activeAfterArchive.any { it.id == targetId })

        val archived = repository.getArchivedThreads().first()
        assertTrue(archived.any { it.id == targetId })

        repository.restoreThread(targetId)
        val activeAfterRestore = repository.getActiveThreads().first()
        assertTrue(activeAfterRestore.any { it.id == targetId })
    }

    @Test
    fun deleteThread_removesThreadAndMessagesFromDao() = runTest {
        val activeThreads = repository.getActiveThreads().first()
        val targetId = activeThreads.first().id

        repository.deleteThread(targetId)

        val activeAfterDelete = repository.getActiveThreads().first()
        assertFalse(activeAfterDelete.any { it.id == targetId })

        val messages = repository.getMessages(targetId).first()
        assertTrue(messages.isEmpty())
    }

    @Test
    fun bindExistingItem_updatesThreadModeAndInsertsSystemMessage() = runTest {
        val activeThreads = repository.getActiveThreads().first()
        val freeThread = activeThreads.first { it.mode == ThreadMode.FREE }

        val result = repository.bindExistingItem(
            threadId = freeThread.id,
            itemId = "101",
            itemName = "Tai nghe Sony",
            itemPriceVnd = 4_500_000L
        )

        assertTrue(result.isSuccess)
        val updatedThread = repository.getThreadById(freeThread.id).first()
        assertNotNull(updatedThread)
        assertEquals(ThreadMode.ITEM, updatedThread?.mode)
        assertEquals("Tai nghe Sony", updatedThread?.linkedItemName)

        val messages = repository.getMessages(freeThread.id).first()
        assertTrue(messages.any { it.sender == MessageSender.SYSTEM && it.text.contains("Tai nghe Sony") })
    }
}
