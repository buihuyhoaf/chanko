package com.hoabh.chanko

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.hoabh.chanko.presentation.navigation.NavGraph
import com.hoabh.chanko.presentation.navigation.Screen
import com.hoabh.chanko.ui.theme.ChankoTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SplashNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun splashOpensLoginAndRequiresAuthentication() = verifyStartup()

    private fun verifyStartup() {
        val auth = NavigationAuth()
        val session = com.hoabh.chanko.presentation.navigation.LoginSessionViewModel(auth)
        lateinit var navigation: NavHostController
        compose.setContent {
            val controller = rememberNavController()
            SideEffect { navigation = controller }
            ChankoTheme { NavGraph(controller, session = session, loginContent = { onSuccess ->
                androidx.compose.foundation.layout.Column {
                androidx.compose.material3.Button(onClick = onSuccess) { androidx.compose.material3.Text("Callback only") }
                androidx.compose.material3.Button(onClick = { auth.accept() }) { androidx.compose.material3.Text("Xác thực từ server") }
                }
            }) }
        }
        compose.onNodeWithText("Đang mở dữ liệu của bạn…").assertIsDisplayed()
        compose.runOnIdle { assertEquals(Screen.Splash.route, navigation.currentDestination?.route) }
        compose.waitUntil(5000) { navigation.currentDestination?.route == Screen.Login.route }
        compose.runOnIdle { assertNull(navigation.previousBackStackEntry) }
        // A restored or programmatically requested protected route cannot bypass Google.
        compose.runOnIdle { navigation.navigate(Screen.Dashboard.route) }
        compose.waitUntil(5000) { navigation.currentDestination?.route == Screen.Login.route }
        compose.runOnIdle { assertNull(navigation.previousBackStackEntry) }
        // Callback only triggers dummy navigation to SetupGate
        compose.onNodeWithText("Callback only").performClick()
        compose.runOnIdle { assertEquals(Screen.Setup.route, navigation.currentDestination?.route) }
        // SetupGate is displayed with status error
        compose.onNodeWithText("Chưa thể xác nhận thiết lập").assertIsDisplayed()
        compose.onNodeWithText("Thử lại").performClick()
        compose.onNodeWithText("Chưa thể xác nhận thiết lập").assertIsDisplayed()
        compose.runOnIdle { auth.accept("other-account") }
        compose.runOnIdle { navigation.navigate(Screen.Dashboard.route) }
        compose.waitUntil(5000) { navigation.currentDestination?.route == Screen.Dashboard.route }
        compose.runOnIdle { assertNull(navigation.previousBackStackEntry) }
        // Authentication alone cannot grant Home while setup status is unverified.
        compose.onNodeWithText("Chưa thể xác nhận thiết lập").assertIsDisplayed()
        compose.onNodeWithText("Thử lại").performClick()
        compose.onNodeWithText("Chưa thể xác nhận thiết lập").assertIsDisplayed()
        compose.runOnIdle { auth.accept("other-account") }
        compose.onNodeWithText("Chưa thể xác nhận thiết lập").assertIsDisplayed()
        compose.runOnIdle { auth.session.value = com.hoabh.chanko.domain.auth.SessionState.SignedOut }
        compose.waitUntil(5000) { navigation.currentDestination?.route == Screen.Login.route }
        compose.runOnIdle { assertNull(navigation.previousBackStackEntry) }
    }
}

private class NavigationAuth : com.hoabh.chanko.domain.auth.AuthenticationRepository {
    override val session = MutableStateFlow<com.hoabh.chanko.domain.auth.SessionState>(com.hoabh.chanko.domain.auth.SessionState.SignedOut)
    fun accept(account: String = "test") { session.value = com.hoabh.chanko.domain.auth.SessionState.Available(com.hoabh.chanko.domain.repository.foundation.AccountScope(account)) }
    override suspend fun restore() = com.hoabh.chanko.domain.auth.AuthResult.Success(Unit)
    override suspend fun signIn(provider: com.hoabh.chanko.domain.auth.GoogleTokenProvider) = com.hoabh.chanko.domain.auth.AuthResult.Success(Unit)
    override suspend fun refresh() = com.hoabh.chanko.domain.auth.AuthResult.Success(Unit)
    override suspend fun logout() = com.hoabh.chanko.domain.auth.AuthResult.Success(com.hoabh.chanko.domain.auth.Revocation.CONFIRMED)
}
