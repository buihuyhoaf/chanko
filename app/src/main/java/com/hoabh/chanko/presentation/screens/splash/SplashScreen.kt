package com.hoabh.chanko.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.drawWithContent
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
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
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme

/** Show the user-approved one-second brand introduction after the scene's first draw.
 * Loading copy remains tied only to real data work; readiness changes do not restart branding.
 */
@Composable
fun SplashScreen(isReady: Boolean, onSplashFinished: () -> Unit) {
    val finish by rememberUpdatedState(onSplashFinished)
    var finished by rememberSaveable { mutableStateOf(false) }
    var presentationComplete by rememberSaveable { mutableStateOf(false) }
    var sceneDrawn by remember { mutableStateOf(false) }

    LaunchedEffect(sceneDrawn) {
        if (sceneDrawn && !presentationComplete) {
            // Start after a rendered scene, not while decoding assets or under the platform icon.
            withFrameNanos { }
            delay(1_000)
            presentationComplete = true
        }
    }
    LaunchedEffect(isReady, presentationComplete) {
        if (isReady && presentationComplete && !finished) {
            finished = true
            finish()
        }
    }
    SplashContent(
        modifier = Modifier.drawWithContent {
            drawContent()
            if (!sceneDrawn) sceneDrawn = true
        },
        isLoading = !isReady
    )
}

// Local colors and logo sizing reproduce the user's artwork, not new global theme tokens.
private val LogoBrown = Color(0xFF60362F)
private val LogoCoral = Color(0xFFD63F55)

/** Static layers also provide the reduced-motion presentation. No simulated progress. */
@Composable
internal fun SplashContent(modifier: Modifier = Modifier, isLoading: Boolean = true) {
    val cream = MaterialTheme.colorScheme.background
    BoxWithConstraints(modifier.fillMaxSize().background(cream)) {
        Image(
            painterResource(R.drawable.chanko_splash_room), contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = if (maxWidth > maxHeight) ContentScale.Crop else ContentScale.FillBounds
        )
        // Suppress decoration when large text or landscape needs the space for native copy.
        if (LocalDensity.current.fontScale <= 1.3f && maxHeight > maxWidth) {
            Image(
                painterResource(R.drawable.chanko_splash_decorations), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit
            )
        }
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val compact = maxHeight < 700.dp
            val logoSize = if (compact) 48.sp else 60.sp
            val mascotHeight = (maxHeight * .46f).coerceIn(120.dp, 400.dp)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = CandySpace.screen, vertical = CandySpace.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CandySpace.sm, Alignment.CenterVertically)
            ) {
                Spacer(Modifier.height(if (compact) 0.dp else CandySpace.section))
                Column(
                    Modifier.padding(8.dp).semantics(mergeDescendants = true) { heading() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedLogoWord(stringResource(R.string.splash_title_first), LogoBrown, logoSize)
                    OutlinedLogoWord(stringResource(R.string.splash_title_second), LogoCoral, logoSize)
                }
                Text(stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(cream, Offset.Zero, 8f)),
                    color = LogoBrown, textAlign = TextAlign.Center)
                Image(
                    painterResource(R.drawable.chanko_splash_cat_bag), contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(mascotHeight),
                    contentScale = ContentScale.Fit
                )
                Column(
                    Modifier.widthIn(max = 360.dp).padding(top = CandySpace.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                ) {
                    if (isLoading) {
                        Text(stringResource(R.string.splash_loading),
                            style = MaterialTheme.typography.labelMedium.copy(
                                shadow = Shadow(cream, Offset.Zero, 8f)),
                            color = LogoBrown, textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    Text(stringResource(R.string.splash_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(cream, Offset.Zero, 8f)),
                        color = LogoBrown, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** Two native text passes create the cream sticker edge without rasterizing the lettering. */
@Composable
private fun OutlinedLogoWord(text: String, color: Color, size: TextUnit) {
    val stroke = with(LocalDensity.current) { 7.dp.toPx() }
    val style = MaterialTheme.typography.displayLarge.copy(
        fontSize = size, lineHeight = size * 1.08f, fontWeight = FontWeight.ExtraBold,
        shadow = Shadow(Color(0x5560362F), Offset(0f, 4f), 4f))
    Box(contentAlignment = Alignment.Center) {
        Text(text, style = style.copy(drawStyle = Stroke(width = stroke, join = StrokeJoin.Round)),
            color = MaterialTheme.colorScheme.background, textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { })
        Text(text, style = style, color = color, textAlign = TextAlign.Center)
    }
}

@Preview(name = "Splash room portrait", widthDp = 393, heightDp = 852)
@Preview(name = "Splash room compact", widthDp = 360, heightDp = 640)
@Preview(name = "Splash room 200%", widthDp = 320, heightDp = 568, fontScale = 2f)
@Preview(name = "Splash room landscape", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
fun SplashRoomPreview() { ChankoTheme { SplashContent() } }
