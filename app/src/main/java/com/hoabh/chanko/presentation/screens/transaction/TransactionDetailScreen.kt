package com.hoabh.chanko.presentation.screens.transaction

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.model.PaymentType
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.home.domain.HomeCurrencyFormatter
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun TransactionDetailRoute(
    viewModel: TransactionDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is TransactionDetailUiEffect.NavigateBack -> onNavigateBack()
            is TransactionDetailUiEffect.ShowToast -> {
                // Toast notification
            }
        }
    }

    TransactionDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack
    )
}

/**
 * GAP-04: Transaction Detail
 * Cho phép xem chi tiết, sửa thông tin nhập sai hoặc xóa giao dịch nhập sai.
 */
@Composable
fun TransactionDetailScreen(
    uiState: TransactionDetailUiState,
    onEvent: (TransactionDetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CandyColors.primary)
            }
            return
        }

        if (uiState.isNotFound) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.transaction_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CandyColors.textSecondary
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    ChankoIcon(
                        id = ChankoIconId.ACT_02,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(
                    text = stringResource(R.string.transaction_detail_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = CandyColors.textPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = CandySpace.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // Item Header Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CandySpace.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CandyColors.background)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_product_placeholder),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(CandySpace.md))
                        Column {
                            Text(
                                text = uiState.itemName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CandyColors.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.transaction_detail_date) + ": " + uiState.formattedPurchasedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = CandyColors.textSecondary
                            )
                        }
                    }
                }

                if (!uiState.isEditMode) {
                    // Chế độ Xem (Read Mode)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CandySpace.lg),
                            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
                        ) {
                            InfoRow(
                                label = "Tổng thanh toán",
                                value = uiState.formattedTotalAmount,
                                isPrimary = true
                            )
                            InfoRow(
                                label = "Đơn giá thực tế",
                                value = uiState.formattedUnitPrice
                            )
                            InfoRow(
                                label = "Số lượng",
                                value = "${uiState.quantity}"
                            )
                            InfoRow(
                                label = "Phương thức thanh toán",
                                value = if (uiState.paymentType == PaymentType.PAY_NOW) "Trả ngay" else "Thẻ tín dụng"
                            )
                            if (uiState.note.isNotBlank()) {
                                InfoRow(
                                    label = "Ghi chú",
                                    value = uiState.note
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(CandySpace.sm))

                    // Hành động: Sửa & Xóa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
                    ) {
                        OutlinedButton(
                            onClick = { onEvent(TransactionDetailUiEvent.ToggleEditMode) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            ChankoIcon(id = ChankoIconId.ACT_05, contentDescription = null, size = 18.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.transaction_detail_edit_action))
                        }

                        Button(
                            onClick = { onEvent(TransactionDetailUiEvent.RequestDelete) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CandyColors.error.copy(alpha = 0.15f),
                                contentColor = CandyColors.error
                            )
                        ) {
                            ChankoIcon(id = ChankoIconId.ACT_06, contentDescription = null, size = 18.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.transaction_detail_delete_action))
                        }
                    }
                } else {
                    // Chế độ Chỉnh sửa (Inline Edit Form)
                    EditTransactionForm(
                        initialUnitPrice = uiState.actualUnitPrice,
                        initialQuantity = uiState.quantity,
                        initialPaymentType = uiState.paymentType,
                        initialNote = uiState.note,
                        onSave = { unitPrice, qty, pType, note ->
                            onEvent(TransactionDetailUiEvent.UpdateFields(unitPrice, qty, pType, note))
                        },
                        onCancel = { onEvent(TransactionDetailUiEvent.ToggleEditMode) }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (uiState.isDeleteDialogOpen) {
            AlertDialog(
                onDismissRequest = { onEvent(TransactionDetailUiEvent.DismissDeleteDialog) },
                title = { Text(stringResource(R.string.transaction_delete_dialog_title)) },
                text = { Text(stringResource(R.string.transaction_delete_dialog_msg)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(TransactionDetailUiEvent.ConfirmDelete) }) {
                        Text(
                            text = stringResource(R.string.transaction_delete_confirm),
                            color = CandyColors.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(TransactionDetailUiEvent.DismissDeleteDialog) }) {
                        Text(stringResource(R.string.transaction_delete_cancel))
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isPrimary: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = CandyColors.textSecondary
        )
        Text(
            text = value,
            style = if (isPrimary) {
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            } else {
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            },
            color = if (isPrimary) CandyColors.primary else CandyColors.textPrimary
        )
    }
}

@Composable
private fun EditTransactionForm(
    initialUnitPrice: Long,
    initialQuantity: Int,
    initialPaymentType: PaymentType,
    initialNote: String,
    onSave: (Long, Int, PaymentType, String) -> Unit,
    onCancel: () -> Unit
) {
    var priceText by remember { mutableStateOf(initialUnitPrice.toString()) }
    var qtyText by remember { mutableStateOf(initialQuantity.toString()) }
    var selectedPType by remember { mutableStateOf(initialPaymentType) }
    var noteText by remember { mutableStateOf(initialNote) }

    val unitPrice = priceText.toLongOrNull() ?: 0L
    val quantity = qtyText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val total = unitPrice * quantity

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                label = { Text(stringResource(R.string.purchase_record_price_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = qtyText,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) qtyText = it },
                label = { Text(stringResource(R.string.purchase_record_qty_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Text(
                text = stringResource(R.string.transaction_detail_total_format, HomeCurrencyFormatter.formatVnd(total)),
                fontWeight = FontWeight.Bold,
                color = CandyColors.primary
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.purchase_record_note_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.transaction_detail_cancel))
                }
                Button(
                    onClick = { onSave(unitPrice, quantity, selectedPType, noteText) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CandyColors.primary)
                ) {
                    Text(stringResource(R.string.transaction_detail_save_changes))
                }
            }
        }
    }
}
