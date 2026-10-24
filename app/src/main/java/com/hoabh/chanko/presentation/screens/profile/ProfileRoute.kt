package com.hoabh.chanko.presentation.screens.profile

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect

@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPersonality: () -> Unit,
    onSignOutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is ProfileUiEffect.NavigateBack -> onNavigateBack()
            is ProfileUiEffect.NavigateToPersonality -> onNavigateToPersonality()
            is ProfileUiEffect.NavigateToLogin -> onSignOutSuccess()
            is ProfileUiEffect.OpenSystemNotificationSettings -> {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
            is ProfileUiEffect.OpenWebUrl -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                context.startActivity(intent)
            }
            is ProfileUiEffect.ShowToast -> {
                Toast.makeText(context, effect.messageResId, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ProfileScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
