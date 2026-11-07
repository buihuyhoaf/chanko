package com.hoabh.chanko.presentation.features.budget.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.features.budget.BudgetUiEvent
import com.hoabh.chanko.presentation.features.budget.BudgetUiState
import com.hoabh.chanko.presentation.features.budget.sheets.BudgetSheetHost
import com.hoabh.chanko.presentation.features.budget.sheets.DiscardChangesDialog
import com.hoabh.chanko.presentation.features.budget.wallet.components.AdjustmentHistorySection
import com.hoabh.chanko.presentation.features.budget.wallet.components.BudgetSettingsSection
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Màn hình Chi tiết Ngân sách (BudgetDetailScreen - Budget v3):
 * - Hero Card: hạn mức, đã chi thực tế, còn lại/vượt, tiến độ (clamp 0..100%, nhãn % thật)
 * - Quá khứ: Read-only
 * - Hiện tại: Cho phép điều chỉnh (khi online)
 * - Tối đa 5 hoạt động gần nhất trong chu kỳ (sử dụng dummy images có sẵn trong app)
 * - Lịch sử điều chỉnh hạn mức
 */
@Composable
fun BudgetDetailScreen(
    state: BudgetUiState,
    onEvent: (BudgetUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Bắt phím Back phần cứng / hệ thống
    BackHandler {
        when {
            state.isSubmitting -> Unit
            state.isDiscardDialogOpen -> onEvent(BudgetUiEvent.CancelDiscardChanges)
            state.currentSheet != null -> onEvent(BudgetUiEvent.RequestCloseCurrentSheet)
            else -> onBackClick()
        }
    }

    val titleText = state.selectedCycle?.label ?: stringResource(R.string.budget_title)

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
            // 1. Top App Bar với nút Back và Tiêu đề
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_02,
                            contentDescription = "Quay lại",
                            size = 24.dp
                        )
                    }

                    Text(
                        text = titleText,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = CandyColors.textPrimary
                    )
                }

                if (state.canWrite) {
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            ChankoIcon(
                                id = ChankoIconId.ACT_10,
                                contentDescription = stringResource(R.string.budget_menu_options)
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.budget_wallet_action_edit_budget)) },
                                leadingIcon = {
                                    ChankoIcon(
                                        id = ChankoIconId.ACT_05,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                enabled = state.canWrite,
                                onClick = {
                                    isMenuExpanded = false
                                    onEvent(BudgetUiEvent.OpenEditBudget)
                                }
                            )
                        }
                    }
                }
            }

            // 2. Nội dung cuộn chính
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = CandySpace.lg,
                    end = CandySpace.lg,
                    top = CandySpace.xs,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // Section 1: Hero Card Ngân sách mua sắm
                item {
                    BudgetSettingsSection(
                        selectedCycle = state.selectedCycle,
                        snapshot = state.snapshot,
                        canWrite = state.canWrite,
                        onCycleClick = { onEvent(BudgetUiEvent.OpenCyclePicker) }
                    )
                }

                // Section 2: Biến động chi tiêu gần đây (tối đa 5 giao dịch trong chu kỳ)
                item {
                    RecentActivityPreviewSection(
                        activities = state.recentActivities.take(5),
                        onItemClick = { activity ->
                            onEvent(BudgetUiEvent.ActivityItemClicked(activity))
                        }
                    )
                }

                // Section 3: Lịch sử điều chỉnh ngân sách
                item {
                    AdjustmentHistorySection(
                        adjustments = state.adjustments,
                        cycleRange = state.selectedCycle?.dateRangeText ?: "",
                        isExpanded = state.isHistoryExpanded,
                        onToggleExpanded = { onEvent(BudgetUiEvent.ToggleHistoryAccordion()) }
                    )
                }
            }
        }

        // Bottom Sheets (Chọn chu kỳ & Sửa ngân sách)
        BudgetSheetHost(
            destination = state.currentSheet,
            state = state,
            onEvent = onEvent
        )

        // Discard Changes Dialog
        if (state.isDiscardDialogOpen) {
            DiscardChangesDialog(
                onConfirmDiscard = { onEvent(BudgetUiEvent.ConfirmDiscardChanges) },
                onContinueEditing = { onEvent(BudgetUiEvent.CancelDiscardChanges) }
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

/**
 * Hiển thị tối đa 5 biến động chi tiêu gần nhất trong chu kỳ (Budget v3 Detail).
 */
@Composable
private fun RecentActivityPreviewSection(
    activities: List<BudgetActivity>,
    onItemClick: (BudgetActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.budget_activity_title),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CandyColors.textPrimary
            )

            if (activities.isEmpty()) {
                Text(
                    text = stringResource(R.string.budget_activity_empty),
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activities.forEach { activity ->
                        val dummyThumbnail = when {
                            activity.title.contains("tai nghe", ignoreCase = true) -> R.drawable.sample_headphones
                            activity.title.contains("bàn phím", ignoreCase = true) -> R.drawable.sample_keyboard
                            activity.title.contains("áo", ignoreCase = true) -> R.drawable.sample_jacket
                            activity.type == BudgetActivityType.PURCHASE_RECORDED -> R.drawable.sample_headphones
                            else -> null
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onItemClick(activity) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (dummyThumbnail != null) {
                                    Image(
                                        painter = painterResource(id = dummyThumbnail),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Column {
                                    Text(
                                        text = activity.title,
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = CandyColors.textPrimary
                                    )
                                    Text(
                                        text = activity.occurredAt,
                                        fontFamily = Nunito,
                                        fontSize = 12.sp,
                                        color = CandyColors.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = activity.amountFormatted,
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (activity.amountChangeMinor < 0) CandyColors.overBudget else CandyColors.success
                            )
                        }
                    }
                }
            }
        }
    }
}
