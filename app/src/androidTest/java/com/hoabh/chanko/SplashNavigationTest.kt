package com.hoabh.chanko

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.OverviewViewModel
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

    @Test fun databaseReadyOpensLoginAndRequiresAuthentication() = verifyStartup(fail = false)
    @Test fun databaseFailureStillRequiresAuthentication() = verifyStartup(fail = true)

    private fun verifyStartup(fail: Boolean) {
        val repository = StartupRepository()
        val auth = NavigationAuth()
        val session = com.hoabh.chanko.presentation.navigation.LoginSessionViewModel(auth)
        lateinit var navigation: NavHostController
        lateinit var model: OverviewViewModel
        compose.runOnUiThread { model = OverviewViewModel(repository) }
        compose.setContent {
            val controller = rememberNavController()
            SideEffect { navigation = controller }
            ChankoTheme { NavGraph(controller, overviewViewModel = model, session = session, loginContent = { onSuccess ->
                androidx.compose.foundation.layout.Column {
                androidx.compose.material3.Button(onClick = onSuccess) { androidx.compose.material3.Text("Callback only") }
                androidx.compose.material3.Button(onClick = { auth.accept() }) { androidx.compose.material3.Text("Xác thực từ server") }
                }
            }) }
        }
        compose.onNodeWithText("Đang mở dữ liệu của bạn…").assertIsDisplayed()
        compose.runOnIdle { assertEquals(Screen.Splash.route, navigation.currentDestination?.route) }
        compose.runOnIdle { repository.ready.value = !fail }
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

/** Withholds the real first emission, then returns either empty local data or a read error. */
private class StartupRepository : ItemRepository {
    val ready = MutableStateFlow<Boolean?>(null)
    override fun getActiveItems(): Flow<List<CooldownItem>> = ready.filterNotNull().map {
        check(it) { "Read failed" }; emptyList()
    }
    override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> =
        ready.filterNotNull().map { emptyList() }
    override fun getTotalSavedAmount(): Flow<Long> = ready.filterNotNull().map { 0L }
    override fun getItemById(id: Int): Flow<CooldownItem?> = ready.map { null }
    override suspend fun insertItem(item: CooldownItem) = error("Not used")
    override suspend fun updateItem(item: CooldownItem) = error("Not used")
    override suspend fun deleteItem(item: CooldownItem) = error("Not used")
    override suspend fun updateItemStatus(id: Int, status: ItemStatus) = error("Not used")
}

private class NavigationAuth : com.hoabh.chanko.domain.auth.AuthenticationRepository {
    override val session = MutableStateFlow<com.hoabh.chanko.domain.auth.SessionState>(com.hoabh.chanko.domain.auth.SessionState.SignedOut)
    fun accept(account: String = "test") { session.value = com.hoabh.chanko.domain.auth.SessionState.Available(com.hoabh.chanko.domain.repository.foundation.AccountScope(account)) }
    override suspend fun restore() = com.hoabh.chanko.domain.auth.AuthResult.Success(Unit)
    override suspend fun signIn(provider: com.hoabh.chanko.domain.auth.GoogleTokenProvider) = com.hoabh.chanko.domain.auth.AuthResult.Success(Unit)
    override suspend fun refresh() = com.hoabh.chanko.domain.auth.AuthResult.Success(Unit)
    override suspend fun logout() = com.hoabh.chanko.domain.auth.AuthResult.Success(com.hoabh.chanko.domain.auth.Revocation.CONFIRMED)
}
