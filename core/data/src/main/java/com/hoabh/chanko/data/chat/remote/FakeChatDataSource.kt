package com.hoabh.chanko.data.chat.remote

import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.DecisionVerdict
import com.hoabh.chanko.domain.chat.model.ItemContextStatus
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
        val fifteenMinsAgo = now - 15 * 60_000
        val twoHoursAgo = now - 2 * 3600_000
        val fiveHoursAgo = now - 5 * 3600_000
        val twoDaysAgo = now - 2 * 86400_000
        val sevenDaysAgo = now - 7 * 86400_000

        // 1. Thread ITEM (Đang cân nhắc - CONSIDERING)
        val phoneThread = ChatThread(
            id = "thread_item_phone",
            title = "Điện thoại Flagship",
            mode = ThreadMode.ITEM,
            linkedItemId = "item_phone_01",
            linkedItemName = "Điện thoại Flagship",
            linkedItemPriceVnd = 18_000_000L,
            previewText = "Chanko: 18 triệu bằng 3 tháng tiền trọ của bạn đấy! Đang hạ nhiệt đừng có bấm mua!",
            itemStatus = ItemContextStatus.CONSIDERING,
            createdAt = twoHoursAgo,
            updatedAt = fifteenMinsAgo,
            isArchived = false
        )
        val phoneMessages = listOf(
            ChatMessage(
                id = "msg_p1",
                threadId = "thread_item_phone",
                sender = MessageSender.USER,
                text = "Mình tính đổi điện thoại mới chụp ảnh cho nét, giá tầm 18 triệu.",
                timestamp = twoHoursAgo
            ),
            ChatMessage(
                id = "msg_p2",
                threadId = "thread_item_phone",
                sender = MessageSender.CHANKO,
                text = "Máy cũ pin vẫn trâu, cam vẫn chụp rõ mặt mà? 18 triệu bằng 3 tháng tiền trọ của bạn đấy! Đang hạ nhiệt đừng có bấm mua!",
                timestamp = fifteenMinsAgo,
                emotion = MascotEmotion.CONCERNED
            )
        )

        // 2. Thread ITEM (Chờ xem lại - READY, có Decision Verdict SKIP)
        val keyboardThread = ChatThread(
            id = "thread_item_keyboard",
            title = "Bàn phím cơ Custom",
            mode = ThreadMode.ITEM,
            linkedItemId = "item_keyboard_01",
            linkedItemName = "Bàn phím cơ Custom",
            linkedItemPriceVnd = 2_500_000L,
            previewText = "Chanko: Nhận định: BỎ QUA (SKIP). Hãy cho số tiền này vào Két tiết kiệm!",
            itemStatus = ItemContextStatus.READY,
            createdAt = fiveHoursAgo,
            updatedAt = twoHoursAgo,
            isArchived = false
        )
        val keyboardMessages = listOf(
            ChatMessage(
                id = "msg_k1",
                threadId = "thread_item_keyboard",
                sender = MessageSender.USER,
                text = "Hết 48h hạ nhiệt rồi, Chanko xem có nên xúc chiếc bàn phím 2.500.000đ này không?",
                timestamp = fiveHoursAgo
            ),
            ChatMessage(
                id = "msg_k2",
                threadId = "thread_item_keyboard",
                sender = MessageSender.CHANKO,
                text = "Bàn phím cũ ở nhà vẫn gõ tốt côm cốp. Bỏ 2.500.000đ lúc này chưa hợp lý đâu bạn ơi!",
                timestamp = twoHoursAgo - 60_000,
                emotion = MascotEmotion.THINKING
            ),
            ChatMessage(
                id = "msg_k3",
                threadId = "thread_item_keyboard",
                sender = MessageSender.CHANKO,
                text = "Nhận định của Chanko: BỎ QUA (SKIP). Hãy cho số tiền này vào Két tiết kiệm!",
                timestamp = twoHoursAgo,
                emotion = MascotEmotion.NEUTRAL,
                verdict = DecisionVerdict.SKIP
            )
        )

        // 3. Thread FREE (Trò chuyện tự do)
        val freeThread = ChatThread(
            id = "thread_free_1",
            title = "Nói chuyện vu vơ về chi tiêu",
            mode = ThreadMode.FREE,
            previewText = "Chanko: Đẹp mắt thì ngắm thôi nha sen! Ví tiền đang mỏng dính kìa!",
            itemStatus = null,
            createdAt = twoDaysAgo,
            updatedAt = fiveHoursAgo,
            isArchived = false
        )
        val freeMessages = listOf(
            ChatMessage(
                id = "msg_f1",
                threadId = "thread_free_1",
                sender = MessageSender.USER,
                text = "Hôm nay đi dạo phố thấy nhiều đồ đẹp quá Chanko ơi!",
                timestamp = twoDaysAgo
            ),
            ChatMessage(
                id = "msg_f2",
                threadId = "thread_free_1",
                sender = MessageSender.CHANKO,
                text = "Đẹp mắt thì ngắm thôi nha sen! Ví tiền đang mỏng dính kìa, đừng để lòng tham dẫn lối!",
                timestamp = fiveHoursAgo,
                emotion = MascotEmotion.CURIOUS
            )
        )

        // 4. Thread ITEM (Đã mua - PURCHASED)
        val jacketThread = ChatThread(
            id = "thread_item_jacket",
            title = "Áo khoác mùa đông",
            mode = ThreadMode.ITEM,
            linkedItemId = "item_jacket_01",
            linkedItemName = "Áo khoác gió ấm",
            linkedItemPriceVnd = 1_200_000L,
            previewText = "Chanko: Bạn có thể cân nhắc mua. Mùa lạnh đến rồi, giữ ấm là cần thiết!",
            itemStatus = ItemContextStatus.PURCHASED,
            createdAt = sevenDaysAgo,
            updatedAt = twoDaysAgo,
            isArchived = false
        )
        val jacketMessages = listOf(
            ChatMessage(
                id = "msg_j1",
                threadId = "thread_item_jacket",
                sender = MessageSender.USER,
                text = "Sắp vào mùa lạnh rồi, mình mua áo khoác 1tr2 nhé?",
                timestamp = sevenDaysAgo
            ),
            ChatMessage(
                id = "msg_j2",
                threadId = "thread_item_jacket",
                sender = MessageSender.CHANKO,
                text = "Nhận định của Chanko: BUY · Có thể cân nhắc mua. Giữ gìn sức khỏe là ưu tiên chính đáng!",
                timestamp = twoDaysAgo,
                emotion = MascotEmotion.NEUTRAL,
                verdict = DecisionVerdict.BUY
            )
        )

        // 5. Thread ARCHIVED (Đã lưu trữ)
        val archivedThread = ChatThread(
            id = "thread_archived_1",
            title = "Đôi giày chạy bộ (Đã xong)",
            mode = ThreadMode.ITEM,
            linkedItemId = "item_shoes_01",
            linkedItemName = "Giày thể thao",
            linkedItemPriceVnd = 1_800_000L,
            previewText = "Chanko: Đã bỏ qua món này thành công. Két tiết kiệm +1.800.000đ!",
            itemStatus = ItemContextStatus.CONSIDERING,
            createdAt = sevenDaysAgo,
            updatedAt = sevenDaysAgo + 86400_000,
            isArchived = true
        )
        val archivedMessages = listOf(
            ChatMessage(
                id = "msg_a1",
                threadId = "thread_archived_1",
                sender = MessageSender.CHANKO,
                text = "Đã bỏ qua món này thành công. Két tiết kiệm +1.800.000đ!",
                timestamp = sevenDaysAgo + 86400_000,
                emotion = MascotEmotion.SLEEPING,
                verdict = DecisionVerdict.SKIP
            )
        )

        _threads.value = listOf(phoneThread, keyboardThread, freeThread, jacketThread, archivedThread)
        _messages.value = mapOf(
            phoneThread.id to phoneMessages,
            keyboardThread.id to keyboardMessages,
            freeThread.id to freeMessages,
            jacketThread.id to jacketMessages,
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

        // Update thread updatedAt and previewText
        _threads.value = _threads.value.map {
            if (it.id == effectiveThreadId) it.copy(
                previewText = "Chanko: ${botMsg.text}",
                updatedAt = System.currentTimeMillis()
            ) else it
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

    suspend fun renameThread(threadId: String, newTitle: String) {
        _threads.value = _threads.value.map {
            if (it.id == threadId) it.copy(title = newTitle, updatedAt = System.currentTimeMillis()) else it
        }
    }

    suspend fun deleteThread(threadId: String) {
        _threads.value = _threads.value.filterNot { it.id == threadId }
        val newMap = _messages.value.toMutableMap()
        newMap.remove(threadId)
        _messages.value = newMap
    }
}
