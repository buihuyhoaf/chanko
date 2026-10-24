package com.hoabh.chanko.presentation.screens.login.screen

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.BuildConfig
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.domain.auth.AuthFailure
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.navigation.Screen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hoabh.chanko.presentation.screens.login.viewmodel.LoginUiEffect
import com.hoabh.chanko.presentation.screens.login.viewmodel.LoginUiState
import com.hoabh.chanko.presentation.screens.login.viewmodel.LoginViewModel
import com.hoabh.chanko.ui.theme.ChankoTheme
import com.hoabh.chanko.ui.theme.Nunito

private val LoginPlum = Color(0xFF4D2939)

fun NavGraphBuilder.loginDestination(
    loginContent: @Composable (() -> Unit) -> Unit = { LoginRoute(it) },
    onLoginSuccess: () -> Unit
) {
    composable(route = Screen.Login.route) {
        loginContent(onLoginSuccess)
    }
}

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var legalUnavailable by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(viewModel) {
        onDispose { viewModel.cancelLogin() }
    }

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is LoginUiEffect.NavigateToHome -> onLoginSuccess()
        }
    }

    LoginScreen(
        uiState = state,
        onGoogleLoginClick = {
            if (BuildConfig.IS_DEV_MOCK) {
                viewModel.loginWithGoogle { "dev-mock-google-token" }
            } else {
                viewModel.loginWithGoogle(
                    com.hoabh.chanko.presentation.screens.login.AndroidGoogleTokenProvider(
                        context,
                        BuildConfig.GOOGLE_WEB_CLIENT_ID
                    )
                )
            }
        },
        onTermsClick = {
            context.launchSafeWebUrl(BuildConfig.TERMS_URL) { legalUnavailable = true }
        },
        onPrivacyClick = {
            context.launchSafeWebUrl(BuildConfig.PRIVACY_URL) { legalUnavailable = true }
        },
        modifier = modifier,
    )

    if (legalUnavailable) {
        LegalUnavailableDialog(onDismiss = { legalUnavailable = false })
    }
}

@Composable
private fun LegalUnavailableDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(stringResource(R.string.login_legal_unavailable)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.login_close))
            }
        },
    )
}

private fun Context.launchSafeWebUrl(url: String, onUnavailable: () -> Unit) {
    val uri = url.toUri()
    val isSafeHttps = uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo.isNullOrEmpty()
    if (!isSafeHttps) {
        onUnavailable()
        return
    }

    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
        onUnavailable()
    }
}

@Composable
fun LoginScreen(
    uiState: LoginUiState = LoginUiState(),
    onGoogleLoginClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize().background(CandyColors.background)) {
        val heroHeight = (maxHeight * 0.55f).coerceIn(300.dp, 600.dp)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(heroHeight)) {
                Image(painterResource(R.drawable.chanko_login_door), contentDescription = null,
                    contentScale = ContentScale.Crop, alignment = Alignment.BottomCenter,
                    modifier = Modifier.fillMaxSize())
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(48.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, CandyColors.background))))
            }
            Column(Modifier.widthIn(max = 440.dp).fillMaxWidth().padding(horizontal = CandySpace.section),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.login_headline),
                    style = TextStyle(fontFamily = Nunito, fontSize = 28.sp, lineHeight = 34.sp,
                        fontWeight = FontWeight.ExtraBold, color = LoginPlum, textAlign = TextAlign.Center),
                    modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.login_subtitle), color = LoginPlum,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp), textAlign = TextAlign.Center)
                Spacer(Modifier.height(22.dp))
                OutlinedButton(
                    onClick = onGoogleLoginClick,
                    enabled = !uiState.isLoading && !uiState.isLoginSuccess,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = RoundedCornerShape(50), border = BorderStroke(1.dp, Color(0xFF747775)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F)),
                    contentPadding = PaddingValues(horizontal = CandySpace.lg, vertical = CandySpace.md),
                ) {
                    Image(painterResource(R.drawable.ic_google), null, Modifier.size(22.dp))
                    Spacer(Modifier.width(CandySpace.md))
                    Text(stringResource(R.string.login_with_google), fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium, fontSize = 16.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false))
                }
                if (uiState.isLoading) Row(
                    Modifier.padding(top = CandySpace.md).semantics { liveRegion = LiveRegionMode.Polite },
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.login_loading), style = MaterialTheme.typography.bodyMedium, color = LoginPlum)
                }
                uiState.error?.let { error ->
                    Text(stringResource(error.messageId()), color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = CandySpace.md).semantics { liveRegion = LiveRegionMode.Polite })
                }
                Spacer(Modifier.height(CandySpace.lg))
                Text(stringResource(R.string.login_legal_intro), color = Color(0xFF6B6570),
                    style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                LegalLinks(onTermsClick, onPrivacyClick)
                Spacer(Modifier.height(CandySpace.md))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegalLinks(onTermsClick: () -> Unit, onPrivacyClick: () -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextButton(onClick = onTermsClick, contentPadding = PaddingValues(horizontal = 6.dp, vertical = CandySpace.sm)) {
            Text(stringResource(R.string.login_terms), color = Color(0xFF6B6570), fontSize = 12.sp,
                textDecoration = TextDecoration.Underline, textAlign = TextAlign.Center)
        }
        TextButton(onClick = onPrivacyClick, contentPadding = PaddingValues(horizontal = 6.dp, vertical = CandySpace.sm)) {
            Text(stringResource(R.string.login_privacy), color = Color(0xFF6B6570), fontSize = 12.sp,
                textDecoration = TextDecoration.Underline, textAlign = TextAlign.Center)
        }
    }
}

private fun AuthFailure.messageId(): Int = when (this) {
    AuthFailure.NOT_CONFIGURED -> R.string.login_not_configured
    AuthFailure.NETWORK -> R.string.login_network_error
    AuthFailure.TIMEOUT -> R.string.login_timeout_error
    AuthFailure.REJECTED -> R.string.login_rejected_error
    AuthFailure.RATE_LIMITED -> R.string.login_rate_limit_error
    AuthFailure.SERVER -> R.string.login_server_error
    AuthFailure.INVALID_RESPONSE -> R.string.login_invalid_error
    AuthFailure.STORAGE -> R.string.login_storage_error
    AuthFailure.GOOGLE -> R.string.login_google_error
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginPreview() { ChankoTheme { LoginScreen(onGoogleLoginClick = {}, onTermsClick = {}, onPrivacyClick = {}) } }
@Preview(showBackground = true, widthDp = 320, heightDp = 568, fontScale = 2f)
@Composable
private fun LoginLargeTextPreview() { LoginPreview() }
