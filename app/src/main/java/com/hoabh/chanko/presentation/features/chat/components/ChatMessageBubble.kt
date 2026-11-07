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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.chat.model.ChatMessage
import com.hoabh.chanko.domain.chat.model.MessageSender
import com.hoabh.chanko.domain.chat.model.ProductSnapshot
import com.hoabh.chanko.ui.theme.ChatTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ChatMessageBubble v2.2:
 * - User: Bong bóng tím nhạt (#EEE6FF), max 82% chiều rộng màn hình.
 * - Chanko: Avatar mascot nodpi (32dp), nền trắng viền mềm, phân tích khách quan (CD-GAP-03, EDGE-11).
 * - Hoàn toàn không còn thẻ Decision Verdict Card (BUY/WAIT/SKIP) cũ.
 */
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onProductClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = timeFormat.format(Date(message.createdAtEpochMillis))

    when (message.sender) {
        MessageSender.USER -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth(0.82f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 4.dp
                                )
                            )
                            .background(ChatTokens.userBubble)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .semantics {
                                contentDescription = "Tin nhắn của bạn: ${message.content}, lúc $timeString"
                            }
                    ) {
                        Text(
                            text = message.content,
                            color = ChatTokens.body,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeString,
                        color = ChatTokens.secondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        MessageSender.CHANKO, MessageSender.SYSTEM -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // Mascot avatar
                Image(
                    painter = painterResource(id = R.drawable.chanko_chat_avatar),
                    contentDescription = "Chanko Mascot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(0.88f)
                ) {
                    // Header name & time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Chanko",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChatTokens.body
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeString,
                            fontSize = 11.sp,
                            color = ChatTokens.secondary
                        )
                    }

                    // Main response bubble
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                )
                            )
                            .background(Color.White)
                            .border(1.dp, ChatTokens.bubbleBorder, RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .semantics {
                                contentDescription = "Chanko phản hồi: ${message.content}"
                            }
                    ) {
                        Column {
                            Text(
                                text = message.content,
                                color = ChatTokens.body,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Normal
                            )

                            // Product Snapshots Analysis (CD-GAP-03, EDGE-11)
                            if (message.productSnapshots.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    message.productSnapshots.forEach { snapshot ->
                                        AnalysisSnapshotCard(
                                            product = snapshot,
                                            onClick = { onProductClick?.invoke(snapshot.productId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * AnalysisSnapshotCard: Thẻ snapshot sản phẩm gắn kèm phân tích (CD-GAP-03, EDGE-11).
 * Không có thẻ BUY/WAIT/SKIP, chỉ thể hiện khách quan dữ liệu sản phẩm.
 */
@Composable
fun AnalysisSnapshotCard(
    product: ProductSnapshot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ChatTokens.snapshotCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, ChatTokens.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Sản phẩm được phân tích: ${product.name}, giá ${formatVndPrice(product.price)}"
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            val thumbRes = product.thumbnailResId ?: getProductThumbnailResId(product.thumbnailUrl)
            Image(
                painter = painterResource(id = thumbRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatTokens.body
                )
                Text(
                    text = formatVndPrice(product.price),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatTokens.primaryTextAndCTA
                )
            }
        }
    }
}
