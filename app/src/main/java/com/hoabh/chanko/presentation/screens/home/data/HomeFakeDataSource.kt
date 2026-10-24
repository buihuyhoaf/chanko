package com.hoabh.chanko.presentation.screens.home.data

import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.ContextKind
import com.hoabh.chanko.presentation.screens.home.DialogueTone
import com.hoabh.chanko.presentation.screens.home.DialogueUi
import com.hoabh.chanko.presentation.screens.home.DraftSyncState
import com.hoabh.chanko.presentation.screens.home.DraftUi
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
import com.hoabh.chanko.presentation.screens.home.domain.HomeCurrencyFormatter
import com.hoabh.chanko.presentation.screens.home.domain.RawDecisionCandidate
import javax.inject.Inject

/**
 * Đầy đủ 22 kịch bản dữ liệu giả lập xác định (S01–S22) khớp 100% với `fixtures/home-scenarios.json`.
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

    // Aliases phục vụ backward compatibility cho các bài test và preview P1
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
        greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING
    ): HomeUiState {
        val baseS21 = buildS21Baseline(greetingPeriod)

        return when (scenario) {
            HomeScenario.S21, HomeScenario.DEFAULT -> baseS21

            HomeScenario.S22 -> baseS21.copy(
                scenarioId = "S22",
                activeSheet = HomeSheet.Global,
                selectedTab = HomeTab.Home
            )

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
                baseS21.copy(
                    scenarioId = "S01",
                    livingRoom = SectionState.Content(
                        defaultLivingRoomUi(
                            dialogue = DialogueUi(
                                headline = "Bạn có 2 món sẵn sàng xem lại đó!",
                                supportingLine = "Cùng xem thử nhé? 💜",
                                tone = DialogueTone.Ready
                            )
                        )
                    ),
                    budgetSection = SectionState.Content(
                        BudgetCalculator.compute(
                            cycleLabel = DEFAULT_CYCLE_LABEL,
                            spentMinor = 2_400_000L,
                            limitMinor = 3_000_000L
                        )
                    ),
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount,
                    observation = null,
                    draft = null
                )
            }

            HomeScenario.S02 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    (1..5).map { idx ->
                        RawDecisionCandidate("R$idx", "Món đồ minh họa R$idx", 680_000L, "Ready")
                    }
                )
                baseS21.copy(
                    scenarioId = "S02",
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
                )
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
                baseS21.copy(
                    scenarioId = "S03",
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
                )
            }

            HomeScenario.S04 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate("S1", "Món đồ minh họa S1", 680_000L, "Saved")
                    )
                )
                baseS21.copy(
                    scenarioId = "S04",
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
                )
            }

            HomeScenario.S05, HomeScenario.NEW_USER -> baseS21.copy(
                scenarioId = "S05",
                livingRoom = SectionState.Content(
                    defaultLivingRoomUi(
                        dialogue = DialogueUi(
                            headline = "Chào mừng bạn đến với phòng khách của Chanko!",
                            supportingLine = "Hãy lưu món đồ đầu tiên khi bạn cần cân nhắc nhé 💜",
                            tone = DialogueTone.Neutral
                        )
                    )
                ),
                budgetSection = SectionState.Content(
                    BudgetCalculator.compute(
                        cycleLabel = DEFAULT_CYCLE_LABEL,
                        spentMinor = null,
                        limitMinor = null
                    )
                ),
                decisionsSection = SectionState.Empty("Chưa có món đồ nào đang chờ"),
                totalAttentionCount = 0,
                observation = null,
                draft = null
            )

            HomeScenario.S06 -> baseS21.copy(
                scenarioId = "S06",
                budgetSection = SectionState.Content(
                    BudgetCalculator.compute(
                        cycleLabel = DEFAULT_CYCLE_LABEL,
                        spentMinor = 3_600_000L,
                        limitMinor = 3_000_000L
                    )
                )
            )

            HomeScenario.S07 -> baseS21.copy(
                scenarioId = "S07",
                budgetSection = SectionState.Content(
                    BudgetCalculator.compute(
                        cycleLabel = DEFAULT_CYCLE_LABEL,
                        spentMinor = 100_000L,
                        limitMinor = 0L
                    )
                )
            )

            HomeScenario.S08 -> baseS21.copy(
                scenarioId = "S08",
                budgetSection = SectionState.Content(
                    BudgetCalculator.compute(
                        cycleLabel = DEFAULT_CYCLE_LABEL,
                        spentMinor = null,
                        limitMinor = null
                    )
                )
            )

            HomeScenario.S09 -> baseS21.copy(
                scenarioId = "S09",
                observation = ObservationUi(
                    id = "O1",
                    text = "Bạn thường mua các món công nghệ vào cuối tuần.",
                    evidence = "3 trong 5 món gần đây được mua vào Thứ 7 hoặc Chủ nhật."
                ),
                draft = DraftUi(
                    id = "D1",
                    title = "Bản nháp tai nghe",
                    priceMinor = 1_250_000L,
                    priceFormatted = HomeCurrencyFormatter.formatVnd(1_250_000L),
                    updatedLabel = "Vừa lưu trên máy",
                    syncState = DraftSyncState.LocalDraft
                )
            )

            HomeScenario.S10 -> baseS21.copy(
                scenarioId = "S10",
                observation = ObservationUi(
                    id = "O1",
                    text = "Nội dung minh họa.",
                    evidence = "3 trong 5 món gần đây được mua vào Thứ 7 hoặc Chủ nhật."
                ),
                draft = null
            )

            HomeScenario.S11 -> baseS21.copy(
                scenarioId = "S11",
                observation = null,
                draft = DraftUi(
                    id = "D1",
                    title = "Bản nháp",
                    priceMinor = null,
                    priceFormatted = null,
                    updatedLabel = "Đã lưu cục bộ",
                    syncState = DraftSyncState.LocalDraft
                )
            )

            HomeScenario.S12 -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate(
                            id = "W1",
                            title = "Món đồ minh họa W1",
                            priceMinor = 680_000L,
                            statusRaw = "Waiting",
                            remainingSeconds = -1L
                        )
                    )
                )
                baseS21.copy(
                    scenarioId = "S12",
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
                )
            }

            HomeScenario.S13, HomeScenario.PARTIAL_ERROR -> baseS21.copy(
                scenarioId = "S13",
                budgetSection = SectionState.Error(
                    message = "Tạm thời chưa tải được ngân sách",
                    canRetry = true
                ),
                decisionsSection = SectionState.Loading,
                observation = null,
                draft = null
            )

            HomeScenario.S14, HomeScenario.OFFLINE_CACHE -> {
                val cachedBudget = (baseS21.budgetSection as SectionState.Content).data
                val cachedDecisions = (baseS21.decisionsSection as SectionState.Content).data
                baseS21.copy(
                    scenarioId = "S14",
                    header = baseS21.header.copy(isOffline = true),
                    budgetSection = SectionState.Content(cachedBudget, isFromCache = true),
                    decisionsSection = SectionState.Content(cachedDecisions, isFromCache = true),
                    observation = null,
                    draft = DraftUi(
                        id = "D1",
                        title = "Bản nháp ngoại tuyến",
                        priceMinor = 550_000L,
                        priceFormatted = HomeCurrencyFormatter.formatVnd(550_000L),
                        updatedLabel = "Đang chờ đồng bộ",
                        syncState = DraftSyncState.PendingSync
                    )
                )
            }

            HomeScenario.S15, HomeScenario.LONG_NAME -> {
                val ranking = DecisionRanker.rankAndSelectTop(
                    listOf(
                        RawDecisionCandidate(
                            id = "L1",
                            title = "Tai nghe chống ồn phiên bản giới hạn với tên sản phẩm rất dài",
                            priceMinor = 680_000L,
                            statusRaw = "Considering",
                            thumbnailResId = null
                        )
                    )
                )
                baseS21.copy(
                    scenarioId = "S15",
                    header = HeaderUiModel(
                        userName = "Nguyễn Hoàng Minh Anh",
                        greetingPeriod = greetingPeriod,
                        isOffline = false
                    ),
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
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
                baseS21.copy(
                    scenarioId = "S18",
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
                )
            }

            HomeScenario.S19 -> {
                // 2026-10-24T17:30:00Z in Asia/Ho_Chi_Minh (+07:00) -> 2026-10-25T00:30:00+07:00
                val (month, day) = CalendarDateResolver.resolve(
                    nowIso = "2026-10-24T17:30:00Z",
                    zoneIdString = "Asia/Ho_Chi_Minh"
                )
                baseS21.copy(
                    scenarioId = "S19",
                    livingRoom = SectionState.Content(
                        defaultLivingRoomUi(
                            calendarMonth = month,
                            calendarDay = day
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
                baseS21.copy(
                    scenarioId = "S20",
                    decisionsSection = SectionState.Content(ranking.topDecisions),
                    totalAttentionCount = ranking.totalValidCount
                )
            }
        }
    }

    private fun buildS21Baseline(greetingPeriod: GreetingPeriod): HomeUiState {
        val (month, day) = CalendarDateResolver.resolve(
            nowIso = FIXED_CLOCK_ISO,
            zoneIdString = FIXED_TIMEZONE
        )
        val ranking = DecisionRanker.rankAndSelectTop(
            listOf(
                RawDecisionCandidate(
                    id = "J1",
                    title = "Áo khoác vintage",
                    priceMinor = 680_000L,
                    statusRaw = "Ready",
                    thumbnailResId = R.drawable.sample_jacket
                ),
                RawDecisionCandidate(
                    id = "H1",
                    title = "Sony WH-1000XM5",
                    priceMinor = 7_490_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = 66_240L,
                    customSecondaryText = "Còn 18 giờ 24 phút",
                    thumbnailResId = R.drawable.sample_headphones
                ),
                RawDecisionCandidate(
                    id = "K1",
                    title = "Keychron K2",
                    priceMinor = 1_850_000L,
                    statusRaw = "Waiting",
                    remainingSeconds = 22_200L,
                    customSecondaryText = "Còn 6 giờ 10 phút",
                    thumbnailResId = R.drawable.sample_keyboard
                )
            )
        )

        return HomeUiState(
            scenarioId = "S21",
            header = HeaderUiModel(
                userName = "Hoa",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            livingRoom = SectionState.Content(
                defaultLivingRoomUi(
                    calendarMonth = month,
                    calendarDay = day,
                    dialogue = DialogueUi(
                        headline = "Bạn có 1 món sẵn sàng xem lại đó!",
                        supportingLine = "Cùng xem thử nhé? 💜",
                        tone = DialogueTone.Ready
                    )
                )
            ),
            budgetSection = SectionState.Content(
                BudgetCalculator.compute(
                    cycleLabel = DEFAULT_CYCLE_LABEL,
                    spentMinor = 2_400_000L,
                    limitMinor = 3_000_000L
                )
            ),
            decisionsSection = SectionState.Content(ranking.topDecisions),
            totalAttentionCount = ranking.totalValidCount,
            observation = ObservationUi(
                id = "O1",
                text = "Bạn thường mua các món công nghệ vào cuối tuần.",
                evidence = "3 trong 5 món gần đây được mua vào Thứ 7 hoặc Chủ nhật."
            ),
            draft = null,
            activeSheet = null,
            selectedTab = HomeTab.Home
        )
    }

    private fun defaultLivingRoomUi(
        calendarMonth: String = "OCT",
        calendarDay: String = "24",
        dialogue: DialogueUi? = DialogueUi(
            headline = "Bạn có 1 món sẵn sàng xem lại đó!",
            supportingLine = "Cùng xem thử nhé? 💜",
            tone = DialogueTone.Ready
        )
    ): LivingRoomUi = LivingRoomUi(
        dialogue = dialogue,
        calendarMonthText = calendarMonth,
        calendarDayText = calendarDay,
        props = listOf(
            RoomPropUi(
                kind = RoomPropKind.Hourglass,
                label = "Đang chờ hạ nhiệt",
                summaryText = "2 món đang trong thời gian chờ hạ nhiệt",
                badgeText = "2"
            ),
            RoomPropUi(
                kind = RoomPropKind.Calendar,
                label = "Sẵn sàng xem lại",
                summaryText = "1 món đã đủ thời gian chờ để bạn xem lại",
                badgeText = "1"
            ),
            RoomPropUi(
                kind = RoomPropKind.Wallet,
                label = "Ngân sách tháng",
                summaryText = "Đã chi 2.400.000 ₫ / 3.000.000 ₫ (Còn lại 600.000 ₫)",
                badgeText = "80%"
            ),
            RoomPropUi(
                kind = RoomPropKind.PauseJar,
                label = "Lần dừng suy nghĩ",
                summaryText = "3 lần tạm dừng mua sắm có suy nghĩ trong chu kỳ này",
                badgeText = "3"
            )
        )
    )

    companion object {
        const val FIXED_CLOCK_ISO = "2026-10-24T03:00:00Z"
        const val FIXED_TIMEZONE = "Asia/Ho_Chi_Minh"
        const val DEFAULT_CYCLE_LABEL = "Ngân sách tháng 10 – 11/2026"
    }
}
