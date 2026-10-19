package com.hoabh.chanko.presentation.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.OverviewUiState
import com.hoabh.chanko.presentation.OverviewViewModel
import com.hoabh.chanko.presentation.components.ItemRow
import com.hoabh.chanko.presentation.components.formatMoney
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.presentation.navigation.MainNavigation
import com.hoabh.chanko.presentation.navigation.Screen
import com.hoabh.chanko.presentation.screens.setup.screen.SetupGate
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hoabh.chanko.ui.theme.ChankoTheme

fun NavGraphBuilder.dashboardDestination(
    sessionProvider: () -> SessionState.Available?,
    isRestored: () -> Boolean
) {
    composable(route = Screen.Dashboard.route) {
        if (!isRestored()) return@composable
        val session = sessionProvider() ?: return@composable
        key(session) {
            SetupGate(
                accountKey = session.account.value,
                onConfirmedComplete = { MainNavigation() }
            )
        }
    }
}

@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = hiltViewModel(),
    onNavigateToAddItem: () -> Unit = {},
    onNavigateToItemDetail: (Int) -> Unit = {}
) {
    val state by viewModel.items.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        modifier = modifier
    )
}

@Composable
fun DashboardScreen(
    state: OverviewUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Dashboard Screen")
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    ChankoTheme {
        DashboardScreen(state = OverviewUiState.Loading)
    }
}

@Preview(showBackground = true)
@Composable
private fun DarkDashboardScreenPreview() {
    ChankoTheme(darkTheme = true) {
        DashboardScreen(state = OverviewUiState.Loading)
    }
}
