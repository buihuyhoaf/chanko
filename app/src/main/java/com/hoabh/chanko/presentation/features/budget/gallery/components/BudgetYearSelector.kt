package com.hoabh.chanko.presentation.features.budget.gallery.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Bộ chọn năm (BudgetYearSelector) cho Budget Gallery:
 * - Nút lùi năm (Previous Year)
 * - Hiển thị "Năm YYYY"
 * - Nút tiến năm (Next Year)
 * - Min touch target 48dp cho các nút bấm
 */
@Composable
fun BudgetYearSelector(
    selectedYear: Int,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút lùi năm
        Box(
            modifier = Modifier
                .size(CandyBudgetThemes.touchTargetMin)
                .clip(CircleShape)
                .clickable(
                    enabled = canNavigatePrevious,
                    role = Role.Button,
                    onClick = onPreviousYear
                )
                .semantics {
                    contentDescription = "Xem năm trước"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            ChankoIcon(
                id = ChankoIconId.NAV_03,
                contentDescription = null,
                enabled = canNavigatePrevious,
                size = 20.dp
            )
        }

        // Nhãn Năm
        Text(
            text = "Năm $selectedYear",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = CandyColors.textPrimary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Nút tiến năm
        Box(
            modifier = Modifier
                .size(CandyBudgetThemes.touchTargetMin)
                .clip(CircleShape)
                .clickable(
                    enabled = canNavigateNext,
                    role = Role.Button,
                    onClick = onNextYear
                )
                .semantics {
                    contentDescription = "Xem năm kế tiếp"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            ChankoIcon(
                id = ChankoIconId.ACT_18,
                contentDescription = null,
                enabled = canNavigateNext,
                size = 20.dp
            )
        }
    }
}
