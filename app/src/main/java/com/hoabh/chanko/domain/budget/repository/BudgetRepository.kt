package com.hoabh.chanko.domain.budget.repository

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetCycleOverview
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import kotlinx.coroutines.flow.Flow

/**
 * Hợp đồng dữ liệu cho phân hệ Ngân sách (Domain Layer).
 *
 * Quy ước lỗi: các hàm ghi trả [Result.failure] với một
 * [com.hoabh.chanko.domain.budget.model.BudgetException] có kiểu.
 * Các hàm ghi tự kiểm tra lại toàn bộ invariant (online, current cycle, khoảng tiền,
 * tên thẻ) — không phụ thuộc vào validation của form.
 */
interface BudgetRepository {
    fun getAvailableCycles(): Flow<List<BudgetCycle>>

    /**
     * Toàn bộ dữ liệu Overview của một chu kỳ, phát ra nguyên tử.
     * Ném [com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException] khi offline
     * và chu kỳ chưa có cache; [com.hoabh.chanko.domain.budget.model.BudgetLoadException] khi lỗi tải.
     */
    fun getCycleOverview(cycleId: String): Flow<BudgetCycleOverview>

    /** Tập ID chu kỳ đã có cache để xem ngoại tuyến (C02 offline). */
    fun getOfflineCachedCycleIds(): Flow<Set<String>>

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
