package com.hoabh.chanko.presentation.screens.itemdetail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.hoabh.chanko.presentation.screens.itemdetail.DecisionJourneyStepUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * DETAIL-05: Collapsible Decision Journey dựa trên lịch sử thật.
 */
@Composable
fun DecisionJourneySection(
    steps: List<DecisionJourneyStepUi>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displaySteps = if (isExpanded) steps else steps.take(1)

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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.item_detail_decision_journey_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = CandyColors.textPrimary
                )
                if (steps.size > 1) {
                    Row(
                        modifier = Modifier.clickable { isExpanded = !isExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) stringResource(R.string.item_detail_decision_journey_collapse) else stringResource(R.string.item_detail_decision_journey_view_more),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = CandyColors.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = CandyColors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CandySpace.sm))

            Column(verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
                displaySteps.forEachIndexed { index, step ->
                    JourneyStepRow(
                        step = step,
                        isLast = index == displaySteps.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyStepRow(
    step: DecisionJourneyStepUi,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(CandyColors.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(CandyColors.divider)
                )
            }
        }

        Spacer(modifier = Modifier.width(CandySpace.sm))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = CandyColors.textPrimary
                )
                Text(
                    text = step.formattedTimestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = CandyColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = CandyColors.textSecondary
            )
        }
    }
}
