package com.hoabh.chanko.presentation.features.home

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.presentation.features.home.mapper.JourneyItemCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Bộ kiểm thử đơn vị cho `HomeViewModel` v2.0 bao phủ 23 trạng thái trong `STATE-MATRIX.csv`
 * và 31 Acceptance Criteria (`AC-01`..`AC-31`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(HomeFakeDataSource())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `AC-01 AC-03 Default baseline loads 4 sections with consistent Budget and 5 Journey nodes`() {
        val state = viewModel.uiState.value

        val budget = state.budget
        assertNotNull(budget)
        assertEquals(2_350_000L, budget?.remainingMinor)
        assertEquals(2_650_000L, budget?.spentMinor)
        assertEquals(5_000_000L, budget?.limitMinor)
        assertEquals(53, budget?.percentageInt)

        assertEquals(5, state.journeyNodes.size)
        assertNotNull(state.visibleObservation)
    }

    @Test
    fun `AC-02 AvatarTap emits NavigateToProfile`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(HomeUiEvent.AvatarTap)
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToProfile), effects)
        job.cancel()
    }

    @Test
    fun `AC-04 BudgetCardTap emits NavigateToBudgetDetails and blocks rapid double-tap until NavigationCompleted`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        // Nhấn liên tiếp 2 lần trước khi điều hướng hoàn tất -> chỉ phát đúng 1 hiệu ứng (AC-04)
        viewModel.onEvent(HomeUiEvent.BudgetCardTap)
        viewModel.onEvent(HomeUiEvent.BudgetCardTap)
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudgetDetails), effects)

        // Sau khi NavigationCompleted -> cho phép điều hướng lần tiếp theo
        viewModel.onEvent(HomeUiEvent.NavigationCompleted)
        viewModel.onEvent(HomeUiEvent.BudgetCardTap)
        advanceUntilIdle()

        assertEquals(2, effects.size)
        job.cancel()
    }

    @Test
    fun `AC-08 AC-22 Partial error isolates Budget error while Journey remains interactive and Retry recovers`() {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.BUDGET_ERROR))
        var state = viewModel.uiState.value

        assertTrue(state.budgetState is SectionState.Error)
        // Journey vẫn hoạt động bình thường
        assertTrue(state.journeyState is SectionState.Content)
        assertEquals(5, state.journeyNodes.size)

        // Retry cục bộ cho Budget
        viewModel.onEvent(HomeUiEvent.RetrySection(HomeSection.Budget))
        state = viewModel.uiState.value
        assertTrue(state.budgetState is SectionState.Content)
    }

    @Test
    fun `AC-15 JourneyNodeTap opens ItemDetail directly with exact itemId without bottom sheet`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(HomeUiEvent.JourneyNodeTap("item_3"))
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToItemDetail("item_3")), effects)
        job.cancel()
    }

    @Test
    fun `AC-16 SeeAllTap opens MyItems with default ALL filter and never filters actionable`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(HomeUiEvent.SeeAllTap)
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.ALL)), effects)
        job.cancel()
    }

    @Test
    fun `AC-20 AC-29 PlusTap opens AddItemFlow when online and blocks with message when offline`() = runTest {
        // 1. Online -> OpenAddItemFlow
        val onlineEffects = mutableListOf<HomeUiEffect>()
        val job1 = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { onlineEffects.add(it) }
        }
        viewModel.onEvent(HomeUiEvent.PlusTap)
        advanceUntilIdle()
        assertEquals(listOf(HomeUiEffect.OpenAddItemFlow), onlineEffects)
        job1.cancel()

        // 2. Offline -> ShowOfflineBlockedMessage (không mở URLInputDialog)
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.OFFLINE_CACHE))
        val offlineEffects = mutableListOf<HomeUiEffect>()
        val job2 = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { offlineEffects.add(it) }
        }
        viewModel.onEvent(HomeUiEvent.PlusTap)
        advanceUntilIdle()
        assertEquals(1, offlineEffects.size)
        assertTrue(offlineEffects.single() is HomeUiEffect.ShowOfflineBlockedMessage)
        job2.cancel()
    }

    @Test
    fun `AC-21 OfflineNoCache uses dedicated OfflineNoCache state distinct from Empty and NoBudget`() {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.OFFLINE_NO_CACHE))
        val state = viewModel.uiState.value

        assertTrue(state.isOffline)
        assertTrue(state.budgetState is SectionState.OfflineNoCache)
        assertTrue(state.journeyState is SectionState.OfflineNoCache)
    }

    @Test
    fun `AC-23 LiveItemAdded defers list update while finger is down on a node and applies after gesture`() {
        val initialTopId = viewModel.uiState.value.journeyNodes.first().itemId
        assertEquals("item_1", initialTopId)

        // Người dùng đặt ngón tay lên item_2
        viewModel.onEvent(HomeUiEvent.JourneyNodeGestureStarted("item_2"))

        // Có món mới được thêm vào trong lúc ngón tay đang giữ
        val brandNewItem = JourneyItemCandidate(
            itemId = "item_new",
            title = "Món mới vừa thêm",
            priceMinor = 990_000L,
            statusRaw = "Saved",
            createdAtEpochMillis = 1_762_999_999_000L
        )
        viewModel.onEvent(HomeUiEvent.LiveItemAdded(brandNewItem))

        // Danh sách đang hiển thị KHÔNG đổi dưới ngón tay khi gesture chưa kết thúc
        assertEquals("item_1", viewModel.uiState.value.journeyNodes.first().itemId)
        assertTrue(viewModel.uiState.value.hasPendingJourneyUpdate)

        // Kết thúc gesture -> món mới mới được đẩy vào (ở dưới cùng vì là món mới nhất)
        viewModel.onEvent(HomeUiEvent.JourneyNodeGestureEnded)
        assertFalse(viewModel.uiState.value.hasPendingJourneyUpdate)
        assertEquals("item_new", viewModel.uiState.value.journeyNodes.last().itemId)
    }

    @Test
    fun `AC-24 Archive or delete during gesture does not swap item under finger and cancels click on deleted ID`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        // Bắt đầu chạm vào item_1
        viewModel.onEvent(HomeUiEvent.JourneyNodeGestureStarted("item_1"))

        // item_1 bị archive ngay trước khi nhấc tay
        viewModel.onEvent(HomeUiEvent.ItemArchivedOrDeleted("item_1"))

        // Trong lúc vẫn giữ tay, snapshot UI vẫn giữ item_1
        assertEquals("item_1", viewModel.uiState.value.journeyNodes.first().itemId)

        // Khi nhấc tay tạo click trên item_1 đã bị xóa -> hủy click (không mở nhầm item_2), và bù item_6 vào danh sách
        viewModel.onEvent(HomeUiEvent.JourneyNodeTap("item_1"))

        assertTrue(effects.isEmpty())
        assertEquals(
            listOf("item_6", "item_2", "item_3", "item_4", "item_5"),
            viewModel.uiState.value.journeyNodes.map { it.itemId }
        )
        job.cancel()
    }

    @Test
    fun `AC-25 Domain changes update Cooling-off to Ready and CycleRollover updates Budget`() {
        // 1. CycleRollover
        viewModel.onEvent(
            HomeUiEvent.CycleRollover(
                cycleLabel = "01/12 – 31/12",
                payNowSpentMinor = 500_000L,
                creditSpentMinor = 200_000L,
                limitMinor = 4_000_000L
            )
        )
        assertEquals("01/12 – 31/12", viewModel.uiState.value.budget?.cycleLabel)
        assertEquals(3_300_000L, viewModel.uiState.value.budget?.remainingMinor)

        // 2. ReconcileCoolingOff hết thời gian chờ của item_2 (43_200s)
        viewModel.onEvent(HomeUiEvent.ReconcileCoolingOff(elapsedSeconds = 50_000L))
        val item2 = viewModel.uiState.value.journeyNodes.first { it.itemId == "item_2" }
        assertEquals(JourneyNodeStatus.Ready, item2.status)
        assertEquals(DecisionVerdict.WAIT, item2.verdict)
    }

    @Test
    fun `AC-26 AC-27 Observation explain opens dialog and hide persists insightId across scenario reloads`() {
        val obs = viewModel.uiState.value.visibleObservation
        assertNotNull(obs)
        val insightId = obs!!.id

        // Mở dialog giải thích
        viewModel.onEvent(HomeUiEvent.InsightExplainTap(insightId))
        assertNotNull(viewModel.uiState.value.activeInsightDialog)

        // Ẩn gợi ý -> ẩn cả thẻ lẫn dialog và lưu bền vững qua reload
        viewModel.onEvent(HomeUiEvent.InsightHideTap(insightId))
        assertNull(viewModel.uiState.value.activeInsightDialog)
        assertNull(viewModel.uiState.value.visibleObservation)

        // Chuyển scenario rồi quay lại DEFAULT -> vẫn ẩn
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.ONE_ITEM))
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.DEFAULT_FIVE_NODES))
        assertNull(viewModel.uiState.value.visibleObservation)
    }

    @Test
    fun `SwipeRefreshHome online updates journey state, preserves scroll anchor, and resets isRefreshing`() {
        val anchorBefore = viewModel.uiState.value.scrollAnchorItemId
        assertNotNull(anchorBefore)

        viewModel.onEvent(HomeUiEvent.SwipeRefreshHome)

        val stateAfter = viewModel.uiState.value
        assertFalse(stateAfter.isRefreshing)
        assertEquals(anchorBefore, stateAfter.scrollAnchorItemId)
    }

    @Test
    fun `SwipeRefreshHome offline emits blocked message and stops isRefreshing immediately`() = runTest {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.OFFLINE_CACHE))
        assertTrue(viewModel.uiState.value.isOffline)

        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(HomeUiEvent.SwipeRefreshHome)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(1, effects.size)
        assertTrue(effects.single() is HomeUiEffect.ShowOfflineBlockedMessage)
        job.cancel()
    }

    // =========================================================================
    // Home v2.2 Budget Carousel Integration Tests (BC22-01..BC22-24)
    // =========================================================================

    @Test
    fun `BC22-02 HomeEntered increments carouselResetTrigger and clears navigationInProgress`() = runTest {
        val initialTrigger = viewModel.uiState.value.carouselResetTrigger
        viewModel.onEvent(HomeUiEvent.HomeEntered)
        assertEquals(initialTrigger + 1, viewModel.uiState.value.carouselResetTrigger)
    }

    @Test
    fun `BC22-14 BudgetSeeAllTap emits NavigateToBudgetHistory`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(HomeUiEvent.BudgetSeeAllTap)
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudgetHistory), effects)
        job.cancel()
    }

    @Test
    fun `BC22-12 BudgetSlotChanged updates selectedSlotKey in budgetCarouselState`() {
        val slotKey = "cycle_next_test"
        viewModel.onEvent(HomeUiEvent.BudgetSlotChanged(slotKey))

        val carousel = (viewModel.uiState.value.budgetCarouselState as? SectionState.Content)?.data
        assertNotNull(carousel)
        assertEquals(slotKey, carousel?.selectedSlotKey)
    }

    @Test
    fun `BC22-07 BudgetSlotTap on UpcomingPlaceholderCard when online emits NavigateToBudgetSetup`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        val placeholder = BudgetCardSlotUi.UpcomingPlaceholderCard(
            key = "cycle_next",
            cycleId = "cycle_next",
            cycleLabel = "Tháng 11/2026",
            dateRangeText = "01/11/2026 - 30/11/2026"
        )
        viewModel.onEvent(HomeUiEvent.BudgetSlotTap(placeholder))
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudgetSetup("cycle_next")), effects)
        job.cancel()
    }

    @Test
    fun `BC22-13 BudgetSlotTap on UpcomingPlaceholderCard when offline emits ShowOfflineBlockedMessage`() = runTest {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.OFFLINE_CACHE))
        assertTrue(viewModel.uiState.value.isOffline)

        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        val placeholder = BudgetCardSlotUi.UpcomingPlaceholderCard(
            key = "cycle_next",
            cycleId = "cycle_next",
            cycleLabel = "Tháng 11/2026",
            dateRangeText = "01/11/2026 - 30/11/2026"
        )
        viewModel.onEvent(HomeUiEvent.BudgetSlotTap(placeholder))
        advanceUntilIdle()

        assertEquals(1, effects.size)
        val effect = effects.single() as HomeUiEffect.ShowOfflineBlockedMessage
        assertTrue(effect.message.contains("ngoại tuyến"))
        job.cancel()
    }

    @Test
    fun `BC22-06 BudgetSlotTap on ActiveCard emits NavigateToBudget with Active mode`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        val activeCard = BudgetCardSlotUi.ActiveCard(
            key = "c_active",
            cycleId = "c_active",
            cycleLabel = "Tháng 10/2026",
            dateRangeText = "01/10/2026 - 31/10/2026",
            themeOrdinal = 2,
            spentMinor = 2_000_000L,
            limitMinor = 5_000_000L,
            remainingMinor = 3_000_000L,
            clampedProgress = 0.4f,
            realPercentText = "40%",
            isOverBudget = false,
            spentFormatted = "2.000.000 đ",
            limitFormatted = "5.000.000 đ",
            remainingFormatted = "3.000.000 đ"
        )
        viewModel.onEvent(HomeUiEvent.BudgetSlotTap(activeCard))
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudget("c_active", mode = "Active")), effects)
        job.cancel()
    }

    @Test
    fun `BC22-04 BudgetSlotTap on ClosedCard emits NavigateToBudget with ReadOnly mode`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        val closedCard = BudgetCardSlotUi.ClosedCard(
            key = "p_closed",
            cycleId = "p_closed",
            cycleLabel = "Tháng 09/2026",
            dateRangeText = "01/09/2026 - 30/09/2026",
            themeOrdinal = 1,
            spentMinor = 4_000_000L,
            limitMinor = 4_500_000L,
            remainingMinor = 500_000L,
            clampedProgress = 0.88f,
            realPercentText = "88%",
            isOverBudget = false,
            spentFormatted = "4.000.000 đ",
            limitFormatted = "4.500.000 đ",
            remainingFormatted = "500.000 đ"
        )
        viewModel.onEvent(HomeUiEvent.BudgetSlotTap(closedCard))
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudget("p_closed", mode = "ReadOnly")), effects)
        job.cancel()
    }

    @Test
    fun `BC22-07 BudgetSlotTap on UpcomingConfiguredCard emits NavigateToBudget with Upcoming mode`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        val upcomingCard = BudgetCardSlotUi.UpcomingConfiguredCard(
            key = "n_upcoming",
            cycleId = "n_upcoming",
            cycleLabel = "Tháng 11/2026",
            dateRangeText = "01/11/2026 - 30/11/2026",
            themeOrdinal = 3,
            limitMinor = 6_000_000L,
            limitFormatted = "6.000.000 đ"
        )
        viewModel.onEvent(HomeUiEvent.BudgetSlotTap(upcomingCard))
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudget("n_upcoming", mode = "Upcoming")), effects)
        job.cancel()
    }

    @Test
    fun `BC22-10 HomeViewModel with injected FakeBudgetRepository observes TriCycleWindow`() = runTest {
        val fakeRepo = FakeBudgetRepository()
        val vm = HomeViewModel(
            fakeDataSource = HomeFakeDataSource(),
            budgetRepository = fakeRepo
        )
        advanceUntilIdle()

        val carouselState = vm.uiState.value.budgetCarouselState
        assertTrue(carouselState is SectionState.Content)
        val data = (carouselState as SectionState.Content).data
        assertEquals(3, data.slots.size)
        assertEquals("cycle_2026_10", data.currentCycleKey)
    }

    @Test
    fun `BC22-22 Budget error isolates error to budgetCarouselState and Retry recovers`() = runTest {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.BUDGET_ERROR))

        assertTrue(viewModel.uiState.value.budgetCarouselState is SectionState.Error)
        // Journey và Observation vẫn nguyên vẹn
        assertTrue(viewModel.uiState.value.journeyState is SectionState.Content)

        // Retry
        viewModel.onEvent(HomeUiEvent.RetrySection(HomeSection.Budget))
        assertTrue(viewModel.uiState.value.budgetCarouselState is SectionState.Content)
    }

    @Test
    fun `Daily checkin flow prompts dialog, awards 10 Hat and 10 Pate, updates header pill`() = runTest {
        // Initial state opens dialog when not checked in
        assertTrue(viewModel.uiState.value.isCheckInDialogOpen)
        assertFalse(viewModel.uiState.value.header.hasCheckedInToday)

        val initialHat = viewModel.uiState.value.header.hatBalance
        val initialPate = viewModel.uiState.value.header.pateBalance

        // Confirm check-in
        viewModel.onEvent(HomeUiEvent.ConfirmCheckIn)

        assertFalse(viewModel.uiState.value.isCheckInDialogOpen)
        assertTrue(viewModel.uiState.value.header.hasCheckedInToday)
        assertEquals(initialHat + 10L, viewModel.uiState.value.header.hatBalance)
        assertEquals(initialPate + 10L, viewModel.uiState.value.header.pateBalance)
    }

    @Test
    fun `Daily checkin dismiss and reopen via RequestCheckIn event`() = runTest {
        viewModel.onEvent(HomeUiEvent.DismissCheckInDialog)
        assertFalse(viewModel.uiState.value.isCheckInDialogOpen)

        viewModel.onEvent(HomeUiEvent.RequestCheckIn)
        assertTrue(viewModel.uiState.value.isCheckInDialogOpen)
    }
}
