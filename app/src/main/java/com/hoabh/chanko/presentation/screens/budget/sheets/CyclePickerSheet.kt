package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun CyclePickerSheet(
    availableCycles: List<BudgetCycle>,
    selectedCycleId: String,
    isOnline: Boolean,
    offlineCachedCycleIds: Set<String> = emptySet(),
    onCycleSelect: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.budget_cycle_picker_title),
                fontFamily = Nunito,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .widthIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onClose)
                    .padding(horizontal = CandySpace.xs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_btn_close),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.brandPurple
                )
            }
        }

        if (availableCycles.isEmpty()) {
            Text(
                text = stringResource(R.string.budget_cycle_empty),
                fontFamily = Nunito,
                fontSize = 14.sp,
                color = CandyColors.textSecondary,
                modifier = Modifier.padding(vertical = CandySpace.lg)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                items(availableCycles, key = { it.id }) { cycle ->
                    val isSelected = cycle.id == selectedCycleId
                    val isCachedOffline = offlineCachedCycleIds.isEmpty() || cycle.id in offlineCachedCycleIds
                    val isSelectable = isOnline || isCachedOffline
                    val uncachedReason = stringResource(R.string.budget_cycle_offline_uncached)

                    val tagText = if (cycle.isCurrent) {
                        stringResource(R.string.budget_cycle_current_tag)
                    } else {
                        stringResource(R.string.budget_cycle_past_tag)
                    }

                    val a11yText = buildString {
                        append("${cycle.dateRangeText}, $tagText")
                        if (isSelected) append(", đang chọn")
                        if (!isSelectable) append(", $uncachedReason")
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isSelected -> CandyColors.primarySoft
                            !isSelectable -> CandyColors.budgetSnapshotProgressTrack
                            else -> CandyColors.budgetSnapshotSpendingPanel
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .semantics {
                                role = Role.RadioButton
                                contentDescription = a11yText
                            }
                            .clickable(
                                enabled = isSelectable,
                                role = Role.RadioButton
                            ) {
                                onCycleSelect(cycle.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cycle.dateRangeText,
                                    fontFamily = Nunito,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelectable) CandyColors.textPrimary else CandyColors.textSecondary
                                )
                                Text(
                                    text = if (isSelectable) tagText else "$tagText · $uncachedReason",
                                    fontFamily = Nunito,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (cycle.isCurrent && isSelectable) CandyColors.brandPurple else CandyColors.textSecondary
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                enabled = isSelectable,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CandyColors.brandPurple,
                                    unselectedColor = CandyColors.textSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
