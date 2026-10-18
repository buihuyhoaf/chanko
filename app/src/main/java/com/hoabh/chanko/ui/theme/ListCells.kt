package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** Text-only cell; optional action is owned by the caller. No nested interactive slots. */
@Composable
fun CandyListCell(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    value: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val action = if (onClick != null) Modifier.clickable(enabled = enabled,
        role = Role.Button, onClick = onClick) else Modifier
    Surface(modifier.then(action).sizeIn(minWidth = CandySize.touchTarget,
        minHeight = CandySize.touchTarget), color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface) {
        // Vertical text avoids competing width constraints for long names and financial values.
        Column(Modifier.padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            supportingText?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium) }
            value?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
