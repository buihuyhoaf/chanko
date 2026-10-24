package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.data.HomeScenario

sealed interface HomeUiEvent {
    data object SettingsClick : HomeUiEvent
    data object ProfileClick : HomeUiEvent
    data class SelectScenario(val scenario: HomeScenario) : HomeUiEvent
}
