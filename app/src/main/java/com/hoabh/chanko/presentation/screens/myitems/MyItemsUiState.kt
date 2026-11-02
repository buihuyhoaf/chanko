package com.hoabh.chanko.presentation.screens.myitems

import androidx.compose.runtime.Immutable

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
    val labelText: String
) {
    Saved("Đã lưu"),
    Considering("Đang cân nhắc"),
    CoolingOff("Đang hạ nhiệt"),
    ReadyForReview("Đến lúc xem lại"),
    Postponed("Đã hoãn"),
    Purchased("Đã mua"),
    Skipped("Đã bỏ qua")
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
    val addedTimestamp: Long
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
