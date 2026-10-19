package com.hoabh.chanko.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.auth.AuthFailure
import com.hoabh.chanko.domain.auth.AuthResult
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import com.hoabh.chanko.domain.auth.GoogleTokenProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: AuthFailure? = null,
    val isLoginSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthenticationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private var loginJob: Job? = null

    fun loginWithGoogle(provider: GoogleTokenProvider) {
        if (_uiState.value.isLoading || _uiState.value.isLoginSuccess) {
            return
        }

        _uiState.update { LoginUiState(isLoading = true) }

        loginJob = viewModelScope.launch {
            try {
                when (val result = repository.signIn(provider)) {
                    is AuthResult.Success -> _uiState.update { it.copy(isLoginSuccess = true) }
                    is AuthResult.Failure -> _uiState.update { it.copy(error = result.reason) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(error = AuthFailure.GOOGLE) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Huỷ bỏ tiến trình đăng nhập khi người dùng thoát màn hình hoặc Activity bị tái tạo.
     */
    fun cancelLogin() {
        loginJob?.cancel()
    }
}
