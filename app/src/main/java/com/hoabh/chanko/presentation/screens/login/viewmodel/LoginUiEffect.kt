package com.hoabh.chanko.presentation.screens.login.viewmodel

sealed interface LoginUiEffect {
    data object NavigateToHome : LoginUiEffect
}
