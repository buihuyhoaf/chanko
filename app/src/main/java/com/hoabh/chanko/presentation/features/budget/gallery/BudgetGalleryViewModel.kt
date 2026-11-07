package com.hoabh.chanko.presentation.features.budget.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.budget.model.BudgetSetupState
import com.hoabh.chanko.domain.budget.model.BudgetTemporalRole
import com.hoabh.chanko.domain.budget.model.MonthlyBudgetOverview
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class BudgetGalleryViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val selectedYearState = MutableStateFlow(2026)
    private val activeFutureSheetState = MutableStateFlow<MonthlyBudgetOverview?>(null)
    private val isSavingFutureState = MutableStateFlow(false)

    private val effectChannel = Channel<BudgetGalleryUiEffect>(Channel.BUFFERED)
    val effect: Flow<BudgetGalleryUiEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<BudgetGalleryUiState> = selectedYearState
        .flatMapLatest { year ->
            budgetRepository.getYearGalleryOverview(year)
                .catch { e ->
                    emit(
                        com.hoabh.chanko.domain.budget.model.BudgetGalleryOverview(
                            selectedYear = year,
                            firstEstablishedMonth = budgetRepository.firstEstablishedMonth,
                            currentClockMonth = YearMonth.of(2026, 11),
                            annualConfiguredTotalMinor = 0L,
                            configuredMonthsCount = 0,
                            months = emptyList(),
                            isOnline = false,
                            lastSyncedAt = null
                        )
                    )
                }
        }
        .combine(activeFutureSheetState) { overview, activeSheetMonth ->
            overview to activeSheetMonth
        }
        .combine(isSavingFutureState) { (overview, activeSheetMonth), isSaving ->
            val suggestedLimit = if (activeSheetMonth != null) {
                val previousMonths = overview.months
                    .filter {
                        val limit = it.limitMinor
                        it.yearMonth < activeSheetMonth.yearMonth && limit != null && limit > 0L
                    }
                previousMonths.lastOrNull()?.limitMinor ?: 6_000_000L
            } else null

            BudgetGalleryUiState(
                selectedYear = overview.selectedYear,
                firstEstablishedMonth = overview.firstEstablishedMonth,
                currentClockMonth = overview.currentClockMonth,
                annualConfiguredTotalMinor = overview.annualConfiguredTotalMinor,
                configuredMonthsCount = overview.configuredMonthsCount,
                months = overview.months,
                isOnline = overview.isOnline,
                isLoading = false,
                lastSyncedAt = overview.lastSyncedAt,
                activeFutureSheetMonth = activeSheetMonth,
                suggestedLimitForFuture = suggestedLimit,
                isSavingFutureBudget = isSaving
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetGalleryUiState()
        )

    fun onEvent(event: BudgetGalleryUiEvent) {
        when (event) {
            is BudgetGalleryUiEvent.SelectYear -> {
                selectedYearState.value = event.year
            }

            BudgetGalleryUiEvent.NextYear -> {
                val current = selectedYearState.value
                val state = uiState.value
                if (state.canNavigateToNextYear) {
                    selectedYearState.value = current + 1
                }
            }

            BudgetGalleryUiEvent.PreviousYear -> {
                val current = selectedYearState.value
                val state = uiState.value
                if (state.canNavigateToPreviousYear) {
                    selectedYearState.value = current - 1
                }
            }

            is BudgetGalleryUiEvent.MonthCardClick -> {
                handleMonthCardClick(event.overview)
            }

            is BudgetGalleryUiEvent.OpenFutureSheet -> {
                openFutureSheet(event.overview)
            }

            BudgetGalleryUiEvent.DismissFutureSheet -> {
                activeFutureSheetState.value = null
            }

            is BudgetGalleryUiEvent.SaveFutureBudget -> {
                saveFutureBudget(event.yearMonth, event.limitMinor)
            }

            BudgetGalleryUiEvent.ShowAboutInfo -> {
                viewModelScope.launch {
                    effectChannel.send(
                        BudgetGalleryUiEffect.ShowSnackbar("Chanko Budget v3: Theo dõi và lập kế hoạch ngân sách thông minh.")
                    )
                }
            }

            BudgetGalleryUiEvent.SyncData -> {
                val currentYear = selectedYearState.value
                selectedYearState.value = currentYear
                viewModelScope.launch {
                    val message = if (uiState.value.isOnline) {
                        "Đã đồng bộ dữ liệu ngân sách mới nhất."
                    } else {
                        "Không thể đồng bộ khi ngoại tuyến."
                    }
                    effectChannel.send(BudgetGalleryUiEffect.ShowSnackbar(message))
                }
            }

            BudgetGalleryUiEvent.Retry -> {
                val year = selectedYearState.value
                selectedYearState.value = year
            }
        }
    }

    private fun handleMonthCardClick(overview: MonthlyBudgetOverview) {
        when (overview.temporalRole) {
            BudgetTemporalRole.Past,
            BudgetTemporalRole.Current -> {
                viewModelScope.launch {
                    effectChannel.send(BudgetGalleryUiEffect.NavigateToDetail(overview.yearMonth))
                }
            }
            BudgetTemporalRole.Future -> {
                openFutureSheet(overview)
            }
        }
    }

    private fun openFutureSheet(overview: MonthlyBudgetOverview) {
        activeFutureSheetState.value = overview
    }

    private fun saveFutureBudget(yearMonth: YearMonth, limitMinor: Long) {
        val state = uiState.value
        if (!state.isOnline) {
            viewModelScope.launch {
                effectChannel.send(BudgetGalleryUiEffect.ShowSnackbar("Không thể lưu khi ngoại tuyến."))
            }
            return
        }

        viewModelScope.launch {
            isSavingFutureState.value = true
            val result = budgetRepository.setBudgetForMonth(yearMonth, limitMinor)
            isSavingFutureState.value = false

            if (result.isSuccess) {
                activeFutureSheetState.value = null
                effectChannel.send(
                    BudgetGalleryUiEffect.ShowSnackbar("Đã lưu ngân sách Tháng %02d/%d thành công.".format(yearMonth.monthValue, yearMonth.year))
                )
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi khi lưu ngân sách."
                effectChannel.send(BudgetGalleryUiEffect.ShowSnackbar(errorMsg))
            }
        }
    }
}
