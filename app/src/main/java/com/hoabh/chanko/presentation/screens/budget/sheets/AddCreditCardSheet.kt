package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.CardNameValidationResult
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun AddCreditCardSheet(
    draftText: String? = null,
    onDraftChange: ((String) -> Unit)? = null,
    canWrite: Boolean,
    disabledReason: String? = null,
    isSubmitting: Boolean,
    inlineError: String? = null,
    existingCardNames: List<String>,
    onSave: (String) -> Unit,
    onCancel: (Boolean) -> Unit,
    onClose: (Boolean) -> Unit = onCancel,
    modifier: Modifier = Modifier
) {
    var localDraft by rememberSaveable { mutableStateOf("") }
    val rawInput = draftText ?: localDraft

    var hadFocus by rememberSaveable { mutableStateOf(false) }
    var hasBlurred by rememberSaveable { mutableStateOf(false) }

    val normalized = CreditCard.normalizeName(rawInput)
    val graphemeCount = CreditCard.graphemeCount(normalized)
    val validation = CreditCard.validateName(rawInput)

    val isDuplicate = when (validation) {
        is CardNameValidationResult.Valid -> {
            existingCardNames.any { CreditCard.isSameName(it, validation.normalizedName) }
        }
        else -> false
    }

    val isValid = validation is CardNameValidationResult.Valid && !isDuplicate
    val isDirty = normalized.isNotEmpty()
    val canSubmit = isValid && canWrite && !isSubmitting

    val fieldErrorText = when {
        isDuplicate -> stringResource(R.string.budget_card_error_duplicate)
        validation is CardNameValidationResult.TooLong -> stringResource(R.string.budget_card_error_too_long)
        validation is CardNameValidationResult.Empty && hasBlurred -> stringResource(R.string.budget_card_error_empty)
        else -> ""
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.budget_card_add_title),
                    fontFamily = Nunito,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
                Text(
                    text = stringResource(R.string.budget_card_manage_helper),
                    fontFamily = Nunito,
                    fontSize = 12.sp,
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

        if (!inlineError.isNullOrBlank()) {
            Text(
                text = inlineError,
                fontFamily = Nunito,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CandyColors.overBudget
            )
        }

        OutlinedTextField(
            value = rawInput,
            onValueChange = { newValue ->
                localDraft = newValue
                onDraftChange?.invoke(newValue)
            },
            enabled = canWrite && !isSubmitting,
            label = { Text(stringResource(R.string.budget_card_add_field_label), fontFamily = Nunito) },
            placeholder = { Text(stringResource(R.string.budget_card_add_field_hint), fontFamily = Nunito) },
            singleLine = true,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fieldErrorText,
                        fontFamily = Nunito,
                        fontSize = 11.sp,
                        color = CandyColors.overBudget
                    )
                    Text(
                        text = "$graphemeCount/${CreditCard.MAX_NAME_LENGTH}",
                        fontFamily = Nunito,
                        fontSize = 11.sp,
                        color = if (graphemeCount > CreditCard.MAX_NAME_LENGTH) CandyColors.overBudget else CandyColors.textSecondary
                    )
                }
            },
            isError = fieldErrorText.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CandyColors.brandPurple,
                unfocusedBorderColor = CandyColors.divider,
                errorBorderColor = CandyColors.overBudget
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

        // Footer Actions: Hủy / Thêm thẻ
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
                        if (validation is CardNameValidationResult.Valid) {
                            onSave(validation.normalizedName)
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSubmitting) "Đang thêm…" else stringResource(R.string.budget_card_action_add),
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
