package com.hoabh.chanko.presentation.screens.archived

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.base.CollectEffect
import com.hoabh.chanko.presentation.screens.myitems.MyItemCardUi
import com.hoabh.chanko.ui.mascot.ChankoMascot
import com.hoabh.chanko.ui.mascot.MascotPresentationState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

@Composable
fun ArchivedItemsRoute(
    viewModel: ArchivedItemsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is ArchivedItemsUiEffect.NavigateBack -> onNavigateBack()
            is ArchivedItemsUiEffect.NavigateToDetail -> onNavigateToDetail(effect.itemId)
            is ArchivedItemsUiEffect.ShowToast -> {}
        }
    }

    ArchivedItemsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack
    )
}

/**
 * GAP-03: Archived Items Screen
 * Danh sách món đồ đã lưu trữ, tìm kiếm và nút Khôi phục (Restore).
 */
@Composable
fun ArchivedItemsScreen(
    uiState: ArchivedItemsUiState,
    onEvent: (ArchivedItemsUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = CandyColors.textPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.archived_items_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = CandyColors.textPrimary
                )
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(ArchivedItemsUiEvent.SearchQueryChanged(it)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.archived_items_search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CandyColors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = CandyColors.textSecondary)
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = CandyColors.primary,
                    unfocusedBorderColor = CandyColors.divider
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CandySpace.lg, vertical = CandySpace.sm)
            )

            // Content List
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CandyColors.primary)
                }
            } else if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(CandySpace.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ChankoMascot(state = MascotPresentationState.Sleeping)
                        Spacer(modifier = Modifier.height(CandySpace.md))
                        Text(
                            text = stringResource(R.string.archived_items_empty_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CandyColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(CandySpace.xs))
                        Text(
                            text = stringResource(R.string.archived_items_empty_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CandyColors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(CandySpace.lg),
                    verticalArrangement = Arrangement.spacedBy(CandySpace.md)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        ArchivedItemRow(
                            item = item,
                            onClick = { onEvent(ArchivedItemsUiEvent.ItemClicked(item.id)) },
                            onRestore = { onEvent(ArchivedItemsUiEvent.RestoreItem(item.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedItemRow(
    item: MyItemCardUi,
    onClick: () -> Unit,
    onRestore: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CandyColors.background)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_product_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.formattedPrice,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Đã lưu trữ",
                    style = MaterialTheme.typography.labelSmall,
                    color = CandyColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(CandySpace.sm))

            Button(
                onClick = onRestore,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.primarySoft,
                    contentColor = CandyColors.primaryInk
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.archived_items_restore_btn),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
