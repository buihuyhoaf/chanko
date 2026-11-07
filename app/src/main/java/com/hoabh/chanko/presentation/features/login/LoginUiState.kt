package com.hoabh.chanko.presentation.features.login

import com.hoabh.chanko.domain.auth.AuthFailure

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: AuthFailure? = null,
    val isLoginSuccess: Boolean = false,
)
