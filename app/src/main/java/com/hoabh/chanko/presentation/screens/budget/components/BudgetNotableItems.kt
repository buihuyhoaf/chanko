package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun BudgetNotableItems(
    items: List<NotableItem>,
    isPastCycle: Boolean,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        Text(
            text = stringResource(R.string.budget_notable_items_title),
            fontFamily = Nunito,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CandyColors.textPrimary
        )

        if (items.isEmpty()) {
            val emptyMsg = if (isPastCycle) {
                stringResource(R.string.budget_state_past_notable_empty)
            } else {
                stringResource(R.string.budget_notable_items_empty)
            }
            Text(
                text = emptyMsg,
                fontFamily = Nunito,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = CandyColors.textSecondary,
                modifier = Modifier.padding(vertical = CandySpace.sm)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CandyColors.budgetSnapshotSpendingPanel)
                    .padding(CandySpace.sm),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                items.take(3).forEach { item ->
                    val a11yDesc = "${item.name}, ${item.priceFormatted}"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .semantics {
                                role = Role.Button
                                contentDescription = a11yDesc
                            }
                            .clickable(role = Role.Button) { onItemClick(item.itemId) }
                            .padding(CandySpace.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.md)
                    ) {
                        // Thumbnail placeholder
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CandyColors.backgroundLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎁", fontSize = 18.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandyColors.textPrimary
                            )
                            Text(
                                text = item.priceFormatted,
                                fontFamily = Nunito,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = CandyColors.textSecondary
                            )
                        }

                        Text(
                            text = "↗",
                            fontFamily = Nunito,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyColors.brandPurple
                        )
                    }
                }
            }
        }
    }
}
