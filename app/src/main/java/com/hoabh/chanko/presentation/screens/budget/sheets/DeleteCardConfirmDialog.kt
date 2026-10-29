package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun DeleteCardConfirmDialog(
    card: CreditCard,
    canWrite: Boolean = true,
    isSubmitting: Boolean = false,
    disabledReason: String? = null,
    errorMessage: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val canConfirm = canWrite && !isSubmitting

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = stringResource(R.string.budget_card_delete_dialog_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CandyColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                Text(
                    text = stringResource(R.string.budget_card_delete_dialog_msg, card.name),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = CandyColors.textSecondary
                )

                if (!canWrite && !disabledReason.isNullOrBlank()) {
                    Text(
                        text = disabledReason,
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CandyColors.budgetWarningBadgeInk
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyColors.overBudget
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = canConfirm,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = if (isSubmitting) stringResource(R.string.budget_status_deleting) else stringResource(R.string.budget_card_delete_confirm),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    color = if (canConfirm) CandyColors.overBudget else CandyColors.textSecondary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.budget_card_delete_cancel),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!isSubmitting) CandyColors.textPrimary else CandyColors.textSecondary
                )
            }
        },
        containerColor = CandyColors.budgetSnapshotSpendingPanel
    )
}
