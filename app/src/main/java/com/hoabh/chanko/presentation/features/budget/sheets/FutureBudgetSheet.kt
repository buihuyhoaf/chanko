package com.hoabh.chanko.presentation.features.budget.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.Nunito
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Nội dung của BottomSheet thiết lập (Setup) hoặc chỉnh sửa (Edit) ngân sách tương lai (Budget v3).
 */
@Composable
fun FutureBudgetSheetContent(
    yearMonth: YearMonth,
    initialLimit: Long?,
    suggestedLimit: Long?,
    isOnline: Boolean,
    isSaving: Boolean,
    onSave: (Long) -> Unit,
    onClose: (isDirty: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = initialLimit != null && initialLimit > 0L
    val initialText = remember(initialLimit) {
        if (isEditMode) MoneyVnd.formatWithoutSymbol(initialLimit!!) else ""
    }

    var rawInput by remember { mutableStateOf(initialText) }

    val lastDay = yearMonth.lengthOfMonth()
    val dateRangeText = "01/%02d – %02d/%02d/%d".format(yearMonth.monthValue, lastDay, yearMonth.monthValue, yearMonth.year)
    val monthTitle = "Tháng %02d/%d".format(yearMonth.monthValue, yearMonth.year)

    val isDirty = rawInput.trim() != initialText.trim()
    val parsedAmount = MoneyVnd.parse(rawInput)
    val isValid = parsedAmount != null && MoneyVnd.isValidBudget(parsedAmount)

    val errorMessage = when {
        rawInput.isNotBlank() && parsedAmount == null -> "Số tiền không hợp lệ"
        rawInput.isNotBlank() && parsedAmount != null && parsedAmount <= 0L -> "Ngân sách phải lớn hơn 0"
        rawInput.isNotBlank() && parsedAmount != null && !MoneyVnd.isValidBudget(parsedAmount) -> "Ngân sách vượt quá giới hạn cho phép"
        else -> null
    }

    val canSave = isOnline && !isSaving && isValid && (!isEditMode || parsedAmount != initialLimit)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sheet Illustration: Purple calendar (104dp) & Gold coins (56dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Image(
                painter = painterResource(id = R.drawable.budget_setup_calendar_purple),
                contentDescription = null,
                modifier = Modifier.size(104.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.budget_setup_coins_gold),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Header: Title, Date Range, Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isEditMode) "Chỉnh sửa ngân sách" else "Thiết lập ngân sách",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = CandyColors.textPrimary
                )
                Text(
                    text = "$monthTitle ($dateRangeText)",
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    color = CandyColors.textSecondary
                )
            }

            IconButton(
                onClick = { onClose(isDirty) },
                modifier = Modifier.size(36.dp)
            ) {
                ChankoIcon(
                    id = ChankoIconId.ACT_02,
                    contentDescription = "Đóng"
                )
            }
        }

        // Subtitle helper
        Text(
            text = "Ngân sách này sẽ áp dụng cho toàn bộ chi tiêu trong tháng tương lai và chưa phát sinh giao dịch thực tế.",
            fontFamily = Nunito,
            fontSize = 13.sp,
            color = CandyColors.mutedInk
        )

        // Amount Input Field
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = rawInput,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                    rawInput = filtered
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền ngân sách (VND)") },
                placeholder = { Text("Ví dụ: 6.000.000") },
                singleLine = true,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CandyColors.brandPurple,
                    unfocusedBorderColor = CandyColors.homeCardBorder,
                    errorBorderColor = CandyColors.overBudget
                ),
                trailingIcon = {
                    if (rawInput.isNotBlank()) {
                        IconButton(onClick = { rawInput = "" }) {
                            ChankoIcon(
                                id = ChankoIconId.ACT_02,
                                contentDescription = "Xóa nội dung",
                                size = 18.dp
                            )
                        }
                    }
                }
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = CandyColors.overBudget,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Suggestion Chip from preceding budget
        if (suggestedLimit != null && suggestedLimit > 0L && rawInput.isBlank()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(CandyColors.backgroundLavender)
                    .border(1.dp, CandyColors.brandPurple.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                .clickable {
                    rawInput = MoneyVnd.formatWithoutSymbol(suggestedLimit)
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChankoIcon(
                    id = ChankoIconId.BUD_01,
                    contentDescription = null,
                    size = 18.dp
                )
                Text(
                    text = "Gợi ý từ tháng trước: ${MoneyVnd.format(suggestedLimit)}",
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CandyColors.brandPurple
                )
            }
        }

        // Offline Banner Warning
        if (!isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CandyColors.overBudget.copy(alpha = 0.1f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Bạn đang ở chế độ ngoại tuyến. Không thể lưu thay đổi lúc này.",
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    color = CandyColors.overBudget
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CTA Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onClose(isDirty) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Hủy",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CandyColors.textSecondary
                )
            }

            Button(
                onClick = {
                    if (canSave && parsedAmount != null) {
                        onSave(parsedAmount)
                    }
                },
                enabled = canSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.brandPurple,
                    disabledContainerColor = CandyColors.addItemDisabledSurface
                )
            ) {
                Text(
                    text = if (isSaving) "Đang lưu..." else "Lưu ngân sách",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * BottomSheet hiển thị thiết lập hoặc chỉnh sửa ngân sách cho các tháng tương lai (AC04, AC07).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureBudgetSheet(
    yearMonth: YearMonth,
    initialLimit: Long?,
    suggestedLimit: Long?,
    isOnline: Boolean,
    isSaving: Boolean,
    onSave: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val scope = rememberCoroutineScope()
    var showDiscardDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSaving) {
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = CandyBudgetThemes.cardRadius, topEnd = CandyBudgetThemes.cardRadius),
        containerColor = Color.White,
        dragHandle = null,
        modifier = modifier
    ) {
        FutureBudgetSheetContent(
            yearMonth = yearMonth,
            initialLimit = initialLimit,
            suggestedLimit = suggestedLimit,
            isOnline = isOnline,
            isSaving = isSaving,
            onSave = onSave,
            onClose = { isDirty ->
                if (isDirty) {
                    showDiscardDialog = true
                } else {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }
            }
        )
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onConfirmDiscard = {
                showDiscardDialog = false
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            },
            onContinueEditing = {
                showDiscardDialog = false
            }
        )
    }
}
