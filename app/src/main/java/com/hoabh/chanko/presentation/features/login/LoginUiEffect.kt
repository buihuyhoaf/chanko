package com.hoabh.chanko.presentation.features.login

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
}
