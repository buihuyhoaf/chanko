package com.hoabh.chanko.ui.mascot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Decorative, static mascot slot; callers must express status/instructions as accessible UI text.
 * No tap actions or animation, so reduced motion is respected without a separate code path.
 * Missing assets reserve an empty slot rather than impersonating an approved character/state.
 * Caller owns placement and may shrink or omit the slot when space is limited.
 */
@Composable
fun ChankoMascot(state: MascotPresentationState, modifier: Modifier = Modifier) {
    val asset = MascotAssets.resolve(state)
    val slot = modifier.size(120.dp) // Reference default; not a new approved placement token.
    if (asset == null) {
        Spacer(slot)
    } else {
        Image(
            painter = painterResource(asset.drawableRes),
            contentDescription = null,
            modifier = slot,
            contentScale = ContentScale.Fit
        )
    }
}
