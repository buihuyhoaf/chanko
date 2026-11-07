package com.hoabh.chanko.presentation.features.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.hoabh.chanko.domain.chat.model.CoolingOffStatus
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.domain.chat.util.MonotonicClockCalculator
import com.hoabh.chanko.ui.theme.ChatTokens
import java.text.NumberFormat
import java.util.Locale

fun formatVndPrice(price: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    return "${formatter.format(price)}đ"
}

fun getProductThumbnailResId(thumbnailUrl: String?): Int {
    return when (thumbnailUrl) {
        "chat_fixture_headphones", "fixture://p_headphones" -> R.drawable.chat_fixture_headphones
        "chat_fixture_watch", "fixture://p_watch" -> R.drawable.chat_fixture_watch
        "chat_fixture_keyboard", "fixture://p_keyboard" -> R.drawable.chat_fixture_keyboard
        "chat_fixture_chair", "fixture://p_chair" -> R.drawable.chat_fixture_chair
        else -> R.drawable.chat_fixture_neutral
    }
}

/**
 * ProductContextCard: Thẻ hiển thị sản phẩm chính (CD-GAP-01, CL-GAP-04, EDGE-01).
 * Khóa bất biến sau khi tạo chat (CD-GAP-02).
 */
@Composable
fun ProductContextCard(
    product: ProductSnapshot,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val formattedPrice = formatVndPrice(product.price)
    val cooling = product.coolingOff
    val coolingText = when (cooling?.status) {
        CoolingOffStatus.ELAPSED -> "Sẵn sàng xem xét"
        CoolingOffStatus.ACTIVE -> MonotonicClockCalculator.formatRemaining(cooling.remainingMillis)
        else -> "Chưa rõ thời gian hạ nhiệt"
    }

    val cardColor = if (product.isPrimaryDeleted) ChatTokens.cardDeletedBg else Color.White
    val borderColor = if (product.isPrimaryDeleted) ChatTokens.cardBorderDeleted else ChatTokens.userBubble

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (product.isPrimaryDeleted) {
                    "Sản phẩm chính đã bị xóa: ${product.name}, giá $formattedPrice"
                } else {
                    "Sản phẩm chính: ${product.name}, giá $formattedPrice, $coolingText"
                }
            }
            .then(
                if (onClick != null && !product.isPrimaryDeleted) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val thumbRes = product.thumbnailResId ?: getProductThumbnailResId(product.thumbnailUrl)
            Image(
                painter = painterResource(id = thumbRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChatTokens.backgroundStart)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (product.isPrimaryDeleted) {
                    Text(
                        text = "Món đồ chính đã bị xóa",
                        color = ChatTokens.alertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = product.name,
                    color = ChatTokens.body,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = formattedPrice,
                    color = ChatTokens.primaryTextAndCTA,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Cooling-off pill badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (cooling?.status) {
                                CoolingOffStatus.ELAPSED -> ChatTokens.coolingElapsedBg
                                CoolingOffStatus.ACTIVE -> ChatTokens.coolingActiveBg
                                else -> ChatTokens.neutralMuted
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chat_cooling_default),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = coolingText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (cooling?.status) {
                            CoolingOffStatus.ELAPSED -> ChatTokens.coolingElapsedText
                            CoolingOffStatus.ACTIVE -> ChatTokens.primaryTextAndCTA
                            else -> ChatTokens.secondary
                        }
                    )
                }
            }
        }
    }
}
