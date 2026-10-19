package com.hoabh.chanko.presentation.screens.itemdetail

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.hoabh.chanko.ui.theme.ChankoTheme
import com.hoabh.chanko.domain.model.CooldownItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import com.hoabh.chanko.ui.theme.CandyPrimaryButton as Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.presentation.*
import com.hoabh.chanko.presentation.components.*

@Composable
fun ItemDetailRoute(onNavigateBack: () -> Unit, onNavigateToDecision: () -> Unit, viewModel: ItemViewModel = hiltViewModel()) {
    val state by viewModel.item.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()
    ItemDetailScreen(state, now, onNavigateBack, onNavigateToDecision)
}

@Composable
fun ItemDetailScreen(
    state: ItemUiState,
    now: Long,
    onNavigateBack: () -> Unit,
    onNavigateToDecision: () -> Unit
) {
    ItemPage(stringResource(R.string.item_detail), onNavigateBack) {
        when(val data = state) {
            ItemUiState.Loading -> Box(Modifier.fillMaxWidth().padding(CandySpace.xl), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ItemUiState.Error -> Text(stringResource(R.string.load_error))
            is ItemUiState.Content -> {
                val item = data.item
                if (item == null) Text(stringResource(R.string.item_missing)) else {
                    Text(item.name, style = MaterialTheme.typography.headlineMedium)
                    Text(formatMoney(item.price))
                    Text(item.reason)
                    val seconds = (item.remainingMillis(now) + 999) / 1000
                    Text(stringResource(R.string.remaining_time, seconds / 3600, (seconds % 3600) / 60, seconds % 60))
                    Button(onClick = onNavigateToDecision, enabled = item.canDecide(now)) { Text(stringResource(R.string.make_decision)) }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun ItemDetailScreenPreview() {
    val sampleItem = CooldownItem(
        id = 1,
        name = "Đồng hồ thông minh",
        price = 4_500_000L,
        addedTimestamp = 1_000_000L,
        cooldownDurationMillis = 86_400_000L,
        reason = "Theo dõi sức khỏe và chạy bộ"
    )
    ChankoTheme {
        ItemDetailScreen(
            state = ItemUiState.Content(sampleItem),
            now = 1_000_000L + 36_000_000L,
            onNavigateBack = {},
            onNavigateToDecision = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ItemDetailScreenDarkPreview() {
    val sampleItem = CooldownItem(
        id = 1,
        name = "Đồng hồ thông minh",
        price = 4_500_000L,
        addedTimestamp = 1_000_000L,
        cooldownDurationMillis = 86_400_000L,
        reason = "Theo dõi sức khỏe và chạy bộ"
    )
    ChankoTheme(darkTheme = true) {
        ItemDetailScreen(
            state = ItemUiState.Content(sampleItem),
            now = 1_000_000L + 36_000_000L,
            onNavigateBack = {},
            onNavigateToDecision = {}
        )
    }
}

