package com.hoabh.chanko.presentation.features.additem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.domain.policy.FreezeDurationPolicy
import com.hoabh.chanko.presentation.designsystem.icon.*
import com.hoabh.chanko.ui.theme.*

@Composable
fun DecisionResultScreen(form: AddItemFormState, onEdit: ((AddItemFormState) -> AddItemFormState) -> Unit) {
    ReviewCard(Modifier.padding(top = CandySpace.lg)) { ProductSummary(form) }
    val snapshot = form.assessment
    if (snapshot == null || snapshot.productRevision != form.productRevision || snapshot.answerRevision != form.answerRevision) {
        Spacer(Modifier.height(CandySpace.section))
        Text(stringResource(R.string.add_item_assessment_unavailable), style = MaterialTheme.typography.titleLarge)
        HelpText(stringResource(R.string.add_item_assessment_help))
        return
    }

    val price = form.price ?: 0L
    val durationDays = FreezeDurationPolicy.calculateDurationDays(price)

    Spacer(Modifier.height(CandySpace.section))
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CandySpace.md),
        color = CandyColors.blue
    ) {
        Row(
            Modifier.padding(horizontal = CandySpace.md, vertical = CandySpace.sm),
            horizontalArrangement = Arrangement.spacedBy(CandySpace.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChankoIcon(id = ChankoIconId.ITM_10, contentDescription = null)
            Text(
                stringResource(R.string.add_item_freeze_badge, durationDays),
                style = MaterialTheme.typography.labelLarge,
                color = CandyColors.textPrimary
            )
        }
    }

    Spacer(Modifier.height(CandySpace.section))
    Text(
        stringResource(R.string.add_item_freeze_title),
        style = MaterialTheme.typography.headlineMedium,
        color = CandyColors.textPrimary,
        modifier = Modifier.semantics { heading() }
    )

    Spacer(Modifier.height(CandySpace.section))
    MascotPlaceholder()

    Spacer(Modifier.height(CandySpace.section))
    ReviewCard(Modifier.padding(top = CandySpace.sm)) {
        Text(
            stringResource(R.string.add_item_freeze_duration_label),
            style = MaterialTheme.typography.titleMedium,
            color = CandyColors.textPrimary
        )
        Text(
            text = "$durationDays ngày (${durationDays * 24} giờ)",
            style = MaterialTheme.typography.bodyLarge,
            color = CandyColors.textPrimary,
            modifier = Modifier.padding(top = CandySpace.xs)
        )

        Spacer(Modifier.height(CandySpace.md))
        Text(
            stringResource(R.string.add_item_freeze_locked_price),
            style = MaterialTheme.typography.titleMedium,
            color = CandyColors.textPrimary
        )
        Text(
            text = formatVnd(price),
            style = MaterialTheme.typography.bodyLarge,
            color = CandyColors.textPrimary,
            modifier = Modifier.padding(top = CandySpace.xs)
        )

        Spacer(Modifier.height(CandySpace.md))
        Surface(
            shape = CandyShape.input,
            color = CandyColors.primarySoft
        ) {
            Column(
                Modifier.fillMaxWidth().padding(CandySpace.md),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                Text(
                    stringResource(R.string.add_item_freeze_reward_hint),
                    style = MaterialTheme.typography.labelLarge,
                    color = CandyColors.textPrimary
                )
                Text(
                    stringResource(R.string.add_item_freeze_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = CandyColors.textSecondary
                )
            }
        }
    }

    HelpText(stringResource(R.string.add_item_freeze_no_skip_hint), Modifier.padding(top = CandySpace.section))
    HelpText(stringResource(R.string.add_item_not_purchase), Modifier.padding(top = CandySpace.sm))
}
