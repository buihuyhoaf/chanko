package com.hoabh.chanko.domain.chat.repository

import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import kotlinx.coroutines.flow.Flow

/**
 * Hợp đồng giao tiếp cho Chat with Chanko (tuân thủ Backend Contract v1.4).
 */
interface ChatRepository {
    /**
     * Luồng danh sách các phiên hội thoại (không bao gồm các thread đã archive).
     */
    fun getActiveThreads(): Flow<List<ChatThread>>

    /**
     * Luồng danh sách các phiên hội thoại đã lưu trữ (Archived).
     */
    fun getArchivedThreads(): Flow<List<ChatThread>>

    /**
     * Lấy chi tiết một phiên trò chuyện theo ID.
     */
    fun getThreadById(threadId: String): Flow<ChatThread?>

    /**
     * Lấy danh sách tin nhắn theo Thread ID.
     */
    fun getMessages(threadId: String): Flow<List<ChatMessage>>

    /**
     * Gửi tin nhắn mới trong một thread (hoặc tạo mới nếu thread chưa tồn tại).
     */
    suspend fun sendMessage(threadId: String, text: String): Result<ChatMessage>

    /**
     * Chuyển đổi ngữ cảnh từ FREE sang ITEM trên CÙNG MỘT THREAD (Approved A2 Amendment).
     * Giữ nguyên toàn bộ message IDs, transcript, thứ tự và draft.
     */
    suspend fun bindExistingItem(
        threadId: String,
        itemId: String,
        itemName: String,
        itemPriceVnd: Long
    ): Result<ChatThread>

    /**
     * Yêu cầu đánh giá sớm (Request Early Verdict) cho món đồ đang liên kết.
     */
    suspend fun requestVerdict(threadId: String): Result<ChatMessage>

    /**
     * Lưu trữ phiên trò chuyện (Archive).
     */
    suspend fun archiveThread(threadId: String): Result<Unit>

    /**
     * Khôi phục phiên trò chuyện từ kho lưu trữ.
     */
    suspend fun restoreThread(threadId: String): Result<Unit>

    /**
     * Xóa phiên trò chuyện (R07 Deletion).
     * Xóa hội thoại nhưng giữ nguyên các Official Decision Records gắn với Shopping Item.
     */
    suspend fun deleteThread(threadId: String): Result<Unit>
}
