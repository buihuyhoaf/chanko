package com.hoabh.chanko.presentation.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors

@Composable
internal fun SetupHeroBanner(
    step: Int,
    imeVisible: Boolean,
    density: Density,
    maxHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val heroHeight = when {
        imeVisible -> 104.dp
        density.fontScale >= 1.5f || maxHeight < 480.dp -> 120.dp
        else -> (maxHeight * 0.30f).coerceIn(180.dp, 260.dp)
    }

    Box(
        modifier = modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .height(heroHeight),
    ) {
        Image(
            painter = painterResource(
                if (step == 1) R.drawable.setup_savings_jar else R.drawable.setup_magic_calendar
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(24.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, CandyColors.background)
                    )
                ),
        )
    }
}
