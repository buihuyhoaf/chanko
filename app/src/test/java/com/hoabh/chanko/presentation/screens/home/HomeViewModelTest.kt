package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDataSource: HomeFakeDataSource
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDataSource = HomeFakeDataSource()
        viewModel = HomeViewModel(fakeDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsS21ScreenshotBaseline() = runTest(testDispatcher) {
        val state = viewModel.uiState.value

        assertEquals("S21", state.scenarioId)
        assertTrue(state.header.hasName)
        assertEquals("Hoa", state.header.displayName)
        assertEquals("H", state.header.avatarInitial)
        assertFalse(state.header.isOffline)

        val livingRoom = (state.livingRoom as SectionState.Content).data
        assertEquals("OCT", livingRoom.calendarMonthText)
        assertEquals("24", livingRoom.calendarDayText)
        assertNotNull(livingRoom.dialogue)
        assertEquals(4, livingRoom.props.size)

        val budget = (state.budgetSection as SectionState.Content).data
        assertEquals(600_000L, budget.remainingMinor)
        assertEquals(80, budget.percentageInt)
        assertEquals("Đã tiêu 80%", budget.badgeText)

        val decisions = (state.decisionsSection as SectionState.Content).data
        assertEquals(listOf("J1", "H1", "K1"), decisions.map { it.id })
        assertNotNull(state.observation)
        assertNull(state.draft)
        assertNull(state.activeSheet)
    }

    @Test
    fun scenarios_S01_through_S04_and_S20_matchExpectedVisibleDecisionIds() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S01))
        val s01 = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        assertEquals(listOf("R1", "R2", "W1"), s01.map { it.id })
        assertFalse(viewModel.uiState.value.isObservationVisible)
        assertFalse(viewModel.uiState.value.isLocalDraftVisible)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S02))
        val s02 = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        assertEquals(listOf("R1", "R2", "R3"), s02.map { it.id })

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S03))
        val s03 = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        assertEquals(listOf("R1", "W1", "W2"), s03.map { it.id })

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S04))
        val s04 = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        assertEquals(listOf("S1"), s04.map { it.id })

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S20))
        val s20 = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        assertEquals(listOf("C1", "C2", "S1"), s20.map { it.id })
    }

    @Test
    fun scenario_S05_and_budgetScenarios_S06_S07_S08_matchExpectedContracts() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S05))
        assertTrue(viewModel.uiState.value.decisionsSection is SectionState.Empty)
        assertEquals(0, viewModel.uiState.value.totalAttentionCount)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S06))
        val s06Budget = (viewModel.uiState.value.budgetSection as SectionState.Content).data
        assertEquals(-600_000L, s06Budget.remainingMinor)
        assertEquals(120, s06Budget.percentageInt)
        assertEquals(1.0f, s06Budget.visualProgress, 0.001f)
        assertEquals(BudgetStateKind.OverBudget, s06Budget.stateKind)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S07))
        val s07Budget = (viewModel.uiState.value.budgetSection as SectionState.Content).data
        assertEquals(-100_000L, s07Budget.remainingMinor)
        assertNull(s07Budget.ratio)
        assertEquals(BudgetStateKind.Zero, s07Budget.stateKind)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S08))
        val s08Budget = (viewModel.uiState.value.budgetSection as SectionState.Content).data
        assertNull(s08Budget.remainingMinor)
        assertNull(s08Budget.ratio)
        assertEquals(BudgetStateKind.Unknown, s08Budget.stateKind)
        assertFalse(s08Budget.spentFormatted.contains("0 ₫"))
    }

    @Test
    fun scenarios_S09_S10_S11_controlOptionalObservationAndDraftSections() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S09))
        assertTrue(viewModel.uiState.value.isObservationVisible)
        assertTrue(viewModel.uiState.value.isLocalDraftVisible)
        assertEquals("D1", viewModel.uiState.value.draft?.id)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S10))
        assertTrue(viewModel.uiState.value.isObservationVisible)
        assertFalse(viewModel.uiState.value.isLocalDraftVisible)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S11))
        assertFalse(viewModel.uiState.value.isObservationVisible)
        assertTrue(viewModel.uiState.value.isLocalDraftVisible)
    }

    @Test
    fun scenario_S12_preservesWaitingWhenLocalTimerHasExpired() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S12))
        val decisions = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        val item = decisions.single()
        assertEquals("W1", item.id)
        assertEquals(DecisionStatus.Waiting, item.status)
        assertTrue(item.isTimerExpiredLocally)
    }

    @Test
    fun scenario_S13_isolatesBudgetErrorAndRecoversOnRetry() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S13))
        val state = viewModel.uiState.value
        assertEquals("Hoa", state.header.displayName)
        assertTrue(state.budgetSection is SectionState.Error)
        assertTrue(state.decisionsSection is SectionState.Loading)

        viewModel.onEvent(HomeUiEvent.RetrySection(HomeSection.Budget))
        assertTrue(viewModel.uiState.value.budgetSection is SectionState.Content)
    }

    @Test
    fun scenarios_S14_S15_EMPTY_NAME_S18_S19_matchExpectedContracts() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S14))
        assertTrue(viewModel.uiState.value.header.isOffline)
        assertTrue((viewModel.uiState.value.budgetSection as SectionState.Content).isFromCache)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S15))
        assertEquals("Nguyễn Hoàng Minh Anh", viewModel.uiState.value.header.displayName)
        val s15Item = (viewModel.uiState.value.decisionsSection as SectionState.Content).data.single()
        assertNull(s15Item.thumbnailResId)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.EMPTY_NAME))
        assertFalse(viewModel.uiState.value.header.hasName)
        assertEquals(HeaderUiModel.DEFAULT_AVATAR_INITIAL, viewModel.uiState.value.header.avatarInitial)

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S18))
        val s18Items = (viewModel.uiState.value.decisionsSection as SectionState.Content).data
        assertEquals(listOf("X"), s18Items.map { it.id })

        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S19))
        val s19Room = (viewModel.uiState.value.livingRoom as SectionState.Content).data
        assertEquals("OCT", s19Room.calendarMonthText)
        assertEquals("25", s19Room.calendarDayText)
    }

    @Test
    fun scenario_S16_and_S22_enforceSingleActiveSheetAndPreserveSelectedTab() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S16))
        assertEquals(HomeTab.Home, viewModel.uiState.value.selectedTab)

        // Tap + twice then dismiss (Back)
        viewModel.onEvent(HomeUiEvent.GlobalAddTapped)
        assertEquals(HomeSheet.Global, viewModel.uiState.value.activeSheet)
        viewModel.onEvent(HomeUiEvent.GlobalAddTapped)
        assertEquals(HomeSheet.Global, viewModel.uiState.value.activeSheet)
        assertEquals(HomeTab.Home, viewModel.uiState.value.selectedTab)

        viewModel.onEvent(HomeUiEvent.DismissSheet)
        assertNull(viewModel.uiState.value.activeSheet)
        assertEquals(HomeTab.Home, viewModel.uiState.value.selectedTab)

        // S22 baseline starts with Global sheet open
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S22))
        assertEquals(HomeSheet.Global, viewModel.uiState.value.activeSheet)
    }

    @Test
    fun scenario_S17_disablesPrimaryActionWhenContextualEntityIsMissing() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S17))
        val sheet = viewModel.uiState.value.activeSheet as HomeSheet.Contextual
        assertTrue(sheet.isEntityMissing)

        // Primary action should be ignored when entity is missing, while DismissSheet works cleanly
        viewModel.onEvent(HomeUiEvent.ContextSheetPrimaryAction(ContextKind.Decision, "R1"))
        assertNotNull(viewModel.uiState.value.activeSheet)

        viewModel.onEvent(HomeUiEvent.DismissSheet)
        assertNull(viewModel.uiState.value.activeSheet)
    }

    @Test
    fun propTapped_and_directActions_and_observationActions_emitExpectedStateAndEffects() = runTest(testDispatcher) {
        // PropTapped opens Contextual sheet
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Hourglass))
        val propSheet = viewModel.uiState.value.activeSheet as HomeSheet.Contextual
        assertEquals(ContextKind.Hourglass, propSheet.kind)

        // QuickCheckTapped closes sheet and emits OpenQuickCheckFlow
        viewModel.onEvent(HomeUiEvent.QuickCheckTapped)
        runCurrent()
        assertNull(viewModel.uiState.value.activeSheet)
        assertEquals(HomeUiEffect.OpenQuickCheckFlow, viewModel.effect.first())

        // SaveItemTapped emits OpenSaveItemFlow
        viewModel.onEvent(HomeUiEvent.SaveItemTapped)
        runCurrent()
        assertEquals(HomeUiEffect.OpenSaveItemFlow, viewModel.effect.first())

        // Observation Explain & Hide
        viewModel.onEvent(HomeUiEvent.ObservationExplainTapped("O1"))
        runCurrent()
        assertEquals(HomeUiEffect.OpenObservationExplanation("O1"), viewModel.effect.first())

        viewModel.onEvent(HomeUiEvent.ObservationHideTapped("O1"))
        assertNull(viewModel.uiState.value.observation)
    }
}
