package com.hoabh.chanko.presentation.features.itemdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.policy.FreezeCountdownCalculator
import com.hoabh.chanko.domain.policy.FreezeDurationPolicy
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import com.hoabh.chanko.presentation.base.UiText
import com.hoabh.chanko.presentation.features.home.mapper.HomeCurrencyFormatter
import com.hoabh.chanko.presentation.features.myitems.ItemDisplayStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val purchaseRepository: PurchaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: Int = checkNotNull(
        when (val raw = savedStateHandle.get<Any>("itemId")) {
            is Int -> raw
            is String -> raw.toIntOrNull()
            else -> null
        }
    ) { "itemId must be provided as an Int or numeric String in SavedStateHandle" }

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<ItemDetailUiEffect>()
    val effect: SharedFlow<ItemDetailUiEffect> = _effect.asSharedFlow()

    init {
        loadItem()
    }

    private fun loadItem() {
        viewModelScope.launch {
            combine(
                itemRepository.getItemById(itemId),
                purchaseRepository.getPurchasesByItemId(itemId)
            ) { item, purchases ->
                Pair(item, purchases)
            }.collect { (item, purchases) ->
                if (item == null) {
                    _uiState.update { it.copy(isLoading = false, itemNotFound = true) }
                    return@collect
                }
                val now = System.currentTimeMillis()
                val displayStatus = when (item.status) {
                    ItemStatus.COOLING_DOWN -> {
                        if (item.canDecide(now)) ItemDisplayStatus.ReadyForReview else ItemDisplayStatus.CoolingOff
                    }
                    ItemStatus.BOUGHT -> ItemDisplayStatus.Purchased
                    ItemStatus.PASSED -> ItemDisplayStatus.Skipped
                }

                val speechUiText = when (displayStatus) {
                    ItemDisplayStatus.CoolingOff -> UiText.StringResource(R.string.item_detail_speech_cooling_off)
                    ItemDisplayStatus.ReadyForReview -> UiText.StringResource(R.string.item_detail_speech_ready)
                    ItemDisplayStatus.Purchased -> UiText.StringResource(R.string.item_detail_speech_purchased)
                    ItemDisplayStatus.Skipped -> UiText.StringResource(R.string.item_detail_speech_skipped)
                    ItemDisplayStatus.Saved -> UiText.StringResource(R.string.item_detail_speech_saved)
                    ItemDisplayStatus.Considering -> UiText.StringResource(R.string.item_detail_speech_considering)
                    ItemDisplayStatus.Postponed -> UiText.StringResource(R.string.item_detail_speech_postponed)
                }

                val speechText = when (displayStatus) {
                    ItemDisplayStatus.CoolingOff -> "Bạn vẫn có quyền quyết định sau khi thời gian chờ kết thúc."
                    ItemDisplayStatus.ReadyForReview -> "Bạn còn muốn món này không? Hãy cùng xem lại nhu cầu nhé."
                    ItemDisplayStatus.Purchased -> "Lịch sử mua được lưu ở bên dưới."
                    ItemDisplayStatus.Skipped -> "Bạn đã tiết kiệm được một khoản tiền nhờ quyết định dừng lại."
                    ItemDisplayStatus.Saved -> "Cứ để ở đây, bạn có thể cân nhắc sau."
                    ItemDisplayStatus.Considering -> "Cùng suy nghĩ thêm một chút xem sao nhé."
                    ItemDisplayStatus.Postponed -> "Món đồ đang được tạm hoãn để đánh giá thêm."
                }

                val ctaUiText = when (displayStatus) {
                    ItemDisplayStatus.ReadyForReview -> UiText.StringResource(R.string.item_detail_cta_ready)
                    ItemDisplayStatus.CoolingOff -> UiText.StringResource(R.string.item_detail_cta_cooling_off)
                    ItemDisplayStatus.Purchased -> UiText.StringResource(R.string.item_detail_cta_purchased)
                    ItemDisplayStatus.Skipped -> UiText.StringResource(R.string.item_detail_cta_skipped)
                    else -> UiText.StringResource(R.string.item_detail_cta_default)
                }

                val ctaText = when (displayStatus) {
                    ItemDisplayStatus.ReadyForReview -> "Xem lại nhu cầu"
                    ItemDisplayStatus.CoolingOff -> "Đang trong thời gian chờ"
                    ItemDisplayStatus.Purchased -> "Xem lịch sử mua"
                    ItemDisplayStatus.Skipped -> "Xem lại hành trình"
                    else -> "Xem lại quyết định"
                }

                val remMillis = item.remainingMillis(now)
                val remainingUiText = if (remMillis > 0) formatRemainingTimeUiText(remMillis) else UiText.StringResource(R.string.item_status_cooldown_finished)
                val remainingText = if (remMillis > 0) formatRemainingTime(remMillis) else "Đã hết thời gian chờ"

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val addedDateStr = dateFormat.format(Date(item.addedTimestamp))
                val durationDays = TimeUnit.MILLISECONDS.toDays(item.cooldownDurationMillis)

                // Decision Journey events mẫu (từ dữ liệu thực của item)
                val journey = buildList {
                    add(
                        DecisionJourneyStepUi(
                            title = "Đã thêm vào danh sách",
                            description = "Bắt đầu thời gian hạ nhiệt ${durationDays} ngày.",
                            formattedTimestamp = addedDateStr,
                            titleUiText = UiText.StringResource(R.string.item_detail_journey_added_title),
                            descriptionUiText = UiText.StringResource(R.string.item_detail_journey_added_desc, durationDays)
                        )
                    )
                    if (item.status == ItemStatus.COOLING_DOWN && item.canDecide(now)) {
                        add(
                            DecisionJourneyStepUi(
                                title = "Đã hoàn thành thời gian chờ",
                                description = "Món đồ đã sẵn sàng để bạn đưa ra quyết định.",
                                formattedTimestamp = dateFormat.format(Date(item.addedTimestamp + item.cooldownDurationMillis)),
                                titleUiText = UiText.StringResource(R.string.item_detail_journey_cooling_finished_title),
                                descriptionUiText = UiText.StringResource(R.string.item_detail_journey_cooling_finished_desc)
                            )
                        )
                    }
                    if (item.status == ItemStatus.BOUGHT) {
                        add(
                            DecisionJourneyStepUi(
                                title = "Đã ghi nhận mua hàng",
                                description = "Giao dịch đã được lưu vào hệ thống.",
                                formattedTimestamp = dateFormat.format(Date(now)),
                                titleUiText = UiText.StringResource(R.string.item_detail_journey_bought_title),
                                descriptionUiText = UiText.StringResource(R.string.item_detail_journey_bought_desc)
                            )
                        )
                    } else if (item.status == ItemStatus.PASSED) {
                        add(
                            DecisionJourneyStepUi(
                                title = "Đã bỏ qua món đồ",
                                description = "Quyết định không mua món đồ này.",
                                formattedTimestamp = dateFormat.format(Date(now)),
                                titleUiText = UiText.StringResource(R.string.item_detail_journey_passed_title),
                                descriptionUiText = UiText.StringResource(R.string.item_detail_journey_passed_desc)
                            )
                        )
                    }
                }

                val mappedPurchases = purchases.map { p ->
                    PurchaseHistoryItemUi(
                        purchaseId = p.id,
                        formattedDate = dateFormat.format(Date(p.purchasedTimestamp)),
                        actualUnitPriceFormatted = HomeCurrencyFormatter.formatVnd(p.actualUnitPrice),
                        quantity = p.quantity,
                        totalAmountFormatted = HomeCurrencyFormatter.formatVnd(p.totalAmount),
                        paymentType = p.paymentType.name
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        itemNotFound = false,
                        id = item.id,
                        name = item.name,
                        formattedPrice = HomeCurrencyFormatter.formatVnd(item.price),
                        rawPrice = item.price,
                        imageUri = item.imageUri,
                        displayStatus = displayStatus,
                        reason = item.reason.ifBlank { "Không có lý do được ghi lại." },
                        formattedAddedDate = addedDateStr,
                        cooldownDurationFormatted = "$durationDays ngày",
                        remainingMillis = remMillis,
                        remainingFormatted = remainingText,
                        speechBubbleText = speechText,
                        primaryCtaText = ctaText,
                        decisionJourney = journey,
                        purchaseHistory = mappedPurchases,
                        speechBubbleUiText = speechUiText,
                        primaryCtaUiText = ctaUiText,
                        remainingFormattedUiText = remainingUiText,
                        cooldownDurationUiText = UiText.StringResource(R.string.item_detail_duration_days, durationDays)
                    )
                }
            }
        }
    }

    fun onEvent(event: ItemDetailUiEvent) {
        when (event) {
            is ItemDetailUiEvent.OpenMoreOptions -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = true) }
            }
            is ItemDetailUiEvent.DismissMoreOptions -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = false) }
            }
            is ItemDetailUiEvent.PrimaryCtaClicked -> {
                // Nhấn CTA chính theo ngữ cảnh
                when (_uiState.value.displayStatus) {
                    ItemDisplayStatus.ReadyForReview -> {
                        _uiState.update { it.copy(isRecordPurchaseSheetOpen = true) }
                    }
                    else -> {}
                }
            }
            is ItemDetailUiEvent.RequestSkipWaiting -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = false, isSkipWaitingConfirmDialogOpen = true) }
            }
            is ItemDetailUiEvent.ConfirmSkipWaiting -> {
                // GAP-02: Skip Waiting kết thúc phiên chờ và chuyển sang Ready để đưa ra quyết định
                _uiState.update { it.copy(isSkipWaitingConfirmDialogOpen = false) }
                viewModelScope.launch {
                    val current = _uiState.value
                    if (current.id > 0) {
                        _uiState.update {
                            it.copy(
                                remainingMillis = 0L,
                                remainingFormatted = "Đã kết thúc thời gian chờ",
                                remainingFormattedUiText = UiText.StringResource(R.string.item_status_cooldown_finished),
                                displayStatus = ItemDisplayStatus.ReadyForReview,
                                speechBubbleText = "Bạn đã kết thúc thời gian chờ. Giờ là lúc đưa ra quyết định.",
                                speechBubbleUiText = UiText.StringResource(R.string.item_detail_speech_decision_time)
                            )
                        }
                    }
                }
            }
            is ItemDetailUiEvent.DismissSkipWaitingDialog -> {
                _uiState.update { it.copy(isSkipWaitingConfirmDialogOpen = false) }
            }
            is ItemDetailUiEvent.RequestReFreeze -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = false, isReFreezeConfirmDialogOpen = true) }
            }
            is ItemDetailUiEvent.DismissReFreezeDialog -> {
                _uiState.update { it.copy(isReFreezeConfirmDialogOpen = false) }
            }
            is ItemDetailUiEvent.ConfirmReFreeze -> {
                _uiState.update { it.copy(isReFreezeConfirmDialogOpen = false) }
                viewModelScope.launch {
                    val current = _uiState.value
                    val now = System.currentTimeMillis()
                    val newDuration = FreezeDurationPolicy.calculateDurationMillis(current.rawPrice)
                    val updated = CooldownItem(
                        id = current.id,
                        name = current.name,
                        price = current.rawPrice,
                        imageUri = current.imageUri,
                        addedTimestamp = now,
                        cooldownDurationMillis = newDuration,
                        reason = current.reason,
                        status = ItemStatus.COOLING_DOWN
                    )
                    itemRepository.updateItem(updated)
                    _uiState.update {
                        it.copy(
                            isRefrozen = true,
                            displayStatus = ItemDisplayStatus.CoolingOff,
                            remainingMillis = newDuration,
                            remainingFormatted = FreezeCountdownCalculator.formatRemainingTimeVi(newDuration),
                            speechBubbleText = "Đã kích hoạt chu kỳ Freeze mới. Bạn sẽ không nhận thêm thưởng Freeze cho món đồ này.",
                            speechBubbleUiText = UiText.DynamicString("Đã kích hoạt chu kỳ Freeze mới. Bạn sẽ không nhận thêm thưởng Freeze cho món đồ này.")
                        )
                    }
                    _effect.emit(ItemDetailUiEffect.ShowToast(UiText.DynamicString("Đã kích hoạt chu kỳ Freeze mới")))
                }
            }
            is ItemDetailUiEvent.RequestPassItem -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = false) }
                viewModelScope.launch {
                    itemRepository.updateItemStatus(_uiState.value.id, ItemStatus.PASSED)
                    _effect.emit(ItemDetailUiEffect.ShowToast(UiText.DynamicString("Đã bỏ qua món đồ này")))
                    _effect.emit(ItemDetailUiEffect.NavigateBack)
                }
            }
            is ItemDetailUiEvent.RequestRecordPurchase -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = false, isRecordPurchaseSheetOpen = true) }
            }
            is ItemDetailUiEvent.DismissRecordPurchaseSheet -> {
                _uiState.update { it.copy(isRecordPurchaseSheetOpen = false) }
            }
            is ItemDetailUiEvent.ConfirmPurchase -> {
                _uiState.update { it.copy(isRecordPurchaseSheetOpen = false) }
                viewModelScope.launch {
                    purchaseRepository.recordPurchase(event.purchase)
                    _effect.emit(ItemDetailUiEffect.ShowToast(UiText.StringResource(R.string.purchase_record_success)))
                }
            }
            is ItemDetailUiEvent.RequestArchiveItem -> {
                _uiState.update { it.copy(isMoreOptionsSheetOpen = false) }
                viewModelScope.launch {
                    _effect.emit(ItemDetailUiEffect.ShowToast(UiText.StringResource(R.string.archived_items_restore_success)))
                    _effect.emit(ItemDetailUiEffect.NavigateBack)
                }
            }
            is ItemDetailUiEvent.PurchaseItemClicked -> {
                viewModelScope.launch {
                    _effect.emit(ItemDetailUiEffect.OpenTransactionDetail(event.purchaseId))
                }
            }
        }
    }

    private fun formatRemainingTimeUiText(remainingMillis: Long): UiText {
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

        return when {
            days > 0 -> UiText.StringResource(R.string.item_remaining_days_hours, days, hours)
            hours > 0 -> UiText.StringResource(R.string.item_remaining_hours_minutes, hours, minutes)
            minutes > 0 -> UiText.StringResource(R.string.item_remaining_minutes, minutes)
            else -> UiText.StringResource(R.string.item_remaining_less_than_one_minute)
        }
    }

    private fun formatRemainingTime(remainingMillis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

        return when {
            days > 0 -> "$days ngày $hours giờ"
            hours > 0 -> "$hours giờ $minutes phút"
            minutes > 0 -> "$minutes phút"
            else -> "Dưới 1 phút"
        }
    }
}
