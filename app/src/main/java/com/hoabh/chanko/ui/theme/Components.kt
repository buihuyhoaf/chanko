package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.hoabh.chanko.R

enum class CandyButtonVariant { Primary, Secondary, Outlined, Text, Destructive }

/** Loading retains the action label and disables activation; callers own operation state. */
@Composable
fun CandyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CandyButtonVariant = CandyButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val pending = stringResource(R.string.component_loading)
    val bounds = modifier.sizeIn(minWidth = CandySize.touchTarget,
        minHeight = if (variant == CandyButtonVariant.Primary) CandySize.primaryButton else CandySize.touchTarget)
        .semantics { if (loading) { stateDescription = pending; liveRegion = LiveRegionMode.Polite } }
    val active = enabled && !loading
    // Unspecified variant radii use the secondary token as a Reference implementation choice.
    val shape = if (variant == CandyButtonVariant.Primary) CandyShape.primaryButton else CandyShape.secondaryButton
    val body: @Composable RowScope.() -> Unit = {
        Column {
            Row(content = content)
            if (loading) Text(pending)
        }
    }
    when (variant) {
        CandyButtonVariant.Outlined -> OutlinedButton(onClick, bounds, active, shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface), content = body)
        CandyButtonVariant.Text -> TextButton(onClick, bounds, active, shape = shape,
            colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurface), content = body)
        else -> {
            val container = when (variant) {
                CandyButtonVariant.Secondary -> colors.secondary
                CandyButtonVariant.Destructive -> colors.error
                else -> colors.primary
            }
            val foreground = when (variant) {
                CandyButtonVariant.Secondary -> colors.onSecondary
                CandyButtonVariant.Destructive -> colors.onError
                else -> colors.onPrimary
            }
            Button(onClick, bounds, active, shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = foreground),
                content = body)
        }
    }
}

/** Compatibility entry point used by existing screens. Height can grow with text. */
@Composable
fun CandyPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit
) = CandyButton(onClick, modifier, enabled = enabled, loading = loading, content = content)
