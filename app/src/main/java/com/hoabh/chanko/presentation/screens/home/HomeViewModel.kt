package com.hoabh.chanko.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.screens.home.data.HomeSpendingRepository
import com.hoabh.chanko.presentation.screens.home.model.CalendarDayState
import com.hoabh.chanko.presentation.screens.home.model.MascotAdvice
import com.hoabh.chanko.presentation.screens.home.model.SpendingCycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val spendingRepository: HomeSpendingRepository
) : ViewModel() {

    var todayProvider: () -> LocalDate = { LocalDate.now() }
    var nowMillisProvider: () -> Long = { System.currentTimeMillis() }
    var anchorDay: Int = 15

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        itemRepository.getActiveItems(),
        _selectedDate
    ) { activeItems, selected ->
        val today = todayProvider()
        val effectiveSelected = selected ?: today
        val nowMillis = nowMillisProvider()

        val readyItems = activeItems.filter { it.canDecide(nowMillis) }
        val coolingItems = activeItems.filter { !it.canDecide(nowMillis) && it.remainingMillis(nowMillis) > 0 }

        val cycle = SpendingCycle.create(anchorDay = anchorDay, referenceDate = today)

        spendingRepository.getDailyPurchases(cycle).combine(
            spendingRepository.getBudgetSummary(cycle)
        ) { dailyPurchases, budgetSummary ->
            val calendarDays = cycle.dates.map { date ->
                val purchase = dailyPurchases[date]
                CalendarDayState(
                    date = date,
                    isCycleStart = (date == cycle.startDate),
                    isToday = (date == today),
                    isSelected = (date == effectiveSelected),
                    purchaseCount = purchase?.purchaseCount ?: 0,
                    totalSpent = purchase?.totalAmount ?: 0L
                )
            }

            val mascotAdvice = when {
                budgetSummary.progressPercent >= 100 -> MascotAdvice.OverBudget
                budgetSummary.progressPercent >= 75 -> MascotAdvice.Warning(budgetSummary.progressPercent)
                else -> MascotAdvice.Safe
            }

            HomeUiState.Content(
                userName = "Hoa",
                cycle = cycle,
                calendarDays = calendarDays,
                selectedDate = effectiveSelected,
                selectedDateTransactions = dailyPurchases[effectiveSelected],
                budgetSummary = budgetSummary,
                mascotAdvice = mascotAdvice,
                readyItems = readyItems,
                coolingItems = coolingItems,
                today = today
            ) as HomeUiState
        }
    }.flatMapLatest { it }
        .catch { emit(HomeUiState.Error) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeUiState.Loading
        )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SelectDate -> {
                _selectedDate.value = event.date
            }
            HomeUiEvent.JumpToToday -> {
                _selectedDate.value = todayProvider()
            }
            is HomeUiEvent.ReviewItem,
            is HomeUiEvent.OpenItem,
            HomeUiEvent.AddItem,
            HomeUiEvent.SettingsClick,
            HomeUiEvent.ProfileClick -> {
                // Delegated to outer screen lambda navigation callbacks
            }
            HomeUiEvent.Retry -> {
                _selectedDate.value = _selectedDate.value
            }
        }
    }
}
