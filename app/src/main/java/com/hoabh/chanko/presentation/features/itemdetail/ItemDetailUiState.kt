package com.hoabh.chanko.presentation.features.itemdetail

import androidx.compose.runtime.Immutable
import com.hoabh.chanko.presentation.base.UiText
import com.hoabh.chanko.presentation.features.myitems.ItemDisplayStatus

@Immutable
data class DecisionJourneyStepUi(
    val title: String,
    val description: String,
    val formattedTimestamp: String,
    val titleUiText: UiText = UiText.DynamicString(title),
    val descriptionUiText: UiText = UiText.DynamicString(description)
)

@Immutable
data class PurchaseHistoryItemUi(
    val purchaseId: String,
    val formattedDate: String,
    val actualUnitPriceFormatted: String,
    val quantity: Int,
    val totalAmountFormatted: String,
    val paymentType: String
)

@Immutable
data class ItemDetailUiState(
    val isLoading: Boolean = true,
    val itemNotFound: Boolean = false,
    val id: Int = 0,
    val name: String = "",
    val formattedPrice: String = "",
    val rawPrice: Long = 0L,
    val imageUri: String? = null,
    val displayStatus: ItemDisplayStatus = ItemDisplayStatus.CoolingOff,
    val reason: String = "",
    val formattedAddedDate: String = "",
    val cooldownDurationFormatted: String = "",
    val remainingMillis: Long = 0L,
    val remainingFormatted: String = "",
    val speechBubbleText: String = "",
    val primaryCtaText: String = "",
    val decisionJourney: List<DecisionJourneyStepUi> = emptyList(),
    val purchaseHistory: List<PurchaseHistoryItemUi> = emptyList(),
    val isRefrozen: Boolean = false,
    val isMoreOptionsSheetOpen: Boolean = false,
    val isSkipWaitingConfirmDialogOpen: Boolean = false,
    val isReFreezeConfirmDialogOpen: Boolean = false,
    val isRecordPurchaseSheetOpen: Boolean = false,
    val speechBubbleUiText: UiText = UiText.DynamicString(speechBubbleText),
    val primaryCtaUiText: UiText = UiText.DynamicString(primaryCtaText),
    val remainingFormattedUiText: UiText = UiText.DynamicString(remainingFormatted),
    val cooldownDurationUiText: UiText = UiText.DynamicString(cooldownDurationFormatted)
)
