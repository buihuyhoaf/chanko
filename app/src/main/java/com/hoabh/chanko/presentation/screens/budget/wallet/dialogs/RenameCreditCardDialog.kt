package com.hoabh.chanko.presentation.screens.budget.wallet.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.budget.wallet.components.CardPreview

/**
 * Modal Dialog Đổi tên thẻ tín dụng (C07) theo chuẩn v2.1.
 * Căn giữa màn hình, nền dim, đếm grapheme /30, màu chỉ đọc, Live Preview, và ghi chú giao dịch cũ.
 */
@Composable
fun RenameCreditCardDialog(
    isOpen: Boolean,
    card: CreditCard?,
    draftName: String,
    errorMessage: String?,
    isLegacyOverLimit: Boolean,
    canSave: Boolean,
    isSubmitting: Boolean,
    onDraftChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen || card == null) return

    val focusManager = LocalFocusManager.current
    val graphemeCount = CreditCard.graphemeCount(draftName)
    val purplePrimary = Color(0xFF7447FF)
    val textPrimary = Color(0xFF24183E)
    val textMuted = Color(0xFF6C657E)
    val dialogBg = Color(0xFFFAF7F5)

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
                .padding(horizontal = 24.dp)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = dialogBg,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header: Icon + Tiêu đề + Nút đóng X
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(purplePrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ChankoIcon(
                                    id = ChankoIconId.ACT_05,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Đổi tên thẻ tín dụng",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary,
                                    fontSize = 19.sp
                                ),
                                modifier = Modifier.semantics { heading() }
                            )
                        }

                        IconButton(
                            onClick = { if (!isSubmitting) onDismissRequest() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .size(48.dp)
                                .semantics { contentDescription = "Đóng" }
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.ACT_03,
                                contentDescription = "Đóng",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mô tả phụ
                    Text(
                        text = "Cập nhật tên hiển thị của thẻ. Màu thẻ sẽ không thay đổi.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textMuted,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Nhãn "Tên thẻ *"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tên thẻ",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        )
                        Text(
                            text = " *",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF4D4F)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Ô nhập tên thẻ
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
                                text = "Ví dụ: Thẻ cá nhân",
                                color = textMuted.copy(alpha = 0.6f)
                            )
                        },
                        isError = errorMessage != null || isLegacyOverLimit,
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
                                } else if (isLegacyOverLimit && graphemeCount > CreditCard.UI_MAX_NAME_LENGTH) {
                                    Text(
                                        text = "Tên hiện tại vượt giới hạn mới. Rút gọn còn tối đa 30 ký tự để lưu.",
                                        color = Color(0xFFD46B08),
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
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = purplePrimary,
                            unfocusedBorderColor = Color(0xFFE2DDEC)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mục "Màu thẻ hiện tại" (Chỉ đọc)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Màu thẻ hiện tại: ",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(card.color.colorLong))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = card.color.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nhãn "Xem trước thẻ"
                    Text(
                        text = "Xem trước thẻ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textMuted
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Preview Component
                    CardPreview(
                        name = draftName,
                        color = card.color
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ghi chú về giao dịch cũ
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0ECF8))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.SYS_15,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Các giao dịch đã ghi nhận trước đây vẫn hiển thị theo tên thẻ cũ, không bị thay đổi.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = textPrimary.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Footer: Bộ nút bấm Hủy / Lưu thay đổi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (!isSubmitting) onDismissRequest() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textPrimary
                            )
                        ) {
                            Text(
                                text = "Hủy",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
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
                                    text = "Đang lưu...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            } else {
                                Text(
                                    text = "Lưu thay đổi",
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
