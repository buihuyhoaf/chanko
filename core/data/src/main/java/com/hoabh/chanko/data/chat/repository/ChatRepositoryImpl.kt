package com.hoabh.chanko.data.chat.repository

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val fakeDataSource: FakeChatDataSource
) : ChatRepository {

    override fun getActiveThreads(): Flow<List<ChatThread>> {
        return fakeDataSource.threads.map { list -> list.filterNot { it.isArchived } }
    }

    override fun getArchivedThreads(): Flow<List<ChatThread>> {
        return fakeDataSource.threads.map { list -> list.filter { it.isArchived } }
    }

    override fun getThreadById(threadId: String): Flow<ChatThread?> {
        return fakeDataSource.getThreadById(threadId)
    }

    override fun getMessages(threadId: String): Flow<List<ChatMessage>> {
        return fakeDataSource.getMessagesFlow(threadId)
    }

    override suspend fun sendMessage(threadId: String, text: String): Result<ChatMessage> {
        return runCatching {
            fakeDataSource.sendMessage(threadId, text)
        }
    }

    override suspend fun bindExistingItem(
        threadId: String,
        itemId: String,
        itemName: String,
        itemPriceVnd: Long
    ): Result<ChatThread> {
        return runCatching {
            fakeDataSource.bindExistingItem(threadId, itemId, itemName, itemPriceVnd)
        }
    }

    override suspend fun requestVerdict(threadId: String): Result<ChatMessage> {
        return runCatching {
            fakeDataSource.requestVerdict(threadId)
        }
    }

    override suspend fun archiveThread(threadId: String): Result<Unit> {
        return runCatching {
            fakeDataSource.archiveThread(threadId)
        }
    }

    override suspend fun restoreThread(threadId: String): Result<Unit> {
        return runCatching {
            fakeDataSource.restoreThread(threadId)
        }
    }

    override suspend fun deleteThread(threadId: String): Result<Unit> {
        return runCatching {
            fakeDataSource.deleteThread(threadId)
        }
    }
}
