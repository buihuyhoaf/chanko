package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito
import kotlin.math.abs

@Composable
fun AdjustmentHistorySheet(
    adjustments: List<BudgetAdjustment>,
    cycleRange: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.budget_adjustment_history_title),
                    fontFamily = Nunito,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Text(
                    text = cycleRange,
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = CandyColors.textSecondary
                )
            }
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .widthIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onClose)
                    .padding(horizontal = CandySpace.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_btn_close),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.brandPurple
                )
            }
        }

        if (adjustments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CandySpace.lg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_adjustment_history_empty),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = CandyColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                items(adjustments, key = { it.id }) { adj ->
                    val changeDirection = if (adj.isIncrease) "tăng" else "giảm"
                    val a11yDesc = "Ngày ${adj.adjustedAt}, ngân sách cũ ${MoneyVnd.spoken(adj.oldLimitMinor)}, ngân sách mới ${MoneyVnd.spoken(adj.newLimitMinor)}, $changeDirection ${MoneyVnd.spoken(abs(adj.diffMinor))}"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CandyColors.budgetSnapshotSpendingPanel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) { contentDescription = a11yDesc }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CandySpace.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                                Text(
                                    text = adj.adjustedAt,
                                    fontFamily = Nunito,
                                    fontSize = 12.sp,
                                    color = CandyColors.textSecondary
                                )
                                Text(
                                    text = stringResource(R.string.budget_adjustment_old_limit, adj.oldLimitFormatted),
                                    fontFamily = Nunito,
                                    fontSize = 12.sp,
                                    color = CandyColors.textSecondary
                                )
                                Text(
                                    text = stringResource(R.string.budget_adjustment_new_limit, adj.newLimitFormatted),
                                    fontFamily = Nunito,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CandyColors.textPrimary
                                )
                            }

                            Text(
                                text = adj.diffFormatted,
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (adj.isIncrease) CandyColors.brandPurple else CandyColors.overBudget
                            )
                        }
                    }
                }
            }
        }
    }
}
