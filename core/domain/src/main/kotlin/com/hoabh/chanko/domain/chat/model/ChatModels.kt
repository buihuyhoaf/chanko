package com.hoabh.chanko.domain.chat.model

/**
 * 8 biểu cảm chính thức của Mascot Chanko theo approved whitelist.
 */
enum class MascotEmotion {
    NEUTRAL,
    CURIOUS,
    THINKING,
    ENCOURAGING,
    RELAXED,
    SLEEPING,
    CONCERNED,
    WELCOME
}

/**
 * Người gửi tin nhắn.
 */
enum class MessageSender {
    USER,
    CHANKO,
    SYSTEM
}

/**
 * Trạng thái gửi của tin nhắn.
 */
enum class MessageStatus {
    SENDING,
    SENT,
    FAILED
}

/**
 * Trạng thái hạ nhiệt bắt buộc (Mandatory Cooling-off).
 */
enum class CoolingOffStatus {
    ACTIVE,
    ELAPSED,
    UNKNOWN
}

/**
 * Thông tin chi tiết thời gian hạ nhiệt.
 */
data class CoolingOffInfo(
    val startedAtEpochMillis: Long,
    val durationMillis: Long,
    val remainingMillis: Long,
    val progress: Float,
    val status: CoolingOffStatus
)

/**
 * Lý do đoạn chat chuyển sang chế độ chỉ đọc (Read-only).
 */
sealed interface ReadOnlyReason {
    data object NONE : ReadOnlyReason
    data object ARCHIVED : ReadOnlyReason
    data object PRIMARY_DELETED : ReadOnlyReason
    data object OFFLINE : ReadOnlyReason
}

/**
 * Snapshot thông tin sản phẩm bất biến tại thời điểm phân tích hoặc gắn kết.
 */
data class ProductSnapshot(
    val productId: String,
    val name: String,
    val price: Long, // Integer minor unit VND (exponent 0)
    val thumbnailResId: Int? = null,
    val thumbnailUrl: String? = null,
    val coolingOff: CoolingOffInfo? = null,
    val isPrimaryDeleted: Boolean = false
)

/**
 * Một tin nhắn trong phiên hội thoại Product-Centric Chat.
 */
data class ChatMessage(
    val id: String,
    val threadId: String,
    val sender: MessageSender,
    val content: String,
    val productSnapshots: List<ProductSnapshot> = emptyList(),
    val createdAtEpochMillis: Long,
    val status: MessageStatus = MessageStatus.SENT,
    val emotion: MascotEmotion = MascotEmotion.NEUTRAL,
    val isPending: Boolean = false
) {
    // Backward compatibility helpers
    val text: String get() = content
    val timestamp: Long get() = createdAtEpochMillis
}

/**
 * Thông tin một phiên hội thoại Product-Centric Chat.
 * Luôn gắn liền với duy nhất 1 sản phẩm chính (primaryProduct) và 0 hoặc nhiều tham chiếu (referenceProducts).
 */
data class ChatThread(
    val id: String,
    val title: String,
    val primaryProduct: ProductSnapshot,
    val referenceProducts: List<ProductSnapshot> = emptyList(),
    val lastMessage: ChatMessage? = null,
    val updatedAtEpochMillis: Long,
    val isArchived: Boolean = false,
    val readOnlyReason: ReadOnlyReason = ReadOnlyReason.NONE
) {
    val previewText: String? get() = lastMessage?.content
    val updatedAt: Long get() = updatedAtEpochMillis
    val createdAt: Long get() = updatedAtEpochMillis
    val isReadOnly: Boolean get() = readOnlyReason != ReadOnlyReason.NONE
}

/**
 * Các sự kiện phản hồi phát trực tuyến (SSE-like streaming generation events).
 */
sealed interface ChatGenerationEvent {
    data class Started(val generationId: String) : ChatGenerationEvent
    data class Delta(val textChunk: String) : ChatGenerationEvent
    data class Done(val fullMessage: ChatMessage) : ChatGenerationEvent
    data class Cancelled(val partialText: String) : ChatGenerationEvent
    data class Error(val message: String, val code: String) : ChatGenerationEvent
}
