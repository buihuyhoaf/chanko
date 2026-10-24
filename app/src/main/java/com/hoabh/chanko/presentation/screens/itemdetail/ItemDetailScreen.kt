package com.hoabh.chanko.presentation.screens.itemdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.itemdetail.components.ChankoSpeechBubble
import com.hoabh.chanko.presentation.screens.itemdetail.components.CoolingOffCountdownCard
import com.hoabh.chanko.presentation.screens.itemdetail.components.DecisionJourneySection
import com.hoabh.chanko.presentation.screens.itemdetail.components.ExpandableDetailsSection
import com.hoabh.chanko.presentation.screens.itemdetail.components.ItemMoreOptionsBottomSheet
import com.hoabh.chanko.presentation.screens.itemdetail.components.ProductStageHero
import com.hoabh.chanko.presentation.screens.itemdetail.components.PurchaseHistorySection
import com.hoabh.chanko.presentation.screens.itemdetail.components.StickyBottomActionBar
import com.hoabh.chanko.presentation.screens.myitems.ItemDisplayStatus
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun ItemDetailRoute(
    viewModel: ItemDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onOpenRecordPurchase: (Int) -> Unit = {},
    onOpenTransactionDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is ItemDetailUiEffect.NavigateBack -> onNavigateBack()
            is ItemDetailUiEffect.OpenRecordPurchaseSheet -> onOpenRecordPurchase(effect.itemId)
            is ItemDetailUiEffect.OpenTransactionDetail -> onOpenTransactionDetail(effect.purchaseId)
            is ItemDetailUiEffect.ShowToast -> {
                // Toast / Snackbar thông báo
            }
        }
    }

    ItemDetailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack
    )
}

/**
 * Màn hình ItemDetail hoàn chỉnh theo Approved Recovery Baseline:
 * DETAIL-01: Pastel Candy Product Stage (dummy neutral)
 * DETAIL-02: Summary + expandable facts (Edit trong More)
 * DETAIL-03..06: Contextual speech bubble, countdown nổi bật, collapsible journey, sticky CTA + More
 * DETAIL-14: Purchase History section riêng
 */
@Composable
fun ItemDetailScreen(
    uiState: ItemDetailUiState,
    onEvent: (ItemDetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CandyColors.primary)
            }
            return
        }

        if (uiState.itemNotFound) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.item_detail_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CandyColors.textSecondary
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // App Bar: Back + Title + More
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = CandyColors.textPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.item_detail_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = CandyColors.textPrimary
                )
                IconButton(onClick = { onEvent(ItemDetailUiEvent.OpenMoreOptions) }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.item_detail_more_options),
                        tint = CandyColors.textPrimary
                    )
                }
            }

            // Scrollable Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = CandySpace.lg,
                    end = CandySpace.lg,
                    top = CandySpace.xs,
                    bottom = 100.dp // Dành chỗ cho Sticky Bottom Bar
                ),
                verticalArrangement = Arrangement.spacedBy(CandySpace.md)
            ) {
                // 1. Pastel Candy Product Stage
                item(key = "stage") {
                    ProductStageHero()
                }

                // 2. Title, Price, Status Chip
                item(key = "title_price") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = CandyColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(CandySpace.xs))
                            DetailStatusBadge(status = uiState.displayStatus)
                        }

                        Spacer(modifier = Modifier.height(CandySpace.xs))

                        Text(
                            text = uiState.formattedPrice,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = CandyColors.primary
                        )
                        Text(
                            text = stringResource(R.string.item_detail_price_unit),
                            style = MaterialTheme.typography.labelSmall,
                            color = CandyColors.textSecondary
                        )
                    }
                }

                // 3. Countdown Card (Khi đang trong thời gian chờ hoặc Ready)
                if (uiState.displayStatus == ItemDisplayStatus.CoolingOff || uiState.displayStatus == ItemDisplayStatus.ReadyForReview) {
                    item(key = "countdown") {
                        CoolingOffCountdownCard(
                            displayStatus = uiState.displayStatus,
                            remainingFormatted = uiState.remainingFormatted
                        )
                    }
                }

                // 4. Speech Bubble Section
                if (uiState.speechBubbleText.isNotBlank()) {
                    item(key = "speech_bubble") {
                        ChankoSpeechBubble(speechText = uiState.speechBubbleText)
                    }
                }

                // 5. Expandable Facts (Reason, Date, Duration)
                item(key = "expandable_details") {
                    ExpandableDetailsSection(
                        reason = uiState.reason,
                        addedDate = uiState.formattedAddedDate,
                        cooldownDuration = uiState.cooldownDurationFormatted
                    )
                }

                // 6. Decision Journey Section
                if (uiState.decisionJourney.isNotEmpty()) {
                    item(key = "decision_journey") {
                        DecisionJourneySection(steps = uiState.decisionJourney)
                    }
                }

                // 7. Purchase History Section
                item(key = "purchase_history") {
                    PurchaseHistorySection(
                        purchases = uiState.purchaseHistory,
                        onPurchaseClick = { purchaseId ->
                            onEvent(ItemDetailUiEvent.PurchaseItemClicked(purchaseId))
                        }
                    )
                }
            }
        }

        // Sticky Bottom Bar
        StickyBottomActionBar(
            primaryCtaText = uiState.primaryCtaText,
            displayStatus = uiState.displayStatus,
            onPrimaryCtaClick = { onEvent(ItemDetailUiEvent.PrimaryCtaClicked) },
            onMoreOptionsClick = { onEvent(ItemDetailUiEvent.OpenMoreOptions) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // More Options Bottom Sheet
        if (uiState.isMoreOptionsSheetOpen) {
            ItemMoreOptionsBottomSheet(
                displayStatus = uiState.displayStatus,
                onRecordPurchase = { onEvent(ItemDetailUiEvent.RequestRecordPurchase) },
                onSkipWaiting = { onEvent(ItemDetailUiEvent.RequestSkipWaiting) },
                onArchive = { onEvent(ItemDetailUiEvent.RequestArchiveItem) },
                onDismiss = { onEvent(ItemDetailUiEvent.DismissMoreOptions) }
            )
        }

        // Skip Waiting Confirmation Dialog (GAP-02)
        if (uiState.isSkipWaitingConfirmDialogOpen) {
            AlertDialog(
                onDismissRequest = { onEvent(ItemDetailUiEvent.DismissSkipWaitingDialog) },
                title = { Text(stringResource(R.string.item_detail_dialog_skip_title)) },
                text = { Text(stringResource(R.string.item_detail_dialog_skip_msg)) },
                confirmButton = {
                    TextButton(onClick = { onEvent(ItemDetailUiEvent.ConfirmSkipWaiting) }) {
                        Text(
                            text = stringResource(R.string.item_detail_dialog_skip_confirm),
                            color = CandyColors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(ItemDetailUiEvent.DismissSkipWaitingDialog) }) {
                        Text(stringResource(R.string.item_detail_dialog_cancel))
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun DetailStatusBadge(
    status: ItemDisplayStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(status.containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.labelText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            ),
            color = status.contentColor
        )
    }
}
