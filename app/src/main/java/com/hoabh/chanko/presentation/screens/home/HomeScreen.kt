package com.hoabh.chanko.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.home.components.BudgetCreditCard
import com.hoabh.chanko.presentation.screens.home.components.HomeHeader
import com.hoabh.chanko.presentation.screens.home.components.HomeHeaderTokens
import com.hoabh.chanko.presentation.screens.home.components.InsightExplanationDialog
import com.hoabh.chanko.presentation.screens.home.components.ItemJourneySection
import com.hoabh.chanko.presentation.screens.home.components.ObservationCard
import com.hoabh.chanko.presentation.screens.home.components.calculateCollapseFraction
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme
import kotlinx.coroutines.launch

private val BottomNavClearanceHeight = 88.dp
private val MaxContentWidthDp = 600.dp

/**
 * Stateful entry point cho màn hình Home v2.0 (Tab Trang chủ trong MainNavigation — AC-01..AC-31).
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onAddNewItemClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBudgetDetailsClick: () -> Unit = {},
    onNavigateToItemDetail: (Int) -> Unit = {},
    onNavigateToMyItems: (HomeMyItemsFilter) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lưu anchor ID hợp lệ qua rememberSaveable để khôi phục sau process death (AC-30)
    val savedAnchorItemId = rememberSaveable(state.scrollAnchorItemId) {
        state.scrollAnchorItemId.orEmpty()
    }

    // Khi quay lại Home (ON_RESUME), làm mới dữ liệu liên quan và giữ nguyên scroll (AC-19, AC-30)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(HomeUiEvent.RefreshHome)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            HomeUiEffect.NavigateToProfile -> onProfileClick()
            HomeUiEffect.OpenAddItemFlow -> onAddNewItemClick()
            is HomeUiEffect.ShowOfflineBlockedMessage -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
            HomeUiEffect.NavigateToBudgetDetails -> onBudgetDetailsClick()
            is HomeUiEffect.NavigateToItemDetail -> {
                val numericId = effect.itemId.filter { it.isDigit() }.toIntOrNull()
                    ?: (effect.itemId.hashCode() and 0x7FFFFFFF)
                onNavigateToItemDetail(numericId)
            }
            is HomeUiEffect.NavigateToMyItems -> {
                onNavigateToMyItems(effect.filter)
            }
        }
        viewModel.onEvent(HomeUiEvent.NavigationCompleted)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        HomeScreen(
            state = state.copy(
                scrollAnchorItemId = state.scrollAnchorItemId ?: savedAnchorItemId.ifEmpty { null }
            ),
            onEvent = viewModel::onEvent,
            listState = listState,
            modifier = Modifier.fillMaxSize()
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Stateless HomeScreen v2.0 tuân thủ cấu trúc 4 khu vực bắt buộc của `Chanko_Home_v2.0_Handoff` (`AC-01`):
 * 1. `HomeHeader` (AC-02, AC-20)
 * 2. `BudgetCreditCard` — Thẻ 3D Candy Orange (AC-03..AC-09)
 * 3. `ItemJourneySection` — "Món đồ gần đây" ziczac 1..5 node + Mascot bên phải (AC-10..AC-17)
 * 4. `ObservationCard` — "Góc nhìn từ Chanko" tùy điều kiện (AC-26, AC-27)
 * (Không còn Living Room, 4 vật thể tương tác hay HomeSheetHost của v1.0).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                CandyColors.homeScreenBackgroundTop,
                CandyColors.homeScreenBackgroundBottom
            )
        )
    }

    val statusBarTopPadding = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    val density = LocalDensity.current
    val maxScrollRangePx = with(density) { HomeHeaderTokens.CollapseScrollRange.toPx() }

    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                calculateCollapseFraction(listState.firstVisibleItemScrollOffset, maxScrollRangePx)
            }
        }
    }

    var expandedHeaderHeightPx by remember { mutableIntStateOf(0) }
    val expandedHeaderHeightDp = if (expandedHeaderHeightPx > 0) {
        with(density) { expandedHeaderHeightPx.toDp() }
    } else {
        statusBarTopPadding + (if (state.header.isOffline) 100.dp else 68.dp)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        val horizontalPadding = when {
            maxWidth < 360.dp -> 12.dp
            maxWidth >= 600.dp -> 24.dp
            else -> CandySpace.lg
        }

        // 1. Pinned Sticky & Collapsing Header (AC-01, AC-02, AC-20)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .background(
                    color = CandyColors.HomeV2.headerFrostedSurface.copy(
                        alpha = (collapseFraction * 1.25f).coerceIn(0f, 0.96f)
                    )
                )
                .drawBehind {
                    val dividerAlpha = (collapseFraction * 0.8f).coerceIn(0f, 0.4f)
                    if (dividerAlpha > 0f) {
                        drawLine(
                            color = CandyColors.HomeV2.headerBorderDivider.copy(alpha = dividerAlpha),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                .onGloballyPositioned { coordinates ->
                    if (collapseFraction == 0f || coordinates.size.height > expandedHeaderHeightPx) {
                        expandedHeaderHeightPx = coordinates.size.height
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            HomeHeader(
                header = state.header,
                onProfileClick = { onEvent(HomeUiEvent.AvatarTap) },
                collapseFraction = collapseFraction,
                modifier = Modifier
                    .widthIn(max = MaxContentWidthDp)
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = statusBarTopPadding + CandySpace.xs,
                        bottom = CandySpace.xs
                    )
            )
        }

        // 2. Danh sách cuộn LazyColumn bọc trong PullToRefreshBox chuẩn Material 3
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.SwipeRefreshHome) },
            state = pullToRefreshState,
            modifier = Modifier
                .widthIn(max = MaxContentWidthDp)
                .fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = expandedHeaderHeightDp),
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    color = CandyColors.brandPurple
                )
            }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = expandedHeaderHeightDp + CandySpace.sm,
                    bottom = BottomNavClearanceHeight
                ),
                verticalArrangement = Arrangement.spacedBy(CandySpace.lg)
            ) {
                // 2. Budget Credit Card 3D Candy Orange (AC-01, AC-03..AC-09, AC-18..AC-22)
                item(key = "section_budget_credit_card") {
                    BudgetCreditCard(
                        budgetState = state.budgetState,
                        onCardClick = { onEvent(HomeUiEvent.BudgetCardTap) },
                        onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Budget)) }
                    )
                }

                // 3. Item Journey ("Món đồ gần đây" — AC-01, AC-10..AC-17, AC-23, AC-24)
                item(key = "section_item_journey") {
                    ItemJourneySection(
                        journeyState = state.journeyState,
                        onNodeGestureStart = { itemId ->
                            onEvent(HomeUiEvent.JourneyNodeGestureStarted(itemId))
                        },
                        onNodeGestureEnd = {
                            onEvent(HomeUiEvent.JourneyNodeGestureEnded)
                        },
                        onNodeClick = { itemId ->
                            onEvent(HomeUiEvent.JourneyNodeTap(itemId))
                        },
                        onSeeAllClick = {
                            onEvent(HomeUiEvent.SeeAllTap)
                        },
                        onRetryClick = {
                            onEvent(HomeUiEvent.RetrySection(HomeSection.Journey))
                        }
                    )
                }

                // 4. Observation ("Góc nhìn từ Chanko" — tùy điều kiện — AC-01, AC-26, AC-27)
                val observation = state.visibleObservation
                if (observation != null) {
                    item(key = "section_observation_${observation.id}") {
                        ObservationCard(
                            observation = observation,
                            onExplainClick = {
                                onEvent(HomeUiEvent.InsightExplainTap(observation.id))
                            },
                            onHideClick = {
                                onEvent(HomeUiEvent.InsightHideTap(observation.id))
                            }
                        )
                    }
                }

                item(key = "bottom_clearance") {
                    Spacer(modifier = Modifier.height(CandySpace.sm))
                }
            }
        }

        // Hộp thoại giải thích chi tiết cho Góc nhìn từ Chanko (AC-27, không tự mở lại sau process death AC-30)
        val activeDialogObservation = state.activeInsightDialog
        if (activeDialogObservation != null) {
            InsightExplanationDialog(
                observation = activeDialogObservation,
                onDismiss = { onEvent(HomeUiEvent.DismissInsightDialog) },
                onHideInsight = { id -> onEvent(HomeUiEvent.InsightHideTap(id)) }
            )
        }
    }
}

// =============================================================================
// Responsive Previews Matrix (Home v2.0 — AC-01..AC-31)
// =============================================================================

@Preview(
    name = "1. Home v2.0 Approved Baseline (412dp - 5 Nodes)",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun HomeScreenV2DefaultPreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.DEFAULT_FIVE_NODES),
            onEvent = {}
        )
    }
}

@Preview(
    name = "2. Compact 320dp + FontScale 2.0f (AC-31)",
    showBackground = true,
    widthDp = 320,
    heightDp = 820,
    fontScale = 2.0f
)
@Composable
private fun HomeScreenV2CompactFont2xPreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.TWO_TO_FOUR_ITEMS),
            onEvent = {}
        )
    }
}

@Preview(
    name = "3. Single Node — No Connector (AC-12)",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun HomeScreenV2OneNodePreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.ONE_ITEM),
            onEvent = {}
        )
    }
}

@Preview(
    name = "4. Over Budget Coral on Orange Card (AC-06)",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun HomeScreenV2OverBudgetPreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.OVER_BUDGET),
            onEvent = {}
        )
    }
}

@Preview(
    name = "5. Empty Journey + No Budget (AC-07, AC-12)",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun HomeScreenV2EmptyAndNoBudgetPreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.EMPTY_ITEMS),
            onEvent = {}
        )
    }
}

@Preview(
    name = "6. Offline Cache + SyncedAt (AC-20)",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun HomeScreenV2OfflineCachePreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.OFFLINE_CACHE),
            onEvent = {}
        )
    }
}

@Preview(
    name = "7. Partial Error — Budget Error + Journey Active (AC-08, AC-22)",
    showBackground = true,
    widthDp = 390,
    heightDp = 844
)
@Composable
private fun HomeScreenV2PartialErrorPreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.BUDGET_ERROR),
            onEvent = {}
        )
    }
}

@Preview(
    name = "8. Scrolled Down — Collapsed Sticky Header",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun HomeScreenV2ScrolledCollapsedPreview() {
    val fakeDataSource = remember { HomeFakeDataSource() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = 1,
        initialFirstVisibleItemScrollOffset = 100
    )
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.DEFAULT_FIVE_NODES),
            onEvent = {},
            listState = listState
        )
    }
}
