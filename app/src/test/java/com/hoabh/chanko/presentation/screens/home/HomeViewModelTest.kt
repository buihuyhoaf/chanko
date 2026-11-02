package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
    fun `initialState loads Candy Pop 3D v1 baseline with 4 Little Room props and no open sheet`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value

        assertEquals("S21", state.scenarioId)
        assertTrue(state.header.hasName)
        assertEquals("Hoa", state.header.displayName)
        assertEquals("H", state.header.avatarInitial)
        assertFalse(state.header.isOffline)

        val livingRoom = (state.livingRoom as SectionState.Content).data
        assertNotNull(livingRoom.dialogue)
        assertEquals(4, livingRoom.props.size)
        assertEquals(
            listOf("Đang chờ", "Cần xem lại", "Ngân sách", "Đã bỏ qua"),
            livingRoom.props.map { it.label }
        )
        // Wallet prop does not have a count badge
        assertNull(livingRoom.props.first { it.kind == RoomPropKind.Wallet }.badgeText)

        val budget = (state.budgetSection as SectionState.Content).data
        assertEquals(600_000L, budget.remainingMinor)
        assertEquals(80, budget.percentageInt)
        assertEquals("Đã chi 80%", budget.progressRightLabel)

        val decisions = (state.decisionsSection as SectionState.Content).data
        assertEquals(listOf("J1", "H1", "K1"), decisions.map { it.id })
        assertTrue(state.isObservationVisible)
        assertNull(state.activeSheet)
    }

    @Test
    fun `AC-04 and AC-05 tapping all 4 Little Room props opens corresponding Contextual Sheet and supports empty state`() = runTest(testDispatcher) {
        // 1. Hourglass -> Đang chờ
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Hourglass))
        val hourglassSheet = viewModel.uiState.value.activeSheet
        assertNotNull(hourglassSheet)
        assertEquals(ContextKind.Hourglass, hourglassSheet?.kind)
        assertEquals("Đang chờ", hourglassSheet?.title)
        assertFalse(hourglassSheet!!.isEmpty)

        // 2. Calendar -> Cần xem lại
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Calendar))
        val calendarSheet = viewModel.uiState.value.activeSheet
        assertEquals(ContextKind.Calendar, calendarSheet?.kind)
        assertEquals("Cần xem lại", calendarSheet?.title)

        // 3. Wallet -> Ngân sách
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Wallet))
        val walletSheet = viewModel.uiState.value.activeSheet
        assertEquals(ContextKind.Wallet, walletSheet?.kind)
        assertEquals("Ngân sách", walletSheet?.title)

        // 4. PauseJar -> Đã bỏ qua
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.PauseJar))
        val pauseJarSheet = viewModel.uiState.value.activeSheet
        assertEquals(ContextKind.PauseJar, pauseJarSheet?.kind)
        assertEquals("Đã bỏ qua", pauseJarSheet?.title)

        // AC-05: Empty state when group has 0 items (Scenario S05)
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S05))
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Hourglass))
        val emptySheet = viewModel.uiState.value.activeSheet
        assertNotNull(emptySheet)
        assertTrue(emptySheet!!.isEmpty)
        assertEquals("0", emptySheet.highlightValue)
    }

    @Test
    fun `AC-06 primary action in Contextual Sheet navigates to MyItems with matching filter or BudgetScreen`() = runTest(testDispatcher) {
        // Hourglass -> WAITING filter
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Hourglass))
        viewModel.onEvent(HomeUiEvent.ContextSheetPrimaryAction(ContextKind.Hourglass))
        assertEquals(HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.WAITING), viewModel.effect.first())
        assertNull(viewModel.uiState.value.activeSheet)

        // Calendar -> REVIEW filter
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Calendar))
        viewModel.onEvent(HomeUiEvent.ContextSheetPrimaryAction(ContextKind.Calendar))
        assertEquals(HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.REVIEW), viewModel.effect.first())

        // PauseJar -> SKIPPED filter
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.PauseJar))
        viewModel.onEvent(HomeUiEvent.ContextSheetPrimaryAction(ContextKind.PauseJar))
        assertEquals(HomeUiEffect.NavigateToMyItems(HomeMyItemsFilter.SKIPPED), viewModel.effect.first())

        // Wallet -> BudgetScreen
        viewModel.onEvent(HomeUiEvent.PropTapped(RoomPropKind.Wallet))
        viewModel.onEvent(HomeUiEvent.ContextSheetPrimaryAction(ContextKind.Wallet))
        assertEquals(HomeUiEffect.NavigateToBudgetDetails, viewModel.effect.first())
    }

    @Test
    fun `AC-07 and AC-08 cooling-off expiration reconciles Waiting into Review without mutating verdict to BUY`() = runTest(testDispatcher) {
        // S21 baseline has 1 Review item (J1) and 2 Waiting items (H1: 66240s, K1: 22200s)
        assertEquals(1, viewModel.uiState.value.reviewItems.size)
        assertEquals(2, viewModel.uiState.value.waitingItems.size)

        // Advance time by 30_000 seconds so K1 expires
        viewModel.onEvent(HomeUiEvent.ReconcileCoolingOff(elapsedSeconds = 30_000L))

        val state = viewModel.uiState.value
        assertEquals(2, state.reviewItems.size)
        assertEquals(1, state.waitingItems.size)

        val expiredK1 = state.reviewItems.first { it.id == "K1" }
        assertEquals(DecisionStatus.Ready, expiredK1.status)
        assertEquals("Vừa hết thời gian chờ", expiredK1.statusLabel)
        assertEquals(DecisionVerdict.WAIT, expiredK1.verdict)

        // Verify Little Room prop badges updated in sync without double counting
        val props = (state.livingRoom as SectionState.Content).data.props
        assertEquals("1", props.first { it.kind == RoomPropKind.Hourglass }.badgeText)
        assertEquals("2", props.first { it.kind == RoomPropKind.Calendar }.badgeText)
    }

    @Test
    fun `AC-10 tapping decision tile emits NavigateToDecisionDetail with exact itemId`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.DecisionTapped("H1"))
        assertEquals(HomeUiEffect.NavigateToDecisionDetail("H1"), viewModel.effect.first())
    }

    @Test
    fun `AC-12 and AC-13 BudgetCard tap opens BudgetDetails and CycleChanged updates snapshot immediately`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.BudgetDetailsTapped)
        assertEquals(HomeUiEffect.NavigateToBudgetDetails, viewModel.effect.first())

        viewModel.onEvent(
            HomeUiEvent.CycleChanged(
                cycleLabel = "01/11 – 30/11 (còn 28 ngày)",
                spentMinor = 1_500_000L,
                limitMinor = 5_000_000L
            )
        )
        val updatedBudget = (viewModel.uiState.value.budgetSection as SectionState.Content).data
        assertEquals("01/11 – 30/11 (còn 28 ngày)", updatedBudget.cycleLabel)
        assertEquals(3_500_000L, updatedBudget.remainingMinor)
        assertEquals(30, updatedBudget.percentageInt)
    }

    @Test
    fun `AC-14 Observation explain opens Contextual Sheet on Home and hidden insightId does not reappear on refresh`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.ObservationExplainTapped("O1"))
        val sheet = viewModel.uiState.value.activeSheet
        assertNotNull(sheet)
        assertEquals(ContextKind.Observation, sheet?.kind)
        assertEquals("Góc nhìn từ Chanko", sheet?.title)
        assertTrue(sheet?.explanationDetail?.isNotBlank() == true)

        // Hide insight O1
        viewModel.onEvent(HomeUiEvent.ObservationHideTapped("O1"))
        assertFalse(viewModel.uiState.value.isObservationVisible)
        assertNull(viewModel.uiState.value.activeSheet)

        // Refresh / reload S21 or S09 (which also uses id "O1") -> stays hidden!
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S21))
        assertFalse(viewModel.uiState.value.isObservationVisible)
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S09))
        assertFalse(viewModel.uiState.value.isObservationVisible)
    }

    @Test
    fun `AC-15 section error is isolated and recovers on retry`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S13))
        val state = viewModel.uiState.value
        assertEquals("Hoa", state.header.displayName)
        assertTrue(state.budgetSection is SectionState.Error)
        assertTrue(state.decisionsSection is SectionState.Loading)

        viewModel.onEvent(HomeUiEvent.RetrySection(HomeSection.Budget))
        assertTrue(viewModel.uiState.value.budgetSection is SectionState.Content)
    }

    @Test
    fun `AC-16 and AC-17 offline mode displays sync timestamp and blocks GlobalAddTapped without creating draft`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S14))
        val state = viewModel.uiState.value
        assertTrue(state.header.isOffline)
        assertEquals("Đồng bộ lần cuối: 09:30 hôm nay", state.header.lastSyncedAtText)
        assertTrue((state.budgetSection as SectionState.Content).isFromCache)

        // Tapping + while offline emits ShowOfflineBlockedMessage, does not open Add Item or sheet
        viewModel.onEvent(HomeUiEvent.GlobalAddTapped)
        val effect = viewModel.effect.first()
        assertTrue(effect is HomeUiEffect.ShowOfflineBlockedMessage)
        assertNull(viewModel.uiState.value.activeSheet)
    }

    @Test
    fun `scenario S15 handles long user name and missing item price as Chua co gia`() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.S15))
        assertEquals("Nguyễn Hoàng Minh Anh", viewModel.uiState.value.header.displayName)
        val item = (viewModel.uiState.value.decisionsSection as SectionState.Content).data.single()
        assertNull(item.priceMinor)
        assertEquals("Chưa có giá", item.priceFormatted)
    }
}
