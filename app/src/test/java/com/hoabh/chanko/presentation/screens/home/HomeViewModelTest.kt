package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDataSource: HomeFakeDataSource
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDataSource = HomeFakeDataSource()
        viewModel = HomeViewModel(fakeDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsDefaultScenarioWithNamedHeader() = runTest(testDispatcher) {
        val state = viewModel.uiState.value

        assertTrue(state.header.hasName)
        assertEquals("Hoa", state.header.displayName)
        assertEquals("H", state.header.avatarInitial)
        assertFalse(state.header.isOffline)
        assertTrue(state.budgetSection is SectionState.Content)
        assertTrue(state.decisionsSection is SectionState.Content)
    }

    @Test
    fun selectEmptyNameScenario_usesSafeAnonymousFallbackAndDefaultInitial() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.EMPTY_NAME))

        val state = viewModel.uiState.value
        assertFalse(state.header.hasName)
        assertEquals("", state.header.displayName)
        assertEquals(HeaderUiModel.DEFAULT_AVATAR_INITIAL, state.header.avatarInitial)
    }

    @Test
    fun selectLongNameScenario_preservesFullTrimmedNameAndFirstInitial() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.LONG_NAME))

        val state = viewModel.uiState.value
        assertTrue(state.header.hasName)
        assertEquals("Nguyễn Hoàng Minh Anh", state.header.displayName)
        assertEquals("N", state.header.avatarInitial)
    }

    @Test
    fun selectPartialErrorScenario_isolatesBudgetErrorWithoutBreakingHeaderOrDecisions() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SelectScenario(HomeScenario.PARTIAL_ERROR))

        val state = viewModel.uiState.value
        assertEquals("Hoa", state.header.displayName)
        assertEquals(
            SectionState.Error(
                message = "Tạm thời chưa tải được ngân sách",
                canRetry = true
            ),
            state.budgetSection
        )
        assertTrue(state.decisionsSection is SectionState.Loading)
    }

    @Test
    fun settingsAndProfileClicks_emitCorrespondingNavigationEffects() = runTest(testDispatcher) {
        viewModel.onEvent(HomeUiEvent.SettingsClick)
        runCurrent()
        assertEquals(HomeUiEffect.NavigateToSettings, viewModel.effect.first())

        viewModel.onEvent(HomeUiEvent.ProfileClick)
        runCurrent()
        assertEquals(HomeUiEffect.NavigateToProfile, viewModel.effect.first())
    }
}
