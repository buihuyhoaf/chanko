package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.data.HomeScenario

sealed interface HomeUiEvent {
    // 1. Header
    data object ProfileClick : HomeUiEvent

    // 2. Little Room
    data class PropTapped(val kind: RoomPropKind) : HomeUiEvent

    // 3. Những món cần chú ý (Attention)
    data class DecisionTapped(val id: String) : HomeUiEvent
    data object ViewAllTapped : HomeUiEvent

    // 4. Budget Snapshot
    data object BudgetDetailsTapped : HomeUiEvent
    data class CycleChanged(
        val cycleLabel: String,
        val spentMinor: Long?,
        val limitMinor: Long?
    ) : HomeUiEvent

    // 5. Observation ("Góc nhìn từ Chanko")
    data class ObservationExplainTapped(val id: String) : HomeUiEvent
    data class ObservationHideTapped(val id: String) : HomeUiEvent
    data class ObservationDismissTapped(val id: String) : HomeUiEvent

    // 6. Contextual Sheet & Bottom Bar (+)
    data object GlobalAddTapped : HomeUiEvent
    data object DismissSheet : HomeUiEvent
    data class ContextSheetPrimaryAction(
        val kind: ContextKind,
        val entityId: String? = null
    ) : HomeUiEvent
    data class TabSelected(val tab: HomeTab) : HomeUiEvent

    // 7. Lifecycle, Reconciliation, Motion & Scenario Control
    data class ReconcileCoolingOff(val elapsedSeconds: Long = 0L) : HomeUiEvent
    data class SetReducedMotion(val enabled: Boolean) : HomeUiEvent
    data class RetrySection(val section: HomeSection) : HomeUiEvent
    data class SelectScenario(val scenario: HomeScenario) : HomeUiEvent
}
