package com.hoabh.chanko.presentation.screens.login

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
}
