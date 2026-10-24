package com.hoabh.chanko.presentation.screens.profile

import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.BuildConfig
import com.hoabh.chanko.domain.auth.AuthenticationRepository
import com.hoabh.chanko.domain.auth.LogoutUseCase
import com.hoabh.chanko.domain.auth.SessionState
import com.hoabh.chanko.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository,
    private val logoutUseCase: LogoutUseCase
) : BaseViewModel<ProfileUiState, ProfileUiEffect>(
    initialState = ProfileUiState(
        appVersion = BuildConfig.VERSION_NAME
    )
) {

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.session.collect { sessionState ->
                if (sessionState is SessionState.Available) {
                    val accountValue = sessionState.account.value
                    updateState { currentState ->
                        currentState.copy(
                            account = ProfileAccountUiModel(
                                displayName = if (accountValue.isNotBlank()) accountValue else "Bạn đồng hành Chanko",
                                email = if (accountValue.contains("@")) accountValue else "user@chanko.app",
                                isGoogleLinked = true
                            )
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.OnBackClicked -> {
                sendEffect(ProfileUiEffect.NavigateBack)
            }
            is ProfileUiEvent.OnCozyCornerTapped -> {
                // Whole scene decoration, no-op business logic
            }
            is ProfileUiEvent.OnPersonalityRowClicked -> {
                sendEffect(ProfileUiEffect.NavigateToPersonality)
            }
            is ProfileUiEvent.OnReadyForReviewToggled -> {
                updateState { it.copy(isReadyForReviewNotificationEnabled = event.enabled) }
            }
            is ProfileUiEvent.OnOpenSystemNotificationSettings -> {
                sendEffect(ProfileUiEffect.OpenSystemNotificationSettings)
            }
            is ProfileUiEvent.OnPrivacyPolicyClicked -> {
                sendEffect(ProfileUiEffect.OpenWebUrl("https://chanko.app/privacy"))
            }
            is ProfileUiEvent.OnAboutClicked -> {
                // Thông tin phiên bản & giới thiệu
            }
            is ProfileUiEvent.OnSignOutClicked -> {
                // UI tự quản lý show dialog xác nhận
            }
            is ProfileUiEvent.OnConfirmSignOut -> {
                signOut()
            }
            is ProfileUiEvent.OnDismissSignOutDialog -> {
                // Dialog dismissed
            }
        }
    }

    fun updateCommittedPersonality(personality: CatPersonality) {
        updateState { it.copy(committedPersonality = personality) }
    }

    private fun signOut() {
        if (uiState.value.isSignOutInProgress) return

        viewModelScope.launch {
            updateState { it.copy(isSignOutInProgress = true) }
            try {
                logoutUseCase()
                sendEffect(ProfileUiEffect.NavigateToLogin)
            } catch (e: Exception) {
                // Vẫn đảm bảo thu hồi và thoát phiên theo AUTH-04 safety
                sendEffect(ProfileUiEffect.NavigateToLogin)
            } finally {
                updateState { it.copy(isSignOutInProgress = false) }
            }
        }
    }
}
