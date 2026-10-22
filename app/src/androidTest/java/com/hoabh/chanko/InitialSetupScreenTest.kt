package com.hoabh.chanko

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.hoabh.chanko.presentation.screens.setup.data.SetupPage
import com.hoabh.chanko.presentation.screens.setup.data.SetupProblem
import com.hoabh.chanko.presentation.screens.setup.data.SetupUiState
import com.hoabh.chanko.presentation.screens.setup.screen.InitialSetupScreen
import com.hoabh.chanko.presentation.screens.setup.screen.SetupStatusScreen
import com.hoabh.chanko.ui.theme.ChankoTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class InitialSetupScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun vietnameseFormHasNoDefaultDayAndBackPreservesBudget() {
        var state by mutableStateOf(SetupUiState(page = SetupPage.FORM))
        compose.setContent { ChankoTheme {
            InitialSetupScreen(
                state,
                { state = state.copy(amount = it) },
                { state = state.copy(day = it) },
                { state = state.copy(step = 2) },
                { state = state.copy(step = 1) },
                {})
        } }
        screenshot("budget-vi.png")
        compose.onNodeWithContentDescription("Ngân sách mua sắm mỗi tháng").performTextInput("3000000")
        compose.onNodeWithText("Tiếp tục").performScrollTo().performClick()
        screenshot("day-vi.png")
        compose.onNodeWithText("Chọn ngày từ 1 đến 31").performScrollTo().performClick()
        compose.onNodeWithTag("setup_days").performScrollToNode(hasText("Ngày 31"))
        compose.onNodeWithText("Ngày 31").performClick()
        compose.onNodeWithText("Ngày 31").assertExists()
        compose.onNodeWithContentDescription("Quay lại").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Ngân sách mua sắm mỗi tháng").assertTextContains("3.000.000")
        compose.runOnIdle { assertEquals(31, state.day) }
    }
    @Test fun doubleTextScaleStillReachesWarningAndSave() {
        var pressed = false
        compose.setContent { ChankoTheme {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                InitialSetupScreen(
                    SetupUiState(
                        page = SetupPage.FORM,
                        step = 2,
                        amount = "3000000"
                    ), {}, {}, {}, {}, { pressed = true })
            }
        } }
        compose.onNodeWithText("Ngày này sẽ được giữ cố định.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Lưu và bắt đầu").performScrollTo().performClick()
        screenshot("day-font200-vi.png")
        compose.runOnIdle { assertTrue(pressed) }
    }
    @Test fun unknownGateOnlyOffersRetry() {
        var retries = 0
        compose.setContent { ChankoTheme {
            SetupStatusScreen(
                SetupUiState(page = SetupPage.STATUS_ERROR, problem = SetupProblem.UNKNOWN),
                { retries++ })
        } }
        compose.onNodeWithText("Thử lại").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1,retries) }
        compose.onNodeWithText("Home").assertDoesNotExist()
    }
    private fun screenshot(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val folder = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)!!
        java.io.File(folder, name).outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

}
