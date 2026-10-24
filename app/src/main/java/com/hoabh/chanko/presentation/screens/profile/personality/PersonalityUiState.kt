package com.hoabh.chanko.presentation.screens.profile.personality

import com.hoabh.chanko.presentation.screens.profile.CatPersonality

/**
 * Immutable UI State cho màn hình con Cat Personality
 */
data class PersonalityUiState(
    val committedPersonality: CatPersonality = CatPersonality.Sassy,
    val draftPersonality: CatPersonality = CatPersonality.Sassy,
    val isOnline: Boolean = true,
    val isSaving: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val errorMessage: String? = null
) {
    val isDirty: Boolean get() = draftPersonality != committedPersonality
    val canSave: Boolean get() = isDirty && isOnline && !isSaving
}

/**
 * Tương tác người dùng trên màn hình Personality
 */
sealed interface PersonalityUiEvent {
    data class OnSelectPersonality(val personality: CatPersonality) : PersonalityUiEvent
    data object OnSaveClicked : PersonalityUiEvent
    data object OnBackClicked : PersonalityUiEvent
    data object OnKeepEditing : PersonalityUiEvent
    data object OnDiscardChanges : PersonalityUiEvent
}

/**
 * Hiệu ứng một lần từ PersonalityViewModel
 */
sealed interface PersonalityUiEffect {
    data object NavigateBack : PersonalityUiEffect
    data class ShowToast(val messageResId: Int) : PersonalityUiEffect
}
