package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.model.BudgetSummary
import com.hoabh.chanko.presentation.screens.home.model.CalendarDayState
import com.hoabh.chanko.presentation.screens.home.model.DailyPurchaseSummary
import com.hoabh.chanko.presentation.screens.home.model.MascotAdvice
import com.hoabh.chanko.presentation.screens.home.model.SpendingCycle
import com.hoabh.chanko.presentation.screens.home.model.toVndCurrency
import com.hoabh.chanko.ui.theme.CandyColors
import java.time.LocalDate

@Composable
fun SpendingCalendarCard(
    cycle: SpendingCycle,
    days: List<CalendarDayState>,
    selectedDate: LocalDate,
    selectedTransactions: DailyPurchaseSummary?,
    budgetSummary: BudgetSummary,
    mascotAdvice: MascotAdvice,
    onDateClick: (LocalDate) -> Unit,
    onTodayClick: () -> Unit,
    onDayDetailClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CandyColors.calendarSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Range + Button "Hôm nay"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_calendar_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(800)
                        ),
                        color = CandyColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val rangeText = "${cycle.startDate.dayOfMonth}/${cycle.startDate.monthValue} – ${cycle.endDate.dayOfMonth}/${cycle.endDate.monthValue}/${cycle.endDate.year}"
                    Text(
                        text = rangeText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight(500)
                        ),
                        color = CandyColors.textSecondary
                    )
                }

                Button(
                    onClick = onTodayClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.primarySoft,
                        contentColor = CandyColors.primaryInk
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_btn_today),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight(800)
                        )
                    )
                }
            }

            // Month subheader: e.g. "Tháng 10 – 11/2026"
            val monthTitle = if (cycle.startDate.monthValue != cycle.endDate.monthValue) {
                stringResource(
                    R.string.home_calendar_month_format,
                    cycle.startDate.monthValue,
                    cycle.endDate.monthValue,
                    cycle.endDate.year
                )
            } else {
                stringResource(
                    R.string.home_calendar_month_single_format,
                    cycle.startDate.monthValue,
                    cycle.startDate.year
                )
            }
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight(800)
                ),
                color = CandyColors.textPrimary
            )

            // Cycle Calendar Grid (7 columns starting on cycle start weekday)
            CycleCalendarGrid(
                weekdayHeaders = cycle.weekdayHeaders,
                days = days,
                onDateClick = onDateClick
            )

            // Selected Date Detail Row
            val detailCd = stringResource(
                R.string.home_cd_open_day_transactions,
                selectedDate.dayOfMonth,
                selectedDate.monthValue
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = { onDayDetailClick(selectedDate) }
                    )
                    .semantics { contentDescription = detailCd }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val purchaseCount = selectedTransactions?.purchaseCount ?: 0
                    val subTitle = if (purchaseCount > 0) {
                        stringResource(
                            R.string.home_selected_day_detail,
                            selectedDate.dayOfMonth,
                            selectedDate.monthValue,
                            purchaseCount
                        )
                    } else {
                        stringResource(
                            R.string.home_selected_day_no_purchase,
                            selectedDate.dayOfMonth,
                            selectedDate.monthValue
                        )
                    }
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight(600)
                        ),
                        color = CandyColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val amountStr = (selectedTransactions?.totalAmount ?: 0L).toVndCurrency()
                    Text(
                        text = amountStr,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(800)
                        ),
                        color = CandyColors.textPrimary
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CandyColors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(
                color = CandyColors.divider,
                thickness = 1.dp
            )

            // Mascot with dynamic speech bubble
            MascotSpeechBanner(
                advice = mascotAdvice
            )

            HorizontalDivider(
                color = CandyColors.divider,
                thickness = 1.dp
            )

            // Budget Summary 3 columns + progress bar + renewal
            BudgetSummarySection(
                budgetSummary = budgetSummary,
                nextCycleStartDate = cycle.nextCycleStartDate
            )
        }
    }
}
