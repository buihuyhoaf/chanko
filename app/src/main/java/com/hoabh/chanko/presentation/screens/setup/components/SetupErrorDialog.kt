package com.hoabh.chanko.presentation.screens.setup.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.setup.data.SetupProblem
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyShape

@Composable
fun SetupErrorDialog(
    problem: SetupProblem,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messageRes = when (problem) {
        SetupProblem.UNKNOWN -> R.string.setup_unknown
        SetupProblem.OFFLINE -> R.string.setup_offline
        SetupProblem.SAVE_ERROR -> R.string.setup_save_error
        SetupProblem.UNAVAILABLE -> R.string.setup_unavailable
        SetupProblem.TIME_ZONE -> R.string.setup_timezone_error
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.setup_error_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = CandyColors.ink,
                modifier = Modifier.semantics { heading() },
            )
        },
        text = {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = CandyColors.mutedInk,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onRetry()
                }
            ) {
                Text(
                    text = stringResource(R.string.setup_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = CandyColors.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.setup_close),
                    style = MaterialTheme.typography.labelLarge,
                    color = CandyColors.mutedInk,
                )
            }
        },
        shape = CandyShape.dialog,
        containerColor = Color.White,
        modifier = modifier,
    )
}
