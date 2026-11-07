package com.hoabh.chanko.presentation.features.budget

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hoabh.chanko.presentation.features.budget.detail.BudgetDetailScreen

/**
 * Composable giao diện Ngân sách - ủy quyền sang [BudgetDetailScreen] (Budget v3).
 */
@Composable
fun BudgetScreen(
    state: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    BudgetDetailScreen(
        state = state,
        onEvent = onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}
