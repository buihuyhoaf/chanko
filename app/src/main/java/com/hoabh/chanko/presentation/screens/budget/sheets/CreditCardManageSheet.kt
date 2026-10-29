package com.hoabh.chanko.presentation.screens.budget.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.CreditCard
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

@Composable
fun CreditCardManageSheet(
    cards: List<CreditCard>,
    canWrite: Boolean,
    onAddCardClick: () -> Unit,
    onRenameCardClick: (CreditCard) -> Unit,
    onDeleteCardClick: (CreditCard) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(CandySpace.md),
        verticalArrangement = Arrangement.spacedBy(CandySpace.md)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.budget_card_manage_title),
                fontFamily = Nunito,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.textPrimary
            )
            Text(
                text = stringResource(R.string.budget_btn_close),
                fontFamily = Nunito,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CandyColors.brandPurple,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onClose)
                    .padding(CandySpace.xs)
            )
        }

        // Helper Note
        Text(
            text = stringResource(R.string.budget_card_manage_helper),
            fontFamily = Nunito,
            fontSize = 12.sp,
            color = CandyColors.textSecondary
        )

        // Cards List
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = CandySpace.lg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_card_manage_empty),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    color = CandyColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                items(cards, key = { it.id }) { card ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CandyColors.budgetSnapshotSpendingPanel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CandySpace.md, vertical = CandySpace.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = card.name,
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandyColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)) {
                                // Rename
                                IconButton(
                                    onClick = { onRenameCardClick(card) },
                                    enabled = canWrite,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = "Đổi tên ${card.name}"
                                        }
                                ) {
                                    ChankoIcon(
                                        id = ChankoIconId.ACT_05,
                                        contentDescription = null,
                                        enabled = canWrite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Delete
                                IconButton(
                                    onClick = { onDeleteCardClick(card) },
                                    enabled = canWrite,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .semantics {
                                            role = Role.Button
                                            contentDescription = "Xóa ${card.name}"
                                        }
                                ) {
                                    ChankoIcon(
                                        id = ChankoIconId.ACT_06,
                                        contentDescription = null,
                                        enabled = canWrite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Footer Action: Thêm thẻ
        Surface(
            shape = CircleShape,
            color = if (canWrite) CandyColors.brandPurple else CandyColors.divider,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics { role = Role.Button }
                .clickable(enabled = canWrite, role = Role.Button, onClick = onAddCardClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.budget_card_action_add),
                    fontFamily = Nunito,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canWrite) Color.White else CandyColors.textSecondary
                )
            }
        }
    }
}
