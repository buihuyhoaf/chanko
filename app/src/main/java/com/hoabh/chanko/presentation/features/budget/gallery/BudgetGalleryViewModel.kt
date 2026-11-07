package com.hoabh.chanko.presentation.features.budget.gallery

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.budget.model.BudgetTemporalRole
import com.hoabh.chanko.domain.budget.model.MonthlyBudgetOverview
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class BudgetGalleryViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : BaseViewModel<BudgetGalleryUiState, BudgetGalleryUiEffect>(
    BudgetGalleryUiState(
        firstEstablishedMonth = budgetRepository.firstEstablishedMonth,
        selectedYear = 2026
    )
) {

    private var galleryJob: Job? = null

    init {
        observeGallery(currentState.selectedYear)
    }

    fun onEvent(event: BudgetGalleryUiEvent) {
        when (event) {
            is BudgetGalleryUiEvent.SelectYear -> {
                if (currentState.selectedYear != event.year) {
                    observeGallery(event.year)
                }
            }

            BudgetGalleryUiEvent.NextYear -> {
                if (currentState.canNavigateToNextYear) {
                    observeGallery(currentState.selectedYear + 1)
                }
            }

            BudgetGalleryUiEvent.PreviousYear -> {
                if (currentState.canNavigateToPreviousYear) {
                    observeGallery(currentState.selectedYear - 1)
                }
            }

            is BudgetGalleryUiEvent.MonthCardClick -> {
                handleMonthCardClick(event.overview)
            }

            is BudgetGalleryUiEvent.OpenFutureSheet -> {
                openFutureSheet(event.overview)
            }

            BudgetGalleryUiEvent.DismissFutureSheet -> {
                updateState {
                    it.copy(
                        activeFutureSheetMonth = null,
                        suggestedLimitForFuture = null
                    )
                }
            }

            is BudgetGalleryUiEvent.SaveFutureBudget -> {
                saveFutureBudget(event.yearMonth, event.limitMinor)
            }

            BudgetGalleryUiEvent.ShowAboutInfo -> {
                sendEffect(
                    BudgetGalleryUiEffect.ShowSnackbar("Chanko Budget v3: Theo dõi và lập kế hoạch ngân sách thông minh.")
                )
            }

            BudgetGalleryUiEvent.SyncData -> {
                val message = if (currentState.isOnline) {
                    "Đã đồng bộ dữ liệu ngân sách mới nhất."
                } else {
                    "Không thể đồng bộ khi ngoại tuyến."
                }
                sendEffect(BudgetGalleryUiEffect.ShowSnackbar(message))
            }

            BudgetGalleryUiEvent.Retry -> {
                observeGallery(currentState.selectedYear)
            }
        }
    }

    private fun handleMonthCardClick(overview: MonthlyBudgetOverview) {
        when (overview.temporalRole) {
            BudgetTemporalRole.Past,
            BudgetTemporalRole.Current -> {
                sendEffect(BudgetGalleryUiEffect.NavigateToDetail(overview.yearMonth))
            }
            BudgetTemporalRole.Future -> {
                openFutureSheet(overview)
            }
        }
    }

    private fun openFutureSheet(overview: MonthlyBudgetOverview) {
        val suggestedLimit = calculateSuggestedLimit(overview, currentState.months)
        updateState {
            it.copy(
                activeFutureSheetMonth = overview,
                suggestedLimitForFuture = suggestedLimit
            )
        }
    }

    private fun calculateSuggestedLimit(
        targetMonth: MonthlyBudgetOverview,
        months: List<MonthlyBudgetOverview>
    ): Long {
        val previousMonths = months.filter {
            val limit = it.limitMinor
            it.yearMonth < targetMonth.yearMonth && limit != null && limit > 0L
        }
        return previousMonths.lastOrNull()?.limitMinor ?: 6_000_000L
    }

    private fun observeGallery(year: Int) {
        updateState { it.copy(selectedYear = year, isLoading = true) }
        galleryJob?.cancel()
        galleryJob = viewModelScope.launch {
            budgetRepository.getYearGalleryOverview(year)
                .catch {
                    updateState { current ->
                        current.copy(
                            selectedYear = year,
                            firstEstablishedMonth = budgetRepository.firstEstablishedMonth,
                            currentClockMonth = YearMonth.of(2026, 11),
                            annualConfiguredTotalMinor = 0L,
                            configuredMonthsCount = 0,
                            months = emptyList(),
                            isOnline = false,
                            isLoading = false,
                            lastSyncedAt = null
                        )
                    }
                }
                .collect { overview ->
                    updateState { current ->
                        val suggestedLimit = current.activeFutureSheetMonth?.let { activeSheet ->
                            calculateSuggestedLimit(activeSheet, overview.months)
                        } ?: current.suggestedLimitForFuture

                        current.copy(
                            selectedYear = overview.selectedYear,
                            firstEstablishedMonth = overview.firstEstablishedMonth,
                            currentClockMonth = overview.currentClockMonth,
                            annualConfiguredTotalMinor = overview.annualConfiguredTotalMinor,
                            configuredMonthsCount = overview.configuredMonthsCount,
                            months = overview.months,
                            isOnline = overview.isOnline,
                            isLoading = false,
                            lastSyncedAt = overview.lastSyncedAt,
                            suggestedLimitForFuture = suggestedLimit
                        )
                    }
                }
        }
    }

    private fun saveFutureBudget(yearMonth: YearMonth, limitMinor: Long) {
        if (!currentState.isOnline) {
            sendEffect(BudgetGalleryUiEffect.ShowSnackbar("Không thể lưu khi ngoại tuyến."))
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isSavingFutureBudget = true) }
            val result = budgetRepository.setBudgetForMonth(yearMonth, limitMinor)
            updateState { it.copy(isSavingFutureBudget = false) }

            if (result.isSuccess) {
                updateState {
                    it.copy(
                        activeFutureSheetMonth = null,
                        suggestedLimitForFuture = null
                    )
                }
                sendEffect(
                    BudgetGalleryUiEffect.ShowSnackbar(
                        "Đã lưu ngân sách Tháng %02d/%d thành công.".format(yearMonth.monthValue, yearMonth.year)
                    )
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi khi lưu ngân sách."
                sendEffect(BudgetGalleryUiEffect.ShowSnackbar(errorMsg))
            }
        }
    }
}
