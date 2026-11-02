package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import com.hoabh.chanko.presentation.screens.home.domain.BudgetCalculator
import com.hoabh.chanko.presentation.screens.home.domain.DecisionRanker
import com.hoabh.chanko.presentation.screens.home.domain.RawDecisionCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fakeDataSource: HomeFakeDataSource
) : BaseViewModel<HomeUiState, HomeUiEffect>(
    initialState = fakeDataSource.getStateForScenario(HomeScenario.S21)
) {

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.ProfileClick -> {
                if (uiState.value.capabilities.canOpenProfile) {
                    sendEffect(HomeUiEffect.NavigateToProfile)
                }
            }

            is HomeUiEvent.PropTapped -> {
                val state = uiState.value
                val room = (state.livingRoom as? SectionState.Content)?.data
                val prop = room?.props?.firstOrNull { it.kind == event.kind }
                val budget = (state.budgetSection as? SectionState.Content)?.data
                val budgetError = (state.budgetSection as? SectionState.Error)?.message
                val decisionsError = (state.decisionsSection as? SectionState.Error)?.message

                val sheet = when (event.kind) {
                    RoomPropKind.Hourglass -> HomeSheet.Contextual(
                        kind = ContextKind.Hourglass,
                        entityId = RoomPropKind.Hourglass.name,
                        title = prop?.label ?: "Đang chờ",
                        subtitle = prop?.summaryText ?: "Các món đang trong thời gian chờ hạ nhiệt",
                        highlightValue = prop?.badgeText,
                        items = state.waitingItems,
                        primaryActionLabel = "Xem trong Món đồ",
                        errorMessage = decisionsError,
                        isLoading = state.decisionsSection is SectionState.Loading
                    )

                    RoomPropKind.Calendar -> HomeSheet.Contextual(
                        kind = ContextKind.Calendar,
                        entityId = RoomPropKind.Calendar.name,
                        title = prop?.label ?: "Cần xem lại",
                        subtitle = prop?.summaryText ?: "Các món đã đến hạn để xem lại nhu cầu",
                        highlightValue = prop?.badgeText,
                        items = state.reviewItems,
                        primaryActionLabel = "Xem món cần xem lại",
                        errorMessage = decisionsError,
                        isLoading = state.decisionsSection is SectionState.Loading
                    )

                    RoomPropKind.Wallet -> HomeSheet.Contextual(
                        kind = ContextKind.Wallet,
                        entityId = RoomPropKind.Wallet.name,
                        title = prop?.label ?: "Ngân sách",
                        subtitle = prop?.summaryText ?: "Tóm tắt ngân sách chu kỳ hiện tại",
                        highlightValue = budget?.remainingFormatted,
                        primaryActionLabel = "Mở chi tiết ngân sách",
                        errorMessage = budgetError,
                        isLoading = state.budgetSection is SectionState.Loading
                    )

                    RoomPropKind.PauseJar -> HomeSheet.Contextual(
                        kind = ContextKind.PauseJar,
                        entityId = RoomPropKind.PauseJar.name,
                        title = prop?.label ?: "Đã bỏ qua",
                        subtitle = prop?.summaryText ?: "Các món bạn đã chủ động bỏ qua",
                        highlightValue = prop?.badgeText,
                        items = state.skippedItems,
                        primaryActionLabel = "Xem món đã bỏ qua",
                        errorMessage = decisionsError,
                        isLoading = state.decisionsSection is SectionState.Loading
                    )
                }

                updateState { it.copy(activeSheet = sheet) }
            }

            HomeUiEvent.BudgetDetailsTapped -> {
                if (uiState.value.capabilities.canOpenBudgetDetails) {
                    sendEffect(HomeUiEffect.NavigateToBudgetDetails)
                }
            }

            is HomeUiEvent.CycleChanged -> {
                // AC-13 & HOME-GAP-04: Tự động cập nhật Budget Snapshot đồng bộ khi đổi chu kỳ
                val updatedBudget = BudgetCalculator.compute(
                    cycleLabel = event.cycleLabel,
                    spentMinor = event.spentMinor,
                    limitMinor = event.limitMinor
                )
                updateState { current ->
                    val currentRoom = (current.livingRoom as? SectionState.Content)?.data
                    val updatedProps = currentRoom?.props?.map { prop ->
                        if (prop.kind == RoomPropKind.Wallet) {
                            prop.copy(
                                summaryText = "Còn lại ${updatedBudget.remainingFormatted} trong chu kỳ ${updatedBudget.cycleLabel}"
                            )
                        } else {
                            prop
                        }
                    }
                    current.copy(
                        budgetSection = SectionState.Content(
                            data = updatedBudget,
                            isFromCache = current.header.isOffline
                        ),
                        livingRoom = if (currentRoom != null && updatedProps != null) {
                            SectionState.Content(
                                data = currentRoom.copy(props = updatedProps),
                                isFromCache = current.header.isOffline
                            )
                        } else {
                            current.livingRoom
                        }
                    )
                }
            }

            is HomeUiEvent.DecisionTapped -> {
                sendEffect(HomeUiEffect.NavigateToDecisionDetail(event.id))
            }

            HomeUiEvent.ViewAllTapped -> {
                if (uiState.value.capabilities.canOpenViewAll) {
                    sendEffect(HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.ALL))
                }
            }

            is HomeUiEvent.ObservationExplainTapped -> {
                if (uiState.value.capabilities.canOpenObservationExplain) {
                    val obs = uiState.value.visibleObservation
                    if (obs != null && obs.id == event.id) {
                        // Hiển thị nội dung giải thích chi tiết ngay trong Contextual Sheet tại Home
                        updateState {
                            it.copy(
                                activeSheet = HomeSheet.Contextual(
                                    kind = ContextKind.Observation,
                                    entityId = obs.id,
                                    title = "Góc nhìn từ Chanko",
                                    subtitle = obs.text,
                                    explanationDetail = obs.detailedExplanation.ifEmpty { obs.text },
                                    evidenceText = obs.evidence,
                                    primaryActionLabel = "Đã hiểu"
                                )
                            )
                        }
                    }
                }
            }

            is HomeUiEvent.ObservationHideTapped -> {
                hideObservation(event.id)
            }

            is HomeUiEvent.ObservationDismissTapped -> {
                hideObservation(event.id)
            }

            HomeUiEvent.GlobalAddTapped -> {
                // AC-17 & HOME-GAP-03: Nút + khi offline thông báo cần kết nối mạng, không mở Add Item, không tạo draft
                if (uiState.value.header.isOffline) {
                    sendEffect(
                        HomeUiEffect.ShowOfflineBlockedMessage(
                            "Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thêm món đồ mới."
                        )
                    )
                } else {
                    sendEffect(HomeUiEffect.OpenAddItemFlow)
                }
            }

            HomeUiEvent.DismissSheet -> {
                updateState { it.copy(activeSheet = null) }
            }

            is HomeUiEvent.ContextSheetPrimaryAction -> {
                val currentSheet = uiState.value.activeSheet
                if (currentSheet != null && !currentSheet.isEntityMissing && currentSheet.errorMessage == null) {
                    updateState { it.copy(activeSheet = null) }
                    when (event.kind) {
                        ContextKind.Hourglass -> sendEffect(
                            HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.WAITING)
                        )
                        ContextKind.Calendar -> sendEffect(
                            HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.REVIEW)
                        )
                        ContextKind.PauseJar -> sendEffect(
                            HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.SKIPPED)
                        )
                        ContextKind.Wallet -> sendEffect(HomeUiEffect.NavigateToBudgetDetails)
                        ContextKind.Observation -> Unit
                        ContextKind.Decision -> {
                            event.entityId?.let { id ->
                                sendEffect(HomeUiEffect.NavigateToDecisionDetail(id))
                            }
                        }
                    }
                }
            }

            is HomeUiEvent.TabSelected -> {
                updateState { it.copy(selectedTab = event.tab) }
            }

            is HomeUiEvent.ReconcileCoolingOff -> {
                // AC-07 & AC-08: Khi thời gian trôi qua làm hết cooling-off, tự động reconcile lại
                // cả danh sách Attention lẫn các nhóm Little Room từ cùng 1 nguồn.
                reconcileCoolingOff(event.elapsedSeconds)
            }

            is HomeUiEvent.SetReducedMotion -> {
                updateState { it.copy(isReducedMotionEnabled = event.enabled) }
            }

            is HomeUiEvent.RetrySection -> {
                val currentHidden = uiState.value.hiddenInsightIds
                val baseline = fakeDataSource.getStateForScenario(
                    scenario = HomeScenario.S21,
                    greetingPeriod = uiState.value.header.greetingPeriod,
                    hiddenInsightIds = currentHidden
                )
                updateState { current ->
                    when (event.section) {
                        HomeSection.Budget -> current.copy(
                            budgetSection = SectionState.Content(
                                BudgetCalculator.compute(
                                    cycleLabel = HomeFakeDataSource.DEFAULT_CYCLE_LABEL,
                                    spentMinor = 2_400_000L,
                                    limitMinor = 3_000_000L
                                )
                            )
                        )
                        HomeSection.Decisions -> current.copy(
                            decisionsSection = baseline.decisionsSection,
                            waitingItems = baseline.waitingItems,
                            reviewItems = baseline.reviewItems,
                            skippedItems = baseline.skippedItems,
                            totalAttentionCount = baseline.totalAttentionCount
                        )
                        HomeSection.LivingRoom -> current.copy(
                            livingRoom = baseline.livingRoom
                        )
                        HomeSection.Observation -> current
                    }
                }
            }

            is HomeUiEvent.SelectScenario -> {
                updateState { current ->
                    fakeDataSource.getStateForScenario(
                        scenario = event.scenario,
                        greetingPeriod = current.header.greetingPeriod,
                        hiddenInsightIds = current.hiddenInsightIds
                    )
                }
            }
        }
    }

    private fun hideObservation(insightId: String) {
        if (!uiState.value.capabilities.canHideObservation) return
        updateState { current ->
            val updatedHidden = current.hiddenInsightIds + insightId
            val shouldCloseSheet = current.activeSheet?.kind == ContextKind.Observation &&
                current.activeSheet.entityId == insightId
            current.copy(
                hiddenInsightIds = updatedHidden,
                activeSheet = if (shouldCloseSheet) null else current.activeSheet
            )
        }
    }

    private fun reconcileCoolingOff(elapsedSeconds: Long) {
        val current = uiState.value
        val allItems = current.reviewItems + current.waitingItems + current.skippedItems
        if (allItems.isEmpty()) return

        val rawCandidates = allItems.map { item ->
            val updatedRemaining = item.remainingSeconds?.let { it - elapsedSeconds }
            val rawStatus = when (item.status) {
                DecisionStatus.Ready -> "Ready"
                DecisionStatus.Waiting -> "Waiting"
                DecisionStatus.Considering -> "Considering"
                DecisionStatus.Saved -> "Saved"
                DecisionStatus.Skipped -> "Skipped"
            }
            RawDecisionCandidate(
                id = item.id,
                title = item.title,
                priceMinor = item.priceMinor,
                statusRaw = rawStatus,
                remainingSeconds = updatedRemaining,
                verdict = item.verdict,
                customSecondaryText = if (updatedRemaining != null && updatedRemaining <= 0L) {
                    null
                } else {
                    item.secondaryText
                },
                thumbnailResId = item.thumbnailResId
            )
        }

        val ranking = DecisionRanker.rankAndSelectTop(rawCandidates)
        val currentBudget = (current.budgetSection as? SectionState.Content)?.data
        val currentRoom = (current.livingRoom as? SectionState.Content)?.data

        updateState { state ->
            state.copy(
                livingRoom = SectionState.Content(
                    fakeDataSource.buildLivingRoomUi(
                        ranking = ranking,
                        budget = currentBudget,
                        calendarMonth = currentRoom?.calendarMonthText ?: "OCT",
                        calendarDay = currentRoom?.calendarDayText ?: "24",
                        dialogue = currentRoom?.dialogue
                    ),
                    isFromCache = state.header.isOffline
                ),
                decisionsSection = if (ranking.topDecisions.isEmpty()) {
                    SectionState.Empty("Chưa có món đồ nào cần chú ý")
                } else {
                    SectionState.Content(ranking.topDecisions, isFromCache = state.header.isOffline)
                },
                waitingItems = ranking.waitingItems,
                reviewItems = ranking.reviewItems,
                skippedItems = ranking.skippedItems,
                totalAttentionCount = ranking.totalValidCount
            )
        }
    }
}
