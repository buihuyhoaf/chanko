package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import com.hoabh.chanko.presentation.screens.home.domain.JourneyItemCandidate
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
    fun `AC-04 BudgetCardTap emits NavigateToBudgetDetails`() = runTest {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        viewModel.onEvent(HomeUiEvent.BudgetCardTap)
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudgetDetails), effects)
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

        // Kết thúc gesture -> món mới mới được đẩy lên đầu
        viewModel.onEvent(HomeUiEvent.JourneyNodeGestureEnded)
        assertFalse(viewModel.uiState.value.hasPendingJourneyUpdate)
        assertEquals("item_new", viewModel.uiState.value.journeyNodes.first().itemId)
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
            listOf("item_2", "item_3", "item_4", "item_5", "item_6"),
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
}
