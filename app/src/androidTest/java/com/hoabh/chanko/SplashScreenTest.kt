package com.hoabh.chanko

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.hoabh.chanko.presentation.screens.splash.SplashContent
import com.hoabh.chanko.presentation.screens.splash.SplashScreen
import com.hoabh.chanko.ui.theme.ChankoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun finishesOnceAfterExistingDelayAndUsesLatestCallback() {
        compose.mainClock.autoAdvance = false
        var first = 0
        var latest = 0
        val useLatest = mutableStateOf(false)
        compose.setContent {
            ChankoTheme { SplashScreen(if (useLatest.value) ({ latest++ }) else ({ first++ })) }
        }
        compose.mainClock.advanceTimeBy(900)
        compose.runOnIdle { useLatest.value = true }
        compose.mainClock.advanceTimeBy(100)
        compose.runOnIdle { assertEquals(0, first + latest) }
        compose.mainClock.advanceTimeBy(900)
        compose.runOnIdle { assertEquals(0, first); assertEquals(1, latest) }
        compose.mainClock.advanceTimeBy(2000)
        compose.runOnIdle { assertEquals(1, latest) }
    }

    @Test fun largeTextRemainsAccessibleWithoutDecorativeImageAnnouncement() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ChankoTheme { SplashContent() }
            }
        }
        compose.onNodeWithText("Chanko").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Hạ nhiệt mua sắm • Tiết kiệm thông minh")
            .performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Bình tĩnh trước mỗi quyết định chi tiêu")
            .performScrollTo().assertIsDisplayed()
        compose.onAllNodes(hasContentDescription("Chanko Logo")).assertCountEquals(0)
    }
}
