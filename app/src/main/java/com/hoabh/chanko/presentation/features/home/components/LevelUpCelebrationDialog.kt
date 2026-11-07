package com.hoabh.chanko.presentation.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Hiệu ứng thông báo chúc mừng Chanko đã lớn hơn (tăng Level).
 */
@Composable
fun LevelUpCelebrationDialog(
    newLevel: Int,
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
                    .padding(vertical = CandySpace.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // Biểu tượng ăn mừng
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CandyColors.readySurface),
                    contentAlignment = Alignment.Center
                ) {
                    ChankoIcon(id = ChankoIconId.ITM_05, contentDescription = null, size = 36.dp)
                }

                Text(
                    text = "Chanko đã lớn hơn!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = CandyColors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Chúc mừng! Nhờ sự kiên định trong việc hạ nhiệt mua sắm và tích lũy Pate, Chanko đã chính thức đạt Cấp độ $newLevel!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandyColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CandyColors.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tuyệt vời!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}
