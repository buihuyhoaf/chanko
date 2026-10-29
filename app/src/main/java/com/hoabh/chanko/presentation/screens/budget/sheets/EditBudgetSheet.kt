package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.presentation.screens.budget.BudgetUiState

@Composable
fun EditBudgetSheet(
    currentLimit: Long,
    currentSpent: Long,
    cycleRange: String,
    draftText: String? = null,
    onDraftChange: ((String) -> Unit)? = null,
    canWrite: Boolean,
    disabledReason: String? = null,
    isSubmitting: Boolean,
    inlineError: String? = null,
    hasConflict: Boolean = false,
    onReloadConflict: () -> Unit = {},
    onSave: (Long) -> Unit,
    onCancel: (Boolean) -> Unit,
    onClose: (Boolean) -> Unit = onCancel,
    modifier: Modifier = Modifier
) {
    val initialText = if (currentLimit > 0L) MoneyVnd.formatWithoutSymbol(currentLimit) else ""
    var localDraft by rememberSaveable(currentLimit) {
        mutableStateOf(draftText?.takeIf { it.isNotEmpty() } ?: initialText)
    }
    LaunchedEffect(draftText) {
        if (draftText != null && draftText != localDraft) {
            localDraft = draftText
        }
    }
    val rawInput = localDraft

    var hadFocus by rememberSaveable { mutableStateOf(false) }
    var hasBlurred by rememberSaveable { mutableStateOf(false) }

    val trimmed = rawInput.trim()
    val parsedAmount = MoneyVnd.parse(rawInput)
    val isValid = parsedAmount != null && MoneyVnd.isValidBudget(parsedAmount)
    val isDirty = BudgetUiState.isBudgetDraftDirty(rawInput, initialText)
    val canSubmit = isValid && isDirty && canWrite && !isSubmitting && !hasConflict

    val fieldError: String? = when {
        trimmed.isEmpty() && hasBlurred -> stringResource(R.string.budget_edit_error_empty)
        trimmed.isNotEmpty() && parsedAmount == null -> {
            val digitsOnly = trimmed.replace(".", "").replace(",", "").replace(" ", "")
            if (digitsOnly.isNotEmpty() && digitsOnly.all { it.isDigit() } && digitsOnly.length > 12) {
                stringResource(R.string.budget_edit_error_overflow)
            } else {
                stringResource(R.string.budget_edit_error_invalid)
            }
        }
        parsedAmount != null && parsedAmount <= 0L -> stringResource(R.string.budget_edit_error_invalid)
        parsedAmount != null && parsedAmount > MoneyVnd.MAX_BUDGET_AMOUNT -> stringResource(R.string.budget_edit_error_overflow)
        else -> null
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Header: Title + Range Subtitle + Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.budget_edit_title),
                    fontFamily = Nunito,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Text(
                    text = stringResource(R.string.budget_edit_cycle_range, cycleRange),
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = CandyColors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .widthIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isSubmitting, role = Role.Button) {
                        onClose(isDirty)
                    }
                    .padding(horizontal = CandySpace.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_btn_close),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isSubmitting) CandyColors.brandPurple else CandyColors.textSecondary
                )
            }
        }

        // Disabled Reason Banner (Offline / Past Cycle)
        if (!canWrite && !disabledReason.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CandyColors.budgetWarningBadgeBg)
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.sm)
            ) {
                Text(
                    text = disabledReason,
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CandyColors.budgetWarningBadgeInk
                )
            }
        }

        // Conflict Banner with "Tải lại" button (C04 / AC09)
        if (hasConflict) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CandyColors.budgetWarningBadgeBg)
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = inlineError ?: stringResource(R.string.budget_edit_conflict),
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.overBudget,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = CandyColors.brandPurple,
                    modifier = Modifier
                        .padding(start = CandySpace.sm)
                        .heightIn(min = 48.dp)
                        .semantics { role = Role.Button }
                        .clickable(enabled = !isSubmitting, role = Role.Button, onClick = onReloadConflict)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = CandySpace.md),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.budget_edit_conflict_reload),
                            fontFamily = Nunito,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else if (!inlineError.isNullOrBlank()) {
            Text(
                text = inlineError,
                fontFamily = Nunito,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CandyColors.overBudget
            )
        }

        // Input Field
        OutlinedTextField(
            value = rawInput,
            onValueChange = { newValue ->
                localDraft = newValue
                onDraftChange?.invoke(newValue)
            },
            enabled = canWrite && !isSubmitting,
            label = { Text(stringResource(R.string.budget_edit_field_label), fontFamily = Nunito) },
            singleLine = true,
            isError = fieldError != null,
            supportingText = if (fieldError != null) {
                {
                    Text(
                        text = fieldError,
                        fontFamily = Nunito,
                        fontSize = 11.sp,
                        color = CandyColors.overBudget
                    )
                }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CandyColors.brandPurple,
                unfocusedBorderColor = CandyColors.divider,
                errorBorderColor = CandyColors.overBudget,
                focusedLabelColor = CandyColors.brandPurple
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        hadFocus = true
                    } else if (hadFocus) {
                        hasBlurred = true
                    }
                }
        )

        // Live Preview Section
        if (parsedAmount != null && isValid) {
            val newLimit = parsedAmount
            val isOver = newLimit < currentSpent
            val remainingOrOver = if (isOver) currentSpent - newLimit else newLimit - currentSpent
            val ratio = if (newLimit > 0L) currentSpent.toFloat() / newLimit else 0f
            val percentageInt = BudgetSnapshot.calculatePercentageInt(currentSpent, newLimit)
            val visualProgress = ratio.coerceIn(0f, 1f)

            val animatedProgress by animateFloatAsState(targetValue = visualProgress, label = "EditPreviewProgress")

            val previewSpoken = stringResource(
                if (isOver) R.string.budget_edit_preview_spoken_over else R.string.budget_edit_preview_spoken_remaining,
                MoneyVnd.spoken(newLimit), MoneyVnd.spoken(currentSpent),
                MoneyVnd.spoken(remainingOrOver), percentageInt
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyColors.primarySoft)
                    .semantics { contentDescription = previewSpoken }
                    .padding(CandySpace.md),
                verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                // Warning if new limit < spent
                if (isOver) {
                    Text(
                        text = stringResource(R.string.budget_edit_warning_below_spent, MoneyVnd.format(remainingOrOver)),
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyColors.overBudget
                    )
                }

                // Ngân sách mới
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.budget_edit_preview_new_limit),
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = CandyColors.textSecondary
                    )
                    Text(
                        text = MoneyVnd.format(newLimit),
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyColors.textPrimary
                    )
                }

                // Đã chi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.budget_edit_preview_spent),
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = CandyColors.textSecondary
                    )
                    Text(
                        text = MoneyVnd.format(currentSpent),
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CandyColors.textPrimary
                    )
                }

                // Còn lại / Vượt ngân sách sau thay đổi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isOver) {
                            stringResource(R.string.budget_edit_preview_new_over)
                        } else {
                            stringResource(R.string.budget_edit_preview_new_remaining)
                        },
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = if (isOver) CandyColors.overBudget else CandyColors.textSecondary
                    )
                    Text(
                        text = MoneyVnd.format(remainingOrOver),
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOver) CandyColors.overBudget else CandyColors.brandPurple
                    )
                }

                // Progress Bar + Percentage
                Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(CandyColors.budgetSnapshotProgressTrack)
                            .semantics {
                                progressBarRangeInfo = ProgressBarRangeInfo(visualProgress, 0f..1f)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(if (isOver) CandyColors.overBudget else CandyColors.brandPurple)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${percentageInt}%",
                            fontFamily = Nunito,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) CandyColors.overBudget else CandyColors.brandPurple
                        )
                    }
                }
            }
        } else {
            // Invalid or Empty Guidance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CandyColors.budgetSnapshotProgressTrack)
                    .padding(CandySpace.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fieldError ?: stringResource(R.string.budget_edit_preview_invalid),
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (fieldError != null) CandyColors.overBudget else CandyColors.textSecondary
                )
            }
        }

        // Footer Actions: Hủy / Lưu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = CandySpace.sm),
            horizontalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            Surface(
                shape = CircleShape,
                color = CandyColors.budgetSnapshotProgressTrack,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { role = Role.Button }
                    .clickable(enabled = !isSubmitting, role = Role.Button) { onCancel(isDirty) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.budget_btn_cancel),
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isSubmitting) CandyColors.textPrimary else CandyColors.textSecondary
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = if (canSubmit) CandyColors.brandPurple else CandyColors.divider,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .semantics { role = Role.Button }
                    .clickable(enabled = canSubmit, role = Role.Button) {
                        parsedAmount?.let { onSave(it) }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSubmitting) stringResource(R.string.budget_status_saving) else stringResource(R.string.budget_btn_save),
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canSubmit) Color.White else CandyColors.textSecondary
                    )
                }
            }
        }
    }
}
