package com.hoabh.chanko.presentation.screens.home.domain

import androidx.annotation.DrawableRes
import com.hoabh.chanko.presentation.screens.home.BudgetStateKind
import com.hoabh.chanko.presentation.screens.home.BudgetUi
import com.hoabh.chanko.presentation.screens.home.DecisionStatus
import com.hoabh.chanko.presentation.screens.home.DecisionUi
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Định dạng tiền tệ VND xác định (Deterministic VND Formatter):
 * - Sử dụng dấu chấm phân tách hàng nghìn theo chuẩn hiển thị tiếng Việt (`2.400.000 ₫`).
 * - Hỗ trợ số âm (`-600.000 ₫`) cho trường hợp vượt ngân sách (S06).
 * - Không bao giờ cắt ngắn số tiền bằng dấu ba chấm (S15).
 */
object HomeCurrencyFormatter {
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
}

/**
 * Bộ tính toán ngân sách thuần Kotlin (HOME-04):
 * - Xử lý S01/S21 (Warning 80%), S06 (OverBudget 120%, clamp 1f, âm -600.000 ₫),
 *   S07 (Zero limit không chia cho 0), S08 (Unknown null không ép thành 0 ₫).
 */
object BudgetCalculator {
    private const val UNKNOWN_TEXT = "Chưa có dữ liệu"
    private const val ZERO_LIMIT_BADGE = "Chưa đặt hạn mức"
    private const val UNKNOWN_BADGE = "Chưa xác định"

    fun compute(
        cycleLabel: String,
        spentMinor: Long?,
        limitMinor: Long?
    ): BudgetUi {
        // S08: Unknown budget
        if (spentMinor == null || limitMinor == null) {
            return BudgetUi(
                cycleLabel = cycleLabel,
                spentMinor = spentMinor,
                limitMinor = limitMinor,
                remainingMinor = null,
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

        // S07: Zero or negative budget limit — never divide by zero
        if (limitMinor <= 0L) {
            val remaining = -spentMinor
            return BudgetUi(
                cycleLabel = cycleLabel,
                spentMinor = spentMinor,
                limitMinor = limitMinor,
                remainingMinor = remaining,
                ratio = null,
                percentageInt = null,
                visualProgress = 0f,
                stateKind = BudgetStateKind.Zero,
                spentFormatted = HomeCurrencyFormatter.formatVnd(spentMinor),
                limitFormatted = HomeCurrencyFormatter.formatVnd(0L),
                remainingFormatted = HomeCurrencyFormatter.formatVnd(remaining),
                badgeText = ZERO_LIMIT_BADGE,
                progressRightLabel = "0%"
            )
        }

        val remainingMinor = limitMinor - spentMinor
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
            BudgetStateKind.Normal -> "Đã tiêu $percentageInt%"
            BudgetStateKind.Warning -> "Đã tiêu $percentageInt%"
            BudgetStateKind.Reached -> "Chạm hạn mức 100%"
            BudgetStateKind.OverBudget -> "Vượt ngân sách $percentageInt%"
            BudgetStateKind.Zero -> ZERO_LIMIT_BADGE
            BudgetStateKind.Unknown -> UNKNOWN_BADGE
        }

        return BudgetUi(
            cycleLabel = cycleLabel,
            spentMinor = spentMinor,
            limitMinor = limitMinor,
            remainingMinor = remainingMinor,
            ratio = rawRatio.toFloat(),
            percentageInt = percentageInt,
            visualProgress = visualProgress,
            stateKind = stateKind,
            spentFormatted = HomeCurrencyFormatter.formatVnd(spentMinor),
            limitFormatted = HomeCurrencyFormatter.formatVnd(limitMinor),
            remainingFormatted = HomeCurrencyFormatter.formatVnd(remainingMinor),
            badgeText = badgeText,
            progressRightLabel = "$percentageInt%"
        )
    }
}

/**
 * Dữ liệu đầu vào thô của một món đồ trước khi qua bộ lọc & xếp hạng (HOME-05).
 */
data class RawDecisionCandidate(
    val id: String?,
    val title: String?,
    val priceMinor: Long,
    val statusRaw: String?,
    val remainingSeconds: Long? = null,
    val customSecondaryText: String? = null,
    @param:DrawableRes val thumbnailResId: Int? = null
)

/**
 * Bộ lọc và xếp hạng quyết định mua sắm (HOME-05):
 * - Loại bỏ ứng viên có id rỗng, title rỗng hoặc status không hợp lệ (S18).
 * - Khử trùng lặp theo id, giữ lần xuất hiện đầu tiên (S20).
 * - Không tự động chuyển Waiting có remainingSeconds <= 0 sang Ready (S12).
 * - Sắp xếp ổn định (stable sort) theo thứ tự ưu tiên: Ready -> Waiting -> Considering -> Saved,
 *   giữ nguyên thứ tự tương đối trong cùng một nhóm và lấy tối đa 3 món (S01–S04).
 */
object DecisionRanker {
    private const val MAX_VISIBLE_DECISIONS = 3

    fun rankAndSelectTop(
        candidates: List<RawDecisionCandidate>,
        maxCount: Int = MAX_VISIBLE_DECISIONS
    ): DecisionRankingResult {
        val validUniqueCandidates = LinkedHashMap<String, ValidatedCandidate>()

        for (candidate in candidates) {
            val cleanId = candidate.id?.trim().orEmpty()
            val cleanTitle = candidate.title?.trim().orEmpty()
            val status = parseStatus(candidate.statusRaw)

            // S18: Drop malformed candidates (blank ID, blank title, or unsupported status)
            if (cleanId.isEmpty() || cleanTitle.isEmpty() || status == null) {
                continue
            }

            // S20: Deduplicate by ID, keeping first occurrence
            if (!validUniqueCandidates.containsKey(cleanId)) {
                validUniqueCandidates[cleanId] = ValidatedCandidate(
                    id = cleanId,
                    title = cleanTitle,
                    priceMinor = candidate.priceMinor,
                    status = status,
                    remainingSeconds = candidate.remainingSeconds,
                    customSecondaryText = candidate.customSecondaryText,
                    thumbnailResId = candidate.thumbnailResId
                )
            }
        }

        val allValid = validUniqueCandidates.values.toList()
        // Kotlin's sortedBy is guaranteed to be a stable sort
        val sorted = allValid.sortedBy { it.status.priorityRank }
        val topItems = sorted.take(maxCount).map(::toDecisionUi)

        return DecisionRankingResult(
            topDecisions = topItems,
            totalValidCount = allValid.size
        )
    }

    private fun parseStatus(raw: String?): DecisionStatus? = when (raw?.trim()) {
        "Ready" -> DecisionStatus.Ready
        "Waiting" -> DecisionStatus.Waiting
        "Considering" -> DecisionStatus.Considering
        "Saved" -> DecisionStatus.Saved
        else -> null
    }

    private fun toDecisionUi(item: ValidatedCandidate): DecisionUi {
        // S12: Authoritative status is preserved even if local timer reaches <= 0
        val isExpiredLocally = item.status == DecisionStatus.Waiting &&
            item.remainingSeconds != null &&
            item.remainingSeconds <= 0L

        val statusLabel = when (item.status) {
            DecisionStatus.Ready -> "Sẵn sàng xem lại"
            DecisionStatus.Waiting -> "Đang chờ"
            DecisionStatus.Considering -> "Đang cân nhắc"
            DecisionStatus.Saved -> "Đã lưu"
        }

        val secondaryText = item.customSecondaryText ?: when (item.status) {
            DecisionStatus.Waiting -> item.remainingSeconds?.let(::formatRemainingDuration)
            else -> null
        }

        return DecisionUi(
            id = item.id,
            title = item.title,
            priceMinor = item.priceMinor,
            priceFormatted = HomeCurrencyFormatter.formatVnd(item.priceMinor),
            status = item.status,
            statusLabel = statusLabel,
            secondaryText = secondaryText,
            remainingSeconds = item.remainingSeconds,
            thumbnailResId = item.thumbnailResId,
            isTimerExpiredLocally = isExpiredLocally
        )
    }

    fun formatRemainingDuration(remainingSeconds: Long): String {
        if (remainingSeconds <= 0L) {
            return "Còn 0 giờ 0 phút"
        }
        val hours = remainingSeconds / 3600L
        val minutes = (remainingSeconds % 3600L) / 60L
        return when {
            hours > 0L && minutes > 0L -> "Còn $hours giờ $minutes phút"
            hours > 0L -> "Còn $hours giờ"
            else -> "Còn ${minutes.coerceAtLeast(1L)} phút"
        }
    }

    private data class ValidatedCandidate(
        val id: String,
        val title: String,
        val priceMinor: Long,
        val status: DecisionStatus,
        val remainingSeconds: Long?,
        val customSecondaryText: String?,
        val thumbnailResId: Int?
    )
}

data class DecisionRankingResult(
    val topDecisions: List<DecisionUi>,
    val totalValidCount: Int
)

/**
 * Tính toán nhãn tháng/ngày động cho tờ lịch trên bàn (S19).
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
