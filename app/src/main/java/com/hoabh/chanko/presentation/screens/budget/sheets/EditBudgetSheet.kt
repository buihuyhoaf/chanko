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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
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
import kotlin.math.roundToInt

@Composable
fun EditBudgetSheet(
    currentLimit: Long,
    currentSpent: Long,
    cycleRange: String,
    canWrite: Boolean,
    isSubmitting: Boolean,
    onSave: (Long) -> Unit,
    onCancel: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialText = if (currentLimit > 0L) MoneyVnd.formatWithoutSymbol(currentLimit) else ""
    var rawInput by remember { mutableStateOf(initialText) }

    val parsedAmount = MoneyVnd.parse(rawInput)
    val isValid = parsedAmount != null && parsedAmount in 1L..MoneyVnd.MAX_BUDGET_AMOUNT
    val isDirty = rawInput.trim() != initialText.trim()
    val canSubmit = isValid && isDirty && canWrite && !isSubmitting

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Title & Range Subtitle
        Column {
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

        // Input Field
        OutlinedTextField(
            value = rawInput,
            onValueChange = { newValue ->
                rawInput = newValue
            },
            label = { Text(stringResource(R.string.budget_edit_field_label), fontFamily = Nunito) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CandyColors.brandPurple,
                unfocusedBorderColor = CandyColors.divider,
                focusedLabelColor = CandyColors.brandPurple
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Live Preview Section
        if (parsedAmount != null && isValid) {
            val newLimit = parsedAmount
            val isOver = newLimit < currentSpent
            val remainingOrOver = if (isOver) currentSpent - newLimit else newLimit - currentSpent
            val ratio = if (newLimit > 0L) currentSpent.toFloat() / newLimit else 0f
            val percentageInt = (ratio * 100f).roundToInt()
            val visualProgress = ratio.coerceIn(0f, 1f)

            val animatedProgress by animateFloatAsState(targetValue = visualProgress, label = "EditPreviewProgress")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CandyColors.primarySoft)
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

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(CandyColors.budgetSnapshotProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (isOver) CandyColors.overBudget else CandyColors.brandPurple)
                    )
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
                    text = stringResource(R.string.budget_edit_preview_invalid),
                    fontFamily = Nunito,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CandyColors.textSecondary
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
                        parsedAmount?.let { onSave(it) }
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
