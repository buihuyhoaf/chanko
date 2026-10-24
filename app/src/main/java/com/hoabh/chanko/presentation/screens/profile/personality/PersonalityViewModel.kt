package com.hoabh.chanko.presentation.screens.profile.personality

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.profile.CatPersonality
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class PersonalityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PersonalityUiState, PersonalityUiEffect>(
    initialState = PersonalityUiState()
) {

    fun onEvent(event: PersonalityUiEvent) {
        when (event) {
            is PersonalityUiEvent.OnSelectPersonality -> {
                updateState { it.copy(draftPersonality = event.personality) }
            }
            is PersonalityUiEvent.OnSaveClicked -> {
                savePersonality()
            }
            is PersonalityUiEvent.OnBackClicked -> {
                if (uiState.value.isDirty) {
                    updateState { it.copy(showUnsavedChangesDialog = true) }
                } else {
                    sendEffect(PersonalityUiEffect.NavigateBack)
                }
            }
            is PersonalityUiEvent.OnKeepEditing -> {
                updateState { it.copy(showUnsavedChangesDialog = false) }
            }
            is PersonalityUiEvent.OnDiscardChanges -> {
                updateState {
                    it.copy(
                        showUnsavedChangesDialog = false,
                        draftPersonality = it.committedPersonality
                    )
                }
                sendEffect(PersonalityUiEffect.NavigateBack)
            }
        }
    }

    fun initCommitted(personality: CatPersonality) {
        updateState {
            it.copy(
                committedPersonality = personality,
                draftPersonality = personality
            )
        }
    }

    fun setNetworkConnectivity(isOnline: Boolean) {
        updateState { it.copy(isOnline = isOnline) }
    }

    private fun savePersonality() {
        if (!uiState.value.canSave) return

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            try {
                // Giả lập network persistence
                delay(300)
                updateState {
                    it.copy(
                        committedPersonality = it.draftPersonality,
                        isSaving = false
                    )
                }
                sendEffect(PersonalityUiEffect.ShowToast(R.string.profile_personality_saved_success))
                sendEffect(PersonalityUiEffect.NavigateBack)
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message
                    )
                }
                sendEffect(PersonalityUiEffect.ShowToast(R.string.profile_personality_save_error))
            }
        }
    }
}
