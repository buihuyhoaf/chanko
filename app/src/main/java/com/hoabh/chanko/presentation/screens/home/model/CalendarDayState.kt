package com.hoabh.chanko.presentation.screens.home.model

import java.time.LocalDate

data class CalendarDayState(
    val date: LocalDate,
    val isCycleStart: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val purchaseCount: Int = 0,
    val totalSpent: Long = 0L
) {
    val dayNumber: Int
        get() = date.dayOfMonth

    val hasPurchases: Boolean
        get() = purchaseCount > 0
}
