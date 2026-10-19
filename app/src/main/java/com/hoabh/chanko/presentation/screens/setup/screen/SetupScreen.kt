package com.hoabh.chanko.presentation.screens.setup.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.setup.Secondary
import com.hoabh.chanko.presentation.screens.setup.SetupAnchorDayStep
import com.hoabh.chanko.presentation.screens.setup.SetupBudgetStep
import com.hoabh.chanko.presentation.screens.setup.SetupButton
import com.hoabh.chanko.presentation.screens.setup.SetupDayPickerBottomSheet
import com.hoabh.chanko.presentation.screens.setup.SetupHeroBanner
import com.hoabh.chanko.presentation.screens.setup.SetupPage
import com.hoabh.chanko.presentation.screens.setup.SetupProblemText
import com.hoabh.chanko.presentation.screens.setup.SetupStatusScreen
import com.hoabh.chanko.presentation.screens.setup.SetupTopBar
import com.hoabh.chanko.presentation.screens.setup.SetupUiState
import com.hoabh.chanko.presentation.screens.setup.SetupViewModel
import com.hoabh.chanko.presentation.screens.setup.findActivity
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme

@Composable
fun SetupGate(
    accountKey: String,
    onConfirmedComplete: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val model: SetupViewModel = viewModel(key = "setup:$accountKey")
    val state by model.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(model) {
        if (model.uiState.value.page == SetupPage.CHECKING && !model.uiState.value.busy) {
            model.checkStatus()
        }
    }

    val context = LocalContext.current
    DisposableEffect(model) {
        onDispose {
            val activity = context.findActivity()
            if (activity?.isChangingConfigurations != true) {
                model.discard()
            }
        }
    }

    when (state.page) {
        SetupPage.COMPLETE -> onConfirmedComplete()
        SetupPage.FORM -> InitialSetupScreen(
            state = state,
            onAmountChanged = model::amountChanged,
            onDayChanged = model::dayChanged,
            onContinue = model::continueToDay,
            onBack = model::back,
            onSave = model::save,
            modifier = modifier,
        )
        else -> SetupStatusScreen(
            state = state,
            onRetry = model::checkStatus,
            modifier = modifier,
        )
    }
}

@Composable
fun InitialSetupScreen(
    state: SetupUiState,
    onAmountChanged: (String) -> Unit,
    onDayChanged: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    var showDays by rememberSaveable { mutableStateOf(false) }

    val focus = remember { FocusRequester() }
    val bring = remember { BringIntoViewRequester() }
    val scroll = rememberScrollState()

    LaunchedEffect(state.step) {
        scroll.scrollTo(0)
    }

    LaunchedEffect(state.amountError, state.dayError) {
        if (state.amountError || state.dayError) {
            focus.requestFocus()
            bring.bringIntoView()
        }
    }

    BackHandler(enabled = state.step == 2 || state.busy) {
        if (!state.busy) onBack()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
            .safeDrawingPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SetupTopBar(
                step = state.step,
                busy = state.busy,
                onBack = onBack,
            )

            SetupHeroBanner(
                step = state.step,
                imeVisible = imeVisible,
                density = density,
                maxHeight = this@BoxWithConstraints.maxHeight,
            )

            Column(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .padding(CandySpace.section),
            ) {
                if (state.step == 1) {
                    SetupBudgetStep(
                        amount = state.amount,
                        amountError = state.amountError,
                        busy = state.busy,
                        onAmountChanged = onAmountChanged,
                        onContinue = onContinue,
                        keyboard = keyboard,
                        focusRequester = focus,
                        bringIntoViewRequester = bring,
                    )
                } else {
                    SetupAnchorDayStep(
                        day = state.day,
                        dayError = state.dayError,
                        amount = state.amount,
                        busy = state.busy,
                        onOpenDayPicker = { showDays = true },
                        keyboard = keyboard,
                        focusRequester = focus,
                        bringIntoViewRequester = bring,
                    )
                }

                state.problem?.let { problem ->
                    Spacer(Modifier.height(CandySpace.md))
                    SetupProblemText(problem)
                }

                Spacer(Modifier.height(CandySpace.section))

                val labelRes = when {
                    state.busy -> R.string.setup_saving
                    state.step == 1 -> R.string.setup_continue
                    state.problem != null -> R.string.setup_retry
                    else -> R.string.setup_save
                }

                SetupButton(
                    text = stringResource(labelRes),
                    onClick = {
                        keyboard?.hide()
                        if (state.step == 1) onContinue() else onSave()
                    },
                    enabled = !state.busy,
                    busy = state.busy,
                )

                Spacer(Modifier.height(CandySpace.md))

                val footerRes = if (state.step == 1) {
                    R.string.setup_budget_footer
                } else {
                    R.string.setup_online_hint
                }
                Text(
                    text = stringResource(footerRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = Secondary,
                )
            }
        }
    }

    if (showDays && !state.busy) {
        SetupDayPickerBottomSheet(
            selectedDay = state.day,
            onDaySelected = { day ->
                onDayChanged(day)
                showDays = false
            },
            onDismiss = { showDays = false },
        )
    }
}

@Preview(widthDp = 393, heightDp = 852, locale = "vi")
@Composable
private fun BudgetPreview() {
    ChankoTheme {
        InitialSetupScreen(
            state = SetupUiState(page = SetupPage.FORM),
            onAmountChanged = {},
            onDayChanged = {},
            onContinue = {},
            onBack = {},
            onSave = {},
        )
    }
}

@Preview(widthDp = 393, heightDp = 852, locale = "vi")
@Composable
private fun DayPreview() {
    ChankoTheme {
        InitialSetupScreen(
            state = SetupUiState(page = SetupPage.FORM, step = 2, amount = "3000000"),
            onAmountChanged = {},
            onDayChanged = {},
            onContinue = {},
            onBack = {},
            onSave = {},
        )
    }
}

@Preview(widthDp = 320, heightDp = 568, fontScale = 2f, locale = "vi")
@Composable
private fun LargeTextPreview() {
    DayPreview()
}
