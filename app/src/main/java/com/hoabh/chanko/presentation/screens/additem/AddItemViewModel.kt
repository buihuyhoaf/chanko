package com.hoabh.chanko.presentation.screens.additem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.hoabh.chanko.presentation.ActionState

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val _action = MutableStateFlow(ActionState.IDLE)
    val action = _action.asStateFlow()
    fun field(key: String) = savedState.getStateFlow(key, "")
    fun setField(key: String, value: String) { savedState[key] = value }
    private fun perform(block: suspend () -> Unit) {
        if (_action.value == ActionState.RUNNING || _action.value == ActionState.DONE) return
        _action.value = ActionState.RUNNING
        viewModelScope.launch {
            try { block(); _action.value = ActionState.DONE }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _action.value = ActionState.ERROR }
        }
    }
    fun add(name: String, price: Long, hours: Long, reason: String) = perform {
        require(name.isNotBlank() && price > 0 && hours in 1..8760)
        repository.insertItem(CooldownItem(name = name.trim(), price = price,
            addedTimestamp = System.currentTimeMillis(), cooldownDurationMillis = hours * 3_600_000,
            reason = reason.trim()))
    }
}
