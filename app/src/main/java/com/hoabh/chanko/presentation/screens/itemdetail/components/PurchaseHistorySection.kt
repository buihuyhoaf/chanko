package com.hoabh.chanko.presentation.screens.itemdetail.components

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
import com.hoabh.chanko.presentation.screens.itemdetail.PurchaseHistoryItemUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * DETAIL-14: Purchase History là section riêng trong Item Detail.
 */
@Composable
fun PurchaseHistorySection(
    purchases: List<PurchaseHistoryItemUi>,
    onPurchaseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.md)
        ) {
            Text(
                text = stringResource(R.string.item_detail_purchase_history_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = CandyColors.textPrimary
            )

            Spacer(modifier = Modifier.height(CandySpace.sm))

            if (purchases.isEmpty()) {
                Text(
                    text = stringResource(R.string.item_detail_purchase_history_empty),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp
                    ),
                    color = CandyColors.textSecondary,
                    modifier = Modifier.padding(vertical = CandySpace.xs)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
                    purchases.forEach { purchase ->
                        PurchaseHistoryRow(
                            item = purchase,
                            onClick = { onPurchaseClick(purchase.purchaseId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseHistoryRow(
    item: PurchaseHistoryItemUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = CandySpace.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = item.formattedDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = CandyColors.textPrimary
            )
            Text(
                text = "${item.actualUnitPriceFormatted} × ${item.quantity}",
                style = MaterialTheme.typography.labelSmall,
                color = CandyColors.textSecondary
            )
        }
        Text(
            text = item.totalAmountFormatted,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = CandyColors.primary
        )
    }
}
