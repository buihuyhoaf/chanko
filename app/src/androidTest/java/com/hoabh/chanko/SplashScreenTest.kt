package com.hoabh.chanko

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.hoabh.chanko.presentation.screens.splash.SplashContent
import com.hoabh.chanko.presentation.screens.splash.SplashScreen
import com.hoabh.chanko.ui.theme.ChankoTheme
import android.graphics.Bitmap
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun waitsForReadinessWithoutTimerAndUsesLatestCallbackOnlyOnce() {
        compose.mainClock.autoAdvance = false
        var first = 0
        var latest = 0
        val ready = mutableStateOf(false)
        val useLatest = mutableStateOf(false)
        compose.setContent {
            ChankoTheme { SplashScreen(ready.value, if (useLatest.value) ({ latest++ }) else ({ first++ })) }
        }
        compose.mainClock.advanceTimeBy(10000)
        compose.runOnIdle { assertEquals(0, first + latest); useLatest.value = true }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { ready.value = true }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { assertEquals(0, first); assertEquals(1, latest) }
        compose.mainClock.advanceTimeBy(10000)
        compose.runOnIdle { assertEquals(1, latest) }
    }

    @Test fun alreadyReadyDoesNotWaitForBranding() {
        var calls = 0
        compose.setContent { ChankoTheme { SplashScreen(true) { calls++ } } }
        compose.runOnIdle { assertEquals(1, calls) }
        compose.onNodeWithText("Đang mở dữ liệu của bạn…").assertDoesNotExist()
    }

    @Test fun largeTextRemainsAccessibleWithoutDecorativeImageAnnouncement() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ChankoTheme { SplashContent() }
            }
        }
        compose.onNodeWithText("Should I", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Buy It?", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Pause. Think. Decide.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Đang mở dữ liệu của bạn…").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kiểm soát chi tiêu bắt đầu từ một quyết định tốt hơn")
            .performScrollTo().assertIsDisplayed()
        compose.onAllNodes(hasContentDescription("Chanko Logo")).assertCountEquals(0)
    }

    @Test fun staticPresentationCanBeCapturedForVisualReview() {
        compose.setContent { ChankoTheme { Box(Modifier.fillMaxSize()) { SplashContent() } } }
        compose.onNodeWithText("Pause. Think. Decide.").assertIsDisplayed()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "splash-review.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
