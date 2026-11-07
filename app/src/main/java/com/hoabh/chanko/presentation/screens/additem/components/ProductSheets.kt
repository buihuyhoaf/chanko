package com.hoabh.chanko.presentation.screens.additem.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.additem.HelpText
import com.hoabh.chanko.presentation.screens.additem.LinkAction
import com.hoabh.chanko.presentation.screens.additem.PrimaryAction
import com.hoabh.chanko.presentation.screens.additem.ReviewCard
import com.hoabh.chanko.presentation.screens.additem.SecondaryAction
import com.hoabh.chanko.presentation.screens.additem.formatVnd
import com.hoabh.chanko.ui.theme.*

/** Presentation-only models; real matching and variant payloads remain integration pending. */
data class DuplicateItemSummary(val id: String, val title: String, val priceVnd: Long)
data class ProductVariantOption(val id: String, val title: String, val priceVnd: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateWarningSheet(item: DuplicateItemSummary, onDismiss: () -> Unit,
    onViewOld: (String) -> Unit, onContinueNew: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(CandySpace.section), verticalArrangement = Arrangement.spacedBy(CandySpace.lg)) {
            Text(stringResource(R.string.add_item_duplicate_title), style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.add_item_duplicate_explanation), style = MaterialTheme.typography.bodyLarge)
            ReviewCard {
                Row(horizontalArrangement = Arrangement.spacedBy(CandySpace.md), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Surface(Modifier.size(64.dp), shape = CandyShape.thumbnail, color = CandyColors.primarySoft) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon(
                                id = com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId.ITM_03, contentDescription = null)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.labelLarge, color = CandyColors.textPrimary)
                        Text(formatVnd(item.priceVnd), style = FinancialTypography.productPrice, color = CandyColors.textPrimary)
                    }
                }
                HelpText(stringResource(R.string.add_item_duplicate_history), Modifier.padding(top = CandySpace.sm))
            }
            PrimaryAction(stringResource(R.string.add_item_view_old)) { onViewOld(item.id) }
            SecondaryAction(stringResource(R.string.add_item_continue_new), onClick = onContinueNew)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantPickerSheet(variants: List<ProductVariantOption>, onDismiss: () -> Unit,
    onSelected: (ProductVariantOption) -> Unit) {
    var selectedId by remember(variants) { mutableStateOf<String?>(null) }
    val selected = variants.find { it.id == selectedId && it.priceVnd > 0 }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(CandySpace.section), verticalArrangement = Arrangement.spacedBy(CandySpace.lg)) {
            Text(stringResource(R.string.add_item_variant_title), style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() })
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(CandySpace.md)) {
                variants.forEach { variant ->
                    Surface(shape = CandyShape.input,
                        color = if (selectedId == variant.id) CandyColors.primarySoft else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(if (selectedId == variant.id) 2.dp else 1.dp,
                            if (selectedId == variant.id) CandyColors.primaryInk else CandyColors.outline)) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp)
                            .selectable(selectedId == variant.id, enabled = variant.priceVnd > 0,
                                role = Role.RadioButton, onClick = { selectedId = variant.id }),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selectedId == variant.id, onClick = null, enabled = variant.priceVnd > 0)
                            Column(Modifier.padding(CandySpace.sm)) {
                                Text(variant.title)
                                Text(formatVnd(variant.priceVnd))
                            }
                        }
                    }
                }
            }
            PrimaryAction(stringResource(R.string.add_item_use_price), selected != null) { selected?.let(onSelected) }
            LinkAction(stringResource(R.string.add_item_cancel), onClick = onDismiss)
        }
    }
}
