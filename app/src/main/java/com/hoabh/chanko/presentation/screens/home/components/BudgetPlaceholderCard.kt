package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.BudgetCardRole
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandySpace

private val PlaceholderCornerShape = RoundedCornerShape(24.dp)
private val PlaceholderMinHeight = 196.dp

/**
 * Thẻ Trung Tính Placeholder cho Chu Kỳ Tiếp Theo Chưa Cấu Hình (Home v2.2 - BC22-05, BC22-06):
 * - Nền trắng ngà/xám pastel, viền nét đứt (dash = 6dp, gap = 4dp).
 * - Icon dấu '+' trung tính, không mang màu gradient chu kỳ.
 * - CTA "+ Thiết lập ngân sách" dẫn tới luồng thiết lập target kỳ sau.
 */
@Composable
fun BudgetPlaceholderCard(
    cycleLabel: String,
    dateRangeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: BudgetCardRole = BudgetCardRole.NextPlaceholder
) {
    val borderColor = CandyBudgetThemes.placeholderBorder
    val bgColor = CandyBudgetThemes.placeholderBackground
    val inkColor = CandyBudgetThemes.placeholderInk

    val cdText = stringResource(
        R.string.home_budget_placeholder_title
    ) + ", " + stringResource(R.string.home_budget_placeholder_cta)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PlaceholderMinHeight)
            .clip(PlaceholderCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cdText
                this.role = Role.Button
            },
        shape = PlaceholderCornerShape,
        color = bgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CandySpace.lg)
        ) {
            // Viền nét đứt vẽ trên Canvas (không hardcode màu)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val dash = 8.dp.toPx()
                val gap = 6.dp.toPx()
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CandySpace.sm),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Tên chu kỳ + Huy hiệu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = cycleLabel.ifBlank { stringResource(R.string.home_budget_placeholder_title) },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = inkColor
                        )
                        if (dateRangeText.isNotBlank()) {
                            Text(
                                text = dateRangeText,
                                style = MaterialTheme.typography.bodySmall,
                                color = inkColor.copy(alpha = 0.75f)
                            )
                        }
                    }
                    BudgetCycleBadge(role = role)
                }

                // Trung tâm: Icon '+' tròn
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(inkColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ACT_01,
                        contentDescription = null,
                        size = 24.dp
                    )
                }

                // Footer CTA: Button giả lập touch target >= 48dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(inkColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.home_budget_placeholder_cta),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = inkColor
                    )
                }
            }
        }
    }
}
