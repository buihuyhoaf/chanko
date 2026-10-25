package com.hoabh.chanko.data.chat.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ThreadMode

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mode: String,
    val linkedItemId: String?,
    val linkedItemName: String?,
    val linkedItemPriceVnd: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean,
    val contextRevision: Int,
    val cachedAt: Long
) {
    fun toDomain(): ChatThread = ChatThread(
        id = id,
        title = title,
        mode = if (mode == "ITEM") ThreadMode.ITEM else ThreadMode.FREE,
        linkedItemId = linkedItemId,
        linkedItemName = linkedItemName,
        linkedItemPriceVnd = linkedItemPriceVnd,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived,
        contextRevision = contextRevision
    )

    companion object {
        fun fromDomain(domain: ChatThread, cachedAt: Long = System.currentTimeMillis()): ChatThreadEntity =
            ChatThreadEntity(
                id = domain.id,
                title = domain.title,
                mode = domain.mode.name,
                linkedItemId = domain.linkedItemId,
                linkedItemName = domain.linkedItemName,
                linkedItemPriceVnd = domain.linkedItemPriceVnd,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                isArchived = domain.isArchived,
                contextRevision = domain.contextRevision,
                cachedAt = cachedAt
            )
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val emotion: String,
    val verdict: String?,
    val cachedAt: Long
)
