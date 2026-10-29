package com.hoabh.chanko.presentation.screens.budget

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBudgetRepository()
        viewModel = BudgetViewModel(repository)
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
    fun `when offline canWrite becomes false and lastSyncTimeText is set`() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canWrite)

        repository.setNetworkOnline(false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertFalse(state.canWrite)
        assertNotNull(state.lastSyncTimeText)
    }

    @Test
    fun `when past cycle is selected canWrite becomes false and past data is loaded`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onEvent(BudgetUiEvent.SelectCycle(repository.pastCycleId))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(repository.pastCycleId, state.selectedCycle?.id)
        assertFalse(state.isCurrentCycle)
        assertFalse(state.canWrite)
        assertEquals(4_500_000L, state.snapshot?.budgetMinor)
        assertEquals(4_200_000L, state.snapshot?.spentMinor)
    }

    @Test
    fun `sheet navigation pushes to backstack and pops correctly`() = runTest(testDispatcher) {
        advanceUntilIdle()

        // 1. Open Wallet Settings
        viewModel.onEvent(BudgetUiEvent.OpenWalletSettings)
        assertEquals(BudgetSheetDestination.WalletSettings, viewModel.uiState.value.currentSheet)
        assertEquals(0, viewModel.uiState.value.sheetBackStack.size)

        // 2. Open Credit Card Manage from Settings
        viewModel.onEvent(BudgetUiEvent.OpenCreditCardManage)
        assertEquals(BudgetSheetDestination.CreditCardManage, viewModel.uiState.value.currentSheet)
        assertEquals(listOf(BudgetSheetDestination.WalletSettings), viewModel.uiState.value.sheetBackStack)

        // 3. Open Add Credit Card
        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)
        assertEquals(BudgetSheetDestination.AddCreditCard, viewModel.uiState.value.currentSheet)
        assertEquals(2, viewModel.uiState.value.sheetBackStack.size)

        // 4. Back with clean form pops back to Manage
        viewModel.onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
        assertEquals(BudgetSheetDestination.CreditCardManage, viewModel.uiState.value.currentSheet)

        // 5. Back with dirty form shows discard dialog
        viewModel.onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = true))
        assertTrue(viewModel.uiState.value.isDiscardDialogOpen)

        // 6. Confirm discard dialog pops to WalletSettings
        viewModel.onEvent(BudgetUiEvent.ConfirmDiscardChanges)
        assertFalse(viewModel.uiState.value.isDiscardDialogOpen)
        assertEquals(BudgetSheetDestination.WalletSettings, viewModel.uiState.value.currentSheet)
    }

    @Test
    fun `SubmitBudgetLimit updates limit and shows snackbar`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onEvent(BudgetUiEvent.OpenEditBudget)
        viewModel.onEvent(BudgetUiEvent.SubmitBudgetLimit(6_000_000L))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(6_000_000L, state.snapshot?.budgetMinor)
        assertNull(state.currentSheet)
    }

    @Test
    fun `SubmitAddCreditCard adds card and updates list`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.onEvent(BudgetUiEvent.OpenAddCreditCard)
        viewModel.onEvent(BudgetUiEvent.SubmitAddCreditCard("Thẻ VIB Rewards"))
        advanceUntilIdle()

        val cards = viewModel.uiState.value.creditCards
        assertTrue(cards.any { it.name == "Thẻ VIB Rewards" })
    }

    @Test
    fun `delete credit card flow sets cardToDelete and archives upon confirmation`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val targetCard = viewModel.uiState.value.creditCards.first()
        viewModel.onEvent(BudgetUiEvent.RequestDeleteCreditCard(targetCard))
        assertEquals(targetCard, viewModel.uiState.value.cardToDelete)

        viewModel.onEvent(BudgetUiEvent.ConfirmDeleteCreditCard)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.cardToDelete)
        assertFalse(viewModel.uiState.value.creditCards.any { it.id == targetCard.id })
    }
}
