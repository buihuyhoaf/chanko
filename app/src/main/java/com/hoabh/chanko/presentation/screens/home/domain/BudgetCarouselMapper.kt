package com.hoabh.chanko.presentation.screens.home.domain

import com.hoabh.chanko.domain.budget.model.BudgetCalculation
import com.hoabh.chanko.domain.budget.model.BudgetCycleStatus
import com.hoabh.chanko.domain.budget.model.BudgetSnapshot
import com.hoabh.chanko.domain.budget.model.TriCycleWindow
import com.hoabh.chanko.presentation.screens.home.BudgetCardSlotUi
import com.hoabh.chanko.presentation.screens.home.BudgetCarouselUi

/**
 * Mapper chuyển đổi từ domain [TriCycleWindow] sang [BudgetCarouselUi] (Home v2.2 - BC22-01..BC22-13).
 */
object BudgetCarouselMapper {

    fun map(
        window: TriCycleWindow,
        snapshots: Map<String, BudgetSnapshot> = emptyMap(),
        selectedSlotKey: String? = null
    ): BudgetCarouselUi {
        val current = window.current
        val currentKey = current.id
        val currentSnapshot = snapshots[currentKey]

        val slots = mutableListOf<BudgetCardSlotUi>()

        // 1. Previous Slot (nếu tồn tại - BC22-04, BC22-10)
        window.previous?.let { prev ->
            val prevSnapshot = snapshots[prev.id]
            val limit = prevSnapshot?.budgetMinor ?: 0L
            val spent = prevSnapshot?.spentMinor ?: 0L
            val metrics = BudgetCalculation.calculateMetrics(limit, spent)

            slots.add(
                BudgetCardSlotUi.ClosedCard(
                    key = prev.id,
                    cycleId = prev.id,
                    cycleLabel = prev.label,
                    dateRangeText = prev.dateRangeText,
                    monthLabel = prev.labelMonth,
                    themeOrdinal = prev.ordinal,
                    spentMinor = spent,
                    limitMinor = limit,
                    remainingMinor = metrics.remainingMinor ?: (limit - spent),
                    overMinor = metrics.overMinor,
                    clampedProgress = metrics.clampedProgress,
                    realPercentText = metrics.realPercentText ?: "0%",
                    isOverBudget = metrics.isOverBudget,
                    spentFormatted = HomeCurrencyFormatter.formatVnd(spent),
                    limitFormatted = HomeCurrencyFormatter.formatVnd(limit),
                    remainingFormatted = HomeCurrencyFormatter.formatVnd(metrics.remainingMinor ?: (limit - spent)),
                    overFormatted = metrics.overMinor?.let { HomeCurrencyFormatter.formatVnd(it) }
                )
            )
        }

        // 2. Current Slot (BC22-11, BC22-13)
        val currentLimit = currentSnapshot?.budgetMinor ?: current.configuredLimitMinor
        val currentSpent = currentSnapshot?.spentMinor ?: 0L

        if (currentLimit == null || currentLimit <= 0L) {
            slots.add(
                BudgetCardSlotUi.CurrentEmptyCard(
                    key = current.id,
                    cycleId = current.id,
                    cycleLabel = current.label,
                    dateRangeText = current.dateRangeText,
                    monthLabel = current.labelMonth,
                    themeOrdinal = current.ordinal
                )
            )
        } else {
            val metrics = BudgetCalculation.calculateMetrics(currentLimit, currentSpent)
            slots.add(
                BudgetCardSlotUi.ActiveCard(
                    key = current.id,
                    cycleId = current.id,
                    cycleLabel = current.label,
                    dateRangeText = current.dateRangeText,
                    monthLabel = current.labelMonth,
                    themeOrdinal = current.ordinal,
                    spentMinor = currentSpent,
                    limitMinor = currentLimit,
                    remainingMinor = metrics.remainingMinor ?: (currentLimit - currentSpent),
                    overMinor = metrics.overMinor,
                    clampedProgress = metrics.clampedProgress,
                    realPercentText = metrics.realPercentText ?: "0%",
                    isOverBudget = metrics.isOverBudget,
                    spentFormatted = HomeCurrencyFormatter.formatVnd(currentSpent),
                    limitFormatted = HomeCurrencyFormatter.formatVnd(currentLimit),
                    remainingFormatted = HomeCurrencyFormatter.formatVnd(metrics.remainingMinor ?: (currentLimit - currentSpent)),
                    overFormatted = metrics.overMinor?.let { HomeCurrencyFormatter.formatVnd(it) }
                )
            )
        }

        // 3. Next Slot (BC22-05, BC22-07)
        val next = window.next
        val nextConfiguredLimit = next.configuredLimitMinor
        if (next.status == BudgetCycleStatus.UpcomingConfigured && nextConfiguredLimit != null) {
            val limit = nextConfiguredLimit
            slots.add(
                BudgetCardSlotUi.UpcomingConfiguredCard(
                    key = next.id,
                    cycleId = next.id,
                    cycleLabel = next.label,
                    dateRangeText = next.dateRangeText,
                    monthLabel = next.labelMonth,
                    themeOrdinal = next.ordinal,
                    limitMinor = limit,
                    limitFormatted = HomeCurrencyFormatter.formatVnd(limit)
                )
            )
        } else {
            slots.add(
                BudgetCardSlotUi.UpcomingPlaceholderCard(
                    key = next.id,
                    cycleId = next.id,
                    cycleLabel = next.label.ifBlank { "Chu kỳ tiếp theo" },
                    dateRangeText = next.dateRangeText,
                    monthLabel = next.labelMonth,
                    themeOrdinal = next.ordinal
                )
            )
        }

        val effectiveSelectedKey = selectedSlotKey?.takeIf { key -> slots.any { it.key == key } }
            ?: currentKey

        return BudgetCarouselUi(
            slots = slots,
            selectedSlotKey = effectiveSelectedKey,
            currentCycleKey = currentKey,
            lastSyncedAtText = null
        )
    }
}
