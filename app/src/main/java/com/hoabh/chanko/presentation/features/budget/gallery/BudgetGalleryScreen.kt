package com.hoabh.chanko.presentation.features.budget.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.presentation.features.budget.gallery.components.AnnualSummarySection
import com.hoabh.chanko.presentation.features.budget.gallery.components.BudgetGalleryHeader
import com.hoabh.chanko.presentation.features.budget.gallery.components.BudgetYearSelector
import com.hoabh.chanko.presentation.features.budget.gallery.components.MonthlyBudgetCompactCard
import com.hoabh.chanko.presentation.features.budget.sheets.FutureBudgetSheet
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * Màn hình Budget Gallery v3:
 * - Lưới 2 cột (LazyVerticalGrid 2 cột), cuộn dọc
 * - Header Gallery với mascot mèo đếm tiền và Dropdown menu (BudgetGalleryHeader)
 * - Tóm tắt tổng ngân sách năm (AnnualSummarySection)
 * - Header chọn năm (BudgetYearSelector) đặt dưới AnnualSummarySection
 * - Thẻ tháng nhỏ (MonthlyBudgetCompactCard)
 * - Sheet thiết lập/sửa tương lai (FutureBudgetSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetGalleryScreen(
    state: BudgetGalleryUiState,
    onEvent: (BudgetGalleryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CandyColors.brandPurple)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = lazyGridState,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item 1: Gallery Header with Mascot & Options Menu (span 2 cột)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BudgetGalleryHeader(
                        onAboutClick = { onEvent(BudgetGalleryUiEvent.ShowAboutInfo) },
                        onSyncClick = { onEvent(BudgetGalleryUiEvent.SyncData) }
                    )
                }

                // Item 2: Annual Summary Card (span 2 cột)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AnnualSummarySection(
                        annualTotalMinor = state.annualConfiguredTotalMinor,
                        configuredMonthsCount = state.configuredMonthsCount,
                        totalMonthsInYear = state.months.size,
                        isOnline = state.isOnline,
                        lastSyncedAt = state.lastSyncedAt
                    )
                }

                // Item 3: Year Selector đặt dưới Summary (span 2 cột)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BudgetYearSelector(
                        selectedYear = state.selectedYear,
                        canNavigatePrevious = state.canNavigateToPreviousYear,
                        canNavigateNext = state.canNavigateToNextYear,
                        onPreviousYear = { onEvent(BudgetGalleryUiEvent.PreviousYear) },
                        onNextYear = { onEvent(BudgetGalleryUiEvent.NextYear) }
                    )
                }

                // Grid Items: Monthly Budget Compact Cards
                items(
                    items = state.months,
                    key = { it.yearMonth.toString() }
                ) { monthOverview ->
                    MonthlyBudgetCompactCard(
                        overview = monthOverview,
                        onClick = { onEvent(BudgetGalleryUiEvent.MonthCardClick(monthOverview)) }
                    )
                }
            }
        }

        // Future Setup / Edit Bottom Sheet
        if (state.activeFutureSheetMonth != null) {
            val activeMonth = state.activeFutureSheetMonth
            FutureBudgetSheet(
                yearMonth = activeMonth.yearMonth,
                initialLimit = activeMonth.limitMinor,
                suggestedLimit = state.suggestedLimitForFuture,
                isOnline = state.isOnline,
                isSaving = state.isSavingFutureBudget,
                onSave = { limit ->
                    onEvent(BudgetGalleryUiEvent.SaveFutureBudget(activeMonth.yearMonth, limit))
                },
                onDismiss = {
                    onEvent(BudgetGalleryUiEvent.DismissFutureSheet)
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
