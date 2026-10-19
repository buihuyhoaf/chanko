package com.hoabh.chanko.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hoabh.chanko.presentation.OverviewUiState
import com.hoabh.chanko.presentation.OverviewViewModel
import com.hoabh.chanko.presentation.navigation.Screen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme
import kotlinx.coroutines.delay

private const val SPLASH_BRANDING_DURATION_MS = 2_000L
private const val MAX_DECORATION_FONT_SCALE = 1.3f
private const val LOGO_LINE_HEIGHT_RATIO = 1.08f
private const val TEXT_SHADOW_BLUR_RADIUS = 8f
private const val LOGO_SHADOW_BLUR_RADIUS = 4f
private val LogoShadowColor = CandyColors.logoBrown.copy(alpha = 0.33f)

private val CompactScreenHeight = 700.dp
private val MascotMinHeight = 120.dp
private val MascotMaxHeight = 400.dp
private const val MascotHeightFraction = 0.46f
private val SplashSubtitleMaxWidth = 360.dp
private val LogoStrokeWidth = 7.dp

private val LogoSizeCompact = 48.sp
private val LogoSizeRegular = 60.sp

fun NavGraphBuilder.splashDestination(
    overviewViewModel: OverviewViewModel,
    isRestored: () -> Boolean,
    onSplashFinished: () -> Unit
) {
    composable(
        route = Screen.Splash.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        val state by overviewViewModel.items.collectAsStateWithLifecycle()
        SplashScreen(
            isReady = isRestored() && state !is OverviewUiState.Loading,
            onSplashFinished = onSplashFinished
        )
    }
}

/** Show the user-approved two-second brand introduction after the scene's first draw.
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
            delay(SPLASH_BRANDING_DURATION_MS)
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

/** Static layers also provide the reduced-motion presentation. No simulated progress. */
@Composable
internal fun SplashContent(modifier: Modifier = Modifier, isLoading: Boolean = true) {
    val cream = MaterialTheme.colorScheme.background
    val textShadow = remember(cream) { Shadow(cream, Offset.Zero, TEXT_SHADOW_BLUR_RADIUS) }

    BoxWithConstraints(modifier.fillMaxSize().background(cream)) {
        Image(
            painterResource(R.drawable.chanko_splash_room), contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = if (maxWidth > maxHeight) ContentScale.Crop else ContentScale.FillBounds
        )
        // Suppress decoration when large text or landscape needs the space for native copy.
        if (LocalDensity.current.fontScale <= MAX_DECORATION_FONT_SCALE && maxHeight > maxWidth) {
            Image(
                painterResource(R.drawable.chanko_splash_decorations), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit
            )
        }
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val compact = maxHeight < CompactScreenHeight
            val logoSize = if (compact) LogoSizeCompact else LogoSizeRegular
            val mascotHeight = (maxHeight * MascotHeightFraction).coerceIn(MascotMinHeight, MascotMaxHeight)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = CandySpace.screen, vertical = CandySpace.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CandySpace.sm, Alignment.CenterVertically)
            ) {
                Spacer(Modifier.height(if (compact) 0.dp else CandySpace.section))
                Column(
                    Modifier.padding(CandySpace.sm).semantics(mergeDescendants = true) { heading() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedLogoWord(stringResource(R.string.splash_title_first), CandyColors.logoBrown, logoSize)
                    OutlinedLogoWord(stringResource(R.string.splash_title_second), CandyColors.logoCoral, logoSize)
                }
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = textShadow
                    ),
                    color = CandyColors.logoBrown,
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(R.drawable.chanko_splash_cat_bag),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(mascotHeight),
                    contentScale = ContentScale.Fit
                )
                Column(
                    Modifier.widthIn(max = SplashSubtitleMaxWidth).padding(top = CandySpace.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                ) {
                    if (isLoading) {
                        Text(
                            text = stringResource(R.string.splash_loading),
                            style = MaterialTheme.typography.labelMedium.copy(shadow = textShadow),
                            color = CandyColors.logoBrown,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    Text(
                        text = stringResource(R.string.splash_subtitle),
                        style = MaterialTheme.typography.bodyMedium.copy(shadow = textShadow),
                        color = CandyColors.logoBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Two native text passes create the cream sticker edge without rasterizing the lettering. */
@Composable
private fun OutlinedLogoWord(text: String, color: Color, size: TextUnit) {
    val stroke = with(LocalDensity.current) { LogoStrokeWidth.toPx() }
    val style = MaterialTheme.typography.displayLarge.copy(
        fontSize = size,
        lineHeight = size * LOGO_LINE_HEIGHT_RATIO,
        fontWeight = FontWeight.ExtraBold,
        shadow = Shadow(LogoShadowColor, Offset(0f, LOGO_SHADOW_BLUR_RADIUS), LOGO_SHADOW_BLUR_RADIUS)
    )
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = style.copy(drawStyle = Stroke(width = stroke, join = StrokeJoin.Round)),
            color = MaterialTheme.colorScheme.background,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics { }
        )
        Text(
            text = text,
            style = style,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Splash room portrait", widthDp = 393, heightDp = 852)
@Preview(name = "Splash room compact", widthDp = 360, heightDp = 640)
@Preview(name = "Splash room 200%", widthDp = 320, heightDp = 568, fontScale = 2f)
@Preview(name = "Splash room landscape", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
fun SplashRoomPreview() { ChankoTheme { SplashContent() } }
