package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fakeDataSource: HomeFakeDataSource
) : BaseViewModel<HomeUiState, HomeUiEffect>(
    initialState = fakeDataSource.getStateForScenario(HomeScenario.DEFAULT)
) {

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.SettingsClick -> sendEffect(HomeUiEffect.NavigateToSettings)
            HomeUiEvent.ProfileClick -> sendEffect(HomeUiEffect.NavigateToProfile)
            is HomeUiEvent.SelectScenario -> {
                updateState {
                    fakeDataSource.getStateForScenario(
                        scenario = event.scenario,
                        greetingPeriod = it.header.greetingPeriod
                    )
                }
            }
        }
    }
}
