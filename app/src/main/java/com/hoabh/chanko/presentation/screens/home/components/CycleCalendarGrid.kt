package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.model.CalendarDayState
import com.hoabh.chanko.ui.theme.CandyColors
import java.time.LocalDate

@Composable
fun CycleCalendarGrid(
    weekdayHeaders: List<String>,
    days: List<CalendarDayState>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Weekday header row (starts dynamically from cycle start day)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdayHeaders.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(600)
                    ),
                    color = CandyColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Grid rows chunked into 7 columns
        val rows = days.chunked(7)
        rows.forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 7) {
                    if (i < rowDays.size) {
                        val day = rowDays[i]
                        CalendarDayCell(
                            day = day,
                            onClick = { onDateClick(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Legend row
        CalendarLegendRow()
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(12.dp)
    val cd = buildString {
        append(day.dayNumber)
        if (day.isToday) append(", hôm nay")
        if (day.isCycleStart) append(", bắt đầu kỳ")
        if (day.isSelected) append(", đã chọn")
        if (day.hasPurchases) append(", ${day.purchaseCount} giao dịch")
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(46.dp)
            .clip(cellShape)
            .then(
                when {
                    day.isToday -> Modifier.background(CandyColors.brandPurple)
                    else -> Modifier
                }
            )
            .then(
                when {
                    day.isSelected -> Modifier.border(BorderStroke(2.dp, CandyColors.brandPink), cellShape)
                    day.isCycleStart -> Modifier.border(BorderStroke(1.5.dp, CandyColors.brandPurple), cellShape)
                    else -> Modifier
                }
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = cd
                this.selected = day.isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (day.isToday || day.isSelected) FontWeight(800) else FontWeight(600)
                ),
                color = if (day.isToday) Color.White else CandyColors.textPrimary
            )

            // Purchase dot indicator
            if (day.hasPurchases) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(CandyColors.brandPink)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CalendarLegendRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start cycle
        Box(
            modifier = Modifier
                .size(10.dp)
                .border(BorderStroke(1.5.dp, CandyColors.brandPurple), CircleShape)
        )
        Text(
            text = stringResource(R.string.home_legend_start),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight(600)
            ),
            color = CandyColors.textSecondary,
            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
        )

        // Today
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(CandyColors.brandPurple)
        )
        Text(
            text = stringResource(R.string.home_legend_today),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight(600)
            ),
            color = CandyColors.textSecondary,
            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
        )

        // Has purchases
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(CandyColors.brandPink)
        )
        Text(
            text = stringResource(R.string.home_legend_purchases),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight(600)
            ),
            color = CandyColors.textSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
