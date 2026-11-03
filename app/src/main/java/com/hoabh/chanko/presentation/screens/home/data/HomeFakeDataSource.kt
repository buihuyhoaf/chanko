package com.hoabh.chanko.presentation.screens.home.data

import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.DecisionVerdict
import com.hoabh.chanko.presentation.screens.home.GreetingPeriod
import com.hoabh.chanko.presentation.screens.home.HeaderUiModel
import com.hoabh.chanko.presentation.screens.home.HomeTab
import com.hoabh.chanko.presentation.screens.home.HomeUiState
import com.hoabh.chanko.presentation.screens.home.ObservationUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.presentation.screens.home.domain.BudgetV2Calculator
import com.hoabh.chanko.presentation.screens.home.domain.JourneyItemCandidate
import com.hoabh.chanko.presentation.screens.home.domain.JourneySelector
import javax.inject.Inject

/**
 * Các kịch bản dữ liệu Home v2.0 phục vụ kiểm thử 23 trạng thái trong `STATE-MATRIX.csv`
 * và 31 Acceptance Criteria (`AC-01`..`AC-31`).
 */
enum class HomeScenario(val id: String) {
    DEFAULT_FIVE_NODES("DEFAULT_FIVE_NODES"),
    INITIAL_LOADING("INITIAL_LOADING"),
    EMPTY_ITEMS("EMPTY_ITEMS"),
    ONE_ITEM("ONE_ITEM"),
    TWO_TO_FOUR_ITEMS("TWO_TO_FOUR_ITEMS"),
    SIX_PLUS_WITH_ARCHIVED("SIX_PLUS_WITH_ARCHIVED"),
    NO_BUDGET("NO_BUDGET"),
    BUDGET_ERROR("BUDGET_ERROR"),
    JOURNEY_ERROR("JOURNEY_ERROR"),
    OVER_BUDGET("OVER_BUDGET"),
    MISSING_PRICE("MISSING_PRICE"),
    OFFLINE_CACHE("OFFLINE_CACHE"),
    OFFLINE_NO_CACHE("OFFLINE_NO_CACHE"),
    COOLING_EXPIRED("COOLING_EXPIRED"),
    INSIGHT_ABSENT("INSIGHT_ABSENT"),
    IMAGE_FAILURE("IMAGE_FAILURE"),

    // Aliases tương thích ngược cho Previews / Tests
    DEFAULT("DEFAULT_FIVE_NODES"),
    NEW_USER("EMPTY_ITEMS"),
    PARTIAL_ERROR("BUDGET_ERROR")
}

class HomeFakeDataSource @Inject constructor() {

    fun defaultCandidates(): List<JourneyItemCandidate> = listOf(
        JourneyItemCandidate(
            itemId = "item_1",
            title = "Tai nghe chụp tai không dây",
            priceMinor = 1_250_000L,
            statusRaw = "Saved",
            createdAtEpochMillis = 1_762_200_500_000L,
            thumbnailResId = R.drawable.sample_headphones
        ),
        JourneyItemCandidate(
            itemId = "item_2",
            title = "Áo khoác thể thao",
            priceMinor = 1_650_000L,
            statusRaw = "Waiting",
            createdAtEpochMillis = 1_762_200_400_000L,
            remainingSeconds = 43_200L,
            thumbnailResId = R.drawable.sample_jacket
        ),
        JourneyItemCandidate(
            itemId = "item_3",
            title = "Máy ảnh kỹ thuật số nhỏ gọn",
            priceMinor = 2_100_000L,
            statusRaw = "Ready",
            createdAtEpochMillis = 1_762_200_300_000L,
            thumbnailResId = R.drawable.product_placeholder_256
        ),
        JourneyItemCandidate(
            itemId = "item_4",
            title = "Bàn phím cơ không dây tím",
            priceMinor = 890_000L,
            statusRaw = "Skipped",
            createdAtEpochMillis = 1_762_200_200_000L,
            thumbnailResId = R.drawable.sample_keyboard
        ),
        JourneyItemCandidate(
            itemId = "item_5",
            title = "Balo vải chống thấm nước",
            priceMinor = 550_000L,
            statusRaw = "Considering",
            createdAtEpochMillis = 1_762_200_100_000L,
            thumbnailResId = R.drawable.sample_jacket
        ),
        JourneyItemCandidate(
            itemId = "item_6",
            title = "Bình giữ nhiệt (Món thứ 6 dự phòng)",
            priceMinor = 350_000L,
            statusRaw = "Purchased",
            createdAtEpochMillis = 1_762_200_000_000L,
            thumbnailResId = R.drawable.sample_headphones
        )
    )

    fun getStateForScenario(
        scenario: HomeScenario = HomeScenario.DEFAULT_FIVE_NODES,
        greetingPeriod: GreetingPeriod = GreetingPeriod.EVENING,
        hiddenInsightIds: Set<String> = emptySet()
    ): HomeUiState {
        val base = buildDefaultBaseline(greetingPeriod, hiddenInsightIds)

        return when (scenario) {
            HomeScenario.DEFAULT_FIVE_NODES,
            HomeScenario.DEFAULT -> base

            HomeScenario.INITIAL_LOADING -> base.copy(
                budgetState = SectionState.Loading,
                journeyState = SectionState.Loading,
                observationState = SectionState.Content(null)
            )

            HomeScenario.EMPTY_ITEMS,
            HomeScenario.NEW_USER -> base.copy(
                journeyState = SectionState.Empty("Hành trình cân nhắc còn trống"),
                observationState = SectionState.Content(null)
            )

            HomeScenario.ONE_ITEM -> {
                val singleJourney = JourneySelector.selectRecentJourney(
                    defaultCandidates().take(1)
                )
                base.copy(
                    journeyState = SectionState.Content(singleJourney),
                    scrollAnchorItemId = singleJourney.nodes.firstOrNull()?.itemId
                )
            }

            HomeScenario.TWO_TO_FOUR_ITEMS -> {
                val threeJourney = JourneySelector.selectRecentJourney(
                    defaultCandidates().take(3)
                )
                base.copy(
                    journeyState = SectionState.Content(threeJourney),
                    scrollAnchorItemId = threeJourney.nodes.firstOrNull()?.itemId
                )
            }

            HomeScenario.SIX_PLUS_WITH_ARCHIVED -> {
                val mixedWithArchived = listOf(
                    JourneyItemCandidate(
                        itemId = "arch_1",
                        title = "Món đã lưu trữ 1",
                        priceMinor = 900_000L,
                        statusRaw = "Saved",
                        createdAtEpochMillis = 1_762_300_000_000L,
                        isArchived = true
                    ),
                    JourneyItemCandidate(
                        itemId = "arch_2",
                        title = "Món đã lưu trữ 2",
                        priceMinor = 700_000L,
                        statusRaw = "Waiting",
                        createdAtEpochMillis = 1_762_250_000_000L,
                        isArchived = true
                    )
                ) + defaultCandidates()
                val journey = JourneySelector.selectRecentJourney(mixedWithArchived)
                base.copy(
                    journeyState = SectionState.Content(journey),
                    scrollAnchorItemId = journey.nodes.firstOrNull()?.itemId
                )
            }

            HomeScenario.NO_BUDGET -> {
                val noBudget = BudgetV2Calculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    payNowSpentMinor = null,
                    creditSpentMinor = null,
                    limitMinor = null
                )
                base.copy(
                    budgetState = SectionState.Content(noBudget)
                )
            }

            HomeScenario.BUDGET_ERROR,
            HomeScenario.PARTIAL_ERROR -> base.copy(
                budgetState = SectionState.Error(
                    message = "Tạm thời chưa tải được ngân sách",
                    canRetry = true
                )
            )

            HomeScenario.JOURNEY_ERROR -> base.copy(
                journeyState = SectionState.Error(
                    message = "Tạm thời chưa tải được món đồ gần đây",
                    canRetry = true
                )
            )

            HomeScenario.OVER_BUDGET -> {
                val overBudget = BudgetV2Calculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    payNowSpentMinor = 4_200_000L,
                    creditSpentMinor = 1_450_000L,
                    limitMinor = 5_000_000L
                )
                base.copy(
                    budgetState = SectionState.Content(overBudget)
                )
            }

            HomeScenario.MISSING_PRICE -> {
                val missingPriceBudget = BudgetV2Calculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    payNowSpentMinor = 2_650_000L,
                    creditSpentMinor = 0L,
                    limitMinor = 5_000_000L,
                    hasMissingPriceItems = true
                )
                val candidatesWithMissingPrice = listOf(
                    defaultCandidates().first().copy(priceMinor = null)
                ) + defaultCandidates().drop(1)
                base.copy(
                    budgetState = SectionState.Content(missingPriceBudget),
                    journeyState = SectionState.Content(
                        JourneySelector.selectRecentJourney(candidatesWithMissingPrice)
                    )
                )
            }

            HomeScenario.OFFLINE_CACHE -> {
                val cachedBudget = (base.budgetState as SectionState.Content).data
                val cachedJourney = (base.journeyState as SectionState.Content).data
                base.copy(
                    isOffline = true,
                    header = base.header.copy(
                        isOffline = true,
                        lastSyncedAtText = "Đồng bộ lúc 21:15"
                    ),
                    budgetState = SectionState.Content(cachedBudget, isFromCache = true),
                    journeyState = SectionState.Content(cachedJourney, isFromCache = true)
                )
            }

            HomeScenario.OFFLINE_NO_CACHE -> base.copy(
                isOffline = true,
                header = base.header.copy(
                    isOffline = true,
                    lastSyncedAtText = null
                ),
                budgetState = SectionState.OfflineNoCache("Chưa có dữ liệu ngân sách"),
                journeyState = SectionState.OfflineNoCache("Chưa có dữ liệu món đồ"),
                observationState = SectionState.Content(null)
            )

            HomeScenario.COOLING_EXPIRED -> {
                val expiredCandidates = listOf(
                    JourneyItemCandidate(
                        itemId = "item_expired",
                        title = "Giày chạy bộ vừa hết chờ",
                        priceMinor = 1_650_000L,
                        statusRaw = "Waiting",
                        createdAtEpochMillis = 1_762_200_600_000L,
                        remainingSeconds = 0L,
                        verdict = DecisionVerdict.WAIT,
                        thumbnailResId = R.drawable.sample_jacket
                    )
                ) + defaultCandidates().take(4)
                base.copy(
                    journeyState = SectionState.Content(
                        JourneySelector.selectRecentJourney(expiredCandidates)
                    )
                )
            }

            HomeScenario.INSIGHT_ABSENT -> base.copy(
                observationState = SectionState.Content(null)
            )

            HomeScenario.IMAGE_FAILURE -> {
                val failedImgCandidates = defaultCandidates().mapIndexed { index, item ->
                    if (index == 0) item.copy(thumbnailResId = null, imageLoadFailed = true) else item
                }
                base.copy(
                    journeyState = SectionState.Content(
                        JourneySelector.selectRecentJourney(failedImgCandidates)
                    )
                )
            }
        }
    }

    private fun buildDefaultBaseline(
        greetingPeriod: GreetingPeriod,
        hiddenInsightIds: Set<String>
    ): HomeUiState {
        val budget = BudgetV2Calculator.compute(
            cycleLabel = DEFAULT_CYCLE_LABEL,
            payNowSpentMinor = 2_000_000L,
            creditSpentMinor = 650_000L,
            limitMinor = 5_000_000L,
            hasMissingPriceItems = false
        )
        val journey = JourneySelector.selectRecentJourney(defaultCandidates())
        val observation = ObservationUi(
            id = DEFAULT_INSIGHT_ID,
            text = "Tuần này bạn đã thêm 3 món mới. Hãy cho bản thân thêm thời gian cân nhắc nhé! 💜",
            evidence = "3 món mới được thêm vào danh sách cân nhắc trong 7 ngày qua.",
            detailedExplanation = "Khi thêm nhiều món đồ liên tiếp trong thời gian ngắn, khoảng dừng Cooling-off giúp bạn tách biệt cảm xúc hào hứng tức thời khỏi nhu cầu sử dụng thực tế dài hạn.",
            hasSufficientData = true
        )

        return HomeUiState(
            header = HeaderUiModel(
                userName = "",
                greetingPeriod = greetingPeriod,
                isOffline = false,
                lastSyncedAtText = null
            ),
            budgetState = SectionState.Content(budget),
            journeyState = SectionState.Content(journey),
            observationState = SectionState.Content(observation),
            hiddenInsightIds = hiddenInsightIds,
            activeInsightDialog = null,
            activeGestureItemId = null,
            hasPendingJourneyUpdate = false,
            isRefreshing = false,
            isOffline = false,
            isReducedMotionEnabled = false,
            scrollAnchorItemId = journey.nodes.firstOrNull()?.itemId,
            selectedTab = HomeTab.Home
        )
    }

    companion object {
        const val DEFAULT_CYCLE_LABEL = "01/11 – 30/11"
        const val DEFAULT_INSIGHT_ID = "insight_weekly_3_new_items"
    }
}
