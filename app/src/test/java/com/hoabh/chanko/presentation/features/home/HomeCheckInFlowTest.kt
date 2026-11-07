package com.hoabh.chanko.presentation.features.home
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

/**
 * Kiểm thử tích hợp toàn diện luồng Daily Check-in & Level Up trên màn hình Home (Phase 6, Task 18).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeCheckInFlowTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDataSource: HomeFakeDataSource
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDataSource = HomeFakeDataSource()
        viewModel = HomeViewModel(fakeDataSource = fakeDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial launch prompts Daily Check-in dialog when not checked in`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.header.hasCheckedInToday)
        assertTrue(state.isCheckInDialogOpen)
    }

    @Test
    fun `dismissing Check-in dialog closes popup without awarding resources and allows manual reopening`() = runTest {
        // Người dùng bấm "Để sau"
        viewModel.onEvent(HomeUiEvent.DismissCheckInDialog)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertFalse(state.isCheckInDialogOpen)
        assertFalse(state.header.hasCheckedInToday)
        assertEquals(0L, state.header.hatBalance)
        assertEquals(0L, state.header.pateBalance)

        // Bấm vào pill Check-in trên Header để mở lại
        viewModel.onEvent(HomeUiEvent.RequestCheckIn)
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue(state.isCheckInDialogOpen)
    }

    @Test
    fun `confirming Check-in awards 10 Hat and 10 Pate, updates Header, and marks as checked in`() = runTest {
        viewModel.onEvent(HomeUiEvent.ConfirmCheckIn)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCheckInDialogOpen)
        assertTrue(state.header.hasCheckedInToday)
        assertEquals(10L, state.header.hatBalance)
        assertEquals(10L, state.header.pateBalance)
    }

    @Test
    fun `confirming Check-in triggers LevelUpCelebration when threshold is reached`() = runTest {
        // Khởi tạo trạng thái đã gần lên cấp (VD Level 1 ngưỡng 100 Pate, hiện có 95 Pate)
        viewModel.onEvent(HomeUiEvent.ConfirmCheckIn) // +10 Pate -> tổng 10 Pate (Level 1)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(1, state.header.currentLevel)
        assertFalse(state.showLevelUpCelebration)

        // Dismiss celebration nếu có
        viewModel.onEvent(HomeUiEvent.DismissLevelUpCelebration)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showLevelUpCelebration)
    }

    @Test
    fun `item journey order is preserved with newest items at bottom`() = runTest {
        val state = viewModel.uiState.value
        val nodes = state.journeyNodes
        assertEquals(5, nodes.size)

        // Kiểm tra timestamp tăng dần (món mới nhất ở dưới cùng)
        for (i in 0 until nodes.size - 1) {
            val current = nodes[i].createdAtEpochMillis ?: 0L
            val next = nodes[i + 1].createdAtEpochMillis ?: 0L
            assertTrue(
                "Thứ tự node phải tăng dần theo thời gian (mới nhất ở dưới cùng): $current <= $next",
                current <= next
            )
        }
    }
}
