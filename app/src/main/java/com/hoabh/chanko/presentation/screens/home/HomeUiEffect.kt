package com.hoabh.chanko.presentation.screens.home

sealed interface HomeUiEffect {
    data object NavigateToProfile : HomeUiEffect
    data object OpenQuickCheckFlow : HomeUiEffect
    data object OpenSaveItemFlow : HomeUiEffect
    data object NavigateToBudgetDetails : HomeUiEffect
    data class NavigateToDecisionDetail(val id: String) : HomeUiEffect
    data object NavigateToAllDecisions : HomeUiEffect
    data class OpenObservationExplanation(val id: String) : HomeUiEffect
    data class OpenDraftFlow(val id: String) : HomeUiEffect
}
