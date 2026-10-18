package com.hoabh.chanko.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Repository identity, never a saved route or a login callback, grants access. */
@HiltViewModel
class LoginSessionViewModel @Inject constructor(repository: AuthenticationRepository) : ViewModel() {
    val session = repository.session
    private val restored = MutableStateFlow(false)
    val restorationComplete = restored.asStateFlow()
    init {
        viewModelScope.launch {
            try { repository.restore() } finally { restored.value = true }
        }
    }
}
