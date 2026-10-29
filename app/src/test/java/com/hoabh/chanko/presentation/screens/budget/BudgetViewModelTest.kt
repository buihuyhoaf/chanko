package com.hoabh.chanko.presentation.screens.budget

import androidx.lifecycle.SavedStateHandle
import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.data.budget.InMemorySharedItemResolver
import com.hoabh.chanko.domain.budget.model.CardColor
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
    @Test
    fun `equivalent budget formatting does not prompt discard but invalid edits do`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("5000000"))
        assertFalse(viewModel.uiState.value.isCurrentSheetDirty)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("6000000"))
        assertTrue(viewModel.uiState.value.isCurrentSheetDirty)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("invalid"))
        assertTrue(viewModel.uiState.value.isCurrentSheetDirty)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft(""))
        assertTrue(viewModel.uiState.value.isCurrentSheetDirty)
    }

    @Test
    fun `conflict reload waits for cold overview and resets draft only once`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("6000000"))
        // The existing subscriber has not processed this external update yet.
        repository.simulateExternalBudgetUpdate(repository.currentCycleId, 7_000_000L)
        viewModel.onEvent(BudgetUiEvent.ReloadConflict)
        assertEquals("6000000", viewModel.uiState.value.editBudgetDraft)
        assertTrue(viewModel.uiState.value.isLoading)
        advanceUntilIdle()
        assertEquals("7.000.000", viewModel.uiState.value.editBudgetDraft)
        assertEquals(2, viewModel.uiState.value.editBudgetBaseRevision)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("8000000"))
        repository.simulateExternalBudgetUpdate(repository.currentCycleId, 9_000_000L)
        advanceUntilIdle()
        assertEquals("8000000", viewModel.uiState.value.editBudgetDraft)
        assertEquals(2, viewModel.uiState.value.editBudgetBaseRevision)
    }

    @Test
    fun `failed conflict reload preserves draft and base revision`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.UpdateEditBudgetDraft("6000000"))
        repository.simulateLoadErrorOnNextFetch = true
        viewModel.onEvent(BudgetUiEvent.ReloadConflict)
        advanceUntilIdle()
        assertEquals("6000000", viewModel.uiState.value.editBudgetDraft)
        assertEquals(1, viewModel.uiState.value.editBudgetBaseRevision)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `OpenRenameCreditCard sets dialog state, prefill, and initializes flags`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val cards = repository.getCreditCards().first()
        val targetCard = cards.first()

        viewModel.onEvent(BudgetUiEvent.OpenRenameCreditCard(targetCard))

        val state = viewModel.uiState.value
        assertTrue(state.isRenameCardDialogOpen)
        assertEquals(targetCard, state.cardToRename)
        assertEquals(targetCard.name, state.renameCardDraft)
        assertEquals(targetCard.name, state.renameCardInitialText)
        assertFalse(state.isRenameCardDirty)
        assertFalse(state.canSubmitRename) // No-change -> disabled
        assertNull(state.renameCardInlineError)
    }

    @Test
    fun `UpdateRenameCardDraft validates 30 graphemes, reserved Pay Now, duplicate, and self case change`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val cards = repository.getCreditCards().first()
        val targetCard = cards[0] // "Thẻ Techcombank"
        val otherCard = cards[1] // "Thẻ VPBank Cake"

        viewModel.onEvent(BudgetUiEvent.OpenRenameCreditCard(targetCard))

        // 1. > 30 graphemes -> Error
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft("A".repeat(31)))
        assertEquals("Tên thẻ tối đa 30 ký tự", viewModel.uiState.value.renameCardInlineError)
        assertFalse(viewModel.uiState.value.canSubmitRename)

        // 2. Pay Now -> Error
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft("Pay Now"))
        assertEquals("Tên này dành cho Pay Now", viewModel.uiState.value.renameCardInlineError)
        assertFalse(viewModel.uiState.value.canSubmitRename)

        // 3. Duplicate other card -> Error
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft(otherCard.name))
        assertEquals("Tên thẻ đã tồn tại", viewModel.uiState.value.renameCardInlineError)
        assertFalse(viewModel.uiState.value.canSubmitRename)

        // 4. Case-only change of self -> Allowed
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft(targetCard.name.lowercase()))
        assertNull(viewModel.uiState.value.renameCardInlineError)
        assertTrue(viewModel.uiState.value.canSubmitRename)

        // 5. Valid unique new name -> Allowed
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft("Thẻ Chi Tiêu Riêng"))
        assertNull(viewModel.uiState.value.renameCardInlineError)
        assertTrue(viewModel.uiState.value.isRenameCardDirty)
        assertTrue(viewModel.uiState.value.canSubmitRename)
    }

    @Test
    fun `DismissRenameCreditCardDialog checks dirty state and prompts discard dialog`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val card = repository.getCreditCards().first().first()
        viewModel.onEvent(BudgetUiEvent.OpenRenameCreditCard(card))

        // Unchanged dismiss -> closes immediately
        viewModel.onEvent(BudgetUiEvent.DismissRenameCreditCardDialog)
        assertFalse(viewModel.uiState.value.isRenameCardDialogOpen)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)

        // Re-open and modify -> dismiss triggers discard dialog
        viewModel.onEvent(BudgetUiEvent.OpenRenameCreditCard(card))
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft("Tên Mới"))
        assertTrue(viewModel.uiState.value.isRenameCardDirty)

        viewModel.onEvent(BudgetUiEvent.DismissRenameCreditCardDialog)
        assertTrue(viewModel.uiState.value.isRenameCardDialogOpen)
        assertTrue(viewModel.uiState.value.isDiscardDialogOpen)

        // Confirm discard -> closes dialog and cleans draft
        viewModel.onEvent(BudgetUiEvent.ConfirmDiscardChanges)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)
        assertFalse(viewModel.uiState.value.isRenameCardDialogOpen)
        assertNull(viewModel.uiState.value.cardToRename)
    }

    @Test
    fun `SubmitRenameCreditCard updates repository and closes dialog on success`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val card = repository.getCreditCards().first().first()
        viewModel.onEvent(BudgetUiEvent.OpenRenameCreditCard(card))
        viewModel.onEvent(BudgetUiEvent.UpdateRenameCardDraft("Thẻ Ngân Hàng Mới"))

        viewModel.onEvent(BudgetUiEvent.SubmitRenameCreditCard(card.id, "Thẻ Ngân Hàng Mới"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRenameCardDialogOpen)
        assertNull(state.cardToRename)

        val updatedCards = repository.getCreditCards().first()
        val renamed = updatedCards.first { it.id == card.id }
        assertEquals("Thẻ Ngân Hàng Mới", renamed.name)
    }

    @Test
    fun `OpenAddCreditCard initializes dialog, empty draft, and default PURPLE color`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)

        val state = viewModel.uiState.value
        assertTrue(state.isAddCardDialogOpen)
        assertEquals("", state.addCardDraft)
        assertEquals(CardColor.PURPLE, state.addCardColor)
        assertFalse(state.isAddCardDirty)
        assertFalse(state.canSubmitAddCard)
        assertNull(state.addCardInlineError)
    }

    @Test
    fun `UpdateAddCardDraft and SelectAddCardColor validate rules and dirty state`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val existingCard = repository.getCreditCards().first().first()
        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)

        // 1. Select color marks dirty
        viewModel.onEvent(BudgetUiEvent.SelectAddCardColor(CardColor.PINK))
        assertEquals(CardColor.PINK, viewModel.uiState.value.addCardColor)
        assertTrue(viewModel.uiState.value.isAddCardDirty)

        // 2. > 30 chars -> Error
        viewModel.onEvent(BudgetUiEvent.UpdateAddCardDraft("A".repeat(31)))
        assertEquals("Tên thẻ tối đa 30 ký tự", viewModel.uiState.value.addCardInlineError)
        assertFalse(viewModel.uiState.value.canSubmitAddCard)

        // 3. Reserved Pay Now -> Error
        viewModel.onEvent(BudgetUiEvent.UpdateAddCardDraft("Pay Now"))
        assertEquals("Tên này dành cho Pay Now", viewModel.uiState.value.addCardInlineError)
        assertFalse(viewModel.uiState.value.canSubmitAddCard)

        // 4. Duplicate name -> Error
        viewModel.onEvent(BudgetUiEvent.UpdateAddCardDraft(existingCard.name))
        assertEquals("Tên thẻ đã tồn tại", viewModel.uiState.value.addCardInlineError)
        assertFalse(viewModel.uiState.value.canSubmitAddCard)

        // 5. Valid unique name -> canSubmitAddCard = true
        viewModel.onEvent(BudgetUiEvent.UpdateAddCardDraft("Thẻ Mới Đẹp"))
        assertNull(viewModel.uiState.value.addCardInlineError)
        assertTrue(viewModel.uiState.value.canSubmitAddCard)
    }

    @Test
    fun `DismissAddCreditCardDialog triggers discard when dirty and closes on confirm`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)

        // Clean dismiss -> closes immediately
        viewModel.onEvent(BudgetUiEvent.DismissAddCreditCardDialog)
        assertFalse(viewModel.uiState.value.isAddCardDialogOpen)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)

        // Dirty dismiss (only color changed) -> triggers discard dialog
        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)
        viewModel.onEvent(BudgetUiEvent.SelectAddCardColor(CardColor.MINT))
        assertTrue(viewModel.uiState.value.isAddCardDirty)

        viewModel.onEvent(BudgetUiEvent.DismissAddCreditCardDialog)
        assertTrue(viewModel.uiState.value.isAddCardDialogOpen)
        assertTrue(viewModel.uiState.value.isDiscardDialogOpen)

        // Confirm discard -> closes dialog and resets
        viewModel.onEvent(BudgetUiEvent.ConfirmDiscardChanges)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)
        assertFalse(viewModel.uiState.value.isAddCardDialogOpen)
    }

    @Test
    fun `SubmitAddCreditCard saves card with chosen color and closes dialog`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)
        viewModel.onEvent(BudgetUiEvent.UpdateAddCardDraft("Thẻ Sinh Viên"))
        viewModel.onEvent(BudgetUiEvent.SelectAddCardColor(CardColor.YELLOW))

        viewModel.onEvent(BudgetUiEvent.SubmitAddCreditCard("Thẻ Sinh Viên", CardColor.YELLOW))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAddCardDialogOpen)
        assertEquals("", state.addCardDraft)

        val cards = repository.getCreditCards().first()
        val created = cards.firstOrNull { it.name == "Thẻ Sinh Viên" }
        assertNotNull(created)
        assertEquals(CardColor.YELLOW, created?.color)
    }
}
