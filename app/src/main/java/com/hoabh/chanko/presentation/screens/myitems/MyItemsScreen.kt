package com.hoabh.chanko.presentation.screens.myitems

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.myitems.components.FilterSortBottomSheet
import com.hoabh.chanko.presentation.screens.myitems.components.MyItemCard
import com.hoabh.chanko.presentation.screens.myitems.components.MyItemsEmptyState
import com.hoabh.chanko.presentation.screens.myitems.components.MyItemsHero
import com.hoabh.chanko.presentation.screens.myitems.components.MyItemsOverviewTiles
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun MyItemsRoute(
    viewModel: MyItemsViewModel = hiltViewModel(),
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigateToArchived: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is MyItemsUiEffect.NavigateToDetail -> onNavigateToDetail(effect.itemId)
            is MyItemsUiEffect.NavigateToArchived -> onNavigateToArchived()
        }
    }

    MyItemsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

/**
 * ITEM-01..07: Màn hình MyItems hoàn chỉnh theo Approved Recovery Baseline:
 * 1. App bar My Items + Menu (Archived Items nằm trong menu; không thêm FAB/Add trùng lặp)
 * 2. Compact 3D Chanko Hero
 * 3. Ba overview tiles (All Items, Cooling Off, Ready) nhấn để lọc
 * 4. Search bar + 2 cấp filter + Sort
 * 5. Danh sách card ngang, thumbnail bên trái, compact read-only, nhấn mở chi tiết
 * 6. Empty State với Chanko + hộp kẹo (hoặc Clear Filters)
 * 7. Giữ 4 tabs navigation & nút + toàn cục
 */
@Composable
fun MyItemsScreen(
    uiState: MyItemsUiState,
    onEvent: (MyItemsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.my_items_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = CandyColors.textPrimary
                )

                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.my_items_archived_menu),
                            tint = CandyColors.textPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.my_items_archived_menu)) },
                            onClick = {
                                isMenuExpanded = false
                                onEvent(MyItemsUiEvent.ArchivedMenuClicked)
                            }
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = CandySpace.lg,
                    end = CandySpace.lg,
                    top = CandySpace.xs,
                    bottom = 96.dp // Để không bị BottomBar che khuất
                ),
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // 2. Compact 3D Chanko Hero
                item(key = "hero") {
                    MyItemsHero()
                }

                // 3. Overview Tiles (All / Cooling Off / Ready)
                item(key = "overview_tiles") {
                    MyItemsOverviewTiles(
                        totalCount = uiState.totalCount,
                        coolingOffCount = uiState.coolingOffCount,
                        readyCount = uiState.readyCount,
                        selectedPrimary = uiState.primaryFilter,
                        selectedSubFilter = uiState.activeSubFilter,
                        onTileClick = { tileType ->
                            onEvent(MyItemsUiEvent.OverviewTileClicked(tileType))
                        }
                    )
                }

                // 4. Search Bar & Filter Button
                item(key = "search_and_filter") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(MyItemsUiEvent.SearchQueryChanged(it)) },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.my_items_search_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CandyColors.textSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = CandyColors.textSecondary
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = CandyColors.primary,
                                unfocusedBorderColor = CandyColors.divider
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(CandySpace.sm))

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onEvent(MyItemsUiEvent.OpenFilterSheet) },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Box(
                                modifier = Modifier.padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterList,
                                    contentDescription = stringResource(R.string.my_items_filter_button),
                                    tint = if (uiState.primaryFilter != PrimaryFilter.ALL) CandyColors.primary else CandyColors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Header đếm số lượng kết quả
                item(key = "result_count") {
                    Text(
                        text = stringResource(R.string.my_items_results_count, uiState.items.size),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CandyColors.textSecondary
                    )
                }

                // 5 & 6. Danh sách item cards hoặc Empty State
                if (uiState.isLoading) {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CandyColors.primary)
                        }
                    }
                } else if (uiState.items.isEmpty()) {
                    item(key = "empty") {
                        val isFiltered = uiState.searchQuery.isNotBlank() ||
                                uiState.primaryFilter != PrimaryFilter.ALL ||
                                uiState.activeSubFilter != ActiveSubFilter.ALL_ACTIVE

                        MyItemsEmptyState(
                            isFiltered = isFiltered,
                            onClearFilters = {
                                onEvent(MyItemsUiEvent.ClearSearchAndFilters)
                            }
                        )
                    }
                } else {
                    items(
                        items = uiState.items,
                        key = { it.id }
                    ) { item ->
                        MyItemCard(
                            item = item,
                            onClick = { onEvent(MyItemsUiEvent.ItemClicked(item.id)) }
                        )
                    }
                }
            }
        }

        // Bottom Sheet Lọc & Sắp xếp
        if (uiState.isFilterSheetVisible) {
            FilterSortBottomSheet(
                currentPrimary = uiState.primaryFilter,
                currentSubFilter = uiState.activeSubFilter,
                currentSort = uiState.sortOption,
                onApply = { primary, subFilter, sort ->
                    onEvent(MyItemsUiEvent.PrimaryFilterSelected(primary))
                    onEvent(MyItemsUiEvent.ActiveSubFilterSelected(subFilter))
                    onEvent(MyItemsUiEvent.SortOptionSelected(sort))
                    onEvent(MyItemsUiEvent.DismissFilterSheet)
                },
                onReset = {
                    onEvent(MyItemsUiEvent.ResetFilters)
                    onEvent(MyItemsUiEvent.DismissFilterSheet)
                },
                onDismiss = {
                    onEvent(MyItemsUiEvent.DismissFilterSheet)
                }
            )
        }
    }
}
