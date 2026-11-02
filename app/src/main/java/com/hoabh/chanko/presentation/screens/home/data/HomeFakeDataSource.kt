package com.hoabh.chanko.presentation.screens.home.data

import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.BudgetUi
import com.hoabh.chanko.presentation.screens.home.ContextKind
import com.hoabh.chanko.presentation.screens.home.DecisionVerdict
import com.hoabh.chanko.presentation.screens.home.DialogueTone
import com.hoabh.chanko.presentation.screens.home.DialogueUi
import com.hoabh.chanko.presentation.screens.home.GreetingPeriod
import com.hoabh.chanko.presentation.screens.home.HeaderUiModel
import com.hoabh.chanko.presentation.screens.home.HomeSheet
import com.hoabh.chanko.presentation.screens.home.HomeTab
import com.hoabh.chanko.presentation.screens.home.HomeUiState
import com.hoabh.chanko.presentation.screens.home.LivingRoomUi
import com.hoabh.chanko.presentation.screens.home.ObservationUi
import com.hoabh.chanko.presentation.screens.home.RoomPropKind
import com.hoabh.chanko.presentation.screens.home.RoomPropUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import com.hoabh.chanko.presentation.screens.home.domain.BudgetCalculator
import com.hoabh.chanko.presentation.screens.home.domain.CalendarDateResolver
import com.hoabh.chanko.presentation.screens.home.domain.DecisionRanker
import com.hoabh.chanko.presentation.screens.home.domain.DecisionRankingResult
import com.hoabh.chanko.presentation.screens.home.domain.RawDecisionCandidate
import javax.inject.Inject

/**
 * Kịch bản dữ liệu Home S01–S22 phục vụ kiểm thử 22 quyết định Frozen và 22 Acceptance Criteria (AC-01..AC-22).
 */
enum class HomeScenario(val id: String) {
    S01("S01"),
    S02("S02"),
    S03("S03"),
    S04("S04"),
    S05("S05"),
    S06("S06"),
    S07("S07"),
    S08("S08"),
    S09("S09"),
    S10("S10"),
    S11("S11"),
    S12("S12"),
    S13("S13"),
    S14("S14"),
    S15("S15"),
    S16("S16"),
    S17("S17"),
    S18("S18"),
    S19("S19"),
    S20("S20"),
    S21("S21"),
    S22("S22"),

    // Aliases
    DEFAULT("S21"),
    NEW_USER("S05"),
    LONG_NAME("S15"),
    EMPTY_NAME("EMPTY_NAME"),
    OFFLINE_CACHE("S14"),
    PARTIAL_ERROR("S13")
}

class HomeFakeDataSource @Inject constructor() {

    fun getStateForScenario(
        scenario: HomeScenario = HomeScenario.S21,
        greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING,
        hiddenInsightIds: Set<String> = emptySet()
    ): HomeUiState {
        val baseS21 = buildS21Baseline(greetingPeriod, hiddenInsightIds)

        return when (scenario) {
            HomeScenario.S21, HomeScenario.DEFAULT -> baseS21

            HomeScenario.S22 -> {
                val waitingProp = (baseS21.livingRoom as SectionState.Content).data.props
                    .first { it.kind == RoomPropKind.Hourglass }
                baseS21.copy(
                    scenarioId = "S22",
                    activeSheet = HomeSheet.Contextual(
                        kind = ContextKind.Hourglass,
                        entityId = RoomPropKind.Hourglass.name,
                        title = waitingProp.label,
                        subtitle = waitingProp.summaryText,
                        highlightValue = waitingProp.badgeText,
                        items = baseS21.waitingItems,
                        primaryActionLabel = "Xem món đang chờ"
                    ),
                    selectedTab = HomeTab.Home
                )
            }

            HomeScenario.S01 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved"),
                        RawDecisionCandidate("W1", "Món đồ minh họa W1", 680_000L, "Waiting", remainingSeconds = 66_240L),
                        RawDecisionCandidate("R1", "Món đồ minh họa R1", 680_000L, "Ready"),
                        RawDecisionCandidate("C1", "Món đồ minh họa C1", 680_000L, "Considering"),
                        RawDecisionCandidate("R2", "Món đồ minh họa R2", 680_000L, "Ready")
                    )
                )
                val budget = BudgetCalculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    spentMinor = 2_400_000L,
                    limitMinor = 3_000_000L
                )
                baseS21.withRankingAndBudget(
                    scenarioId = "S01",
                    ranking = ranking,
                    budget = budget,
                    dialogue = DialogueUi(
                        headline = "Có món nào bạn muốn xem lại hôm nay không? 💜",
                        tone = DialogueTone.Ready
                    )
                ).copy(observation = null)
            }

            HomeScenario.S02 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    (1..5).map { idx ->
                        RawDecisionCandidate("R$idx", "Món đồ minh họa R$idx", 680_000L, "Ready")
                    }
                )
                baseS21.withRanking(scenarioId = "S02", ranking = ranking)
            }

            HomeScenario.S03 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved"),
                        RawDecisionCandidate("C1", "Món đồ minh họa C1", 680_000L, "Considering"),
                        RawDecisionCandidate("W1", "Món đồ minh họa W1", 680_000L, "Waiting", remainingSeconds = 66_240L),
                        RawDecisionCandidate("W2", "Món đồ minh họa W2", 680_000L, "Waiting", remainingSeconds = 22_200L),
                        RawDecisionCandidate("R1", "Món đồ minh họa R1", 680_000L, "Ready")
                    )
                )
                baseS21.withRanking(scenarioId = "S03", ranking = ranking)
            }

            HomeScenario.S04 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved")
                    )
                )
                baseS21.withRanking(scenarioId = "S04", ranking = ranking)
            }

            HomeScenario.S05, HomeScenario.NEW_USER -> {
                val emptyRanking = DecisionRankingResult(
                    topDecisions = emptyList(),
                    totalValidCount = 0,
                    waitingItems = emptyList(),
                    reviewItems = emptyList(),
                    skippedItems = emptyList()
                )
                val unknownBudget = BudgetCalculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    spentMinor = null,
                    limitMinor = null
                )
                baseS21.copy(
                    scenarioId = "S05",
                    livingRoom = SectionState.Content(
                        buildLivingRoomUi(
                            ranking = emptyRanking,
                            budget = unknownBudget,
                            dialogue = DialogueUi(
                                headline = "Hôm nay mình cùng Chanko mua sắm có suy nghĩ nhé 💜",
                                tone = DialogueTone.Neutral
                            )
                        )
                    ),
                    budgetSection = SectionState.Content(unknownBudget),
                    decisionsSection = SectionState.Empty("Chưa có món đồ nào cần chú ý"),
                    waitingItems = emptyList(),
                    reviewItems = emptyList(),
                    skippedItems = emptyList(),
                    totalAttentionCount = 0,
                    observation = null
                )
            }

            HomeScenario.S06 -> {
                val overBudget = BudgetCalculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    spentMinor = 3_600_000L,
                    limitMinor = 3_000_000L
                )
                baseS21.copy(
                    scenarioId = "S06",
                    budgetSection = SectionState.Content(overBudget)
                )
            }

            HomeScenario.S07 -> {
                val zeroBudget = BudgetCalculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    spentMinor = 100_000L,
                    limitMinor = 0L
                )
                baseS21.copy(
                    scenarioId = "S07",
                    budgetSection = SectionState.Content(zeroBudget)
                )
            }

            HomeScenario.S08 -> {
                val unknownBudget = BudgetCalculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    spentMinor = null,
                    limitMinor = null
                )
                baseS21.copy(
                    scenarioId = "S08",
                    budgetSection = SectionState.Content(unknownBudget)
                )
            }

            HomeScenario.S09 -> baseS21.copy(
                scenarioId = "S09",
                observation = ObservationUi(
                    id = "O1",
                    text = "Bạn đang cân nhắc 2 món công nghệ cùng lúc. Có thể thử so sánh nhu cầu để chọn món phù hợp nhất nhé!",
                    evidence = "Dựa trên 2 món công nghệ đang trong danh sách chú ý tuần này.",
                    detailedExplanation = "Khi cân nhắc nhiều món đồ cùng danh mục công nghệ một lúc, việc đặt chúng lên bàn cân về tần suất sử dụng thực tế sẽ giúp bạn chọn được món thực sự cần thiết nhất mà vẫn bảo vệ ngân sách."
                )
            )

            HomeScenario.S10 -> baseS21.copy(
                scenarioId = "S10",
                observation = ObservationUi(
                    id = "O1",
                    text = "Bạn thường lưu các món đồ vào buổi tối cuối tuần.",
                    evidence = "3 trong 5 món gần đây được thêm sau 21:00 vào Thứ 7 hoặc Chủ nhật.",
                    detailedExplanation = "Thời điểm tối cuối tuần thường dễ phát sinh cảm xúc mua sắm ngẫu hứng. Thời gian chờ (Cooling-off) sẽ giúp bạn kiểm chứng lại nhu cầu vào đầu tuần mới."
                )
            )

            HomeScenario.S11 -> baseS21.copy(
                scenarioId = "S11",
                observation = null
            )

            HomeScenario.S12 -> {
                // AC-07 & AC-08: Món hết cooling-off (remainingSeconds <= 0) tự động chuyển sang nhóm Cần xem lại (Ready),
                // giữ nguyên verdict = WAIT (không tự đổi thành BUY) và không tính trùng giữa Waiting & Review.
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate(
                            id = "W1",
                            title = "Bình giữ nhiệt 500ml",
                            priceMinor = 350_000L,
                            statusRaw = "Waiting",
                            remainingSeconds = -1L,
                            verdict = DecisionVerdict.WAIT
                        )
                    )
                )
                baseS21.withRanking(scenarioId = "S12", ranking = ranking)
            }

            HomeScenario.S13, HomeScenario.PARTIAL_ERROR -> baseS21.copy(
                scenarioId = "S13",
                budgetSection = SectionState.Error(
                    message = "Tạm thời chưa tải được ngân sách",
                    canRetry = true
                ),
                decisionsSection = SectionState.Loading,
                observation = null
            )

            HomeScenario.S14, HomeScenario.OFFLINE_CACHE -> {
                val cachedBudget = (baseS21.budgetSection as SectionState.Content).data
                val cachedDecisions = (baseS21.decisionsSection as SectionState.Content).data
                val cachedRoom = (baseS21.livingRoom as SectionState.Content).data
                baseS21.copy(
                    scenarioId = "S14",
                    header = baseS21.header.copy(
                        isOffline = true,
                        lastSyncedAtText = "Đồng bộ lần cuối: 09:30 hôm nay"
                    ),
                    livingRoom = SectionState.Content(cachedRoom, isFromCache = true),
                    budgetSection = SectionState.Content(cachedBudget, isFromCache = true),
                    decisionsSection = SectionState.Content(cachedDecisions, isFromCache = true),
                    observation = null
                )
            }

            HomeScenario.S15, HomeScenario.LONG_NAME -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate(
                            id = "L1",
                            title = "Tai nghe chống ồn phiên bản giới hạn với tên sản phẩm rất dài",
                            priceMinor = null,
                            statusRaw = "Ready",
                            thumbnailResId = null
                        )
                    )
                )
                baseS21.withRanking(scenarioId = "S15", ranking = ranking).copy(
                    header = HeaderUiModel(
                        userName = "Nguyễn Hoàng Minh Anh",
                        greetingPeriod = greetingPeriod,
                        isOffline = false
                    )
                )
            }

            HomeScenario.EMPTY_NAME -> baseS21.copy(
                scenarioId = "EMPTY_NAME",
                header = HeaderUiModel(
                    userName = "   ",
                    greetingPeriod = greetingPeriod,
                    isOffline = false
                )
            )

            HomeScenario.S16 -> baseS21.copy(
                scenarioId = "S16",
                activeSheet = null,
                selectedTab = HomeTab.Home
            )

            HomeScenario.S17 -> baseS21.copy(
                scenarioId = "S17",
                activeSheet = HomeSheet.Contextual(
                    kind = ContextKind.Decision,
                    entityId = "R1",
                    title = "Món đồ không còn khả dụng",
                    subtitle = "Mục này đã bị xóa hoặc cập nhật ở nơi khác. Bạn có thể đóng bảng này.",
                    highlightValue = null,
                    isEntityMissing = true
                )
            )

            HomeScenario.S18 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate("X", "Món đồ minh họa X", 680_000L, "Ready"),
                        RawDecisionCandidate("X", "Món đồ minh họa X", 680_000L, "Waiting"),
                        RawDecisionCandidate("U", "Món đồ minh họa U", 680_000L, "Unknown")
                    )
                )
                baseS21.withRanking(scenarioId = "S18", ranking = ranking)
            }

            HomeScenario.S19 -> {
                val (month, day) = CalendarDateResolver.resolve(
                    nowIso = "2026-10-24T17:30:00Z",
                    zoneIdString = "Asia/Ho_Chi_Minh"
                )
                val currentRoom = (baseS21.livingRoom as SectionState.Content).data
                baseS21.copy(
                    scenarioId = "S19",
                    livingRoom = SectionState.Content(
                        currentRoom.copy(
                            calendarMonthText = month,
                            calendarDayText = day
                        )
                    )
                )
            }

            HomeScenario.S20 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved"),
                        RawDecisionCandidate("C1", "Món đồ minh họa C1", 680_000L, "Considering"),
                        RawDecisionCandidate("C2", "Món đồ minh họa C2", 680_000L, "Considering"),
                        RawDecisionCandidate("S2", "Món đồ minh họa S2", 680_000L, "Saved")
                    )
                )
                baseS21.withRanking(scenarioId = "S20", ranking = ranking)
            }
        }
    }

    private fun buildS21Baseline(
        greetingPeriod: GreetingPeriod,
        hiddenInsightIds: Set<String>
    ): HomeUiState {
        val (month, day) = CalendarDateResolver.resolve(
            nowIso = FIXED_CLOCK_ISO,
            zoneIdString = FIXED_TIMEZONE
        )
        // Baseline S21 theo đúng thiết kế Candy Pop 3D v1.0:
        // - 1 món đến hạn xem lại (Tai nghe Bluetooth)
        // - 2 món đang chờ (Bình giữ nhiệt 500ml, Bàn phím cơ)
        // - 3 món đã bỏ qua (trong Pause Jar)
        val ranking = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate(
                    id = "J1",
                    title = "Tai nghe Bluetooth",
                    priceMinor = 1_290_000L,
                    statusRaw = "Ready",
                    thumbnailResId = R.drawable.sample_headphones
                ),
                RawDecisionCandidate(
                    id = "H1",
                    title = "Bình giữ nhiệt 500ml",
                    priceMinor = 350_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = 66_240L,
                    customSecondaryText = "Còn 18 giờ 24 phút",
                    thumbnailResId = R.drawable.sample_jacket
                ),
                RawDecisionCandidate(
                    id = "K1",
                    title = "Bàn phím cơ không dây",
                    priceMinor = 1_850_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = 22_200L,
                    customSecondaryText = "Còn 6 giờ 10 phút",
                    thumbnailResId = R.drawable.sample_keyboard
                ),
                RawDecisionCandidate(
                    id = "SK1",
                    title = "Ốp lưng silicon",
                    priceMinor = 150_000L,
                    statusRaw = "Skipped"
                ),
                RawDecisionCandidate(
                    id = "SK2",
                    title = "Đèn bàn LED",
                    priceMinor = 420_000L,
                    statusRaw = "Skipped"
                ),
                RawDecisionCandidate(
                    id = "SK3",
                    title = "Giá đỡ laptop nhôm",
                    priceMinor = 390_000L,
                    statusRaw = "Skipped"
                )
            )
        )

        val budget = BudgetCalculator.compute(
            cycleLabel = DEFAULT_CYCLE_LABEL,
            spentMinor = 2_400_000L,
            limitMinor = 3_000_000L
        )

        return HomeUiState(
            scenarioId = "S21",
            header = HeaderUiModel(
                userName = "Hoa",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            livingRoom = SectionState.Content(
                buildLivingRoomUi(
                    ranking = ranking,
                    budget = budget,
                    calendarMonth = month,
                    calendarDay = day,
                    dialogue = DialogueUi(
                        headline = "Có món nào bạn muốn xem lại hôm nay không? 💜",
                        tone = DialogueTone.Ready
                    )
                )
            ),
            decisionsSection = SectionState.Content(ranking.topDecisions),
            budgetSection = SectionState.Content(budget),
            waitingItems = ranking.waitingItems,
            reviewItems = ranking.reviewItems,
            skippedItems = ranking.skippedItems,
            totalAttentionCount = ranking.totalValidCount,
            observation = ObservationUi(
                id = "O1",
                text = "Bạn đang cân nhắc 2 món công nghệ cùng lúc. Có thể thử so sánh nhu cầu để chọn món phù hợp nhất nhé!",
                evidence = "Dựa trên 2 món công nghệ trong danh sách chú ý của bạn.",
                detailedExplanation = "Khi có nhiều món đồ cùng nhóm nhu cầu đang chờ quyết định, bạn có thể so sánh mức độ ưu tiên và tần suất sử dụng hàng ngày để chọn ra 1 món phù hợp nhất với ngân sách hiện tại."
            ),
            hiddenInsightIds = hiddenInsightIds,
            activeSheet = null,
            selectedTab = HomeTab.Home
        )
    }

    private fun HomeUiState.withRanking(
        scenarioId: String,
        ranking: DecisionRankingResult
    ): HomeUiState {
        val currentBudget = (budgetSection as? SectionState.Content)?.data
        val currentRoom = (livingRoom as? SectionState.Content)?.data
        return copy(
            scenarioId = scenarioId,
            livingRoom = SectionState.Content(
                buildLivingRoomUi(
                    ranking = ranking,
                    budget = currentBudget,
                    calendarMonth = currentRoom?.calendarMonthText ?: "OCT",
                    calendarDay = currentRoom?.calendarDayText ?: "24",
                    dialogue = currentRoom?.dialogue
                )
            ),
            decisionsSection = SectionState.Content(ranking.topDecisions),
            waitingItems = ranking.waitingItems,
            reviewItems = ranking.reviewItems,
            skippedItems = ranking.skippedItems,
            totalAttentionCount = ranking.totalValidCount
        )
    }

    private fun HomeUiState.withRankingAndBudget(
        scenarioId: String,
        ranking: DecisionRankingResult,
        budget: BudgetUi,
        dialogue: DialogueUi?
    ): HomeUiState {
        val currentRoom = (livingRoom as? SectionState.Content)?.data
        return copy(
            scenarioId = scenarioId,
            livingRoom = SectionState.Content(
                buildLivingRoomUi(
                    ranking = ranking,
                    budget = budget,
                    calendarMonth = currentRoom?.calendarMonthText ?: "OCT",
                    calendarDay = currentRoom?.calendarDayText ?: "24",
                    dialogue = dialogue
                )
            ),
            decisionsSection = SectionState.Content(ranking.topDecisions),
            budgetSection = SectionState.Content(budget),
            waitingItems = ranking.waitingItems,
            reviewItems = ranking.reviewItems,
            skippedItems = ranking.skippedItems,
            totalAttentionCount = ranking.totalValidCount
        )
    }

    fun buildLivingRoomUi(
        ranking: DecisionRankingResult,
        budget: BudgetUi?,
        calendarMonth: String = "OCT",
        calendarDay: String = "24",
        dialogue: DialogueUi? = DialogueUi(
            headline = "Có món nào bạn muốn xem lại hôm nay không? 💜",
            tone = DialogueTone.Ready
        )
    ): LivingRoomUi {
        val waitingCount = ranking.waitingCount
        val reviewCount = ranking.reviewCount
        val skippedCount = ranking.skippedCount

        val waitingSummary = if (waitingCount > 0) {
            "Bạn đang có $waitingCount món trong thời gian chờ hạ nhiệt"
        } else {
            "Chưa có món nào đang trong thời gian chờ"
        }

        val reviewSummary = if (reviewCount > 0) {
            "Có $reviewCount món đã đến hạn để bạn xem lại nhu cầu"
        } else {
            "Chưa có món nào đến hạn xem lại"
        }

        val walletSummary = when {
            budget == null || budget.limitMinor == null -> "Chưa thiết lập ngân sách cho chu kỳ này"
            budget.isOverBudget -> "Đã chi ${budget.spentFormatted} / ${budget.limitFormatted} (${budget.badgeText})"
            else -> "Còn lại ${budget.remainingFormatted} trong chu kỳ ${budget.cycleLabel}"
        }

        val skippedSummary = if (skippedCount > 0) {
            "Bạn đã chủ động bỏ qua $skippedCount món trong chu kỳ này"
        } else {
            "Chưa có món nào trong hũ đã bỏ qua"
        }

        return LivingRoomUi(
            dialogue = dialogue,
            calendarMonthText = calendarMonth,
            calendarDayText = calendarDay,
            props = listOf(
                RoomPropUi(
                    kind = RoomPropKind.Hourglass,
                    label = "Đang chờ",
                    summaryText = waitingSummary,
                    badgeText = waitingCount.toString(),
                    itemCount = waitingCount
                ),
                RoomPropUi(
                    kind = RoomPropKind.Calendar,
                    label = "Cần xem lại",
                    summaryText = reviewSummary,
                    badgeText = reviewCount.toString(),
                    itemCount = reviewCount
                ),
                RoomPropUi(
                    kind = RoomPropKind.Wallet,
                    label = "Ngân sách",
                    summaryText = walletSummary,
                    badgeText = null,
                    itemCount = 0
                ),
                RoomPropUi(
                    kind = RoomPropKind.PauseJar,
                    label = "Đã bỏ qua",
                    summaryText = skippedSummary,
                    badgeText = skippedCount.toString(),
                    itemCount = skippedCount
                )
            )
        )
    }

    companion object {
        const val FIXED_CLOCK_ISO = "2026-10-24T03:00:00Z"
        const val FIXED_TIMEZONE = "Asia/Ho_Chi_Minh"
        const val DEFAULT_CYCLE_LABEL = "01/10 – 31/10 (còn 12 ngày)"
    }
}
