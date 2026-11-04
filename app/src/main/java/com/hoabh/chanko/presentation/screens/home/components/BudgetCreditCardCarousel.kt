package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.BudgetCardRole
import com.hoabh.chanko.presentation.screens.home.BudgetCardSlotUi
import com.hoabh.chanko.presentation.screens.home.BudgetCarouselUi
import com.hoabh.chanko.presentation.screens.home.SectionState
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Khu vực 2 — Budget Credit Card Carousel 3 vị trí (Home v2.2 - BC22-01..BC22-24):
 * - Tối đa 3 slots: [Previous? - Current - Next].
 * - Mặc định và khi vào lại Home: snap về thẻ Current (BC22-02).
 * - Vuốt ngang tối đa 1 trang mỗi lần vuốt, hé lộ mép thẻ (peek 16-24dp).
 * - Xử lý đầy đủ SectionState: Loading skeleton, Error + Retry, OfflineNoCache, Content.
 */
@Composable
fun BudgetCreditCardCarousel(
    carouselState: SectionState<BudgetCarouselUi>,
    onCardClick: (BudgetCardSlotUi) -> Unit,
    onSlotChanged: (slotKey: String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
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

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 12.dp,
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
