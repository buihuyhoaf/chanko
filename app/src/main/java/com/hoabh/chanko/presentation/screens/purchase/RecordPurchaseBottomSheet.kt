package com.hoabh.chanko.presentation.screens.purchase

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.model.PaymentType
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.presentation.screens.home.domain.HomeCurrencyFormatter
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import java.util.UUID

/**
 * DETAIL-11 & DETAIL-13:
 * Record Purchase Bottom Sheet (Ghi nhận mua hàng thủ công).
 * Đơn giá thực tế, số lượng, tự động tính tổng, không có confetti hoặc mascot ăn mừng.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPurchaseBottomSheet(
    itemId: Int,
    itemName: String,
    estimatedPrice: Long,
    onConfirmPurchase: (PurchaseTransaction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var unitPriceText by remember { mutableStateOf(estimatedPrice.toString()) }
    var quantityText by remember { mutableStateOf("1") }
    var selectedPaymentType by remember { mutableStateOf(PaymentType.PAY_NOW) }
    var noteText by remember { mutableStateOf("") }

    val unitPrice = unitPriceText.toLongOrNull() ?: 0L
    val quantity = quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val totalAmount = unitPrice * quantity

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = stringResource(R.string.purchase_record_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.purchase_record_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = CandyColors.textSecondary
            )

            Spacer(modifier = Modifier.height(CandySpace.md))

            // Item Summary: Thumbnail 48dp dummy + Tên
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyColors.background)
                    .padding(CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_product_placeholder),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(CandySpace.sm))
                Column {
                    Text(
                        text = itemName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.textPrimary
                    )
                    Text(
                        text = "Giá dự kiến: ${HomeCurrencyFormatter.formatVnd(estimatedPrice)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CandyColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(CandySpace.md))

            // Đơn giá thực tế
            OutlinedTextField(
                value = unitPriceText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        unitPriceText = input
                    }
                },
                label = { Text(stringResource(R.string.purchase_record_price_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CandyColors.primary,
                    unfocusedBorderColor = CandyColors.divider
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(CandySpace.sm))

            // Số lượng
            OutlinedTextField(
                value = quantityText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        quantityText = input
                    }
                },
                label = { Text(stringResource(R.string.purchase_record_qty_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CandyColors.primary,
                    unfocusedBorderColor = CandyColors.divider
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(CandySpace.md))

            // Phương thức thanh toán
            Text(
                text = stringResource(R.string.purchase_record_payment_type),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(CandySpace.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                PaymentChip(
                    label = "Trả ngay",
                    isSelected = selectedPaymentType == PaymentType.PAY_NOW,
                    onClick = { selectedPaymentType = PaymentType.PAY_NOW },
                    modifier = Modifier.weight(1f)
                )
                PaymentChip(
                    label = "Thẻ tín dụng",
                    isSelected = selectedPaymentType == PaymentType.CREDIT,
                    onClick = { selectedPaymentType = PaymentType.CREDIT },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(CandySpace.sm))

            // Ghi chú
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(stringResource(R.string.purchase_record_note_label)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CandyColors.primary,
                    unfocusedBorderColor = CandyColors.divider
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(CandySpace.md))

            // Tổng thanh toán preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyColors.primarySoft)
                    .padding(CandySpace.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.purchase_record_total_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = CandyColors.primaryInk
                )
                Text(
                    text = HomeCurrencyFormatter.formatVnd(totalAmount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = CandyColors.primaryInk
                )
            }

            Spacer(modifier = Modifier.height(CandySpace.lg))

            // Save Button
            Button(
                onClick = {
                    val tx = PurchaseTransaction(
                        id = UUID.randomUUID().toString(),
                        itemId = itemId,
                        actualUnitPrice = unitPrice,
                        quantity = quantity,
                        totalAmount = totalAmount,
                        paymentType = selectedPaymentType,
                        purchasedTimestamp = System.currentTimeMillis(),
                        note = noteText
                    )
                    onConfirmPurchase(tx)
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.primary,
                    contentColor = CandyColors.textPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.purchase_record_save_btn),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(CandySpace.md))
        }
    }
}

@Composable
private fun PaymentChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) CandyColors.primarySoft else CandyColors.background,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, CandyColors.primary) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) CandyColors.primaryInk else CandyColors.textPrimary
            )
        }
    }
}
