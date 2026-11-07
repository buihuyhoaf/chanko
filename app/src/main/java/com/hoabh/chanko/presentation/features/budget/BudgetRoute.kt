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
 * Route chuyên biệt cho màn hình Thư viện Ngân sách theo năm [BudgetGalleryScreen].
 * Mở khi người dùng nhấn "Xem tất cả" từ Home hoặc từ Tab Ngân sách.
 */
@Composable
fun BudgetGalleryRoute(
    onBackClick: () -> Unit,
    onNavigateToDetail: (cycleId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BudgetGalleryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            is BudgetGalleryUiEffect.NavigateToDetail -> {
                val targetCycleId = "cycle_${effect.yearMonth.year}_%02d".format(effect.yearMonth.monthValue)
                onNavigateToDetail(targetCycleId)
            }
            is BudgetGalleryUiEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    BudgetGalleryScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Route chuyên biệt cho màn hình Chi tiết Ngân sách [BudgetDetailScreen].
 * Mở khi người dùng nhấn trực tiếp vào thẻ Ngân sách trên Home hoặc từ một tháng trong Gallery.
 */
@Composable
fun BudgetDetailRoute(
    onBackClick: () -> Unit,
    onNavigateToItemDetail: (Int) -> Unit = {},
    cycleId: String? = null,
    mode: String? = null,
    expandHistory: Boolean = false,
    openCyclePicker: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    BudgetDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        modifier = modifier,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Route điều phối tổng hợp (hỗ trợ tương thích ngược với [Screen.BudgetDetails]):
 * - Nếu [cycleId] != null: hiển thị [BudgetDetailScreen]
 * - Ngược lại: hiển thị [BudgetGalleryScreen]
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

    if (cycleId != null || selectedDetailMonth != null) {
        BudgetDetailScreen(
            state = detailState,
            onEvent = viewModel::onEvent,
            onBackClick = {
                if (selectedDetailMonth != null && cycleId == null) {
                    selectedDetailMonth = null
                } else {
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
