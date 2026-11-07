package com.hoabh.chanko.presentation.features.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.policy.FreezeDurationPolicy
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.features.home.ItemJourneyUi
import com.hoabh.chanko.presentation.features.home.JourneyNodeStatus
import com.hoabh.chanko.presentation.features.home.JourneyNodeUi
import com.hoabh.chanko.presentation.features.home.SectionState
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace
import kotlin.math.hypot
import kotlin.math.roundToInt

private val JourneyCardCornerShape = RoundedCornerShape(24.dp)

/**
 * Cấu hình thị giác (viền màu + màu nền badge) cho đủ 6 trạng thái của Node trên Item Journey (AC-14).
 * Không bao giờ chỉ dựa vào màu sắc đơn thuần: mỗi trạng thái có một ký hiệu hình học/icon riêng biệt trên badge.
 */
data class JourneyNodeVisualStyle(
    val ringColor: Color,
    val badgeColor: Color
)

fun resolveJourneyNodeStyle(status: JourneyNodeStatus): JourneyNodeVisualStyle = when (status) {
    JourneyNodeStatus.CoolingOff -> JourneyNodeVisualStyle(
        ringColor = CandyColors.HomeV2.statusCoolingOffRing,
        badgeColor = CandyColors.HomeV2.statusCoolingOffRing
    )
    JourneyNodeStatus.Ready -> JourneyNodeVisualStyle(
        ringColor = CandyColors.HomeV2.statusReadyRing,
        badgeColor = CandyColors.HomeV2.statusReadyRing
    )
    JourneyNodeStatus.Purchased -> JourneyNodeVisualStyle(
        ringColor = CandyColors.HomeV2.statusPurchasedRing,
        badgeColor = CandyColors.HomeV2.statusPurchasedInk
    )
    JourneyNodeStatus.Skipped -> JourneyNodeVisualStyle(
        ringColor = CandyColors.HomeV2.statusSkippedRing,
        badgeColor = CandyColors.HomeV2.statusSkippedRing
    )
}

/**
 * Khu vực 3 — Item Journey ("Món đồ gần đây" — Home v2.0 — AC-10..AC-17, AC-18..AC-24, AC-31):
 * - Hiển thị tối đa `min(n, 5)` node tròn (`80–96dp`) theo thứ tự `createdAt DESC` (đã lọc bỏ `Archived`).
 * - 1 node không có đường nối; 2–5 node xếp ziczac dọc bên trái nối bằng `n - 1` đoạn nét đứt màu lavender (#BDA8EB).
 * - Mỗi node có ảnh sản phẩm ở giữa (hoặc placeholder khi lỗi ảnh), viền màu và badge trạng thái góc trên phải.
 * - Không hiển thị tên món thường trực bên cạnh node nhưng TalkBack đọc đầy đủ tên + trạng thái + giá (AC-31).
 * - Chạm node mở `ItemDetailScreen(itemId)` trực tiếp không qua bottom sheet (AC-15).
 * - Chạm "Xem tất cả ›" mở `MyItems` thông thường, không lọc actionable (AC-16).
 * - 1 mascot Chanko canonical đứng bên phải cuộn cùng Journey, không che vùng chạm của các node (AC-17).
 */
@Composable
fun ItemJourneySection(
    journeyState: SectionState<ItemJourneyUi>,
    onNodeGestureStart: (String) -> Unit,
    onNodeGestureEnd: () -> Unit,
    onNodeClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isReducedMotionEnabled: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
    ) {
        // 1. Section Header: Icon túi mua sắm + "Món đồ gần đây" + "Xem tất cả ›"
        ItemJourneyHeader(onSeeAllClick = onSeeAllClick)

        // 2. Nội dung theo SectionState độc lập
        when (journeyState) {
            SectionState.Loading -> {
                ItemJourneyLoadingState()
            }

            is SectionState.OfflineNoCache -> {
                ItemJourneyMessageCard(
                    title = stringResource(R.string.home_journey_offline_no_cache_title),
                    subtitle = stringResource(R.string.home_journey_offline_no_cache_subtitle),
                    actionLabel = null,
                    onActionClick = {}
                )
            }

            is SectionState.Error -> {
                ItemJourneyMessageCard(
                    title = journeyState.message.ifBlank {
                        stringResource(R.string.home_section_error_journey)
                    },
                    subtitle = stringResource(R.string.home_journey_offline_no_cache_subtitle),
                    actionLabel = stringResource(R.string.home_section_retry),
                    onActionClick = onRetryClick
                )
            }

            is SectionState.Empty -> {
                ItemJourneyEmptyState()
            }

            is SectionState.Content -> {
                val nodes = journeyState.data.nodes
                if (nodes.isEmpty()) {
                    ItemJourneyEmptyState()
                } else {
                    ItemJourneyZigZagCanvas(
                        nodes = nodes,
                        onNodeGestureStart = onNodeGestureStart,
                        onNodeGestureEnd = onNodeGestureEnd,
                        onNodeClick = onNodeClick,
                        isReducedMotionEnabled = isReducedMotionEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemJourneyHeader(
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CandySpace.xs),
            modifier = Modifier.weight(1f)
        ) {
            ChankoIcon(
                id = ChankoIconId.ITM_01,
                contentDescription = null,
                size = 24.dp
            )
            Text(
                text = stringResource(R.string.home_journey_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = CandyColors.HomeV2.textPrimary
            )
        }

        Box(
            modifier = Modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    role = Role.Button,
                    onClick = onSeeAllClick
                )
                .padding(horizontal = CandySpace.xs, vertical = CandySpace.xs),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "${stringResource(R.string.home_journey_see_all)} ›",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = CandyColors.brandPurple
            )
        }
    }
}

/**
 * Kết quả tính toán bố cục đường ziczac ở GIỮA màn hình kèm 2 chú mèo hai bên trái/phải.
 */
data class CenteredZigZagLayoutDp(
    val canvasCenterX: Dp,
    val nodeDiameter: Dp,
    val mascotSize: Dp,
    val nodeCenters: List<Pair<Dp, Dp>>,
    val totalHeight: Dp
)

/**
 * Tính toán toạ độ đường ziczac nằm cân đối ở chính giữa (`maxWidth / 2`),
 * đảm bảo 2 chú mèo bên trái và bên phải không bao giờ chồng lấn vào vùng chạm của các node.
 */
fun computeCenteredZigZagLayout(
    maxWidth: Dp,
    nodeCount: Int
): CenteredZigZagLayoutDp {
    val count = nodeCount.coerceIn(1, 5)
    val isCompactWidth = maxWidth < 340.dp
    // Đường kính node clamp trong 80..96dp theo đúng DESIGN-TOKENS.json & AC-13
    val nodeDiameter: Dp = when {
        isCompactWidth -> 80.dp
        maxWidth < 390.dp -> 84.dp
        else -> 88.dp
    }
    val nodeRadius: Dp = nodeDiameter / 2
    val verticalStep: Dp = if (isCompactWidth) 116.dp else 124.dp
    val topPadding: Dp = 8.dp
    val subLabelHeight: Dp = 42.dp
    val bottomPadding: Dp = 12.dp

    val canvasCenterX: Dp = maxWidth / 2
    val zigZagAmplitude: Dp = when {
        count <= 1 -> 0.dp
        isCompactWidth -> 16.dp
        else -> 24.dp
    }
    val leftCenterX: Dp = canvasCenterX - zigZagAmplitude
    val rightCenterX: Dp = canvasCenterX + zigZagAmplitude

    val nodeCenters: List<Pair<Dp, Dp>> = (0 until count).map { index ->
        val centerX = when {
            count <= 1 -> canvasCenterX
            index % 2 == 0 -> rightCenterX
            else -> leftCenterX
        }
        val centerY = topPadding + nodeRadius + (verticalStep * index)
        centerX to centerY
    }

    // Khoảng trống mỗi bên từ mép màn hình tới mép ngoài của node gần nhất
    val availableSideClearance: Dp = (leftCenterX - nodeRadius - 6.dp).coerceAtLeast(60.dp)
    val preferredMascotSize: Dp = when {
        isCompactWidth -> 72.dp
        count == 1 -> 92.dp
        else -> 96.dp
    }
    val mascotSize: Dp = minOf(preferredMascotSize, availableSideClearance)

    val lastNodeBottom = (nodeCenters.lastOrNull()?.second ?: nodeRadius) + nodeRadius + subLabelHeight + bottomPadding
    val totalHeight: Dp = maxOf(lastNodeBottom, mascotSize + 24.dp)

    return CenteredZigZagLayoutDp(
        canvasCenterX = canvasCenterX,
        nodeDiameter = nodeDiameter,
        mascotSize = mascotSize,
        nodeCenters = nodeCenters,
        totalHeight = totalHeight
    )
}

@Composable
private fun ItemJourneyZigZagCanvas(
    nodes: List<JourneyNodeUi>,
    onNodeGestureStart: (String) -> Unit,
    onNodeGestureEnd: () -> Unit,
    onNodeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isReducedMotionEnabled: Boolean = false
) {
    val visibleNodes = nodes.take(5)
    val density = LocalDensity.current
    val connectorColor = CandyColors.HomeV2.lavenderConnector

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val layout = computeCenteredZigZagLayout(
            maxWidth = maxWidth,
            nodeCount = visibleNodes.size
        )
        val nodeDiameter = layout.nodeDiameter
        val nodeRadius = nodeDiameter / 2
        val nodeCentersDp = layout.nodeCenters
        val mascotSize = layout.mascotSize
        val totalHeight = layout.totalHeight

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .semantics { isTraversalGroup = true }
        ) {
            // 1. Lớp nền Canvas: Vẽ n - 1 đoạn đường nối nét đứt màu lavender từ biên node i tới biên node i+1 (AC-12, AC-13)
            if (visibleNodes.size >= 2) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clearAndSetSemantics {}
                ) {
                    val radiusPx = nodeRadius.toPx()
                    val dashEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(6.dp.toPx(), 6.dp.toPx()),
                        phase = 0f
                    )
                    for (i in 0 until nodeCentersDp.size - 1) {
                        val (x1Dp, y1Dp) = nodeCentersDp[i]
                        val (x2Dp, y2Dp) = nodeCentersDp[i + 1]
                        val p1 = Offset(x1Dp.toPx(), y1Dp.toPx())
                        val p2 = Offset(x2Dp.toPx(), y2Dp.toPx())
                        val dx = p2.x - p1.x
                        val dy = p2.y - p1.y
                        val dist = hypot(dx, dy)
                        if (dist > radiusPx * 2f) {
                            val ux = dx / dist
                            val uy = dy / dist
                            val start = Offset(p1.x + ux * radiusPx, p1.y + uy * radiusPx)
                            val end = Offset(p2.x - ux * radiusPx, p2.y - uy * radiusPx)
                            drawLine(
                                color = connectorColor,
                                start = start,
                                end = end,
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = dashEffect
                            )
                        }
                    }
                }
            }

            // 2. Hai chú mèo Chanko (mascot_xxx) đứng hai bên trái & phải đường ziczac ở giữa, không che vùng chạm node (AC-17, AC-31)
            val leftMascotTopOffset = if (visibleNodes.size <= 2) {
                (totalHeight - mascotSize) / 2
            } else {
                totalHeight * 0.16f
            }
            val rightMascotTopOffset = if (visibleNodes.size <= 2) {
                (totalHeight - mascotSize) / 2
            } else {
                totalHeight * 0.52f
            }

            // Chú mèo bên trái (mascot_hopeful_upward)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = leftMascotTopOffset)
                    .clearAndSetSemantics {}
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = mascotSize * 0.70f, height = 14.dp)
                        .clip(CircleShape)
                        .background(CandyColors.primarySoft)
                )
                Image(
                    painter = painterResource(id = R.drawable.mascot_hopeful_upward),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(mascotSize)
                )
            }

            // Chú mèo bên phải (mascot_playful_right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = rightMascotTopOffset)
                    .clearAndSetSemantics {}
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = mascotSize * 0.70f, height = 14.dp)
                        .clip(CircleShape)
                        .background(CandyColors.primarySoft)
                )
                Image(
                    painter = painterResource(id = R.drawable.mascot_playful_right),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(mascotSize)
                )
            }

            // 3. Các Node tròn (80–96dp) kèm Sub-Label theo đúng thứ tự createdAt DESC để TalkBack đọc chuẩn xác (AC-13..AC-15, AC-23, AC-24, AC-31)
            visibleNodes.forEachIndexed { index, node ->
                key(node.itemId) {
                    val (centerX, centerY) = nodeCentersDp[index]
                    val columnWidth = if (nodeDiameter < 84.dp) 96.dp else 104.dp
                    val topLeftX = centerX - (columnWidth / 2)
                    val topLeftY = centerY - nodeRadius

                    JourneyProductNode(
                        node = node,
                        diameter = nodeDiameter,
                        traversalOrder = index.toFloat(),
                        onGestureStart = { onNodeGestureStart(node.itemId) },
                        onGestureEnd = onNodeGestureEnd,
                        onClick = { onNodeClick(node.itemId) },
                        isReducedMotionEnabled = isReducedMotionEnabled,
                        modifier = Modifier.offset {
                            with(density) {
                                IntOffset(
                                    x = topLeftX.toPx().roundToInt(),
                                    y = topLeftY.toPx().roundToInt()
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyProductNode(
    node: JourneyNodeUi,
    diameter: Dp,
    traversalOrder: Float,
    onGestureStart: () -> Unit,
    onGestureEnd: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isReducedMotionEnabled: Boolean = false
) {
    val style = resolveJourneyNodeStyle(node.status)
    val nodeCd = when (node.status) {
        JourneyNodeStatus.CoolingOff -> {
            val remText = node.remainingSeconds?.let { sec ->
                val days = sec / 86400
                val hours = (sec % 86400) / 3600
                if (days > 0) "còn $days ngày $hours giờ" else "còn $hours giờ"
            } ?: "đang hạ nhiệt"
            "${node.title}, đang hạ nhiệt, $remText, giá ${node.priceFormatted}"
        }
        JourneyNodeStatus.Ready -> {
            "${node.title}, đã sẵn sàng xem xét, giá ${node.priceFormatted}, chạm để quyết định"
        }
        else -> stringResource(
            R.string.home_cd_journey_node,
            node.title,
            node.statusLabel,
            node.priceFormatted
        )
    }

    // Hiệu ứng nhịp thở nhẹ nhàng cho món Ready (mời gọi xem xét quyết định)
    val isPulseActive = (node.status == JourneyNodeStatus.Ready) && !isReducedMotionEnabled
    val pulseTransition = rememberInfiniteTransition(label = "pulse_transition_${node.itemId}")
    val pulseAlpha by if (isPulseActive) {
        pulseTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha_${node.itemId}"
        )
    } else {
        remember { mutableFloatStateOf(0.4f) }
    }

    // Tính toán góc quét đếm ngược cho món CoolingOff
    val totalSeconds = remember(node.priceMinor) {
        val totalMillis = FreezeDurationPolicy.calculateDurationMillis(node.priceMinor ?: 0L)
        (totalMillis / 1000L).coerceAtLeast(1L)
    }
    val remSeconds = (node.remainingSeconds ?: totalSeconds).coerceIn(0L, totalSeconds)
    val remainingFraction = (remSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val countdownSweepAngle = 360f * remainingFraction

    val columnWidth = if (diameter < 84.dp) 96.dp else 104.dp

    Column(
        modifier = modifier
            .width(columnWidth)
            .pointerInput(node.itemId) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    onGestureStart()
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    onGestureEnd()
                }
            }
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = nodeCd
                role = Role.Button
                traversalIndex = traversalOrder
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Vòng tròn sản phẩm chính
        Box(
            modifier = Modifier
                .size(diameter)
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            // 1. Viền Canvas bên ngoài: Cung đếm ngược (CoolingOff) hoặc Viền nhịp thở (Ready)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clearAndSetSemantics {}
            ) {
                val strokeWidth = 3.dp.toPx()
                val orbitRadius = (size.minDimension / 2f) - (strokeWidth / 2f) - 2.dp.toPx()

                when (node.status) {
                    JourneyNodeStatus.CoolingOff -> {
                        // Rãnh nền mờ bên dưới
                        drawCircle(
                            color = style.ringColor.copy(alpha = 0.18f),
                            radius = orbitRadius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        // Cung đếm ngược tròn (Circular Countdown Arc) từ 12h (-90 độ)
                        val arcDiameter = orbitRadius * 2f
                        val topLeftOffset = Offset(center.x - orbitRadius, center.y - orbitRadius)
                        drawArc(
                            color = style.ringColor,
                            startAngle = -90f,
                            sweepAngle = countdownSweepAngle,
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = Size(arcDiameter, arcDiameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    JourneyNodeStatus.Ready -> {
                        // Viền chính khép kín nổi bật
                        drawCircle(
                            color = style.ringColor,
                            radius = orbitRadius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                        // Vòng hào quang nhịp thở bên ngoài khi pulse active
                        if (isPulseActive) {
                            drawCircle(
                                color = style.ringColor.copy(alpha = pulseAlpha * 0.35f),
                                radius = orbitRadius + 2.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }

                    else -> {
                        // Purchased / Skipped: Viền nét liền tĩnh, sắc thái trung tính
                        drawCircle(
                            color = style.ringColor,
                            radius = orbitRadius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
            }

            // 2. Vòng tròn lõi vật phẩm bên trong
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
                    .shadow(
                        elevation = if (isPulseActive) 6.dp else 4.dp,
                        shape = CircleShape,
                        ambientColor = style.ringColor.copy(alpha = if (isPulseActive) pulseAlpha * 0.7f else 0.35f),
                        spotColor = style.ringColor.copy(alpha = if (isPulseActive) pulseAlpha else 0.45f)
                    ),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(CandyColors.HomeV2.background),
                    contentAlignment = Alignment.Center
                ) {
                    if (node.thumbnailResId != null && !node.imageLoadFailed) {
                        Image(
                            painter = painterResource(id = node.thumbnailResId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        // Placeholder an toàn khi thiếu ảnh hoặc tải ảnh lỗi (STATE-MATRIX: image_failure)
                        ChankoIcon(
                            id = ChankoIconId.ITM_01,
                            contentDescription = null,
                            size = 32.dp
                        )
                    }
                }
            }

            // 3. Huy hiệu trạng thái góc trên phải (không bị xoay)
            JourneyStatusBadge(
                status = node.status,
                badgeColor = style.badgeColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Cụm Sub-Label: Tên món đồ & Pill Giá tiền
        JourneyNodeSubLabel(
            title = node.title,
            priceFormatted = node.priceFormatted
        )
    }
}

@Composable
private fun JourneyNodeSubLabel(
    title: String,
    priceFormatted: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Tên món đồ (1 dòng, ellipsize, chữ đậm vừa, màu Ink810)
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 15.sp
            ),
            color = CandyColors.HomeV2.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Pill Giá tiền (Chip nhỏ bo tròn, nền tím phấn nhạt, chữ tím đậm)
        val displayPrice = if (!priceFormatted.isNullOrBlank()) {
            priceFormatted
        } else {
            stringResource(R.string.home_journey_missing_price)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CandyColors.HomeV2.journeyPricePillBackground)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayPrice,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp
                ),
                color = CandyColors.HomeV2.journeyPricePillText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun JourneyStatusBadge(
    status: JourneyNodeStatus,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .shadow(elevation = 2.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(badgeColor)
            .border(width = 2.dp, color = Color.White, shape = CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            when (status) {
                JourneyNodeStatus.CoolingOff -> {
                    // Hình Đồng hồ cát trắng
                    val path = Path().apply {
                        moveTo(w * 0.20f, h * 0.14f)
                        lineTo(w * 0.80f, h * 0.14f)
                        lineTo(w * 0.50f, h * 0.50f)
                        close()
                        moveTo(w * 0.50f, h * 0.50f)
                        lineTo(w * 0.80f, h * 0.86f)
                        lineTo(w * 0.20f, h * 0.86f)
                        close()
                    }
                    drawPath(path = path, color = Color.White, style = stroke)
                }

                JourneyNodeStatus.Ready -> {
                    // Hình Dấu tích ✓ trắng
                    val path = Path().apply {
                        moveTo(w * 0.16f, h * 0.54f)
                        lineTo(w * 0.42f, h * 0.78f)
                        lineTo(w * 0.84f, h * 0.24f)
                    }
                    drawPath(path = path, color = Color.White, style = stroke)
                }

                JourneyNodeStatus.Skipped -> {
                    // Hình Dấu trừ − trắng
                    drawLine(
                        color = Color.White,
                        start = Offset(w * 0.20f, h * 0.50f),
                        end = Offset(w * 0.80f, h * 0.50f),
                        strokeWidth = 2.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                JourneyNodeStatus.Purchased -> {
                    // Hình Túi mua sắm nhỏ trắng
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.18f, h * 0.34f),
                        size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.54f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        style = stroke
                    )
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.32f, h * 0.14f),
                        size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.36f),
                        style = stroke
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemJourneyEmptyState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = JourneyCardCornerShape,
        color = CandyColors.homeCardSurface,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            Image(
                painter = painterResource(id = R.drawable.mascot_curious_front),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
                    .clearAndSetSemantics {}
            )
            Text(
                text = stringResource(R.string.home_journey_empty_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.HomeV2.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.home_journey_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = CandyColors.HomeV2.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ItemJourneyLoadingState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp),
        shape = JourneyCardCornerShape,
        color = CandyColors.homeCardSurface,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = CandyColors.brandPurple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(CandySpace.sm))
            Text(
                text = stringResource(R.string.home_section_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = CandyColors.HomeV2.textSecondary
            )
        }
    }
}

@Composable
private fun ItemJourneyMessageCard(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = JourneyCardCornerShape,
        color = CandyColors.homeCardSurface,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.HomeV2.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CandyColors.HomeV2.textSecondary
            )
            if (actionLabel != null) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandyColors.brandPurple,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
