package com.hoabh.chanko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hoabh.chanko.presentation.screens.additem.AddItemScreen
import com.hoabh.chanko.presentation.screens.dashboard.DashboardScreen
import com.hoabh.chanko.presentation.screens.decision.DecisionScreen
import com.hoabh.chanko.presentation.screens.itemdetail.ItemDetailScreen
import com.hoabh.chanko.presentation.screens.splash.SplashScreen
import com.hoabh.chanko.presentation.screens.vault.VaultScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
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
            AddItemScreen(
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
            ItemDetailScreen(
                itemId = itemId,
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
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            DecisionScreen(
                itemId = itemId,
                onDecisionMade = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }

        composable(route = Screen.Vault.route) {
            VaultScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
