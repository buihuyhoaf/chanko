package com.hoabh.chanko.presentation.screens.budget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetHost
import com.hoabh.chanko.presentation.screens.budget.sheets.DeleteCardConfirmDialog
import com.hoabh.chanko.presentation.screens.budget.sheets.DiscardChangesDialog
import com.hoabh.chanko.ui.theme.CandyColors
import kotlinx.coroutines.launch

@Composable
fun BudgetRoute(
    onBackClick: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is BudgetUiEffect.NavigateToSharedItemDetail -> {
                val id = effect.itemId.toIntOrNull()
                if (id != null) {
                    onNavigateToItemDetail(id)
                }
            }
            is BudgetUiEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    // Intercept back press when sheet is open
    BackHandler {
        if (state.currentSheet != null) {
            viewModel.onEvent(BudgetUiEvent.BackPressedInSheet(isDirty = false))
        } else {
            onBackClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        BudgetScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onBackClick = onBackClick
        )

        // Single Sheet Host
        BudgetSheetHost(
            destination = state.currentSheet,
            state = state,
            onEvent = viewModel::onEvent
        )

        // C08 Delete Card Dialog
        state.cardToDelete?.let { card ->
            DeleteCardConfirmDialog(
                card = card,
                onConfirm = { viewModel.onEvent(BudgetUiEvent.ConfirmDeleteCreditCard) },
                onDismiss = { viewModel.onEvent(BudgetUiEvent.DismissDeleteCreditCardDialog) }
            )
        }

        // Discard Changes Dialog
        if (state.isDiscardDialogOpen) {
            DiscardChangesDialog(
                onConfirmDiscard = { viewModel.onEvent(BudgetUiEvent.ConfirmDiscardChanges) },
                onContinueEditing = { viewModel.onEvent(BudgetUiEvent.CancelDiscardChanges) }
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
