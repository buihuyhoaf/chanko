package com.hoabh.chanko.presentation.features.budget.gallery

import com.hoabh.chanko.domain.budget.model.MonthlyBudgetOverview
import java.time.YearMonth

/**
 * Trạng thái UI cho màn hình Budget Gallery v3 (Lưới 2 cột theo năm).
 */
data class BudgetGalleryUiState(
    val selectedYear: Int = 2026,
    val firstEstablishedMonth: YearMonth = YearMonth.of(2026, 9),
    val currentClockMonth: YearMonth = YearMonth.of(2026, 11),
    val annualConfiguredTotalMinor: Long = 0L,
    val configuredMonthsCount: Int = 0,
    val months: List<MonthlyBudgetOverview> = emptyList(),
    val isOnline: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastSyncedAt: Long? = null,
    // Sheet thiết lập hoặc sửa cho tháng tương lai
    val activeFutureSheetMonth: MonthlyBudgetOverview? = null,
    val suggestedLimitForFuture: Long? = null,
    val isSavingFutureBudget: Boolean = false
) {
    val canNavigateToPreviousYear: Boolean
        get() = selectedYear > firstEstablishedMonth.year

    val canNavigateToNextYear: Boolean
        get() = selectedYear < currentClockMonth.year + 2
}

sealed interface BudgetGalleryUiEvent {
    data class SelectYear(val year: Int) : BudgetGalleryUiEvent
    data object NextYear : BudgetGalleryUiEvent
    data object PreviousYear : BudgetGalleryUiEvent
    data class MonthCardClick(val overview: MonthlyBudgetOverview) : BudgetGalleryUiEvent
    data class OpenFutureSheet(val overview: MonthlyBudgetOverview) : BudgetGalleryUiEvent
    data object DismissFutureSheet : BudgetGalleryUiEvent
    data class SaveFutureBudget(val yearMonth: YearMonth, val limitMinor: Long) : BudgetGalleryUiEvent
    data object ShowAboutInfo : BudgetGalleryUiEvent
    data object SyncData : BudgetGalleryUiEvent
    data object Retry : BudgetGalleryUiEvent
}

sealed interface BudgetGalleryUiEffect {
    data class NavigateToDetail(val yearMonth: YearMonth) : BudgetGalleryUiEffect
    data class ShowSnackbar(val message: String) : BudgetGalleryUiEffect
}
