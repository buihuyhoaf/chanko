package com.hoabh.chanko.presentation.screens.home.data

import com.hoabh.chanko.presentation.screens.home.model.BudgetSummary
import com.hoabh.chanko.presentation.screens.home.model.DailyPurchaseSummary
import com.hoabh.chanko.presentation.screens.home.model.SpendingCycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface HomeSpendingRepository {
    fun getCycle(anchorDay: Int, referenceDate: LocalDate): Flow<SpendingCycle>
    fun getDailyPurchases(cycle: SpendingCycle): Flow<Map<LocalDate, DailyPurchaseSummary>>
    fun getBudgetSummary(cycle: SpendingCycle): Flow<BudgetSummary>
}

@Singleton
class InMemoryHomeSpendingRepository @Inject constructor() : HomeSpendingRepository {

    override fun getCycle(anchorDay: Int, referenceDate: LocalDate): Flow<SpendingCycle> = flowOf(
        SpendingCycle.create(anchorDay = anchorDay, referenceDate = referenceDate)
    )

    override fun getDailyPurchases(cycle: SpendingCycle): Flow<Map<LocalDate, DailyPurchaseSummary>> {
        val map = mutableMapOf<LocalDate, DailyPurchaseSummary>()

        // Default mock sample data matching UI-HANDOFF v5 (cycle starting 15/10/2026)
        val day16 = cycle.startDate.plusDays(1)
        val day18 = cycle.startDate.plusDays(3)

        if (!day16.isAfter(cycle.endDate)) {
            map[day16] = DailyPurchaseSummary(purchaseCount = 1, totalAmount = 1_000_000L)
        }
        if (!day18.isAfter(cycle.endDate)) {
            map[day18] = DailyPurchaseSummary(purchaseCount = 1, totalAmount = 1_400_000L)
        }

        return flowOf(map)
    }

    override fun getBudgetSummary(cycle: SpendingCycle): Flow<BudgetSummary> = flowOf(
        BudgetSummary(
            totalBudget = 3_000_000L,
            totalSpent = 2_400_000L
        )
    )
}
