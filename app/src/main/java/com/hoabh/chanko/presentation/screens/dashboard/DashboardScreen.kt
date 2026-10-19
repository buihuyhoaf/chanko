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
import com.hoabh.chanko.ui.theme.ChankoTheme

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
