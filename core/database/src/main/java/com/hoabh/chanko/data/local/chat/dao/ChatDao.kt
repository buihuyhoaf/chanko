package com.hoabh.chanko.data.local.chat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hoabh.chanko.data.local.chat.entity.ChatMessageEntity
import com.hoabh.chanko.data.local.chat.entity.ChatThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT COUNT(*) FROM chat_threads")
    suspend fun getThreadCount(): Int

    @Query("SELECT * FROM chat_threads WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getActiveThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    fun getThreadById(threadId: String): Flow<ChatThreadEntity?>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    suspend fun getThreadByIdDirect(threadId: String): ChatThreadEntity?

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesByThreadId(threadId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateThread(thread: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateThreads(threads: List<ChatThreadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_threads SET title = :newTitle, updatedAt = :updatedAt WHERE id = :threadId")
    suspend fun renameThread(threadId: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_threads SET isArchived = 1, updatedAt = :updatedAt WHERE id = :threadId")
    suspend fun archiveThread(threadId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_threads SET isArchived = 0, updatedAt = :updatedAt WHERE id = :threadId")
    suspend fun restoreThread(threadId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_threads SET previewText = :previewText, updatedAt = :updatedAt WHERE id = :threadId")
    suspend fun updateThreadPreview(threadId: String, previewText: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_threads SET mode = 'ITEM', linkedItemId = :itemId, linkedItemName = :itemName, linkedItemPriceVnd = :price, contextRevision = contextRevision + 1, updatedAt = :updatedAt WHERE id = :threadId")
    suspend fun bindItemToThread(threadId: String, itemId: String, itemName: String, price: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesByThreadId(threadId: String)
}
