package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Small, neutral content primitive. Business state and navigation stay with callers. */
@Composable
fun CandyCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier, shape = CandyShape.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = CandyElevation.low)) {
        Column(Modifier.padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.sm), content = content)
    }
}

/** Material's clickable overload provides focus, disabled and button semantics. */
@Composable
fun CandyActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(onClick, modifier.sizeIn(minWidth = CandySize.touchTarget, minHeight = CandySize.touchTarget),
        enabled = enabled, shape = CandyShape.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = CandyElevation.low)) {
        Column(Modifier.padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.sm), content = content)
    }
}

enum class CandyStatus { CoolingOff, ReadyForReview, Purchased, Skipped, Postponed }

/** Always supply a meaningful localized label; color is supplementary. No click action. */
@Composable
fun CandyStatusChip(label: String, status: CandyStatus, modifier: Modifier = Modifier) {
    val container = when (status) {
        CandyStatus.CoolingOff -> CandyColors.coolingOff
        CandyStatus.ReadyForReview -> CandyColors.readyForReview
        CandyStatus.Purchased -> CandyColors.purchased
        CandyStatus.Skipped -> CandyColors.skipped
        CandyStatus.Postponed -> CandyColors.postponed
    }
    Surface(modifier, shape = CandyShape.chip, color = container, contentColor = CandyColors.ink) {
        Text(label, Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
            style = MaterialTheme.typography.labelSmall)
    }
}

/** Selection is exposed by Material semantics and a visible check indicator. */
@Composable
fun CandyFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(selected, onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier.sizeIn(minWidth = CandySize.touchTarget, minHeight = CandySize.touchTarget),
        enabled = enabled, shape = CandyShape.chip,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        leadingIcon = if (selected) ({ Text("✓") }) else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary))
}
