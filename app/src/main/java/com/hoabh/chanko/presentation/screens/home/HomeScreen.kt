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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.home.components.BudgetCard
import com.hoabh.chanko.presentation.screens.home.components.CompactLivingRoom
import com.hoabh.chanko.presentation.screens.home.components.HomeHeader
import com.hoabh.chanko.presentation.screens.home.components.HomeSheetHost
import com.hoabh.chanko.presentation.screens.home.components.ObservationCard
import com.hoabh.chanko.presentation.screens.home.components.YourDecisionsSection
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme
import kotlinx.coroutines.launch

private val BottomNavClearanceHeight = 88.dp
private val MaxContentWidthDp = 600.dp

/**
 * Stateful entry point cho màn hình Home (Tab Trang chủ trong MainNavigation).
 * Kết nối đầy đủ các điều hướng theo Chanko Home Design Handoff v1.0 (AC-01..AC-22).
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
            is HomeUiEffect.NavigateToDecisionDetail -> {
                val numericId = effect.id.filter { it.isDigit() }.toIntOrNull()
                    ?: (effect.id.hashCode() and 0x7FFFFFFF)
                onNavigateToItemDetail(numericId)
            }
            is HomeUiEffect.NavigateToMyItems -> {
                onNavigateToMyItems(effect.filter)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
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
 * Stateless HomeScreen tuân thủ cấu trúc 5 khu vực của Chanko Home Design Handoff v1.0 (Candy Pop 3D):
 * 1. Header (AC-03, AC-16)
 * 2. Little Room (AC-04, HOME-REV-03, HOME-GAP-05)
 * 3. Những món cần chú ý (AC-07, AC-08, AC-09, AC-10, HOME-STATE-01, HOME-EDGE-04)
 * 4. Budget Snapshot ("Ngân sách mua sắm" — AC-11, AC-12, AC-13)
 * 5. Observation ("Góc nhìn từ Chanko" — optional — AC-14, HOME-REV-05, HOME-OBS-01)
 * + Contextual Bottom Sheet Host (`HomeSheetHost` — AC-04, AC-05, AC-06, AC-19)
 * (Không còn 2 thẻ Quick Actions cũ — AC-02, HOME-REV-01).
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            CandyColors.homeScreenBackgroundTop,
            CandyColors.backgroundLavender.copy(alpha = 0.45f),
            CandyColors.homeScreenBackgroundBottom,
            CandyColors.background
        )
    )
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        val horizontalPadding = if (maxWidth < 360.dp) CandySpace.md else CandySpace.lg

        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = MaxContentWidthDp)
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                top = statusBarTopPadding + CandySpace.xs,
                end = horizontalPadding,
                bottom = CandySpace.lg
            ),
            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            // 1. Khu vực 1 — Header (AC-03, AC-16)
            item(key = "home_section_1_header") {
                HomeHeader(
                    header = state.header,
                    onProfileClick = { onEvent(HomeUiEvent.ProfileClick) },
                    canOpenProfile = state.capabilities.canOpenProfile
                )
            }

            // 2. Khu vực 2 — Little Room (AC-04, HOME-REV-03, HOME-GAP-05, AC-21)
            item(key = "home_section_2_little_room") {
                val roomData = when (val roomState = state.livingRoom) {
                    is SectionState.Content -> roomState.data
                    else -> LivingRoomUi()
                }
                CompactLivingRoom(
                    livingRoom = roomData,
                    onPropTapped = { kind -> onEvent(HomeUiEvent.PropTapped(kind)) },
                    reducedMotion = state.isReducedMotionEnabled
                )
            }

            // 3. Khu vực 3 — Những món cần chú ý (AC-07, AC-08, AC-09, AC-10, HOME-REV-02)
            item(key = "home_section_3_decisions") {
                YourDecisionsSection(
                    sectionState = state.decisionsSection,
                    totalCount = state.totalAttentionCount,
                    onDecisionClick = { id -> onEvent(HomeUiEvent.DecisionTapped(id)) },
                    onViewAllClick = { onEvent(HomeUiEvent.ViewAllTapped) },
                    onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Decisions)) },
                    canOpenViewAll = state.capabilities.canOpenViewAll
                )
            }

            // 4. Khu vực 4 — Budget Snapshot ("Ngân sách mua sắm" — AC-11, AC-12, AC-13, HOME-REV-02)
            item(key = "home_section_4_budget") {
                BudgetCard(
                    sectionState = state.budgetSection,
                    onDetailsClick = { onEvent(HomeUiEvent.BudgetDetailsTapped) },
                    onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Budget)) },
                    canOpenDetails = state.capabilities.canOpenBudgetDetails
                )
            }

            // 5. Khu vực 5 — Observation ("Góc nhìn từ Chanko" — optional — AC-14)
            state.visibleObservation?.let { observation ->
                item(key = "home_section_5_observation_${observation.id}") {
                    ObservationCard(
                        observation = observation,
                        onExplainClick = { id -> onEvent(HomeUiEvent.ObservationExplainTapped(id)) },
                        onHideClick = { id -> onEvent(HomeUiEvent.ObservationHideTapped(id)) },
                        onDismissClick = { id -> onEvent(HomeUiEvent.ObservationDismissTapped(id)) },
                        canExplain = state.capabilities.canOpenObservationExplain,
                        canHide = state.capabilities.canHideObservation
                    )
                }
            }

            // Bottom clearance spacer cho cố định Bottom Navigation
            item(key = "bottom_nav_clearance") {
                Spacer(modifier = Modifier.height(BottomNavClearanceHeight))
            }
        }

        // Contextual Modal Overlay Sheet Host (AC-04, AC-05, AC-06, AC-19)
        HomeSheetHost(
            activeSheet = state.activeSheet,
            onDismiss = { onEvent(HomeUiEvent.DismissSheet) },
            onContextPrimaryAction = { kind, entityId ->
                onEvent(HomeUiEvent.ContextSheetPrimaryAction(kind, entityId))
            },
            onItemClick = { itemId ->
                onEvent(HomeUiEvent.DismissSheet)
                onEvent(HomeUiEvent.DecisionTapped(itemId))
            }
        )
    }
}

@Composable
private fun HomeScreenPreviewWrapper(
    scenario: HomeScenario,
    reducedMotion: Boolean = false
) {
    ChankoTheme {
        val baseState = HomeFakeDataSource().getStateForScenario(scenario)
        HomeScreen(
            state = baseState.copy(isReducedMotionEnabled = reducedMotion),
            onEvent = {}
        )
    }
}

@Preview(name = "S21 - Candy Pop 3D v1.0 Baseline (412dp)", showBackground = true, widthDp = 412, heightDp = 1150)
@Composable
private fun HomeScreenS21BaselinePreview() {
    HomeScreenPreviewWrapper(HomeScenario.S21)
}

@Preview(name = "S22 - Contextual Sheet Open (393dp)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenS22SheetOpenPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S22)
}

@Preview(name = "S05 - Empty Attention & Unset Budget (360dp)", showBackground = true, widthDp = 360, heightDp = 950)
@Composable
private fun HomeScreenS05EmptyPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S05)
}

@Preview(name = "S06 - Over Budget State (120%)", showBackground = true, widthDp = 393, heightDp = 1050)
@Composable
private fun HomeScreenS06OverBudgetPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S06)
}

@Preview(name = "S12 - Expired Cooling-Off Reconciled to Review", showBackground = true, widthDp = 393, heightDp = 1000)
@Composable
private fun HomeScreenS12CoolingOffExpiredPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S12)
}

@Preview(name = "S13 - Section Error & Loading Isolation", showBackground = true, widthDp = 393, heightDp = 950)
@Composable
private fun HomeScreenS13PartialErrorPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S13)
}

@Preview(name = "S14 - Offline Cache + Sync Timestamp", showBackground = true, widthDp = 393, heightDp = 1000)
@Composable
private fun HomeScreenS14OfflinePreview() {
    HomeScreenPreviewWrapper(HomeScenario.S14)
}

@Preview(name = "S15 - Narrow 320dp & FontScale 2.0 + Missing Price", showBackground = true, widthDp = 320, heightDp = 1400, fontScale = 2.0f)
@Composable
private fun HomeScreenS15NarrowLargeFontPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S15, reducedMotion = true)
}

@Preview(name = "Tablet / Wide 840dp (Centered 600dp Max)", showBackground = true, widthDp = 840, heightDp = 1200)
@Composable
private fun HomeScreenWide840dpPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S21)
}
