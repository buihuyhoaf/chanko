package com.hoabh.chanko.presentation.screens.budget.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R

@Composable
fun BudgetMascotSlot(
    isOverBudget: Boolean,
    isZeroSpend: Boolean,
    modifier: Modifier = Modifier
) {
    val mascotResId = when {
        isOverBudget -> R.drawable.mascot_worried
        isZeroSpend -> R.drawable.mascot_sleepy
        else -> R.drawable.mascot_happy
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = mascotResId),
            contentDescription = null, // Trang trí, không lặp lại thông tin bằng TalkBack
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(104.dp)
        )
    }
}
