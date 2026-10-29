package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.CardNameValidationResult
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeBudgetRepository @Inject constructor() : BudgetRepository {

    // --- State Storage ---
    private val isOnlineState = MutableStateFlow(true)
    private val isOfflineUncachedState = MutableStateFlow(false)

    var simulateConflictOnNextUpdate: Boolean = false

    // Cycles
    val currentCycleId = "cycle_2026_10"
    val pastCycleId = "cycle_2026_09"

    private val cyclesState = MutableStateFlow(
        listOf(
            BudgetCycle(
                id = currentCycleId,
                label = "Tháng 10/2026",
                startDate = "01/10/2026",
                endDate = "31/10/2026",
                isCurrent = true,
                anchorDay = 1
            ),
            BudgetCycle(
                id = pastCycleId,
                label = "Tháng 09/2026",
                startDate = "01/09/2026",
                endDate = "30/09/2026",
                isCurrent = false,
                anchorDay = 1
            )
        )
    )

    // Snapshots map (cycleId -> BudgetSnapshot)
    private val snapshotsState = MutableStateFlow<Map<String, BudgetSnapshot>>(
        mapOf(
            currentCycleId to BudgetSnapshot(
                cycleId = currentCycleId,
                budgetMinor = 5_000_000L,
                spentMinor = 3_000_000L,
                revision = 1
            ),
            pastCycleId to BudgetSnapshot(
                cycleId = pastCycleId,
                budgetMinor = 4_500_000L,
                spentMinor = 4_200_000L,
                revision = 1
            )
        )
    )

    // Payment breakdowns (cycleId -> List<PaymentMethodBreakdown>)
    private val paymentBreakdownState = MutableStateFlow<Map<String, List<PaymentMethodBreakdown>>>(
        mapOf(
            currentCycleId to listOf(
                PaymentMethodBreakdown("pm_1", "Thẻ Techcombank", 1_800_000L),
                PaymentMethodBreakdown("pm_2", "Tiền mặt", 1_200_000L)
            ),
            pastCycleId to listOf(
                PaymentMethodBreakdown("pm_1", "Thẻ Techcombank", 3_000_000L),
                PaymentMethodBreakdown("pm_2", "Tiền mặt", 1_200_000L)
            )
        )
    )

    // Notable items (cycleId -> List<NotableItem>)
    private val notableItemsState = MutableStateFlow<Map<String, List<NotableItem>>>(
        mapOf(
            currentCycleId to listOf(
                NotableItem("item_1", "Tai nghe chống ồn Sony", 3_500_000L),
                NotableItem("item_2", "Bình giữ nhiệt Candy Pop", 350_000L),
                NotableItem("item_3", "Sách Tư Duy Nhanh Và Chậm", 180_000L)
            ),
            pastCycleId to emptyList()
        )
    )

    // Activities (cycleId -> List<BudgetActivity>)
    private val activitiesState = MutableStateFlow<Map<String, List<BudgetActivity>>>(
        mapOf(
            currentCycleId to listOf(
                BudgetActivity("act_1", currentCycleId, "Mua tai nghe Sony", -3_500_000L, "Hôm nay, 10:15", "item_1", BudgetActivityType.PURCHASE_RECORDED),
                BudgetActivity("act_2", currentCycleId, "Hoàn tiền đơn hàng cũ", 500_000L, "Hôm qua, 16:30", null, BudgetActivityType.EXPENSE_CORRECTED),
                BudgetActivity("act_3", currentCycleId, "Tăng hạn mức chi tiêu", 1_000_000L, "25/10/2026", null, BudgetActivityType.BUDGET_ADJUSTED)
            ),
            pastCycleId to listOf(
                BudgetActivity("act_past_1", pastCycleId, "Chi tiêu ăn uống", -1_200_000L, "28/09/2026", null, BudgetActivityType.PURCHASE_RECORDED)
            )
        )
    )

    // Adjustments (cycleId -> List<BudgetAdjustment>)
    private val adjustmentsState = MutableStateFlow<Map<String, List<BudgetAdjustment>>>(
        mapOf(
            currentCycleId to listOf(
                BudgetAdjustment("adj_1", currentCycleId, 4_000_000L, 5_000_000L, "25/10/2026, 09:00")
            ),
            pastCycleId to emptyList()
        )
    )

    // Credit cards
    private val cardsState = MutableStateFlow(
        listOf(
            CreditCard("card_1", "Thẻ Techcombank", isArchived = false),
            CreditCard("card_2", "Thẻ VPBank Cake", isArchived = false)
        )
    )

    // --- Scenario Switchers for Testing & Preview ---
    fun setScenarioOverBudget() {
        val current = snapshotsState.value[currentCycleId] ?: return
        snapshotsState.value = snapshotsState.value + (currentCycleId to current.copy(
            budgetMinor = 5_000_000L,
            spentMinor = 5_500_000L
        ))
    }

    fun setScenarioZeroSpend() {
        val current = snapshotsState.value[currentCycleId] ?: return
        snapshotsState.value = snapshotsState.value + (currentCycleId to current.copy(
            budgetMinor = 5_000_000L,
            spentMinor = 0L
        ))
        paymentBreakdownState.value = paymentBreakdownState.value + (currentCycleId to emptyList())
        activitiesState.value = activitiesState.value + (currentCycleId to emptyList())
    }

    fun setScenarioNormalWithin() {
        val current = snapshotsState.value[currentCycleId] ?: return
        snapshotsState.value = snapshotsState.value + (currentCycleId to current.copy(
            budgetMinor = 5_000_000L,
            spentMinor = 3_000_000L
        ))
    }

    fun setOfflineUncached(uncached: Boolean) {
        isOfflineUncachedState.value = uncached
    }

    // --- BudgetRepository Implementation ---
    override fun isOnlineFlow(): Flow<Boolean> = isOnlineState.asStateFlow()

    override suspend fun setNetworkOnline(isOnline: Boolean) {
        isOnlineState.value = isOnline
    }

    override fun getAvailableCycles(): Flow<List<BudgetCycle>> {
        return cyclesState.asStateFlow()
    }

    override fun getBudgetSnapshot(cycleId: String): Flow<BudgetSnapshot> {
        return snapshotsState.map { map ->
            if (!isOnlineState.value && isOfflineUncachedState.value) {
                throw IllegalStateException("Chưa có dữ liệu ngoại tuyến")
            }
            map[cycleId] ?: BudgetSnapshot(cycleId, 0L, 0L)
        }
    }

    override fun getPaymentBreakdown(cycleId: String): Flow<List<PaymentMethodBreakdown>> {
        return paymentBreakdownState.map { map ->
            map[cycleId] ?: emptyList()
        }
    }

    override fun getNotableItems(cycleId: String): Flow<List<NotableItem>> {
        return notableItemsState.map { map ->
            map[cycleId] ?: emptyList()
        }
    }

    override fun getRecentActivities(cycleId: String): Flow<List<BudgetActivity>> {
        return activitiesState.map { map ->
            (map[cycleId] ?: emptyList()).take(3)
        }
    }

    override fun getAdjustments(cycleId: String): Flow<List<BudgetAdjustment>> {
        return adjustmentsState.map { map ->
            map[cycleId] ?: emptyList()
        }
    }

    override fun getCreditCards(): Flow<List<CreditCard>> {
        return cardsState.map { list ->
            list.filter { !it.isArchived }
        }
    }

    override suspend fun updateBudgetLimit(
        cycleId: String,
        newLimitMinor: Long,
        expectedRevision: Int
    ): Result<Unit> {
        if (!isOnlineState.value) {
            return Result.failure(IllegalStateException("Kết nối mạng để chỉnh sửa"))
        }

        val cycle = cyclesState.value.firstOrNull { it.id == cycleId }
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy chu kỳ"))

        if (!cycle.isCurrent) {
            return Result.failure(IllegalStateException("Chỉ chỉnh sửa trong chu kỳ hiện tại"))
        }

        if (simulateConflictOnNextUpdate) {
            simulateConflictOnNextUpdate = false
            return Result.failure(IllegalStateException("Ngân sách đã thay đổi. Tải lại để chỉnh sửa."))
        }

        val currentSnap = snapshotsState.value[cycleId]
            ?: return Result.failure(IllegalStateException("Không tìm thấy dữ liệu ngân sách"))

        if (currentSnap.revision != expectedRevision) {
            return Result.failure(IllegalStateException("Ngân sách đã thay đổi. Tải lại để chỉnh sửa."))
        }

        val updatedSnap = currentSnap.copy(
            budgetMinor = newLimitMinor,
            revision = currentSnap.revision + 1,
            updatedAt = System.currentTimeMillis()
        )
        snapshotsState.value = snapshotsState.value + (cycleId to updatedSnap)

        // Ghi lại sự kiện điều chỉnh lịch sử (Adjustment)
        val newAdjustment = BudgetAdjustment(
            id = "adj_${UUID.randomUUID()}",
            cycleId = cycleId,
            oldLimitMinor = currentSnap.budgetMinor,
            newLimitMinor = newLimitMinor,
            adjustedAt = "Vừa xong"
        )
        val currentAdjustments = adjustmentsState.value[cycleId] ?: emptyList()
        adjustmentsState.value = adjustmentsState.value + (cycleId to (listOf(newAdjustment) + currentAdjustments))

        // Ghi thêm một sự kiện biến động vào Recent Activities
        val newActivity = BudgetActivity(
            id = "act_${UUID.randomUUID()}",
            cycleId = cycleId,
            title = if (newLimitMinor >= currentSnap.budgetMinor) "Tăng hạn mức ngân sách" else "Giảm hạn mức ngân sách",
            amountChangeMinor = newLimitMinor - currentSnap.budgetMinor,
            occurredAt = "Vừa xong",
            type = BudgetActivityType.BUDGET_ADJUSTED
        )
        val currentActivities = activitiesState.value[cycleId] ?: emptyList()
        activitiesState.value = activitiesState.value + (cycleId to (listOf(newActivity) + currentActivities))

        return Result.success(Unit)
    }

    override suspend fun addCreditCard(name: String): Result<CreditCard> {
        if (!isOnlineState.value) {
            return Result.failure(IllegalStateException("Kết nối mạng để chỉnh sửa"))
        }

        val validation = CreditCard.validateName(name)
        if (validation !is CardNameValidationResult.Valid) {
            return Result.failure(IllegalArgumentException("Tên thẻ không hợp lệ"))
        }

        val normalized = validation.normalizedName
        val activeCards = cardsState.value.filter { !it.isArchived }
        if (activeCards.any { it.name.equals(normalized, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Tên thẻ đã tồn tại"))
        }

        val newCard = CreditCard(
            id = "card_${UUID.randomUUID()}",
            name = normalized,
            isArchived = false
        )
        cardsState.value = cardsState.value + newCard
        return Result.success(newCard)
    }

    override suspend fun renameCreditCard(cardId: String, newName: String): Result<Unit> {
        if (!isOnlineState.value) {
            return Result.failure(IllegalStateException("Kết nối mạng để chỉnh sửa"))
        }

        val target = cardsState.value.firstOrNull { it.id == cardId && !it.isArchived }
            ?: return Result.failure(IllegalStateException("Thẻ không còn khả dụng"))

        val validation = CreditCard.validateName(newName)
        if (validation !is CardNameValidationResult.Valid) {
            return Result.failure(IllegalArgumentException("Tên thẻ không hợp lệ"))
        }

        val normalized = validation.normalizedName
        val activeCards = cardsState.value.filter { !it.isArchived && it.id != cardId }
        if (activeCards.any { it.name.equals(normalized, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Tên thẻ đã tồn tại"))
        }

        cardsState.value = cardsState.value.map {
            if (it.id == cardId) it.copy(name = normalized) else it
        }
        return Result.success(Unit)
    }

    override suspend fun archiveCreditCard(cardId: String): Result<Unit> {
        if (!isOnlineState.value) {
            return Result.failure(IllegalStateException("Kết nối mạng để chỉnh sửa"))
        }

        val exists = cardsState.value.any { it.id == cardId && !it.isArchived }
        if (!exists) {
            return Result.failure(IllegalStateException("Thẻ đã được xóa"))
        }

        cardsState.value = cardsState.value.map {
            if (it.id == cardId) it.copy(isArchived = true) else it
        }
        return Result.success(Unit)
    }
}
