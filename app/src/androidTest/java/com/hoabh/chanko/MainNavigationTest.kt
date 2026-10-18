package com.hoabh.chanko

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.hoabh.chanko.presentation.navigation.MainNavigation
import com.hoabh.chanko.presentation.navigation.TopLevelDestination
import com.hoabh.chanko.ui.theme.ChankoTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MainNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fourTabsReselectAndBackReturnHomeWithoutDuplicateEntries() {
        lateinit var controller: NavHostController
        compose.setContent {
            val nav = rememberNavController()
            SideEffect { controller = nav }
            ChankoTheme { MainNavigation(nav) }
        }
        TopLevelDestination.entries.forEach { tab ->
            compose.onNodeWithText(tab.label).performClick().assertIsSelected()
            compose.onNodeWithText("${tab.label} — Chưa triển khai").assertIsDisplayed()
            compose.onNodeWithText(tab.label).performClick()
        }
        compose.runOnIdle {
            assertTrue(controller.popBackStack())
            assertEquals(TopLevelDestination.Home.route, controller.currentDestination?.route)
            assertNull(controller.previousBackStackEntry)
        }
    }

    @Test fun savedNavigationRestoresSelectedTabAndCanStillSwitch() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { ChankoTheme { MainNavigation() } }
        compose.onNodeWithText("My Items").performClick()
        compose.onNodeWithText("Budget").performClick()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Budget").assertIsSelected()
        compose.onNodeWithText("Budget — Chưa triển khai").assertIsDisplayed()
        compose.onNodeWithText("My Items").performClick().assertIsSelected()
        compose.onNodeWithText("My Items — Chưa triển khai").assertIsDisplayed()
    }
}
