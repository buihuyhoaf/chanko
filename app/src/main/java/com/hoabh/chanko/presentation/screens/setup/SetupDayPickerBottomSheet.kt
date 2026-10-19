package com.hoabh.chanko.presentation.screens.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SetupDayPickerBottomSheet(
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CandyColors.background,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.setup_day_label),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier
                .padding(horizontal = CandySpace.section, vertical = CandySpace.md)
                .semantics { heading() },
        )
        LazyColumn(
            modifier = Modifier
                .testTag("setup_days")
                .fillMaxWidth()
                .weight(1f, fill = false)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = CandySpace.section),
        ) {
            items((1..31).toList(), key = { it }) { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.RadioButton) {
                            onDaySelected(day)
                        }
                        .semantics(mergeDescendants = true) {
                            selected = selectedDay == day
                        }
                        .padding(horizontal = CandySpace.section, vertical = CandySpace.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedDay == day,
                        onClick = null,
                    )
                    Spacer(Modifier.width(CandySpace.lg))
                    Text(
                        text = stringResource(R.string.setup_day_value, day),
                        color = Ink,
                    )
                }
            }
        }
    }
}
