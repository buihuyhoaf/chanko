package com.hoabh.chanko.presentation.screens.budget.wallet.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hoabh.chanko.domain.budget.model.CardColor
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.budget.wallet.components.CardPreview

/**
 * Modal Dialog Thêm thẻ tín dụng (C06) theo chuẩn v2.1.
 * Đồng bộ 100% với reference/add-credit-card-v2.1-approved-reference.png:
 * Modal căn giữa viewport trên nền dim, tối đa 30 grapheme, 6 màu Candy swatches,
 * Live Preview đồng bộ tên & màu, nút Hủy và Thêm thẻ.
 */
@Composable
fun AddCreditCardDialog(
    isOpen: Boolean,
    draftName: String,
    selectedColor: CardColor,
    errorMessage: String?,
    canSave: Boolean,
    isSubmitting: Boolean,
    onDraftChange: (String) -> Unit,
    onColorSelect: (CardColor) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val focusManager = LocalFocusManager.current
    val graphemeCount = CreditCard.graphemeCount(draftName)
    val purplePrimary = Color(0xFF7447FF)
    val textPrimary = Color(0xFF24183E)
    val textMuted = Color(0xFF7E7694)

    Dialog(
        onDismissRequest = {
            if (!isSubmitting) onDismissRequest()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting
        )
    ) {
        // Nền dim bao phủ toàn màn hình
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header: Icon minh họa + Tiêu đề & Subtitle + Nút đóng (X)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEDE9FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.BUD_01,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "Thêm thẻ tín dụng",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    fontSize = 18.sp
                                ),
                                modifier = Modifier.semantics { heading() }
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Thêm thẻ để theo dõi chi tiêu mua sắm trong ngân sách hàng tháng.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = textMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }

                        IconButton(
                            onClick = { if (!isSubmitting) onDismissRequest() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .size(36.dp)
                                .semantics { contentDescription = "Đóng" }
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.ACT_03,
                                contentDescription = "Đóng",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Nhãn "Tên thẻ"
                    Text(
                        text = "Tên thẻ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Ô nhập tên thẻ (single-line, placeholder, counter n/30)
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = onDraftChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Tên thẻ, tối đa 30 ký tự, hiện có $graphemeCount ký tự"
                            },
                        singleLine = true,
                        enabled = !isSubmitting,
                        placeholder = {
                            Text(
                                text = "Techcombank Visa",
                                color = textMuted.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        },
                        isError = errorMessage != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                Text(
                                    text = "$graphemeCount/${CreditCard.UI_MAX_NAME_LENGTH}",
                                    color = if (graphemeCount > CreditCard.UI_MAX_NAME_LENGTH) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        textMuted
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (canSave) onSubmit()
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = purplePrimary,
                            unfocusedBorderColor = Color(0xFFE2DDEC)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nhãn "Màu thẻ"
                    Text(
                        text = "Màu thẻ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6 Candy Color Swatches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardColor.entries.forEach { color ->
                            val isSelected = color == selectedColor
                            val swatchColor = Color(color.colorLong)

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(swatchColor)
                                    .clickable(enabled = !isSubmitting) {
                                        onColorSelect(color)
                                    }
                                    .semantics {
                                        this.role = Role.RadioButton
                                        this.selected = isSelected
                                        this.contentDescription = "Màu ${color.displayName}${if (isSelected) ", đã chọn" else ""}"
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.5.dp,
                                                color = purplePrimary,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nhãn "Xem trước thẻ"
                    Text(
                        text = "Xem trước thẻ",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Preview Component: phản ánh cùng draft và màu đang chọn
                    CardPreview(
                        name = draftName,
                        color = selectedColor
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Footer: Bộ nút bấm Hủy / Thêm thẻ
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (!isSubmitting) onDismissRequest() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3E8FF),
                                contentColor = purplePrimary,
                                disabledContainerColor = Color(0xFFF3E8FF).copy(alpha = 0.5f),
                                disabledContentColor = purplePrimary.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "Hủy",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = purplePrimary
                                )
                            )
                        }

                        Button(
                            onClick = onSubmit,
                            enabled = canSave,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = purplePrimary,
                                contentColor = Color.White,
                                disabledContainerColor = purplePrimary.copy(alpha = 0.35f),
                                disabledContentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang thêm...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            } else {
                                Text(
                                    text = "Thêm thẻ",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
