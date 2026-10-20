package com.hoabh.chanko.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lớp ViewModel cơ sở quản lý UI State và One-off UI Effects.
 *
 * @param UiState Kiểu dữ liệu đại diện cho trạng thái màn hình (thường là data class).
 * @param UiEffect Kiểu dữ liệu đại diện cho sự kiện dùng 1 lần (thường là sealed interface).
 *                 Nếu màn hình không có effect, có thể truyền [Nothing].
 */
abstract class BaseViewModel<UiState, UiEffect>(
    initialState: UiState
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    protected val currentState: UiState
        get() = _uiState.value

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect: Flow<UiEffect> = _effect.receiveAsFlow()

    /**
     * Cập nhật UI State một cách an toàn và đồng bộ.
     */
    protected fun updateState(reducer: (UiState) -> UiState) {
        _uiState.update(reducer)
    }

    /**
     * Gửi một Side Effect đến UI (chạy trong viewModelScope).
     */
    protected fun sendEffect(effect: UiEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Gửi một Side Effect thông qua lambda builder.
     */
    protected fun sendEffect(builder: () -> UiEffect) {
        sendEffect(builder())
    }
}
