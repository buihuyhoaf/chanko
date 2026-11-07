package com.hoabh.chanko.data.chat.fake

import com.hoabh.chanko.domain.chat.model.ChatGenerationEvent
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.CoolingOffInfo
import com.hoabh.chanko.domain.chat.model.CoolingOffStatus
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.MessageStatus
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import com.hoabh.chanko.domain.chat.util.MonotonicClockCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeChatRepository @Inject constructor() : ChatRepository {

    private val now = System.currentTimeMillis()
    private val oneDay = 86_400_000L

    // In-memory products
    private val _products = MutableStateFlow<List<ProductSnapshot>>(emptyList())

    // In-memory threads & messages
    private val _threads = MutableStateFlow<List<ChatThread>>(emptyList())
    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    // Track active generations for explicit cancellation (EDGE-12)
    private val activeGenerations = ConcurrentHashMap<String, Boolean>()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val pHeadphones = ProductSnapshot(
            productId = "p_headphones",
            name = "Tai nghe mẫu A",
            price = 7_990_000L,
            thumbnailUrl = "chat_fixture_headphones",
            coolingOff = CoolingOffInfo(
                startedAtEpochMillis = now - oneDay,
                durationMillis = 3 * oneDay,
                remainingMillis = 2 * oneDay,
                progress = 0.33f,
                status = CoolingOffStatus.ACTIVE
            )
        )

        val pWatch = ProductSnapshot(
            productId = "p_watch",
            name = "Đồng hồ mẫu B",
            price = 11_990_000L,
            thumbnailUrl = "chat_fixture_watch",
            coolingOff = CoolingOffInfo(
                startedAtEpochMillis = now - oneDay,
                durationMillis = 3 * oneDay,
                remainingMillis = 2 * oneDay,
                progress = 0.33f,
                status = CoolingOffStatus.ACTIVE
            )
        )

        val pKeyboard = ProductSnapshot(
            productId = "p_keyboard",
            name = "Bàn phím mẫu C",
            price = 2_190_000L,
            thumbnailUrl = "chat_fixture_keyboard",
            coolingOff = CoolingOffInfo(
                startedAtEpochMillis = now - 2 * oneDay,
                durationMillis = oneDay,
                remainingMillis = 0L,
                progress = 1.0f,
                status = CoolingOffStatus.ELAPSED
            )
        )

        val pChair = ProductSnapshot(
            productId = "p_chair",
            name = "Ghế mẫu D",
            price = 3_990_000L,
            thumbnailUrl = "chat_fixture_chair",
            coolingOff = CoolingOffInfo(
                startedAtEpochMillis = 0L,
                durationMillis = 0L,
                remainingMillis = 0L,
                progress = 0f,
                status = CoolingOffStatus.UNKNOWN
            )
        )

        _products.value = listOf(pHeadphones, pWatch, pKeyboard, pChair)

        // Seed thread 1
        val t1Messages = listOf(
            ChatMessage(
                id = "m_01",
                threadId = "t_01",
                sender = MessageSender.USER,
                content = "Tôi đang phân vân chiếc tai nghe này, giá 7.990.000đ có đáng không?",
                createdAtEpochMillis = now - 3600_000L,
                status = MessageStatus.SENT
            ),
            ChatMessage(
                id = "m_02",
                threadId = "t_01",
                sender = MessageSender.CHANKO,
                content = "Tai nghe mẫu A có chất âm tốt và chống ồn chủ động xuất sắc, tuy nhiên mức giá 7.990.000đ tương đối cao. Chanko khuyên bạn nên đợi hết thời gian hạ nhiệt để cân nhắc thực tế nhu cầu.",
                productSnapshots = listOf(pHeadphones),
                createdAtEpochMillis = now - 3500_000L,
                status = MessageStatus.SENT,
                emotion = MascotEmotion.CURIOUS
            )
        )

        val t1 = ChatThread(
            id = "t_01",
            title = "Tai nghe mẫu A",
            primaryProduct = pHeadphones,
            referenceProducts = emptyList(),
            lastMessage = t1Messages.last(),
            updatedAtEpochMillis = now - 3500_000L,
            isArchived = false,
            readOnlyReason = ReadOnlyReason.NONE
        )

        // Seed thread 2
        val t2Messages = listOf(
            ChatMessage(
                id = "m_03",
                threadId = "t_02",
                sender = MessageSender.USER,
                content = "Nên mua bàn phím mẫu C hay ghế mẫu D trước?",
                createdAtEpochMillis = now - 7200_000L,
                status = MessageStatus.SENT
            ),
            ChatMessage(
                id = "m_04",
                threadId = "t_02",
                sender = MessageSender.CHANKO,
                content = "Bàn phím mẫu C (2.190.000đ) đã hết thời gian hạ nhiệt và cần thiết cho công việc gõ máy hàng ngày. Ghế mẫu D (3.990.000đ) có chi phí cao hơn, bạn nên hạ nhiệt thêm trước khi quyết định.",
                productSnapshots = listOf(pKeyboard, pChair),
                createdAtEpochMillis = now - 7100_000L,
                status = MessageStatus.SENT,
                emotion = MascotEmotion.THINKING
            )
        )

        val t2 = ChatThread(
            id = "t_02",
            title = "Bàn phím mẫu C",
            primaryProduct = pKeyboard,
            referenceProducts = listOf(pChair),
            lastMessage = t2Messages.last(),
            updatedAtEpochMillis = now - 7100_000L,
            isArchived = false,
            readOnlyReason = ReadOnlyReason.NONE
        )

        _threads.value = listOf(t1, t2)
        _messages.value = mapOf(
            "t_01" to t1Messages,
            "t_02" to t2Messages
        )
    }

    override fun getEligibleProducts(query: String?, cursor: String?, limit: Int): Flow<List<ProductSnapshot>> {
        return _products.asStateFlow().map { list ->
            if (query.isNullOrBlank()) {
                list.take(limit)
            } else {
                list.filter { it.name.contains(query, ignoreCase = true) || it.productId.contains(query, ignoreCase = true) }
                    .take(limit)
            }
        }
    }

    override suspend fun findActiveThreadForProduct(productId: String): ChatThread? {
        return _threads.value.firstOrNull {
            it.primaryProduct.productId == productId && !it.isArchived
        }
    }

    override suspend fun getArchivedThreadsForProduct(productId: String): List<ChatThread> {
        return _threads.value.filter {
            it.primaryProduct.productId == productId && it.isArchived
        }
    }

    override fun getThreads(query: String?, isArchived: Boolean): Flow<List<ChatThread>> {
        return _threads.asStateFlow().map { list ->
            list.filter { it.isArchived == isArchived }
                .filter { thread ->
                    if (query.isNullOrBlank()) true
                    else thread.title.contains(query, ignoreCase = true) ||
                            thread.primaryProduct.name.contains(query, ignoreCase = true)
                }
                .sortedWith(compareByDescending<ChatThread> { it.updatedAtEpochMillis }.thenByDescending { it.id })
        }
    }

    override fun getThreadById(threadId: String): Flow<ChatThread?> {
        return _threads.asStateFlow().map { list ->
            list.firstOrNull { it.id == threadId }
        }
    }

    override suspend fun sendFirstMessage(
        draftThread: ChatThread,
        firstMessageText: String,
        idempotencyKey: String,
        clientMessageId: String
    ): Result<ChatThread> {
        val threadId = draftThread.id.ifBlank { "t_${UUID.randomUUID().toString().take(8)}" }
        val messageId = clientMessageId.ifBlank { "m_${UUID.randomUUID().toString().take(8)}" }

        val userMessage = ChatMessage(
            id = messageId,
            threadId = threadId,
            sender = MessageSender.USER,
            content = firstMessageText,
            createdAtEpochMillis = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        val readOnly = if (draftThread.primaryProduct.isPrimaryDeleted) {
            ReadOnlyReason.PRIMARY_DELETED
        } else {
            draftThread.readOnlyReason
        }

        val newThread = draftThread.copy(
            id = threadId,
            lastMessage = userMessage,
            updatedAtEpochMillis = System.currentTimeMillis(),
            isArchived = draftThread.isArchived,
            readOnlyReason = readOnly
        )

        val currentThreads = _threads.value.toMutableList()
        currentThreads.removeAll { it.id == threadId }
        currentThreads.add(0, newThread)
        _threads.value = currentThreads

        val currentMap = _messages.value.toMutableMap()
        currentMap[threadId] = listOf(userMessage)
        _messages.value = currentMap

        return Result.success(newThread)
    }

    override suspend fun renameThread(threadId: String, newTitle: String): Result<Unit> {
        val currentThreads = _threads.value.map {
            if (it.id == threadId) it.copy(title = newTitle, updatedAtEpochMillis = System.currentTimeMillis())
            else it
        }
        _threads.value = currentThreads
        return Result.success(Unit)
    }

    override suspend fun archiveThread(threadId: String): Result<Unit> {
        val currentThreads = _threads.value.map {
            if (it.id == threadId) it.copy(
                isArchived = true,
                readOnlyReason = ReadOnlyReason.ARCHIVED,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            else it
        }
        _threads.value = currentThreads
        return Result.success(Unit)
    }

    override suspend fun restoreThread(threadId: String): Result<Unit> {
        val currentThreads = _threads.value.map {
            if (it.id == threadId) it.copy(
                isArchived = false,
                readOnlyReason = if (it.primaryProduct.isPrimaryDeleted) ReadOnlyReason.PRIMARY_DELETED else ReadOnlyReason.NONE,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            else it
        }
        _threads.value = currentThreads
        return Result.success(Unit)
    }

    override suspend fun deleteThread(threadId: String): Result<Unit> {
        _threads.value = _threads.value.filterNot { it.id == threadId }
        val currentMap = _messages.value.toMutableMap()
        currentMap.remove(threadId)
        _messages.value = currentMap
        return Result.success(Unit)
    }

    override fun getMessages(threadId: String): Flow<List<ChatMessage>> {
        return _messages.asStateFlow().map { map ->
            map[threadId] ?: emptyList()
        }
    }

    override suspend fun sendMessage(
        threadId: String,
        text: String,
        idempotencyKey: String,
        clientMessageId: String
    ): Result<ChatMessage> {
        val currentThread = _threads.value.firstOrNull { it.id == threadId }
            ?: return Result.failure(IllegalArgumentException("Thread not found"))

        if (currentThread.readOnlyReason != ReadOnlyReason.NONE) {
            return Result.failure(IllegalStateException("Thread is read-only: ${currentThread.readOnlyReason}"))
        }

        val messageId = clientMessageId.ifBlank { "m_${UUID.randomUUID().toString().take(8)}" }
        val userMessage = ChatMessage(
            id = messageId,
            threadId = threadId,
            sender = MessageSender.USER,
            content = text,
            createdAtEpochMillis = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        val currentMessages = (_messages.value[threadId] ?: emptyList()).toMutableList()
        currentMessages.add(userMessage)

        val currentMap = _messages.value.toMutableMap()
        currentMap[threadId] = currentMessages
        _messages.value = currentMap

        // Update thread updatedAt
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(lastMessage = userMessage, updatedAtEpochMillis = System.currentTimeMillis())
            else it
        }

        return Result.success(userMessage)
    }

    override suspend fun addReference(threadId: String, productId: String): Result<ChatThread> {
        val thread = _threads.value.firstOrNull { it.id == threadId }
            ?: return Result.failure(IllegalArgumentException("Thread not found"))

        if (thread.primaryProduct.productId == productId) {
            return Result.failure(IllegalArgumentException("Cannot add primary product as reference (EDGE-07)"))
        }

        if (thread.referenceProducts.any { it.productId == productId }) {
            return Result.failure(IllegalArgumentException("Product already referenced (EDGE-07)"))
        }

        val productToAdd = _products.value.firstOrNull { it.productId == productId }
            ?: return Result.failure(IllegalArgumentException("Product not found"))

        val updatedReferences = thread.referenceProducts + productToAdd
        val updatedThread = thread.copy(
            referenceProducts = updatedReferences,
            updatedAtEpochMillis = System.currentTimeMillis()
        )

        _threads.value = _threads.value.map { if (it.id == threadId) updatedThread else it }
        return Result.success(updatedThread)
    }

    override suspend fun removeReference(threadId: String, productId: String): Result<ChatThread> {
        val thread = _threads.value.firstOrNull { it.id == threadId }
            ?: return Result.failure(IllegalArgumentException("Thread not found"))

        val updatedReferences = thread.referenceProducts.filterNot { it.productId == productId }
        val updatedThread = thread.copy(
            referenceProducts = updatedReferences,
            updatedAtEpochMillis = System.currentTimeMillis()
        )

        _threads.value = _threads.value.map { if (it.id == threadId) updatedThread else it }
        return Result.success(updatedThread)
    }

    override fun streamAiResponse(threadId: String, generationId: String): Flow<ChatGenerationEvent> = flow {
        val thread = _threads.value.firstOrNull { it.id == threadId }
        if (thread == null) {
            emit(ChatGenerationEvent.Error("Thread not found", "NOT_FOUND"))
            return@flow
        }

        activeGenerations[generationId] = true
        emit(ChatGenerationEvent.Started(generationId))
        delay(100)

        val productNames = (listOf(thread.primaryProduct) + thread.referenceProducts).joinToString(", ") { it.name }
        val chunks = listOf(
            "Chanko đã phân tích chi tiết ",
            "về các sản phẩm bạn quan tâm: $productNames. ",
            "Sản phẩm chính ${thread.primaryProduct.name} có giá ${thread.primaryProduct.price}đ. ",
            "Hãy cân nhắc dựa trên ngân sách và ưu tiên thực tế nhé!"
        )

        val fullTextBuilder = StringBuilder()
        for (chunk in chunks) {
            if (activeGenerations[generationId] != true) {
                emit(ChatGenerationEvent.Cancelled(fullTextBuilder.toString()))
                return@flow
            }
            delay(150)
            fullTextBuilder.append(chunk)
            emit(ChatGenerationEvent.Delta(chunk))
        }

        val assistantMessage = ChatMessage(
            id = "m_ai_${UUID.randomUUID().toString().take(8)}",
            threadId = threadId,
            sender = MessageSender.CHANKO,
            content = fullTextBuilder.toString(),
            productSnapshots = listOf(thread.primaryProduct) + thread.referenceProducts,
            createdAtEpochMillis = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            emotion = MascotEmotion.CURIOUS
        )

        // Save assistant message
        val currentMessages = (_messages.value[threadId] ?: emptyList()).toMutableList()
        currentMessages.add(assistantMessage)
        val currentMap = _messages.value.toMutableMap()
        currentMap[threadId] = currentMessages
        _messages.value = currentMap

        // Update thread
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(lastMessage = assistantMessage, updatedAtEpochMillis = System.currentTimeMillis())
            else it
        }

        activeGenerations.remove(generationId)
        emit(ChatGenerationEvent.Done(assistantMessage))
    }

    override suspend fun cancelGeneration(threadId: String, generationId: String): Result<Unit> {
        activeGenerations[generationId] = false
        activeGenerations.remove(generationId)
        return Result.success(Unit)
    }
}
