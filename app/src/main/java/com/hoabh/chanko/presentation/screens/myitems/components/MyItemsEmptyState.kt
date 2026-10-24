package com.hoabh.chanko.presentation.screens.myitems.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.mascot.ChankoMascot
import com.hoabh.chanko.ui.mascot.MascotPresentationState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * ITEM-06: Empty State với Chanko và hộp kẹo; có Clear Filters theo ngữ cảnh.
 */
@Composable
fun MyItemsEmptyState(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CandySpace.xxl, horizontal = CandySpace.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ChankoMascot(
            state = if (isFiltered) MascotPresentationState.Curious else MascotPresentationState.Sleeping,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(CandySpace.md))

        Text(
            text = if (isFiltered) {
                stringResource(R.string.my_items_empty_filtered_title)
            } else {
                stringResource(R.string.my_items_empty_title)
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            ),
            color = CandyColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CandySpace.xs))

        Text(
            text = if (isFiltered) {
                stringResource(R.string.my_items_empty_filtered_description)
            } else {
                stringResource(R.string.my_items_empty_description)
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = CandyColors.textSecondary,
            textAlign = TextAlign.Center
        )

        if (isFiltered) {
            Spacer(modifier = Modifier.height(CandySpace.lg))
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.primary,
                    contentColor = CandyColors.textPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.my_items_empty_clear_filter),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
