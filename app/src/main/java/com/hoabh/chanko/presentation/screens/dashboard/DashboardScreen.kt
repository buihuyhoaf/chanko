package com.hoabh.chanko.presentation.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import com.hoabh.chanko.ui.theme.CandyPrimaryButton as Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.presentation.OverviewUiState
import com.hoabh.chanko.presentation.OverviewViewModel
import com.hoabh.chanko.presentation.components.ItemRow
import com.hoabh.chanko.presentation.components.formatMoney
import com.hoabh.chanko.ui.theme.ChankoTheme

@Composable
fun DashboardRoute(
    onNavigateToAddItem: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit,
    onNavigateToVault: () -> Unit,
    viewModel: OverviewViewModel = hiltViewModel()
) {
    val state by viewModel.items.collectAsStateWithLifecycle()
    DashboardScreen(state, onNavigateToAddItem, onNavigateToItemDetail, onNavigateToVault)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: OverviewUiState,
    onNavigateToAddItem: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit,
    onNavigateToVault: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) }, actions = {
                IconButton(onClick = onNavigateToVault) {
                    Icon(Icons.Default.Savings, contentDescription = stringResource(R.string.vault))
                }
            })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNavigateToAddItem,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_item)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = CandySpace.screen, end = CandySpace.screen, top = CandySpace.md, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(CandySpace.lg)
        ) {
            item { MascotBanner() }
            when (state) {
                OverviewUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(CandySpace.xl), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                OverviewUiState.Error -> item { Text(stringResource(R.string.load_error)) }
                is OverviewUiState.Content -> {
                    item { SavingsStats(state.saved, state.active.size) }
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.cooling_items), style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = onNavigateToVault, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text(stringResource(R.string.history)) }
                        }
                    }
                    if (state.active.isEmpty()) item { EmptyItems(onNavigateToAddItem) }
                    items(state.active, key = { it.id }) { item ->
                        ItemRow(item) { onNavigateToItemDetail(item.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MascotBanner() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.banner_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.banner_description), style = MaterialTheme.typography.bodyMedium)
            }
            Image(painterResource(R.drawable.cat_mascot), contentDescription = null, modifier = Modifier.size(96.dp))
        }
    }
}

@Composable
private fun SavingsStats(saved: Long, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(CandySpace.md)) {
        Card(Modifier.weight(1f)) {
            Column(Modifier.padding(CandySpace.lg)) {
                Text(stringResource(R.string.savings))
                Text(formatMoney(saved), style = MaterialTheme.typography.titleLarge)
            }
        }
        Card(Modifier.weight(1f)) {
            Column(Modifier.padding(CandySpace.lg)) {
                Text(stringResource(R.string.cooling))
                Text(stringResource(R.string.active_count, count), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun EmptyItems(onAdd: () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(CandySpace.section), verticalArrangement = Arrangement.spacedBy(CandySpace.md)) {
            Text(stringResource(R.string.empty_items), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.empty_description))
            Button(onClick = onAdd) { Text(stringResource(R.string.add_item)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyDashboardPreview() {
    ChankoTheme { DashboardScreen(OverviewUiState.Content(emptyList(), emptyList(), emptyList(), 0), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun DarkDashboardPreview() {
    ChankoTheme(darkTheme = true) { DashboardScreen(OverviewUiState.Content(emptyList(), emptyList(), emptyList(), 0), {}, {}, {}) }
}
