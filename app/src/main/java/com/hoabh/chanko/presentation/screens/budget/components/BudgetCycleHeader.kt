package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun BudgetCycleHeader(
    cycleRangeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val a11yDesc = stringResource(R.string.budget_cycle_picker_title) + ": " + cycleRangeText

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CandyColors.budgetSnapshotSpendingPanel,
        border = BorderStroke(1.5.dp, CandyColors.budgetSnapshotBorder),
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Button
                contentDescription = a11yDesc
            }
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            ChankoIcon(
                id = ChankoIconId.BUD_05,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = cycleRangeText,
                fontFamily = Nunito,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Text(
                text = "▾",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textSecondary
            )
        }
    }
}
