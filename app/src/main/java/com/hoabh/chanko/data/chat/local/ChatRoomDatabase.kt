package com.hoabh.chanko.data.chat.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hoabh.chanko.data.chat.local.dao.ChatDao
import com.hoabh.chanko.data.chat.local.entity.ChatMessageEntity
import com.hoabh.chanko.data.chat.local.entity.ChatThreadEntity

@Database(
    entities = [ChatThreadEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatRoomDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
