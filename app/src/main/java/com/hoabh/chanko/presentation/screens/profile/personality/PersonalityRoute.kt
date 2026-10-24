package com.hoabh.chanko.presentation.screens.profile.personality

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect

@Composable
fun PersonalityRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonalityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is PersonalityUiEffect.NavigateBack -> onNavigateBack()
            is PersonalityUiEffect.ShowToast -> {
                Toast.makeText(context, effect.messageResId, Toast.LENGTH_SHORT).show()
            }
        }
    }

    PersonalityScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}
