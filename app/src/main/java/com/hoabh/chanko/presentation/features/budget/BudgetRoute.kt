package com.hoabh.chanko.presentation.features.budget

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.features.budget.detail.BudgetDetailScreen
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryScreen
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryUiEffect
import com.hoabh.chanko.presentation.features.budget.gallery.BudgetGalleryViewModel
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Route điều phối chính cho phân hệ Ngân sách (Budget v3):
 * - Mặc định (hoặc từ Tab Ngân sách): Mở [BudgetGalleryScreen] (Lưới 2 cột theo năm).
 * - Chọn tháng từ Gallery: Mở [BudgetDetailScreen] của tháng đó. Khi Back, quay về Gallery và bảo toàn năm/vị trí cuộn.
 * - Điều hướng từ Home Carousel: Truyền trực tiếp [cycleId], mở thẳng [BudgetDetailScreen] của tháng tương ứng.
 */
@Composable
fun BudgetRoute(
    onBackClick: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit = {},
    cycleId: String? = null,
    mode: String? = null,
    expandHistory: Boolean = false,
    openCyclePicker: Boolean = false,
    modifier: Modifier = Modifier,
    galleryViewModel: BudgetGalleryViewModel = hiltViewModel(),
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val galleryState by galleryViewModel.uiState.collectAsStateWithLifecycle()
    val detailState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val galleryGridState = rememberLazyGridState()

    // State theo dõi tháng đang xem chi tiết (nếu vào từ Gallery)
    var selectedDetailMonth by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(cycleId) {
        if (!cycleId.isNullOrBlank()) {
            viewModel.onEvent(BudgetUiEvent.SelectCycle(cycleId))
        }
    }

    LaunchedEffect(expandHistory) {
        if (expandHistory) viewModel.onEvent(BudgetUiEvent.ToggleHistoryAccordion(isExpanded = true))
    }
    LaunchedEffect(openCyclePicker) {
        if (openCyclePicker) viewModel.onEvent(BudgetUiEvent.OpenCyclePicker)
    }

    // Lắng nghe Effect từ Gallery ViewModel
    CollectEffect(effectFlow = galleryViewModel.effect) { effect ->
        when (effect) {
            is BudgetGalleryUiEffect.NavigateToDetail -> {
                selectedDetailMonth = effect.yearMonth.toString()
                val targetCycleId = "cycle_${effect.yearMonth.year}_%02d".format(effect.yearMonth.monthValue)
                viewModel.onEvent(BudgetUiEvent.SelectCycle(targetCycleId))
            }
            is BudgetGalleryUiEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    // Lắng nghe Effect từ Detail ViewModel
    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is BudgetUiEffect.NavigateToSharedItemDetail -> {
                onNavigateToItemDetail(effect.routeId)
            }
            is BudgetUiEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
            is BudgetUiEffect.ShowSnackbarResource -> {
                scope.launch { snackbarHostState.showSnackbar(context.getString(effect.messageRes)) }
            }
            is BudgetUiEffect.NavigateToWalletSettings -> {
                if (effect.expandHistory) viewModel.onEvent(BudgetUiEvent.ToggleHistoryAccordion(isExpanded = true))
            }
        }
    }

    // Điều hướng hiển thị:
    // 1. Nếu mở trực tiếp từ Home (cycleId != null) hoặc người dùng đã chọn tháng từ Gallery: hiển thị Detail
    // 2. Mặc định: hiển thị Budget Gallery (Lưới 2 cột theo năm)
    if (cycleId != null || selectedDetailMonth != null) {
        BudgetDetailScreen(
            state = detailState,
            onEvent = viewModel::onEvent,
            onBackClick = {
                if (selectedDetailMonth != null && cycleId == null) {
                    // Quay lại Gallery: giữ năm và vị trí cuộn trên galleryGridState
                    selectedDetailMonth = null
                } else {
                    // Mở từ Home: quay lại màn trước
                    onBackClick()
                }
            },
            modifier = modifier,
            snackbarHostState = snackbarHostState
        )
    } else {
        BudgetGalleryScreen(
            state = galleryState,
            onEvent = galleryViewModel::onEvent,
            onBackClick = onBackClick,
            modifier = modifier,
            lazyGridState = galleryGridState,
            snackbarHostState = snackbarHostState
        )
    }
}
