package com.hoabh.chanko

import androidx.compose.runtime.SideEffect
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

    @Test fun databaseReadyOpensDashboardAndBackCannotReopenSplash() = verifyStartup(fail = false)
    @Test fun databaseFailureLeavesSplashAndShowsExistingError() = verifyStartup(fail = true)

    private fun verifyStartup(fail: Boolean) {
        val repository = StartupRepository()
        lateinit var navigation: NavHostController
        lateinit var model: OverviewViewModel
        compose.runOnUiThread { model = OverviewViewModel(repository) }
        compose.setContent {
            val controller = rememberNavController()
            SideEffect { navigation = controller }
            ChankoTheme { NavGraph(controller, overviewViewModel = model) }
        }
        compose.onNodeWithText("Đang mở dữ liệu của bạn…").assertIsDisplayed()
        compose.runOnIdle { assertEquals(Screen.Splash.route, navigation.currentDestination?.route) }
        compose.runOnIdle { repository.ready.value = !fail }
        compose.waitUntil(5000) { navigation.currentDestination?.route == Screen.Dashboard.route }
        compose.runOnIdle { assertNull(navigation.previousBackStackEntry) }
        if (fail) compose.onNodeWithText("Không thể tải dữ liệu. Hãy mở lại màn hình để thử lại.")
            .assertIsDisplayed()
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
