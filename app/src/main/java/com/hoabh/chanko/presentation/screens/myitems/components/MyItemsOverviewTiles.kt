package com.hoabh.chanko.presentation.screens.myitems.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.hoabh.chanko.presentation.screens.myitems.OverviewTileType
import com.hoabh.chanko.presentation.screens.myitems.PrimaryFilter
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * ITEM-03: Ba overview tiles: All Items, Cooling Off, Ready; nhấn để lọc tương ứng.
 */
@Composable
fun MyItemsOverviewTiles(
    totalCount: Int,
    coolingOffCount: Int,
    readyCount: Int,
    selectedPrimary: PrimaryFilter,
    selectedSubFilter: ActiveSubFilter,
    onTileClick: (OverviewTileType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAllSelected = selectedPrimary == PrimaryFilter.ALL
    val isCoolingSelected = selectedPrimary == PrimaryFilter.ACTIVE && selectedSubFilter == ActiveSubFilter.COOLING_OFF
    val isReadySelected = selectedPrimary == PrimaryFilter.ACTIVE && selectedSubFilter == ActiveSubFilter.READY

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        OverviewTile(
            count = totalCount,
            label = stringResource(R.string.my_items_tile_all),
            isSelected = isAllSelected,
            containerColor = CandyColors.calendarSurface,
            selectedBorderColor = CandyColors.primary,
            modifier = Modifier.weight(1f),
            onClick = { onTileClick(OverviewTileType.ALL_ITEMS) }
        )
        OverviewTile(
            count = coolingOffCount,
            label = stringResource(R.string.my_items_tile_cooling),
            isSelected = isCoolingSelected,
            containerColor = CandyColors.blue.copy(alpha = 0.15f),
            selectedBorderColor = Color(0xFF1B5A8A),
            modifier = Modifier.weight(1f),
            onClick = { onTileClick(OverviewTileType.COOLING_OFF) }
        )
        OverviewTile(
            count = readyCount,
            label = stringResource(R.string.my_items_tile_ready),
            isSelected = isReadySelected,
            containerColor = CandyColors.readySurface,
            selectedBorderColor = CandyColors.readyAction,
            modifier = Modifier.weight(1f),
            onClick = { onTileClick(OverviewTileType.READY) }
        )
    }
}

@Composable
private fun OverviewTile(
    count: Int,
    label: String,
    isSelected: Boolean,
    containerColor: Color,
    selectedBorderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = if (isSelected) BorderStroke(2.dp, selectedBorderColor) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = CandySpace.sm, vertical = CandySpace.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = CandyColors.textSecondary,
                maxLines = 1
            )
        }
    }
}
