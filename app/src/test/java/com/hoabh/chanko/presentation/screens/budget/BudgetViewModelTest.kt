package com.hoabh.chanko.presentation.screens.budget

import androidx.lifecycle.SavedStateHandle
import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.data.budget.InMemorySharedItemResolver
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeBudgetRepository
    private lateinit var sharedItemResolver: InMemorySharedItemResolver
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBudgetRepository()
        sharedItemResolver = InMemorySharedItemResolver(mutableSetOf("1", "2", "3", "item_1", "item_2", "item_3"))
        savedStateHandle = SavedStateHandle()
        viewModel = BudgetViewModel(repository, sharedItemResolver, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads current cycle with full snapshot and canWrite true`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.selectedCycle)
        assertTrue(state.isCurrentCycle)
        assertTrue(state.isOnline)
        assertTrue(state.canWrite)
        assertNotNull(state.snapshot)
        assertEquals(5_000_000L, state.snapshot?.budgetMinor)
        assertEquals(3_000_000L, state.snapshot?.spentMinor)
        assertEquals(2_000_000L, state.snapshot?.remainingMinor)
        assertEquals(2, state.paymentMethods.size)
        assertEquals(3, state.notableItems.size)
        assertEquals(3, state.recentActivities.size)
    }

    @Test
    fun `when offline canWrite becomes false and lastSyncTimeText uses dataUpdatedAt timestamp`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canWrite)
        val expectedDataUpdatedAt = viewModel.uiState.value.dataUpdatedAt
        assertNotNull(expectedDataUpdatedAt)

        repository.setNetworkOnline(false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertFalse(state.canWrite)
        assertEquals("Kết nối mạng để chỉnh sửa", state.writeDisabledReason)
        assertNotNull(state.lastSyncTimeText)
        assertEquals(expectedDataUpdatedAt, state.dataUpdatedAt)
    }

    @Test
    fun `when past cycle is selected canWrite becomes false and past data is loaded atomically`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.pastCycleId))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(repository.pastCycleId, state.selectedCycle?.id)
        assertFalse(state.isCurrentCycle)
        assertFalse(state.canWrite)
        assertEquals("Chỉ chỉnh sửa trong chu kỳ hiện tại", state.writeDisabledReason)
        assertEquals(4_500_000L, state.snapshot?.budgetMinor)
        assertEquals(4_200_000L, state.snapshot?.spentMinor)
    }

    @Test
    fun `cycle switch load error clears previous snapshot so old cycle data is never mixed with new cycle header`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.snapshot)

        repository.simulateLoadErrorOnNextFetch = true
        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.pastCycleId))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(repository.pastCycleId, state.selectedCycle?.id)
        assertNull(state.snapshot)
        assertEquals("Chưa tải được dữ liệu ngân sách", state.errorMessage)

        // Retry recovers cleanly
        viewModel.onEvent(BudgetUiEvent.Retry)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.snapshot)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `selecting uncached cycle while offline blocks selection and shows snackbar`() = runTest(testDispatcher) {
        advanceUntilIdle()
        repository.setNetworkOnline(false)
        advanceUntilIdle()

        val effectDeferred = async { viewModel.effect.first() }
        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.pastUncachedCycleId))
        advanceUntilIdle()

        // Selected cycle remains current cycle
        assertEquals(repository.currentCycleId, viewModel.uiState.value.selectedCycle?.id)
        val effect = effectDeferred.await()
        assertTrue(effect is BudgetUiEffect.ShowSnackbar)
        assertEquals("Chưa có dữ liệu ngoại tuyến", (effect as BudgetUiEffect.ShowSnackbar).message)
    }

    @Test
    fun `NotableItemClicked navigates when item exists and shows offline message when unavailable`() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Valid item "1" -> navigates to shared Item Detail routeId = 1
        val navEffectDeferred = async { viewModel.effect.first() }
        viewModel.onEvent(BudgetUiEvent.NotableItemClicked("1"))
        advanceUntilIdle()
        val navEffect = navEffectDeferred.await()
        assertTrue(navEffect is BudgetUiEffect.NavigateToSharedItemDetail)
        assertEquals(1, (navEffect as BudgetUiEffect.NavigateToSharedItemDetail).routeId)

        // Missing item "item_999" -> shows snackbar "Món đồ chưa có dữ liệu ngoại tuyến"
        val snackbarDeferred = async { viewModel.effect.first() }
        viewModel.onEvent(BudgetUiEvent.NotableItemClicked("item_999"))
        advanceUntilIdle()
        val snackbarEffect = snackbarDeferred.await()
        assertTrue(snackbarEffect is BudgetUiEffect.ShowSnackbar)
        assertEquals("Món đồ chưa có dữ liệu ngoại tuyến", (snackbarEffect as BudgetUiEffect.ShowSnackbar).message)
    }

    @Test
    fun `state restoration via SavedStateHandle preserves selectedCycleId and drafts across recreation`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.pastCycleId))
        advanceUntilIdle()
        assertEquals(repository.pastCycleId, savedStateHandle.get<String>(BudgetViewModel.KEY_SELECTED_CYCLE_ID))

        // Switch back to current cycle and type a draft in EditBudget
        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.currentCycleId))
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("7.500.000"))
        assertEquals("7.500.000", savedStateHandle.get<String>(BudgetViewModel.KEY_EDIT_BUDGET_DRAFT))

        // Select past cycle and recreate ViewModel with the same SavedStateHandle
        viewModel.onEvent(BudgetUiEvent.ConfirmDiscardChanges)
        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.pastCycleId))
        advanceUntilIdle()

        val recreatedViewModel = BudgetViewModel(repository, sharedItemResolver, savedStateHandle)
        advanceUntilIdle()

        assertEquals(repository.pastCycleId, recreatedViewModel.uiState.value.selectedCycle?.id)
        assertEquals(4_500_000L, recreatedViewModel.uiState.value.snapshot?.budgetMinor)
    }

    @Test
    fun `RequestCloseCurrentSheet checks dirty draft and prompts DiscardChangesDialog`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        assertFalse(viewModel.uiState.value.isCurrentSheetDirty)

        // Modify draft -> becomes dirty
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("6.500.000"))
        assertTrue(viewModel.uiState.value.isCurrentSheetDirty)

        // Scrim tap / swipe / back -> RequestCloseCurrentSheet opens DiscardChangesDialog instead of closing
        viewModel.onEvent(BudgetUiEvent.RequestCloseCurrentSheet)
        assertTrue(viewModel.uiState.value.isDiscardDialogOpen)
        assertNotNull(viewModel.uiState.value.currentSheet)

        // Continue editing -> closes dialog, keeps sheet & draft
        viewModel.onEvent(BudgetUiEvent.CancelDiscardChanges)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)
        assertEquals("6.500.000", viewModel.uiState.value.editBudgetDraft)

        // Confirm discard -> closes dialog and sheet
        viewModel.onEvent(BudgetUiEvent.RequestCloseCurrentSheet)
        viewModel.onEvent(BudgetUiEvent.ConfirmDiscardChanges)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)
        assertNull(viewModel.uiState.value.currentSheet)
    }

    @Test
    fun `pending lock prevents closing sheet while mutation is in flight`() = runTest(testDispatcher) {
        advanceUntilIdle()
        repository.simulatedDelayMs = 500L

        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("6000000"))
        viewModel.onEvent(BudgetUiEvent.SubmitBudgetLimit(6_000_000L))

        // Advance partially so coroutine starts and sets isSubmitting = true
        advanceTimeBy(100L)
        assertTrue(viewModel.uiState.value.isSubmitting)

        // Attempt to dismiss or press back while submitting -> ignored
        viewModel.onEvent(BudgetUiEvent.RequestCloseCurrentSheet)
        viewModel.onEvent(BudgetUiEvent.DismissSheet)
        assertNotNull(viewModel.uiState.value.currentSheet)

        // Finish submission
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertNull(viewModel.uiState.value.currentSheet)
        assertEquals(6_000_000L, viewModel.uiState.value.snapshot?.budgetMinor)
    }

    @Test
    fun `conflict during SubmitBudgetLimit shows inline conflict and ReloadConflict updates base and draft`() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Open EditBudget when revision = 1, limit = 5_000_000
        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("6.000.000"))

        // External update changes snapshot to 7_000_000 (revision = 2)
        repository.simulateExternalBudgetUpdate(repository.currentCycleId, 7_000_000L)
        advanceUntilIdle()

        // Submitting against base revision 1 triggers BudgetConflictException
        viewModel.onEvent(BudgetUiEvent.SubmitBudgetLimit(6_000_000L))
        advanceUntilIdle()

        val conflictState = viewModel.uiState.value
        assertTrue(conflictState.hasEditBudgetConflict)
        assertEquals("Ngân sách đã thay đổi. Tải lại để chỉnh sửa.", conflictState.editBudgetInlineError)
        assertNotNull(conflictState.currentSheet)

        // User taps "Tải lại" (ReloadConflict) -> base updates to 7_000_000 (rev 2), draft resets to "7.000.000"
        viewModel.onEvent(BudgetUiEvent.ReloadConflict)
        advanceUntilIdle()

        val reloadedState = viewModel.uiState.value
        assertFalse(reloadedState.hasEditBudgetConflict)
        assertNull(reloadedState.editBudgetInlineError)
        assertEquals(2, reloadedState.editBudgetBaseRevision)
        assertEquals("7.000.000", reloadedState.editBudgetDraft)
    }

    @Test
    fun `delete credit card error keeps dialog open with inline error and retry succeeds`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val targetCard = viewModel.uiState.value.creditCards.first()
        viewModel.onEvent(BudgetUiEvent.RequestDeleteCreditCard(targetCard))
        assertEquals(targetCard, viewModel.uiState.value.cardToDelete)

        // Simulate save error on first delete attempt
        repository.simulateSaveErrorOnNextMutation = true
        viewModel.onEvent(BudgetUiEvent.ConfirmDeleteCreditCard)
        advanceUntilIdle()

        // Dialog stays open and shows inline error
        assertEquals(targetCard, viewModel.uiState.value.cardToDelete)
        assertEquals("Chưa xóa được thẻ. Vui lòng thử lại.", viewModel.uiState.value.deleteCardInlineError)

        // Retry succeeds and closes dialog
        viewModel.onEvent(BudgetUiEvent.ConfirmDeleteCreditCard)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.cardToDelete)
        assertNull(viewModel.uiState.value.deleteCardInlineError)
        assertFalse(viewModel.uiState.value.creditCards.any { it.id == targetCard.id })
    }
}
