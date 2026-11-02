package com.hoabh.chanko.presentation.screens.additem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.presentation.designsystem.icon.*
import com.hoabh.chanko.ui.theme.*

@Composable
fun DecisionResultScreen(form: AddItemFormState, onEdit: ((AddItemFormState) -> AddItemFormState) -> Unit) {
    ReviewCard(Modifier.padding(top = CandySpace.lg)) { ProductSummary(form) }
    val snapshot = form.assessment
    if (snapshot == null || snapshot.productRevision != form.productRevision || snapshot.answerRevision != form.answerRevision) {
        Spacer(Modifier.height(CandySpace.section))
        Text(stringResource(R.string.add_item_assessment_unavailable), style = MaterialTheme.typography.titleLarge)
        HelpText(stringResource(R.string.add_item_assessment_help))
        return
    }
    val (color, label, icon) = when (snapshot.verdict) {
        Verdict.BUY -> Triple(CandyColors.yellow, R.string.add_item_buy, ChankoIconId.ITM_09)
        Verdict.WAIT -> Triple(CandyColors.blue, R.string.add_item_wait, ChankoIconId.ITM_10)
        Verdict.SKIP -> Triple(CandyColors.mint, R.string.add_item_skip, ChankoIconId.ITM_11)
    }
    Spacer(Modifier.height(CandySpace.section))
    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(CandySpace.md), color = color) {
        Row(Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            ChankoIcon(id = icon, contentDescription = null)
            Text(snapshot.verdict.name, style = MaterialTheme.typography.labelLarge, color = CandyColors.textPrimary)
        }
    }
    Spacer(Modifier.height(CandySpace.section))
    Text(stringResource(label), style = MaterialTheme.typography.headlineMedium,
        color = CandyColors.textPrimary, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(CandySpace.section))
    MascotPlaceholder()
    Spacer(Modifier.height(CandySpace.section))
    Text(stringResource(R.string.add_item_result_reason), style = MaterialTheme.typography.titleLarge, color = CandyColors.textPrimary)
    snapshot.reasons.forEach { Text(it, Modifier.padding(top = CandySpace.md), style = MaterialTheme.typography.bodyLarge, color = CandyColors.textPrimary) }
    if (snapshot.limitations.isNotEmpty()) {
        Surface(Modifier.padding(top = CandySpace.lg), shape = CandyShape.input, color = CandyColors.primarySoft) {
            Column(Modifier.fillMaxWidth().padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
                Text(stringResource(R.string.add_item_limits), style = MaterialTheme.typography.labelLarge, color = CandyColors.textPrimary)
                snapshot.limitations.forEach { HelpText(it) }
            }
        }
    }
    if (snapshot.verdict == Verdict.WAIT) CoolingChoiceCard(form, onEdit)
    HelpText(stringResource(R.string.add_item_not_purchase), Modifier.padding(top = CandySpace.section))
    if (snapshot.verdict == Verdict.WAIT && form.coolingChoice == CoolingChoice.UNSELECTED) {
        HelpText(stringResource(R.string.add_item_cooling_helper), Modifier.padding(top = CandySpace.sm))
    }
    HelpText(stringResource(R.string.add_item_save_unavailable), Modifier.padding(top = CandySpace.sm))
}

@Composable
private fun CoolingChoiceCard(form: AddItemFormState, onEdit: ((AddItemFormState) -> AddItemFormState) -> Unit) {
    var showCooling by remember { mutableStateOf(false) }
    ReviewCard(Modifier.padding(top = CandySpace.lg)) {
        Text(stringResource(R.string.add_item_cooling_title), style = MaterialTheme.typography.titleLarge, color = CandyColors.textPrimary)
        Text(stringResource(if (form.coolingChoice == CoolingChoice.NOT_APPLIED) R.string.add_item_no_cooling
            else R.string.add_item_cooling_unselected), Modifier.padding(top = CandySpace.md),
            style = MaterialTheme.typography.bodyLarge, color = CandyColors.textPrimary)
        SecondaryAction(stringResource(R.string.add_item_cooling_edit), Modifier.padding(top = CandySpace.md)) { showCooling = true }
        LinkAction(stringResource(R.string.add_item_cooling_none)) { onEdit { it.copy(coolingChoice = CoolingChoice.NOT_APPLIED, coolingDurationMillis = null) } }
        HelpText(stringResource(R.string.add_item_cooling_explanation))
    }
    if (showCooling) CoolingDurationSheet(onDismiss = { showCooling = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoolingDurationSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(CandySpace.section), verticalArrangement = Arrangement.spacedBy(CandySpace.md)) {
            Text(stringResource(R.string.add_item_cooling_title), style = MaterialTheme.typography.headlineMedium)
            HelpText(stringResource(R.string.add_item_cooling_pending))
            PrimaryAction(stringResource(R.string.add_item_apply), enabled = false, onClick = {})
            LinkAction(stringResource(R.string.add_item_cancel), onClick = onDismiss)
        }
    }
}
