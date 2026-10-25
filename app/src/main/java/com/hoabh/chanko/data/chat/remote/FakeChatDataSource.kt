package com.hoabh.chanko.data.chat.remote

import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.DecisionVerdict
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ThreadMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Nguồn dữ liệu mẫu xác định (Deterministic Fake Data Source) mô phỏng 11 màn hình S01–S11
 * và các quy tắc nghiệp vụ theo hợp đồng v1.4.
 * Gắn nhãn Prototype rõ ràng, độc lập với backend.
 */
class FakeChatDataSource {

    private val _threads = MutableStateFlow<List<ChatThread>>(emptyList())
    val threads: Flow<List<ChatThread>> = _threads.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())

    init {
        seedInitialFixtures()
    }

    private fun seedInitialFixtures() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600_000
        val oneDayAgo = now - 86400_000

        // S02: Free Chat thread
        val freeThread = ChatThread(
            id = "thread_free_1",
            title = "Nói chuyện vu vơ về chi tiêu",
            mode = ThreadMode.FREE,
            createdAt = oneDayAgo,
            updatedAt = oneHourAgo,
            isArchived = false
        )

        val freeMessages = listOf(
            ChatMessage(
                id = "msg_f1",
                threadId = "thread_free_1",
                sender = MessageSender.USER,
                text = "Hôm nay đi dạo phố thấy nhiều đồ đẹp quá Chanko ơi!",
                timestamp = oneHourAgo - 60_000
            ),
            ChatMessage(
                id = "msg_f2",
                threadId = "thread_free_1",
                sender = MessageSender.CHANKO,
                text = "Đẹp mắt thì ngắm thôi nha sen! Ví tiền đang mỏng dính kìa, đừng để lòng tham dẫn lối!",
                timestamp = oneHourAgo - 30_000,
                emotion = MascotEmotion.CURIOUS
            )
        )

        // S03: Item Chat có Verdict
        val itemThread = ChatThread(
            id = "thread_item_1",
            title = "Bàn phím cơ không dây",
            mode = ThreadMode.ITEM,
            linkedItemId = "item_keyboard_01",
            linkedItemName = "Bàn phím cơ Custom",
            linkedItemPriceVnd = 2_500_000L,
            createdAt = oneHourAgo,
            updatedAt = now,
            isArchived = false
        )

        val itemMessages = listOf(
            ChatMessage(
                id = "msg_i1",
                threadId = "thread_item_1",
                sender = MessageSender.USER,
                text = "Tao đang muốn mua bàn phím cơ 2 triệu rưỡi này để gõ code cho sướng.",
                timestamp = now - 120_000
            ),
            ChatMessage(
                id = "msg_i2",
                threadId = "thread_item_1",
                sender = MessageSender.CHANKO,
                text = "Bàn phím cũ ở nhà vẫn gõ côm cốp mà? 2.500.000đ bằng 1/4 số tiền ăn cả tháng rồi đó!",
                timestamp = now - 90_000,
                emotion = MascotEmotion.THINKING
            ),
            ChatMessage(
                id = "msg_i3",
                threadId = "thread_item_1",
                sender = MessageSender.CHANKO,
                text = "Phán quyết của Chanko: BỎ QUA (SKIP). Hãy cho số tiền này vào Két tiết kiệm để hạ nhiệt cơn bốc đồng!",
                timestamp = now - 30_000,
                emotion = MascotEmotion.ENCOURAGING,
                verdict = DecisionVerdict.SKIP
            )
        )

        // S11: Archived thread
        val archivedThread = ChatThread(
            id = "thread_archived_1",
            title = "Đôi giày chạy bộ (Đã xong)",
            mode = ThreadMode.ITEM,
            linkedItemId = "item_shoes_01",
            linkedItemName = "Giày thể thao",
            linkedItemPriceVnd = 1_800_000L,
            createdAt = now - 7 * 86400_000,
            updatedAt = now - 6 * 86400_000,
            isArchived = true
        )

        val archivedMessages = listOf(
            ChatMessage(
                id = "msg_a1",
                threadId = "thread_archived_1",
                sender = MessageSender.CHANKO,
                text = "Đã bỏ qua món này thành công. Két tiết kiệm +1.800.000đ!",
                timestamp = now - 6 * 86400_000,
                emotion = MascotEmotion.SLEEPING,
                verdict = DecisionVerdict.SKIP
            )
        )

        _threads.value = listOf(freeThread, itemThread, archivedThread)
        _messages.value = mapOf(
            freeThread.id to freeMessages,
            itemThread.id to itemMessages,
            archivedThread.id to archivedMessages
        )
    }

    fun getMessagesFlow(threadId: String): Flow<List<ChatMessage>> {
        return _messages.map { it[threadId] ?: emptyList() }
    }

    fun getThreadById(threadId: String): Flow<ChatThread?> {
        return _threads.map { list -> list.find { it.id == threadId } }
    }

    suspend fun sendMessage(threadId: String, text: String): ChatMessage {
        val currentThreads = _threads.value.toMutableList()
        var targetThread = currentThreads.find { it.id == threadId }
        val effectiveThreadId = if (targetThread == null) {
            val newId = "thread_${UUID.randomUUID().toString().take(8)}"
            targetThread = ChatThread(
                id = newId,
                title = text.take(30),
                mode = ThreadMode.FREE,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            currentThreads.add(0, targetThread)
            _threads.value = currentThreads
            newId
        } else {
            targetThread.id
        }

        val userMsg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            threadId = effectiveThreadId,
            sender = MessageSender.USER,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        val existingMsgs = _messages.value[effectiveThreadId]?.toMutableList() ?: mutableListOf()
        existingMsgs.add(userMsg)

        // Phản hồi giả lập từ Chanko
        val botMsg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            threadId = effectiveThreadId,
            sender = MessageSender.CHANKO,
            text = "Chanko đã nghe: \"$text\". Bình tĩnh nào, cân nhắc kỹ túi tiền xem có đáng không nhé!",
            timestamp = System.currentTimeMillis() + 500,
            emotion = MascotEmotion.CURIOUS
        )
        existingMsgs.add(botMsg)

        val newMsgMap = _messages.value.toMutableMap()
        newMsgMap[effectiveThreadId] = existingMsgs
        _messages.value = newMsgMap

        // Update thread updatedAt
        _threads.value = _threads.value.map {
            if (it.id == effectiveThreadId) it.copy(updatedAt = System.currentTimeMillis()) else it
        }

        return botMsg
    }

    suspend fun bindExistingItem(
        threadId: String,
        itemId: String,
        itemName: String,
        itemPriceVnd: Long
    ): ChatThread {
        val currentThreads = _threads.value.toMutableList()
        val index = currentThreads.indexOfFirst { it.id == threadId }
        require(index >= 0) { "Thread không tồn tại" }

        val oldThread = currentThreads[index]
        val updatedThread = oldThread.copy(
            mode = ThreadMode.ITEM,
            linkedItemId = itemId,
            linkedItemName = itemName,
            linkedItemPriceVnd = itemPriceVnd,
            contextRevision = oldThread.contextRevision + 1,
            updatedAt = System.currentTimeMillis()
        )
        currentThreads[index] = updatedThread
        _threads.value = currentThreads

        // Bổ sung system message vào cùng transcript
        val existingMsgs = _messages.value[threadId]?.toMutableList() ?: mutableListOf()
        val systemMsg = ChatMessage(
            id = "sys_${UUID.randomUUID().toString().take(8)}",
            threadId = threadId,
            sender = MessageSender.SYSTEM,
            text = "Đã liên kết hội thoại với món đồ: $itemName (${itemPriceVnd}đ)",
            timestamp = System.currentTimeMillis()
        )
        existingMsgs.add(systemMsg)

        val newMap = _messages.value.toMutableMap()
        newMap[threadId] = existingMsgs
        _messages.value = newMap

        return updatedThread
    }

    suspend fun requestVerdict(threadId: String): ChatMessage {
        val thread = _threads.value.find { it.id == threadId }
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

        val existingMsgs = _messages.value[threadId]?.toMutableList() ?: mutableListOf()
        existingMsgs.add(verdictMsg)

        val newMap = _messages.value.toMutableMap()
        newMap[threadId] = existingMsgs
        _messages.value = newMap

        return verdictMsg
    }

    suspend fun archiveThread(threadId: String) {
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(isArchived = true) else it
        }
    }

    suspend fun restoreThread(threadId: String) {
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(isArchived = false) else it
        }
    }

    suspend fun deleteThread(threadId: String) {
        _threads.value = _threads.value.filterNot { it.id == threadId }
        val newMap = _messages.value.toMutableMap()
        newMap.remove(threadId)
        _messages.value = newMap
    }
}
