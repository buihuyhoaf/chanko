package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.domain.budget.model.BudgetCycleStatus
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-End Integration & Regression Tests cho Home v2.2 Budget Carousel (BC22-01..BC22-24):
 * - Tri-Cycle Window (Previous?, Current, Next)
 * - Setup Next Cycle Flow -> Home reflect
 * - Rollover cycle transition -> Home carousel update
 * - Offline setup protection with Vietnamese notice
 * - "Xem tất cả" Section Header navigation
 * - Over-budget progress clamp at 100% with real %
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeBudgetE2EFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeBudgetRepository
    private lateinit var fakeDataSource: HomeFakeDataSource
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeBudgetRepository()
        fakeDataSource = HomeFakeDataSource()
        viewModel = HomeViewModel(
            fakeDataSource = fakeDataSource,
            budgetRepository = fakeRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `BC22-01 BC22-03 Default carousel renders 3 slots with Current active and Next placeholder`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val carouselState = viewModel.uiState.value.budgetCarouselState
        assertTrue("Carousel state must be Content", carouselState is SectionState.Content)
        val data = (carouselState as SectionState.Content).data

        // 3 slots: Previous, Current, Next
        assertEquals(3, data.slots.size)

        // Slot 1: Closed previous
        val prevSlot = data.slots[0]
        assertTrue(prevSlot is BudgetCardSlotUi.ClosedCard)
        assertEquals(BudgetCardRole.Previous, prevSlot.role)
        assertEquals("cycle_2026_09", prevSlot.cycleId)

        // Slot 2: Active current
        val currentSlot = data.slots[1]
        assertTrue(currentSlot is BudgetCardSlotUi.ActiveCard)
        assertEquals(BudgetCardRole.Current, currentSlot.role)
        assertEquals("cycle_2026_10", currentSlot.cycleId)
        assertEquals("cycle_2026_10", data.currentCycleKey)

        // Slot 3: Upcoming placeholder
        val nextSlot = data.slots[2]
        assertTrue(nextSlot is BudgetCardSlotUi.UpcomingPlaceholderCard)
        assertEquals(BudgetCardRole.NextPlaceholder, nextSlot.role)
    }

    @Test
    fun `BC22-04 Account without previous cycle renders only 2 slots Current and Next`() = runTest(testDispatcher) {
        fakeRepository.setNoPreviousCycleForTesting()
        val vmNoPrev = HomeViewModel(
            fakeDataSource = fakeDataSource,
            budgetRepository = fakeRepository
        )
        advanceUntilIdle()

        val carouselState = vmNoPrev.uiState.value.budgetCarouselState
        assertTrue(carouselState is SectionState.Content)
        val data = (carouselState as SectionState.Content).data

        assertEquals(2, data.slots.size)
        assertEquals(BudgetCardRole.Current, data.slots[0].role)
        assertEquals(BudgetCardRole.NextPlaceholder, data.slots[1].role)
    }

    @Test
    fun `BC22-07 Next Cycle Setup flow updates Home carousel immediately with configured limit and theme`() = runTest(testDispatcher) {
        advanceUntilIdle()

        // 1. Initial Next is Placeholder
        val initialData = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        assertTrue(initialData.slots[2] is BudgetCardSlotUi.UpcomingPlaceholderCard)

        // 2. Setup budget for Next cycle (7.500.000 VND)
        val setupResult = fakeRepository.setupNextCycleBudget(7_500_000L)
        assertTrue(setupResult.isSuccess)
        advanceUntilIdle()

        // 3. Carousel on Home updates automatically to UpcomingConfiguredCard
        val updatedData = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        val updatedNextSlot = updatedData.slots[2]
        assertTrue(updatedNextSlot is BudgetCardSlotUi.UpcomingConfiguredCard)
        val configuredNext = updatedNextSlot as BudgetCardSlotUi.UpcomingConfiguredCard
        assertEquals(7_500_000L, configuredNext.limitMinor)
        assertEquals("7.500.000 ₫", configuredNext.limitFormatted)
        assertEquals(1, configuredNext.themeOrdinal) // ordinal 1 -> floorMod(1, 12) = 1
    }

    @Test
    fun `BC22-19 BC22-20 Rollover advances cycles next becomes current and preserves identity and theme`() = runTest(testDispatcher) {
        // Setup next cycle first
        fakeRepository.setupNextCycleBudget(6_000_000L)
        advanceUntilIdle()

        // Trigger Rollover
        val rolloverResult = fakeRepository.triggerRollover()
        assertTrue(rolloverResult.isSuccess)
        advanceUntilIdle()

        val data = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        assertEquals("cycle_2026_11", data.currentCycleKey)

        // Current slot is now cycle_2026_11 with configured limit 6.000.000 đ
        val currentSlot = data.slots.first { it.role == BudgetCardRole.Current }
        assertTrue(currentSlot is BudgetCardSlotUi.ActiveCard)
        val activeCurrent = currentSlot as BudgetCardSlotUi.ActiveCard
        assertEquals("cycle_2026_11", activeCurrent.cycleId)
        assertEquals(6_000_000L, activeCurrent.limitMinor)
        assertEquals(1, activeCurrent.themeOrdinal)
    }

    @Test
    fun `BC22-13 Offline tapping on Next Placeholder is blocked with Vietnamese notice`() = runTest(testDispatcher) {
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
        assertTrue(effect.message.contains("kết nối mạng"))
        job.cancel()
    }

    @Test
    fun `BC22-14 BudgetSeeAllTap always emits NavigateToBudgetHistory regardless of active slot`() = runTest(testDispatcher) {
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }

        // Swipe to next slot
        viewModel.onEvent(HomeUiEvent.BudgetSlotChanged("cycle_2026_11"))

        // Tap "Xem tất cả" at Section Header
        viewModel.onEvent(HomeUiEvent.BudgetSeeAllTap)
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudgetHistory), effects)
        job.cancel()
    }

    @Test
    fun `BC22-09 Over-budget progress clamps at 100 percent with real percentage and overMinor`() {
        val ds = HomeFakeDataSource()
        val overState = ds.getStateForScenario(HomeScenario.OVER_BUDGET)
        val carousel = (overState.budgetCarouselState as SectionState.Content).data
        val activeSlot = carousel.slots.first { it.role == BudgetCardRole.Current } as BudgetCardSlotUi.ActiveCard

        assertEquals(1.0f, activeSlot.clampedProgress, 0.001f)
        assertTrue(activeSlot.isOverBudget)
        assertEquals(650_000L, activeSlot.overMinor)
        assertEquals("650.000 ₫", activeSlot.overFormatted)
        assertEquals("113%", activeSlot.realPercentText)
    }

    @Test
    fun `BC22-02 HomeEntered triggers carousel reset to current slot`() = runTest(testDispatcher) {
        val initialResetTrigger = viewModel.uiState.value.carouselResetTrigger
        viewModel.onEvent(HomeUiEvent.HomeEntered)
        assertEquals(initialResetTrigger + 1, viewModel.uiState.value.carouselResetTrigger)
    }
}
