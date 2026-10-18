package com.hoabh.chanko

import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.hoabh.chanko.domain.auth.AuthFailure
import com.hoabh.chanko.presentation.screens.login.*
import com.hoabh.chanko.ui.theme.ChankoTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun googleOnlyVietnameseContentAndLegalActions() {
        var google = 0; var terms = 0; var privacy = 0
        compose.setContent { ChankoTheme { LoginScreen(onGoogleLoginClick = { google++ }, onTermsClick = { terms++ }, onPrivacyClick = { privacy++ }) } }
        compose.onNodeWithText("Tiếp tục với Google").performScrollTo().performClick()
        compose.onNodeWithText("Điều khoản dịch vụ").performScrollTo().performClick()
        compose.onNodeWithText("Chính sách quyền riêng tư").performScrollTo().performClick()
        compose.onNodeWithText("Mật khẩu").assertDoesNotExist()
        compose.onAllNodesWithText("Nên mua\nkhông nhỉ?", useUnmergedTree = true).assertCountEquals(1)
        compose.runOnIdle { assertEquals(1, google); assertEquals(1, terms); assertEquals(1, privacy) }
    }
    @Test fun loadingDisablesGoogleAndAnnouncesProgress() {
        compose.setContent { ChankoTheme { LoginScreen(LoginUiState(isLoading = true), {}, {}, {}) } }
        compose.onNodeWithText("Tiếp tục với Google").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Đang đăng nhập…").performScrollTo().assertIsDisplayed()
    }
    @Test fun configurationErrorStaysOnLoginAndAllowsRetry() {
        compose.setContent { ChankoTheme { LoginScreen(LoginUiState(error = AuthFailure.NOT_CONFIGURED), {}, {}, {}) } }
        compose.onNodeWithText("Đăng nhập hiện chưa sẵn sàng. Vui lòng thử lại sau.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Tiếp tục với Google").performScrollTo().assertIsEnabled()
    }
    @Test fun largeTextCanScrollToEveryAction() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                ChankoTheme { LoginScreen(onGoogleLoginClick = {}, onTermsClick = {}, onPrivacyClick = {}) }
            }
        }
        compose.onNodeWithText("Tiếp tục với Google").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Điều khoản dịch vụ").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Chính sách quyền riêng tư").performScrollTo().assertIsDisplayed()
    }
    @Test fun captureLoginForVisualReview() {
        compose.setContent { ChankoTheme { LoginScreen(onGoogleLoginClick = {}, onTermsClick = {}, onPrivacyClick = {}) } }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.getExternalFilesDir(null), "login-review.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
