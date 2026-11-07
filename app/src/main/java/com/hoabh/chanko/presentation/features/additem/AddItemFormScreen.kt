package com.hoabh.chanko.presentation.features.additem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.ui.theme.*

@Composable
fun AddItemFormScreen(form: AddItemFormState, enabled: Boolean,
    onEdit: ((AddItemFormState) -> AddItemFormState) -> Unit, onPhotoError: () -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onEdit { it.copy(photoUri = uri.toString()) }
    }
    AddItemProductHero(
        form = form,
        enabled = enabled,
        onPickPhoto = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemovePhoto = {
            onEdit { it.copy(photoUri = "") }
        },
        onPhotoError = onPhotoError
    )
    Spacer(Modifier.height(CandySpace.section))
    Text(stringResource(R.string.add_item_information), style = MaterialTheme.typography.titleLarge,
        color = CandyColors.textPrimary, modifier = Modifier.semantics { heading() })
    Column(verticalArrangement = Arrangement.spacedBy(CandySpace.lg), modifier = Modifier.padding(top = CandySpace.lg)) {
        FormField(form.title, requiredLabel(R.string.add_item_name), enabled, form.title.isBlank(),
            onChange = { text -> onEdit { it.editProduct { d -> d.copy(title = text) } } })
        FormField(form.rawPrice, requiredLabel(R.string.add_item_price), enabled, form.price == null,
            error = stringResource(R.string.add_item_price_error), keyboard = KeyboardType.Number,
            onChange = { text -> onEdit { it.editProduct { d -> d.copy(rawPrice = text) } } })
    }
    val price = form.price
    Row(Modifier.fillMaxWidth().heightIn(min = CandySize.touchTarget)
        .toggleable(value = price != null && form.confirmedPrice == price, enabled = enabled && price != null,
            role = Role.Checkbox, onValueChange = { checked -> onEdit { it.copy(confirmedPrice = if (checked) price else null) } }),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CandySpace.md)) {
        Checkbox(price != null && form.confirmedPrice == price, onCheckedChange = null, enabled = enabled && price != null,
            colors = CheckboxDefaults.colors(checkedColor = CandyColors.primaryInk))
        Text(stringResource(R.string.add_item_confirm_price, price?.let(::formatVnd) ?: form.rawPrice),
            style = MaterialTheme.typography.bodyLarge, color = CandyColors.textPrimary)
    }
    Column(verticalArrangement = Arrangement.spacedBy(CandySpace.lg)) {
        CategoryPicker(form.category, enabled) { value -> onEdit { it.editProduct { d -> d.copy(category = value) } } }
        FormField(form.reason, requiredLabel(R.string.add_item_reason), enabled, form.reason.isBlank(), singleLine = false,
            onChange = { text -> onEdit { it.editProduct { d -> d.copy(reason = text) } } })
    }
    HelpText(stringResource(R.string.add_item_reason_help), Modifier.padding(top = CandySpace.sm))
    if (isProductUrl(form.url)) HelpText(stringResource(R.string.add_item_duplicate_unavailable), Modifier.padding(top = CandySpace.sm))
    HelpText(stringResource(if (form.canContinue) R.string.add_item_ready_help else R.string.add_item_form_helper),
        Modifier.padding(top = CandySpace.sm))
}

@Composable
private fun requiredLabel(id: Int) = stringResource(R.string.add_item_required_label, stringResource(id))

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface, focusedBorderColor = CandyColors.primaryInk,
    unfocusedBorderColor = CandyColors.outline, focusedTextColor = CandyColors.textPrimary, unfocusedTextColor = CandyColors.textPrimary)

@Composable
private fun FormField(value: String, label: String, enabled: Boolean, invalid: Boolean,
    modifier: Modifier = Modifier, error: String = stringResource(R.string.add_item_required),
    keyboard: KeyboardType = KeyboardType.Text, singleLine: Boolean = true, onChange: (String) -> Unit) {
    var visited by remember(label) { mutableStateOf(false) }
    var focused by remember(label) { mutableStateOf(false) }
    val showError = visited && !focused && invalid
    Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = CandyColors.textPrimary)
        OutlinedTextField(value, onChange, enabled = enabled,
            modifier = modifier.fillMaxWidth().heightIn(min = if (singleLine) 56.dp else 96.dp)
                .semantics { contentDescription = label }.onFocusChanged {
                    if (focused && !it.isFocused) visited = true
                    focused = it.isFocused
                }, shape = CandyShape.input, textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = singleLine, minLines = if (singleLine) 1 else 3, colors = fieldColors(),
            isError = showError, supportingText = if (showError) {{ Text(error) }} else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboard))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(value: String, enabled: Boolean, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = stringArrayResource(R.array.add_item_categories)
    val label = requiredLabel(R.string.add_item_category)
    Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = CandyColors.textPrimary)
        ExposedDropdownMenuBox(expanded, onExpandedChange = { if (enabled) expanded = it }) {
            OutlinedTextField(value, {}, readOnly = true, enabled = enabled, shape = CandyShape.input,
                placeholder = { Text(stringResource(R.string.add_item_select_category)) }, colors = fieldColors(),
                textStyle = MaterialTheme.typography.bodyLarge,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    .heightIn(min = 56.dp).semantics { contentDescription = label })
            ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { category -> DropdownMenuItem(text = { Text(category) }, onClick = { onSelect(category); expanded = false }) }
            }
        }
    }
}
