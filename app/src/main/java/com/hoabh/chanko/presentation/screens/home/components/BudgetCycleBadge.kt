package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.BudgetCardRole

private val BadgeCornerShape = RoundedCornerShape(999.dp)

/**
 * Huy hiệu trạng thái chu kỳ trên thẻ ngân sách (Home v2.2 - BC22-04, BC22-07, BC22-10).
 */
@Composable
fun BudgetCycleBadge(
    role: BudgetCardRole,
    modifier: Modifier = Modifier,
    cycleLabel: String? = null
) {
    val (labelRes, containerColor, textColor) = when (role) {
        BudgetCardRole.Previous -> Triple(
            R.string.home_budget_status_closed,
            Color.White.copy(alpha = 0.25f),
            Color.White
        )
        BudgetCardRole.Current -> Triple(
            R.string.home_budget_status_active,
            Color.White.copy(alpha = 0.35f),
            Color.White
        )
        BudgetCardRole.NextConfigured -> Triple(
            R.string.home_budget_status_upcoming,
            Color.White.copy(alpha = 0.25f),
            Color.White
        )
        BudgetCardRole.NextPlaceholder -> Triple(
            R.string.home_budget_status_upcoming,
            Color.Black.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val displayText = if (!cycleLabel.isNullOrBlank()) {
        "$cycleLabel · ${stringResource(labelRes)}"
    } else {
        stringResource(labelRes)
    }

    Box(
        modifier = modifier
            .clip(BadgeCornerShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor,
            maxLines = 1
        )
    }
}
