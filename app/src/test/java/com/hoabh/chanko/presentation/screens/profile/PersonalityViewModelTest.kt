package com.hoabh.chanko.presentation.screens.profile.personality

import androidx.lifecycle.SavedStateHandle
import com.hoabh.chanko.presentation.screens.profile.CatPersonality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PersonalityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: PersonalityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PersonalityViewModel(SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default initial personality is Sassy and not dirty`() {
        val state = viewModel.uiState.value
        assertEquals(CatPersonality.Sassy, state.committedPersonality)
        assertEquals(CatPersonality.Sassy, state.draftPersonality)
        assertFalse(state.isDirty)
        assertFalse(state.canSave)
    }

    @Test
    fun `selecting new personality marks state as dirty and enables save when online`() {
        viewModel.onEvent(PersonalityUiEvent.OnSelectPersonality(CatPersonality.Friendly))

        val state = viewModel.uiState.value
        assertEquals(CatPersonality.Friendly, state.draftPersonality)
        assertEquals(CatPersonality.Sassy, state.committedPersonality)
        assertTrue(state.isDirty)
        assertTrue(state.canSave)
    }

    @Test
    fun `when offline canSave is false even if state is dirty`() {
        viewModel.setNetworkConnectivity(false)
        viewModel.onEvent(PersonalityUiEvent.OnSelectPersonality(CatPersonality.Savage))

        val state = viewModel.uiState.value
        assertTrue(state.isDirty)
        assertFalse(state.canSave)
    }

    @Test
    fun `back click when dirty opens unsaved changes dialog`() {
        viewModel.onEvent(PersonalityUiEvent.OnSelectPersonality(CatPersonality.Friendly))
        viewModel.onEvent(PersonalityUiEvent.OnBackClicked)

        val state = viewModel.uiState.value
        assertTrue(state.showUnsavedChangesDialog)
    }

    @Test
    fun `back click when clean directly sends NavigateBack effect`() = runTest {
        viewModel.onEvent(PersonalityUiEvent.OnBackClicked)

        val effect = viewModel.effect.first()
        assertEquals(PersonalityUiEffect.NavigateBack, effect)
    }

    @Test
    fun `keep editing dismisses unsaved changes dialog`() {
        viewModel.onEvent(PersonalityUiEvent.OnSelectPersonality(CatPersonality.Friendly))
        viewModel.onEvent(PersonalityUiEvent.OnBackClicked)
        assertTrue(viewModel.uiState.value.showUnsavedChangesDialog)

        viewModel.onEvent(PersonalityUiEvent.OnKeepEditing)
        assertFalse(viewModel.uiState.value.showUnsavedChangesDialog)
        assertEquals(CatPersonality.Friendly, viewModel.uiState.value.draftPersonality)
    }

    @Test
    fun `discard changes restores committed personality and navigates back`() = runTest {
        viewModel.onEvent(PersonalityUiEvent.OnSelectPersonality(CatPersonality.Friendly))
        viewModel.onEvent(PersonalityUiEvent.OnBackClicked)

        viewModel.onEvent(PersonalityUiEvent.OnDiscardChanges)

        val state = viewModel.uiState.value
        assertFalse(state.showUnsavedChangesDialog)
        assertEquals(CatPersonality.Sassy, state.draftPersonality)

        val effect = viewModel.effect.first()
        assertEquals(PersonalityUiEffect.NavigateBack, effect)
    }

    @Test
    fun `save personality persists draft as committed and navigates back`() = runTest {
        viewModel.onEvent(PersonalityUiEvent.OnSelectPersonality(CatPersonality.Savage))
        viewModel.onEvent(PersonalityUiEvent.OnSaveClicked)

        advanceTimeBy(400)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CatPersonality.Savage, state.committedPersonality)
        assertFalse(state.isDirty)
        assertFalse(state.isSaving)
    }
}
