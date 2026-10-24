package com.hoabh.chanko.presentation.screens.home

sealed interface HomeUiEffect {
    data object NavigateToSettings : HomeUiEffect
    data object NavigateToProfile : HomeUiEffect
}
