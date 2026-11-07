package com.hoabh.chanko.presentation.features.itemdetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Dialog xác nhận Unfreeze sớm với giọng điệu trung lập, minh bạch và không phán xét.
 * Tuân thủ nguyên tắc MASCOT-FEEDING = A: Chanko không phán xét, không tạo cảm giác tội lỗi.
 */
@Composable
fun UnfreezeConfirmDialog(
    hatFee: Long,
    pateFee: Long,
    currentHatBalance: Long,
    currentPateBalance: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingHat = (currentHatBalance - hatFee).coerceAtLeast(0L)
    val remainingPate = (currentPateBalance - pateFee).coerceAtLeast(0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                ChankoIcon(id = ChankoIconId.ITM_04, contentDescription = null, size = 24.dp)
                Text(
                    text = "Mở sớm món đồ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = CandyColors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                Text(
                    text = "Mở sớm trước khi kết thúc thời gian chờ sẽ áp dụng khấu trừ Hạt và Pate theo thời gian còn lại. Chanko luôn tôn trọng mọi quyết định của bạn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandyColors.textSecondary
                )

                // Khối chi phí khấu trừ
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CandyColors.orange.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, CandyColors.orange.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(CandySpace.md),
                        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        Text(
                            text = "Chi phí khấu trừ:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CandyColors.textPrimary
                        )
                        Text(
                            text = "• Hạt: -$hatFee Hạt",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CandyColors.error
                        )
                        Text(
                            text = "• Pate: -$pateFee Pate",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CandyColors.error
                        )
                    }
                }

                // Khối dự kiến số dư sau khi mở
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CandyColors.homeCardSurface,
                    border = BorderStroke(1.dp, CandyColors.homeCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(CandySpace.md),
                        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        Text(
                            text = "Số dư dự kiến sau khi mở:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CandyColors.textPrimary
                        )
                        Text(
                            text = "• Hạt còn lại: $remainingHat Hạt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CandyColors.textPrimary
                        )
                        Text(
                            text = "• Pate còn lại: $remainingPate Pate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CandyColors.textPrimary
                        )
                        Text(
                            text = "• Level Chanko: Được bảo lưu nguyên vẹn",
                            style = MaterialTheme.typography.labelSmall,
                            color = CandyColors.primaryInk
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Xác nhận mở sớm",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CandyColors.outline)
            ) {
                Text(
                    text = "Giữ thời gian chờ",
                    color = CandyColors.textSecondary
                )
            }
        }
    )
}
