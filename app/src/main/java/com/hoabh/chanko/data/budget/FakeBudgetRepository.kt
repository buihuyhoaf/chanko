package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.model.BudgetActivity
import com.hoabh.chanko.domain.budget.model.BudgetActivityType
import com.hoabh.chanko.domain.budget.model.BudgetAdjustment
import com.hoabh.chanko.domain.budget.model.BudgetConflictException
import com.hoabh.chanko.domain.budget.model.BudgetCycle
import com.hoabh.chanko.domain.budget.model.BudgetCycleOverview
import com.hoabh.chanko.domain.budget.model.BudgetLoadException
import com.hoabh.chanko.domain.budget.model.BudgetNoOfflineCacheException
import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.model.BudgetPastCycleException
import com.hoabh.chanko.domain.budget.model.BudgetSaveException
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.CardColor
import com.hoabh.chanko.domain.budget.model.CardNameValidationResult
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.domain.budget.model.AddCardValidationResult
import com.hoabh.chanko.domain.budget.model.CreditCardUnavailableException
import com.hoabh.chanko.domain.budget.model.DuplicateCardNameException
import com.hoabh.chanko.domain.budget.model.InvalidBudgetAmountException
import com.hoabh.chanko.domain.budget.model.InvalidCardNameException
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.domain.budget.model.NotableItem
import com.hoabh.chanko.domain.budget.model.PaymentMethodBreakdown
import com.hoabh.chanko.domain.budget.model.ReservedCardNameException
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Locale
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
                anchorDay = 1
            ),
            BudgetCycle(
                id = pastCycleId,
                label = "Tháng 09/2026",
                startDate = "01/09/2026",
                endDate = "30/09/2026",
                isCurrent = false,
                anchorDay = 1
            ),
            BudgetCycle(
                id = pastUncachedCycleId,
                label = "Tháng 08/2026",
                startDate = "01/08/2026",
                endDate = "31/08/2026",
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

    // Payment breakdowns (cycleId -> List<PaymentMethodBreakdown>)
    private val paymentBreakdownState = MutableStateFlow<Map<String, List<PaymentMethodBreakdown>>>(
        mapOf(
            currentCycleId to listOf(
                PaymentMethodBreakdown("card_1", "Thẻ Techcombank", 1_800_000L),
                PaymentMethodBreakdown("pm_2", "Tiền mặt", 1_200_000L)
            ),
            pastCycleId to listOf(
                PaymentMethodBreakdown("card_1", "Thẻ Techcombank", 3_000_000L),
                PaymentMethodBreakdown("pm_2", "Tiền mặt", 1_200_000L)
            ),
            pastUncachedCycleId to listOf(
                PaymentMethodBreakdown("pm_2", "Tiền mặt", 3_800_000L)
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

    // Credit cards
    private val cardsState = MutableStateFlow(
        listOf(
            CreditCard("card_1", "Thẻ Techcombank", color = CardColor.PINK, isArchived = false),
            CreditCard("card_2", "Thẻ VPBank Cake", color = CardColor.PURPLE, isArchived = false)
        )
    )

    // --- Scenario Switchers for Testing & Preview ---
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
        paymentBreakdownState.value = paymentBreakdownState.value + (cycleId to emptyList())
        activitiesState.value = activitiesState.value + (cycleId to emptyList())
    }

    fun setScenarioUnsetBudget(cycleId: String = currentCycleId) {
        val current = snapshotsState.value[cycleId] ?: BudgetSnapshot(cycleId, 0L, 0L)
        snapshotsState.value = snapshotsState.value + (cycleId to current.copy(
            budgetMinor = 0L,
            spentMinor = 0L,
            updatedAt = System.currentTimeMillis()
        ))
        paymentBreakdownState.value = paymentBreakdownState.value + (cycleId to emptyList())
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
            paymentBreakdownState,
            notableItemsState,
            activitiesState,
            adjustmentsState
        ) { snapshots, payments, notables, activities, adjustments ->
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
                paymentMethods = payments[cycleId] ?: emptyList(),
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

    override fun getPaymentBreakdown(cycleId: String): Flow<List<PaymentMethodBreakdown>> {
        return paymentBreakdownState.map { map ->
            map[cycleId] ?: emptyList()
        }
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

    override suspend fun addCreditCard(name: String, color: CardColor): Result<CreditCard> {
        if (simulatedDelayMs > 0L) delay(simulatedDelayMs)

        if (!isOnlineState.value) {
            return Result.failure(BudgetOfflineException())
        }

        val validation = CreditCard.validateAdd(name, cardsState.value.filter { !it.isArchived }.map { it.name })
        val normalized = when (validation) {
            is AddCardValidationResult.Valid -> validation.normalizedName
            AddCardValidationResult.ReservedPayNow -> return Result.failure(ReservedCardNameException())
            AddCardValidationResult.Duplicate -> return Result.failure(DuplicateCardNameException())
            else -> return Result.failure(InvalidCardNameException())
        }

        if (simulateSaveErrorOnNextMutation) {
            simulateSaveErrorOnNextMutation = false
            return Result.failure(BudgetSaveException())
        }

        val newCard = CreditCard(
            id = "card_${UUID.randomUUID()}",
            name = normalized,
            color = color,
            isArchived = false
        )
        cardsState.value = cardsState.value + newCard
        return Result.success(newCard)
    }

    override suspend fun renameCreditCard(cardId: String, newName: String): Result<Unit> {
        if (simulatedDelayMs > 0L) delay(simulatedDelayMs)

        if (!isOnlineState.value) {
            return Result.failure(BudgetOfflineException())
        }

        val target = cardsState.value.firstOrNull { it.id == cardId && !it.isArchived }
            ?: return Result.failure(CreditCardUnavailableException())

        val validation = CreditCard.validateName(newName)
        if (validation !is CardNameValidationResult.Valid) {
            return Result.failure(InvalidCardNameException())
        }

        val normalized = validation.normalizedName
        if (normalized.lowercase(Locale.ROOT) == CreditCard.RESERVED_PAY_NOW) {
            return Result.failure(ReservedCardNameException())
        }

        val activeCards = cardsState.value.filter { !it.isArchived && it.id != cardId }
        if (activeCards.any { CreditCard.isSameName(it.name, normalized) }) {
            return Result.failure(DuplicateCardNameException())
        }

        if (simulateSaveErrorOnNextMutation) {
            simulateSaveErrorOnNextMutation = false
            return Result.failure(BudgetSaveException())
        }

        cardsState.value = cardsState.value.map {
            if (it.id == cardId) it.copy(name = normalized) else it
        }
        // Current cycle metadata reflects renamed card; past cycles keep historical snapshot name
        val currentBreakdown = paymentBreakdownState.value[currentCycleId]
        if (currentBreakdown != null) {
            paymentBreakdownState.value = paymentBreakdownState.value + (
                currentCycleId to currentBreakdown.map {
                    if (it.id == cardId) it.copy(name = normalized) else it
                }
            )
        }
        return Result.success(Unit)
    }

    override suspend fun archiveCreditCard(cardId: String): Result<Unit> {
        if (simulatedDelayMs > 0L) delay(simulatedDelayMs)

        if (!isOnlineState.value) {
            return Result.failure(BudgetOfflineException())
        }

        val exists = cardsState.value.any { it.id == cardId && !it.isArchived }
        if (!exists) {
            return Result.failure(CreditCardUnavailableException("Thẻ đã được xóa"))
        }

        if (simulateSaveErrorOnNextMutation) {
            simulateSaveErrorOnNextMutation = false
            return Result.failure(BudgetSaveException())
        }

        cardsState.value = cardsState.value.map {
            if (it.id == cardId) it.copy(isArchived = true) else it
        }
        return Result.success(Unit)
    }
}
