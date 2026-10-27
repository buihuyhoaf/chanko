package com.hoabh.chanko.presentation.screens.myitems.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.myitems.ActiveSubFilter
import com.hoabh.chanko.presentation.screens.myitems.ItemSortOption
import com.hoabh.chanko.presentation.screens.myitems.PrimaryFilter
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * Filter & Sort Bottom Sheet:
 * ITEM-04: Lọc 2 cấp (Cấp 1: All/Active/Purchased/Skipped; Cấp 2 trong Active) + Sắp xếp.
 * Sheet top radius 28dp, Apply và Reset footer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortBottomSheet(
    currentPrimary: PrimaryFilter,
    currentSubFilter: ActiveSubFilter,
    currentSort: ItemSortOption,
    onApply: (PrimaryFilter, ActiveSubFilter, ItemSortOption) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tempPrimary by remember { mutableStateOf(currentPrimary) }
    var tempSubFilter by remember { mutableStateOf(currentSubFilter) }
    var tempSort by remember { mutableStateOf(currentSort) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.my_items_filter_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    ),
                    color = CandyColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    ChankoIcon(
                        id = ChankoIconId.ACT_03,
                        contentDescription = stringResource(R.string.my_items_filter_close)
                    )
                }
            }

            Spacer(modifier = Modifier.height(CandySpace.md))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Primary Filter Section
                Text(
                    text = stringResource(R.string.my_items_filter_status_primary),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(CandySpace.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    FilterChipItem(
                        label = stringResource(R.string.my_items_filter_all),
                        isSelected = tempPrimary == PrimaryFilter.ALL,
                        onClick = {
                            tempPrimary = PrimaryFilter.ALL
                            tempSubFilter = ActiveSubFilter.ALL_ACTIVE
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChipItem(
                        label = stringResource(R.string.my_items_filter_active),
                        isSelected = tempPrimary == PrimaryFilter.ACTIVE,
                        onClick = { tempPrimary = PrimaryFilter.ACTIVE },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChipItem(
                        label = stringResource(R.string.my_items_status_purchased),
                        isSelected = tempPrimary == PrimaryFilter.PURCHASED,
                        onClick = {
                            tempPrimary = PrimaryFilter.PURCHASED
                            tempSubFilter = ActiveSubFilter.ALL_ACTIVE
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChipItem(
                        label = stringResource(R.string.my_items_status_skipped),
                        isSelected = tempPrimary == PrimaryFilter.SKIPPED,
                        onClick = {
                            tempPrimary = PrimaryFilter.SKIPPED
                            tempSubFilter = ActiveSubFilter.ALL_ACTIVE
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sub-filter section (Khi chọn Active)
                if (tempPrimary == PrimaryFilter.ACTIVE) {
                    Spacer(modifier = Modifier.height(CandySpace.md))
                    Text(
                        text = stringResource(R.string.my_items_filter_status_sub),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = CandyColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(CandySpace.sm))

                    Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                        ) {
                            FilterChipItem(
                                label = stringResource(R.string.my_items_filter_all),
                                isSelected = tempSubFilter == ActiveSubFilter.ALL_ACTIVE,
                                onClick = { tempSubFilter = ActiveSubFilter.ALL_ACTIVE },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChipItem(
                                label = stringResource(R.string.my_items_status_cooling),
                                isSelected = tempSubFilter == ActiveSubFilter.COOLING_OFF,
                                onClick = { tempSubFilter = ActiveSubFilter.COOLING_OFF },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChipItem(
                                label = stringResource(R.string.my_items_status_ready),
                                isSelected = tempSubFilter == ActiveSubFilter.READY,
                                onClick = { tempSubFilter = ActiveSubFilter.READY },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
                        ) {
                            FilterChipItem(
                                label = stringResource(R.string.my_items_status_saved),
                                isSelected = tempSubFilter == ActiveSubFilter.SAVED,
                                onClick = { tempSubFilter = ActiveSubFilter.SAVED },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChipItem(
                                label = stringResource(R.string.my_items_status_considering),
                                isSelected = tempSubFilter == ActiveSubFilter.CONSIDERING,
                                onClick = { tempSubFilter = ActiveSubFilter.CONSIDERING },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChipItem(
                                label = stringResource(R.string.my_items_status_postponed),
                                isSelected = tempSubFilter == ActiveSubFilter.POSTPONED,
                                onClick = { tempSubFilter = ActiveSubFilter.POSTPONED },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(CandySpace.md))

                // Sort Options Section
                Text(
                    text = stringResource(R.string.my_items_filter_sort),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = CandyColors.textPrimary
                )
                Spacer(modifier = Modifier.height(CandySpace.sm))

                Column(verticalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                    SortItem(
                        label = stringResource(R.string.my_items_sort_updated),
                        isSelected = tempSort == ItemSortOption.UPDATED_RECENTLY,
                        onClick = { tempSort = ItemSortOption.UPDATED_RECENTLY }
                    )
                    SortItem(
                        label = stringResource(R.string.my_items_sort_newest),
                        isSelected = tempSort == ItemSortOption.NEWEST_ADDED,
                        onClick = { tempSort = ItemSortOption.NEWEST_ADDED }
                    )
                    SortItem(
                        label = stringResource(R.string.my_items_sort_price_asc),
                        isSelected = tempSort == ItemSortOption.PRICE_ASC,
                        onClick = { tempSort = ItemSortOption.PRICE_ASC }
                    )
                    SortItem(
                        label = stringResource(R.string.my_items_sort_price_desc),
                        isSelected = tempSort == ItemSortOption.PRICE_DESC,
                        onClick = { tempSort = ItemSortOption.PRICE_DESC }
                    )
                    SortItem(
                        label = stringResource(R.string.my_items_sort_name_az),
                        isSelected = tempSort == ItemSortOption.NAME_AZ,
                        onClick = { tempSort = ItemSortOption.NAME_AZ }
                    )
                }

                Spacer(modifier = Modifier.height(CandySpace.lg))
            }

            // Footer Actions: Đặt lại & Áp dụng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CandySpace.md),
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                OutlinedButton(
                    onClick = {
                        tempPrimary = PrimaryFilter.ALL
                        tempSubFilter = ActiveSubFilter.ALL_ACTIVE
                        tempSort = ItemSortOption.UPDATED_RECENTLY
                        onReset()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.my_items_filter_reset),
                        color = CandyColors.textPrimary
                    )
                }
                Button(
                    onClick = {
                        onApply(tempPrimary, tempSubFilter, tempSort)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.primary,
                        contentColor = CandyColors.textPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.my_items_filter_apply),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) CandyColors.primarySoft else CandyColors.background,
        border = if (isSelected) BorderStroke(1.5.dp, CandyColors.primary) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (isSelected) CandyColors.primaryInk else CandyColors.textPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SortItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) CandyColors.primarySoft else CandyColors.background,
        border = if (isSelected) BorderStroke(1.dp, CandyColors.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                ),
                color = if (isSelected) CandyColors.primaryInk else CandyColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
