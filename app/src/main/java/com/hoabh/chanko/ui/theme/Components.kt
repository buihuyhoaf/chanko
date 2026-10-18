package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Static interaction in foundation: no added motion, including when system motion is disabled.
// Minimum height, never fixed height, allows multiline labels and font scaling.
@Composable
fun CandyPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = CandySize.primaryButton)
        .sizeIn(minWidth = CandySize.touchTarget), enabled = enabled,
        shape = CandyShape.primaryButton, content = content)
}
