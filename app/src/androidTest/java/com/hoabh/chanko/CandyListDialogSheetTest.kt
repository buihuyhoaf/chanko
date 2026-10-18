@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hoabh.chanko

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CandyListDialogSheetTest {
    @get:Rule val compose = createComposeRule()

    @Test fun disabledCellSuppressesActionAndRetainsReadableContentAt200Percent() {
        var clicks = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ChankoTheme {
                    CandyListCell("Tên món đồ rất dài", Modifier.width(280.dp).testTag("cell"),
                        supportingText = "Giá dự kiến", value = "123.456.789 ₫",
                        enabled = false, onClick = { clicks++ })
                }
            }
        }
        compose.onNodeWithTag("cell").assertIsNotEnabled().assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp).performClick()
        compose.onNodeWithText("123.456.789 ₫").assertExists()
        compose.runOnIdle { assertEquals(0, clicks) }
    }

    @Test fun activeCellInvokesOnlyCallerAction() {
        var clicks = 0
        compose.setContent { ChankoTheme {
            CandyListCell("Chi tiết", onClick = { clicks++ })
        } }
        compose.onNodeWithText("Chi tiết").performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test fun confirmationInvokesCallerWithoutImplicitDismissal() {
        var confirmed = 0
        var dismissed = 0
        compose.setContent { ChankoTheme {
            CandyConfirmationDialog("Xác nhận", "Nội dung", "Tiếp tục", "Quay lại",
                { confirmed++ }, { dismissed++ }, errorMessage = "Vui lòng thử lại")
        } }
        compose.onNodeWithText("Vui lòng thử lại").assertExists()
        compose.onNodeWithText("Tiếp tục").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, confirmed); assertEquals(0, dismissed) }
        compose.onNodeWithText("Quay lại").performClick()
        compose.runOnIdle { assertEquals(1, dismissed) }
    }

    @Test fun pendingConfirmationDisablesBothActions() {
        var actions = 0
        compose.setContent { ChankoTheme {
            CandyConfirmationDialog("Xác nhận", "Nội dung", "Tiếp tục", "Quay lại",
                { actions++ }, { actions++ }, loading = true)
        } }
        compose.onNodeWithText("Tiếp tục", substring = true).assertIsNotEnabled().performClick()
        compose.onNodeWithText("Quay lại").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(0, actions) }
    }

    @Test fun sheetPreservesIndependentChildActions() {
        var clicks = 0
        compose.setContent { ChankoTheme {
            CandyBottomSheet("Thông tin", {}) {
                Text("Nội dung")
                CandyButton({ clicks++ }) { Text("Thao tác") }
            }
        } }
        compose.onNodeWithText("Thông tin").assertExists()
        compose.onNodeWithText("Thao tác").assertHasClickAction().performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
