package com.hoabh.chanko.presentation.features.budget.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.features.budget.BudgetUiEvent
import com.hoabh.chanko.presentation.features.budget.BudgetUiState
import com.hoabh.chanko.presentation.features.budget.sheets.BudgetSheetHost
import com.hoabh.chanko.presentation.features.budget.sheets.DiscardChangesDialog
import com.hoabh.chanko.presentation.features.home.components.CardOrganicBackground
import com.hoabh.chanko.presentation.features.home.components.CreditCardTopBar
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Màn hình Chi tiết Ngân sách (BudgetDetailScreen - Budget v3):
 * Bố cục 1 cột cuộn dọc (LazyColumn):
 * 1. Thẻ chip Hero Credit Card:
 *    - Palette 12 tháng native gradient
 *    - Nền hữu cơ sóng 3D + dấu chân watermark
 *    - Mascot Chanko cat
 *    - Top bar: Month badge MM, chip vàng EMV, sóng contactless
 *    - Nhãn "Còn lại" / "Vượt ngân sách" và số tiền to
 *    - Thanh tiến trình capsule và pill phần trăm
 * 2. Thẻ thông tin ngân sách:
 *    - Thời gian chu kỳ
 *    - Ngân sách (hạn mức) kèm icon edit (bút chì) cho phép sửa nếu là tháng hiện tại / canWrite
 *    - Số tiền đã chi
 *    - Số tiền còn lại / vượt
 *    - Tiến độ sử dụng (%)
 * 3. Danh sách các hoạt động gần đây ("Hoạt động gần đây"):
 *    - Hiển thị tất cả giao dịch trong chu kỳ: mua sản phẩm, hoàn tiền, và lịch sử điều chỉnh ngân sách
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

    val titleText = stringResource(R.string.budget_detail_title)

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
                            contentDescription = stringResource(R.string.back),
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

            // 2. Nội dung cuộn chính dạng 1 Column
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
                // Item 1: Thẻ chip Hero Credit Card
                item {
                    DetailHeroCreditCard(
                        cycle = state.selectedCycle,
                        snapshot = state.snapshot
                    )
                }

                // Item 2: Thẻ thông tin chi tiết ngân sách
                item {
                    BudgetInfoCard(
                        cycle = state.selectedCycle,
                        snapshot = state.snapshot,
                        canWrite = state.canWrite,
                        onEditBudgetClick = { onEvent(BudgetUiEvent.OpenEditBudget) }
                    )
                }

                // Item 3: Danh sách các giao dịch gần đây (gồm cả chi tiêu & điều chỉnh ngân sách)
                item {
                    RecentActivitiesSection(
                        activities = state.recentActivities,
                        adjustments = state.adjustments,
                        onActivityClick = { activity ->
                            onEvent(BudgetUiEvent.ActivityItemClicked(activity))
                        },
                        canWrite = state.canWrite,
                        onEditBudgetClick = { onEvent(BudgetUiEvent.OpenEditBudget) }
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
 * Item 1: Thẻ chip Hero Credit Card với đầy đủ thông tin visual tương đồng Home Screen.
 */
@Composable
private fun DetailHeroCreditCard(
    cycle: BudgetCycle?,
    snapshot: BudgetSnapshot?,
    modifier: Modifier = Modifier
) {
    val themeOrdinal = cycle?.themeIndex ?: 0
    val theme = remember(themeOrdinal) {
        CandyBudgetThemes.getTheme(themeOrdinal)
    }
    val isOver = snapshot?.isOverBudget == true
    val monthLabel = cycle?.labelMonth ?: "11"
    val visualProgress = snapshot?.visualProgress ?: 0f
    val percentText = "${snapshot?.percentageInt ?: 0}%"

    val mainLabel = if (isOver) {
        stringResource(R.string.home_budget_over_budget_label)
    } else {
        stringResource(R.string.home_budget_remaining_label)
    }
    val amountFormatted = if (isOver) {
        snapshot?.overFormatted ?: snapshot?.remainingFormatted ?: "0 đ"
    } else {
        snapshot?.remainingFormatted ?: "0 đ"
    }

    val cardCd = buildString {
        append("Thẻ ngân sách tháng $monthLabel. ")
        append("Hạn mức: ${snapshot?.budgetFormatted ?: "0 đ"}, đã chi: ${snapshot?.spentFormatted ?: "0 đ"}. ")
        if (isOver) {
            append("Vượt ngân sách: $amountFormatted. ")
        } else {
            append("Còn lại: $amountFormatted. ")
        }
        append("Tiến độ: $percentText.")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CandyBudgetThemes.cardMinHeight)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(CandyBudgetThemes.cardRadius),
                ambientColor = theme.dark.copy(alpha = 0.35f),
                spotColor = theme.dark.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(CandyBudgetThemes.cardRadius))
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                role = Role.Button
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = visualProgress.coerceIn(0f, 1f),
                    range = 0f..1f
                )
            },
        shape = RoundedCornerShape(CandyBudgetThemes.cardRadius),
        color = theme.main,
        border = BorderStroke(1.5.dp, theme.light)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Nền hữu cơ với sóng 3D và dấu chân watermark
            CardOrganicBackground(
                theme = theme,
                modifier = Modifier.matchParentSize()
            )

            // Mascot Chanko
            Image(
                painter = painterResource(
                    id = if (isOver) R.drawable.mascot_worried else R.drawable.mascot_happy
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
                    .size(104.dp)
                    .clearAndSetSemantics {}
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = CandyBudgetThemes.cardMinHeight)
                    .padding(
                        start = CandyBudgetThemes.contentPadding,
                        top = CandyBudgetThemes.contentPadding,
                        end = CandyBudgetThemes.contentPadding,
                        bottom = 14.dp
                    )
            ) {
                // TopBar với month chip pill, chip vàng EMV & contactless waves
                CreditCardTopBar(monthLabel = monthLabel)

                // Vùng giữa: Nhãn & Số tiền căn giữa
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.66f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                    ) {
                        Text(
                            text = mainLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = CandyBudgetThemes.labelTextSize
                            ),
                            color = if (isOver) CandyBudgetThemes.coralWarning else Color.White
                        )

                        Text(
                            text = amountFormatted,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = CandyBudgetThemes.amountTextSize,
                                lineHeight = 38.sp
                            ),
                            color = if (isOver) CandyBudgetThemes.coralWarning else Color.White
                        )
                    }
                }

                // Vùng dưới: Thanh tiến trình & Percent pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.35f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(visualProgress.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (isOver) CandyBudgetThemes.coralWarning else Color.White
                                )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = percentText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            ),
                            color = if (isOver) CandyBudgetThemes.coralWarning else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Item 2: Thẻ thông tin ngân sách (Card trắng bo góc 20dp, viền nhạt):
 * - Thời gian chu kỳ
 * - Ngân sách (kèm icon bút chì để sửa nếu canWrite)
 * - Số tiền đã chi
 * - Còn lại / Vượt ngân sách
 * - Tiến độ sử dụng: %
 */
@Composable
private fun BudgetInfoCard(
    cycle: BudgetCycle?,
    snapshot: BudgetSnapshot?,
    canWrite: Boolean,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateRangeText = cycle?.dateRangeText ?: "01/11 – 30/11/2026"
    val budgetFormatted = snapshot?.budgetFormatted ?: "0 đ"
    val spentFormatted = snapshot?.spentFormatted ?: "0 đ"
    val isOver = snapshot?.isOverBudget == true
    val remainingOrOverLabel = if (isOver) "Vượt ngân sách" else "Còn lại"
    val remainingOrOverFormatted = if (isOver) {
        snapshot?.overFormatted ?: "0 đ"
    } else {
        snapshot?.remainingFormatted ?: "0 đ"
    }
    val percentage = snapshot?.percentageInt ?: 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CandyColors.detailCardBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Thời gian
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Thời gian",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary
                )
                Text(
                    text = dateRangeText,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CandyColors.textPrimary
                )
            }

            // 2. Ngân sách (kèm icon edit nếu canWrite)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ngân sách",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = budgetFormatted,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CandyColors.textPrimary
                    )

                    if (canWrite) {
                        IconButton(
                            onClick = onEditBudgetClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            ChankoIcon(
                                id = ChankoIconId.ACT_05,
                                contentDescription = stringResource(R.string.budget_wallet_action_edit_budget),
                                size = 20.dp
                            )
                        }
                    }
                }
            }

            // 3. Đã chi
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Đã chi",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary
                )
                Text(
                    text = spentFormatted,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CandyColors.textPrimary
                )
            }

            // 4. Còn lại / Vượt ngân sách
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = remainingOrOverLabel,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary
                )
                Text(
                    text = remainingOrOverFormatted,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isOver) CandyColors.overBudget else CandyColors.textPrimary
                )
            }

            // 5. Tiến độ sử dụng: %
            Text(
                text = "Tiến độ sử dụng: $percentage%",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isOver) CandyColors.overBudget else CandyColors.textPrimary
            )
        }
    }
}

/**
 * Item 3: Danh sách tất cả các giao dịch trong chu kỳ ("Hoạt động gần đây"):
 * Hiển thị toàn bộ các giao dịch bao gồm mua sản phẩm, hoàn tiền, và lịch sử điều chỉnh ngân sách.
 */
@Composable
private fun RecentActivitiesSection(
    activities: List<BudgetActivity>,
    adjustments: List<BudgetAdjustment>,
    onActivityClick: (BudgetActivity) -> Unit,
    canWrite: Boolean,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasItems = activities.isNotEmpty() || adjustments.isNotEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.budget_recent_activity_title),
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = CandyColors.textPrimary
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, CandyColors.detailCardBorder),
            shadowElevation = 2.dp
        ) {
            if (!hasItems) {
                Text(
                    text = stringResource(R.string.budget_activity_empty),
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Danh sách các hoạt động chi tiêu / hoàn tiền
                    activities.forEachIndexed { index, activity ->
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
                                .clickable { onActivityClick(activity) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CandyColors.detailIconSurface)
                                        .border(1.dp, CandyColors.detailCardBorder, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dummyThumbnail != null) {
                                        Image(
                                            painter = painterResource(id = dummyThumbnail),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        ChankoIcon(
                                            id = ChankoIconId.ITM_01,
                                            contentDescription = null,
                                            size = 20.dp
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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

                        if (index < activities.size - 1 || adjustments.isNotEmpty()) {
                            HorizontalDivider(
                                color = CandyColors.detailDivider,
                                thickness = 1.dp
                            )
                        }
                    }

                    // 2. Danh sách các điều chỉnh hạn mức ngân sách
                    adjustments.forEachIndexed { index, adjustment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = canWrite, onClick = onEditBudgetClick)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CandyColors.detailIconSurface)
                                        .border(1.dp, CandyColors.detailCardBorder, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ChankoIcon(
                                        id = ChankoIconId.ACT_05,
                                        contentDescription = null,
                                        size = 20.dp
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = stringResource(R.string.budget_adjustment_history_title),
                                        fontFamily = Nunito,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = CandyColors.textPrimary
                                    )
                                    Text(
                                        text = "${adjustment.oldLimitFormatted} → ${adjustment.newLimitFormatted} · ${adjustment.adjustedAt}",
                                        fontFamily = Nunito,
                                        fontSize = 12.sp,
                                        color = CandyColors.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = adjustment.diffFormatted,
                                fontFamily = Nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (adjustment.isIncrease) CandyColors.success else CandyColors.overBudget
                            )
                        }

                        if (index < adjustments.size - 1) {
                            HorizontalDivider(
                                color = CandyColors.detailDivider,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
