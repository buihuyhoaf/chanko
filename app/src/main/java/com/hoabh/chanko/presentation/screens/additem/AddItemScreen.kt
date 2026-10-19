package com.hoabh.chanko.presentation.screens.additem

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.hoabh.chanko.ui.theme.ChankoTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import com.hoabh.chanko.ui.theme.CandyPrimaryButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace
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
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(CandySpace.md)) {
            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.onSurface), shape = com.hoabh.chanko.ui.theme.CandyShape.input, value = name, onValueChange = { onFieldChange("name", it) }, label = { Text(stringResource(R.string.item_name)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.onSurface), shape = com.hoabh.chanko.ui.theme.CandyShape.input, value = price, onValueChange = { onFieldChange("price", it) }, label = { Text(stringResource(R.string.item_price)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.onSurface), shape = com.hoabh.chanko.ui.theme.CandyShape.input, value = hours, onValueChange = { onFieldChange("hours", it) }, label = { Text(stringResource(R.string.cooldown_hours)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = MaterialTheme.colorScheme.onSurface, cursorColor = MaterialTheme.colorScheme.onSurface), shape = com.hoabh.chanko.ui.theme.CandyShape.input, value = reason, onValueChange = { onFieldChange("reason", it) }, label = { Text(stringResource(R.string.item_reason)) }, modifier = Modifier.fillMaxWidth())
            if (action == ActionState.ERROR) Text(stringResource(R.string.action_error), color = MaterialTheme.colorScheme.error)
            CandyPrimaryButton(
                onClick = onSave,
                enabled = isInputValid && action != ActionState.RUNNING && action != ActionState.DONE
            ) {
                Text(stringResource(R.string.save_item))
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun AddItemScreenPreview() {
    ChankoTheme {
        AddItemScreen(
            name = "Tai nghe Sony",
            price = "1500000",
            hours = "48",
            reason = "Cần nghe nhạc tập trung hơn",
            action = ActionState.IDLE,
            onFieldChange = { _, _ -> },
            onSave = {},
            onNavigateBack = {}
        )
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AddItemScreenDarkPreview() {
    ChankoTheme(darkTheme = true) {
        AddItemScreen(
            name = "Tai nghe Sony",
            price = "1500000",
            hours = "48",
            reason = "Cần nghe nhạc tập trung hơn",
            action = ActionState.IDLE,
            onFieldChange = { _, _ -> },
            onSave = {},
            onNavigateBack = {}
        )
    }
}

