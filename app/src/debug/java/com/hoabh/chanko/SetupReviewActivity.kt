package com.hoabh.chanko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hoabh.chanko.presentation.screens.setup.*
import com.hoabh.chanko.ui.theme.ChankoTheme

/** Debug-only visual review. It cannot enter Home or fake a saved account. */
class SetupReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChankoTheme {
                val model: SetupViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T = SetupViewModel(object : SetupGateway {
                        override suspend fun checkStatus() = SetupStatus.INCOMPLETE
                        override suspend fun submit(draft: SetupDraft) = SetupSubmission.UNAVAILABLE
                    }) as T
                })
                val state by model.state.collectAsStateWithLifecycle()
                Column(Modifier.fillMaxSize()) {
                    Text(stringResource(R.string.setup_review_notice), modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp))
                    Box(Modifier.weight(1f)) {
                        InitialSetupScreen(state, model::amountChanged, model::dayChanged, model::continueToDay, model::back, model::save)
                    }
                }
            }
        }
    }
}
