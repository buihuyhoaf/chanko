package com.hoabh.chanko.presentation.screens.additem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.ActionState
import com.hoabh.chanko.presentation.components.ItemPage

@Composable
fun AddItemRoute(onNavigateBack: () -> Unit, viewModel: AddItemViewModel = hiltViewModel()) {
    val name by viewModel.field("name").collectAsStateWithLifecycle()
    val price by viewModel.field("price").collectAsStateWithLifecycle()
    val hours by viewModel.field("hours").collectAsStateWithLifecycle()
    val reason by viewModel.field("reason").collectAsStateWithLifecycle()
    val action by viewModel.action.collectAsStateWithLifecycle()
    LaunchedEffect(action) { if (action == ActionState.DONE) onNavigateBack() }
    AddItemScreen(name, price, hours, reason, action, viewModel::setField,
        onSave = {
            val p = price.trim().toLongOrNull()
            val h = hours.trim().toLongOrNull()
            if (p != null && h != null) {
                viewModel.add(name, p, h, reason)
            }
        },
        onNavigateBack = onNavigateBack)
}

@Composable
fun AddItemScreen(
    name: String,
    price: String,
    hours: String,
    reason: String,
    action: ActionState,
    onFieldChange: (String, String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isInputValid = name.isNotBlank() &&
        (price.trim().toLongOrNull() ?: 0L) > 0L &&
        (hours.trim().toLongOrNull() ?: 0L) in 1..8760
    ItemPage(stringResource(R.string.add_item), onNavigateBack) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, { onFieldChange("name", it) }, label = { Text(stringResource(R.string.item_name)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(price, { onFieldChange("price", it) }, label = { Text(stringResource(R.string.item_price)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(hours, { onFieldChange("hours", it) }, label = { Text(stringResource(R.string.cooldown_hours)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(reason, { onFieldChange("reason", it) }, label = { Text(stringResource(R.string.item_reason)) }, modifier = Modifier.fillMaxWidth())
            if (action == ActionState.ERROR) Text(stringResource(R.string.action_error), color = MaterialTheme.colorScheme.error)
            Button(
                onClick = onSave,
                enabled = isInputValid && action != ActionState.RUNNING && action != ActionState.DONE
            ) {
                Text(stringResource(R.string.save_item))
            }
        }
    }
}
