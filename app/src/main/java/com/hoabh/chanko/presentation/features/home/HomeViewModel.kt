package com.hoabh.chanko.presentation.features.home

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.features.home.mapper.BudgetCarouselMapper
import com.hoabh.chanko.presentation.features.home.mapper.BudgetV2Calculator
import com.hoabh.chanko.presentation.features.home.mapper.JourneyItemCandidate
import com.hoabh.chanko.presentation.features.home.mapper.JourneySelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fakeDataSource: HomeFakeDataSource,
    private val budgetRepository: BudgetRepository? = null
) : BaseViewModel<HomeUiState, HomeUiEffect>(
    initialState = fakeDataSource.getStateForScenario(
        scenario = HomeScenario.DEFAULT_FIVE_NODES,
        greetingPeriod = GreetingPeriod.fromHour(LocalTime.now().hour)
    )
) {

    // Lưu bền vững danh sách insightId đã ẩn qua các lần refresh/select scenario (AC-26)
    private val persistentHiddenInsightIds = LinkedHashSet<String>()

    // Nguồn dữ liệu ứng viên thực tế ở tầng ViewModel (bao gồm cả món thứ 6+ để bù khi xóa/archive — AC-24)
    private var backingCandidates: MutableList<JourneyItemCandidate> =
        fakeDataSource.defaultCandidates().toMutableList()

    // Chặn double-tap phát nhiều hiệu ứng điều hướng liên tiếp (ANDROID-COMPOSE-GUIDE.md)
    private var isNavigationInProgress: Boolean = false

    private var budgetObserveJob: Job? = null

    init {
        observeBudgetRepository()
    }

    private fun observeBudgetRepository() {
        val repo = budgetRepository ?: return
        budgetObserveJob?.cancel()
        budgetObserveJob = viewModelScope.launch {
            repo.observeTriCycleWindow()
                .catch { e ->
                    if (e is BudgetNoOfflineCacheException) {
                        updateState { it.copy(budgetCarouselState = SectionState.OfflineNoCache(e.message ?: "")) }
                    } else {
                        updateState {
                            it.copy(
                                budgetCarouselState = SectionState.Error(
                                    message = e.message ?: "Tạm thời chưa tải được ngân sách",
                                    canRetry = true
                                )
                            )
                        }
                    }
                }
                .collect { window ->
                    val carousel = BudgetCarouselMapper.map(window, window.snapshots)
                    updateState {
                        it.copy(budgetCarouselState = SectionState.Content(carousel))
                    }
                }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.HomeEntered -> {
                isNavigationInProgress = false
                updateState {
                    it.copy(carouselResetTrigger = it.carouselResetTrigger + 1)
                }
            }

            HomeUiEvent.AvatarTap -> {
                emitNavigationOnce(HomeUiEffect.NavigateToProfile)
            }

            HomeUiEvent.BudgetCardTap -> {
                emitNavigationOnce(HomeUiEffect.NavigateToBudgetDetails)
            }

            HomeUiEvent.BudgetSeeAllTap -> {
                emitNavigationOnce(HomeUiEffect.NavigateToBudgetHistory)
            }

            is HomeUiEvent.BudgetSlotTap -> {
                when (val slot = event.slot) {
                    is BudgetCardSlotUi.UpcomingPlaceholderCard -> {
                        if (currentState.isOffline || currentState.header.isOffline) {
                            sendEffect(
                                HomeUiEffect.ShowOfflineBlockedMessage(
                                    "Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thiết lập ngân sách."
                                )
                            )
                        } else {
                            emitNavigationOnce(HomeUiEffect.NavigateToBudgetSetup(slot.cycleId))
                        }
                    }
                    is BudgetCardSlotUi.ClosedCard -> {
                        emitNavigationOnce(HomeUiEffect.NavigateToBudget(cycleId = slot.cycleId, mode = "ReadOnly"))
                    }
                    is BudgetCardSlotUi.ActiveCard -> {
                        emitNavigationOnce(HomeUiEffect.NavigateToBudget(cycleId = slot.cycleId, mode = "Active"))
                    }
                    is BudgetCardSlotUi.UpcomingConfiguredCard -> {
                        emitNavigationOnce(HomeUiEffect.NavigateToBudget(cycleId = slot.cycleId, mode = "Upcoming"))
                    }
                    is BudgetCardSlotUi.CurrentEmptyCard -> {
                        emitNavigationOnce(HomeUiEffect.NavigateToBudget(cycleId = slot.cycleId, mode = "Active"))
                    }
                }
            }

            is HomeUiEvent.BudgetSlotChanged -> {
                updateState { state ->
                    val currentCarousel = (state.budgetCarouselState as? SectionState.Content)?.data
                    if (currentCarousel != null) {
                        state.copy(
                            budgetCarouselState = SectionState.Content(
                                data = currentCarousel.copy(selectedSlotKey = event.slotKey),
                                isFromCache = (state.budgetCarouselState as? SectionState.Content)?.isFromCache ?: false
                            )
                        )
                    } else {
                        state
                    }
                }
            }

            is HomeUiEvent.CycleRollover -> {
                val updatedBudget = BudgetV2Calculator.compute(
                    cycleLabel = event.cycleLabel,
                    payNowSpentMinor = event.payNowSpentMinor,
                    creditSpentMinor = event.creditSpentMinor,
                    limitMinor = event.limitMinor,
                    hasMissingPriceItems = event.hasMissingPriceItems
                )
                updateState {
                    it.copy(budgetState = SectionState.Content(updatedBudget))
                }
            }

            is HomeUiEvent.JourneyNodeGestureStarted -> {
                updateState {
                    it.copy(activeGestureItemId = event.itemId)
                }
            }

            HomeUiEvent.JourneyNodeGestureEnded -> {
                flushPendingJourneyIfNeeded()
            }

            is HomeUiEvent.JourneyNodeTap -> {
                val tappedId = event.itemId
                // Kiểm tra xem món đó có vừa bị xóa/archive trong lúc đang giữ ngón tay không (AC-24, NAVIGATION-NOTES.md)
                val stillExistsAndNotArchived = backingCandidates.any {
                    it.itemId == tappedId && !it.isArchived
                }
                flushPendingJourneyIfNeeded()
                if (stillExistsAndNotArchived) {
                    emitNavigationOnce(HomeUiEffect.NavigateToItemDetail(tappedId))
                }
            }

            HomeUiEvent.SeeAllTap -> {
                // AC-16: "Xem tất cả" mở My Items thông thường (HomeMyItemsFilter.ALL), không lọc actionable
                emitNavigationOnce(HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.ALL))
            }

            is HomeUiEvent.LiveItemAdded -> {
                backingCandidates.removeAll { it.itemId == event.candidate.itemId }
                backingCandidates.add(0, event.candidate)
                applyOrQueueJourneyUpdate()
            }

            is HomeUiEvent.ItemStatusChanged -> {
                // AC-11: Đổi trạng thái không đổi createdAt và không đổi thứ tự node
                backingCandidates = backingCandidates.map { candidate ->
                    if (candidate.itemId == event.itemId) {
                        candidate.copy(statusRaw = event.newStatusRaw)
                    } else {
                        candidate
                    }
                }.toMutableList()
                applyOrQueueJourneyUpdate()
            }

            is HomeUiEvent.ItemArchivedOrDeleted -> {
                // AC-24: Loại món và bù món kế tiếp; nếu đang trong gesture thì giữ snapshot dưới ngón tay
                backingCandidates = backingCandidates.map { candidate ->
                    if (candidate.itemId == event.itemId) {
                        candidate.copy(isArchived = true)
                    } else {
                        candidate
                    }
                }.toMutableList()
                applyOrQueueJourneyUpdate()
            }

            is HomeUiEvent.ReconcileCoolingOff -> {
                // AC-25: Khi cooling-off hết hạn -> chuyển sang Ready theo domain, không tự BUY
                backingCandidates = backingCandidates.map { candidate ->
                    val rem = candidate.remainingSeconds
                    if (rem != null) {
                        candidate.copy(remainingSeconds = rem - event.elapsedSeconds.coerceAtLeast(0L))
                    } else {
                        candidate
                    }
                }.toMutableList()
                applyOrQueueJourneyUpdate()
            }

            is HomeUiEvent.InsightExplainTap -> {
                val obs = currentState.visibleObservation
                if (obs != null && obs.id == event.insightId) {
                    updateState { it.copy(activeInsightDialog = obs) }
                }
            }

            HomeUiEvent.DismissInsightDialog -> {
                updateState { it.copy(activeInsightDialog = null) }
            }

            is HomeUiEvent.InsightHideTap -> {
                persistentHiddenInsightIds.add(event.insightId)
                updateState {
                    it.copy(
                        hiddenInsightIds = persistentHiddenInsightIds.toSet(),
                        activeInsightDialog = null
                    )
                }
            }

            HomeUiEvent.PlusTap -> {
                // AC-20 & AC-29: Online mở URLInputDialog (1 lần, không đổi tab); Offline báo cần mạng và không mở dialog
                if (currentState.isOffline || currentState.header.isOffline) {
                    sendEffect(
                        HomeUiEffect.ShowOfflineBlockedMessage(
                            "Bạn đang ngoại tuyến. Vui lòng kết nối mạng để thêm món đồ mới."
                        )
                    )
                } else {
                    sendEffect(HomeUiEffect.OpenAddItemFlow)
                }
            }

            is HomeUiEvent.TabTap -> {
                updateState { it.copy(selectedTab = event.tab) }
            }

            HomeUiEvent.RefreshHome -> {
                // AC-19 & AC-30: Giữ nguyên nội dung và vị trí cuộn/anchor hợp lệ khi đang refresh hoặc quay lại
                isNavigationInProgress = false
                val currentAnchor = resolveValidAnchor(currentState.scrollAnchorItemId)
                updateState {
                    it.copy(
                        isRefreshing = false,
                        scrollAnchorItemId = currentAnchor
                    )
                }
            }

            HomeUiEvent.SwipeRefreshHome -> {
                isNavigationInProgress = false
                if (currentState.isOffline || currentState.header.isOffline) {
                    sendEffect(
                        HomeUiEffect.ShowOfflineBlockedMessage(
                            "Bạn đang ngoại tuyến. Dữ liệu được giữ từ lần đồng bộ gần nhất."
                        )
                    )
                    updateState { it.copy(isRefreshing = false) }
                } else {
                    rebuildJourneyStateFromBacking()
                    val currentAnchor = resolveValidAnchor(currentState.scrollAnchorItemId)
                    updateState {
                        it.copy(
                            isRefreshing = false,
                            scrollAnchorItemId = currentAnchor
                        )
                    }
                }
            }

            HomeUiEvent.NavigationCompleted -> {
                isNavigationInProgress = false
            }

            is HomeUiEvent.RetrySection -> {
                val defaultBaseline = fakeDataSource.getStateForScenario(
                    scenario = HomeScenario.DEFAULT_FIVE_NODES,
                    greetingPeriod = currentState.header.greetingPeriod,
                    hiddenInsightIds = persistentHiddenInsightIds
                )
                when (event.section) {
                    HomeSection.Budget -> {
                        if (budgetRepository != null) {
                            observeBudgetRepository()
                        } else {
                            updateState {
                                it.copy(
                                    budgetState = defaultBaseline.budgetState,
                                    budgetCarouselState = defaultBaseline.budgetCarouselState
                                )
                            }
                        }
                    }
                    HomeSection.Journey -> {
                        backingCandidates = fakeDataSource.defaultCandidates().toMutableList()
                        val journey = JourneySelector.selectRecentJourney(backingCandidates)
                        updateState { state ->
                            state.copy(
                                journeyState = if (journey.nodes.isEmpty()) {
                                    SectionState.Empty("Hành trình cân nhắc còn trống")
                                } else {
                                    SectionState.Content(journey)
                                },
                                scrollAnchorItemId = resolveValidAnchor(state.scrollAnchorItemId, journey.nodes)
                            )
                        }
                    }
                    HomeSection.Observation -> updateState {
                        it.copy(observationState = defaultBaseline.observationState)
                    }
                }
            }

            is HomeUiEvent.SetReducedMotion -> {
                updateState {
                    it.copy(isReducedMotionEnabled = event.enabled)
                }
            }

            is HomeUiEvent.SelectScenario -> {
                isNavigationInProgress = false
                val nextState = fakeDataSource.getStateForScenario(
                    scenario = event.scenario,
                    greetingPeriod = currentState.header.greetingPeriod,
                    hiddenInsightIds = persistentHiddenInsightIds
                )
                backingCandidates = when (val js = nextState.journeyState) {
                    is SectionState.Content -> js.data.nodes.map { node ->
                        JourneyItemCandidate(
                            itemId = node.itemId,
                            title = node.title,
                            priceMinor = node.priceMinor,
                            statusRaw = node.status.name,
                            createdAtEpochMillis = node.createdAtEpochMillis,
                            isArchived = false,
                            remainingSeconds = node.remainingSeconds,
                            verdict = node.verdict,
                            thumbnailResId = node.thumbnailResId,
                            imageLoadFailed = node.imageLoadFailed
                        )
                    }.toMutableList().also { list ->
                        if (event.scenario == HomeScenario.DEFAULT_FIVE_NODES ||
                            event.scenario == HomeScenario.DEFAULT ||
                            event.scenario == HomeScenario.SIX_PLUS_WITH_ARCHIVED
                        ) {
                            // Giữ thêm món thứ 6 dự phòng để kiểm thử bù node khi xóa/archive (AC-24)
                            val sixth = fakeDataSource.defaultCandidates().last()
                            if (list.none { it.itemId == sixth.itemId }) {
                                list.add(sixth)
                            }
                        }
                    }
                    else -> mutableListOf()
                }
                updateState {
                    nextState.copy(
                        isReducedMotionEnabled = currentState.isReducedMotionEnabled,
                        hiddenInsightIds = persistentHiddenInsightIds.toSet()
                    )
                }
            }
        }
    }

    private fun applyOrQueueJourneyUpdate() {
        if (currentState.activeGestureItemId != null) {
            // Đang giữ ngón tay trên node -> xếp hàng đợi cho đến khi kết thúc gesture (AC-23, AC-24)
            updateState { it.copy(hasPendingJourneyUpdate = true) }
        } else {
            rebuildJourneyStateFromBacking()
        }
    }

    private fun flushPendingJourneyIfNeeded() {
        val hadPending = currentState.hasPendingJourneyUpdate
        updateState {
            it.copy(
                activeGestureItemId = null,
                hasPendingJourneyUpdate = false
            )
        }
        if (hadPending) {
            rebuildJourneyStateFromBacking()
        }
    }

    private fun rebuildJourneyStateFromBacking() {
        val journey = JourneySelector.selectRecentJourney(backingCandidates)
        val nextSectionState = if (journey.nodes.isEmpty()) {
            SectionState.Empty("Hành trình cân nhắc còn trống")
        } else {
            SectionState.Content(
                data = journey,
                isFromCache = currentState.isOffline
            )
        }
        val validAnchor = resolveValidAnchor(currentState.scrollAnchorItemId, journey.nodes)
        updateState {
            it.copy(
                journeyState = nextSectionState,
                scrollAnchorItemId = validAnchor
            )
        }
    }

    private fun resolveValidAnchor(
        previousAnchorId: String?,
        nodes: List<JourneyNodeUi> = currentState.journeyNodes
    ): String? {
        if (nodes.isEmpty()) return null
        if (previousAnchorId != null && nodes.any { it.itemId == previousAnchorId }) {
            return previousAnchorId
        }
        return nodes.first().itemId
    }

    private fun emitNavigationOnce(effect: HomeUiEffect) {
        if (isNavigationInProgress) return
        isNavigationInProgress = true
        sendEffect(effect)
    }
}
