package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito
import kotlin.math.abs

@Composable
fun BudgetRecentActivity(
    activities: List<BudgetActivity>,
    onActivityClick: (BudgetActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        Text(
            text = stringResource(R.string.budget_activity_title),
            fontFamily = Nunito,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CandyColors.textPrimary
        )

        if (activities.isEmpty()) {
            Text(
                text = stringResource(R.string.budget_activity_empty),
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
                activities.take(3).forEach { activity ->
                    val amountColor = if (activity.amountChangeMinor >= 0) {
                        CandyColors.brandPurple
                    } else {
                        CandyColors.textPrimary
                    }
                    val spokenPrefix = if (activity.amountChangeMinor >= 0) "cộng " else "trừ "
                    val a11yDesc = "${activity.title}, $spokenPrefix${MoneyVnd.spoken(abs(activity.amountChangeMinor))}, ${activity.occurredAt}"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .semantics {
                                role = Role.Button
                                contentDescription = a11yDesc
                            }
                            .clickable(role = Role.Button) { onActivityClick(activity) }
                            .padding(CandySpace.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activity.title,
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CandyColors.textPrimary
                            )
                            Text(
                                text = activity.occurredAt,
                                fontFamily = Nunito,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = CandyColors.textSecondary
                            )
                        }

                        Text(
                            text = activity.amountFormatted,
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    }
                }
            }
        }
    }
}
