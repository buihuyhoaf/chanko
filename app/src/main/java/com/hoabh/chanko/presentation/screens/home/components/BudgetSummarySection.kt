package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.model.BudgetSummary
import com.hoabh.chanko.presentation.screens.home.model.toVndCurrency
import com.hoabh.chanko.ui.theme.CandyColors
import java.time.LocalDate

@Composable
fun BudgetSummarySection(
    budgetSummary: BudgetSummary,
    nextCycleStartDate: LocalDate,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 3 columns: Ngân sách, Đã tiêu, Còn lại
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ngân sách
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_budget_title),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500)
                    ),
                    color = CandyColors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = budgetSummary.totalBudget.toVndCurrency(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight(800)
                    ),
                    color = CandyColors.textPrimary
                )
            }

            // Đã tiêu
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_spent_title),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500)
                    ),
                    color = CandyColors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = budgetSummary.totalSpent.toVndCurrency(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight(800)
                    ),
                    color = CandyColors.textPrimary
                )
            }

            // Còn lại
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_remaining_title),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500)
                    ),
                    color = CandyColors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = budgetSummary.remaining.toVndCurrency(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight(800)
                    ),
                    color = CandyColors.primaryInk
                )
            }
        }

        // Progress bar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(CandyColors.progressTrack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(budgetSummary.progressRatio)
                        .clip(CircleShape)
                        .background(CandyColors.brandPurple)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = stringResource(R.string.home_progress_label, budgetSummary.progressPercent),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight(600)
                ),
                color = CandyColors.textPrimary
            )
        }

        // Next cycle renewal label
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Autorenew,
                contentDescription = null,
                tint = CandyColors.primaryInk,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            val dateStr = "${nextCycleStartDate.dayOfMonth.toString().padStart(2, '0')}/${nextCycleStartDate.monthValue.toString().padStart(2, '0')}/${nextCycleStartDate.year}"
            Text(
                text = stringResource(R.string.home_renewal_label, dateStr),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight(600)
                ),
                color = CandyColors.textPrimary
            )
        }
    }
}
