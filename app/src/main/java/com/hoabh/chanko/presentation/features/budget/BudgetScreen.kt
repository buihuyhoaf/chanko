package com.hoabh.chanko.presentation.features.budget

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hoabh.chanko.presentation.features.budget.detail.BudgetDetailScreen
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryScreen

/**
 * @deprecated Đã phân tách rõ ràng thành [BudgetDetailScreen] (chi tiết một chu kỳ)
 * và [BudgetGalleryScreen] (danh sách các tháng theo năm).
 * Vui lòng sử dụng trực tiếp [BudgetDetailScreen] hoặc [BudgetGalleryScreen] tùy theo mục đích điều hướng.
 */
@Deprecated(
    message = "Sử dụng BudgetDetailScreen hoặc BudgetGalleryScreen thay thế để tường minh mục đích sử dụng.",
    replaceWith = ReplaceWith(
        "BudgetDetailScreen(state = state, onEvent = onEvent, onBackClick = onBackClick, modifier = modifier, snackbarHostState = snackbarHostState)",
        "com.hoabh.chanko.presentation.features.budget.detail.BudgetDetailScreen"
    ),
    level = DeprecationLevel.WARNING
)
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
