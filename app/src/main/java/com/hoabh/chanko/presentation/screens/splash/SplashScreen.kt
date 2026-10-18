package com.hoabh.chanko.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme

/** Readiness comes from the existing Overview data stream, never an animation or a timer. */
@Composable
fun SplashScreen(isReady: Boolean, onSplashFinished: () -> Unit) {
    val finish by rememberUpdatedState(onSplashFinished)
    var finished by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isReady) {
        if (isReady && !finished) {
            finished = true
            finish()
        }
    }
    SplashContent(isLoading = !isReady)
}

// Reference artwork colors local to this screen; not new approved design-system tokens.
private val LogoBrown = Color(0xFF60362F)
private val LogoCoral = Color(0xFFB32945)

/** All layers are static, including loading text: identical with reduced motion enabled. */
@Composable
internal fun SplashContent(modifier: Modifier = Modifier, isLoading: Boolean = true) {
    val cream = MaterialTheme.colorScheme.background
    Box(modifier.fillMaxSize().background(cream)) {
        Image(
            painterResource(R.drawable.chanko_splash_room), contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
        )
        // Keep native copy readable across background crops and window sizes.
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(cream.copy(alpha = .12f), Color.Transparent, cream.copy(alpha = .8f))
        )))
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val mascotHeight = (maxHeight * .46f).coerceIn(120.dp, 360.dp)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = CandySpace.screen, vertical = CandySpace.section),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CandySpace.lg, Alignment.CenterVertically)
            ) {
                Surface(
                    shape = CandyShape.hero, color = cream,
                    shadowElevation = CandyElevation.medium,
                    modifier = Modifier.widthIn(max = 360.dp)
                ) {
                    Column(
                        Modifier.padding(horizontal = CandySpace.section, vertical = CandySpace.md)
                            .semantics(mergeDescendants = true) { heading() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.splash_title_first),
                            style = MaterialTheme.typography.displayLarge, color = LogoBrown,
                            textAlign = TextAlign.Center)
                        Text(stringResource(R.string.splash_title_second),
                            style = MaterialTheme.typography.displayLarge, color = LogoCoral,
                            textAlign = TextAlign.Center)
                    }
                }
                Surface(shape = CandyShape.chip, color = cream.copy(alpha = .96f)) {
                    Text(stringResource(R.string.splash_tagline),
                        style = MaterialTheme.typography.bodyLarge, color = LogoBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = CandySpace.lg, vertical = CandySpace.xs))
                }
                Image(
                    painterResource(R.drawable.chanko_splash_cat_bag), contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(mascotHeight).widthIn(max = 360.dp),
                    contentScale = ContentScale.Fit
                )
                Surface(shape = CandyShape.card, color = cream.copy(alpha = .96f)) {
                    Column(
                        Modifier.padding(CandySpace.lg).widthIn(max = 360.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                    ) {
                        if (isLoading) {
                            Text(stringResource(R.string.splash_loading),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                        Text(stringResource(R.string.splash_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Preview(name = "Splash compact", widthDp = 360, heightDp = 640)
@Preview(name = "Splash 150%", widthDp = 360, heightDp = 640, fontScale = 1.5f)
@Preview(name = "Splash 200%", widthDp = 320, heightDp = 568, fontScale = 2f)
@Preview(name = "Splash landscape", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun SplashPreview() { ChankoTheme { SplashContent() } }
