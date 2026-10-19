package com.hoabh.chanko.presentation.screens.itemdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.hoabh.chanko.presentation.ActionState

sealed interface ItemUiState {
    data object Loading : ItemUiState
    data object Error : ItemUiState
    data class Content(val item: CooldownItem?) : ItemUiState
}

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val repository: ItemRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    private val itemId: Int = checkNotNull(savedState["itemId"])
    val item = repository.getItemById(itemId).map<CooldownItem?, ItemUiState> { ItemUiState.Content(it) }
        .catch { emit(ItemUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemUiState.Loading)
    val now = flow { while (true) { emit(System.currentTimeMillis()); delay(1_000) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), System.currentTimeMillis())
    private val _action = MutableStateFlow(ActionState.IDLE)
    val action = _action.asStateFlow()
    private fun perform(block: suspend () -> Unit) {
        if (_action.value == ActionState.RUNNING || _action.value == ActionState.DONE) return
        _action.value = ActionState.RUNNING
        viewModelScope.launch {
            try { block(); _action.value = ActionState.DONE }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _action.value = ActionState.ERROR }
        }
    }
    fun decide(status: ItemStatus) = perform { repository.updateItemStatus(itemId, status) }
}
