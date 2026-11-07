package com.hoabh.chanko.presentation.features.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.ui.theme.ChatTokens

/**
 * ReferenceChipsRow: Hàng chip tham chiếu cuộn ngang (CD-GAP-01, CD-GAP-04, EDGE-09).
 * Cho phép so sánh thêm các sản phẩm khác trong cùng cuộc trò chuyện.
 */
@Composable
fun ReferenceChipsRow(
    referenceProducts: List<ProductSnapshot>,
    onAddReferenceClick: () -> Unit,
    onRemoveReferenceClick: (String) -> Unit,
    isLocked: Boolean = false, // Locked when streaming (EDGE-09) or read-only
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active reference chips
        items(referenceProducts, key = { it.productId }) { ref ->
            ReferenceChip(
                product = ref,
                onRemove = { onRemoveReferenceClick(ref.productId) },
                isLocked = isLocked
            )
        }

        // Add Reference Action Chip
        item(key = "add_reference_action") {
            AddReferenceChip(
                onClick = onAddReferenceClick,
                isLocked = isLocked
            )
        }
    }
}

@Composable
fun ReferenceChip(
    product: ProductSnapshot,
    onRemove: () -> Unit,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ChatTokens.composerInputBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatTokens.indicatorTrack),
        modifier = modifier
            .height(48.dp)
            .semantics {
                contentDescription = "Món tham chiếu: ${product.name}, giá ${formatVndPrice(product.price)}"
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, end = 2.dp)
        ) {
            val thumbRes = product.thumbnailResId ?: getProductThumbnailResId(product.thumbnailUrl)
            Image(
                painter = painterResource(id = thumbRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ChatTokens.body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(IntrinsicSize.Min)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Delete button with >= 48dp target
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Gỡ bỏ tham chiếu ${product.name}"
                    }
                    .clickable(enabled = !isLocked, onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (isLocked) R.drawable.chat_close_disabled else R.drawable.chat_close_default
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddReferenceChip(
    onClick: () -> Unit,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isLocked) ChatTokens.disabledBg else ChatTokens.userBubble,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLocked) ChatTokens.disabledBorder else ChatTokens.primary
        ),
        modifier = modifier
            .height(48.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (isLocked) "Đang phản hồi, không thể thêm món so sánh" else "Thêm món so sánh"
            }
            .clickable(enabled = !isLocked, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Image(
                painter = painterResource(
                    id = if (isLocked) R.drawable.chat_reference_disabled else R.drawable.chat_reference_default
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+ Thêm món so sánh",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) ChatTokens.secondary else ChatTokens.primaryTextAndCTA
            )
        }
    }
}
