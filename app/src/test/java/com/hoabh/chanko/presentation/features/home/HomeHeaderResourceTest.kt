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
}
