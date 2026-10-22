package com.hoabh.chanko.presentation.screens.setup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
internal fun SetupAnchorDayStep(
    day: Int?,
    dayError: Boolean,
    amount: String,
    busy: Boolean,
    onOpenDayPicker: () -> Unit,
    keyboard: SoftwareKeyboardController?,
    focusRequester: FocusRequester,
    bringIntoViewRequester: BringIntoViewRequester,
    modifier: Modifier = Modifier,
) {
    val dayErrorText = stringResource(R.string.setup_day_error)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.setup_day_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Ink,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(CandySpace.sm))
        Text(
            text = stringResource(R.string.setup_day_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Secondary,
        )
        Spacer(Modifier.height(CandySpace.section))
        Text(
            text = stringResource(R.string.setup_day_label),
            style = MaterialTheme.typography.titleSmall,
            color = Ink,
        )
        Spacer(Modifier.height(CandySpace.sm))
        OutlinedButton(
            onClick = {
                keyboard?.hide()
                onOpenDayPicker()
            },
            enabled = !busy,
            shape = CandyShape.input,
            contentPadding = PaddingValues(CandySpace.lg),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .focusRequester(focusRequester)
                .bringIntoViewRequester(bringIntoViewRequester)
                .semantics {
                    if (dayError) error(dayErrorText)
                },
        ) {
            Text(
                text = if (day == null) {
                    stringResource(R.string.setup_day_placeholder)
                } else {
                    stringResource(R.string.setup_day_value, day)
                },
                color = Ink,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = Ink,
            )
        }
        if (dayError) {
            SetupErrorText(dayErrorText)
        }
        Spacer(Modifier.height(CandySpace.lg))
        Row {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(CandyColors.primary),
            )
            Spacer(Modifier.width(CandySpace.md))
            Column {
                Text(
                    text = stringResource(R.string.setup_day_fixed),
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.setup_day_notice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
        }
        Spacer(Modifier.height(CandySpace.lg))
        Text(
            text = stringResource(R.string.setup_budget_summary, groupedVnd(amount)),
            style = MaterialTheme.typography.bodyMedium,
            color = Secondary,
        )
    }
}
