package com.hoabh.chanko.presentation.features.budget

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.domain.budget.model.BudgetCalculation
import com.hoabh.chanko.domain.budget.model.BudgetSetupState
import com.hoabh.chanko.domain.budget.model.BudgetTemporalRole
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryUiEffect
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryUiEvent
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

/**
 * Kiểm thử nghiệm thu toàn diện (Acceptance Criteria AC01..AC10) cho Budget v3 UI/UX Upgrade:
 * Tham chiếu: docs/features/budget-ui-v3-spec.md
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetV3AcceptanceE2ETest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeBudgetRepository
    private lateinit var galleryViewModel: BudgetGalleryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBudgetRepository()
        galleryViewModel = BudgetGalleryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `AC01 - First 09_2026, chon 2026 thi gallery chi hien thi 09 10 11 12`() = runTest {
        val collectJob = launch { galleryViewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = galleryViewModel.uiState.value
        assertEquals(2026, state.selectedYear)
        assertEquals(4, state.months.size)
        assertEquals(listOf(9, 10, 11, 12), state.months.map { it.yearMonth.monthValue })

        collectJob.cancel()
    }

    @Test
    fun `AC02 - Chon 2027 hien thi du 12 thang 01 den 12`() = runTest {
        val collectJob = launch { galleryViewModel.uiState.collect {} }
        advanceUntilIdle()

        galleryViewModel.onEvent(BudgetGalleryUiEvent.NextYear)
        advanceUntilIdle()

        val state = galleryViewModel.uiState.value
        assertEquals(2027, state.selectedYear)
        assertEquals(12, state.months.size)
        assertEquals((1..12).toList(), state.months.map { it.yearMonth.monthValue })

        collectJob.cancel()
    }

    @Test
    fun `AC03 - Chon thang qua khu hoac hien tai dieu huong den Detail dung thang`() = runTest {
        val collectJob = launch { galleryViewModel.uiState.collect {} }
        advanceUntilIdle()

        val currentMonth11 = galleryViewModel.uiState.value.months.first { it.yearMonth.monthValue == 11 }
        var capturedEffect: BudgetGalleryUiEffect? = null
        val effectJob = launch {
            galleryViewModel.effect.collect { capturedEffect = it }
        }

        galleryViewModel.onEvent(BudgetGalleryUiEvent.MonthCardClick(currentMonth11))
        advanceUntilIdle()

        assertTrue(capturedEffect is BudgetGalleryUiEffect.NavigateToDetail)
        assertEquals(YearMonth.of(2026, 11), (capturedEffect as BudgetGalleryUiEffect.NavigateToDetail).yearMonth)

        effectJob.cancel()
        collectJob.cancel()
    }

    @Test
    fun `AC04 - Chon thang 12 tuong lai mo Future Sheet setup khong mo Detail chi tieu`() = runTest {
        val collectJob = launch { galleryViewModel.uiState.collect {} }
        advanceUntilIdle()

        val futureMonth12 = galleryViewModel.uiState.value.months.first { it.yearMonth.monthValue == 12 }
        assertEquals(BudgetTemporalRole.Future, futureMonth12.temporalRole)
        assertEquals(BudgetSetupState.Missing, futureMonth12.setupState)

        galleryViewModel.onEvent(BudgetGalleryUiEvent.MonthCardClick(futureMonth12))
        advanceUntilIdle()

        val state = galleryViewModel.uiState.value
        assertNotNull(state.activeFutureSheetMonth)
        assertEquals(YearMonth.of(2026, 12), state.activeFutureSheetMonth?.yearMonth)
        assertEquals(6_000_000L, state.suggestedLimitForFuture)

        collectJob.cancel()
    }

    @Test
    fun `AC05 - Tong nam truoc save 18tr sau save 12 tro thanh 24tr`() = runTest {
        val collectJob = launch { galleryViewModel.uiState.collect {} }
        advanceUntilIdle()

        // Trước khi lưu tháng 12
        assertEquals(18_000_000L, galleryViewModel.uiState.value.annualConfiguredTotalMinor)
        assertEquals(3, galleryViewModel.uiState.value.configuredMonthsCount)

        // Lưu tháng 12 = 6tr
        galleryViewModel.onEvent(BudgetGalleryUiEvent.SaveFutureBudget(YearMonth.of(2026, 12), 6_000_000L))
        advanceUntilIdle()

        // Sau khi lưu tháng 12
        assertEquals(24_000_000L, galleryViewModel.uiState.value.annualConfiguredTotalMinor)
        assertEquals(4, galleryViewModel.uiState.value.configuredMonthsCount)

        collectJob.cancel()
    }

    @Test
    fun `AC06 - Thang tuong lai khong co chi tieu actualSpent hoac remaining`() = runTest {
        val overview = repository.getYearGalleryOverview(2026).first()
        val month12 = overview.months.first { it.yearMonth.monthValue == 12 }

        assertEquals(BudgetTemporalRole.Future, month12.temporalRole)
        assertNull(month12.actualSpentMinor)
        assertNull(month12.remainingMinor)
        assertNull(month12.usagePercent)
    }

    @Test
    fun `AC07 & AC15 - Validate input so nguyen duong VND chan so am so le overflow`() {
        assertNull(MoneyVnd.parse(""))
        assertNull(MoneyVnd.parse("-500000"))
        assertNull(MoneyVnd.parse("abc"))
        assertNull(MoneyVnd.parse("5000.5"))

        assertEquals(6_000_000L, MoneyVnd.parse("6000000"))
        assertEquals(6_000_000L, MoneyVnd.parse("6.000.000"))

        assertFalse(MoneyVnd.isValidBudget(0L))
        assertTrue(MoneyVnd.isValidBudget(6_000_000L))
        assertFalse(MoneyVnd.isValidBudget(1_000_000_000_000L))
    }

    @Test
    fun `AC08 - Vuot ngan sach clamp thanh tien do 100 percent va hien thi dung ti le percent`() {
        // Tháng 10: hạn mức 6tr, đã chi 6.2tr -> vượt 200k, usage 103%
        val metrics = BudgetCalculation.calculateMetrics(limitMinor = 6_000_000L, spentMinor = 6_200_000L)

        assertTrue(metrics.isOverBudget)
        assertEquals(200_000L, metrics.overMinor)
        assertEquals(-200_000L, metrics.remainingMinor)
        assertEquals(103, metrics.realPercentInt)
        assertEquals("103%", metrics.realPercentText)
        assertEquals(1.0f, metrics.clampedProgress, 0.001f) // Clamped at 100%
    }

    @Test
    fun `AC09 - Mat mang offline khoa toan bo hanh dong ghi va thong bao ro rang`() = runTest {
        repository.setNetworkOnline(false)
        advanceUntilIdle()

        val saveResult = repository.setBudgetForMonth(YearMonth.of(2026, 12), 6_000_000L)
        assertTrue(saveResult.isFailure)
        assertTrue(saveResult.exceptionOrNull() is com.hoabh.chanko.domain.budget.model.BudgetOfflineException)

        val overview = repository.getYearGalleryOverview(2026).first()
        assertFalse(overview.isOnline)
        assertNotNull(overview.lastSyncedAt)
    }

    @Test
    fun `AC10 - Home carousel tiep tuc hoat dong doc lap khong bi anh huong boi Budget v3`() = runTest {
        val window = repository.observeTriCycleWindow().first()
        assertTrue(window.hasPrevious)
        assertEquals("10", window.current.labelMonth)
        assertEquals("09", window.previous?.labelMonth)
        assertEquals("11", window.next.labelMonth)
    }

    @Test
    fun `AC25 - Menu options events on Gallery Header emit snackbar effects`() = runTest {
        var capturedEffect: BudgetGalleryUiEffect? = null
        val effectJob = launch {
            galleryViewModel.effect.collect { capturedEffect = it }
        }

        galleryViewModel.onEvent(BudgetGalleryUiEvent.ShowAboutInfo)
        advanceUntilIdle()
        assertTrue(capturedEffect is BudgetGalleryUiEffect.ShowSnackbar)
        assertTrue((capturedEffect as BudgetGalleryUiEffect.ShowSnackbar).message.contains("Chanko Budget v3"))

        galleryViewModel.onEvent(BudgetGalleryUiEvent.SyncData)
        advanceUntilIdle()
        assertTrue(capturedEffect is BudgetGalleryUiEffect.ShowSnackbar)

        effectJob.cancel()
    }

    @Test
    fun `AC27 - Verify resource drawables for 11 PNGs and 9 vectors exist in R drawable`() {
        // 11 PNGs
        assertTrue(com.hoabh.chanko.R.drawable.budget_header_cat_counting_money != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_card_cat_happy != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_card_cat_sleepy != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_card_cat_concerned != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_summary_calendar_pink != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_setup_calendar_purple != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_setup_coins_gold != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_sparkle_01 != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_sparkle_02 != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_sparkle_03 != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_sparkle_04 != 0)

        // 9 Vector XMLs
        assertTrue(com.hoabh.chanko.R.drawable.budget_attention_rays != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_back_wave_home != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_chanko_lockup != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_chip_decorative != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_chip_home != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_contactless_home != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_front_wave_home != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_paw_mark_home != 0)
        assertTrue(com.hoabh.chanko.R.drawable.budget_screen_cloud_wave != 0)
    }
}
