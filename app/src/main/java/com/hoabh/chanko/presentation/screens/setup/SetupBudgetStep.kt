package com.hoabh.chanko.presentation.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace
import kotlinx.coroutines.launch

@Composable
internal fun SetupBudgetStep(
    amount: String,
    amountError: Boolean,
    busy: Boolean,
    onAmountChanged: (String) -> Unit,
    onContinue: () -> Unit,
    keyboard: SoftwareKeyboardController?,
    focusRequester: FocusRequester,
    bringIntoViewRequester: BringIntoViewRequester,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val budgetLabel = stringResource(R.string.setup_budget_label)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.setup_budget_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(CandySpace.sm))
        Text(
            text = stringResource(R.string.setup_budget_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Secondary,
        )
        Spacer(Modifier.height(CandySpace.section))
        Text(
            text = budgetLabel,
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
        Spacer(Modifier.height(CandySpace.sm))
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChanged,
            enabled = !busy,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.setup_budget_placeholder)) },
            suffix = { Text(stringResource(R.string.setup_currency)) },
            textStyle = MaterialTheme.typography.headlineLarge.copy(color = Ink),
            visualTransformation = VndGrouping,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    onContinue()
                }
            ),
            isError = amountError,
            shape = CandyShape.input,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .semantics { contentDescription = budgetLabel }
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        scope.launch { bringIntoViewRequester.bringIntoView() }
                    }
                },
        )
        Spacer(Modifier.height(CandySpace.sm))
        Text(
            text = stringResource(R.string.setup_budget_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Secondary,
        )
        if (amountError) {
            SetupErrorText(stringResource(R.string.setup_budget_error))
        }
    }
}
