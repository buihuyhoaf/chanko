package com.hoabh.chanko.presentation.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.budget.components.BudgetCycleHeader
import com.hoabh.chanko.presentation.screens.budget.components.BudgetMascotSlot
import com.hoabh.chanko.presentation.screens.budget.components.BudgetNotableItems
import com.hoabh.chanko.presentation.screens.budget.components.BudgetPaymentBreakdown
import com.hoabh.chanko.presentation.screens.budget.components.BudgetReadOnlyBanner
import com.hoabh.chanko.presentation.screens.budget.components.BudgetRecentActivity
import com.hoabh.chanko.presentation.screens.budget.components.BudgetSummaryCard
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun BudgetScreen(
    state: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top App Bar (đồng bộ 100% kiểu dáng thanh tiêu đề Món đồ)
            BudgetTopAppBar(
                onWalletSettingsClick = { onEvent(BudgetUiEvent.OpenWalletSettings) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    state.isLoading && state.snapshot == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CandyColors.brandPurple,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    state.errorMessage != null && state.snapshot == null -> {
                        BudgetErrorView(
                            message = state.errorMessage,
                            onRetry = { onEvent(BudgetUiEvent.Retry) }
                        )
                    }

                    state.snapshot != null -> {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = CandySpace.lg)
                                .padding(top = CandySpace.xs, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
                        ) {
                            // 1. Cycle Selector Header
                            val cycleText = state.selectedCycle?.dateRangeText ?: ""
                            BudgetCycleHeader(
                                cycleRangeText = cycleText,
                                onClick = { onEvent(BudgetUiEvent.OpenCyclePicker) }
                            )

                            // 2. Offline and Past-cycle Banners
                            BudgetReadOnlyBanner(
                                isOnline = state.isOnline,
                                lastSyncTimeText = state.lastSyncTimeText,
                                isPastCycle = !state.isCurrentCycle
                            )

                            // 3. Summary Card (Ngân sách, Đã chi, Còn lại / Vượt ngân sách, Progress bar)
                            BudgetSummaryCard(
                                snapshot = state.snapshot,
                                canWrite = state.canWrite,
                                onEditBudgetClick = { onEvent(BudgetUiEvent.OpenEditBudget) }
                            )

                            // 4. Mascot 3D Slot
                            BudgetMascotSlot(
                                isOverBudget = state.isOverBudget,
                                isZeroSpend = state.isZeroSpend
                            )

                            // 5. Payment Breakdown (Readonly list)
                            BudgetPaymentBreakdown(
                                methods = state.paymentMethods
                            )

                            // 6. Notable Items (Up to 3 items -> Shared Item Detail)
                            BudgetNotableItems(
                                items = state.notableItems,
                                isPastCycle = !state.isCurrentCycle,
                                onItemClick = { itemId -> onEvent(BudgetUiEvent.NotableItemClicked(itemId)) }
                            )

                            // 7. Recent Activity (Up to 3 budget-affecting events)
                            BudgetRecentActivity(
                                activities = state.recentActivities,
                                onActivityClick = { activity -> onEvent(BudgetUiEvent.ActivityItemClicked(activity)) }
                            )

                            Spacer(modifier = Modifier.height(CandySpace.xl))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetTopAppBar(
    onWalletSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsActionDesc = stringResource(R.string.budget_settings_action)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.budget_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = CandyColors.textPrimary
        )

        IconButton(
            onClick = onWalletSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = settingsActionDesc
                }
        ) {
            ChankoIcon(
                id = ChankoIconId.SYS_04,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BudgetErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(CandySpace.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontFamily = Nunito,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = CandyColors.textSecondary,
            modifier = Modifier.padding(bottom = CandySpace.md)
        )
        Surface(
            shape = CircleShape,
            color = CandyColors.brandPurple,
            modifier = Modifier
                .heightIn(min = 44.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Thử lại"
                }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_btn_retry),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.budgetSnapshotSpendingPanel
                )
            }
        }
    }
}
