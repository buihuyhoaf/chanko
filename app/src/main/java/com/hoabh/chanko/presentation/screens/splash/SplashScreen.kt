package com.hoabh.chanko.presentation.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val finish by rememberUpdatedState(onSplashFinished)
    // Existing 1800ms launch delay, independent of art, font scale and motion settings.
    LaunchedEffect(Unit) {
        delay(1800)
        finish()
    }
    SplashContent()
}

/** Static art is also the reduced-motion presentation; no decorative loop or fake progress. */
@Composable
internal fun SplashContent(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()
    ) {
        // Reference placement: shrink decoration in short windows, retaining readable text.
        val mascotSize = if (maxHeight < 480.dp) 120.dp else 220.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = CandySpace.screen, vertical = CandySpace.section),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CandySpace.section, Alignment.CenterVertically)
        ) {
            Box(
                Modifier.size(mascotSize)
                    .shadow(CandyElevation.medium, CandyShape.hero)
                    .background(MaterialTheme.colorScheme.primary, CandyShape.hero)
                    .padding(CandySpace.lg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(R.drawable.chanko_splash_mascot),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                stringResource(R.string.splash_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().semantics { heading() }
            )
            Text(
                stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Splash compact", widthDp = 360, heightDp = 640)
@Preview(name = "Splash 150%", widthDp = 360, heightDp = 640, fontScale = 1.5f)
@Preview(name = "Splash 200%", widthDp = 320, heightDp = 568, fontScale = 2f)
@Preview(name = "Splash landscape", widthDp = 640, heightDp = 360, fontScale = 2f)
@Composable
private fun SplashPreview() { ChankoTheme { SplashContent() } }
