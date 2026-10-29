package com.hoabh.chanko.presentation.screens.budget.wallet

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.budget.BudgetScreen
import com.hoabh.chanko.presentation.screens.budget.BudgetUiEffect
import com.hoabh.chanko.presentation.screens.budget.BudgetUiEvent
import com.hoabh.chanko.presentation.screens.budget.BudgetViewModel
import kotlinx.coroutines.launch

/**
 * Route cho màn hình Ngân sách / Thiết lập ví.
 */
@Composable
fun WalletSettingsRoute(
    onBackClick: () -> Unit,
    expandHistory: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(expandHistory) {
        if (expandHistory && !state.isHistoryExpanded) {
            viewModel.onEvent(BudgetUiEvent.ToggleHistoryAccordion())
        }
    }

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is BudgetUiEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
            else -> Unit
        }
    }

    BudgetScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}
