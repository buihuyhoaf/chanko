package com.hoabh.chanko.presentation.screens.setup.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.setup.data.SetupProblem
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
internal fun SetupButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    busy: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = CandyColors.primary,
            contentColor = OnPurple,
        ),
        shape = SetupButtonShape,
        contentPadding = PaddingValues(CandySpace.lg),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = OnPurple,
            )
            Spacer(Modifier.width(CandySpace.sm))
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
internal fun SetupErrorText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = CandyColors.errorText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .padding(top = CandySpace.sm)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
}

@Composable
internal fun SetupProblemText(
    problem: SetupProblem,
    modifier: Modifier = Modifier,
) {
    val messageRes = when (problem) {
        SetupProblem.UNKNOWN -> R.string.setup_unknown
        SetupProblem.OFFLINE -> R.string.setup_offline
        SetupProblem.SAVE_ERROR -> R.string.setup_save_error
        SetupProblem.UNAVAILABLE -> R.string.setup_unavailable
        SetupProblem.TIME_ZONE -> R.string.setup_timezone_error
    }
    SetupErrorText(
        text = stringResource(messageRes),
        modifier = modifier,
    )
}
