package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.data.HomeScenario

sealed interface HomeUiEvent {
    // Header (HOME-01)
    data object ProfileClick : HomeUiEvent

    // Living Room Props (HOME-02)
    data class PropTapped(val kind: RoomPropKind) : HomeUiEvent

    // Quick Actions (HOME-03)
    data object QuickCheckTapped : HomeUiEvent
    data object SaveItemTapped : HomeUiEvent

    // Budget Snapshot (HOME-04)
    data object BudgetDetailsTapped : HomeUiEvent

    // Your Decisions (HOME-05)
    data class DecisionTapped(val id: String) : HomeUiEvent
    data object ViewAllTapped : HomeUiEvent

    // Observation (HOME-06)
    data class ObservationExplainTapped(val id: String) : HomeUiEvent
    data class ObservationHideTapped(val id: String) : HomeUiEvent
    data class ObservationDismissTapped(val id: String) : HomeUiEvent

    // Local Draft (HOME-07)
    data class ResumeDraftTapped(val id: String) : HomeUiEvent

    // Bottom Navigation & Sheets (HOME-08)
    data object GlobalAddTapped : HomeUiEvent
    data object DismissSheet : HomeUiEvent
    data class ContextSheetPrimaryAction(
        val kind: ContextKind,
        val entityId: String? = null
    ) : HomeUiEvent
    data class TabSelected(val tab: HomeTab) : HomeUiEvent

    // Section Error Recovery & Scenario Selection
    data class RetrySection(val section: HomeSection) : HomeUiEvent
    data class SelectScenario(val scenario: HomeScenario) : HomeUiEvent
}
