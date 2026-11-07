package com.hoabh.chanko.presentation.features.profile

import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.base.UiText

/**
 * Ba mức tính cách của Linh thú Chanko:
 * - [Friendly]: Thân thiện, nhẹ nhàng, động viên
 * - [Sassy]: Lém lỉnh, hóm hỉnh (mặc định)
 * - [Savage]: Thẳng thắn, kỷ luật cao
 */
enum class CatPersonality {
    Friendly,
    Sassy,
    Savage
}

/**
 * Trạng thái dữ liệu tài khoản hiển thị trên Profile (Read-only)
 */
data class ProfileAccountUiModel(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isGoogleLinked: Boolean = true,
    val displayNameUiText: UiText = if (displayName.isNotBlank()) UiText.DynamicString(displayName) else UiText.StringResource(R.string.profile_default_display_name),
    val emailUiText: UiText = if (email.isNotBlank()) UiText.DynamicString(email) else UiText.StringResource(R.string.profile_default_email)
)

/**
 * Immutable UI State cho màn hình Profile & Settings
 */
data class ProfileUiState(
    val account: ProfileAccountUiModel = ProfileAccountUiModel(),
    val committedPersonality: CatPersonality = CatPersonality.Sassy,
    val isReadyForReviewNotificationEnabled: Boolean = true,
    val isSystemNotificationBlocked: Boolean = false,
    val appVersion: String = com.hoabh.chanko.BuildConfig.VERSION_NAME,
    val isLoading: Boolean = false,
    val isSignOutInProgress: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Tương tác người dùng trên màn hình Profile
 */
sealed interface ProfileUiEvent {
    data object OnBackClicked : ProfileUiEvent
    data object OnCozyCornerTapped : ProfileUiEvent
    data object OnPersonalityRowClicked : ProfileUiEvent
    data class OnReadyForReviewToggled(val enabled: Boolean) : ProfileUiEvent
    data object OnOpenSystemNotificationSettings : ProfileUiEvent
    data object OnPrivacyPolicyClicked : ProfileUiEvent
    data object OnAboutClicked : ProfileUiEvent
    data object OnSignOutClicked : ProfileUiEvent
    data object OnConfirmSignOut : ProfileUiEvent
    data object OnDismissSignOutDialog : ProfileUiEvent
}

/**
 * Hiệu ứng một lần từ ViewModel gửi lên UI (Navigation, Toast, System Settings)
 */
sealed interface ProfileUiEffect {
    data object NavigateBack : ProfileUiEffect
    data object NavigateToPersonality : ProfileUiEffect
    data object NavigateToLogin : ProfileUiEffect
    data object OpenSystemNotificationSettings : ProfileUiEffect
    data class OpenWebUrl(val url: String) : ProfileUiEffect
    data class ShowToast(val messageResId: Int) : ProfileUiEffect
}
