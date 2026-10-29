package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun RenameCreditCardSheet(
    card: CreditCard,
    canWrite: Boolean,
    isSubmitting: Boolean,
    existingCardNames: List<String>,
    onSave: (String) -> Unit,
    onCancel: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var rawInput by remember { mutableStateOf(card.name) }
    val validation = CreditCard.validateName(rawInput)

    val isDuplicate = when (validation) {
        is CardNameValidationResult.Valid -> {
            existingCardNames.any {
                !it.equals(card.name, ignoreCase = true) && it.equals(validation.normalizedName, ignoreCase = true)
            }
        }
        else -> false
    }

    val isChanged = validation is CardNameValidationResult.Valid && validation.normalizedName != card.name
    val isValid = validation is CardNameValidationResult.Valid && !isDuplicate
    val canSubmit = isValid && isChanged && canWrite && !isSubmitting
    val isDirty = rawInput.trim() != card.name.trim()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        Text(
            text = stringResource(R.string.budget_card_rename_title),
            fontFamily = Nunito,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CandyColors.textPrimary
        )

        OutlinedTextField(
            value = rawInput,
            onValueChange = { rawInput = it },
            label = { Text(stringResource(R.string.budget_card_add_field_label), fontFamily = Nunito) },
            singleLine = true,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val errorText = when {
                        isDuplicate -> stringResource(R.string.budget_card_error_duplicate)
                        validation is CardNameValidationResult.TooLong -> stringResource(R.string.budget_card_error_too_long)
                        else -> ""
                    }
                    Text(
                        text = errorText,
                        fontFamily = Nunito,
                        fontSize = 11.sp,
                        color = CandyColors.overBudget
                    )
                    Text(
                        text = "${rawInput.length}/40",
                        fontFamily = Nunito,
                        fontSize = 11.sp,
                        color = CandyColors.textSecondary
                    )
                }
            },
            isError = isDuplicate || validation is CardNameValidationResult.TooLong,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CandyColors.brandPurple,
                unfocusedBorderColor = CandyColors.divider,
                errorBorderColor = CandyColors.overBudget
            ),
            modifier = Modifier.fillMaxWidth()
        )

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
                    .clickable(role = Role.Button) { onCancel(isDirty) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.budget_btn_cancel),
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CandyColors.textPrimary
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
                        text = if (isSubmitting) "Đang lưu…" else stringResource(R.string.budget_btn_save),
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
