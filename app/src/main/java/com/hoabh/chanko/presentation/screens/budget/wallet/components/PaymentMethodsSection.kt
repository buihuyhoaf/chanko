package com.hoabh.chanko.presentation.screens.budget.wallet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.CardColor
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors

/** Payment catalog with a fixed system method and editable credit cards. */
@Composable
fun PaymentMethodsSection(
    cards: List<CreditCard>,
    canWrite: Boolean,
    onAddCardClick: () -> Unit,
    onRenameCardClick: (CreditCard) -> Unit,
    onDeleteCardClick: (CreditCard) -> Unit,
    modifier: Modifier = Modifier,
    disabledReason: String? = null
) {
    var showDescription by rememberSaveable { mutableStateOf(false) }
    val title = stringResource(R.string.budget_payment_breakdown_title)

    if (showDescription) {
        AlertDialog(
            onDismissRequest = { showDescription = false },
            title = { Text(title) },
            text = { Text(stringResource(R.string.budget_payment_description)) },
            confirmButton = {
                TextButton(onClick = { showDescription = false }) {
                    Text(stringResource(R.string.budget_payment_description_ok))
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CandyColors.divider.copy(alpha = 0.6f))
    ) {
        Box {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // The help control occupies the corner; the title has its own width.
                Text(
                    text = title,
                    modifier = Modifier.fillMaxWidth().padding(end = 32.dp).semantics { heading() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CandyColors.textPrimary
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = CandyColors.calendarInnerSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(MaterialTheme.shapes.medium)
                                .background(CandyColors.primarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            ChankoIcon(ChankoIconId.BUD_01, contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.budget_payment_pay_now),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CandyColors.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.budget_payment_pay_now_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = CandyColors.textSecondary
                            )
                            Surface(shape = CircleShape, color = CandyColors.primarySoft) {
                                Text(
                                    text = stringResource(R.string.budget_payment_default),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CandyColors.primaryInk
                                )
                            }
                        }
                    }
                }

                val activeCards = cards.filterNot { it.isArchived }
                if (activeCards.isEmpty()) {
                    Text(
                        text = stringResource(R.string.budget_payment_no_cards),
                        style = MaterialTheme.typography.bodySmall,
                        color = CandyColors.textSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Column {
                        activeCards.forEachIndexed { index, card ->
                            if (index > 0) {
                                HorizontalDivider(color = CandyColors.divider.copy(alpha = 0.5f))
                            }
                            PaymentCardRow(
                                card = card,
                                canWrite = canWrite,
                                onRename = { onRenameCardClick(card) },
                                onDelete = { onDeleteCardClick(card) }
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onAddCardClick,
                    enabled = canWrite,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, if (canWrite) CandyColors.divider else CandyColors.progressTrack)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ACT_01,
                        enabled = canWrite,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.budget_card_action_add),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (canWrite) CandyColors.primaryInk else CandyColors.textSecondary
                    )
                }

                if (!canWrite && !disabledReason.isNullOrBlank()) {
                    Text(disabledReason, style = MaterialTheme.typography.bodySmall, color = CandyColors.textSecondary)
                }
            }
            IconButton(
                onClick = { showDescription = true },
                modifier = Modifier.align(Alignment.TopEnd).size(48.dp)
            ) {
                ChankoIcon(
                    id = ChankoIconId.SYS_16,
                    contentDescription = stringResource(R.string.budget_payment_description_action),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentCardRow(
    card: CreditCard,
    canWrite: Boolean,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniCardGraphic(color = card.color)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(card.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CandyColors.textPrimary)
            Text(stringResource(R.string.budget_payment_credit_card_label), style = MaterialTheme.typography.bodySmall, color = CandyColors.textSecondary)
        }
        IconButton(onClick = onRename, enabled = canWrite, modifier = Modifier.size(48.dp)) {
            ChankoIcon(
                id = ChankoIconId.ACT_05,
                enabled = canWrite,
                contentDescription = stringResource(R.string.budget_card_rename_action_format, card.name),
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onDelete, enabled = canWrite, modifier = Modifier.size(48.dp)) {
            ChankoIcon(
                id = ChankoIconId.ACT_06,
                enabled = canWrite,
                contentDescription = stringResource(R.string.budget_card_delete_action_format, card.name),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MiniCardGraphic(color: CardColor, modifier: Modifier = Modifier) {
    val baseColor = Color(color.colorLong)
    Box(
        modifier = modifier.size(width = 42.dp, height = 30.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Brush.linearGradient(listOf(baseColor.copy(alpha = 0.65f), baseColor)))
            .padding(7.dp)
    ) {
        Box(
            modifier = Modifier.size(width = 10.dp, height = 8.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f))
        )
        Box(
            modifier = Modifier.size(6.dp).align(Alignment.BottomEnd).clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f))
        )
    }
}
