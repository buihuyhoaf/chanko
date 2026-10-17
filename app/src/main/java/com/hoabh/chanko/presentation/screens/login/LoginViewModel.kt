package com.hoabh.chanko.presentation.screens.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class LoginUiState(
    val account: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val accountError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onAccountChange(account: String) {
        _uiState.update {
            it.copy(
                account = account,
                accountError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun loginWithCredentials() {
        val current = _uiState.value
        val isAccountEmpty = current.account.isBlank()
        val isPasswordEmpty = current.password.isBlank()

        if (isAccountEmpty || isPasswordEmpty) {
            _uiState.update {
                it.copy(
                    accountError = if (isAccountEmpty) "Tài khoản không được để trống" else null,
                    passwordError = if (isPasswordEmpty) "Mật khẩu không được để trống" else null,
                    generalError = "Vui lòng nhập tài khoản và mật khẩu",
                    isLoginSuccess = false
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                accountError = null,
                passwordError = null,
                generalError = null,
                isLoginSuccess = true
            )
        }
    }

    fun loginWithGoogle() {
        _uiState.update {
            it.copy(
                accountError = null,
                passwordError = null,
                generalError = null,
                isLoginSuccess = true
            )
        }
    }

    fun consumeLoginSuccess() {
        _uiState.update {
            it.copy(isLoginSuccess = false)
        }
    }
}
