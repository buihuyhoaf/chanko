package com.hoabh.chanko.presentation.features.additem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

internal fun formatVnd(value: Long): String = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value) + " ₫"

@Composable
fun AddItemRoute(step: AddItemStep, viewModel: AddItemViewModel, onEffect: (AddItemEffect) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectAddItemEffects(viewModel, onEffect)
    LaunchedEffect(state.active) { if (!state.active) onEffect(AddItemEffect.Exit) }
    BackHandler { viewModel.cancel() }
    AddItemFlowScreen(state, step, viewModel::cancel, viewModel::edit, viewModel::continueForm,
        onEvaluate = { viewModel.evaluate() }, onEarly = { viewModel.evaluate(early = true) },
        onAnswer = viewModel::answer, onPhotoError = viewModel::photoFailed)
}

@Composable
fun AddItemFlowScreen(state: AddItemUiState, step: AddItemStep, onBack: () -> Unit,
    onEdit: ((AddItemFormState) -> AddItemFormState) -> Unit, onContinue: () -> Unit,
    onEvaluate: () -> Unit, onEarly: () -> Unit, onAnswer: (String, String) -> Unit, onPhotoError: () -> Unit) {
    val title = stringResource(when (step) {
        AddItemStep.VERIFY_INFO -> R.string.add_item_verify_title
        AddItemStep.MANUAL_INPUT -> R.string.add_item_manual_title
        AddItemStep.QUICK_CHECK -> R.string.add_item_quick_title
        AddItemStep.DECISION_RESULT -> R.string.add_item_result_title
        AddItemStep.URL_DIALOG -> R.string.add_item_title
    })
    BoxWithConstraints(Modifier.fillMaxSize().imePadding()) {
        val inlineFooter = maxHeight < 480.dp || LocalDensity.current.fontScale >= 1.5f
        val footer: @Composable () -> Unit = {
            if (state.active) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(CandySpace.lg)) {
                    when (step) {
                        AddItemStep.MANUAL_INPUT, AddItemStep.VERIFY_INFO -> PrimaryAction(stringResource(R.string.add_item_continue),
                            state.form.canContinue && !state.assessing, onContinue)
                        AddItemStep.QUICK_CHECK -> {
                            val question = state.evaluation as? EvaluationState.Question
                            PrimaryAction(stringResource(R.string.add_item_show_result),
                                !state.assessing && question != null && state.form.answers[question.question.id] != null, onEvaluate)
                        }
                        AddItemStep.DECISION_RESULT -> PrimaryAction(stringResource(R.string.add_item_save), enabled = false, onClick = {})
                        AddItemStep.URL_DIALOG -> Unit
                    }
                }
            }
        }
        Scaffold(containerColor = CandyColors.background,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_02,
                            contentDescription = stringResource(R.string.add_item_back)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = CandyColors.textPrimary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }
            },
            bottomBar = {
                if (!inlineFooter && state.active) {
                    Column(Modifier.navigationBarsPadding()) {
                        HorizontalDivider(color = CandyColors.outline)
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { footer() }
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = if (maxWidth >= 600.dp) CandySpace.section else CandySpace.lg),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(vertical = CandySpace.lg),
                    verticalArrangement = Arrangement.Top) {
                    if (state.photoError) Text(stringResource(R.string.add_item_photo_error),
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = CandyColors.errorText)
                    if (state.active) when (step) {
                        AddItemStep.VERIFY_INFO -> VerifyItemInfoScreen(state.form, !state.assessing, onEdit, onPhotoError)
                        AddItemStep.MANUAL_INPUT -> ManualItemInputScreen(state.form, !state.assessing, onEdit, onPhotoError)
                        AddItemStep.QUICK_CHECK -> QuickCheckScreen(state.form, state.evaluation, onAnswer,
                            earlyEnabled = !state.assessing && state.evaluation is EvaluationState.Question, onEarly = onEarly)
                        AddItemStep.DECISION_RESULT -> DecisionResultScreen(state.form, onEdit)
                        AddItemStep.URL_DIALOG -> Unit
                    }
                    if (inlineFooter) {
                        Spacer(Modifier.height(CandySpace.lg))
                        HorizontalDivider(color = CandyColors.outline)
                        footer()
                    }
                }
            }
        }
    }
}
