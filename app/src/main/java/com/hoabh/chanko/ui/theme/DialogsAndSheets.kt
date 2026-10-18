package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Controlled confirmation primitive: never dismisses or reports success on its own. */
@Composable
fun CandyConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
    errorMessage: String? = null
) {
    Dialog(onDismissRequest = { if (!loading) onDismissRequest() },
        properties = DialogProperties(dismissOnBackPress = !loading, dismissOnClickOutside = !loading)) {
        Surface(modifier, shape = CandyShape.dialog, color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface, tonalElevation = CandyElevation.high) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(CandySpace.section),
                verticalArrangement = Arrangement.spacedBy(CandySpace.lg)) {
                Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
                Text(message, style = MaterialTheme.typography.bodyMedium)
                errorMessage?.let {
                    Text(it, Modifier.semantics { liveRegion = LiveRegionMode.Polite; error(it) },
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                // Stacked full-width actions retain readable labels at 200% font scale.
                CandyButton(onConfirm, Modifier.fillMaxWidth(),
                    variant = if (destructive) CandyButtonVariant.Destructive else CandyButtonVariant.Primary,
                    enabled = enabled, loading = loading) { Text(confirmLabel) }
                CandyButton(onDismissRequest, Modifier.fillMaxWidth(), variant = CandyButtonVariant.Text,
                    enabled = !loading) { Text(dismissLabel) }
            }
        }
    }
}

/** Caller controls visibility, state and business actions. Material owns modal focus/insets. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandyBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest, modifier, sheetState = sheetState,
        shape = CandyShape.sheet, containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface, tonalElevation = CandyElevation.high) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = CandySpace.screen).padding(bottom = CandySpace.section),
            verticalArrangement = Arrangement.spacedBy(CandySpace.lg)) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}
