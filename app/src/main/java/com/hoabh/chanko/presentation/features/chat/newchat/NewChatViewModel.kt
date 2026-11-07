package com.hoabh.chanko.presentation.features.chat.newchat

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.chat.model.ChatThread
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewChatUiState(
    val products: List<ProductSnapshot> = emptyList(),
    val filteredProducts: List<ProductSnapshot> = emptyList(),
    val selectedProduct: ProductSnapshot? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val existingActiveThread: ChatThread? = null,
    val archivedThreads: List<ChatThread> = emptyList(),
    val showHistoryDialog: Boolean = false,
    val errorMessage: String? = null
) {
    val canStartChat: Boolean get() = selectedProduct != null
}

sealed interface NewChatUiEffect {
    data class NavigateToExistingChat(val threadId: String) : NewChatUiEffect
    data class NavigateToNewDraftChat(
        val productId: String,
        val productName: String,
        val productPrice: Long,
        val thumbnailUrl: String? = null
    ) : NewChatUiEffect
    data object NavigateToMyItems : NewChatUiEffect
    data object Close : NewChatUiEffect
}

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : BaseViewModel<NewChatUiState, NewChatUiEffect>(NewChatUiState(isLoading = true)) {

    init {
        loadEligibleProducts()
    }

    private fun loadEligibleProducts() {
        viewModelScope.launch {
            chatRepository.getEligibleProducts()
                .catch { e ->
                    updateState { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { products ->
                    updateState { state ->
                        val filtered = filterProducts(products, state.searchQuery)
                        state.copy(
                            products = products,
                            filteredProducts = filtered,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun filterProducts(products: List<ProductSnapshot>, query: String): List<ProductSnapshot> {
        if (query.isBlank()) return products
        val q = query.trim().lowercase()
        return products.filter {
            it.name.lowercase().contains(q) || it.productId.lowercase().contains(q)
        }
    }

    fun updateSearchQuery(query: String) {
        updateState { state ->
            state.copy(
                searchQuery = query,
                filteredProducts = filterProducts(state.products, query)
            )
        }
    }

    fun selectProduct(product: ProductSnapshot) {
        updateState { it.copy(selectedProduct = product) }
    }

    fun startChat() {
        val product = currentState.selectedProduct ?: return
        viewModelScope.launch {
            val existing = chatRepository.findActiveThreadForProduct(product.productId)
            val archived = chatRepository.getArchivedThreadsForProduct(product.productId)

            if (existing != null) {
                // CHAT-02, NC-GAP-01: Có chat cũ -> hiển thị dialog tùy chọn
                updateState {
                    it.copy(
                        existingActiveThread = existing,
                        archivedThreads = archived,
                        showHistoryDialog = true
                    )
                }
            } else {
                // NC-GAP-04: Mở draft mới
                sendEffect(
                    NewChatUiEffect.NavigateToNewDraftChat(
                        productId = product.productId,
                        productName = product.name,
                        productPrice = product.price,
                        thumbnailUrl = product.thumbnailUrl
                    )
                )
            }
        }
    }

    fun onContinueExistingChat() {
        val existing = currentState.existingActiveThread ?: return
        updateState { it.copy(showHistoryDialog = false) }
        sendEffect(NewChatUiEffect.NavigateToExistingChat(existing.id))
    }

    fun onCreateNewThreadAnyway() {
        val product = currentState.selectedProduct ?: return
        updateState { it.copy(showHistoryDialog = false) }
        sendEffect(
            NewChatUiEffect.NavigateToNewDraftChat(
                productId = product.productId,
                productName = product.name,
                productPrice = product.price,
                thumbnailUrl = product.thumbnailUrl
            )
        )
    }

    fun onDismissHistoryDialog() {
        updateState { it.copy(showHistoryDialog = false) }
    }

    fun onNavigateToMyItems() {
        sendEffect(NewChatUiEffect.NavigateToMyItems)
    }

    fun onClose() {
        sendEffect(NewChatUiEffect.Close)
    }
}
