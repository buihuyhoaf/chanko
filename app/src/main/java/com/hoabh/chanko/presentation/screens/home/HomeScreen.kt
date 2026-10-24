package com.hoabh.chanko.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.ChankoTheme

@Composable
fun HomeRoute(
    onAddNewItemClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    HomeScreen(modifier = modifier)
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CandyColors.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_placeholder_title),
            style = MaterialTheme.typography.bodyLarge,
            color = CandyColors.textPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ChankoTheme {
        HomeScreen()
    }
}
