package com.hoabh.chanko.presentation.screens.budget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetHost
import com.hoabh.chanko.presentation.screens.budget.sheets.DiscardChangesDialog
import com.hoabh.chanko.presentation.screens.budget.wallet.components.AdjustmentHistorySection
import com.hoabh.chanko.presentation.screens.budget.wallet.components.BudgetSettingsSection
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Màn hình Ngân sách (BudgetScreen).
 * 1. Top App Bar: Tiêu đề "Ngân sách" 24sp Bold và Menu tùy chọn.
 * 2. Section 1: Ngân sách mua sắm (Đã chi, Còn lại, % tiến độ, Chu kỳ, Sửa ngân sách).
 * 3. Section 2: Lịch sử điều chỉnh ngân sách (Timeline Tăng/Giảm hạn mức).
 */
@Composable
fun BudgetScreen(
    state: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Bắt phím Back phần cứng / hệ thống
    BackHandler {
        when {
            state.isSubmitting -> Unit
            state.isDiscardDialogOpen -> onEvent(BudgetUiEvent.CancelDiscardChanges)
            state.currentSheet != null -> onEvent(BudgetUiEvent.RequestCloseCurrentSheet)
            else -> onBackClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.budget_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = CandyColors.textPrimary
                )

                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_10,
                            contentDescription = stringResource(R.string.budget_menu_options)
                        )
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.budget_wallet_action_edit_budget)) },
                            leadingIcon = {
                                ChankoIcon(
                                    id = ChankoIconId.ACT_05,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            enabled = state.canWrite,
                            onClick = {
                                isMenuExpanded = false
                                onEvent(BudgetUiEvent.OpenEditBudget)
                            }
                        )
                    }
                }
            }

            // 2. Nội dung cuộn chính
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = CandySpace.lg,
                    end = CandySpace.lg,
                    top = CandySpace.xs,
                    bottom = 96.dp // Để không bị BottomBar che khuất
                ),
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // Section 1: Ngân sách mua sắm
                item {
                    BudgetSettingsSection(
                        selectedCycle = state.selectedCycle,
                        snapshot = state.snapshot,
                        canWrite = state.canWrite,
                        onCycleClick = { onEvent(BudgetUiEvent.OpenCyclePicker) }
                    )
                }

                // Section 2: Lịch sử điều chỉnh ngân sách
                item {
                    AdjustmentHistorySection(
                        adjustments = state.adjustments,
                        cycleRange = state.selectedCycle?.dateRangeText ?: "",
                        isExpanded = state.isHistoryExpanded,
                        onToggleExpanded = { onEvent(BudgetUiEvent.ToggleHistoryAccordion()) }
                    )
                }
            }
        }

        // Bottom Sheets (Chọn chu kỳ & Sửa ngân sách)
        BudgetSheetHost(
            destination = state.currentSheet,
            state = state,
            onEvent = onEvent
        )

        // Discard Changes Dialog
        if (state.isDiscardDialogOpen) {
            DiscardChangesDialog(
                onConfirmDiscard = { onEvent(BudgetUiEvent.ConfirmDiscardChanges) },
                onContinueEditing = { onEvent(BudgetUiEvent.CancelDiscardChanges) }
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
