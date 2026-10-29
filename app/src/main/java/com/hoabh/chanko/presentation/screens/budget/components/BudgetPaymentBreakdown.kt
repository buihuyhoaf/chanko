package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun BudgetPaymentBreakdown(
    methods: List<PaymentMethodBreakdown>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        Text(
            text = stringResource(R.string.budget_payment_breakdown_title),
            fontFamily = Nunito,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CandyColors.textPrimary
        )

        if (methods.isEmpty()) {
            Text(
                text = stringResource(R.string.budget_state_zero_spend_empty),
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
                    .padding(CandySpace.md),
                verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                methods.forEach { method ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.BUD_01,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = method.name,
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CandyColors.textPrimary
                            )
                        }

                        Text(
                            text = method.spentFormatted,
                            fontFamily = Nunito,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CandyColors.brandPurple
                        )
                    }
                }
            }
        }
    }
}
