package com.hoabh.chanko.presentation.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun SetupStatusScreen(
    state: SetupUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(CandySpace.section),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.page == SetupPage.CHECKING) {
            CircularProgressIndicator(color = CandyColors.primary)
            Spacer(Modifier.height(CandySpace.lg))
            Text(
                text = stringResource(R.string.setup_checking),
                color = Ink,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        } else {
            Text(
                text = stringResource(R.string.setup_status_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Ink,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(CandySpace.md))
            SetupProblemText(state.problem ?: SetupProblem.UNKNOWN)
            Spacer(Modifier.height(CandySpace.lg))
            SetupButton(
                text = stringResource(R.string.setup_retry),
                onClick = onRetry,
            )
        }
    }
}
