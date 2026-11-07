package com.hoabh.chanko.presentation.features.home

import com.hoabh.chanko.domain.rewards.DefaultLevelProgressionTable
import com.hoabh.chanko.domain.rewards.ResourceBalance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeHeaderResourceTest {

    @Test
    fun testHeaderUiModelHoldsResourceProperties() {
        val balance = ResourceBalance(hatBalance = 150L, pateBalance = 250L, currentLevel = 3)
        val fraction = DefaultLevelProgressionTable.calculateProgressFraction(balance.pateBalance)

        val header = HeaderUiModel(
            userName = "Bùi Hoa",
            hatBalance = balance.hatBalance,
            pateBalance = balance.pateBalance,
            currentLevel = balance.currentLevel,
            pateProgressFraction = fraction
        )

        assertEquals(150L, header.hatBalance)
        assertEquals(250L, header.pateBalance)
        assertEquals(3, header.currentLevel)
        assertEquals(0.0f, header.pateProgressFraction, 0.001f) // Level 3 starts at 250
    }

    @Test
    fun testResourceSheetToggleInState() {
        var state = HomeUiState()
        assertFalse(state.isResourceSheetOpen)

        // Simulate open
        state = state.copy(isResourceSheetOpen = true)
        assertTrue(state.isResourceSheetOpen)

        // Simulate dismiss
        state = state.copy(isResourceSheetOpen = false)
        assertFalse(state.isResourceSheetOpen)
    }

    @Test
    fun testHeaderUiModelHasCheckedInTodayProperty() {
        val headerDefault = HeaderUiModel(userName = "Hoa")
        assertFalse(headerDefault.hasCheckedInToday)

        val headerCheckedIn = HeaderUiModel(userName = "Hoa", hasCheckedInToday = true)
        assertTrue(headerCheckedIn.hasCheckedInToday)
    }

    @Test
    fun testCollapsedHeaderMinHeightAccommodatesChipsRow() {
        // Sticky header retains the chips row (hạt, level, check-in +10) even when collapsed
        val minHeight = com.hoabh.chanko.presentation.features.home.components.HomeHeaderTokens.CollapsedMinHeight
        assertTrue("CollapsedMinHeight must accommodate chips row (>= 80dp)", minHeight.value >= 80f)
    }

    @Test
    fun testHomeCarouselDisplaysNovemberCreditCardAsCurrent() {
        val fakeRepo = com.hoabh.chanko.data.budget.FakeBudgetRepository()
        assertEquals("cycle_2026_11", fakeRepo.currentCycleId)

        val fakeSource = HomeFakeDataSource()
        val defaultState = fakeSource.getStateForScenario(HomeScenario.DEFAULT)
        val carousel = (defaultState.budgetCarouselState as SectionState.Content).data
        val currentSlot = carousel.slots.first { it.role == BudgetCardRole.Current } as BudgetCardSlotUi.ActiveCard
        assertEquals("11", currentSlot.monthLabel)
        assertEquals("Tháng 11/2026", currentSlot.cycleLabel)
        assertEquals(10, currentSlot.themeOrdinal)
        assertEquals("Pistachio", com.hoabh.chanko.ui.theme.CandyBudgetThemes.getTheme(currentSlot.themeOrdinal).name)
    }

    @Test
    fun testCurrentMonthCardRolesIdentifyCurrentMonthForBorder() {
        val fakeSource = HomeFakeDataSource()
        val defaultState = fakeSource.getStateForScenario(HomeScenario.DEFAULT)
        val carousel = (defaultState.budgetCarouselState as SectionState.Content).data
        val currentSlot = carousel.slots.first { it.role == BudgetCardRole.Current }
        assertTrue(currentSlot is BudgetCardSlotUi.ActiveCard)
        assertEquals("11", currentSlot.monthLabel)

        val noBudgetState = fakeSource.getStateForScenario(HomeScenario.NO_BUDGET)
        val noBudgetCarousel = (noBudgetState.budgetCarouselState as SectionState.Content).data
        val currentEmptySlot = noBudgetCarousel.slots.first { it.role == BudgetCardRole.Current }
        assertTrue(currentEmptySlot is BudgetCardSlotUi.CurrentEmptyCard)
        assertEquals("11", currentEmptySlot.monthLabel)
    }
}
