package com.hoabh.chanko.presentation.features.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import com.hoabh.chanko.presentation.base.UiText
import com.hoabh.chanko.presentation.features.home.domain.HomeCurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val purchaseId: String = checkNotNull(savedStateHandle["purchaseId"])

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<TransactionDetailUiEffect>()
    val effect: SharedFlow<TransactionDetailUiEffect> = _effect.asSharedFlow()

    private var currentTransaction: PurchaseTransaction? = null

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            purchaseRepository.getPurchaseById(purchaseId).collect { tx ->
                if (tx == null) {
                    _uiState.update { it.copy(isLoading = false, isNotFound = true) }
                    return@collect
                }
                currentTransaction = tx
                val item = itemRepository.getItemById(tx.itemId).firstOrNull()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNotFound = false,
                        purchaseId = tx.id,
                        itemId = tx.itemId,
                        itemName = item?.name ?: "Món đồ",
                        actualUnitPrice = tx.actualUnitPrice,
                        quantity = tx.quantity,
                        totalAmount = tx.totalAmount,
                        formattedTotalAmount = HomeCurrencyFormatter.formatVnd(tx.totalAmount),
                        formattedUnitPrice = HomeCurrencyFormatter.formatVnd(tx.actualUnitPrice),
                        paymentType = tx.paymentType,
                        formattedPurchasedDate = dateFormat.format(Date(tx.purchasedTimestamp)),
                        note = tx.note,
                        itemNameUiText = item?.name?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.transaction_detail_default_item_name)
                    )
                }
            }
        }
    }

    fun onEvent(event: TransactionDetailUiEvent) {
        when (event) {
            is TransactionDetailUiEvent.ToggleEditMode -> {
                _uiState.update { it.copy(isEditMode = !it.isEditMode) }
            }
            is TransactionDetailUiEvent.UpdateFields -> {
                val current = currentTransaction ?: return
                viewModelScope.launch {
                    val updated = current.copy(
                        actualUnitPrice = event.actualUnitPrice,
                        quantity = event.quantity,
                        totalAmount = event.actualUnitPrice * event.quantity,
                        paymentType = event.paymentType,
                        note = event.note
                    )
                    purchaseRepository.updatePurchase(updated)
                    _uiState.update { it.copy(isEditMode = false) }
                    _effect.emit(TransactionDetailUiEffect.ShowToast(UiText.StringResource(R.string.transaction_detail_save_success)))
                }
            }
            is TransactionDetailUiEvent.RequestDelete -> {
                _uiState.update { it.copy(isDeleteDialogOpen = true) }
            }
            is TransactionDetailUiEvent.DismissDeleteDialog -> {
                _uiState.update { it.copy(isDeleteDialogOpen = false) }
            }
            is TransactionDetailUiEvent.ConfirmDelete -> {
                _uiState.update { it.copy(isDeleteDialogOpen = false) }
                viewModelScope.launch {
                    purchaseRepository.deletePurchase(purchaseId)
                    _effect.emit(TransactionDetailUiEffect.ShowToast(UiText.StringResource(R.string.transaction_detail_delete_success)))
                    _effect.emit(TransactionDetailUiEffect.NavigateBack)
                }
            }
        }
    }
}
