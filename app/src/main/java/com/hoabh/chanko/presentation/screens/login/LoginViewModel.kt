package com.hoabh.chanko.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.domain.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: AuthFailure? = null,
    val isLoginSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: GoogleAuthRepository) : ViewModel() {
    private val state = MutableStateFlow(LoginUiState())
    val uiState = state.asStateFlow()
    private var attempt: Job? = null

    fun loginWithGoogle(provider: GoogleTokenProvider) {
        if (state.value.isLoading || state.value.isLoginSuccess) return
        state.value = LoginUiState(isLoading = true)
        attempt = viewModelScope.launch {
            try {
                when (val challenge = repository.createChallenge()) {
                    is AuthResult.Failure -> state.update { it.copy(error = challenge.reason) }
                    is AuthResult.Success -> {
                        val token = provider.getIdToken(challenge.value.nonce)
                        if (token != null) {
                            when (val result = repository.signIn(challenge.value, token)) {
                                is AuthResult.Success -> state.update { it.copy(isLoginSuccess = true) }
                                is AuthResult.Failure -> state.update { it.copy(error = result.reason) }
                            }
                        }
                    }
                }
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { state.update { it.copy(error = AuthFailure.GOOGLE) }
            } finally { state.update { it.copy(isLoading = false) } }
        }
    }

    // Leaving the route (including Activity recreation) cancels its credential UI safely.
    fun cancelLogin() { attempt?.cancel() }
}
