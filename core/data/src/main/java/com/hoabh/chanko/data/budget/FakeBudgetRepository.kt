package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetCalculation
import com.hoabh.chanko.domain.budget.model.BudgetConflictException
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetCycleOverview
import com.hoabh.chanko.domain.budget.model.BudgetCycleStatus
import com.hoabh.chanko.domain.budget.model.BudgetLoadException
import com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException
import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.model.BudgetPastCycleException
import com.hoabh.chanko.domain.budget.model.BudgetSaveException
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.InvalidBudgetAmountException
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.TriCycleWindow
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    var simulateSaveErrorOnNextMutation: Boolean = false
    var simulateLoadErrorOnNextFetch: Boolean = false
    var simulatedDelayMs: Long = 0L

    // Cycles
    val currentCycleId = "cycle_2026_10"
    val pastCycleId = "cycle_2026_09"
    val pastUncachedCycleId = "cycle_2026_08"
    val nextCycleId = "cycle_2026_11"

    // Default data timestamp (fixed base or current time when data was last synced)
    private val initialDataTimestamp = 1_761_708_600_000L // 2025-10-29 10:30

    private val cachedCycleIdsState = MutableStateFlow(
        setOf(currentCycleId, pastCycleId)
    )

    private val cyclesState = MutableStateFlow(
        listOf(
            BudgetCycle(
                id = currentCycleId,
                label = "Tháng 10/2026",
                startDate = "01/10/2026",
                endDate = "31/10/2026",
                isCurrent = true,
                anchorDay = 1,
                ordinal = 0,
                status = BudgetCycleStatus.Active
            ),
            BudgetCycle(
                id = pastCycleId,
                label = "Tháng 09/2026",
                startDate = "01/09/2026",
                endDate = "30/09/2026",
                isCurrent = false,
                anchorDay = 1,
                ordinal = -1,
                status = BudgetCycleStatus.Closed
            ),
            BudgetCycle(
                id = pastUncachedCycleId,
                label = "Tháng 08/2026",
                startDate = "01/08/2026",
                endDate = "31/08/2026",
                isCurrent = false,
                anchorDay = 1,
                ordinal = -2,
                status = BudgetCycleStatus.Closed
            ),
            BudgetCycle(
                id = nextCycleId,
                label = "Tháng 11/2026",
                startDate = "01/11/2026",
                endDate = "30/11/2026",
                isCurrent = false,
                anchorDay = 1,
                ordinal = 1,
                status = BudgetCycleStatus.UpcomingUnconfigured
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
                revision = 1,
                updatedAt = initialDataTimestamp
            ),
            pastCycleId to BudgetSnapshot(
                cycleId = pastCycleId,
                budgetMinor = 4_500_000L,
                spentMinor = 4_200_000L,
                revision = 1,
                updatedAt = initialDataTimestamp - 86_400_000L
            ),
            pastUncachedCycleId to BudgetSnapshot(
                cycleId = pastUncachedCycleId,
                budgetMinor = 4_000_000L,
                spentMinor = 3_800_000L,
                revision = 1,
                updatedAt = initialDataTimestamp - 2 * 86_400_000L
            )
        )
    )

    // Notable items (cycleId -> List<NotableItem>)
    private val notableItemsState = MutableStateFlow<Map<String, List<NotableItem>>>(
        mapOf(
            currentCycleId to listOf(
                NotableItem("1", "Tai nghe chống ồn Sony", 3_500_000L),
                NotableItem("2", "Bình giữ nhiệt Candy Pop", 350_000L),
                NotableItem("3", "Sách Tư Duy Nhanh Và Chậm", 180_000L)
            ),
            pastCycleId to emptyList(),
            pastUncachedCycleId to emptyList()
        )
    )

    // Activities (cycleId -> List<BudgetActivity>)
    private val activitiesState = MutableStateFlow<Map<String, List<BudgetActivity>>>(
        mapOf(
            currentCycleId to listOf(
                BudgetActivity("act_1", currentCycleId, "Mua tai nghe Sony", -3_500_000L, "Hôm nay, 10:15", "1", BudgetActivityType.PURCHASE_RECORDED),
                BudgetActivity("act_2", currentCycleId, "Hoàn tiền đơn hàng cũ", 500_000L, "Hôm qua, 16:30", null, BudgetActivityType.EXPENSE_CORRECTED),
                BudgetActivity("act_3", currentCycleId, "Tăng hạn mức chi tiêu", 1_000_000L, "25/10/2026", null, BudgetActivityType.BUDGET_ADJUSTED)
            ),
            pastCycleId to listOf(
                BudgetActivity("act_past_1", pastCycleId, "Chi tiêu ăn uống", -1_200_000L, "28/09/2026", null, BudgetActivityType.PURCHASE_RECORDED)
            ),
            pastUncachedCycleId to emptyList()
        )
    )

    // Adjustments (cycleId -> List<BudgetAdjustment>)
    private val adjustmentsState = MutableStateFlow<Map<String, List<BudgetAdjustment>>>(
        mapOf(
            currentCycleId to listOf(
                BudgetAdjustment("adj_1", currentCycleId, 4_000_000L, 5_000_000L, "25/10/2026, 09:00")
            ),
            pastCycleId to emptyList(),
            pastUncachedCycleId to emptyList()
        )
    )

    // --- Scenario Switchers for Testing & Preview ---
    fun recordItemExpense(
        itemId: String,
        itemName: String,
        amountVnd: Long,
        paymentType: com.hoabh.chanko.domain.model.PaymentType,
        cycleId: String = currentCycleId
    ) {
        val currentSnapshot = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 5_000_000L, 0L)
        val newSpent = currentSnapshot.spentMinor + amountVnd
        snapshotsState.value = snapshotsState.value + (cycleId to currentSnapshot.copy(
            spentMinor = newSpent,
            updatedAt = System.currentTimeMillis()
        ))

        val currentActivities = activitiesState.value[cycleId] ?: emptyList()
        val paymentDesc = if (paymentType == com.hoabh.chanko.domain.model.PaymentType.CREDIT) " (Thẻ tín dụng)" else ""
        val newActivity = BudgetActivity(
            id = UUID.randomUUID().toString(),
            cycleId = cycleId,
            title = "Mua $itemName$paymentDesc",
            amountChangeMinor = -amountVnd,
            occurredAt = "Vừa xong",
            itemId = itemId,
            type = BudgetActivityType.PURCHASE_RECORDED
        )
        activitiesState.value = activitiesState.value + (cycleId to (listOf(newActivity) + currentActivities))
    }

    fun refundExpense(
        amountVnd: Long,
        cycleId: String = currentCycleId
    ) {
        val currentSnapshot = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 5_000_000L, 0L)
        val newSpent = maxOf(0L, currentSnapshot.spentMinor - amountVnd)
        snapshotsState.value = snapshotsState.value + (cycleId to currentSnapshot.copy(
            spentMinor = newSpent,
            updatedAt = System.currentTimeMillis()
        ))

        val currentActivities = activitiesState.value[cycleId] ?: emptyList()
        val newActivity = BudgetActivity(
            id = UUID.randomUUID().toString(),
            cycleId = cycleId,
            title = "Hoàn tiền giao dịch",
            amountChangeMinor = amountVnd,
            occurredAt = "Vừa xong",
            itemId = null,
            type = BudgetActivityType.EXPENSE_CORRECTED
        )
        activitiesState.value = activitiesState.value + (cycleId to (listOf(newActivity) + currentActivities))
    }

    fun setScenarioOverBudget(cycleId: String = currentCycleId) {
        val current = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 5_000_000L, 5_500_000L)
        snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
            budgetMinor = 5_000_000L,
            spentMinor = 5_500_000L,
            updatedAt = System.currentTimeMillis()
        ))
    }

    fun setScenarioZeroSpend(cycleId: String = currentCycleId) {
        val current = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 5_000_000L, 0L)
        snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
            budgetMinor = 5_000_000L,
            spentMinor = 0L,
            updatedAt = System.currentTimeMillis()
        ))
        activitiesState.value = activitiesState.value + (cycleId to emptyList())
    }

    fun setScenarioUnsetBudget(cycleId: String = currentCycleId) {
        val current = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 0L, 0L)
        snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
            budgetMinor = 0L,
            spentMinor = 0L,
            updatedAt = System.currentTimeMillis()
        ))
        activitiesState.value = activitiesState.value + (cycleId to emptyList())
    }

    fun setScenarioNormalWithin(cycleId: String = currentCycleId) {
        val current = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 5_000_000L, 3_000_000L)
        snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
            budgetMinor = 5_000_000L,
            spentMinor = 3_000_000L,
            updatedAt = System.currentTimeMillis()
        ))
    }

    /** Tổ hợp past + offline + over-budget (C10 + C11 + C12). */
    fun setScenarioPastOfflineOver() {
        setScenarioOverBudget(pastCycleId)
        cachedCycleIdsState.value = cachedCycleIdsState.value + pastCycleId
        isOfflineUncachedState.value = false
        isOnlineState.value = false
    }

    /** Tổ hợp past + offline + zero-spend (C11 + C12 + C13). */
    fun setScenarioPastOfflineZero() {
        setScenarioZeroSpend(pastCycleId)
        cachedCycleIdsState.value = cachedCycleIdsState.value + pastCycleId
        isOfflineUncachedState.value = false
        isOnlineState.value = false
    }

    fun setOfflineUncached(uncached: Boolean) {
        isOfflineUncachedState.value = uncached
        if (uncached) {
            cachedCycleIdsState.value = emptySet()
        } else {
            cachedCycleIdsState.value = setOf(currentCycleId, pastCycleId)
        }
    }

    fun setCycleCachedOffline(cycleId: String, cached: Boolean) {
        cachedCycleIdsState.value = if (cached) {
            cachedCycleIdsState.value + cycleId
        } else {
            cachedCycleIdsState.value - cycleId
        }
    }

    /** Mô phỏng thay đổi từ thiết bị/phiên khác để kiểm thử C04 Conflict Reload. */
    fun simulateExternalBudgetUpdate(
        cycleId: String = currentCycleId,
        externalNewLimitMinor: Long
    ) {
        val current = snapshotsState.value[cycleId] ?: return
        snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
            budgetMinor = externalNewLimitMinor,
            revision = current.revision + 1,
            updatedAt = System.currentTimeMillis()
        ))
    }

    // --- BudgetRepository Implementation ---
    override fun isOnlineFlow(): Flow<Boolean> = isOnlineState.asStateFlow()

    override suspend fun setNetworkOnline(isOnline: Boolean) {
        isOnlineState.value = isOnline
    }

    override fun getAvailableCycles(): Flow<List<BudgetCycle>> {
        return cyclesState.asStateFlow()
    }

    override fun getOfflineCachedCycleIds(): Flow<Set<String>> {
        return cachedCycleIdsState.asStateFlow()
    }

    override fun getCycleOverview(cycleId: String): Flow<BudgetCycleOverview> {
        return combine(
            snapshotsState,
            notableItemsState,
            activitiesState,
            adjustmentsState
        ) { snapshots, notables, activities, adjustments ->
            if (simulateLoadErrorOnNextFetch) {
                simulateLoadErrorOnNextFetch = false
                throw BudgetLoadException()
            }
            val isCached = cycleId in cachedCycleIdsState.value && !isOfflineUncachedState.value
            if (!isOnlineState.value && !isCached) {
                throw BudgetNoOfflineCacheException()
            }
            val snap = snapshots[cycleId] ?: throw BudgetLoadException()
            if (snap.budgetMinor < 0L || snap.spentMinor < 0L) {
                throw BudgetLoadException()
            }
            BudgetCycleOverview(
                cycleId = cycleId,
                snapshot = snap,
                notableItems = (notables[cycleId] ?: emptyList()).take(3),
                recentActivities = (activities[cycleId] ?: emptyList()).take(3),
                adjustments = adjustments[cycleId] ?: emptyList(),
                dataUpdatedAt = snap.updatedAt
            )
        }
    }

    override fun getBudgetSnapshot(cycleId: String): Flow<BudgetSnapshot> {
        return getCycleOverview(cycleId).map { it.snapshot }
    }

    override fun getNotableItems(cycleId: String): Flow<List<NotableItem>> {
        return notableItemsState.map { map ->
            (map[cycleId] ?: emptyList()).take(3)
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

    override suspend fun updateBudgetLimit(
        cycleId: String,
        newLimitMinor: Long,
        expectedRevision: Int
    ): Result<Unit> {
        if (simulatedDelayMs > 0L) delay(simulatedDelayMs)

        if (!isOnlineState.value) {
            return Result.failure(BudgetOfflineException())
        }

        val cycle = cyclesState.value.firstOrNull { it.id == cycleId }
            ?: return Result.failure(BudgetLoadException())

        if (!cycle.isCurrent) {
            return Result.failure(BudgetPastCycleException())
        }

        if (!MoneyVnd.isValidBudget(newLimitMinor)) {
            return Result.failure(InvalidBudgetAmountException())
        }

        if (simulateSaveErrorOnNextMutation) {
            simulateSaveErrorOnNextMutation = false
            return Result.failure(BudgetSaveException())
        }

        if (simulateConflictOnNextUpdate) {
            simulateConflictOnNextUpdate = false
            val current = snapshotsState.value[cycleId]
            if (current != null) {
                snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
                    revision = current.revision + 1,
                    updatedAt = System.currentTimeMillis()
                ))
            }
            return Result.failure(BudgetConflictException())
        }

        val currentSnap = snapshotsState.value[cycleId]
            ?: return Result.failure(BudgetLoadException())

        if (currentSnap.revision != expectedRevision) {
            return Result.failure(BudgetConflictException())
        }

        val now = System.currentTimeMillis()
        val updatedSnap = currentSnap.copy(
            budgetMinor = newLimitMinor,
            revision = currentSnap.revision + 1,
            updatedAt = now
        )
        snapshotsState.value = snapshotsState.value + (cycleId to updatedSnap)

        val newAdjustment = BudgetAdjustment(
            id = "adj_${UUID.randomUUID()}",
            cycleId = cycleId,
            oldLimitMinor = currentSnap.budgetMinor,
            newLimitMinor = newLimitMinor,
            adjustedAt = "Vừa xong"
        )
        val currentAdjustments = adjustmentsState.value[cycleId] ?: emptyList()
        adjustmentsState.value = adjustmentsState.value + (cycleId to (listOf(newAdjustment) + currentAdjustments))

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
    // --- Home v2.2 Budget Carousel Integration (BC22-01..BC22-20) ---

    override fun observeTriCycleWindow(): Flow<TriCycleWindow> {
        return combine(
            cyclesState,
            snapshotsState,
            isOnlineState,
            isOfflineUncachedState
        ) { cycles, snapshots, isOnline, isOfflineUncached ->
            if (!isOnline && isOfflineUncached) {
                throw BudgetNoOfflineCacheException()
            }

            val current = cycles.firstOrNull { it.isCurrent }
                ?: cycles.firstOrNull { it.status == BudgetCycleStatus.Active }
                ?: cycles.first()

            val previous = cycles.firstOrNull { it.ordinal == current.ordinal - 1 }

            val next = cycles.firstOrNull { it.ordinal == current.ordinal + 1 }
                ?: BudgetCycle(
                    id = "cycle_next_${current.ordinal + 1}",
                    label = "Chu kỳ tiếp theo",
                    startDate = "01/11/2026",
                    endDate = "30/11/2026",
                    isCurrent = false,
                    anchorDay = current.anchorDay,
                    ordinal = current.ordinal + 1,
                    status = BudgetCycleStatus.UpcomingUnconfigured
                )

            TriCycleWindow(
                previous = previous,
                current = current,
                next = next,
                snapshots = snapshots,
                lastSyncedAt = initialDataTimestamp
            )
        }
    }

    override suspend fun setupNextCycleBudget(limitMinor: Long): Result<Unit> {
        if (!isOnlineState.value) {
            return Result.failure(BudgetOfflineException())
        }
        if (limitMinor <= 0L) {
            return Result.failure(InvalidBudgetAmountException())
        }

        val current = cyclesState.value.firstOrNull { it.isCurrent }
            ?: return Result.failure(BudgetLoadException())
        val nextOrdinal = current.ordinal + 1
        val nextCycle = cyclesState.value.firstOrNull { it.ordinal == nextOrdinal }
            ?: BudgetCycle(
                id = nextCycleId,
                label = "Tháng 11/2026",
                startDate = "01/11/2026",
                endDate = "30/11/2026",
                isCurrent = false,
                anchorDay = current.anchorDay,
                ordinal = nextOrdinal,
                status = BudgetCycleStatus.UpcomingUnconfigured
            )

        val updatedNext = nextCycle.copy(
            status = BudgetCycleStatus.UpcomingConfigured,
            configuredLimitMinor = limitMinor
        )

        val existingIndex = cyclesState.value.indexOfFirst { it.ordinal == nextOrdinal }
        val newCycles = if (existingIndex >= 0) {
            cyclesState.value.toMutableList().apply { set(existingIndex, updatedNext) }
        } else {
            cyclesState.value + updatedNext
        }
        cyclesState.value = newCycles

        val updatedSnapshots = snapshotsState.value.toMutableMap().apply {
            put(
                updatedNext.id,
                BudgetSnapshot(
                    cycleId = updatedNext.id,
                    budgetMinor = limitMinor,
                    spentMinor = 0L,
                    revision = 1,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        snapshotsState.value = updatedSnapshots

        return Result.success(Unit)
    }

    override suspend fun triggerRollover(): Result<Unit> {
        val current = cyclesState.value.firstOrNull { it.isCurrent }
            ?: return Result.failure(BudgetLoadException())
        val nextOrdinal = current.ordinal + 1
        val next = cyclesState.value.firstOrNull { it.ordinal == nextOrdinal }
            ?: BudgetCycle(
                id = nextCycleId,
                label = "Tháng 11/2026",
                startDate = "01/11/2026",
                endDate = "30/11/2026",
                isCurrent = false,
                anchorDay = current.anchorDay,
                ordinal = nextOrdinal,
                status = BudgetCycleStatus.UpcomingUnconfigured
            )

        val oldWindow = TriCycleWindow(
            previous = cyclesState.value.firstOrNull { it.ordinal == current.ordinal - 1 },
            current = current,
            next = next
        )

        val futureOrdinal = nextOrdinal + 1
        val newNextCycle = BudgetCycle(
            id = "cycle_future_$futureOrdinal",
            label = "Tháng 12/2026",
            startDate = "01/12/2026",
            endDate = "31/12/2026",
            isCurrent = false,
            anchorDay = current.anchorDay,
            ordinal = futureOrdinal,
            status = BudgetCycleStatus.UpcomingUnconfigured
        )

        val newWindow = BudgetCalculation.performRollover(oldWindow, newNextCycle)

        val updatedCycles = cyclesState.value.map { cycle ->
            when (cycle.id) {
                newWindow.previous?.id -> newWindow.previous!!
                newWindow.current.id -> newWindow.current
                else -> cycle
            }
        }.toMutableList()

        if (updatedCycles.none { it.id == newWindow.next.id }) {
            updatedCycles.add(newWindow.next)
        }
        cyclesState.value = updatedCycles

        if (newWindow.current.configuredLimitMinor != null) {
            val limit = newWindow.current.configuredLimitMinor!!
            val newSnapshots = snapshotsState.value.toMutableMap().apply {
                put(
                    newWindow.current.id,
                    BudgetSnapshot(
                        cycleId = newWindow.current.id,
                        budgetMinor = limit,
                        spentMinor = 0L,
                        revision = 1,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            snapshotsState.value = newSnapshots
        }

        return Result.success(Unit)
    }

    fun setNoPreviousCycleForTesting() {
        val current = cyclesState.value.first { it.isCurrent }
        cyclesState.value = cyclesState.value.filter { it.ordinal >= current.ordinal }
    }
}
