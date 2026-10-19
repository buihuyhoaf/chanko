package com.hoabh.chanko.presentation.screens.setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.ChankoTheme
import kotlinx.coroutines.launch

// Screen-scoped visual reference colors from the reviewed mockup; DS PV-01 stays open.
private val Ink = Color(0xFF30233E)
private val Secondary = Color(0xFF62596D)
private val OnPurple = Color(0xFF15131D)

@Composable
fun SetupGate(accountKey: String, onConfirmedComplete: @Composable () -> Unit) {
    val model: SetupViewModel = viewModel(key = "setup:$accountKey")
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(model) { if (model.state.value.page == SetupPage.CHECKING && !model.state.value.busy) model.checkStatus() }
    val context = LocalContext.current
    DisposableEffect(model) { onDispose {
        var current = context
        while (current is android.content.ContextWrapper && current !is android.app.Activity) current = current.baseContext
        if ((current as? android.app.Activity)?.isChangingConfigurations != true) model.discard()
    } }
    when (state.page) {
        SetupPage.COMPLETE -> onConfirmedComplete()
        SetupPage.FORM -> InitialSetupScreen(state, model::amountChanged, model::dayChanged,
            model::continueToDay, model::back, model::save)
        else -> SetupStatusScreen(state, model::checkStatus)
    }
}

@Composable
fun SetupStatusScreen(state: SetupUiState, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().background(CandyColors.background).safeDrawingPadding()
        .verticalScroll(rememberScrollState()).padding(CandySpace.section),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if (state.page == SetupPage.CHECKING) {
            CircularProgressIndicator(color = CandyColors.primary)
            Spacer(Modifier.height(CandySpace.lg))
            Text(stringResource(R.string.setup_checking), color = Ink,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        } else {
            Text(stringResource(R.string.setup_status_title), style = MaterialTheme.typography.headlineMedium,
                color = Ink, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(CandySpace.md))
            ProblemText(state.problem ?: SetupProblem.UNKNOWN)
            Spacer(Modifier.height(CandySpace.lg))
            SetupButton(stringResource(R.string.setup_retry), onRetry)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun InitialSetupScreen(
    state: SetupUiState,
    onAmountChanged: (String) -> Unit,
    onDayChanged: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    var showDays by rememberSaveable { mutableStateOf(false) }
    val dayErrorText = stringResource(R.string.setup_day_error)
    val focus = remember { FocusRequester() }
    val bring = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    // Keep forms and CTA in the same scroll container. IME reduces the available viewport.
    val scroll = rememberScrollState()
    LaunchedEffect(state.step) { scroll.scrollTo(0) }
    LaunchedEffect(state.amountError, state.dayError) {
        if (state.amountError || state.dayError) { focus.requestFocus(); bring.bringIntoView() }
    }
    BackHandler(enabled = state.step == 2 || state.busy) { if (!state.busy) onBack() }
    BoxWithConstraints(Modifier.fillMaxSize().background(CandyColors.background).safeDrawingPadding().imePadding()) {
        val heroHeight = when {
            imeVisible -> 104.dp
            density.fontScale >= 1.5f || maxHeight < 480.dp -> 120.dp
            else -> (maxHeight * .30f).coerceIn(180.dp, 260.dp)
        }
        Column(Modifier.fillMaxSize().verticalScroll(scroll), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.widthIn(max = 600.dp).fillMaxWidth().padding(horizontal = CandySpace.section)
                .heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                if (state.step == 2) IconButton(onClick = onBack, enabled = !state.busy, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), tint = Ink)
                }
                Text(stringResource(R.string.setup_step, state.step), style = MaterialTheme.typography.labelMedium,
                    color = Ink, modifier = Modifier.weight(1f))
                Row(Modifier.clearAndSetSemantics { }, horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                    (1..2).forEach { step -> Box(Modifier.size(24.dp, 4.dp).background(
                        if (step <= state.step) CandyColors.primary else Color(0xFFD9D1E4), RoundedCornerShape(4.dp))) }
                }
            }
            Box(Modifier.widthIn(max = 600.dp).fillMaxWidth().height(heroHeight)) {
                Image(painterResource(if (state.step == 1) R.drawable.setup_savings_jar else R.drawable.setup_magic_calendar),
                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(24.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, CandyColors.background))))
            }
            Column(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(CandySpace.section)) {
                Text(stringResource(if (state.step == 1) R.string.setup_budget_title else R.string.setup_day_title),
                    style = MaterialTheme.typography.headlineLarge, color = Ink, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(CandySpace.sm))
                Text(stringResource(if (state.step == 1) R.string.setup_budget_subtitle else R.string.setup_day_subtitle),
                    style = MaterialTheme.typography.bodyLarge, color = Secondary)
                Spacer(Modifier.height(CandySpace.section))
                if (state.step == 1) {
                    val budgetLabel = stringResource(R.string.setup_budget_label)
                    Text(budgetLabel, style = MaterialTheme.typography.titleSmall, color = Ink)
                    Spacer(Modifier.height(CandySpace.sm))
                    OutlinedTextField(value = state.amount, onValueChange = onAmountChanged,
                        enabled = !state.busy, singleLine = true,
                        placeholder = { Text(stringResource(R.string.setup_budget_placeholder)) },
                        suffix = { Text(stringResource(R.string.setup_currency)) },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(color = Ink),
                        visualTransformation = VndGrouping,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); onContinue() }),
                        isError = state.amountError,
                        shape = CandyShape.input,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).semantics { contentDescription = budgetLabel }.focusRequester(focus)
                            .bringIntoViewRequester(bring).onFocusChanged { if (it.isFocused) scope.launch { bring.bringIntoView() } })
                    Spacer(Modifier.height(CandySpace.sm))
                    Text(stringResource(R.string.setup_budget_hint), style = MaterialTheme.typography.bodyMedium, color = Secondary)
                    if (state.amountError) ErrorText(stringResource(R.string.setup_budget_error))
                } else {
                    Text(stringResource(R.string.setup_day_label), style = MaterialTheme.typography.titleSmall, color = Ink)
                    Spacer(Modifier.height(CandySpace.sm))
                    OutlinedButton(onClick = { keyboard?.hide(); showDays = true }, enabled = !state.busy,
                        shape = CandyShape.input, contentPadding = PaddingValues(CandySpace.lg),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).focusRequester(focus).bringIntoViewRequester(bring)
                            .semantics { if (state.dayError) error(dayErrorText) }) {
                        Text(if (state.day == null) stringResource(R.string.setup_day_placeholder)
                            else stringResource(R.string.setup_day_value, state.day), color = Ink,
                            modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = Ink)
                    }
                    if (state.dayError) ErrorText(stringResource(R.string.setup_day_error))
                    Spacer(Modifier.height(CandySpace.lg))
                    Row {
                        Box(Modifier.width(3.dp).height(32.dp).background(CandyColors.primary))
                        Spacer(Modifier.width(CandySpace.md))
                        Column {
                            Text(stringResource(R.string.setup_day_fixed), fontWeight = FontWeight.Bold, color = Ink,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.setup_day_notice), style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                    Spacer(Modifier.height(CandySpace.lg))
                    Text(stringResource(R.string.setup_budget_summary, groupedVnd(state.amount)),
                        style = MaterialTheme.typography.bodyMedium, color = Secondary)
                }
                state.problem?.let { Spacer(Modifier.height(CandySpace.md)); ProblemText(it) }
                Spacer(Modifier.height(CandySpace.section))
                val label = when {
                    state.busy -> R.string.setup_saving
                    state.step == 1 -> R.string.setup_continue
                    state.problem != null -> R.string.setup_retry
                    else -> R.string.setup_save
                }
                SetupButton(stringResource(label), {
                    keyboard?.hide()
                    if (state.step == 1) onContinue() else onSave()
                }, !state.busy, state.busy)
                Spacer(Modifier.height(CandySpace.md))
                Text(stringResource(if (state.step == 1) R.string.setup_budget_footer else R.string.setup_online_hint),
                    style = MaterialTheme.typography.bodySmall, color = Secondary)
            }
        }
    }
    if (showDays && !state.busy) ModalBottomSheet(onDismissRequest = { showDays = false }, containerColor = CandyColors.background) {
        Text(stringResource(R.string.setup_day_label), style = MaterialTheme.typography.titleLarge,
            color = Ink, modifier = Modifier.padding(horizontal = CandySpace.section, vertical = CandySpace.md).semantics { heading() })
        LazyColumn(Modifier.testTag("setup_days").fillMaxWidth().weight(1f, fill = false).navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = CandySpace.section)) {
            items((1..31).toList(), key = { it }) { day ->
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .clickable(role = Role.RadioButton) { onDayChanged(day); showDays = false }
                    .semantics(mergeDescendants = true) { selected = state.day == day }
                    .padding(horizontal = CandySpace.section, vertical = CandySpace.sm), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = state.day == day, onClick = null)
                    Spacer(Modifier.width(CandySpace.lg))
                    Text(stringResource(R.string.setup_day_value, day), color = Ink)
                }
            }
        }
    }
}

@Composable
private fun SetupButton(text: String, onClick: () -> Unit, enabled: Boolean = true, busy: Boolean = false) {
    Button(onClick = onClick, enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = CandyColors.primary, contentColor = OnPurple),
        shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(CandySpace.lg),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        if (busy) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = OnPurple); Spacer(Modifier.width(CandySpace.sm)) }
        Text(text, modifier = Modifier.weight(1f, fill = false), style = MaterialTheme.typography.labelLarge)
    }
}
@Composable
private fun ErrorText(text: String) {
    Text(text, color = CandyColors.errorText, style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = CandySpace.sm).semantics { liveRegion = LiveRegionMode.Polite })
}
@Composable
private fun ProblemText(problem: SetupProblem) = ErrorText(stringResource(when (problem) {
    SetupProblem.UNKNOWN -> R.string.setup_unknown
    SetupProblem.OFFLINE -> R.string.setup_offline
    SetupProblem.SAVE_ERROR -> R.string.setup_save_error
    SetupProblem.UNAVAILABLE -> R.string.setup_unavailable
    SetupProblem.TIME_ZONE -> R.string.setup_timezone_error
}))

internal fun groupedVnd(raw: String): String = if (raw.all { it in '0'..'9' }) raw.reversed().chunked(3).joinToString(".").reversed() else raw
private object VndGrouping : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (text.text.any { it !in '0'..'9' }) return TransformedText(text, OffsetMapping.Identity)
        val formatted = groupedVnd(text.text)
        val originalOffsets = IntArray(text.length + 1)
        val transformedOffsets = IntArray(formatted.length + 1)
        var digits = 0
        formatted.forEachIndexed { index, char ->
            if (char != '.') { digits++; originalOffsets[digits] = index + 1 }
            transformedOffsets[index + 1] = digits
        }
        return TransformedText(AnnotatedString(formatted), object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = originalOffsets[offset.coerceIn(originalOffsets.indices)]
            override fun transformedToOriginal(offset: Int) = transformedOffsets[offset.coerceIn(transformedOffsets.indices)]
        })
    }
}

@Preview(widthDp = 393, heightDp = 852, locale = "vi")
@Composable private fun BudgetPreview() { ChankoTheme { InitialSetupScreen(SetupUiState(page = SetupPage.FORM), {}, {}, {}, {}, {}) } }
@Preview(widthDp = 393, heightDp = 852, locale = "vi")
@Composable private fun DayPreview() { ChankoTheme { InitialSetupScreen(SetupUiState(page = SetupPage.FORM, step = 2, amount = "3000000"), {}, {}, {}, {}, {}) } }
@Preview(widthDp = 320, heightDp = 568, fontScale = 2f, locale = "vi")
@Composable private fun LargeTextPreview() { DayPreview() }
