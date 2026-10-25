package com.hoabh.chanko.domain.chat.model

/**
 * Phán quyết mua sắm từ Decision Engine (TECH-03, Immutable).
 */
enum class DecisionVerdict {
    BUY,
    WAIT,
    SKIP
}

/**
 * Chế độ phiên trò chuyện.
 */
enum class ThreadMode {
    FREE,
    ITEM
}

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
 * Phong cách phản hồi của Chanko (chỉ thay đổi giọng điệu, không đổi verdict).
 */
enum class ChankoPersonality {
    FRIENDLY,
    SASSY,
    SAVAGE
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
 * Một tin nhắn trong phiên hội thoại.
 */
data class ChatMessage(
    val id: String,
    val threadId: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long,
    val emotion: MascotEmotion = MascotEmotion.NEUTRAL,
    val verdict: DecisionVerdict? = null,
    val isPending: Boolean = false
)

/**
 * Thông tin một phiên hội thoại Chat.
 */
data class ChatThread(
    val id: String,
    val title: String,
    val mode: ThreadMode,
    val linkedItemId: String? = null,
    val linkedItemName: String? = null,
    val linkedItemPriceVnd: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val contextRevision: Int = 1
)
