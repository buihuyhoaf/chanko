package com.hoabh.chanko.presentation.screens.home.data

import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.GreetingPeriod
import com.hoabh.chanko.presentation.screens.home.HeaderUiModel
import com.hoabh.chanko.presentation.screens.home.HomeUiState
import com.hoabh.chanko.presentation.screens.home.SectionState
import javax.inject.Inject

/**
 * Các kịch bản dữ liệu giả lập xác định (Deterministic Scenarios) cho màn hình Home.
 */
enum class HomeScenario {
    DEFAULT,
    NEW_USER,
    LONG_NAME,
    EMPTY_NAME,
    OFFLINE_CACHE,
    PARTIAL_ERROR
}

class HomeFakeDataSource @Inject constructor() {

    fun getStateForScenario(
        scenario: HomeScenario = HomeScenario.DEFAULT,
        greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING
    ): HomeUiState = when (scenario) {
        HomeScenario.DEFAULT -> HomeUiState(
            header = HeaderUiModel(
                userName = "Hoa",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            budgetSection = SectionState.Content(Unit),
            decisionsSection = SectionState.Content(Unit),
            isObservationVisible = true,
            isLocalDraftVisible = false
        )

        HomeScenario.NEW_USER -> HomeUiState(
            header = HeaderUiModel(
                userName = "Hoa",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            budgetSection = SectionState.Content(Unit),
            decisionsSection = SectionState.Content(Unit),
            isObservationVisible = false,
            isLocalDraftVisible = false
        )

        HomeScenario.LONG_NAME -> HomeUiState(
            header = HeaderUiModel(
                userName = "Nguyễn Hoàng Huyền Trang Bảo Ngọc",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            budgetSection = SectionState.Content(Unit),
            decisionsSection = SectionState.Content(Unit)
        )

        HomeScenario.EMPTY_NAME -> HomeUiState(
            header = HeaderUiModel(
                userName = "   ",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            budgetSection = SectionState.Content(Unit),
            decisionsSection = SectionState.Content(Unit)
        )

        HomeScenario.OFFLINE_CACHE -> HomeUiState(
            header = HeaderUiModel(
                userName = "Hoa",
                greetingPeriod = greetingPeriod,
                isOffline = true
            ),
            budgetSection = SectionState.Content(Unit, isFromCache = true),
            decisionsSection = SectionState.Content(Unit, isFromCache = true),
            isObservationVisible = false,
            isLocalDraftVisible = true
        )

        HomeScenario.PARTIAL_ERROR -> HomeUiState(
            header = HeaderUiModel(
                userName = "Hoa",
                greetingPeriod = greetingPeriod,
                isOffline = false
            ),
            budgetSection = SectionState.Error(R.string.home_section_error_budget),
            decisionsSection = SectionState.Content(Unit),
            isObservationVisible = false,
            isLocalDraftVisible = false
        )
    }
}
