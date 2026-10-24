package com.hoabh.chanko.presentation.screens.profile

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
    val isGoogleLinked: Boolean = true
)

/**
 * Immutable UI State cho màn hình Profile & Settings
 */
data class ProfileUiState(
    val account: ProfileAccountUiModel = ProfileAccountUiModel(),
    val committedPersonality: CatPersonality = CatPersonality.Sassy,
    val isReadyForReviewNotificationEnabled: Boolean = true,
    val isSystemNotificationBlocked: Boolean = false,
    val appVersion: String = "1.0.0",
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
