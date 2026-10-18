package com.hoabh.chanko

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hoabh.chanko.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CandyComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loadingButtonRetainsLabelAndBlocksClicks() {
        var clicks = 0
        compose.setContent { ChankoTheme {
            CandyPrimaryButton({ clicks++ }, Modifier.testTag("button"), loading = true) { Text("Lưu") }
        } }
        compose.onNodeWithTag("button").assertIsNotEnabled().assertHeightIsAtLeast(56.dp)
            .assertWidthIsAtLeast(48.dp)
        compose.onNodeWithText("Lưu", substring = true).assertExists()
        compose.onNodeWithTag("button").performClick()
        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test fun filterExposesSelectionAndMinimumBounds() {
        compose.setContent { ChankoTheme { CandyFilterChip("Đã chọn", true, {}, Modifier.testTag("chip")) } }
        compose.onNodeWithTag("chip").assertIsSelected().assertHasClickAction()
            .assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
    }

    @Test fun disabledCardBlocksAction() {
        var clicks = 0
        compose.setContent { ChankoTheme {
            CandyActionCard({ clicks++ }, Modifier.testTag("card"), enabled = false) { Text("Xem") }
        } }
        compose.onNodeWithTag("card").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test fun primaryExpandsForLargeMultilineText() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ChankoTheme { CandyPrimaryButton({}, Modifier.testTag("large")) { Text("Lưu món đồ\nvà tiếp tục cân nhắc") } }
            }
        }
        compose.onNodeWithTag("large").assertHeightIsAtLeast(96.dp)
        compose.onNodeWithText("Lưu món đồ\nvà tiếp tục cân nhắc").assertExists()
    }
}
