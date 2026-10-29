package com.hoabh.chanko.data.chat.repository

import com.hoabh.chanko.data.local.chat.dao.ChatDao
import com.hoabh.chanko.data.local.chat.entity.ChatMessageEntity
import com.hoabh.chanko.data.local.chat.entity.ChatThreadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeChatDao : ChatDao {
    private val threadsFlow = MutableStateFlow<List<ChatThreadEntity>>(emptyList())
    private val messagesFlow = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    override suspend fun getThreadCount(): Int = threadsFlow.value.size

    override fun getActiveThreads(): Flow<List<ChatThreadEntity>> =
        threadsFlow.map { list -> list.filterNot { it.isArchived } }

    override fun getArchivedThreads(): Flow<List<ChatThreadEntity>> =
        threadsFlow.map { list -> list.filter { it.isArchived } }

    override fun getThreadById(threadId: String): Flow<ChatThreadEntity?> =
        threadsFlow.map { list -> list.find { it.id == threadId } }

    override suspend fun getThreadByIdDirect(threadId: String): ChatThreadEntity? =
        threadsFlow.value.find { it.id == threadId }

    override fun getMessagesByThreadId(threadId: String): Flow<List<ChatMessageEntity>> =
        messagesFlow.map { list -> list.filter { it.threadId == threadId }.sortedBy { it.timestamp } }

    override suspend fun insertOrUpdateThread(thread: ChatThreadEntity) {
        val current = threadsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == thread.id }
        if (index >= 0) {
            current[index] = thread
        } else {
            current.add(0, thread)
        }
        threadsFlow.value = current
    }

    override suspend fun insertOrUpdateThreads(threads: List<ChatThreadEntity>) {
        val current = threadsFlow.value.toMutableList()
        for (thread in threads) {
            val index = current.indexOfFirst { it.id == thread.id }
            if (index >= 0) {
                current[index] = thread
            } else {
                current.add(thread)
            }
        }
        threadsFlow.value = current
    }

    override suspend fun insertMessage(message: ChatMessageEntity) {
        val current = messagesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            current[index] = message
        } else {
            current.add(message)
        }
        messagesFlow.value = current
    }

    override suspend fun insertMessages(messages: List<ChatMessageEntity>) {
        val current = messagesFlow.value.toMutableList()
        for (msg in messages) {
            val index = current.indexOfFirst { it.id == msg.id }
            if (index >= 0) {
                current[index] = msg
            } else {
                current.add(msg)
            }
        }
        messagesFlow.value = current
    }

    override suspend fun renameThread(threadId: String, newTitle: String, updatedAt: Long) {
        threadsFlow.value = threadsFlow.value.map {
            if (it.id == threadId) it.copy(title = newTitle, updatedAt = updatedAt) else it
        }
    }

    override suspend fun archiveThread(threadId: String, updatedAt: Long) {
        threadsFlow.value = threadsFlow.value.map {
            if (it.id == threadId) it.copy(isArchived = true, updatedAt = updatedAt) else it
        }
    }

    override suspend fun restoreThread(threadId: String, updatedAt: Long) {
        threadsFlow.value = threadsFlow.value.map {
            if (it.id == threadId) it.copy(isArchived = false, updatedAt = updatedAt) else it
        }
    }

    override suspend fun updateThreadPreview(threadId: String, previewText: String, updatedAt: Long) {
        threadsFlow.value = threadsFlow.value.map {
            if (it.id == threadId) it.copy(previewText = previewText, updatedAt = updatedAt) else it
        }
    }

    override suspend fun bindItemToThread(
        threadId: String,
        itemId: String,
        itemName: String,
        price: Long,
        updatedAt: Long
    ) {
        threadsFlow.value = threadsFlow.value.map {
            if (it.id == threadId) it.copy(
                mode = "ITEM",
                linkedItemId = itemId,
                linkedItemName = itemName,
                linkedItemPriceVnd = price,
                contextRevision = it.contextRevision + 1,
                updatedAt = updatedAt
            ) else it
        }
    }

    override suspend fun deleteThread(threadId: String) {
        threadsFlow.value = threadsFlow.value.filterNot { it.id == threadId }
    }

    override suspend fun deleteMessagesByThreadId(threadId: String) {
        messagesFlow.value = messagesFlow.value.filterNot { it.threadId == threadId }
    }
}
