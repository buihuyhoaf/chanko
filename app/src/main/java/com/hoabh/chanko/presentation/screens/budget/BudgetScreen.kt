package com.hoabh.chanko.presentation.screens.budget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.budget.sheets.BudgetSheetHost
import com.hoabh.chanko.presentation.screens.budget.sheets.DeleteCardConfirmDialog
import com.hoabh.chanko.presentation.screens.budget.sheets.DiscardChangesDialog
import com.hoabh.chanko.presentation.screens.budget.wallet.components.AdjustmentHistorySection
import com.hoabh.chanko.presentation.screens.budget.wallet.components.BudgetSettingsSection
import com.hoabh.chanko.presentation.screens.budget.wallet.components.PaymentMethodsSection
import com.hoabh.chanko.presentation.screens.budget.wallet.dialogs.AddCreditCardDialog
import com.hoabh.chanko.presentation.screens.budget.wallet.dialogs.RenameCreditCardDialog
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Màn hình Ngân sách (BudgetScreen) chuẩn v2.1.
 * Đã hợp nhất màn Tổng quan cũ và màn Thiết lập ví thành một màn hình Ngân sách duy nhất:
 * 1. Top App Bar: Đồng bộ 100% với màn hình Món đồ (MyItemsScreen) với nút Quay lại và tiêu đề "Ngân sách" 24sp Bold.
 * 2. Section 1: Ngân sách mua sắm (Đã chi, Còn lại, % tiến độ, Chu kỳ, Sửa ngân sách).
 * 3. Section 2: Phương thức thanh toán (Pay Now mặc định, danh sách thẻ tín dụng Candy, Thêm thẻ, Sửa, Xóa).
 * 4. Section 3: Lịch sử điều chỉnh ngân sách (Timeline Tăng/Giảm hạn mức).
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
            state.isAddCardDialogOpen -> onEvent(BudgetUiEvent.DismissAddCreditCardDialog)
            state.isRenameCardDialogOpen -> onEvent(BudgetUiEvent.DismissRenameCreditCardDialog)
            state.cardToDelete != null -> onEvent(BudgetUiEvent.DismissDeleteCreditCardDialog)
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
            // 1. Top App Bar — Đồng bộ 100% phong cách màn hình Món đồ (MyItemsScreen)
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

            // 2. Nội dung cuộn chính (3 Section)
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

                // Section 2: Phương thức thanh toán (C05)
                item {
                    PaymentMethodsSection(
                        cards = state.creditCards,
                        canWrite = state.canWrite,
                        disabledReason = state.writeDisabledReason,
                        onAddCardClick = { onEvent(BudgetUiEvent.OpenAddCreditCard) },
                        onRenameCardClick = { card -> onEvent(BudgetUiEvent.OpenRenameCreditCard(card)) },
                        onDeleteCardClick = { card -> onEvent(BudgetUiEvent.RequestDeleteCreditCard(card)) }
                    )
                }

                // Section 3: Lịch sử điều chỉnh ngân sách (C09)
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

        // Bottom Sheets (C02 Chọn chu kỳ & C04 Sửa ngân sách)
        BudgetSheetHost(
            destination = state.currentSheet,
            state = state,
            onEvent = onEvent
        )

        // C06 Add Credit Card Dialog
        if (state.isAddCardDialogOpen) {
            AddCreditCardDialog(
                isOpen = state.isAddCardDialogOpen,
                draftName = state.addCardDraft,
                selectedColor = state.addCardColor,
                errorMessage = state.addCardInlineError?.let { stringResource(it) },
                canSave = state.canSubmitAddCard,
                isSubmitting = state.isSubmitting,
                onDraftChange = { text -> onEvent(BudgetUiEvent.UpdateAddCardDraft(text)) },
                onColorSelect = { color -> onEvent(BudgetUiEvent.SelectAddCardColor(color)) },
                onDismissRequest = { onEvent(BudgetUiEvent.DismissAddCreditCardDialog) },
                onSubmit = {
                    onEvent(BudgetUiEvent.SubmitAddCreditCard(state.addCardDraft, state.addCardColor))
                }
            )
        }

        // C07 Rename Credit Card Dialog
        if (state.isRenameCardDialogOpen && state.cardToRename != null) {
            RenameCreditCardDialog(
                isOpen = state.isRenameCardDialogOpen,
                card = state.cardToRename,
                draftName = state.renameCardDraft,
                errorMessage = state.renameCardInlineError?.let { stringResource(it) },
                isLegacyOverLimit = state.isLegacyNameOverLimit,
                canSave = state.canSubmitRename,
                isSubmitting = state.isSubmitting,
                onDraftChange = { text -> onEvent(BudgetUiEvent.UpdateRenameCardDraft(text)) },
                onDismissRequest = { onEvent(BudgetUiEvent.DismissRenameCreditCardDialog) },
                onSubmit = {
                    val cardId = state.cardToRename?.id ?: return@RenameCreditCardDialog
                    onEvent(BudgetUiEvent.SubmitRenameCreditCard(cardId, state.renameCardDraft))
                }
            )
        }

        // C08 Delete Card Dialog
        state.cardToDelete?.let { card ->
            DeleteCardConfirmDialog(
                card = card,
                canWrite = state.canWrite,
                isSubmitting = state.isSubmitting,
                disabledReason = state.writeDisabledReason,
                errorMessage = state.deleteCardInlineError,
                onConfirm = { onEvent(BudgetUiEvent.ConfirmDeleteCreditCard) },
                onDismiss = { onEvent(BudgetUiEvent.DismissDeleteCreditCardDialog) }
            )
        }

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
