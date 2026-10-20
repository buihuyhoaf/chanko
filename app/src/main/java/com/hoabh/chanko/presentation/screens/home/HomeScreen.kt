package com.hoabh.chanko.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.presentation.screens.home.components.CoolingOffSection
import com.hoabh.chanko.presentation.screens.home.components.HomeHeader
import com.hoabh.chanko.presentation.screens.home.components.ReadyReviewSection
import com.hoabh.chanko.presentation.screens.home.components.SpendingCalendarCard
import com.hoabh.chanko.presentation.screens.home.model.BudgetSummary
import com.hoabh.chanko.presentation.screens.home.model.CalendarDayState
import com.hoabh.chanko.presentation.screens.home.model.DailyPurchaseSummary
import com.hoabh.chanko.presentation.screens.home.model.SpendingCycle
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.ChankoTheme
import java.time.LocalDate

@Composable
fun HomeRoute(
    onAddNewItemClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReviewItemClick: (Int) -> Unit = {},
    onItemClick: (Int) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onAddNewItemClick = onAddNewItemClick,
        onSettingsClick = onSettingsClick,
        onProfileClick = onProfileClick,
        onReviewItemClick = onReviewItemClick,
        onItemClick = onItemClick,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    onAddNewItemClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onReviewItemClick: (Int) -> Unit,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle lavender to pink gradient overlay on background cream per UI-HANDOFF §1
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            CandyColors.background,
            CandyColors.backgroundLavender.copy(alpha = 0.35f),
            CandyColors.backgroundPink.copy(alpha = 0.30f),
            CandyColors.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        when (state) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CandyColors.brandPurple)
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.home_error_load),
                            style = MaterialTheme.typography.bodyLarge,
                            color = CandyColors.textPrimary
                        )
                        Button(
                            onClick = { onEvent(HomeUiEvent.Retry) },
                            colors = ButtonDefaults.buttonColors(containerColor = CandyColors.brandPurple),
                            shape = CircleShape
                        ) {
                            Text(
                                text = stringResource(R.string.home_btn_retry),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight(800))
                            )
                        }
                    }
                }
            }

            is HomeUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Header (Greeting + Settings + Avatar)
                    item(key = "header") {
                        HomeHeader(
                            userName = state.userName,
                            onSettingsClick = onSettingsClick,
                            onProfileClick = onProfileClick
                        )
                    }

                    // 2. Spending Calendar Card
                    item(key = "spending_calendar") {
                        SpendingCalendarCard(
                            cycle = state.cycle,
                            days = state.calendarDays,
                            selectedDate = state.selectedDate,
                            selectedTransactions = state.selectedDateTransactions,
                            budgetSummary = state.budgetSummary,
                            mascotSpeechMessage = state.mascotSpeechMessage,
                            onDateClick = { onEvent(HomeUiEvent.SelectDate(it)) },
                            onTodayClick = { onEvent(HomeUiEvent.JumpToToday) },
                            onDayDetailClick = { /* Could open transaction history bottom sheet */ }
                        )
                    }

                    // 3. Ready for Review Section
                    item(key = "ready_review") {
                        ReadyReviewSection(
                            items = state.readyItems,
                            onReviewClick = onReviewItemClick
                        )
                    }

                    // 4. Cooling-off Section
                    item(key = "cooling_off") {
                        CoolingOffSection(
                            items = state.coolingItems,
                            onItemClick = onItemClick
                        )
                    }

                    // Bottom padding spacer to clear the elevated bottom bar (72dp + gap)
                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 1200)
@Composable
private fun HomeScreenPreview() {
    val today = LocalDate.of(2026, 10, 19)
    val cycle = SpendingCycle.create(anchorDay = 15, referenceDate = today)

    val sampleDays = cycle.dates.map { date ->
        CalendarDayState(
            date = date,
            isCycleStart = (date == cycle.startDate),
            isToday = (date == today),
            isSelected = (date == LocalDate.of(2026, 10, 18)),
            purchaseCount = if (date == LocalDate.of(2026, 10, 16) || date == LocalDate.of(2026, 10, 18)) 1 else 0,
            totalSpent = if (date == LocalDate.of(2026, 10, 18)) 1_400_000L else if (date == LocalDate.of(2026, 10, 16)) 1_000_000L else 0L
        )
    }

    val sampleReadyItems = listOf(
        CooldownItem(
            id = 1,
            name = "Áo khoác vintage",
            price = 680_000L,
            addedTimestamp = System.currentTimeMillis() - 7 * 86_400_000L,
            cooldownDurationMillis = 7 * 86_400_000L,
            reason = "Thích màu retro"
        )
    )

    val sampleCoolingItems = listOf(
        CooldownItem(
            id = 2,
            name = "Sony WH-1000XM5",
            price = 7_490_000L,
            addedTimestamp = System.currentTimeMillis() - 10_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Chống ồn khi làm việc"
        ),
        CooldownItem(
            id = 3,
            name = "Keychron K2",
            price = 1_850_000L,
            addedTimestamp = System.currentTimeMillis() - 20_000_000L,
            cooldownDurationMillis = 86_400_000L,
            reason = "Bàn phím cơ văn phòng"
        )
    )

    val contentState = HomeUiState.Content(
        userName = "Hoa",
        cycle = cycle,
        calendarDays = sampleDays,
        selectedDate = LocalDate.of(2026, 10, 18),
        selectedDateTransactions = DailyPurchaseSummary(purchaseCount = 1, totalAmount = 1_400_000L),
        budgetSummary = BudgetSummary(totalBudget = 3_000_000L, totalSpent = 2_400_000L),
        mascotSpeechMessage = "Đã tiêu 80% rồi! Mình chậm lại chút nhé.",
        readyItems = sampleReadyItems,
        coolingItems = sampleCoolingItems,
        today = today
    )

    ChankoTheme {
        HomeScreen(
            state = contentState,
            onEvent = {},
            onAddNewItemClick = {},
            onSettingsClick = {},
            onProfileClick = {},
            onReviewItemClick = {},
            onItemClick = {}
        )
    }
}
