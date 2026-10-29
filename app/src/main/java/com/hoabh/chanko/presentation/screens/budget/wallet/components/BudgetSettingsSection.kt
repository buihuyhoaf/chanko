package com.hoabh.chanko.presentation.screens.budget.wallet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.hoabh.chanko.R
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors

/**
 * Section 1 trong màn Thiết lập ví (C03): Ngân sách mua sắm.
 * Bố cục đồng bộ 100% với reference/wallet-settings-v2.1-approved-reference.png:
 * Header: Icon ví hồng + "Ngân sách mua sắm" + Chu kỳ ngày + Nút "Sửa ngân sách" (viền tím, icon bút).
 * Cột trái: "Đã chi" + Số tiền to + Thanh tiến trình tím + "% tiến độ".
 * Cột phải: "Còn lại" (hoặc "Vượt") + Số tiền to.
 */
@Composable
fun BudgetSettingsSection(
    selectedCycle: BudgetCycle?,
    snapshot: BudgetSnapshot?,
    canWrite: Boolean,
    modifier: Modifier = Modifier,
    onCycleClick: () -> Unit = {}
) {
    val budgetMinor = snapshot?.budgetMinor ?: 0L
    val spentMinor = snapshot?.spentMinor ?: 0L
    val remainingMinor = snapshot?.remainingMinor ?: 0L
    val isOver = snapshot?.isOverBudget == true
    val percentage = snapshot?.percentageInt ?: 0

    val progressFraction = if (budgetMinor > 0L) {
        (spentMinor.toFloat() / budgetMinor.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val purplePrimary = Color(0xFF7447FF)
    val textPrimary = Color(0xFF24183E)
    val textMuted = Color(0xFF7E7694)
    val cycleRangeText = selectedCycle?.dateRangeText ?: stringResource(R.string.budget_cycle_selection_empty)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0x14000000)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Icon ví + Tiêu đề & Chu kỳ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFDE8F3)),
                    contentAlignment = Alignment.Center
                ) {
                    ChankoIcon(
                        id = ChankoIconId.BUD_01,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ngân sách mua sắm",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            fontSize = 17.sp
                        ),
                        modifier = Modifier.semantics { heading() }
                    )

                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val cycleActionDescription = stringResource(R.string.budget_cycle_selection_action, cycleRangeText)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = CandyColors.primarySoft,
                border = BorderStroke(1.dp, CandyColors.brandPurple.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(role = Role.Button, onClick = onCycleClick)
                    .semantics(mergeDescendants = true) { contentDescription = cycleActionDescription }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.BUD_05,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.budget_cycle_picker_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = CandyColors.primaryInk
                        )
                        Text(
                            text = cycleRangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CandyColors.textPrimary
                        )
                    }
                    ChankoIcon(
                        id = ChankoIconId.ACT_18,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).rotate(90f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dòng thông số: Đã chi (kèm thanh progress) và Còn lại (2 cột đúng layout ảnh)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Cột trái: Đã chi
                Column(modifier = Modifier.weight(1.1f)) {
                    Text(
                        text = "Đã chi",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = MoneyVnd.format(spentMinor),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary,
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Thanh tiến trình tím + nhãn %
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF0ECF8))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progressFraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isOver) {
                                            Brush.horizontalGradient(listOf(CandyColors.overBudget, CandyColors.overBudget))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), purplePrimary))
                                        }
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Cột phải: Còn lại hoặc Vượt
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (isOver) "Vượt" else "Còn lại",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isOver) MoneyVnd.format(-remainingMinor) else MoneyVnd.format(remainingMinor),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOver) CandyColors.overBudget else textPrimary,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    }
}
