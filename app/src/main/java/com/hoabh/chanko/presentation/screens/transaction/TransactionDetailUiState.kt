package com.hoabh.chanko.presentation.screens.transaction

import androidx.compose.runtime.Immutable
import com.hoabh.chanko.domain.model.PaymentType

@Immutable
data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false,
    val purchaseId: String = "",
    val itemId: Int = 0,
    val itemName: String = "",
    val actualUnitPrice: Long = 0L,
    val quantity: Int = 1,
    val totalAmount: Long = 0L,
    val formattedTotalAmount: String = "",
    val formattedUnitPrice: String = "",
    val paymentType: PaymentType = PaymentType.PAY_NOW,
    val formattedPurchasedDate: String = "",
    val note: String = "",
    val isEditMode: Boolean = false,
    val isDeleteDialogOpen: Boolean = false
)

sealed interface TransactionDetailUiEvent {
    data object ToggleEditMode : TransactionDetailUiEvent
    data class UpdateFields(val actualUnitPrice: Long, val quantity: Int, val paymentType: PaymentType, val note: String) : TransactionDetailUiEvent
    data object RequestDelete : TransactionDetailUiEvent
    data object ConfirmDelete : TransactionDetailUiEvent
    data object DismissDeleteDialog : TransactionDetailUiEvent
}

sealed interface TransactionDetailUiEffect {
    data object NavigateBack : TransactionDetailUiEffect
    data class ShowToast(val message: String) : TransactionDetailUiEffect
}
