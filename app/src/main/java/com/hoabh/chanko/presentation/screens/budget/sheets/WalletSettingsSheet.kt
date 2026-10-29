package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun WalletSettingsSheet(
    selectedCycle: BudgetCycle?,
    currentSnapshot: BudgetSnapshot?,
    canWrite: Boolean,
    onEditBudgetClick: () -> Unit,
    onManageCardsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.budget_wallet_settings_title),
                fontFamily = Nunito,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Text(
                text = stringResource(R.string.budget_btn_close),
                fontFamily = Nunito,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.brandPurple,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onClose)
                    .padding(CandySpace.xs)
            )
        }

        // Info Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CandyColors.primarySoft)
                .padding(CandySpace.md),
            verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
        ) {
            Text(
                text = selectedCycle?.dateRangeText ?: "",
                fontFamily = Nunito,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = CandyColors.textSecondary
            )
            Text(
                text = "${stringResource(R.string.budget_wallet_settings_current_budget)}: ${currentSnapshot?.budgetFormatted ?: stringResource(R.string.budget_wallet_no_data)}",
                fontFamily = Nunito,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Text(
                text = stringResource(R.string.budget_wallet_settings_anchor_format, selectedCycle?.anchorDay ?: 1),
                fontFamily = Nunito,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = CandyColors.textSecondary
            )
        }

        // Action Rows
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            // Row 1: Sửa ngân sách
            SettingsActionRow(
                iconId = ChankoIconId.ACT_05,
                title = stringResource(R.string.budget_wallet_action_edit_budget),
                enabled = canWrite,
                disabledReason = if (!canWrite) stringResource(R.string.budget_wallet_disabled_past_reason) else null,
                onClick = onEditBudgetClick
            )

            // Row 2: Quản lý thẻ tín dụng
            SettingsActionRow(
                iconId = ChankoIconId.BUD_01,
                title = stringResource(R.string.budget_wallet_action_manage_cards),
                enabled = canWrite,
                disabledReason = if (!canWrite) stringResource(R.string.budget_wallet_disabled_past_reason) else null,
                onClick = onManageCardsClick
            )

            // Row 3: Lịch sử điều chỉnh ngân sách (Luôn mở được để đọc)
            SettingsActionRow(
                iconId = ChankoIconId.BUD_04,
                title = stringResource(R.string.budget_wallet_action_history),
                enabled = true,
                disabledReason = null,
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    iconId: ChankoIconId,
    title: String,
    enabled: Boolean,
    disabledReason: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (enabled) CandyColors.budgetSnapshotSpendingPanel else CandyColors.budgetSnapshotProgressTrack,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (enabled) title else "$title ($disabledReason)"
            }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CandySpace.md),
                modifier = Modifier.weight(1f)
            ) {
                ChankoIcon(
                    id = iconId,
                    contentDescription = null,
                    enabled = enabled,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontFamily = Nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) CandyColors.textPrimary else CandyColors.textSecondary
                    )
                    if (disabledReason != null) {
                        Text(
                            text = disabledReason,
                            fontFamily = Nunito,
                            fontSize = 11.sp,
                            color = CandyColors.textSecondary
                        )
                    }
                }
            }

            Text(
                text = "→",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) CandyColors.brandPurple else CandyColors.textSecondary
            )
        }
    }
}
