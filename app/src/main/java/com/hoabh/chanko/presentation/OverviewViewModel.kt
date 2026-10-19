package com.hoabh.chanko.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data object Error : OverviewUiState
    data class Content(val active: List<CooldownItem>, val passed: List<CooldownItem>, val bought: List<CooldownItem>, val saved: Long) : OverviewUiState
}

@HiltViewModel
class OverviewViewModel @Inject constructor(repository: ItemRepository) : ViewModel() {
    val items = combine(repository.getActiveItems(), repository.getItemsByStatus(ItemStatus.PASSED),
        repository.getItemsByStatus(ItemStatus.BOUGHT), repository.getTotalSavedAmount()) { active, passed, bought, saved ->
        OverviewUiState.Content(active, passed, bought, saved) as OverviewUiState
    }.catch { emit(OverviewUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewUiState.Loading)

}
