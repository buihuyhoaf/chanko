package com.hoabh.chanko.presentation.screens.home.domain

import androidx.annotation.DrawableRes
import com.hoabh.chanko.presentation.screens.home.BudgetStateKind
import com.hoabh.chanko.presentation.screens.home.BudgetUi
import com.hoabh.chanko.presentation.screens.home.DecisionStatus
import com.hoabh.chanko.presentation.screens.home.DecisionUi
import com.hoabh.chanko.presentation.screens.home.DecisionVerdict
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Định dạng tiền tệ VND xác định (Deterministic VND Formatter):
 * - Sử dụng dấu chấm phân tách hàng nghìn theo chuẩn hiển thị tiếng Việt (`2.350.000 ₫`).
 * - Giá thiếu (`null`) hiển thị đúng `"Chưa có giá"`, không bao giờ coi là `0 ₫` (HOME-EDGE-04).
 * - Hỗ trợ số âm (`-600.000 ₫`) cho trường hợp vượt ngân sách.
 */
object HomeCurrencyFormatter {
    const val MISSING_PRICE_TEXT = "Chưa có giá"

    fun formatVnd(amountMinor: Long): String {
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
        return if (isNegative) "-$grouped ₫" else "$grouped ₫"
    }

    fun formatNullableVnd(amountMinor: Long?): String {
        return amountMinor?.let(::formatVnd) ?: MISSING_PRICE_TEXT
    }
}

/**
 * Bộ tính toán ngân sách thuần Kotlin (AC-11, AC-13):
 * - Tính số còn lại, số vượt, phần trăm đã chi và thanh tiến trình.
 * - Không chia cho 0 khi hạn mức <= 0; không bịa 0 ₫ khi chưa thiết lập ngân sách.
 */
object BudgetCalculator {
    private const val UNKNOWN_TEXT = "Chưa thiết lập"
    private const val ZERO_LIMIT_BADGE = "Chưa đặt hạn mức"
    private const val UNKNOWN_BADGE = "Chưa thiết lập ngân sách"

    fun compute(
        cycleLabel: String,
        spentMinor: Long?,
        limitMinor: Long?
    ): BudgetUi {
        if (spentMinor == null || limitMinor == null) {
            return BudgetUi(
                cycleLabel = cycleLabel,
                spentMinor = spentMinor,
                limitMinor = limitMinor,
                remainingMinor = null,
                overMinor = null,
                ratio = null,
                percentageInt = null,
                visualProgress = 0f,
                stateKind = BudgetStateKind.Unknown,
                spentFormatted = spentMinor?.let(HomeCurrencyFormatter::formatVnd) ?: UNKNOWN_TEXT,
                limitFormatted = limitMinor?.let(HomeCurrencyFormatter::formatVnd) ?: UNKNOWN_TEXT,
                remainingFormatted = UNKNOWN_TEXT,
                badgeText = UNKNOWN_BADGE,
                progressRightLabel = "--%"
            )
        }

        if (limitMinor <= 0L) {
            val remaining = -spentMinor
            return BudgetUi(
                cycleLabel = cycleLabel,
                spentMinor = spentMinor,
                limitMinor = limitMinor,
                remainingMinor = remaining,
                overMinor = max(spentMinor, 0L),
                ratio = null,
                percentageInt = null,
                visualProgress = 0f,
                stateKind = BudgetStateKind.Zero,
                spentFormatted = HomeCurrencyFormatter.formatVnd(spentMinor),
                limitFormatted = HomeCurrencyFormatter.formatVnd(0L),
                remainingFormatted = HomeCurrencyFormatter.formatVnd(remaining),
                badgeText = ZERO_LIMIT_BADGE,
                progressRightLabel = "Đã chi 0%"
            )
        }

        val remainingMinor = limitMinor - spentMinor
        val overMinor = max(spentMinor - limitMinor, 0L)
        val rawRatio = spentMinor.toDouble() / limitMinor.toDouble()
        val percentageInt = (rawRatio * 100.0).roundToInt()
        val visualProgress = rawRatio.toFloat().coerceIn(0f, 1f)

        val stateKind = when {
            spentMinor > limitMinor -> BudgetStateKind.OverBudget
            spentMinor == limitMinor -> BudgetStateKind.Reached
            rawRatio >= 0.80 -> BudgetStateKind.Warning
            else -> BudgetStateKind.Normal
        }

        val badgeText = when (stateKind) {
            BudgetStateKind.Normal,
            BudgetStateKind.Warning -> "Đã chi $percentageInt%"
            BudgetStateKind.Reached -> "Chạm hạn mức 100%"
            BudgetStateKind.OverBudget -> "Vượt hạn mức ${HomeCurrencyFormatter.formatVnd(overMinor)}"
            BudgetStateKind.Zero -> ZERO_LIMIT_BADGE
            BudgetStateKind.Unknown -> UNKNOWN_BADGE
        }

        return BudgetUi(
            cycleLabel = cycleLabel,
            spentMinor = spentMinor,
            limitMinor = limitMinor,
            remainingMinor = remainingMinor,
            overMinor = overMinor,
            ratio = rawRatio.toFloat(),
            percentageInt = percentageInt,
            visualProgress = visualProgress,
            stateKind = stateKind,
            spentFormatted = HomeCurrencyFormatter.formatVnd(spentMinor),
            limitFormatted = HomeCurrencyFormatter.formatVnd(limitMinor),
            remainingFormatted = HomeCurrencyFormatter.formatVnd(remainingMinor),
            badgeText = badgeText,
            progressRightLabel = "Đã chi $percentageInt%"
        )
    }
}

/**
 * Dữ liệu đầu vào thô của một món đồ trước khi qua bộ phân nhóm & xếp hạng (AC-07, AC-08, AC-09, HOME-EDGE-04).
 */
data class RawDecisionCandidate(
    val id: String?,
    val title: String?,
    val priceMinor: Long?,
    val statusRaw: String?,
    val remainingSeconds: Long? = null,
    val verdict: DecisionVerdict = DecisionVerdict.WAIT,
    val customSecondaryText: String? = null,
    @param:DrawableRes val thumbnailResId: Int? = null
)

/**
 * Bộ phân nhóm đồng nhất và xếp hạng quyết định mua sắm (AC-07, AC-08, AC-09, HOME-GAP-01, HOME-EDGE-01, HOME-EDGE-04):
 * - Loại bỏ ứng viên có id rỗng, title rỗng hoặc status không hợp lệ.
 * - Khử trùng lặp theo `id`, giữ lần xuất hiện đầu tiên để một món không bao giờ bị tính trùng (AC-08).
 * - Khi món `Waiting` có `remainingSeconds <= 0` (hết hạn cooling-off), tự động chuyển sang nhóm `Ready` ("Cần xem lại")
 *   với nhãn `"Vừa hết thời gian chờ"` nhưng KHÔNG tự động đổi `verdict` thành `BUY` (AC-07).
 * - Ưu tiên chọn tối đa 3 món cần hành động cho khu vực "Những món cần chú ý" (AC-09).
 */
object DecisionRanker {
    private const val MAX_VISIBLE_DECISIONS = 3

    fun rankAndSelectTop(
        candidates: List<RawDecisionCandidate>,
        maxCount: Int = MAX_VISIBLE_DECISIONS
    ): DecisionRankingResult {
        val validUniqueCandidates = LinkedHashMap<String, ReconciledCandidate>()

        for (candidate in candidates) {
            val cleanId = candidate.id?.trim().orEmpty()
            val cleanTitle = candidate.title?.trim().orEmpty()
            val rawStatus = parseStatus(candidate.statusRaw)

            if (cleanId.isEmpty() || cleanTitle.isEmpty() || rawStatus == null) {
                continue
            }

            if (!validUniqueCandidates.containsKey(cleanId)) {
                val isCoolingOffExpired = rawStatus == DecisionStatus.Waiting &&
                    candidate.remainingSeconds != null &&
                    candidate.remainingSeconds <= 0L

                // AC-07 & HOME-EDGE-01: Món hết cooling-off tự chuyển sang nhóm Cần xem lại (Ready),
                // nhưng giữ nguyên verdict gốc (không tự đổi thành BUY).
                val effectiveStatus = if (isCoolingOffExpired) {
                    DecisionStatus.Ready
                } else {
                    rawStatus
                }

                val preservedVerdict = if (isCoolingOffExpired && candidate.verdict == DecisionVerdict.BUY) {
                    DecisionVerdict.WAIT
                } else {
                    candidate.verdict
                }

                validUniqueCandidates[cleanId] = ReconciledCandidate(
                    id = cleanId,
                    title = cleanTitle,
                    priceMinor = candidate.priceMinor,
                    effectiveStatus = effectiveStatus,
                    verdict = preservedVerdict,
                    remainingSeconds = candidate.remainingSeconds,
                    customSecondaryText = candidate.customSecondaryText,
                    thumbnailResId = candidate.thumbnailResId,
                    isCoolingOffJustExpired = isCoolingOffExpired
                )
            }
        }

        val allReconciled = validUniqueCandidates.values.map(::toDecisionUi)

        // Phân tách các nhóm độc lập cho Little Room & Contextual Sheet (AC-08: không trùng itemId)
        val reviewItems = allReconciled.filter { it.status == DecisionStatus.Ready }
        val waitingItems = allReconciled.filter { it.status == DecisionStatus.Waiting }
        val skippedItems = allReconciled.filter { it.status == DecisionStatus.Skipped }

        // Khu vực "Những món cần chú ý" chỉ hiển thị các món đang hoạt động (không lấy Skipped),
        // ưu tiên món cần hành động (Ready / Cần xem lại lên đầu) và giới hạn tối đa 3 món (AC-09).
        val actionableCandidates = allReconciled
            .filter { it.status != DecisionStatus.Skipped }
            .sortedBy { it.status.priorityRank }

        val topItems = actionableCandidates.take(maxCount)

        return DecisionRankingResult(
            topDecisions = topItems,
            totalValidCount = actionableCandidates.size,
            waitingItems = waitingItems,
            reviewItems = reviewItems,
            skippedItems = skippedItems
        )
    }

    private fun parseStatus(raw: String?): DecisionStatus? = when (raw?.trim()) {
        "Ready", "Review" -> DecisionStatus.Ready
        "Waiting", "CoolingOff" -> DecisionStatus.Waiting
        "Considering" -> DecisionStatus.Considering
        "Saved" -> DecisionStatus.Saved
        "Skipped" -> DecisionStatus.Skipped
        else -> null
    }

    private fun toDecisionUi(item: ReconciledCandidate): DecisionUi {
        val statusLabel = when {
            item.isCoolingOffJustExpired -> "Vừa hết thời gian chờ"
            item.effectiveStatus == DecisionStatus.Ready -> "Đến hạn xem lại"
            item.effectiveStatus == DecisionStatus.Waiting -> "Đang chờ"
            item.effectiveStatus == DecisionStatus.Considering -> "Đang cân nhắc"
            item.effectiveStatus == DecisionStatus.Saved -> "Đã lưu"
            item.effectiveStatus == DecisionStatus.Skipped -> "Đã bỏ qua"
            else -> "Đến hạn xem lại"
        }

        val secondaryText = item.customSecondaryText ?: when {
            item.isCoolingOffJustExpired -> "Vừa hết thời gian chờ"
            item.effectiveStatus == DecisionStatus.Waiting -> item.remainingSeconds?.let(::formatRemainingDuration)
            else -> null
        }

        return DecisionUi(
            id = item.id,
            title = item.title,
            priceMinor = item.priceMinor,
            priceFormatted = HomeCurrencyFormatter.formatNullableVnd(item.priceMinor),
            status = item.effectiveStatus,
            verdict = item.verdict,
            statusLabel = statusLabel,
            secondaryText = secondaryText,
            remainingSeconds = item.remainingSeconds,
            thumbnailResId = item.thumbnailResId,
            isTimerExpiredLocally = item.isCoolingOffJustExpired
        )
    }

    fun formatRemainingDuration(remainingSeconds: Long): String {
        if (remainingSeconds <= 0L) {
            return "Vừa hết thời gian chờ"
        }
        val hours = remainingSeconds / 3600L
        val minutes = (remainingSeconds % 3600L) / 60L
        return when {
            hours > 0L && minutes > 0L -> "Còn $hours giờ $minutes phút"
            hours > 0L -> "Còn $hours giờ"
            else -> "Còn ${minutes.coerceAtLeast(1L)} phút"
        }
    }

    private data class ReconciledCandidate(
        val id: String,
        val title: String,
        val priceMinor: Long?,
        val effectiveStatus: DecisionStatus,
        val verdict: DecisionVerdict,
        val remainingSeconds: Long?,
        val customSecondaryText: String?,
        val thumbnailResId: Int?,
        val isCoolingOffJustExpired: Boolean
    )
}

data class DecisionRankingResult(
    val topDecisions: List<DecisionUi>,
    val totalValidCount: Int,
    val waitingItems: List<DecisionUi> = emptyList(),
    val reviewItems: List<DecisionUi> = emptyList(),
    val skippedItems: List<DecisionUi> = emptyList()
) {
    val waitingCount: Int get() = waitingItems.size
    val reviewCount: Int get() = reviewItems.size
    val skippedCount: Int get() = skippedItems.size
}

/**
 * Tính toán nhãn tháng/ngày động cho tờ lịch (S19).
 */
object CalendarDateResolver {
    fun resolve(
        nowIso: String,
        zoneIdString: String
    ): Pair<String, String> {
        return try {
            val instant = Instant.parse(nowIso)
            val zoneId = ZoneId.of(zoneIdString)
            val zonedDateTime = instant.atZone(zoneId)
            val month = zonedDateTime.month
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .uppercase(Locale.ENGLISH)
            val day = zonedDateTime.dayOfMonth.toString()
            month to day
        } catch (_: Exception) {
            "OCT" to "24"
        }
    }
}
