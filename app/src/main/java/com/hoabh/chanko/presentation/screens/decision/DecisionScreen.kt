package com.hoabh.chanko.presentation.screens.decision

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.hoabh.chanko.ui.theme.ChankoTheme
import com.hoabh.chanko.domain.model.CooldownItem
import androidx.compose.material3.*
import com.hoabh.chanko.ui.theme.CandyPrimaryButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.presentation.*
import com.hoabh.chanko.presentation.screens.itemdetail.ItemViewModel
import com.hoabh.chanko.presentation.screens.itemdetail.ItemUiState
import com.hoabh.chanko.presentation.components.*

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DecisionRoute(
    onDecisionMade: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ItemViewModel = hiltViewModel()
) {
    val state by viewModel.item.collectAsStateWithLifecycle()
    val action by viewModel.action.collectAsStateWithLifecycle()
    val now by viewModel.now.collectAsStateWithLifecycle()
    LaunchedEffect(action) { if (action == ActionState.DONE) onDecisionMade() }
    DecisionScreen(state, action, now, viewModel::decide, onNavigateBack)
}

@Composable
fun DecisionScreen(
    state: ItemUiState,
    action: ActionState,
    now: Long,
    onDecide: (ItemStatus) -> Unit,
    onNavigateBack: () -> Unit
) {
    ItemPage(stringResource(R.string.make_decision), onNavigateBack) {
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
                    val enabled = item.canDecide(now) && action != ActionState.RUNNING && action != ActionState.DONE
                    CandyPrimaryButton(onClick = { onDecide(ItemStatus.BOUGHT) }, enabled = enabled) { Text(stringResource(R.string.buy_item)) }
                    OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface), onClick = { onDecide(ItemStatus.PASSED) }, enabled = enabled) { Text(stringResource(R.string.pass_item)) }
                    if (action == ActionState.ERROR) Text(stringResource(R.string.action_error), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun DecisionScreenPreview() {
    val sampleItem = CooldownItem(
        id = 1,
        name = "Bàn phím cơ Custom",
        price = 3_200_000L,
        addedTimestamp = 1_000_000L,
        cooldownDurationMillis = 0L,
        reason = "Gõ code sướng tay hơn"
    )
    ChankoTheme {
        DecisionScreen(
            state = ItemUiState.Content(sampleItem),
            action = ActionState.IDLE,
            now = 2_000_000L,
            onDecide = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DecisionScreenDarkPreview() {
    val sampleItem = CooldownItem(
        id = 1,
        name = "Bàn phím cơ Custom",
        price = 3_200_000L,
        addedTimestamp = 1_000_000L,
        cooldownDurationMillis = 0L,
        reason = "Gõ code sướng tay hơn"
    )
    ChankoTheme(darkTheme = true) {
        DecisionScreen(
            state = ItemUiState.Content(sampleItem),
            action = ActionState.IDLE,
            now = 2_000_000L,
            onDecide = {},
            onNavigateBack = {}
        )
    }
}

