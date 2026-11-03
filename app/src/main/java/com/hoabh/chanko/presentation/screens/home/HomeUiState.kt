package com.hoabh.chanko.presentation.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/**
 * Trạng thái độc lập của từng khu vực dữ liệu trên màn hình Home v2.0 (ANDROID-COMPOSE-GUIDE.md, STATE-MATRIX.csv).
 * Lỗi ở một nguồn (Budget hoặc Journey) không xóa dữ liệu của nguồn còn lại (AC-08, AC-18..AC-22).
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

    data class OfflineNoCache(
        val message: String = ""
    ) : SectionState<Nothing>

    data class Error<T>(
        val message: String,
        val cachedData: T? = null,
        val canRetry: Boolean = true
    ) : SectionState<T>
}

enum class HomeSection {
    Budget,
    Journey,
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
 * Khu vực 1 — Header (AC-02, AC-20).
 */
@Immutable
data class HeaderUiModel(
    val userName: String = "",
    val greetingPeriod: GreetingPeriod = GreetingPeriod.EVENING,
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
 * Phân loại trạng thái ngân sách trên Budget Credit Card v2.0 (AC-03..AC-09).
 */
enum class BudgetStateKind {
    Normal,
    Warning,
    Reached,
    OverBudget,
    NoBudget
}

/**
 * Khu vực 2 — Budget Credit Card 3D Candy Orange (AC-03..AC-09).
 */
@Immutable
data class BudgetCreditCardUi(
    val cycleLabel: String,
    val spentMinor: Long?,
    val limitMinor: Long?,
    val remainingMinor: Long?,
    val overMinor: Long? = null,
    val ratio: Float?,
    val percentageInt: Int?,
    val visualProgress: Float,
    val stateKind: BudgetStateKind,
    val hasMissingPriceItems: Boolean = false,
    val missingPriceNote: String? = null,
    val spentFormatted: String,
    val limitFormatted: String,
    val remainingFormatted: String,
    val overWarningFormatted: String? = null,
    val progressPercentText: String
) {
    val isConfigured: Boolean
        get() = stateKind != BudgetStateKind.NoBudget

    val isOverBudget: Boolean
        get() = stateKind == BudgetStateKind.OverBudget
}

/**
 * Nhận định gốc của món đồ — không tự động đổi thành BUY khi hết hạn cooling-off (AC-25).
 */
enum class DecisionVerdict {
    WAIT,
    BUY,
    SKIP,
    NEUTRAL
}

/**
 * 6 trạng thái hợp lệ của node trên Item Journey v2.0 (UX-SPEC.md, DESIGN-TOKENS.json, AC-14):
 * - Saved ("Đã lưu")
 * - Considering ("Đang cân nhắc")
 * - Waiting ("Đang chờ")
 * - Ready ("Sẵn sàng xem lại")
 * - Purchased ("Đã mua")
 * - Skipped ("Đã bỏ qua")
 */
enum class JourneyNodeStatus(val vietnameseLabel: String) {
    Saved("Đã lưu"),
    Considering("Đang cân nhắc"),
    Waiting("Đang chờ"),
    Ready("Sẵn sàng xem lại"),
    Purchased("Đã mua"),
    Skipped("Đã bỏ qua")
}

/**
 * Một node tròn (80–96dp) trên hành trình món đồ gần đây (AC-10..AC-17).
 */
@Immutable
data class JourneyNodeUi(
    val itemId: String,
    val title: String,
    val priceMinor: Long?,
    val priceFormatted: String,
    val status: JourneyNodeStatus,
    val statusLabel: String = status.vietnameseLabel,
    val verdict: DecisionVerdict = DecisionVerdict.WAIT,
    val createdAtEpochMillis: Long,
    val remainingSeconds: Long? = null,
    @param:DrawableRes val thumbnailResId: Int? = null,
    val imageLoadFailed: Boolean = false,
    val isCoolingOffJustExpired: Boolean = false
)

/**
 * Khu vực 3 — Item Journey ("Món đồ gần đây" — AC-10..AC-17).
 */
@Immutable
data class ItemJourneyUi(
    val nodes: List<JourneyNodeUi> = emptyList(),
    val totalActiveAndHistoryCount: Int = 0
) {
    val connectorCount: Int
        get() = (nodes.size - 1).coerceAtLeast(0)
}

/**
 * Khu vực 4 — Observation ("Góc nhìn từ Chanko" — AC-26, AC-27).
 */
@Immutable
data class ObservationUi(
    val id: String,
    val text: String,
    val evidence: String,
    val detailedExplanation: String = "",
    val hasSufficientData: Boolean = true
)

enum class HomeTab {
    Home,
    Items,
    Chat,
    Insights
}

/**
 * State tổng thể của HomeScreen v2.0 (AC-01..AC-31):
 * - Thứ tự bắt buộc 4 khu vực cuộn: Header → Budget Credit Card → Item Journey → Observation.
 * - Không còn Living Room, 4 vật thể hay HomeSheetHost của v1.0.
 */
@Immutable
data class HomeUiState(
    val header: HeaderUiModel = HeaderUiModel(),
    val budgetState: SectionState<BudgetCreditCardUi> = SectionState.Loading,
    val journeyState: SectionState<ItemJourneyUi> = SectionState.Loading,
    val observationState: SectionState<ObservationUi?> = SectionState.Content(null),
    val hiddenInsightIds: Set<String> = emptySet(),
    val activeInsightDialog: ObservationUi? = null,
    val activeGestureItemId: String? = null,
    val hasPendingJourneyUpdate: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val isReducedMotionEnabled: Boolean = false,
    val scrollAnchorItemId: String? = null,
    val selectedTab: HomeTab = HomeTab.Home
) {
    val budget: BudgetCreditCardUi?
        get() = when (val s = budgetState) {
            is SectionState.Content -> s.data
            is SectionState.Error -> s.cachedData
            else -> null
        }

    val journeyNodes: List<JourneyNodeUi>
        get() = when (val s = journeyState) {
            is SectionState.Content -> s.data.nodes
            is SectionState.Error -> s.cachedData?.nodes.orEmpty()
            else -> emptyList()
        }

    /**
     * Observation chỉ hiển thị khi có đủ dữ liệu thật và chưa bị ẩn theo insightId (AC-26, AC-27).
     */
    val visibleObservation: ObservationUi?
        get() = (observationState as? SectionState.Content)?.data?.takeIf { obs ->
            obs.hasSufficientData &&
                obs.text.isNotBlank() &&
                obs.id !in hiddenInsightIds
        }
}
