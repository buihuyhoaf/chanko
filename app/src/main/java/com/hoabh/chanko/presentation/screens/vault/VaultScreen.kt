package com.hoabh.chanko.presentation.screens.vault

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.hoabh.chanko.ui.theme.ChankoTheme
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.*
import com.hoabh.chanko.presentation.components.*

@Composable
fun VaultRoute(onNavigateBack: () -> Unit, viewModel: OverviewViewModel = hiltViewModel()) {
    val state by viewModel.items.collectAsStateWithLifecycle()
    VaultScreen(state, onNavigateBack)
}

@Composable
fun VaultScreen(state: OverviewUiState, onNavigateBack: () -> Unit) {
    ItemPage(stringResource(R.string.vault), onNavigateBack) {
        when(val data = state) {
            OverviewUiState.Loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            OverviewUiState.Error -> Text(stringResource(R.string.load_error))
            is OverviewUiState.Content -> {
                Text(stringResource(R.string.total_saved, formatMoney(data.saved)), style = MaterialTheme.typography.headlineSmall)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Text(stringResource(R.string.passed_items), style = MaterialTheme.typography.titleMedium) }
                    if (data.passed.isEmpty()) item { Text(stringResource(R.string.no_history)) }
                    items(data.passed, key = { "passed-${it.id}" }) { item -> ItemRow(item) }
                    item { Text(stringResource(R.string.bought_items), style = MaterialTheme.typography.titleMedium) }
                    items(data.bought, key = { "bought-${it.id}" }) { item -> ItemRow(item) }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun VaultScreenPreview() {
    val samplePassed = listOf(
        CooldownItem(
            id = 1,
            name = "Giày thể thao",
            price = 1_800_000L,
            addedTimestamp = 1_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Thấy đẹp nhưng chưa cần",
            status = ItemStatus.PASSED
        )
    )
    val sampleBought = listOf(
        CooldownItem(
            id = 2,
            name = "Sách học lập trình",
            price = 250_000L,
            addedTimestamp = 1_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Cần cho công việc",
            status = ItemStatus.BOUGHT
        )
    )
    ChankoTheme {
        VaultScreen(
            state = OverviewUiState.Content(
                active = emptyList(),
                passed = samplePassed,
                bought = sampleBought,
                saved = 1_800_000L
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun VaultScreenDarkPreview() {
    val samplePassed = listOf(
        CooldownItem(
            id = 1,
            name = "Giày thể thao",
            price = 1_800_000L,
            addedTimestamp = 1_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Thấy đẹp nhưng chưa cần",
            status = ItemStatus.PASSED
        )
    )
    val sampleBought = listOf(
        CooldownItem(
            id = 2,
            name = "Sách học lập trình",
            price = 250_000L,
            addedTimestamp = 1_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Cần cho công việc",
            status = ItemStatus.BOUGHT
        )
    )
    ChankoTheme(darkTheme = true) {
        VaultScreen(
            state = OverviewUiState.Content(
                active = emptyList(),
                passed = samplePassed,
                bought = sampleBought,
                saved = 1_800_000L
            ),
            onNavigateBack = {}
        )
    }
}

