package com.hoabh.chanko.presentation.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.features.home.BudgetCardRole
import com.hoabh.chanko.presentation.features.home.BudgetCardSlotUi
import com.hoabh.chanko.presentation.features.home.BudgetCarouselUi
import com.hoabh.chanko.presentation.features.home.SectionState
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandySpace
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Khu vực 2 — Budget Credit Card Carousel 3 vị trí (Home v2.2 - BC22-01..BC22-24, BC-CARD-LABEL-01, BC-CARD-CONTENT-01):
 * - Tối đa 3 slots: [Previous? - Current - Next].
 * - Mặc định và khi vào lại Home: snap về thẻ Current (BC22-02).
 * - Vuốt ngang tối đa 1 trang mỗi lần vuốt, hé lộ mép thẻ (peek 16dp).
 * - Dải điều hướng bên dưới: Nút <, dải dots tím/xám, nút >, và liên kết "Xem tất cả →".
 * - Xử lý đầy đủ SectionState: Loading skeleton, Error + Retry, OfflineNoCache, Content.
 */
@Composable
fun BudgetCreditCardCarousel(
    carouselState: SectionState<BudgetCarouselUi>,
    onCardClick: (BudgetCardSlotUi) -> Unit,
    onSlotChanged: (slotKey: String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {},
    resetToCurrentTrigger: Any? = null
) {
    when (carouselState) {
        SectionState.Loading -> {
            BudgetCreditCardSkeleton(modifier = modifier)
        }

        is SectionState.OfflineNoCache -> {
            BudgetCreditCardStatusFrame(
                title = stringResource(R.string.home_budget_offline_no_cache_title),
                subtitle = stringResource(R.string.home_budget_offline_no_cache_subtitle),
                actionLabel = null,
                onCardClick = {},
                onActionClick = {},
                modifier = modifier
            )
        }

        is SectionState.Error -> {
            BudgetCreditCardStatusFrame(
                title = carouselState.message.ifBlank {
                    stringResource(R.string.home_section_error_budget)
                },
                subtitle = stringResource(R.string.home_budget_offline_no_cache_subtitle),
                actionLabel = stringResource(R.string.home_section_retry),
                onCardClick = onRetryClick,
                onActionClick = onRetryClick,
                modifier = modifier
            )
        }

        is SectionState.Empty -> {
            BudgetCreditCardStatusFrame(
                title = stringResource(R.string.home_budget_no_budget_title),
                subtitle = stringResource(R.string.home_budget_no_budget_subtitle),
                actionLabel = stringResource(R.string.home_budget_no_budget_cta),
                onCardClick = {},
                onActionClick = {},
                modifier = modifier
            )
        }

        is SectionState.Content -> {
            val carousel = carouselState.data
            val slots = carousel.slots

            if (slots.isEmpty()) {
                BudgetCreditCardStatusFrame(
                    title = stringResource(R.string.home_budget_no_budget_title),
                    subtitle = stringResource(R.string.home_budget_no_budget_subtitle),
                    actionLabel = stringResource(R.string.home_budget_no_budget_cta),
                    onCardClick = {},
                    onActionClick = {},
                    modifier = modifier
                )
                return
            }

            val currentIndex = slots.indexOfFirst { it.key == carousel.currentCycleKey }
                .takeIf { it >= 0 }
                ?: slots.indexOfFirst { it.role == BudgetCardRole.Current }
                .takeIf { it >= 0 }
                ?: 0

            val selectedIndex = slots.indexOfFirst { it.key == carousel.selectedSlotKey }
                .takeIf { it >= 0 }
                ?: currentIndex

            val pagerState = rememberPagerState(
                initialPage = selectedIndex,
                pageCount = { slots.size }
            )

            // Khi resetToCurrentTrigger thay đổi (ví dụ: vào lại Home hoặc Rollover), snap về Current (BC22-02)
            LaunchedEffect(resetToCurrentTrigger) {
                if (resetToCurrentTrigger != null && pagerState.currentPage != currentIndex) {
                    pagerState.animateScrollToPage(currentIndex)
                }
            }

            // Lắng nghe khi pager settle để thông báo slot đã đổi
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }
                    .distinctUntilChanged()
                    .collect { page ->
                        if (page in slots.indices) {
                            onSlotChanged(slots[page].key)
                        }
                    }
            }

            // Carousel dạng vuốt ngang tự nhiên (không còn dải nút <>, chấm chỉ báo hay Xem tất cả bên dưới)
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = CandyBudgetThemes.peek),
                pageSpacing = CandyBudgetThemes.pagerSpacing,
                beyondViewportPageCount = 1,
                modifier = modifier.fillMaxWidth()
            ) { page ->
                val slot = slots[page]
                BudgetCreditCardSlotView(
                    slot = slot,
                    onCardClick = { onCardClick(slot) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
