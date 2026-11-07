package com.hoabh.chanko.presentation.features.home.components

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Dialog Popup Daily Check-in nhẹ nhàng:
 * - Người dùng xác nhận không mua sắm ngoài kế hoạch hôm nay để nhận +10 Hạt & +10 Pate.
 * - Tuân thủ nguyên tắc MASCOT-FEEDING = A: thân thiện, không phán xét, không ép buộc.
 */
@Composable
fun DailyCheckInDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CandySpace.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // Biểu tượng linh vật/check-in tròn
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(CandyColors.readySurface),
                    contentAlignment = Alignment.Center
                ) {
                    ChankoIcon(id = ChankoIconId.ITM_01, contentDescription = null, size = 32.dp)
                }

                Text(
                    text = "Daily Check-in hôm nay",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = CandyColors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Hôm nay bạn có giữ được kế hoạch chi tiêu, không mua sắm ngoài dự kiến chứ?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandyColors.textSecondary,
                    textAlign = TextAlign.Center
                )

                // Khối phần thưởng cố định mỗi ngày
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CandyColors.resourceChipBg,
                    border = BorderStroke(1.dp, CandyColors.homeCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(CandySpace.md),
                        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        Text(
                            text = "Phần thưởng điểm danh hôm nay:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CandyColors.primaryInk
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                        ) {
                            ChankoIcon(id = ChankoIconId.ITM_01, contentDescription = null, size = 16.dp)
                            Text(
                                text = "+10 Hạt",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = CandyColors.textPrimary
                            )
                            Text(
                                text = "(Dự phòng chi tiêu)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CandyColors.textSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                        ) {
                            ChankoIcon(id = ChankoIconId.ITM_05, contentDescription = null, size = 16.dp)
                            Text(
                                text = "+10 Pate",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = CandyColors.primary
                            )
                            Text(
                                text = "(EXP tăng Level cho Chanko)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CandyColors.textSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Xác nhận (+10 Hạt, +10 Pate)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CandyColors.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Để sau",
                    color = CandyColors.textSecondary
                )
            }
        }
    )
}

@Preview(name = "Daily Check-in Dialog Preview", showBackground = true)
@Composable
private fun DailyCheckInDialogPreview() {
    DailyCheckInDialog(
        onConfirm = {},
        onDismiss = {}
    )
}
