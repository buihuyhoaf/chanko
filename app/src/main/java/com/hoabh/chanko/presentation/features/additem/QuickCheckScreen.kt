package com.hoabh.chanko.presentation.features.additem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.ui.theme.*

@Composable
fun QuickCheckScreen(form: AddItemFormState, evaluation: EvaluationState,
    onAnswer: (String, String) -> Unit = { _, _ -> }, earlyEnabled: Boolean = false, onEarly: () -> Unit = {}) {
    ReviewCard(Modifier.padding(top = CandySpace.lg)) { ProductSummary(form) }
    if (evaluation is EvaluationState.Question) {
        HelpText(stringResource(R.string.add_item_question_index, evaluation.index + 1), Modifier.padding(top = CandySpace.section))
        Spacer(Modifier.height(CandySpace.section))
        MascotPlaceholder()
        Spacer(Modifier.height(CandySpace.section))
        Text(evaluation.question.text, style = MaterialTheme.typography.headlineMedium,
            color = CandyColors.textPrimary, modifier = Modifier.semantics { heading() })
        Column(Modifier.selectableGroup().padding(top = CandySpace.md), verticalArrangement = Arrangement.spacedBy(CandySpace.md)) {
            evaluation.question.choices.forEach { choice ->
                val selected = form.answers[evaluation.question.id] == choice
                Surface(shape = CandyShape.input, color = if (selected) CandyColors.primarySoft else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) CandyColors.primaryInk else CandyColors.outline)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp)
                        .selectable(selected, role = Role.RadioButton, onClick = { onAnswer(evaluation.question.id, choice) })
                        .padding(CandySpace.lg), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.md)) {
                        RadioButton(selected, onClick = null, modifier = Modifier.size(22.dp),
                            colors = RadioButtonDefaults.colors(selectedColor = CandyColors.primaryInk))
                        Text(choice, style = MaterialTheme.typography.bodyLarge, color = CandyColors.textPrimary)
                    }
                }
            }
        }
    } else {
        Spacer(Modifier.height(CandySpace.section))
        MascotPlaceholder()
        Spacer(Modifier.height(CandySpace.section))
        Surface(shape = CandyShape.input, color = CandyColors.primarySoft) {
            Column(Modifier.padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
                Text(stringResource(R.string.add_item_assessment_unavailable), style = MaterialTheme.typography.titleLarge,
                    color = CandyColors.textPrimary, modifier = Modifier.semantics { heading() })
                HelpText(stringResource(R.string.add_item_assessment_help))
            }
        }
    }
    LinkAction(stringResource(R.string.add_item_early), enabled = earlyEnabled, onClick = onEarly)
    HelpText(stringResource(R.string.add_item_early_help))
}
