package com.hoabh.chanko.presentation.screens.myitems

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.base.UiText

/**
 * Filter cấp 1 theo ITEM-04:
 * All / Active / Purchased / Skipped
 */
enum class PrimaryFilter {
    ALL,
    ACTIVE,
    PURCHASED,
    SKIPPED
}

/**
 * Filter cấp 2 trong Active theo ITEM-04:
 * Saved / Considering / Cooling Off / Ready / Postponed (hoặc ALL_ACTIVE)
 */
enum class ActiveSubFilter {
    ALL_ACTIVE,
    SAVED,
    CONSIDERING,
    COOLING_OFF,
    READY,
    POSTPONED
}

/**
 * Tiêu chí sắp xếp (ITEM-04 / Reference)
 */
enum class ItemSortOption {
    UPDATED_RECENTLY,
    NEWEST_ADDED,
    PRICE_ASC,
    PRICE_DESC,
    NAME_AZ
}

/**
 * Trạng thái hiển thị dẫn xuất (Derived Display Status) cho từng thẻ Item
 */
enum class ItemDisplayStatus(
    @get:StringRes val labelRes: Int,
    val labelText: String = ""
) {
    Saved(R.string.item_status_saved, "Đã lưu"),
    Considering(R.string.item_status_considering, "Đang cân nhắc"),
    CoolingOff(R.string.item_status_cooling_off, "Đang hạ nhiệt"),
    ReadyForReview(R.string.item_status_ready_for_review, "Đến lúc xem lại"),
    Postponed(R.string.item_status_postponed, "Đã hoãn"),
    Purchased(R.string.item_status_purchased, "Đã mua"),
    Skipped(R.string.item_status_skipped, "Đã bỏ qua")
}

@Immutable
data class MyItemCardUi(
    val id: Int,
    val title: String,
    val formattedPrice: String,
    val rawPrice: Long,
    val imageUri: String? = null,
    val displayStatus: ItemDisplayStatus,
    val secondaryInfoText: String, // Ví dụ: "Còn 2 ngày" hoặc "Đã thêm 3 ngày trước"
    val addedTimestamp: Long,
    val secondaryInfoUiText: UiText = UiText.DynamicString(secondaryInfoText)
)

@Immutable
data class MyItemsUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val primaryFilter: PrimaryFilter = PrimaryFilter.ALL,
    val activeSubFilter: ActiveSubFilter = ActiveSubFilter.ALL_ACTIVE,
    val sortOption: ItemSortOption = ItemSortOption.UPDATED_RECENTLY,
    val totalCount: Int = 0,
    val coolingOffCount: Int = 0,
    val readyCount: Int = 0,
    val items: List<MyItemCardUi> = emptyList(),
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null
)
