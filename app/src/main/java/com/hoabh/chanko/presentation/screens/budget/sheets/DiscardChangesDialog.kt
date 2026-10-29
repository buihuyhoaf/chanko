package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun DiscardChangesDialog(
    onConfirmDiscard: () -> Unit,
    onContinueEditing: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinueEditing,
        title = {
            Text(
                text = stringResource(R.string.budget_discard_dialog_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CandyColors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.budget_discard_dialog_msg),
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = CandyColors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDiscard) {
                Text(
                    text = stringResource(R.string.budget_discard_confirm),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.overBudget
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onContinueEditing) {
                Text(
                    text = stringResource(R.string.budget_discard_continue),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    color = CandyColors.textPrimary
                )
            }
        },
        containerColor = CandyColors.budgetSnapshotSpendingPanel
    )
}
