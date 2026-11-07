package com.hoabh.chanko.presentation.features.chat.detail

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.chat.model.ChatGenerationEvent
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.MascotEmotion
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.model.ReadOnlyReason
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel<ChatUiState, ChatUiEffect>(ChatUiState()) {

    private var observeThreadJob: Job? = null
    private var observeMessagesJob: Job? = null
    private var streamingJob: Job? = null

    fun initThread(
        threadId: String?,
        productId: String? = null,
        productName: String? = null,
        productPrice: Long? = null,
        thumbnailUrl: String? = null
    ) {
        if (!threadId.isNullOrBlank() && threadId != "new") {
            if (currentState.thread?.id != threadId) {
                selectThread(threadId)
            }
        } else if (!productId.isNullOrBlank()) {
            // NC-GAP-04: Draft client mode (chưa tạo thread server/Room trước tin đầu tiên)
            observeThreadJob?.cancel()
            observeMessagesJob?.cancel()

            val draftProduct = ProductSnapshot(
                productId = productId,
                name = productName ?: "Món đồ đang cân nhắc",
                price = productPrice ?: 0L,
                thumbnailUrl = thumbnailUrl
            )

            val welcomeMessage = ChatMessage(
                id = "m_welcome",
                threadId = "draft",
                sender = MessageSender.CHANKO,
                content = "Chào bạn! Mình thấy bạn đang cân nhắc \"${draftProduct.name}\". Hãy cùng Chanko phân tích xem món này có thực sự phù hợp với bạn không nhé!",
                productSnapshots = listOf(draftProduct),
                createdAtEpochMillis = System.currentTimeMillis(),
                emotion = MascotEmotion.CURIOUS
            )

            updateState {
                it.copy(
                    thread = null,
                    draftProduct = draftProduct,
                    messages = listOf(welcomeMessage),
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun selectThread(threadId: String) {
        observeThreadJob?.cancel()
        observeMessagesJob?.cancel()

        observeThreadJob = viewModelScope.launch {
            chatRepository.getThreadById(threadId).collect { thread ->
                updateState { it.copy(thread = thread, draftProduct = null, isLoading = false) }
            }
        }

        observeMessagesJob = viewModelScope.launch {
            chatRepository.getMessages(threadId).collect { messages ->
                updateState { it.copy(messages = messages) }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        updateState { it.copy(inputText = text) }
    }

    fun onQuickReplyClicked(reply: String) {
        // Quick reply fills composer only, does NOT auto-submit (SUPERSESSION / CONTRACT-SYNC)
        updateState { it.copy(inputText = reply) }
    }

    fun sendMessage() {
        val text = currentState.inputText.trim()
        if (text.isEmpty() || currentState.isSending || currentState.isStreaming || currentState.isReadOnly) return

        val idempotencyKey = "key_${UUID.randomUUID()}"
        val clientMessageId = "msg_${UUID.randomUUID()}"

        updateState { it.copy(isSending = true, inputText = "", error = null) }

        val activeThread = currentState.thread
        val draftProduct = currentState.draftProduct

        if (activeThread == null && draftProduct != null) {
            // First send: atomical creation (NC-GAP-04, EDGE-06)
            viewModelScope.launch {
                val newDraft = ChatThread(
                    id = "t_${UUID.randomUUID().toString().take(8)}",
                    title = draftProduct.name,
                    primaryProduct = draftProduct,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )

                val result = chatRepository.sendFirstMessage(
                    draftThread = newDraft,
                    firstMessageText = text,
                    idempotencyKey = idempotencyKey,
                    clientMessageId = clientMessageId
                )

                result.onSuccess { createdThread ->
                    updateState { it.copy(thread = createdThread, draftProduct = null, isSending = false) }
                    selectThread(createdThread.id)
                    triggerAiStream(createdThread.id)
                }.onFailure { error ->
                    updateState { it.copy(isSending = false, error = error.message, inputText = text) }
                }
            }
        } else if (activeThread != null) {
            viewModelScope.launch {
                val result = chatRepository.sendMessage(
                    threadId = activeThread.id,
                    text = text,
                    idempotencyKey = idempotencyKey,
                    clientMessageId = clientMessageId
                )

                result.onSuccess {
                    updateState { it.copy(isSending = false) }
                    triggerAiStream(activeThread.id)
                }.onFailure { error ->
                    updateState { it.copy(isSending = false, error = error.message, inputText = text) }
                }
            }
        }
    }

    private fun triggerAiStream(threadId: String) {
        val generationId = "gen_${UUID.randomUUID()}"
        streamingJob?.cancel()

        streamingJob = viewModelScope.launch {
            updateState {
                it.copy(
                    isStreaming = true,
                    activeGenerationId = generationId,
                    streamingChunk = ""
                )
            }

            chatRepository.streamAiResponse(threadId, generationId).collect { event ->
                when (event) {
                    is ChatGenerationEvent.Started -> {
                        updateState { it.copy(emotion = MascotEmotion.THINKING) }
                    }
                    is ChatGenerationEvent.Delta -> {
                        updateState { it.copy(streamingChunk = it.streamingChunk + event.textChunk) }
                    }
                    is ChatGenerationEvent.Done -> {
                        updateState {
                            it.copy(
                                isStreaming = false,
                                activeGenerationId = null,
                                streamingChunk = "",
                                emotion = event.fullMessage.emotion
                            )
                        }
                    }
                    is ChatGenerationEvent.Cancelled -> {
                        updateState {
                            it.copy(
                                isStreaming = false,
                                activeGenerationId = null,
                                streamingChunk = ""
                            )
                        }
                    }
                    is ChatGenerationEvent.Error -> {
                        updateState {
                            it.copy(
                                isStreaming = false,
                                activeGenerationId = null,
                                error = event.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onAddReferenceClicked() {
        val currentThread = currentState.thread ?: return
        viewModelScope.launch {
            val allEligible = chatRepository.getEligibleProducts().first()
            // EDGE-07: Filter out primary product and already linked references
            val available = allEligible.filterNot { product ->
                product.productId == currentThread.primaryProduct.productId ||
                        currentThread.referenceProducts.any { it.productId == product.productId }
            }
            updateState { it.copy(showReferencePicker = true, eligibleReferences = available) }
        }
    }

    fun onDismissReferencePicker() {
        updateState { it.copy(showReferencePicker = false) }
    }

    fun onSelectReference(product: ProductSnapshot) {
        val threadId = currentState.thread?.id ?: return
        updateState { it.copy(showReferencePicker = false) }
        viewModelScope.launch {
            chatRepository.addReference(threadId, product.productId)
        }
    }

    fun onRemoveReference(productId: String) {
        val threadId = currentState.thread?.id ?: return
        viewModelScope.launch {
            chatRepository.removeReference(threadId, productId)
        }
    }

    fun onOpenOptions() {
        updateState { it.copy(showOptionsSheet = true) }
    }

    fun onDismissOptions() {
        updateState { it.copy(showOptionsSheet = false) }
    }

    fun onRequestRename() {
        updateState { it.copy(showOptionsSheet = false, showRenameDialog = true) }
    }

    fun onDismissRename() {
        updateState { it.copy(showRenameDialog = false) }
    }

    fun onConfirmRename(newTitle: String) {
        val threadId = currentState.thread?.id ?: return
        viewModelScope.launch {
            chatRepository.renameThread(threadId, newTitle)
            updateState { it.copy(showRenameDialog = false) }
        }
    }

    fun onRequestArchive() {
        checkStreamAndExecute {
            updateState { it.copy(showOptionsSheet = false, showArchiveDialog = true) }
        }
    }

    fun onDismissArchive() {
        updateState { it.copy(showArchiveDialog = false) }
    }

    fun onConfirmArchive() {
        val threadId = currentState.thread?.id ?: return
        viewModelScope.launch {
            chatRepository.archiveThread(threadId)
            updateState { it.copy(showArchiveDialog = false) }
        }
    }

    fun onRestoreThread() {
        val threadId = currentState.thread?.id ?: return
        viewModelScope.launch {
            chatRepository.restoreThread(threadId)
            updateState { it.copy(showOptionsSheet = false) }
        }
    }

    fun onRequestDelete() {
        checkStreamAndExecute {
            updateState { it.copy(showOptionsSheet = false, showDeleteDialog = true) }
        }
    }

    fun onDismissDelete() {
        updateState { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        val threadId = currentState.thread?.id ?: return
        viewModelScope.launch {
            chatRepository.deleteThread(threadId)
            updateState { it.copy(showDeleteDialog = false) }
            sendEffect(ChatUiEffect.NavigateBack)
        }
    }

    private fun checkStreamAndExecute(action: () -> Unit) {
        if (currentState.isStreaming) {
            // EDGE-12: Must cancel or finish before delete/archive
            val genId = currentState.activeGenerationId
            val threadId = currentState.thread?.id
            if (genId != null && threadId != null) {
                viewModelScope.launch {
                    chatRepository.cancelGeneration(threadId, genId)
                    streamingJob?.cancel()
                    updateState { it.copy(isStreaming = false, activeGenerationId = null) }
                    action()
                }
            }
        } else {
            action()
        }
    }

    fun onProductCardClicked(productId: String) {
        sendEffect(ChatUiEffect.NavigateToItemDetail(productId))
    }

    fun onBackClicked() {
        sendEffect(ChatUiEffect.NavigateBack)
    }
}
