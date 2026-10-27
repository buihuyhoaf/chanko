package com.hoabh.chanko.presentation.screens.itemdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.hoabh.chanko.presentation.screens.myitems.ItemDisplayStatus
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

/**
 * More Options Bottom Sheet cho Item Detail:
 * DETAIL-07, DETAIL-13, EDGE-04, GAP-02:
 * Gồm: Ghi nhận mua hàng / Mua lại, Bỏ qua thời gian chờ (khi đang CoolingOff), Lưu trữ món đồ.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemMoreOptionsBottomSheet(
    displayStatus: ItemDisplayStatus,
    onRecordPurchase: () -> Unit,
    onSkipWaiting: () -> Unit,
    onArchive: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onChatWithChanko: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CandySpace.lg)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.item_detail_more_options),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                ),
                color = CandyColors.textPrimary
            )

            Spacer(modifier = Modifier.height(CandySpace.md))

            // Cân nhắc cùng Chanko (Chat with Chanko about this item)
            if (onChatWithChanko != null) {
                MoreOptionRow(
                    icon = ChankoIconId.SYS_01,
                    label = "Cân nhắc cùng Chanko",
                    onClick = onChatWithChanko
                )
            }

            // Direct Mark as Purchased / Buy Again
            val isPurchased = displayStatus == ItemDisplayStatus.Purchased
            MoreOptionRow(
                icon = if (isPurchased) ChankoIconId.ACT_11 else ChankoIconId.ITM_09,
                label = if (isPurchased) stringResource(R.string.item_detail_more_buy_again) else stringResource(R.string.item_detail_more_record_purchase),
                onClick = onRecordPurchase
            )

            // Skip Waiting (chỉ khi đang hạ nhiệt)
            if (displayStatus == ItemDisplayStatus.CoolingOff) {
                MoreOptionRow(
                    icon = ChankoIconId.ITM_11,
                    label = stringResource(R.string.item_detail_more_skip_waiting),
                    onClick = onSkipWaiting
                )
            }

            // Archive Item
            MoreOptionRow(
                icon = ChankoIconId.ITM_06,
                label = stringResource(R.string.item_detail_more_archive),
                onClick = onArchive
            )

            Spacer(modifier = Modifier.height(CandySpace.lg))
        }
    }
}

@Composable
private fun MoreOptionRow(
    icon: ChankoIconId,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = CandySpace.sm, horizontal = CandySpace.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CandyColors.primarySoft)
                    .padding(8.dp)
            ) {
                ChankoIcon(
                    id = icon,
                    contentDescription = null,
                    size = 20.dp
                )
            }
            Spacer(modifier = Modifier.width(CandySpace.md))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = CandyColors.textPrimary
            )
        }
    }
}
