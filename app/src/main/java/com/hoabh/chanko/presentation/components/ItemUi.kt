package com.hoabh.chanko.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hoabh.chanko.R
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.domain.model.CooldownItem
import java.text.NumberFormat
import java.util.Locale

fun formatMoney(value: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).apply {
    maximumFractionDigits = 0
}.format(value)

@OptIn(ExperimentalMaterial3Api::class)
/** Caller owns navigation; modifier applies to the page scaffold, outside content insets. */
@Composable
fun ItemPage(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text(title) }, navigationIcon = {
        IconButton(onClick = onBack) {
            ChankoIcon(
                id = ChankoIconId.ACT_02,
                contentDescription = stringResource(R.string.back)
            )
        }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.md), content = content)
    }
}

@Composable
fun ItemRow(item: CooldownItem, onClick: (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
        Column(Modifier.padding(CandySpace.lg)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(formatMoney(item.price))
        }
    }
}
