package com.hoabh.chanko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.OverviewUiState
import com.hoabh.chanko.presentation.OverviewViewModel
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.presentation.screens.splash.SplashScreen
import com.hoabh.chanko.presentation.screens.login.LoginRoute
import com.hoabh.chanko.presentation.screens.setup.screen.SetupGate

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    overviewViewModel: OverviewViewModel = hiltViewModel(),
    loginContent: @Composable (() -> Unit) -> Unit = { LoginRoute(it) },
    session: LoginSessionViewModel = hiltViewModel()
) {
    val identity by session.session.collectAsStateWithLifecycle()
    val restored by session.restorationComplete.collectAsStateWithLifecycle()
    val authenticated = identity is com.hoabh.chanko.domain.auth.SessionState.Available
    val entry by navController.currentBackStackEntryAsState()
    LaunchedEffect(entry, authenticated, restored) {
        if (!restored) return@LaunchedEffect
        if (authenticated && entry?.destination?.route == Screen.Login.route) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(navController.graph.id)
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        val route = entry?.destination?.route
        // TODO(dummy): Cho phép truy cập Screen.Setup khi chưa có session authenticated chính thức
        if (!authenticated && route != null && route != Screen.Splash.route && route != Screen.Login.route && route != Screen.Setup.route) {
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
        composable(route = Screen.Splash.route,
            enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None }) {
            val state by overviewViewModel.items.collectAsStateWithLifecycle()
            SplashScreen(
                isReady = restored && state !is OverviewUiState.Loading,
                onSplashFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            loginContent {
                // TODO(dummy): Navigate tới màn SetupGate khi logic onLoginSuccess pass (tạm thời cho đến khi kết nối backend/OAuth)
                navController.navigate(Screen.Setup.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(route = Screen.Setup.route) {
            // TODO(dummy): Hiển thị màn SetupGate độc lập với accountKey giả lập trước khi có session thật từ server
            val accountKey = (identity as? SessionState.Available)?.account?.value ?: "dummy_account"
            SetupGate(
                accountKey = accountKey,
                onConfirmedComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.Dashboard.route) {
            if (!restored || !authenticated) return@composable
            // Account changes discard all remembered/saved tab stacks from the prior account.
            key(identity) {
                SetupGate(
                    accountKey = (identity as SessionState.Available).account.value,
                    onConfirmedComplete = { MainNavigation() },
                )
            }
        }
    }
}
