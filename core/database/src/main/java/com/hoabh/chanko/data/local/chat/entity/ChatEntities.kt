package com.hoabh.chanko.data.local.chat.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.MessageStatus
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val primaryProductId: String,
    val primaryProductName: String,
    val primaryProductPrice: Long,
    val primaryProductThumbnailUrl: String? = null,
    val referenceProductsJson: String = "[]",
    val previewText: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val readOnlyReason: String = "NONE",
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChatThread = ChatThread(
        id = id,
        title = title,
        primaryProduct = ProductSnapshot(
            productId = primaryProductId,
            name = primaryProductName,
            price = primaryProductPrice,
            thumbnailUrl = primaryProductThumbnailUrl,
            isPrimaryDeleted = readOnlyReason == "PRIMARY_DELETED"
        ),
        referenceProducts = referenceProductsJson.toProductSnapshotList(),
        lastMessage = null, // Injected via repository when loading messages
        updatedAtEpochMillis = updatedAt,
        isArchived = isArchived,
        readOnlyReason = when (readOnlyReason) {
            "ARCHIVED" -> ReadOnlyReason.ARCHIVED
            "PRIMARY_DELETED" -> ReadOnlyReason.PRIMARY_DELETED
            "OFFLINE" -> ReadOnlyReason.OFFLINE
            else -> ReadOnlyReason.NONE
        }
    )

    companion object {
        fun fromDomain(domain: ChatThread, cachedAt: Long = System.currentTimeMillis()): ChatThreadEntity =
            ChatThreadEntity(
                id = domain.id,
                title = domain.title,
                primaryProductId = domain.primaryProduct.productId,
                primaryProductName = domain.primaryProduct.name,
                primaryProductPrice = domain.primaryProduct.price,
                primaryProductThumbnailUrl = domain.primaryProduct.thumbnailUrl,
                referenceProductsJson = domain.referenceProducts.toJsonString(),
                previewText = domain.previewText,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAtEpochMillis,
                isArchived = domain.isArchived,
                readOnlyReason = when (domain.readOnlyReason) {
                    ReadOnlyReason.ARCHIVED -> "ARCHIVED"
                    ReadOnlyReason.PRIMARY_DELETED -> "PRIMARY_DELETED"
                    ReadOnlyReason.OFFLINE -> "OFFLINE"
                    ReadOnlyReason.NONE -> "NONE"
                },
                cachedAt = cachedAt
            )
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val sender: String,
    val content: String,
    val productSnapshotsJson: String = "[]",
    val createdAt: Long,
    val emotion: String = "NEUTRAL",
    val status: String = "SENT",
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        threadId = threadId,
        sender = when (sender) {
            "USER" -> MessageSender.USER
            "SYSTEM" -> MessageSender.SYSTEM
            else -> MessageSender.CHANKO
        },
        content = content,
        productSnapshots = productSnapshotsJson.toProductSnapshotList(),
        createdAtEpochMillis = createdAt,
        emotion = runCatching { MascotEmotion.valueOf(emotion) }.getOrDefault(MascotEmotion.NEUTRAL),
        status = runCatching { MessageStatus.valueOf(status) }.getOrDefault(MessageStatus.SENT),
        isPending = status == "SENDING"
    )

    companion object {
        fun fromDomain(domain: ChatMessage, cachedAt: Long = System.currentTimeMillis()): ChatMessageEntity =
            ChatMessageEntity(
                id = domain.id,
                threadId = domain.threadId,
                sender = domain.sender.name,
                content = domain.content,
                productSnapshotsJson = domain.productSnapshots.toJsonString(),
                createdAt = domain.createdAtEpochMillis,
                emotion = domain.emotion.name,
                status = domain.status.name,
                cachedAt = cachedAt
            )
    }
}

private fun ProductSnapshot.toJson(): JSONObject = JSONObject().apply {
    put("productId", productId)
    put("name", name)
    put("price", price)
    put("thumbnailUrl", thumbnailUrl ?: "")
    put("isPrimaryDeleted", isPrimaryDeleted)
}

private fun JSONObject.toProductSnapshot(): ProductSnapshot = ProductSnapshot(
    productId = optString("productId", ""),
    name = optString("name", ""),
    price = optLong("price", 0L),
    thumbnailUrl = optString("thumbnailUrl").takeIf { it.isNotEmpty() },
    isPrimaryDeleted = optBoolean("isPrimaryDeleted", false)
)

private fun List<ProductSnapshot>.toJsonString(): String {
    val arr = JSONArray()
    forEach { arr.put(it.toJson()) }
    return arr.toString()
}

private fun String?.toProductSnapshotList(): List<ProductSnapshot> {
    if (isNullOrEmpty()) return emptyList()
    return runCatching {
        val arr = JSONArray(this)
        val list = mutableListOf<ProductSnapshot>()
        for (i in 0 until arr.length()) {
            list.add(arr.getJSONObject(i).toProductSnapshot())
        }
        list
    }.getOrDefault(emptyList())
}
