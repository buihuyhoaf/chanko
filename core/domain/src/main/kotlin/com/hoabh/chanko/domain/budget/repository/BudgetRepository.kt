package com.hoabh.chanko.domain.budget.repository

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetCycleOverview
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.model.PaymentType
import kotlinx.coroutines.flow.Flow

/**
 * Hợp đồng dữ liệu cho phân hệ Ngân sách (Domain Layer).
 *
 * Quy ước lỗi: các hàm ghi trả [Result.failure] với một
 * [com.hoabh.chanko.domain.budget.model.BudgetException] có kiểu.
 * Các hàm ghi tự kiểm tra lại toàn bộ invariant (online, current cycle, khoảng tiền)
 * — không phụ thuộc vào validation của form.
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
    fun getNotableItems(cycleId: String): Flow<List<NotableItem>>
    fun getRecentActivities(cycleId: String): Flow<List<BudgetActivity>>
    fun getAdjustments(cycleId: String): Flow<List<BudgetAdjustment>>

    fun isOnlineFlow(): Flow<Boolean>

    /**
     * Quan sát cửa sổ 3 chu kỳ đại diện cho Carousel trên màn Home (Home v2.2 - BC22-01..BC22-04).
     */
    fun observeTriCycleWindow(): Flow<com.hoabh.chanko.domain.budget.model.TriCycleWindow>

    /**
     * Thiết lập hạn mức dự kiến cho chu kỳ tiếp theo (BC22-07).
     * Yêu cầu kết nối mạng; trả về Result.failure(BudgetOfflineException) nếu đang ngoại tuyến.
     */
    suspend fun setupNextCycleBudget(limitMinor: Long): Result<Unit>

    /**
     * Kích hoạt chuyển chu kỳ (Rollover) sang kỳ mới (BC22-19, BC22-20).
     */
    suspend fun triggerRollover(): Result<Unit>

    suspend fun updateBudgetLimit(cycleId: String, newLimitMinor: Long, expectedRevision: Int): Result<Unit>
    suspend fun setNetworkOnline(isOnline: Boolean)

    /**
     * Ghi nhận chi tiêu từ sản phẩm vào chu kỳ ngân sách hiện tại.
     */
    suspend fun recordItemExpense(
        itemId: String,
        itemName: String,
        amountVnd: Long,
        paymentType: PaymentType
    ): Result<Unit>

    /**
     * Hoàn tiền / khôi phục hạn mức chi tiêu trong chu kỳ hiện tại.
     */
    suspend fun refundExpense(
        amountVnd: Long
    ): Result<Unit>
}
