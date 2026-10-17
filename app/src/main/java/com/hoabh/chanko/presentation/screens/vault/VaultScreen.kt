package com.hoabh.chanko.presentation.screens.vault

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
fun VaultScreen(state: ItemsState, onNavigateBack: () -> Unit) {
    ItemPage(stringResource(R.string.vault), onNavigateBack) {
        when(val data = state) {
            ItemsState.Loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ItemsState.Error -> Text(stringResource(R.string.load_error))
            is ItemsState.Content -> {
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
