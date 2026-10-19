package com.hoabh.chanko.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hoabh.chanko.R
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

internal fun NavHostController.selectTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** This controller lives only inside the authenticated account subtree. */
@Composable
fun MainNavigation(controller: NavHostController = rememberNavController()) {
    val entry by controller.currentBackStackEntryAsState()
    Scaffold(bottomBar = {
        NavigationBar {
            TopLevelDestination.entries.forEach { tab ->
                NavigationBarItem(
                    selected = entry?.destination?.route == tab.route,
                    onClick = { controller.selectTab(tab) },
                    icon = { Icon(when (tab) {
                        TopLevelDestination.Home -> Icons.Rounded.Home
                        TopLevelDestination.MyItems -> Icons.AutoMirrored.Rounded.List
                        TopLevelDestination.Budget -> Icons.Rounded.AccountBalanceWallet
                        TopLevelDestination.Insights -> Icons.Rounded.Insights
                    }, contentDescription = null) },
                    label = { Text(stringResource(tab.labelRes)) },
                )
            }
        }
    }) { padding ->
        NavHost(controller, startDestination = TopLevelDestination.Home.route,
            modifier = Modifier.padding(padding)) {
            TopLevelDestination.entries.forEach { tab ->
                composable(tab.route) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.tab_not_implemented, stringResource(tab.labelRes)), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
