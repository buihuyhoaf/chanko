package com.hoabh.chanko.presentation.screens.budget.wallet.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hoabh.chanko.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.domain.budget.model.CardColor
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId

/**
 * Thẻ xem trước Live Preview trung lập theo phong cách Candy Pop 3D.
 * Đồng bộ với reference/add-credit-card-v2.1-approved-reference.png:
 * Nền gradient Candy Pop, chip trung tính góc trái, linh vật góc phải,
 * tên hiển thị thời gian thực góc trái dưới.
 */
@Composable
fun CardPreview(
    name: String,
    color: CardColor,
    modifier: Modifier = Modifier
) {
    val placeholder = stringResource(R.string.budget_card_add_field_label)
    val displayName = name.trim().ifEmpty { placeholder }
    val colorName = stringResource(when (color) {
        CardColor.PURPLE -> R.string.budget_card_color_purple
        CardColor.PINK -> R.string.budget_card_color_pink
        CardColor.BLUE -> R.string.budget_card_color_blue
        CardColor.MINT -> R.string.budget_card_color_mint
        CardColor.YELLOW -> R.string.budget_card_color_yellow
        CardColor.ORANGE -> R.string.budget_card_color_orange
    })
    val previewDescription = stringResource(R.string.budget_card_preview_description, displayName, colorName)
    val isPlaceholder = name.trim().isEmpty()
    val baseColor = Color(color.colorLong)
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.95f),
            baseColor
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(135.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = baseColor.copy(alpha = 0.45f),
                ambientColor = baseColor.copy(alpha = 0.2f)
            )
            .semantics {
                contentDescription = previewDescription
            },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.matchParentSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Hàng trên: Chip mô phỏng trung tính bên trái + Mascot tròn bên phải
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip mô phỏng trắng mờ bo góc
                    Box(
                        modifier = Modifier
                            .size(width = 34.dp, height = 26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }

                    // Biểu tượng linh vật tròn góc phải
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.BUD_01,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Hàng dưới: Tên thẻ thời gian thực
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaceholder) Color.White.copy(alpha = 0.65f) else Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = stringResource(R.string.budget_card_preview_brand),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}
