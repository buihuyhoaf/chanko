package com.hoabh.chanko.presentation.screens.itemdetail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * DETAIL-02: Expandable item details (Lý do muốn mua, ngày thêm vào, thời gian chờ).
 */
@Composable
fun ExpandableDetailsSection(
    reason: String,
    addedDate: String,
    cooldownDuration: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.item_detail_expandable_header),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = CandyColors.textPrimary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = CandyColors.textSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = CandySpace.sm),
                    verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                ) {
                    DetailRow(
                        label = stringResource(R.string.item_detail_field_reason),
                        value = reason
                    )
                    DetailRow(
                        label = stringResource(R.string.item_detail_field_date),
                        value = addedDate
                    )
                    DetailRow(
                        label = stringResource(R.string.item_detail_field_duration),
                        value = cooldownDuration
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = CandyColors.textSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = CandyColors.textPrimary
        )
    }
}
