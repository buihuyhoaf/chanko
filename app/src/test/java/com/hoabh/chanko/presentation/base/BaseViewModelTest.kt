package com.hoabh.chanko.presentation.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

data class TestUiState(val count: Int = 0, val text: String = "")

sealed interface TestUiEffect {
    data class ShowToast(val message: String) : TestUiEffect
    data object NavigateNext : TestUiEffect
}

class TestViewModel(initialState: TestUiState = TestUiState()) :
    BaseViewModel<TestUiState, TestUiEffect>(initialState) {

    val publicCurrentState: TestUiState get() = currentState

    fun increment() {
        updateState { it.copy(count = it.count + 1) }
    }

    fun setText(newText: String) {
        updateState { it.copy(text = newText) }
    }

    fun triggerToast(msg: String) {
        sendEffect(TestUiEffect.ShowToast(msg))
    }

    fun triggerNavigate() {
        sendEffect { TestUiEffect.NavigateNext }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsExposedCorrectly() = runTest(dispatcher) {
        val initial = TestUiState(count = 42, text = "init")
        val viewModel = TestViewModel(initial)

        assertEquals(42, viewModel.uiState.value.count)
        assertEquals("init", viewModel.uiState.value.text)
        assertEquals(initial, viewModel.publicCurrentState)
    }

    @Test
    fun updateStateModifiesUiStateAndCurrentState() = runTest(dispatcher) {
        val viewModel = TestViewModel()

        viewModel.increment()
        assertEquals(1, viewModel.uiState.value.count)
        assertEquals(1, viewModel.publicCurrentState.count)

        viewModel.setText("hello")
        assertEquals("hello", viewModel.uiState.value.text)
        assertEquals(1, viewModel.uiState.value.count)
    }

    @Test
    fun sendEffectEmitsThroughEffectFlow() = runTest(dispatcher) {
        val viewModel = TestViewModel()
        val receivedEffects = mutableListOf<TestUiEffect>()

        val job = launch {
            viewModel.effect.toList(receivedEffects)
        }

        viewModel.triggerToast("Hello Toast")
        runCurrent()

        viewModel.triggerNavigate()
        runCurrent()

        assertEquals(
            listOf(
                TestUiEffect.ShowToast("Hello Toast"),
                TestUiEffect.NavigateNext
            ),
            receivedEffects
        )

        job.cancel()
    }

    @Test
    fun channelBuffersEffectsBeforeCollectorStarts() = runTest(dispatcher) {
        val viewModel = TestViewModel()

        // Gửi effect TRƯỚC KHI có subscriber
        viewModel.triggerToast("Buffered Message")
        runCurrent()

        // Collector bắt đầu sau
        val firstEffect = viewModel.effect.first()
        assertEquals(TestUiEffect.ShowToast("Buffered Message"), firstEffect)
    }
}
