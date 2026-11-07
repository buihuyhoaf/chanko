package com.hoabh.chanko.presentation.features.home.domain

import androidx.annotation.DrawableRes
import com.hoabh.chanko.presentation.features.home.BudgetCreditCardUi
import com.hoabh.chanko.presentation.features.home.BudgetStateKind
import com.hoabh.chanko.presentation.features.home.DecisionVerdict
import com.hoabh.chanko.presentation.features.home.ItemJourneyUi
import com.hoabh.chanko.presentation.features.home.JourneyNodeStatus
import com.hoabh.chanko.presentation.features.home.JourneyNodeUi
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Định dạng tiền tệ VND xác định (Deterministic VND Formatter):
 * - Sử dụng dấu chấm phân tách hàng nghìn (`2.350.000đ` hoặc `2.350.000 ₫`).
 * - Giá thiếu (`null`) hiển thị đúng `"Chưa có giá"`, không bao giờ ngầm coi là `0đ` (AC-09).
 * - Hỗ trợ số âm (`-600.000đ`) cho trường hợp vượt ngân sách (AC-06).
 */
object HomeCurrencyFormatter {
    const val MISSING_PRICE_TEXT = "Chưa có giá"
    const val MISSING_BUDGET_PRICE_NOTE = "Chưa đủ dữ liệu giá"

    fun formatVnd(amountMinor: Long, compactUnit: Boolean = false): String {
        val isNegative = amountMinor < 0L
        val absValue = abs(amountMinor)
        val digits = absValue.toString()
        val grouped = buildString {
            digits.forEachIndexed { index, ch ->
                if (index > 0 && (digits.length - index) % 3 == 0) {
                    append('.')
                }
                append(ch)
            }
        }
        val suffix = if (compactUnit) "đ" else " ₫"
        return if (isNegative) "-$grouped$suffix" else "$grouped$suffix"
    }

    fun formatNullableVnd(amountMinor: Long?, compactUnit: Boolean = false): String {
        return amountMinor?.let { formatVnd(it, compactUnit) } ?: MISSING_PRICE_TEXT
    }
}

/**
 * Bộ tính toán ngân sách cho Budget Credit Card v2.0 (UX-SPEC.md, AC-03..AC-09, AC-25):
 * - Gộp cả `payNowSpentMinor` và `creditSpentMinor` theo quy tắc Budget hiện hành (AC-03).
 * - `remaining = limit - spent` khi dữ liệu đầy đủ; khi vượt giữ số âm và giải thích số vượt bằng Coral (AC-06).
 * - `visualProgress` clamp về `0f..1f` chỉ cho thanh tiến trình, không clamp số tiền (AC-06).
 * - `limit <= 0` hoặc `limit == null` chuyển sang `BudgetStateKind.NoBudget`, không chia cho 0, không báo `0đ` giả (AC-07).
 * - Khi `hasMissingPriceItems == true`: đính kèm chú thích `"Chưa đủ dữ liệu giá"`, không tự coi giá thiếu là `0đ` (AC-09).
 */
object BudgetV2Calculator {
    private const val UNCONFIGURED_TEXT = "Chưa thiết lập"

    fun compute(
        cycleLabel: String,
        payNowSpentMinor: Long?,
        creditSpentMinor: Long? = 0L,
        limitMinor: Long?,
        hasMissingPriceItems: Boolean = false
    ): BudgetCreditCardUi {
        val totalSpentMinor = if (payNowSpentMinor == null && creditSpentMinor == null) {
            null
        } else {
            (payNowSpentMinor ?: 0L) + (creditSpentMinor ?: 0L)
        }

        val missingNote = if (hasMissingPriceItems) {
            HomeCurrencyFormatter.MISSING_BUDGET_PRICE_NOTE
        } else {
            null
        }

        if (totalSpentMinor == null || limitMinor == null || limitMinor <= 0L) {
            return BudgetCreditCardUi(
                cycleLabel = cycleLabel,
                spentMinor = totalSpentMinor,
                limitMinor = limitMinor,
                remainingMinor = null,
                overMinor = null,
                ratio = null,
                percentageInt = null,
                visualProgress = 0f,
                stateKind = BudgetStateKind.NoBudget,
                hasMissingPriceItems = hasMissingPriceItems,
                missingPriceNote = missingNote,
                spentFormatted = totalSpentMinor?.let { HomeCurrencyFormatter.formatVnd(it, compactUnit = true) }
                    ?: UNCONFIGURED_TEXT,
                limitFormatted = UNCONFIGURED_TEXT,
                remainingFormatted = UNCONFIGURED_TEXT,
                overWarningFormatted = null,
                progressPercentText = "--%"
            )
        }

        val remainingMinor = limitMinor - totalSpentMinor
        val overMinor = max(totalSpentMinor - limitMinor, 0L)
        val rawRatio = totalSpentMinor.toDouble() / limitMinor.toDouble()
        val percentageInt = (rawRatio * 100.0).roundToInt()
        val visualProgress = rawRatio.toFloat().coerceIn(0f, 1f)

        val stateKind = when {
            totalSpentMinor > limitMinor -> BudgetStateKind.OverBudget
            totalSpentMinor == limitMinor -> BudgetStateKind.Reached
            rawRatio >= 0.80 -> BudgetStateKind.Warning
            else -> BudgetStateKind.Normal
        }

        val overWarning = if (stateKind == BudgetStateKind.OverBudget) {
            "Vượt ngân sách ${HomeCurrencyFormatter.formatVnd(overMinor, compactUnit = true)}"
        } else {
            null
        }

        return BudgetCreditCardUi(
            cycleLabel = cycleLabel,
            spentMinor = totalSpentMinor,
            limitMinor = limitMinor,
            remainingMinor = remainingMinor,
            overMinor = overMinor,
            ratio = rawRatio.toFloat(),
            percentageInt = percentageInt,
            visualProgress = visualProgress,
            stateKind = stateKind,
            hasMissingPriceItems = hasMissingPriceItems,
            missingPriceNote = missingNote,
            spentFormatted = HomeCurrencyFormatter.formatVnd(totalSpentMinor, compactUnit = true),
            limitFormatted = HomeCurrencyFormatter.formatVnd(limitMinor, compactUnit = true),
            remainingFormatted = HomeCurrencyFormatter.formatVnd(remainingMinor, compactUnit = true),
            overWarningFormatted = overWarning,
            progressPercentText = "$percentageInt%"
        )
    }
}

/**
 * Ứng viên đầu vào cho hành trình món đồ gần đây (Item Journey v2.0 — AC-10..AC-15, AC-23..AC-25).
 */
data class JourneyItemCandidate(
    val itemId: String?,
    val title: String?,
    val priceMinor: Long?,
    val statusRaw: String?,
    val createdAtEpochMillis: Long,
    val isArchived: Boolean = false,
    val remainingSeconds: Long? = null,
    val verdict: DecisionVerdict = DecisionVerdict.WAIT,
    @param:DrawableRes val thumbnailResId: Int? = null,
    val imageLoadFailed: Boolean = false
)

/**
 * Bộ chọn & sắp xếp ổn định cho Item Journey v2.0 (UX-SPEC.md, ANDROID-COMPOSE-GUIDE.md, AC-10..AC-14, AC-25):
 * 1. Loại bỏ các món có `isArchived == true` TRƯỚC KHI sort và lấy tối đa 5 món (AC-10).
 * 2. Khử trùng lặp theo `itemId` (giữ bản ghi hợp lệ mới nhất của cùng `itemId`).
 * 3. Reconcile hết hạn `Cooling-off` (`status == Waiting && remainingSeconds <= 0`) sang `Ready` ("Sẵn sàng xem lại")
 *    mà KHÔNG tự động chuyển `verdict` sang `BUY` (AC-25).
 * 4. Sắp xếp theo `createdAtEpochMillis DESC`, khi trùng `createdAt` thì tie-break ổn định theo `itemId ASC`;
 *    thay đổi `status` tuyệt đối KHÔNG làm thay đổi thứ tự các node (AC-11).
 * 5. Lấy tối đa 5 món (`min(n, 5)`) — không tạo node giả cho đủ 5 (AC-12).
 */
object JourneySelector {
    const val MAX_JOURNEY_NODES = 5

    fun selectRecentJourney(
        candidates: List<JourneyItemCandidate>,
        maxNodes: Int = MAX_JOURNEY_NODES
    ): ItemJourneyUi {
        val uniqueNonArchived = LinkedHashMap<String, JourneyNodeUi>()

        for (candidate in candidates) {
            if (candidate.isArchived) continue

            val cleanId = candidate.itemId?.trim().orEmpty()
            val cleanTitle = candidate.title?.trim().orEmpty()
            val parsedStatus = parseStatus(candidate.statusRaw)

            if (cleanId.isEmpty() || cleanTitle.isEmpty() || parsedStatus == null) {
                continue
            }

            val isCoolingExpired = parsedStatus == JourneyNodeStatus.Waiting &&
                candidate.remainingSeconds != null &&
                candidate.remainingSeconds <= 0L

            val effectiveStatus = if (isCoolingExpired) {
                JourneyNodeStatus.Ready
            } else {
                parsedStatus
            }

            val preservedVerdict = if (isCoolingExpired && candidate.verdict == DecisionVerdict.BUY) {
                DecisionVerdict.WAIT
            } else {
                candidate.verdict
            }

            uniqueNonArchived[cleanId] = JourneyNodeUi(
                itemId = cleanId,
                title = cleanTitle,
                priceMinor = candidate.priceMinor,
                priceFormatted = HomeCurrencyFormatter.formatNullableVnd(
                    amountMinor = candidate.priceMinor,
                    compactUnit = true
                ),
                status = effectiveStatus,
                statusLabel = effectiveStatus.vietnameseLabel,
                verdict = preservedVerdict,
                createdAtEpochMillis = candidate.createdAtEpochMillis,
                remainingSeconds = candidate.remainingSeconds,
                thumbnailResId = candidate.thumbnailResId,
                imageLoadFailed = candidate.imageLoadFailed,
                isCoolingOffJustExpired = isCoolingExpired
            )
        }

        val sortedAll = uniqueNonArchived.values.sortedWith(
            compareByDescending<JourneyNodeUi> { it.createdAtEpochMillis }
                .thenBy { it.itemId }
        )

        return ItemJourneyUi(
            nodes = sortedAll.take(maxNodes),
            totalActiveAndHistoryCount = sortedAll.size
        )
    }

    fun parseStatus(raw: String?): JourneyNodeStatus? = when (raw?.trim()) {
        "Saved" -> JourneyNodeStatus.Saved
        "Considering" -> JourneyNodeStatus.Considering
        "Waiting", "CoolingOff" -> JourneyNodeStatus.Waiting
        "Ready", "Review" -> JourneyNodeStatus.Ready
        "Purchased", "Bought" -> JourneyNodeStatus.Purchased
        "Skipped", "Passed" -> JourneyNodeStatus.Skipped
        else -> null
    }
}
