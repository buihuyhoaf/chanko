package com.hoabh.chanko.presentation.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/**
 * Trạng thái độc lập của từng khu vực dữ liệu trên màn hình Home (Section-aware Data State).
 * Lỗi ở một section không làm toàn bộ Home chuyển sang màn hình lỗi (S13).
 */
@Immutable
sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>

    data class Content<T>(
        val data: T,
        val isFromCache: Boolean = false
    ) : SectionState<T>

    data class Empty(
        val message: String = ""
    ) : SectionState<Nothing>

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : SectionState<Nothing>
}

enum class HomeSection {
    LivingRoom,
    Budget,
    Decisions,
    Observation,
    Draft
}

enum class GreetingPeriod {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        fun fromHour(hour: Int): GreetingPeriod = when (hour) {
            in 5..11 -> MORNING
            in 12..17 -> AFTERNOON
            else -> EVENING
        }
    }
}

/**
 * Mô hình trạng thái cho HOME-01 — Header.
 */
@Immutable
data class HeaderUiModel(
    val userName: String = "",
    val greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING,
    val isOffline: Boolean = false,
    val customGreeting: String? = null,
    val customSubtitle: String? = null
) {
    val displayName: String
        get() = userName.trim()

    val hasName: Boolean
        get() = displayName.isNotEmpty()

    val avatarInitial: String
        get() = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: DEFAULT_AVATAR_INITIAL

    companion object {
        const val DEFAULT_AVATAR_INITIAL = "C"
    }
}

/**
 * Bốn vật thể tương tác trong Compact Living Room (HOME-02).
 */
enum class RoomPropKind {
    Hourglass,
    Calendar,
    Wallet,
    PauseJar
}

@Immutable
data class RoomPropUi(
    val kind: RoomPropKind,
    val label: String,
    val summaryText: String,
    val badgeText: String? = null
)

enum class DialogueTone {
    Ready,
    Waiting,
    Budget,
    Neutral,
    Offline
}

/**
 * Bong bóng thoại bên trong Compact Living Room (z = 6).
 */
@Immutable
data class DialogueUi(
    val headline: String,
    val supportingLine: String? = null,
    val tone: DialogueTone = DialogueTone.Neutral
)

/**
 * Mô hình trạng thái cho HOME-02 — Compact Living Room.
 */
@Immutable
data class LivingRoomUi(
    val dialogue: DialogueUi? = null,
    val calendarMonthText: String = "OCT",
    val calendarDayText: String = "24",
    val props: List<RoomPropUi> = emptyList()
)

/**
 * Phân loại trạng thái ngân sách (HOME-04).
 */
enum class BudgetStateKind {
    Normal,
    Warning,
    Reached,
    OverBudget,
    Zero,
    Unknown
}

@Immutable
data class BudgetUi(
    val cycleLabel: String,
    val spentMinor: Long?,
    val limitMinor: Long?,
    val remainingMinor: Long?,
    val ratio: Float?,
    val percentageInt: Int?,
    val visualProgress: Float,
    val stateKind: BudgetStateKind,
    val spentFormatted: String,
    val limitFormatted: String,
    val remainingFormatted: String,
    val badgeText: String,
    val progressRightLabel: String
) {
    val isOverBudget: Boolean
        get() = stateKind == BudgetStateKind.OverBudget
}

/**
 * Bốn trạng thái hợp lệ cho Your Decisions theo thứ tự ưu tiên:
 * Ready (0) -> Waiting (1) -> Considering (2) -> Saved (3)
 */
enum class DecisionStatus(val priorityRank: Int) {
    Ready(0),
    Waiting(1),
    Considering(2),
    Saved(3)
}

@Immutable
data class DecisionUi(
    val id: String,
    val title: String,
    val priceMinor: Long,
    val priceFormatted: String,
    val status: DecisionStatus,
    val statusLabel: String,
    val secondaryText: String? = null,
    val remainingSeconds: Long? = null,
    @param:DrawableRes val thumbnailResId: Int? = null,
    val isTimerExpiredLocally: Boolean = false
)

/**
 * HOME-06 — Observation (Góc nhìn hôm nay).
 */
@Immutable
data class ObservationUi(
    val id: String,
    val text: String,
    val evidence: String
)

/**
 * HOME-07 — Local Draft Indicator.
 */
enum class DraftSyncState {
    LocalDraft,
    PendingSync,
    SyncFailed
}

@Immutable
data class DraftUi(
    val id: String,
    val title: String,
    val priceMinor: Long? = null,
    val priceFormatted: String? = null,
    val updatedLabel: String = "",
    val syncState: DraftSyncState = DraftSyncState.LocalDraft
)

/**
 * HOME-08 — Bottom Sheet (tối đa 1 sheet mở tại một thời điểm).
 */
enum class ContextKind {
    Hourglass,
    Calendar,
    Wallet,
    PauseJar,
    Decision
}

@Immutable
sealed interface HomeSheet {
    data object Global : HomeSheet

    data class Contextual(
        val kind: ContextKind,
        val entityId: String? = null,
        val title: String,
        val subtitle: String,
        val highlightValue: String? = null,
        val isEntityMissing: Boolean = false
    ) : HomeSheet
}

enum class HomeTab {
    Home,
    Items,
    Budget,
    Insights
}

@Immutable
data class HomeCapabilities(
    val canOpenSettings: Boolean = true,
    val canOpenProfile: Boolean = true,
    val canOpenBudgetDetails: Boolean = true,
    val canOpenViewAll: Boolean = true,
    val canOpenObservationExplain: Boolean = true,
    val canHideObservation: Boolean = true,
    val canResumeDraft: Boolean = true
)

/**
 * Trạng thái tổng thể của màn hình Home theo Spec v2.0 (Image-Aligned).
 */
@Immutable
data class HomeUiState(
    val scenarioId: String = "S21",
    val header: HeaderUiModel = HeaderUiModel(),
    val livingRoom: SectionState<LivingRoomUi> = SectionState.Content(LivingRoomUi()),
    val budgetSection: SectionState<BudgetUi> = SectionState.Loading,
    val decisionsSection: SectionState<List<DecisionUi>> = SectionState.Loading,
    val totalAttentionCount: Int = 0,
    val observation: ObservationUi? = null,
    val draft: DraftUi? = null,
    val activeSheet: HomeSheet? = null,
    val selectedTab: HomeTab = HomeTab.Home,
    val capabilities: HomeCapabilities = HomeCapabilities()
) {
    val isObservationVisible: Boolean
        get() = observation != null

    val isLocalDraftVisible: Boolean
        get() = draft != null
}
