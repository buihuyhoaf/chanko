package com.hoabh.chanko.presentation.screens.setup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
internal fun SetupTopBar(
    step: Int,
    busy: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = CandySpace.section)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (step == 2) {
            IconButton(
                onClick = onBack,
                enabled = !busy,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Ink,
                )
            }
        }
        Text(
            text = stringResource(R.string.setup_step, step),
            style = MaterialTheme.typography.labelMedium,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs),
        ) {
            (1..2).forEach { indicatorStep ->
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 4.dp)
                        .background(
                            color = if (indicatorStep <= step) CandyColors.primary else StepDotInactive,
                            shape = RoundedCornerShape(4.dp),
                        ),
                )
            }
        }
    }
}
