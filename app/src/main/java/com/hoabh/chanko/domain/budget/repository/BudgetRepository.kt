package com.hoabh.chanko.domain.budget.repository

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import kotlinx.coroutines.flow.Flow

/**
 * Hợp đồng dữ liệu cho phân hệ Ngân sách (Domain Layer).
 */
interface BudgetRepository {
    fun getAvailableCycles(): Flow<List<BudgetCycle>>
    fun getBudgetSnapshot(cycleId: String): Flow<BudgetSnapshot>
    fun getPaymentBreakdown(cycleId: String): Flow<List<PaymentMethodBreakdown>>
    fun getNotableItems(cycleId: String): Flow<List<NotableItem>>
    fun getRecentActivities(cycleId: String): Flow<List<BudgetActivity>>
    fun getAdjustments(cycleId: String): Flow<List<BudgetAdjustment>>
    fun getCreditCards(): Flow<List<CreditCard>>

    fun isOnlineFlow(): Flow<Boolean>

    suspend fun updateBudgetLimit(cycleId: String, newLimitMinor: Long, expectedRevision: Int): Result<Unit>
    suspend fun addCreditCard(name: String): Result<CreditCard>
    suspend fun renameCreditCard(cardId: String, newName: String): Result<Unit>
    suspend fun archiveCreditCard(cardId: String): Result<Unit>
    suspend fun setNetworkOnline(isOnline: Boolean)
}
