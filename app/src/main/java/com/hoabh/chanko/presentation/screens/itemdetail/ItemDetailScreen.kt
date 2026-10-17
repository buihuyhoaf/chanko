package com.hoabh.chanko.presentation.screens.itemdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun ItemDetailRoute(onNavigateBack: () -> Unit, onNavigateToDecision: () -> Unit, viewModel: ItemViewModel = hiltViewModel()) {
    val state by viewModel.item.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()
    ItemDetailScreen(state, now, onNavigateBack, onNavigateToDecision)
}

@Composable
fun ItemDetailScreen(
    state: ItemState,
    now: Long,
    onNavigateBack: () -> Unit,
    onNavigateToDecision: () -> Unit
) {
    ItemPage(stringResource(R.string.item_detail), onNavigateBack) {
        when(val data = state) {
            ItemState.Loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ItemState.Error -> Text(stringResource(R.string.load_error))
            is ItemState.Content -> {
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
