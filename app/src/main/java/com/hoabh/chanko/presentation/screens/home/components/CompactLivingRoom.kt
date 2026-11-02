package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.screens.home.DialogueUi
import com.hoabh.chanko.presentation.screens.home.LivingRoomUi
import com.hoabh.chanko.presentation.screens.home.RoomPropKind
import com.hoabh.chanko.presentation.screens.home.RoomPropUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private const val SCENE_ASPECT_RATIO = 1.95f
private const val CALENDAR_TEXT_ROTATION_DEG = -3f
private val DialogueBubbleShape = RoundedCornerShape(18.dp)
private val DialogueTailWidth = 12.dp
private val DialogueTailHeight = 8.dp
private val PropLabelPillShape = RoundedCornerShape(999.dp)
private val HitTargetCornerShape = RoundedCornerShape(16.dp)
private val CountBadgeColor = CandyColors.error

/**
 * Khu vực 2 — Little Room (Candy Pop 3D v1.0 — AC-04, HOME-REV-03, HOME-GAP-05, AC-20, AC-21):
 * - Bốn vật thể theo thứ tự nhìn trái sang phải:
 *   1. Đồng hồ cát ("Đang chờ") + count badge động
 *   2. Lịch xem lại ("Cần xem lại") + count badge động
 *   3. Ví ("Ngân sách") — không có count badge
 *   4. Pause Jar ("Đã bỏ qua") + count badge động
 * - Vật thể + pill nhãn + badge hợp thành 1 action semantic duy nhất (>= 48dp), mở contextual sheet đúng nhóm.
 * - Bóng thoại ngắn gọn của Chanko ở phía trên (HOME-GAP-05).
 */
@Composable
fun CompactLivingRoom(
    livingRoom: LivingRoomUi,
    onPropTapped: (RoomPropKind) -> Unit,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.35f
    val minSceneHeight = if (isLargeFont) 216.dp else 184.dp

    val propsByKind = remember(livingRoom.props) {
        livingRoom.props.associateBy { it.kind }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minSceneHeight),
        shape = RoundedCornerShape(28.dp),
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(SCENE_ASPECT_RATIO, matchHeightConstraintsFirst = false)
            ) {
                val sceneWidth = maxWidth
                val sceneHeight = maxHeight

                // z = 0: Room Background
                Image(
                    painter = painterResource(id = R.drawable.room_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // z = 1: Chanko Cat on Lavender Cushion (centered decorative mascot)
                Image(
                    painter = painterResource(id = R.drawable.cat_on_cushion),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.sceneLayer(
                        sceneWidth = sceneWidth,
                        sceneHeight = sceneHeight,
                        x = 0.28f,
                        y = 0.14f,
                        w = 0.44f,
                        h = 0.64f
                    )
                )

                // z = 2: Short Speech Bubble in top-right (HOME-GAP-05)
                if (livingRoom.dialogue != null) {
                    val bubbleX = if (isLargeFont) sceneWidth * 0.28f else sceneWidth * 0.44f
                    val bubbleY = sceneHeight * 0.03f
                    val bubbleMaxWidth = if (isLargeFont) sceneWidth * 0.68f else sceneWidth * 0.52f

                    ChankoDialogueBubble(
                        dialogue = livingRoom.dialogue,
                        modifier = Modifier
                            .offset(x = bubbleX, y = bubbleY)
                            .widthIn(max = bubbleMaxWidth)
                    )
                }

                // z = 3: 4 Interactive Props + Pill Labels + Dynamic Count Badges along the bottom row
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 1. Đồng hồ cát -> "Đang chờ"
                    RoomPropInteractiveSlot(
                        prop = propsByKind[RoomPropKind.Hourglass],
                        defaultLabel = stringResource(R.string.home_prop_hourglass_label),
                        showBadge = true,
                        onClick = { onPropTapped(RoomPropKind.Hourglass) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hourglass_books),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    // 2. Lịch -> "Cần xem lại"
                    RoomPropInteractiveSlot(
                        prop = propsByKind[RoomPropKind.Calendar],
                        defaultLabel = stringResource(R.string.home_prop_calendar_label),
                        showBadge = true,
                        onClick = { onPropTapped(RoomPropKind.Calendar) },
                        modifier = Modifier.weight(1f)
                    ) {
                        CalendarPropSpriteWithDynamicDate(
                            monthText = livingRoom.calendarMonthText,
                            dayText = livingRoom.calendarDayText,
                            reducedMotion = reducedMotion,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    // 3. Ví -> "Ngân sách" (Không có count badge theo COMPONENTS.md)
                    RoomPropInteractiveSlot(
                        prop = propsByKind[RoomPropKind.Wallet],
                        defaultLabel = stringResource(R.string.home_prop_wallet_label),
                        showBadge = false,
                        onClick = { onPropTapped(RoomPropKind.Wallet) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wallet_mint),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    // 4. Pause Jar -> "Đã bỏ qua"
                    RoomPropInteractiveSlot(
                        prop = propsByKind[RoomPropKind.PauseJar],
                        defaultLabel = stringResource(R.string.home_prop_pause_jar_label),
                        showBadge = true,
                        onClick = { onPropTapped(RoomPropKind.PauseJar) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pause_jar_heart),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Một vật thể + badge số lượng + pill nhãn dưới đáy kết hợp thành 1 node tương tác duy nhất (>= 48dp, AC-04, AC-20).
 */
@Composable
private fun RoomPropInteractiveSlot(
    prop: RoomPropUi?,
    defaultLabel: String,
    showBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    spriteContent: @Composable () -> Unit
) {
    val labelText = prop?.label ?: defaultLabel
    val badgeText = if (showBadge) prop?.badgeText else null
    val talkBackDescription = buildString {
        append(labelText)
        if (!badgeText.isNullOrBlank()) {
            append(", ")
            append(badgeText)
            append(" món")
        }
        if (!prop?.summaryText.isNullOrBlank()) {
            append(". ")
            append(prop?.summaryText)
        }
    }

    Column(
        modifier = modifier
            .sizeIn(
                minWidth = CandySize.touchTarget,
                minHeight = CandySize.touchTarget
            )
            .clip(HitTargetCornerShape)
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackDescription
                role = Role.Button
            }
            .clickable(
                role = Role.Button,
                onClickLabel = labelText,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                spriteContent()
            }

            if (!badgeText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 22.dp, minHeight = 22.dp)
                        .clip(CircleShape)
                        .background(CountBadgeColor)
                        .border(1.5.dp, Color.White, CircleShape)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = PropLabelPillShape,
            color = Color.White.copy(alpha = 0.94f),
            tonalElevation = CandyElevation.low,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, CandyColors.homeCardBorder)
        ) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CandyColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CalendarPropSpriteWithDynamicDate(
    monthText: String,
    dayText: String,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val calWidth = maxWidth
        val calHeight = maxHeight
        val density = LocalDensity.current
        val rotationDeg = if (reducedMotion) 0f else CALENDAR_TEXT_ROTATION_DEG

        val monthFontSize = with(density) { (calWidth * 0.13f).toSp() }
        val dayFontSize = with(density) { (calWidth * 0.27f).toSp() }

        Image(
            painter = painterResource(id = R.drawable.calendar_blank),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .offset(x = calWidth * 0.35f, y = calHeight * 0.23f)
                .size(width = calWidth * 0.42f, height = calHeight * 0.14f)
                .graphicsLayer { rotationZ = rotationDeg },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthText,
                color = CandyColors.onReadyAction,
                fontSize = monthFontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .offset(x = calWidth * 0.34f, y = calHeight * 0.43f)
                .size(width = calWidth * 0.45f, height = calHeight * 0.32f)
                .graphicsLayer { rotationZ = rotationDeg },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayText,
                color = CandyColors.calendarDayInk,
                fontSize = dayFontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Bong bóng thoại ngắn gọn của Chanko (HOME-GAP-05).
 */
@Composable
fun ChankoDialogueBubble(
    dialogue: DialogueUi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = DialogueBubbleShape,
            color = CandyColors.dialogueBubbleSurface,
            tonalElevation = CandyElevation.low,
            shadowElevation = CandyElevation.low,
            border = BorderStroke(1.dp, CandyColors.dialogueBubbleBorder)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = CandySpace.md,
                    vertical = 6.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = dialogue.headline,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (!dialogue.supportingLine.isNullOrBlank()) {
                    Text(
                        text = dialogue.supportingLine,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CandyColors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .padding(start = CandySpace.lg)
                .size(width = DialogueTailWidth, height = DialogueTailHeight)
        ) {
            val path = Path().apply {
                moveTo(size.width * 0.25f, 0f)
                lineTo(size.width, 0f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = path,
                color = CandyColors.dialogueBubbleSurface
            )
        }
    }
}

private fun Modifier.sceneLayer(
    sceneWidth: Dp,
    sceneHeight: Dp,
    x: Float,
    y: Float,
    w: Float,
    h: Float
): Modifier = this
    .offset(x = sceneWidth * x, y = sceneHeight * y)
    .size(width = sceneWidth * w, height = sceneHeight * h)
