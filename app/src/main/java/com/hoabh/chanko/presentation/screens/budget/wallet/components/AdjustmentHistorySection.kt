package com.hoabh.chanko.presentation.screens.budget.wallet.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId

/**
 * Section 3 trong màn Thiết lập ví (C03): Lịch sử điều chỉnh ngân sách.
 * Đồng bộ 100% với reference/wallet-settings-v2.1-approved-reference.png:
 * Header: Icon đồng hồ vàng cam + stringResource(R.string.budget_adjustment_history_title) + chevron đóng/mở.
 * Content Timeline:
 * Cột 1: Ngày giờ địa phương.
 * Cột 2: Huy hiệu tròn mũi tên Tăng (xanh) hoặc Giảm (đỏ).
 * Cột 3: Tiêu đề Tăng/Giảm + hạn mức Trước -> Sau.
 * Cột 4: Mũi tên chevron phải (>).
 */
@Composable
fun AdjustmentHistorySection(
    adjustments: List<BudgetAdjustment>,
    cycleRange: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyDescription = stringResource(
        if (isExpanded) R.string.budget_history_expanded_description else R.string.budget_history_collapsed_description
    )
    val textPrimary = Color(0xFF24183E)
    val textMuted = Color(0xFF7E7694)

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
            // Header: Icon đồng hồ + Tiêu đề & Subtitle + Nút toggle chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpanded)
                    .semantics {
                        heading()
                        contentDescription = historyDescription
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_12,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.budget_adjustment_history_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.budget_history_helper),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textMuted,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Nút chevron tròn
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8F5FC)),
                    contentAlignment = Alignment.Center
                ) {
                    ChankoIcon(
                        id = if (isExpanded) ChankoIconId.ACT_08 else ChankoIconId.ACT_09,
                        contentDescription = if (isExpanded) stringResource(R.string.budget_history_collapse) else stringResource(R.string.budget_history_expand),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Danh sách Timeline
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (adjustments.isEmpty()) {
                        Text(
                            text = stringResource(R.string.budget_history_empty),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textMuted,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        adjustments.forEachIndexed { index, adj ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            val isIncrease = adj.newLimitMinor >= adj.oldLimitMinor
                            val badgeBg = if (isIncrease) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            val badgeIconId = if (isIncrease) ChankoIconId.ACT_08 else ChankoIconId.ACT_09

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cột 1: Ngày giờ
                                Column(modifier = Modifier.width(80.dp)) {
                                    Text(
                                        text = adj.adjustedAt,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = stringResource(R.string.budget_history_current_cycle),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = textMuted,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Cột 2: Huy hiệu tròn mũi tên Tăng / Giảm
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ChankoIcon(
                                        id = badgeIconId,
                                        contentDescription = if (isIncrease) stringResource(R.string.budget_history_increase_short) else stringResource(R.string.budget_history_decrease_short),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Cột 3: Tăng/Giảm ngân sách + Hạn mức
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isIncrease) stringResource(R.string.budget_history_increase) else stringResource(R.string.budget_history_decrease),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.budget_history_amount_change, MoneyVnd.format(adj.oldLimitMinor), MoneyVnd.format(adj.newLimitMinor)),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = textMuted,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                // Cột 4: Mũi tên chevron phải
                                ChankoIcon(
                                    id = ChankoIconId.ACT_04,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
