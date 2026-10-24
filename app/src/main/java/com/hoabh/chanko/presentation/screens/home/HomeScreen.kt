package com.hoabh.chanko.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.home.components.BudgetCard
import com.hoabh.chanko.presentation.screens.home.components.CompactLivingRoom
import com.hoabh.chanko.presentation.screens.home.components.DraftResumeCard
import com.hoabh.chanko.presentation.screens.home.components.HomeHeader
import com.hoabh.chanko.presentation.screens.home.components.ObservationCard
import com.hoabh.chanko.presentation.screens.home.components.QuickActionsRow
import com.hoabh.chanko.presentation.screens.home.components.YourDecisionsSection
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.ChankoTheme

private val BottomNavClearanceHeight = 88.dp

/**
 * Stateful entry point cho màn hình Home (Tab Trang chủ trong MainNavigation).
 */
@Composable
fun HomeRoute(
    onAddNewItemClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
            HomeUiEffect.NavigateToSettings -> onSettingsClick()
            HomeUiEffect.NavigateToProfile -> onProfileClick()
            HomeUiEffect.OpenQuickCheckFlow,
            HomeUiEffect.OpenSaveItemFlow -> onAddNewItemClick()
            HomeUiEffect.NavigateToBudgetDetails,
            is HomeUiEffect.NavigateToDecisionDetail,
            HomeUiEffect.NavigateToAllDecisions,
            is HomeUiEffect.OpenObservationExplanation,
            is HomeUiEffect.OpenDraftFlow -> Unit
        }
    }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

/**
 * Stateless HomeScreen tuân thủ cấu trúc vùng cuộn của Chanko Home Specification v1.1:
 * 1. Header (HOME-01)
 * 2. Chanko's Compact Living Room (HOME-02 — P2)
 * 3. Quick Actions (HOME-03 — P3)
 * 4. Budget Snapshot (HOME-04 — P4)
 * 5. Your Decisions (HOME-05 — P5)
 * 6. Observation — optional (HOME-06 — P6)
 * 7. Local Draft Indicator — optional (HOME-07 — P6)
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            CandyColors.background,
            CandyColors.backgroundLavender.copy(alpha = 0.35f),
            CandyColors.backgroundPink.copy(alpha = 0.25f),
            CandyColors.background
        )
    )
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = CandySpace.lg,
                top = statusBarTopPadding + CandySpace.md,
                end = CandySpace.lg,
                bottom = CandySpace.lg
            ),
            verticalArrangement = Arrangement.spacedBy(CandySpace.lg)
        ) {
            // 1. HOME-01 — Header
            item(key = "home_01_header") {
                HomeHeader(
                    header = state.header,
                    onSettingsClick = { onEvent(HomeUiEvent.SettingsClick) },
                    onProfileClick = { onEvent(HomeUiEvent.ProfileClick) },
                    canOpenSettings = state.capabilities.canOpenSettings,
                    canOpenProfile = state.capabilities.canOpenProfile
                )
            }

            // 2. HOME-02 — Chanko's Compact Living Room (with embedded ChankoDialogue)
            item(key = "home_02_living_room") {
                when (val roomState = state.livingRoom) {
                    is SectionState.Content -> {
                        CompactLivingRoom(
                            livingRoom = roomState.data,
                            onPropTapped = { kind -> onEvent(HomeUiEvent.PropTapped(kind)) }
                        )
                    }
                    SectionState.Loading,
                    is SectionState.Empty,
                    is SectionState.Error -> {
                        CompactLivingRoom(
                            livingRoom = LivingRoomUi(),
                            onPropTapped = { kind -> onEvent(HomeUiEvent.PropTapped(kind)) }
                        )
                    }
                }
            }

            // 3. HOME-03 — Quick Actions
            item(key = "home_03_quick_actions") {
                QuickActionsRow(
                    onQuickCheckClick = { onEvent(HomeUiEvent.QuickCheckTapped) },
                    onSaveItemClick = { onEvent(HomeUiEvent.SaveItemTapped) }
                )
            }

            // 4. HOME-04 — Budget Snapshot
            item(key = "home_04_budget") {
                BudgetCard(
                    sectionState = state.budgetSection,
                    onDetailsClick = { onEvent(HomeUiEvent.BudgetDetailsTapped) },
                    onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Budget)) },
                    canOpenDetails = state.capabilities.canOpenBudgetDetails
                )
            }

            // 5. HOME-05 — Your Decisions ("Những món cần chú ý")
            item(key = "home_05_decisions") {
                YourDecisionsSection(
                    sectionState = state.decisionsSection,
                    totalCount = state.totalAttentionCount,
                    onDecisionClick = { id -> onEvent(HomeUiEvent.DecisionTapped(id)) },
                    onViewAllClick = { onEvent(HomeUiEvent.ViewAllTapped) },
                    onSaveFirstItemClick = { onEvent(HomeUiEvent.SaveItemTapped) },
                    onRetryClick = { onEvent(HomeUiEvent.RetrySection(HomeSection.Decisions)) },
                    canOpenViewAll = state.capabilities.canOpenViewAll
                )
            }

            // 6. HOME-06 — Observation ("Góc nhìn hôm nay" — optional)
            state.observation?.let { observation ->
                item(key = "home_06_observation_${observation.id}") {
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

            // 7. HOME-07 — Local Draft Indicator (optional)
            state.draft?.let { draft ->
                item(key = "home_07_draft_${draft.id}") {
                    DraftResumeCard(
                        draft = draft,
                        onResumeClick = { id -> onEvent(HomeUiEvent.ResumeDraftTapped(id)) },
                        canResume = state.capabilities.canResumeDraft
                    )
                }
            }

            // Bottom clearance spacer cho cố định Bottom Navigation
            item(key = "bottom_nav_clearance") {
                Spacer(modifier = Modifier.height(BottomNavClearanceHeight))
            }
        }
    }
}

@Preview(name = "P1 - Default Header", showBackground = true, widthDp = 393)
@Composable
private fun HomeScreenDefaultPreview() {
    val fakeDataSource = HomeFakeDataSource()
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.DEFAULT),
            onEvent = {}
        )
    }
}

@Preview(name = "P1 - Long Name & Narrow 320dp", showBackground = true, widthDp = 320)
@Composable
private fun HomeScreenLongNamePreview() {
    val fakeDataSource = HomeFakeDataSource()
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.LONG_NAME),
            onEvent = {}
        )
    }
}

@Preview(name = "P1 - Empty Name & Offline Badge", showBackground = true, widthDp = 393)
@Composable
private fun HomeScreenEmptyNameOfflinePreview() {
    val fakeDataSource = HomeFakeDataSource()
    val baseState = fakeDataSource.getStateForScenario(HomeScenario.EMPTY_NAME)
    ChankoTheme {
        HomeScreen(
            state = baseState.copy(header = baseState.header.copy(isOffline = true)),
            onEvent = {}
        )
    }
}

@Preview(name = "P1 - Font Scale 1.5x", showBackground = true, widthDp = 393, fontScale = 1.5f)
@Composable
private fun HomeScreenLargeFontPreview() {
    val fakeDataSource = HomeFakeDataSource()
    ChankoTheme {
        HomeScreen(
            state = fakeDataSource.getStateForScenario(HomeScenario.LONG_NAME),
            onEvent = {}
        )
    }
}
