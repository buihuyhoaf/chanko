package com.hoabh.chanko.presentation.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Activity lifetime only; a restored navigation stack is never proof of authentication. */
class LoginSessionViewModel : ViewModel() {
    private val accepted = MutableStateFlow(false)
    val authenticated = accepted.asStateFlow()
    internal fun backendAccepted() { accepted.value = true }
}
