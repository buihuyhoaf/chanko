package com.hoabh.chanko.data.chat.dto

import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.CoolingOffInfo
import com.hoabh.chanko.domain.chat.model.CoolingOffStatus
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.MessageStatus
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import com.hoabh.chanko.domain.chat.util.MonotonicClockCalculator

data class ProductSnapshotDto(
    val productId: String,
    val name: String,
    val thumbnailUri: String? = null,
    val priceMinor: Long,
    val currency: String = "VND",
    val capturedAt: String? = null
)

data class CoolingOffDto(
    val required: Boolean = true,
    val status: String = "active", // active, elapsed, unknown
    val startAt: String? = null,
    val endAt: String? = null
)

data class ChatProductDto(
    val snapshot: ProductSnapshotDto,
    val deleted: Boolean = false,
    val coolingOff: CoolingOffDto? = null
)

data class ChatMessageDto(
    val id: String,
    val threadId: String,
    val sender: String, // user, chanko, system
    val content: String,
    val productSnapshots: List<ProductSnapshotDto> = emptyList(),
    val createdAt: String,
    val status: String = "sent"
)

data class ChatThreadDto(
    val id: String,
    val title: String,
    val primaryProductId: String,
    val primaryContext: ChatProductDto,
    val referenceContexts: List<ChatProductDto> = emptyList(),
    val archivedAt: String? = null,
    val readOnlyReason: String? = null,
    val activeGenerationId: String? = null,
    val createdAt: String,
    val updatedAt: String
)

object ChatDtoMapper {

    fun toDomain(dto: ChatProductDto): ProductSnapshot {
        return ProductSnapshot(
            productId = dto.snapshot.productId,
            name = dto.snapshot.name,
            price = dto.snapshot.priceMinor,
            thumbnailUrl = dto.snapshot.thumbnailUri,
            coolingOff = dto.coolingOff?.let { toDomain(it) },
            isPrimaryDeleted = dto.deleted
        )
    }

    fun toDomain(dto: CoolingOffDto): CoolingOffInfo {
        val status = when (dto.status.lowercase()) {
            "elapsed" -> CoolingOffStatus.ELAPSED
            "unknown" -> CoolingOffStatus.UNKNOWN
            else -> CoolingOffStatus.ACTIVE
        }
        return CoolingOffInfo(
            startedAtEpochMillis = 0L,
            durationMillis = 0L,
            remainingMillis = if (status == CoolingOffStatus.ELAPSED) 0L else 86400000L,
            progress = if (status == CoolingOffStatus.ELAPSED) 1.0f else 0.5f,
            status = status
        )
    }

    fun toDomain(dto: ChatMessageDto, timestampMillis: Long = System.currentTimeMillis()): ChatMessage {
        return ChatMessage(
            id = dto.id,
            threadId = dto.threadId,
            sender = when (dto.sender.lowercase()) {
                "user" -> MessageSender.USER
                "system" -> MessageSender.SYSTEM
                else -> MessageSender.CHANKO
            },
            content = dto.content,
            productSnapshots = dto.productSnapshots.map {
                ProductSnapshot(
                    productId = it.productId,
                    name = it.name,
                    price = it.priceMinor,
                    thumbnailUrl = it.thumbnailUri
                )
            },
            createdAtEpochMillis = timestampMillis,
            status = when (dto.status.lowercase()) {
                "sending" -> MessageStatus.SENDING
                "failed" -> MessageStatus.FAILED
                else -> MessageStatus.SENT
            }
        )
    }

    fun toDomain(dto: ChatThreadDto, lastMessage: ChatMessage? = null, updatedMillis: Long = System.currentTimeMillis()): ChatThread {
        return ChatThread(
            id = dto.id,
            title = dto.title,
            primaryProduct = toDomain(dto.primaryContext),
            referenceProducts = dto.referenceContexts.map { toDomain(it) },
            lastMessage = lastMessage,
            updatedAtEpochMillis = updatedMillis,
            isArchived = dto.archivedAt != null,
            readOnlyReason = when (dto.readOnlyReason?.uppercase()) {
                "ARCHIVED" -> ReadOnlyReason.ARCHIVED
                "PRIMARY_DELETED" -> ReadOnlyReason.PRIMARY_DELETED
                "OFFLINE" -> ReadOnlyReason.OFFLINE
                else -> ReadOnlyReason.NONE
            }
        )
    }
}
