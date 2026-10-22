package com.hoabh.chanko.presentation.screens.login.viewmodel

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.auth.AuthFailure
import com.hoabh.chanko.domain.auth.AuthResult
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import com.hoabh.chanko.domain.auth.GoogleTokenProvider
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: AuthFailure? = null,
    val isLoginSuccess: Boolean = false,
)

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthenticationRepository,
) : BaseViewModel<LoginUiState, LoginUiEffect>(LoginUiState()) {

    private var loginJob: Job? = null

    fun loginWithGoogle(provider: GoogleTokenProvider) {
        if (currentState.isLoading || currentState.isLoginSuccess) {
            return
        }

        updateState { LoginUiState(isLoading = true) }

        loginJob = viewModelScope.launch {
            try {
                when (val result = repository.signIn(provider)) {
                    is AuthResult.Success -> {
                        updateState { it.copy(isLoginSuccess = true) }
                        sendEffect(LoginUiEffect.NavigateToHome)
                    }
                    is AuthResult.Failure -> {
                        updateState { it.copy(error = result.reason) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                updateState { it.copy(error = AuthFailure.GOOGLE) }
            } finally {
                updateState { it.copy(isLoading = false) }
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
