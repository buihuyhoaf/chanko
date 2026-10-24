package com.hoabh.chanko.presentation.screens.itemdetail

sealed interface ItemDetailUiEvent {
    data object OpenMoreOptions : ItemDetailUiEvent
    data object DismissMoreOptions : ItemDetailUiEvent
    data object PrimaryCtaClicked : ItemDetailUiEvent
    data object RequestSkipWaiting : ItemDetailUiEvent
    data object ConfirmSkipWaiting : ItemDetailUiEvent
    data object DismissSkipWaitingDialog : ItemDetailUiEvent
    data object RequestRecordPurchase : ItemDetailUiEvent
    data object DismissRecordPurchaseSheet : ItemDetailUiEvent
    data class ConfirmPurchase(val purchase: com.hoabh.chanko.domain.model.PurchaseTransaction) : ItemDetailUiEvent
    data object RequestArchiveItem : ItemDetailUiEvent
    data class PurchaseItemClicked(val purchaseId: String) : ItemDetailUiEvent
}

sealed interface ItemDetailUiEffect {
    data object NavigateBack : ItemDetailUiEffect
    data class OpenRecordPurchaseSheet(val itemId: Int) : ItemDetailUiEffect
    data class OpenTransactionDetail(val purchaseId: String) : ItemDetailUiEffect
    data class ShowToast(val message: String) : ItemDetailUiEffect
}
