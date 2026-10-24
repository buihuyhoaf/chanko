package com.hoabh.chanko.presentation.screens.myitems

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.hoabh.chanko.ui.theme.CandyColors

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
    val labelText: String,
    val containerColor: Color,
    val contentColor: Color
) {
    Saved("Đã lưu", CandyColors.primarySoft, CandyColors.primaryInk),
    Considering("Đang cân nhắc", CandyColors.primarySoft, CandyColors.primaryInk),
    CoolingOff("Đang hạ nhiệt", CandyColors.blue.copy(alpha = 0.2f), Color(0xFF1B5A8A)),
    ReadyForReview("Đến lúc xem lại", CandyColors.readyChip, CandyColors.readyInk),
    Postponed("Đã hoãn", CandyColors.orange.copy(alpha = 0.2f), Color(0xFF8A4B10)),
    Purchased("Đã mua", CandyColors.purchased.copy(alpha = 0.2f), Color(0xFF2E4E8A)),
    Skipped("Đã bỏ qua", CandyColors.skipped.copy(alpha = 0.2f), Color(0xFF146E56))
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
