package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.presentation.screens.home.model.BudgetSummary
import com.hoabh.chanko.presentation.screens.home.model.CalendarDayState
import com.hoabh.chanko.presentation.screens.home.model.DailyPurchaseSummary
import com.hoabh.chanko.presentation.screens.home.model.MascotAdvice
import com.hoabh.chanko.presentation.screens.home.model.SpendingCycle
import java.time.LocalDate

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Error : HomeUiState

    data class Content(
        val userName: String = "Hoa",
        val cycle: SpendingCycle,
        val calendarDays: List<CalendarDayState>,
        val selectedDate: LocalDate,
        val selectedDateTransactions: DailyPurchaseSummary?,
        val budgetSummary: BudgetSummary,
        val mascotAdvice: MascotAdvice,
        val readyItems: List<CooldownItem>,
        val coolingItems: List<CooldownItem>,
        val today: LocalDate
    ) : HomeUiState
}

sealed interface HomeUiEvent {
    data class SelectDate(val date: LocalDate) : HomeUiEvent
    data object JumpToToday : HomeUiEvent
    data class ReviewItem(val itemId: Int) : HomeUiEvent
    data class OpenItem(val itemId: Int) : HomeUiEvent
    data object AddItem : HomeUiEvent
    data object SettingsClick : HomeUiEvent
    data object ProfileClick : HomeUiEvent
    data object Retry : HomeUiEvent
}
