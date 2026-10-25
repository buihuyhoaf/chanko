package com.hoabh.chanko.data.chat.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hoabh.chanko.data.chat.local.entity.ChatMessageEntity
import com.hoabh.chanko.data.chat.local.entity.ChatThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_threads WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getActiveThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    fun getThreadById(threadId: String): Flow<ChatThreadEntity?>

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

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesByThreadId(threadId: String)
}
