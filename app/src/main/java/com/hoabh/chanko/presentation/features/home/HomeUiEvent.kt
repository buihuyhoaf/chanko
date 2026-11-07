package com.hoabh.chanko.presentation.features.home

import com.hoabh.chanko.presentation.features.home.mapper.JourneyItemCandidate

/**
 * Các sự kiện tương tác trên màn hình Home v2.0 (ANDROID-COMPOSE-GUIDE.md, NAVIGATION-CONTRACT.csv).
 */
sealed interface HomeUiEvent {
    // 1. Header (AC-02)
    data object AvatarTap : HomeUiEvent
    data object ResourcePillTap : HomeUiEvent
    data object DismissResourceSheet : HomeUiEvent
    data object RequestCheckIn : HomeUiEvent
    data object DismissCheckInDialog : HomeUiEvent
    data object ConfirmCheckIn : HomeUiEvent
    data object DismissLevelUpCelebration : HomeUiEvent

    // 2. Budget Credit Card (AC-04, AC-08, AC-25) & Carousel v2.2 (BC22-01..BC22-20)
    data object BudgetCardTap : HomeUiEvent
    data object BudgetSeeAllTap : HomeUiEvent
    data class BudgetSlotTap(val slot: BudgetCardSlotUi) : HomeUiEvent
    data class BudgetSlotChanged(val slotKey: String) : HomeUiEvent
    data object HomeEntered : HomeUiEvent
    data class CycleRollover(
        val cycleLabel: String,
        val payNowSpentMinor: Long?,
        val creditSpentMinor: Long? = 0L,
        val limitMinor: Long?,
        val hasMissingPriceItems: Boolean = false
    ) : HomeUiEvent

    // 3. Item Journey ("Món đồ gần đây" — AC-15, AC-16, AC-23, AC-24)
    data class JourneyNodeGestureStarted(val itemId: String) : HomeUiEvent
    data object JourneyNodeGestureEnded : HomeUiEvent
    data class JourneyNodeTap(val itemId: String) : HomeUiEvent
    data object SeeAllTap : HomeUiEvent

    // 4. Live Domain Updates trên Journey (AC-23, AC-24, AC-25)
    data class LiveItemAdded(val candidate: JourneyItemCandidate) : HomeUiEvent
    data class ItemStatusChanged(val itemId: String, val newStatusRaw: String) : HomeUiEvent
    data class ItemArchivedOrDeleted(val itemId: String) : HomeUiEvent
    data class ReconcileCoolingOff(val elapsedSeconds: Long = 0L) : HomeUiEvent

    // 5. Observation ("Góc nhìn từ Chanko" — AC-26, AC-27)
    data class InsightExplainTap(val insightId: String) : HomeUiEvent
    data object DismissInsightDialog : HomeUiEvent
    data class InsightHideTap(val insightId: String) : HomeUiEvent

    // 6. Bottom Navigation & Global (+) Action (AC-20, AC-28, AC-29)
    data object PlusTap : HomeUiEvent
    data class TabTap(val tab: HomeTab) : HomeUiEvent

    // 7. Refresh, Retry, Restoration & Accessibility (AC-08, AC-19, AC-22, AC-30, AC-31)
    data object RefreshHome : HomeUiEvent
    data object SwipeRefreshHome : HomeUiEvent
    data object NavigationCompleted : HomeUiEvent
    data class RetrySection(val section: HomeSection) : HomeUiEvent
    data class SetReducedMotion(val enabled: Boolean) : HomeUiEvent
    data class SelectScenario(val scenario: HomeScenario) : HomeUiEvent
}
