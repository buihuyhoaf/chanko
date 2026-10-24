package com.hoabh.chanko.presentation.screens.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

/**
 * Trạng thái độc lập của từng khu vực dữ liệu trên màn hình Home (Section-aware Data State).
 * Lỗi ở một section không làm toàn bộ Home chuyển sang màn hình lỗi.
 */
@Immutable
sealed interface SectionState<out T> {
    data object Loading : SectionState<Nothing>

    data class Content<T>(
        val data: T,
        val isFromCache: Boolean = false
    ) : SectionState<T>

    data class Error(
        @param:StringRes val messageRes: Int
    ) : SectionState<Nothing>
}

enum class GreetingPeriod {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        fun fromHour(hour: Int): GreetingPeriod = when (hour) {
            in 5..11 -> MORNING
            in 12..17 -> AFTERNOON
            else -> EVENING
        }
    }
}

/**
 * Mô hình trạng thái cho HOME-01 — Header.
 */
@Immutable
data class HeaderUiModel(
    val userName: String = "",
    val greetingPeriod: GreetingPeriod = GreetingPeriod.MORNING,
    val isOffline: Boolean = false
) {
    val displayName: String
        get() = userName.trim()

    val hasName: Boolean
        get() = displayName.isNotEmpty()

    val avatarInitial: String
        get() = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: DEFAULT_AVATAR_INITIAL

    companion object {
        const val DEFAULT_AVATAR_INITIAL = "C"
    }
}

/**
 * Trạng thái tổng thể của màn hình Home theo Spec v1.1.
 * Các section P2–P6 sẽ được mở rộng dần theo lộ trình mà không phá vỡ hợp đồng P1.
 */
@Immutable
data class HomeUiState(
    val header: HeaderUiModel = HeaderUiModel(),
    val budgetSection: SectionState<Unit> = SectionState.Content(Unit),
    val decisionsSection: SectionState<Unit> = SectionState.Content(Unit),
    val isObservationVisible: Boolean = false,
    val isLocalDraftVisible: Boolean = false
)
