package com.hoabh.chanko.presentation.screens.myitems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.screens.home.domain.HomeCurrencyFormatter
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MyItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyItemsUiState(isLoading = true))
    val uiState: StateFlow<MyItemsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MyItemsUiEffect>()
    val effect: SharedFlow<MyItemsUiEffect> = _effect.asSharedFlow()

    // Query & Filter states
    private val _searchQuery = MutableStateFlow("")
    private val _primaryFilter = MutableStateFlow(PrimaryFilter.ALL)
    private val _activeSubFilter = MutableStateFlow(ActiveSubFilter.ALL_ACTIVE)
    private val _sortOption = MutableStateFlow(ItemSortOption.UPDATED_RECENTLY)

    init {
        observeItems()
    }

    private fun observeItems() {
        viewModelScope.launch {
            val itemsFlow = combine(
                itemRepository.getActiveItems(),
                itemRepository.getItemsByStatus(ItemStatus.BOUGHT),
                itemRepository.getItemsByStatus(ItemStatus.PASSED)
            ) { active, bought, passed ->
                Triple(active, bought, passed)
            }

            combine(
                itemsFlow,
                _searchQuery,
                _primaryFilter,
                _activeSubFilter,
                _sortOption
            ) { (active, bought, passed), query, primary, subFilter, sort ->
                val now = System.currentTimeMillis()
                val allRawItems = active + bought + passed

                // Tile counts
                val totalCount = allRawItems.size
                val coolingCount = active.count { !it.canDecide(now) }
                val readyCount = active.count { it.canDecide(now) }

                // Map to UI models
                val mappedUiItems: List<MyItemCardUi> = allRawItems.map { it.toUiModel(now) }

                // 1. Filter by Search Query
                val queryFiltered: List<MyItemCardUi> = if (query.isBlank()) {
                    mappedUiItems
                } else {
                    val trimmed = query.trim().lowercase()
                    mappedUiItems.filter { it.title.lowercase().contains(trimmed) }
                }

                // 2. Filter by Primary & Sub filter
                val statusFiltered: List<MyItemCardUi> = queryFiltered.filter { item: MyItemCardUi ->
                    when (primary) {
                        PrimaryFilter.ALL -> true
                        PrimaryFilter.PURCHASED -> item.displayStatus == ItemDisplayStatus.Purchased
                        PrimaryFilter.SKIPPED -> item.displayStatus == ItemDisplayStatus.Skipped
                        PrimaryFilter.ACTIVE -> {
                            when (subFilter) {
                                ActiveSubFilter.ALL_ACTIVE -> item.displayStatus in listOf(
                                    ItemDisplayStatus.Saved,
                                    ItemDisplayStatus.Considering,
                                    ItemDisplayStatus.CoolingOff,
                                    ItemDisplayStatus.ReadyForReview,
                                    ItemDisplayStatus.Postponed
                                )
                                ActiveSubFilter.SAVED -> item.displayStatus == ItemDisplayStatus.Saved
                                ActiveSubFilter.CONSIDERING -> item.displayStatus == ItemDisplayStatus.Considering
                                ActiveSubFilter.COOLING_OFF -> item.displayStatus == ItemDisplayStatus.CoolingOff
                                ActiveSubFilter.READY -> item.displayStatus == ItemDisplayStatus.ReadyForReview
                                ActiveSubFilter.POSTPONED -> item.displayStatus == ItemDisplayStatus.Postponed
                            }
                        }
                    }
                }

                // 3. Sort
                val sorted: List<MyItemCardUi> = when (sort) {
                    ItemSortOption.UPDATED_RECENTLY,
                    ItemSortOption.NEWEST_ADDED -> statusFiltered.sortedByDescending { it.addedTimestamp }
                    ItemSortOption.PRICE_ASC -> statusFiltered.sortedBy { it.rawPrice }
                    ItemSortOption.PRICE_DESC -> statusFiltered.sortedByDescending { it.rawPrice }
                    ItemSortOption.NAME_AZ -> statusFiltered.sortedBy { it.title.lowercase() }
                }

                MyItemsUiState(
                    isLoading = false,
                    searchQuery = query,
                    primaryFilter = primary,
                    activeSubFilter = subFilter,
                    sortOption = sort,
                    totalCount = totalCount,
                    coolingOffCount = coolingCount,
                    readyCount = readyCount,
                    items = sorted,
                    isFilterSheetVisible = _uiState.value.isFilterSheetVisible
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: MyItemsUiEvent) {
        when (event) {
            is MyItemsUiEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is MyItemsUiEvent.PrimaryFilterSelected -> {
                _primaryFilter.value = event.filter
                if (event.filter != PrimaryFilter.ACTIVE) {
                    _activeSubFilter.value = ActiveSubFilter.ALL_ACTIVE
                }
            }
            is MyItemsUiEvent.ActiveSubFilterSelected -> {
                _primaryFilter.value = PrimaryFilter.ACTIVE
                _activeSubFilter.value = event.subFilter
            }
            is MyItemsUiEvent.SortOptionSelected -> {
                _sortOption.value = event.sortOption
            }
            is MyItemsUiEvent.OverviewTileClicked -> {
                when (event.tileType) {
                    OverviewTileType.ALL_ITEMS -> {
                        _primaryFilter.value = PrimaryFilter.ALL
                        _activeSubFilter.value = ActiveSubFilter.ALL_ACTIVE
                    }
                    OverviewTileType.COOLING_OFF -> {
                        _primaryFilter.value = PrimaryFilter.ACTIVE
                        _activeSubFilter.value = ActiveSubFilter.COOLING_OFF
                    }
                    OverviewTileType.READY -> {
                        _primaryFilter.value = PrimaryFilter.ACTIVE
                        _activeSubFilter.value = ActiveSubFilter.READY
                    }
                }
            }
            is MyItemsUiEvent.OpenFilterSheet -> {
                _uiState.update { it.copy(isFilterSheetVisible = true) }
            }
            is MyItemsUiEvent.DismissFilterSheet -> {
                _uiState.update { it.copy(isFilterSheetVisible = false) }
            }
            is MyItemsUiEvent.ResetFilters -> {
                _primaryFilter.value = PrimaryFilter.ALL
                _activeSubFilter.value = ActiveSubFilter.ALL_ACTIVE
                _sortOption.value = ItemSortOption.UPDATED_RECENTLY
            }
            is MyItemsUiEvent.ClearSearchAndFilters -> {
                _searchQuery.value = ""
                _primaryFilter.value = PrimaryFilter.ALL
                _activeSubFilter.value = ActiveSubFilter.ALL_ACTIVE
                _sortOption.value = ItemSortOption.UPDATED_RECENTLY
            }
            is MyItemsUiEvent.ItemClicked -> {
                viewModelScope.launch {
                    _effect.emit(MyItemsUiEffect.NavigateToDetail(event.itemId))
                }
            }
            is MyItemsUiEvent.ArchivedMenuClicked -> {
                viewModelScope.launch {
                    _effect.emit(MyItemsUiEffect.NavigateToArchived)
                }
            }
        }
    }

    private fun CooldownItem.toUiModel(now: Long): MyItemCardUi {
        val displayStatus = when (status) {
            ItemStatus.COOLING_DOWN -> {
                if (canDecide(now)) ItemDisplayStatus.ReadyForReview else ItemDisplayStatus.CoolingOff
            }
            ItemStatus.BOUGHT -> ItemDisplayStatus.Purchased
            ItemStatus.PASSED -> ItemDisplayStatus.Skipped
        }

        val secondaryInfo = when (displayStatus) {
            ItemDisplayStatus.CoolingOff -> {
                val remMillis = remainingMillis(now)
                formatRemainingTime(remMillis)
            }
            ItemDisplayStatus.ReadyForReview -> "Đã hết thời gian chờ"
            ItemDisplayStatus.Purchased -> "Đã quyết định mua"
            ItemDisplayStatus.Skipped -> "Đã bỏ qua món đồ này"
            else -> ""
        }

        return MyItemCardUi(
            id = id,
            title = name,
            formattedPrice = HomeCurrencyFormatter.formatVnd(price),
            rawPrice = price,
            imageUri = imageUri,
            displayStatus = displayStatus,
            secondaryInfoText = secondaryInfo,
            addedTimestamp = addedTimestamp
        )
    }

    private fun formatRemainingTime(remainingMillis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60

        return when {
            days > 0 -> "Còn $days ngày $hours giờ"
            hours > 0 -> "Còn $hours giờ $minutes phút"
            minutes > 0 -> "Còn $minutes phút"
            else -> "Còn dưới 1 phút"
        }
    }
}
