package com.hoabh.chanko.presentation.screens.budget

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import kotlinx.coroutines.launch

/**
 * Route cho phân hệ Ngân sách (BudgetDetails route).
 * Hiển thị trực tiếp màn hình Ngân sách chuẩn v2.1:
 * Top app bar đồng bộ 100% phong cách Món đồ (MyItemsScreen), tích hợp trực tiếp 3 Section:
 * Ngân sách mua sắm, Phương thức thanh toán, Lịch sử điều chỉnh.
 */
@Composable
fun BudgetRoute(
    onBackClick: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit = {},
    onNavigateToWalletSettings: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is BudgetUiEffect.NavigateToSharedItemDetail -> {
                onNavigateToItemDetail(effect.routeId)
            }
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
