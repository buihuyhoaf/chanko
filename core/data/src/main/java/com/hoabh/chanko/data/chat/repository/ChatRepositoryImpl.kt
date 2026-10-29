package com.hoabh.chanko.data.chat.repository

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.data.local.chat.dao.ChatDao
import com.hoabh.chanko.data.local.chat.entity.ChatMessageEntity
import com.hoabh.chanko.data.local.chat.entity.ChatThreadEntity
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.DecisionVerdict
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ThreadMode
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val fakeDataSource: FakeChatDataSource
) : ChatRepository {

    private val seedMutex = Mutex()
    private var isSeeded = false

    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            if (chatDao.getThreadCount() == 0) {
                val initialThreads = fakeDataSource.initialThreads
                val initialMessages = fakeDataSource.initialMessages
                chatDao.insertOrUpdateThreads(initialThreads.map { ChatThreadEntity.fromDomain(it) })
                val allMessages = initialMessages.values.flatten()
                chatDao.insertMessages(allMessages.map { ChatMessageEntity.fromDomain(it) })
            }
            isSeeded = true
        }
    }

    override fun getActiveThreads(): Flow<List<ChatThread>> {
        return chatDao.getActiveThreads()
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getArchivedThreads(): Flow<List<ChatThread>> {
        return chatDao.getArchivedThreads()
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getThreadById(threadId: String): Flow<ChatThread?> {
        return chatDao.getThreadById(threadId)
            .onStart { ensureSeeded() }
            .map { it?.toDomain() }
    }

    override fun getMessages(threadId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesByThreadId(threadId)
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun sendMessage(threadId: String, text: String): Result<ChatMessage> {
        return runCatching {
            ensureSeeded()
            val existing = if (threadId.isNotBlank()) chatDao.getThreadByIdDirect(threadId) else null
            val effectiveThreadId = if (existing == null) {
                val newId = "thread_${UUID.randomUUID().toString().take(8)}"
                val newThread = ChatThread(
                    id = newId,
                    title = text.take(30),
                    mode = ThreadMode.FREE,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                chatDao.insertOrUpdateThread(ChatThreadEntity.fromDomain(newThread))
                newId
            } else {
                existing.id
            }

            // 1. Lưu tin nhắn của User vào Room DB ngay lập tức
            val userMsg = ChatMessage(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                threadId = effectiveThreadId,
                sender = MessageSender.USER,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(ChatMessageEntity.fromDomain(userMsg))

            // 2. Mô phỏng thời gian Chanko suy nghĩ (1.5s typing delay)
            delay(1500L)

            // 3. Tạo câu trả lời của Chanko và lưu vào Room DB
            val botMsg = ChatMessage(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                threadId = effectiveThreadId,
                sender = MessageSender.CHANKO,
                text = "Chanko đã nghe: \"$text\". Bình tĩnh nào, cân nhắc kỹ túi tiền xem có đáng không nhé!",
                timestamp = System.currentTimeMillis(),
                emotion = MascotEmotion.CURIOUS
            )
            chatDao.insertMessage(ChatMessageEntity.fromDomain(botMsg))

            // 4. Cập nhật previewText và updatedAt của Thread trong Room DB
            chatDao.updateThreadPreview(
                threadId = effectiveThreadId,
                previewText = "Chanko: ${botMsg.text}",
                updatedAt = botMsg.timestamp
            )

            botMsg
        }
    }

    override suspend fun bindExistingItem(
        threadId: String,
        itemId: String,
        itemName: String,
        itemPriceVnd: Long
    ): Result<ChatThread> {
        return runCatching {
            ensureSeeded()
            chatDao.bindItemToThread(
                threadId = threadId,
                itemId = itemId,
                itemName = itemName,
                price = itemPriceVnd,
                updatedAt = System.currentTimeMillis()
            )

            val systemMsg = ChatMessage(
                id = "sys_${UUID.randomUUID().toString().take(8)}",
                threadId = threadId,
                sender = MessageSender.SYSTEM,
                text = "Đã liên kết hội thoại với món đồ: $itemName (${itemPriceVnd}đ)",
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(ChatMessageEntity.fromDomain(systemMsg))

            val updatedEntity = chatDao.getThreadByIdDirect(threadId)
                ?: error("Thread không tồn tại")
            updatedEntity.toDomain()
        }
    }

    override suspend fun requestVerdict(threadId: String): Result<ChatMessage> {
        return runCatching {
            ensureSeeded()
            val thread = chatDao.getThreadByIdDirect(threadId)
            val price = thread?.linkedItemPriceVnd ?: 1_000_000L
            val verdict = if (price > 2_000_000L) DecisionVerdict.SKIP else DecisionVerdict.WAIT

            val verdictMsg = ChatMessage(
                id = "verdict_${UUID.randomUUID().toString().take(8)}",
                threadId = threadId,
                sender = MessageSender.CHANKO,
                text = when (verdict) {
                    DecisionVerdict.SKIP -> "Chanko khuyên bạn nên BỎ QUA món này. Hãy giữ số tiền để mua những thứ thiết yếu hơn!"
                    DecisionVerdict.WAIT -> "Món này tạm thời hãy ĐỢI HẾT THỜI GIAN HẠ NHIỆT rồi hẵng chốt nhé!"
                    DecisionVerdict.BUY -> "Món đồ này hợp lý, nếu thực sự cần bạn có thể mua!"
                },
                timestamp = System.currentTimeMillis(),
                emotion = MascotEmotion.ENCOURAGING,
                verdict = verdict
            )

            chatDao.insertMessage(ChatMessageEntity.fromDomain(verdictMsg))
            chatDao.updateThreadPreview(
                threadId = threadId,
                previewText = "Chanko: ${verdictMsg.text}",
                updatedAt = verdictMsg.timestamp
            )

            verdictMsg
        }
    }

    override suspend fun archiveThread(threadId: String): Result<Unit> {
        return runCatching {
            ensureSeeded()
            chatDao.archiveThread(threadId)
        }
    }

    override suspend fun restoreThread(threadId: String): Result<Unit> {
        return runCatching {
            ensureSeeded()
            chatDao.restoreThread(threadId)
        }
    }

    override suspend fun renameThread(threadId: String, newTitle: String): Result<Unit> {
        return runCatching {
            ensureSeeded()
            chatDao.renameThread(threadId, newTitle)
        }
    }

    override suspend fun deleteThread(threadId: String): Result<Unit> {
        return runCatching {
            ensureSeeded()
            chatDao.deleteMessagesByThreadId(threadId)
            chatDao.deleteThread(threadId)
        }
    }
}
