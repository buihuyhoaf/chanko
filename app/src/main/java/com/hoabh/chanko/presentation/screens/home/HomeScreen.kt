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
import com.hoabh.chanko.presentation.screens.home.components.HomeSheetHost
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
    modifier: Modifier = Modifier,
    onAddNewItemClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(effectFlow = viewModel.effect) { effect ->
        when (effect) {
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
        onChatClick = onChatClick,
        modifier = modifier
    )
}

/**
 * Stateless HomeScreen tuân thủ cấu trúc vùng cuộn của Chanko Home Specification v2.0 (Image-Aligned):
 * 1. Header (HOME-01)
 * 2. Chanko's Compact Living Room + Embedded ChankoDialogue (HOME-02)
 * 3. Quick Actions (HOME-03)
 * 4. Budget Snapshot (HOME-04)
 * 5. Your Decisions ("Những món cần chú ý" — HOME-05)
 * 6. Observation ("Góc nhìn hôm nay" — optional — HOME-06)
 * 7. Local Draft Indicator (optional — HOME-07)
 * + Modal Overlay Sheet Host (`HomeSheetHost` — HOME-08)
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onChatClick: () -> Unit = {}
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = CandySpace.lg,
                top = statusBarTopPadding + CandySpace.xs,
                end = CandySpace.lg,
                bottom = CandySpace.lg
            ),
            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            // 1. HOME-01 — Header
            item(key = "home_01_header") {
                HomeHeader(
                    header = state.header,
                    onProfileClick = { onEvent(HomeUiEvent.ProfileClick) },
                    canOpenProfile = state.capabilities.canOpenProfile,
                    onChatClick = onChatClick
                )
            }

            // 2. HOME-02 — Chanko's Compact Living Room (with embedded ChankoDialogue)
            item(key = "home_02_living_room") {
                val roomData = when (val roomState = state.livingRoom) {
                    is SectionState.Content -> roomState.data
                    else -> LivingRoomUi()
                }
                CompactLivingRoom(
                    livingRoom = roomData,
                    onPropTapped = { kind -> onEvent(HomeUiEvent.PropTapped(kind)) }
                )
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

        // 8. HOME-08 — Modal Overlay Sheet Host (S16, S17, S22)
        HomeSheetHost(
            activeSheet = state.activeSheet,
            onDismiss = { onEvent(HomeUiEvent.DismissSheet) },
            onQuickCheckClick = { onEvent(HomeUiEvent.QuickCheckTapped) },
            onSaveItemClick = { onEvent(HomeUiEvent.SaveItemTapped) },
            onContextPrimaryAction = { kind, entityId ->
                onEvent(HomeUiEvent.ContextSheetPrimaryAction(kind, entityId))
            }
        )
    }
}

@Composable
private fun HomeScreenPreviewWrapper(scenario: HomeScenario) {
    ChankoTheme {
        HomeScreen(
            state = HomeFakeDataSource().getStateForScenario(scenario),
            onEvent = {}
        )
    }
}

@Preview(name = "S21 - Screenshot Baseline (393dp)", showBackground = true, widthDp = 393, heightDp = 1150)
@Composable
private fun HomeScreenS21BaselinePreview() {
    HomeScreenPreviewWrapper(HomeScenario.S21)
}

@Preview(name = "S22 - Global Sheet Open Baseline (393dp)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenS22SheetOpenPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S22)
}

@Preview(name = "S05 - New User Empty State", showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun HomeScreenS05EmptyPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S05)
}

@Preview(name = "S06 - Over Budget State (120%)", showBackground = true, widthDp = 393, heightDp = 950)
@Composable
private fun HomeScreenS06OverBudgetPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S06)
}

@Preview(name = "S09 - Observation + Local Draft", showBackground = true, widthDp = 393, heightDp = 1220)
@Composable
private fun HomeScreenS09ObservationAndDraftPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S09)
}

@Preview(name = "S13 - Section Error & Loading Isolation", showBackground = true, widthDp = 393, heightDp = 900)
@Composable
private fun HomeScreenS13PartialErrorPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S13)
}

@Preview(name = "S15 - Narrow 320dp & FontScale 2.0", showBackground = true, widthDp = 320, heightDp = 1400, fontScale = 2.0f)
@Composable
private fun HomeScreenS15NarrowLargeFontPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S15)
}

@Preview(name = "Tablet / Wide 600dp", showBackground = true, widthDp = 600, heightDp = 1200)
@Composable
private fun HomeScreenWide600dpPreview() {
    HomeScreenPreviewWrapper(HomeScenario.S21)
}
