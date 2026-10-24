package com.hoabh.chanko.presentation.screens.home

import com.hoabh.chanko.presentation.base.BaseViewModel
import com.hoabh.chanko.presentation.screens.home.data.HomeFakeDataSource
import com.hoabh.chanko.presentation.screens.home.data.HomeScenario
import com.hoabh.chanko.presentation.screens.home.domain.BudgetCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fakeDataSource: HomeFakeDataSource
) : BaseViewModel<HomeUiState, HomeUiEffect>(
    initialState = fakeDataSource.getStateForScenario(HomeScenario.S21)
) {

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.ProfileClick -> {
                if (uiState.value.capabilities.canOpenProfile) {
                    sendEffect(HomeUiEffect.NavigateToProfile)
                }
            }

            is HomeUiEvent.PropTapped -> {
                val room = (uiState.value.livingRoom as? SectionState.Content)?.data
                val prop = room?.props?.firstOrNull { it.kind == event.kind }
                val contextKind = when (event.kind) {
                    RoomPropKind.Hourglass -> ContextKind.Hourglass
                    RoomPropKind.Calendar -> ContextKind.Calendar
                    RoomPropKind.Wallet -> ContextKind.Wallet
                    RoomPropKind.PauseJar -> ContextKind.PauseJar
                }
                updateState {
                    it.copy(
                        activeSheet = HomeSheet.Contextual(
                            kind = contextKind,
                            entityId = event.kind.name,
                            title = prop?.label ?: event.kind.name,
                            subtitle = prop?.summaryText.orEmpty(),
                            highlightValue = prop?.badgeText,
                            isEntityMissing = false
                        )
                    )
                }
            }

            HomeUiEvent.QuickCheckTapped -> {
                updateState { it.copy(activeSheet = null) }
                sendEffect(HomeUiEffect.OpenQuickCheckFlow)
            }

            HomeUiEvent.SaveItemTapped -> {
                updateState { it.copy(activeSheet = null) }
                sendEffect(HomeUiEffect.OpenSaveItemFlow)
            }

            HomeUiEvent.BudgetDetailsTapped -> {
                if (uiState.value.capabilities.canOpenBudgetDetails) {
                    sendEffect(HomeUiEffect.NavigateToBudgetDetails)
                }
            }

            is HomeUiEvent.DecisionTapped -> {
                sendEffect(HomeUiEffect.NavigateToDecisionDetail(event.id))
            }

            HomeUiEvent.ViewAllTapped -> {
                if (uiState.value.capabilities.canOpenViewAll) {
                    sendEffect(HomeUiEffect.NavigateToAllDecisions)
                }
            }

            is HomeUiEvent.ObservationExplainTapped -> {
                if (uiState.value.capabilities.canOpenObservationExplain) {
                    sendEffect(HomeUiEffect.OpenObservationExplanation(event.id))
                }
            }

            is HomeUiEvent.ObservationHideTapped,
            is HomeUiEvent.ObservationDismissTapped -> {
                updateState { it.copy(observation = null) }
            }

            is HomeUiEvent.ResumeDraftTapped -> {
                if (uiState.value.capabilities.canResumeDraft) {
                    sendEffect(HomeUiEffect.OpenDraftFlow(event.id))
                }
            }

            HomeUiEvent.GlobalAddTapped -> {
                // S16: Single active sheet policy, preserve selectedTab
                updateState { it.copy(activeSheet = HomeSheet.Global) }
            }

            HomeUiEvent.DismissSheet -> {
                updateState { it.copy(activeSheet = null) }
            }

            is HomeUiEvent.ContextSheetPrimaryAction -> {
                val currentSheet = uiState.value.activeSheet as? HomeSheet.Contextual
                if (currentSheet != null && !currentSheet.isEntityMissing) {
                    updateState { it.copy(activeSheet = null) }
                    when (event.kind) {
                        ContextKind.Hourglass,
                        ContextKind.Calendar,
                        ContextKind.PauseJar -> sendEffect(HomeUiEffect.NavigateToAllDecisions)
                        ContextKind.Wallet -> sendEffect(HomeUiEffect.NavigateToBudgetDetails)
                        ContextKind.Decision -> {
                            event.entityId?.let { id ->
                                sendEffect(HomeUiEffect.NavigateToDecisionDetail(id))
                            }
                        }
                    }
                }
            }

            is HomeUiEvent.TabSelected -> {
                updateState { it.copy(selectedTab = event.tab) }
            }

            is HomeUiEvent.RetrySection -> {
                val baseline = fakeDataSource.getStateForScenario(HomeScenario.S21)
                updateState { current ->
                    when (event.section) {
                        HomeSection.Budget -> current.copy(
                            budgetSection = SectionState.Content(
                                BudgetCalculator.compute(
                                    cycleLabel = HomeFakeDataSource.DEFAULT_CYCLE_LABEL,
                                    spentMinor = 2_400_000L,
                                    limitMinor = 3_000_000L
                                )
                            )
                        )
                        HomeSection.Decisions -> current.copy(
                            decisionsSection = baseline.decisionsSection,
                            totalAttentionCount = baseline.totalAttentionCount
                        )
                        HomeSection.LivingRoom -> current.copy(
                            livingRoom = baseline.livingRoom
                        )
                        HomeSection.Observation,
                        HomeSection.Draft -> current
                    }
                }
            }

            is HomeUiEvent.SelectScenario -> {
                updateState {
                    fakeDataSource.getStateForScenario(
                        scenario = event.scenario,
                        greetingPeriod = it.header.greetingPeriod
                    )
                }
            }
        }
    }
}
