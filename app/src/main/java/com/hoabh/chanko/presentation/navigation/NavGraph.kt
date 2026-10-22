package com.hoabh.chanko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.presentation.screens.dashboard.dashboardDestination
import com.hoabh.chanko.presentation.screens.login.screen.LoginRoute
import com.hoabh.chanko.presentation.screens.login.screen.loginDestination
import com.hoabh.chanko.presentation.screens.setup.screen.setupDestination
import com.hoabh.chanko.presentation.screens.splash.splashDestination

/** Các route công khai không yêu cầu phiên đăng nhập */
private val UnauthenticatedRoutes = setOf(
    Screen.Splash.route,
    Screen.Login.route,
    Screen.Setup.route,
    Screen.Dashboard.route
)

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    loginContent: @Composable (() -> Unit) -> Unit = { LoginRoute(it) },
    session: LoginSessionViewModel = hiltViewModel()
) {
    val identity by session.session.collectAsStateWithLifecycle()
    val restored by session.restorationComplete.collectAsStateWithLifecycle()
    val authenticated = identity is SessionState.Available
    val entry by navController.currentBackStackEntryAsState()

    LaunchedEffect(entry, authenticated, restored) {
        if (!restored) return@LaunchedEffect

        val currentRoute = entry?.destination?.route
        if (authenticated && currentRoute == Screen.Login.route) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(navController.graph.id)
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        // TODO(dummy): Cho phép truy cập Screen.Setup khi chưa có session authenticated chính thức
        if (!authenticated && currentRoute != null && currentRoute !in UnauthenticatedRoutes) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        splashDestination(
            isRestored = { restored },
            onSplashFinished = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )

        loginDestination(
            loginContent = loginContent,
            onLoginSuccess = {
                // TODO(dummy): Navigate tới màn SetupGate khi logic onLoginSuccess pass (tạm thời cho đến khi kết nối backend/OAuth)
                navController.navigate(Screen.Setup.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        setupDestination(
            accountKeyProvider = {
                // TODO(dummy): Hiển thị màn SetupGate độc lập với accountKey giả lập trước khi có session thật từ server
                (identity as? SessionState.Available)?.account?.value ?: "dummy_account"
            },
            onConfirmedComplete = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Setup.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        dashboardDestination(
            sessionProvider = { identity as? SessionState.Available },
            isRestored = { restored }
        )
    }
}
