package com.hoabh.chanko.presentation.features.budget

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.domain.budget.model.BudgetSetupState
import com.hoabh.chanko.domain.budget.model.BudgetTemporalRole
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryUiEffect
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryUiEvent
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetGalleryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeBudgetRepository
    private lateinit var viewModel: BudgetGalleryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBudgetRepository()
        viewModel = BudgetGalleryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads year 2026 with months 09 to 12 and annual total 18M`() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2026, state.selectedYear)
        assertEquals(4, state.months.size)
        assertEquals(listOf(9, 10, 11, 12), state.months.map { it.yearMonth.monthValue })
        assertEquals(18_000_000L, state.annualConfiguredTotalMinor)
        assertEquals(3, state.configuredMonthsCount)
        assertFalse(state.canNavigateToPreviousYear)
        assertTrue(state.canNavigateToNextYear)

        collectJob.cancel()
    }

    @Test
    fun `NextYear shifts to 2027 and loads all 12 months`() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(BudgetGalleryUiEvent.NextYear)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2027, state.selectedYear)
        assertEquals(12, state.months.size)
        assertTrue(state.canNavigateToPreviousYear)

        collectJob.cancel()
    }

    @Test
    fun `PreviousYear on 2026 does not change year`() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(BudgetGalleryUiEvent.PreviousYear)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2026, state.selectedYear)

        collectJob.cancel()
    }

    @Test
    fun `MonthCardClick on Past or Current month emits NavigateToDetail effect`() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val month11 = viewModel.uiState.value.months.first { it.yearMonth.monthValue == 11 }
        assertEquals(BudgetTemporalRole.Current, month11.temporalRole)

        var emittedEffect: BudgetGalleryUiEffect? = null
        val effectJob = launch {
            viewModel.effect.collect { emittedEffect = it }
        }

        viewModel.onEvent(BudgetGalleryUiEvent.MonthCardClick(month11))
        advanceUntilIdle()

        assertTrue(emittedEffect is BudgetGalleryUiEffect.NavigateToDetail)
        assertEquals(YearMonth.of(2026, 11), (emittedEffect as BudgetGalleryUiEffect.NavigateToDetail).yearMonth)

        effectJob.cancel()
        collectJob.cancel()
    }

    @Test
    fun `MonthCardClick on Future month opens FutureBudgetSheet with suggested limit`() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val month12 = viewModel.uiState.value.months.first { it.yearMonth.monthValue == 12 }
        assertEquals(BudgetTemporalRole.Future, month12.temporalRole)

        viewModel.onEvent(BudgetGalleryUiEvent.MonthCardClick(month12))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.activeFutureSheetMonth)
        assertEquals(YearMonth.of(2026, 12), state.activeFutureSheetMonth?.yearMonth)
        assertEquals(6_000_000L, state.suggestedLimitForFuture)

        collectJob.cancel()
    }

    @Test
    fun `SaveFutureBudget updates month 12 and recalculates annual total to 24M`() = runTest {
        val collectJob = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(BudgetGalleryUiEvent.SaveFutureBudget(YearMonth.of(2026, 12), 6_000_000L))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.activeFutureSheetMonth)
        assertEquals(24_000_000L, state.annualConfiguredTotalMinor)
        assertEquals(4, state.configuredMonthsCount)

        val month12 = state.months.first { it.yearMonth.monthValue == 12 }
        assertEquals(BudgetSetupState.Configured, month12.setupState)
        assertEquals(6_000_000L, month12.limitMinor)

        collectJob.cancel()
    }
}
