package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.domain.budget.model.BudgetCycleStatus
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
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

    @Test
    fun `BC22-05 BC22-06 Placeholder has target monthLabel, setup CTA, and swiping past it does not mutate repository state`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val initialData = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        val placeholder = initialData.slots[2] as BudgetCardSlotUi.UpcomingPlaceholderCard

        // Month chip is 2-digit MM for next month
        assertEquals("11", placeholder.monthLabel)
        assertEquals("cycle_2026_11", placeholder.cycleId)

        // Swiping across placeholder slot does not mutate repository or create garbage cycle
        viewModel.onEvent(HomeUiEvent.BudgetSlotChanged(placeholder.key))
        advanceUntilIdle()

        val windowAfterSwipe = fakeRepository.observeTriCycleWindow().first()
        assertEquals(BudgetCycleStatus.UpcomingUnconfigured, windowAfterSwipe.next?.status)
        assertEquals(null, windowAfterSwipe.next?.configuredLimitMinor)
    }

    @Test
    fun `BC22-08 12 Candy gradient themes map strictly to floorMod ordinal, 12 + 1`() {
        for (ordinal in -24..36) {
            val expectedId = Math.floorMod(ordinal, 12) + 1
            val theme = com.hoabh.chanko.ui.theme.CandyBudgetThemes.getTheme(ordinal)
            assertEquals("Ordinal $ordinal should map to theme ID $expectedId", expectedId, theme.id)
            assertTrue("Theme ID must be 1..12", theme.id in 1..12)
        }
    }

    @Test
    fun `BC22-10 BC22-12 BC-CARD-CONTENT-01 Previous card has role Previous, monthLabel, and read-only navigation`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val data = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        val prevSlot = data.slots[0] as BudgetCardSlotUi.ClosedCard

        assertEquals(BudgetCardRole.Previous, prevSlot.role)
        assertEquals("09", prevSlot.monthLabel)
        assertEquals("cycle_2026_09", prevSlot.cycleId)
        assertTrue(prevSlot.remainingMinor >= 0)

        // Navigate on Previous card emits ReadOnly mode
        val effects = mutableListOf<HomeUiEffect>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects.add(it) }
        }
        viewModel.onEvent(HomeUiEvent.BudgetSlotTap(prevSlot))
        advanceUntilIdle()

        assertEquals(listOf(HomeUiEffect.NavigateToBudget(prevSlot.cycleId, mode = "ReadOnly")), effects)
        job.cancel()
    }

    @Test
    fun `BC22-11 BC22-13 BC-CARD-LABEL-01 Current card displays 2-digit MM monthLabel and reflects combined spending`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val data = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        val currentSlot = data.slots[1] as BudgetCardSlotUi.ActiveCard

        // Month chip is 2-digit MM
        assertEquals("10", currentSlot.monthLabel)
        assertEquals(2, currentSlot.monthLabel.length)
        assertTrue(currentSlot.monthLabel.toInt() in 1..12)

        // Combined spending: limit = 5_000_000, spent = 3_000_000, remaining = 2_000_000 (60%)
        assertEquals(5_000_000L, currentSlot.limitMinor)
        assertEquals(3_000_000L, currentSlot.spentMinor)
        assertEquals(2_000_000L, currentSlot.remainingMinor)
        assertEquals("60%", currentSlot.realPercentText)
    }

    @Test
    fun `BC22-17 Offline with cache preserves carousel data without zeroing out to 0`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.OFFLINE_CACHE))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOffline)
        val carouselState = viewModel.uiState.value.budgetCarouselState
        assertTrue("Budget carousel must still be Content when cached offline", carouselState is SectionState.Content)
        val data = (carouselState as SectionState.Content).data
        val current = data.slots.first { it.role == BudgetCardRole.Current } as BudgetCardSlotUi.ActiveCard

        // Preserves snapshot data, never reverts to 0 đ
        assertTrue(current.limitMinor > 0)
        assertTrue(current.spentMinor > 0)
        assertTrue(current.remainingMinor > 0)
    }

    @Test
    fun `BC22-23 Partial error in Budget is isolated and does not affect Journey nodes`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.BUDGET_ERROR))

        // Budget is in Error state
        assertTrue(viewModel.uiState.value.budgetCarouselState is SectionState.Error)

        // Journey remains fully functional in Content state with 5 nodes
        assertTrue(viewModel.uiState.value.journeyState is SectionState.Content)
        assertEquals(5, viewModel.uiState.value.journeyNodes.size)

        // Recovery via retry restores Content
        viewModel.onEvent(HomeUiEvent.RetrySection(HomeSection.Budget))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.budgetCarouselState is SectionState.Content)
    }

    @Test
    fun `BC22-24 Full end-to-end flow from setup budget to home reflect and rollover`() = runTest(testDispatcher) {
        advanceUntilIdle()

        // 1. Initial: Next slot is Placeholder
        val initialData = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        assertEquals(BudgetCardRole.NextPlaceholder, initialData.slots[2].role)

        // 2. User sets up budget for next cycle (8_000_000 đ)
        val setupResult = fakeRepository.setupNextCycleBudget(8_000_000L)
        assertTrue(setupResult.isSuccess)
        advanceUntilIdle()

        // 3. Home carousel immediately reflects UpcomingConfiguredCard with limit 8.000.000 đ
        val configuredData = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        val configuredNext = configuredData.slots[2] as BudgetCardSlotUi.UpcomingConfiguredCard
        assertEquals(8_000_000L, configuredNext.limitMinor)
        assertEquals("8.000.000 ₫", configuredNext.limitFormatted)

        // 4. Rollover: next becomes current, keeps theme and configured limit
        val rolloverResult = fakeRepository.triggerRollover()
        assertTrue(rolloverResult.isSuccess)
        advanceUntilIdle()

        val postRolloverData = (viewModel.uiState.value.budgetCarouselState as SectionState.Content).data
        val newCurrent = postRolloverData.slots.first { it.role == BudgetCardRole.Current } as BudgetCardSlotUi.ActiveCard
        assertEquals("cycle_2026_11", newCurrent.cycleId)
        assertEquals(8_000_000L, newCurrent.limitMinor)
        assertEquals(configuredNext.themeOrdinal, newCurrent.themeOrdinal)

        // Previous cycle now contains October 2026 as closed
        val newPrev = postRolloverData.slots.first { it.role == BudgetCardRole.Previous } as BudgetCardSlotUi.ClosedCard
        assertEquals("cycle_2026_10", newPrev.cycleId)
    }
}
