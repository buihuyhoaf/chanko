package com.hoabh.chanko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.ItemsState
import com.hoabh.chanko.presentation.OverviewViewModel
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hoabh.chanko.presentation.screens.additem.AddItemRoute
import com.hoabh.chanko.presentation.screens.dashboard.DashboardRoute
import com.hoabh.chanko.presentation.screens.decision.DecisionRoute
import com.hoabh.chanko.presentation.screens.itemdetail.ItemDetailRoute
import com.hoabh.chanko.presentation.screens.splash.SplashScreen
import com.hoabh.chanko.presentation.screens.login.LoginRoute
import com.hoabh.chanko.presentation.screens.vault.VaultRoute

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    overviewViewModel: OverviewViewModel = hiltViewModel(),
    loginContent: @Composable (() -> Unit) -> Unit = { LoginRoute(it) },
    session: LoginSessionViewModel = viewModel()
) {
    val authenticated by session.authenticated.collectAsStateWithLifecycle()
    val entry by navController.currentBackStackEntryAsState()
    LaunchedEffect(entry, authenticated) {
        val route = entry?.destination?.route
        if (!authenticated && route != null && route != Screen.Splash.route && route != Screen.Login.route) {
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
                isReady = state !is ItemsState.Loading,
                onSplashFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            loginContent {
                session.backendAccepted()
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(route = Screen.Dashboard.route, enterTransition = {
            if (initialState.destination.route == Screen.Login.route) EnterTransition.None else null
        }) {
            if (!authenticated) return@composable
            DashboardRoute(
                viewModel = overviewViewModel,
                onNavigateToAddItem = {
                    navController.navigate(Screen.AddItem.route)
                },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onNavigateToVault = {
                    navController.navigate(Screen.Vault.route)
                }
            )
        }

        composable(route = Screen.AddItem.route) {
            if (!authenticated) return@composable
            AddItemRoute(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            if (!authenticated) return@composable
            ItemDetailRoute(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDecision = {
                    navController.navigate(Screen.Decision.createRoute(itemId))
                }
            )
        }

        composable(
            route = Screen.Decision.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) {
            if (!authenticated) return@composable
            DecisionRoute(
                onDecisionMade = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Vault.route) {
            if (!authenticated) return@composable
            VaultRoute(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
