package com.hoabh.chanko.presentation.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/**
 * Trạng thái độc lập của từng khu vực dữ liệu trên màn hình Home (Section-aware Data State).
 * Lỗi ở một section không làm toàn bộ Home chuyển sang màn hình lỗi (AC-15, HOME-EDGE-02).
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
    Observation
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
 * Mô hình trạng thái cho Khu vực 1 — Header (AC-03, AC-16, HOME-EDGE-05).
 */
@Immutable
data class HeaderUiModel(
    val userName: String = "",
    val greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING,
    val isOffline: Boolean = false,
    val lastSyncedAtText: String? = null,
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
 * Bốn vật thể tương tác trong Little Room (AC-04, HOME-REV-03):
 * - Hourglass ("Đang chờ")
 * - Calendar ("Cần xem lại")
 * - Wallet ("Ngân sách")
 * - PauseJar ("Đã bỏ qua")
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
    val badgeText: String? = null,
    val itemCount: Int = 0
)

enum class DialogueTone {
    Ready,
    Waiting,
    Budget,
    Neutral,
    Offline
}

/**
 * Bong bóng thoại ngắn bên trong Little Room (HOME-GAP-05).
 */
@Immutable
data class DialogueUi(
    val headline: String,
    val supportingLine: String? = null,
    val tone: DialogueTone = DialogueTone.Neutral
)

/**
 * Mô hình trạng thái cho Khu vực 2 — Little Room.
 */
@Immutable
data class LivingRoomUi(
    val dialogue: DialogueUi? = null,
    val calendarMonthText: String = "OCT",
    val calendarDayText: String = "24",
    val props: List<RoomPropUi> = emptyList()
)

/**
 * Phân loại trạng thái ngân sách (AC-11).
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
    val overMinor: Long? = null,
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
 * Nhận định gốc của món đồ — không tự động đổi thành BUY khi hết hạn cooling-off (AC-07).
 */
enum class DecisionVerdict {
    WAIT,
    BUY,
    SKIP,
    NEUTRAL
}

/**
 * Trạng thái phân nhóm món đồ:
 * Ready (0: Cần xem lại / Vừa hết thời gian chờ) -> Waiting (1: Đang chờ) -> Considering (2) -> Saved (3) -> Skipped (4: Đã bỏ qua)
 */
enum class DecisionStatus(val priorityRank: Int) {
    Ready(0),
    Waiting(1),
    Considering(2),
    Saved(3),
    Skipped(4)
}

@Immutable
data class DecisionUi(
    val id: String,
    val title: String,
    val priceMinor: Long?,
    val priceFormatted: String,
    val status: DecisionStatus,
    val verdict: DecisionVerdict = DecisionVerdict.WAIT,
    val statusLabel: String,
    val secondaryText: String? = null,
    val remainingSeconds: Long? = null,
    @param:DrawableRes val thumbnailResId: Int? = null,
    val isTimerExpiredLocally: Boolean = false
)

/**
 * Khu vực 5 — Observation ("Góc nhìn từ Chanko" — AC-14, HOME-REV-05, HOME-OBS-01).
 */
@Immutable
data class ObservationUi(
    val id: String,
    val text: String,
    val evidence: String,
    val detailedExplanation: String = "",
    val hasSufficientData: Boolean = true
)

/**
 * Ngữ cảnh của Contextual Bottom Sheet (AC-04, AC-05, AC-06).
 */
enum class ContextKind {
    Hourglass,
    Calendar,
    Wallet,
    PauseJar,
    Observation,
    Decision
}

@Immutable
sealed interface HomeSheet {
    data class Contextual(
        val kind: ContextKind,
        val entityId: String? = null,
        val title: String,
        val subtitle: String,
        val highlightValue: String? = null,
        val items: List<DecisionUi> = emptyList(),
        val explanationDetail: String? = null,
        val evidenceText: String? = null,
        val primaryActionLabel: String? = null,
        val errorMessage: String? = null,
        val isLoading: Boolean = false,
        val isEntityMissing: Boolean = false
    ) : HomeSheet {
        val isEmpty: Boolean
            get() = !isLoading &&
                errorMessage == null &&
                !isEntityMissing &&
                items.isEmpty() &&
                kind in setOf(ContextKind.Hourglass, ContextKind.Calendar, ContextKind.PauseJar)
    }
}

enum class HomeTab {
    Home,
    Items,
    Chat,
    Insights
}

@Immutable
data class HomeCapabilities(
    val canOpenProfile: Boolean = true,
    val canOpenBudgetDetails: Boolean = true,
    val canOpenViewAll: Boolean = true,
    val canOpenObservationExplain: Boolean = true,
    val canHideObservation: Boolean = true
)

/**
 * Trạng thái tổng thể của màn hình Home theo Chanko Home Design Handoff v1.0 (Candy Pop 3D).
 * Thứ tự 5 khu vực: Header -> Little Room -> Những món cần chú ý -> Budget -> Observation (AC-01, AC-02).
 */
@Immutable
data class HomeUiState(
    val scenarioId: String = "S21",
    val header: HeaderUiModel = HeaderUiModel(),
    val livingRoom: SectionState<LivingRoomUi> = SectionState.Content(LivingRoomUi()),
    val decisionsSection: SectionState<List<DecisionUi>> = SectionState.Loading,
    val budgetSection: SectionState<BudgetUi> = SectionState.Loading,
    val waitingItems: List<DecisionUi> = emptyList(),
    val reviewItems: List<DecisionUi> = emptyList(),
    val skippedItems: List<DecisionUi> = emptyList(),
    val totalAttentionCount: Int = 0,
    val observation: ObservationUi? = null,
    val hiddenInsightIds: Set<String> = emptySet(),
    val activeSheet: HomeSheet.Contextual? = null,
    val selectedTab: HomeTab = HomeTab.Home,
    val isReducedMotionEnabled: Boolean = false,
    val capabilities: HomeCapabilities = HomeCapabilities()
) {
    val visibleObservation: ObservationUi?
        get() = observation?.takeIf { it.hasSufficientData && it.id !in hiddenInsightIds }

    val isObservationVisible: Boolean
        get() = visibleObservation != null
}
