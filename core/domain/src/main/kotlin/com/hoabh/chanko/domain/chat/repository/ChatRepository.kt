package com.hoabh.chanko.domain.chat.repository

import com.hoabh.chanko.domain.chat.model.ChatGenerationEvent
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện Kho dữ liệu Chat tuân thủ chuẩn OpenAPI 3.0.3 (13 operations).
 * B07: Contract Ready — Pending Backend Implementation.
 */
interface ChatRepository {
    /**
     * 1. GET /chat/products - Lấy danh sách sản phẩm đủ điều kiện chọn làm SP chính hoặc tham chiếu.
     */
    fun getEligibleProducts(query: String? = null, cursor: String? = null, limit: Int = 20): Flow<List<ProductSnapshot>>

    /**
     * 2. GET /chat/products/{productId}/threads - Tra cứu lịch sử chat của sản phẩm.
     */
    suspend fun findActiveThreadForProduct(productId: String): ChatThread?

    suspend fun getArchivedThreadsForProduct(productId: String): List<ChatThread>

    /**
     * 3. GET /chat/threads - Lấy danh sách cuộc trò chuyện sắp xếp theo lastMessageAt DESC.
     */
    fun getThreads(query: String? = null, isArchived: Boolean = false): Flow<List<ChatThread>>

    /**
     * 4. GET /chat/threads/{threadId} - Lấy chi tiết một phiên trò chuyện.
     */
    fun getThreadById(threadId: String): Flow<ChatThread?>

    /**
     * 5. POST /chat/threads - Khởi tạo thread mới nguyên tử kèm tin nhắn đầu tiên (NC-GAP-04).
     */
    suspend fun sendFirstMessage(
        draftThread: ChatThread,
        firstMessageText: String,
        idempotencyKey: String,
        clientMessageId: String
    ): Result<ChatThread>

    /**
     * 6. PATCH /chat/threads/{threadId} - Đổi tên cuộc trò chuyện.
     */
    suspend fun renameThread(threadId: String, newTitle: String): Result<Unit>

    /**
     * 7. POST /chat/threads/{threadId}/archive - Lưu trữ cuộc trò chuyện.
     */
    suspend fun archiveThread(threadId: String): Result<Unit>

    /**
     * 8. POST /chat/threads/{threadId}/restore - Khôi phục cuộc trò chuyện từ kho lưu trữ.
     */
    suspend fun restoreThread(threadId: String): Result<Unit>

    /**
     * 9. DELETE /chat/threads/{threadId} - Xóa vĩnh viễn phiên trò chuyện.
     */
    suspend fun deleteThread(threadId: String): Result<Unit>

    /**
     * 10. GET /chat/threads/{threadId}/messages - Lấy danh sách tin nhắn của cuộc trò chuyện.
     */
    fun getMessages(threadId: String): Flow<List<ChatMessage>>

    /**
     * 11. POST /chat/threads/{threadId}/messages - Gửi tin nhắn mới với cơ chế idempotent an toàn.
     */
    suspend fun sendMessage(
        threadId: String,
        text: String,
        idempotencyKey: String,
        clientMessageId: String
    ): Result<ChatMessage>

    /**
     * 12. POST /chat/threads/{threadId}/references - Thêm sản phẩm tham chiếu.
     *     DELETE /chat/threads/{threadId}/references/{productId} - Gỡ bỏ sản phẩm tham chiếu.
     */
    suspend fun addReference(threadId: String, productId: String): Result<ChatThread>

    suspend fun removeReference(threadId: String, productId: String): Result<ChatThread>

    /**
     * 13. GET /chat/threads/{threadId}/generations/{generationId}/events - Nhận luồng SSE phản hồi của AI.
     *     POST /chat/threads/{threadId}/generations/{generationId}/cancel - Hủy generation.
     */
    fun streamAiResponse(threadId: String, generationId: String): Flow<ChatGenerationEvent>

    suspend fun cancelGeneration(threadId: String, generationId: String): Result<Unit>
}
