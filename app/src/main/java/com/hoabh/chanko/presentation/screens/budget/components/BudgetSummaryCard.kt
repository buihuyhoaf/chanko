package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

private val SnapshotCardRadius = 32.dp
private val SnapshotBorderWidth = 2.dp
private val ProgressBarHeight = 12.dp

@Composable
fun BudgetSummaryCard(
    snapshot: BudgetSnapshot,
    canWrite: Boolean,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOver = snapshot.isOverBudget
    val remainingColor = if (isOver) CandyColors.overBudget else CandyColors.budgetSnapshotAmount
    val progressBarFillColor = if (isOver) CandyColors.overBudget else CandyColors.budgetSnapshotProgressFill

    val animatedProgress by animateFloatAsState(
        targetValue = snapshot.visualProgress,
        label = "BudgetProgressAnimation"
    )

    val a11ySummary = buildString {
        append("Ngân sách: ${snapshot.budgetFormatted}. ")
        append("Đã chi: ${snapshot.spentFormatted}. ")
        if (isOver) {
            append("Vượt ngân sách: ${snapshot.overFormatted}. ")
        } else {
            append("Còn lại: ${snapshot.remainingFormatted}. ")
        }
        append("Tỷ lệ: ${snapshot.percentageInt} phần trăm.")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11ySummary },
        shape = RoundedCornerShape(SnapshotCardRadius),
        color = CandyColors.budgetSnapshotBg,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(SnapshotBorderWidth, CandyColors.budgetSnapshotBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            // Row 1: Header + Sửa ngân sách button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.budget_title),
                    fontFamily = Nunito,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )

                Surface(
                    shape = CircleShape,
                    color = if (canWrite) CandyColors.brandPurple else CandyColors.progressTrack,
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Sửa ngân sách"
                        }
                        .clickable(enabled = canWrite, role = Role.Button, onClick = onEditBudgetClick)
                ) {
                    Text(
                        text = stringResource(R.string.budget_summary_edit_btn),
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (canWrite) Color.White else CandyColors.textSecondary,
                        modifier = Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.xs)
                    )
                }
            }

            // Row 2: Con số chính (Còn lại / Vượt ngân sách)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isOver) {
                        stringResource(R.string.budget_summary_label_over)
                    } else {
                        stringResource(R.string.budget_summary_label_remaining)
                    },
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOver) CandyColors.overBudget else CandyColors.textSecondary
                )
                Text(
                    text = if (isOver) snapshot.overFormatted else snapshot.remainingFormatted,
                    fontFamily = Nunito,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = remainingColor
                )
            }

            // Row 3: Progress Bar Capsule + Tỷ lệ %
            Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ProgressBarHeight)
                        .clip(CircleShape)
                        .background(CandyColors.budgetSnapshotProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(progressBarFillColor)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.budget_summary_label_spent)}: ${snapshot.spentFormatted}",
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CandyColors.textSecondary
                    )

                    Text(
                        text = "${snapshot.percentageInt}%",
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOver) CandyColors.overBudget else CandyColors.brandPurple
                    )
                }
            }

            // Row 4: Ngân sách tổng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CandyColors.budgetSnapshotSpendingPanel)
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.budget_summary_label_limit),
                    fontFamily = Nunito,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CandyColors.textSecondary
                )
                Text(
                    text = snapshot.budgetFormatted,
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )
            }
        }
    }
}
