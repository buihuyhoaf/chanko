package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private const val SCENE_ASPECT_RATIO = 2.5f
private const val CALENDAR_TEXT_ROTATION_DEG = -3f
private val DialogueBubbleShape = RoundedCornerShape(18.dp)
private val DialogueTailWidth = 14.dp
private val DialogueTailHeight = 8.dp
private val HitTargetCornerShape = RoundedCornerShape(14.dp)

/**
 * HOME-02 — Chanko's Compact Living Room (Asset Pack v3 — `scene-layout.json`):
 * - Lắp ráp 6 lớp PNG tách rời theo đúng thứ tự z-order (0..5):
 *   0. `room_background.png` (`[0, 0, 1, 1]`, FillBounds)
 *   1. `hourglass_books.png` (`[0.005, 0.395, 0.215, 0.60]`, Fit)
 *   2. `calendar_blank.png` (`[0.19, 0.51, 0.20, 0.46]`, Fit) + chữ động `month`/`day` nghiêng `-3°`
 *   3. `wallet_mint.png` (`[0.35, 0.55, 0.18, 0.43]`, Fit)
 *   4. `cat_on_cushion.png` (`[0.47, 0.12, 0.35, 0.875]`, Fit — trang trí, không bấm)
 *   5. `pause_jar_heart.png` (`[0.78, 0.48, 0.215, 0.515]`, Fit)
 * - Lớp `z = 6`: `ChankoDialogue` nằm ở góc trên-trái bên trong scene (tuyệt đối không tách thành hàng dưới scene).
 * - 4 vùng chạm (`HitTarget`) độc lập, không chồng lấn (>= 48dp) có TalkBack semantics riêng cho
 *   `Hourglass`, `Calendar`, `Wallet`, `PauseJar`.
 */
@Composable
fun CompactLivingRoom(
    livingRoom: LivingRoomUi,
    onPropTapped: (RoomPropKind) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.4f
    val minSceneHeight = if (isLargeFont && livingRoom.dialogue != null) 184.dp else 140.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minSceneHeight)
            .aspectRatio(SCENE_ASPECT_RATIO, matchHeightConstraintsFirst = false),
        shape = CandyShape.card,
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(CandyShape.card)
        ) {
            val sceneWidth = maxWidth
            val sceneHeight = maxHeight

            // z = 0: Room Background
            Image(
                painter = painterResource(id = R.drawable.room_background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // z = 1: Hourglass on Books ([0.005, 0.395, 0.215, 0.60])
            Image(
                painter = painterResource(id = R.drawable.hourglass_books),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.sceneLayer(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.005f,
                    y = 0.395f,
                    w = 0.215f,
                    h = 0.60f
                )
            )

            // z = 2: Blank Desk Calendar ([0.19, 0.51, 0.20, 0.46]) + Dynamic Compose Text (-3 deg)
            CalendarPropSpriteWithDynamicDate(
                monthText = livingRoom.calendarMonthText,
                dayText = livingRoom.calendarDayText,
                modifier = Modifier.sceneLayer(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.19f,
                    y = 0.51f,
                    w = 0.20f,
                    h = 0.46f
                )
            )

            // z = 3: Mint Wallet ([0.35, 0.55, 0.18, 0.43])
            Image(
                painter = painterResource(id = R.drawable.wallet_mint),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.sceneLayer(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.35f,
                    y = 0.55f,
                    w = 0.18f,
                    h = 0.43f
                )
            )

            // z = 4: Chanko Cat on Lavender Cushion ([0.47, 0.12, 0.35, 0.875]) — decorative
            Image(
                painter = painterResource(id = R.drawable.cat_on_cushion),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.sceneLayer(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.47f,
                    y = 0.12f,
                    w = 0.35f,
                    h = 0.875f
                )
            )

            // z = 5: Pause Jar with Pink Heart ([0.78, 0.48, 0.215, 0.515])
            Image(
                painter = painterResource(id = R.drawable.pause_jar_heart),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.sceneLayer(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.78f,
                    y = 0.48f,
                    w = 0.215f,
                    h = 0.515f
                )
            )

            // z = 6: ChankoDialogue Overlay inside top-left of scene ([0.19, 0.055, 0.33, 0.39])
            if (livingRoom.dialogue != null) {
                val bubbleX = if (isLargeFont) sceneWidth * 0.12f else sceneWidth * 0.18f
                val bubbleY = sceneHeight * 0.05f
                val bubbleMaxWidth = if (isLargeFont) sceneWidth * 0.58f else sceneWidth * 0.42f

                ChankoDialogueBubble(
                    dialogue = livingRoom.dialogue,
                    modifier = Modifier
                        .offset(x = bubbleX, y = bubbleY)
                        .widthIn(min = sceneWidth * 0.32f, max = bubbleMaxWidth)
                )
            }

            // 4 Non-Overlapping Interactive Hit Targets (>= 48dp) for Accessibility & Touch
            val propsByKind = remember(livingRoom.props) { livingRoom.props.associateBy { it.kind } }

            // 1. Hourglass Hit Target: x in [0.01..0.185]
            PropHitTarget(
                prop = propsByKind[RoomPropKind.Hourglass],
                fallbackDescription = stringResource(
                    R.string.home_cd_prop_hourglass,
                    stringResource(R.string.home_prop_hourglass_label)
                ),
                onClick = { onPropTapped(RoomPropKind.Hourglass) },
                modifier = Modifier.sceneHitTarget(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.01f,
                    y = 0.42f,
                    w = 0.175f,
                    h = 0.56f
                )
            )

            // 2. Calendar Hit Target: x in [0.195..0.355]
            val calendarProp = propsByKind[RoomPropKind.Calendar]
            val calendarCd = stringResource(
                R.string.home_cd_prop_calendar,
                livingRoom.calendarMonthText,
                livingRoom.calendarDayText,
                calendarProp?.summaryText ?: stringResource(R.string.home_prop_calendar_label)
            )
            PropHitTarget(
                prop = calendarProp,
                fallbackDescription = calendarCd,
                overrideDescription = calendarCd,
                onClick = { onPropTapped(RoomPropKind.Calendar) },
                modifier = Modifier.sceneHitTarget(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.195f,
                    y = 0.50f,
                    w = 0.16f,
                    h = 0.48f
                )
            )

            // 3. Wallet Hit Target: x in [0.365..0.535]
            PropHitTarget(
                prop = propsByKind[RoomPropKind.Wallet],
                fallbackDescription = stringResource(
                    R.string.home_cd_prop_wallet,
                    stringResource(R.string.home_prop_wallet_label)
                ),
                onClick = { onPropTapped(RoomPropKind.Wallet) },
                modifier = Modifier.sceneHitTarget(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.365f,
                    y = 0.52f,
                    w = 0.17f,
                    h = 0.46f
                )
            )

            // 4. PauseJar Hit Target: x in [0.78..0.985]
            PropHitTarget(
                prop = propsByKind[RoomPropKind.PauseJar],
                fallbackDescription = stringResource(
                    R.string.home_cd_prop_pause_jar,
                    stringResource(R.string.home_prop_pause_jar_label)
                ),
                onClick = { onPropTapped(RoomPropKind.PauseJar) },
                modifier = Modifier.sceneHitTarget(
                    sceneWidth = sceneWidth,
                    sceneHeight = sceneHeight,
                    x = 0.78f,
                    y = 0.48f,
                    w = 0.205f,
                    h = 0.50f
                )
            )
        }
    }
}

/**
 * Vẽ sprite `calendar_blank.png` kèm chữ `month` và `day` động xoay `-3°` theo `scene-layout.json`:
 * - `month`: box `[0.35, 0.25, 0.42, 0.11]`, chữ trắng đậm (`OCT`)
 * - `day`: box `[0.34, 0.45, 0.45, 0.29]`, chữ `#585066` extra-bold (`24`)
 */
@Composable
private fun CalendarPropSpriteWithDynamicDate(
    monthText: String,
    dayText: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val calWidth = maxWidth
        val calHeight = maxHeight
        val density = LocalDensity.current

        // Kích thước chữ gắn theo kích thước vật lý của lịch 3D để không tràn khung khi fontScale = 2.0
        val monthFontSize = with(density) { (calWidth * 0.125f).toSp() }
        val dayFontSize = with(density) { (calWidth * 0.28f).toSp() }

        Image(
            painter = painterResource(id = R.drawable.calendar_blank),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Month text box: [0.35, 0.25, 0.42, 0.11]
        Box(
            modifier = Modifier
                .offset(x = calWidth * 0.35f, y = calHeight * 0.23f)
                .size(width = calWidth * 0.42f, height = calHeight * 0.14f)
                .graphicsLayer { rotationZ = CALENDAR_TEXT_ROTATION_DEG },
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

        // Day text box: [0.34, 0.45, 0.45, 0.29]
        Box(
            modifier = Modifier
                .offset(x = calWidth * 0.34f, y = calHeight * 0.43f)
                .size(width = calWidth * 0.45f, height = calHeight * 0.32f)
                .graphicsLayer { rotationZ = CALENDAR_TEXT_ROTATION_DEG },
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
 * Bong bóng thoại của Chanko nằm ở góc trên-trái bên trong Compact Living Room (`z = 6`).
 */
@Composable
fun ChankoDialogueBubble(
    dialogue: DialogueUi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
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
                    vertical = CandySpace.sm
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = dialogue.headline,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.textPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (!dialogue.supportingLine.isNullOrBlank()) {
                    Text(
                        text = dialogue.supportingLine,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = CandyColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Đuôi bong bóng thoại hướng về phía Chanko ở bên phải
        Canvas(
            modifier = Modifier
                .padding(end = CandySpace.lg)
                .size(width = DialogueTailWidth, height = DialogueTailHeight)
        ) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.75f, 0f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(
                path = path,
                color = CandyColors.dialogueBubbleSurface
            )
        }
    }
}

@Composable
private fun PropHitTarget(
    prop: RoomPropUi?,
    fallbackDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overrideDescription: String? = null
) {
    val description = overrideDescription
        ?: prop?.let { "${it.label}: ${it.summaryText}" }
        ?: fallbackDescription

    Box(
        modifier = modifier
            .sizeIn(
                minWidth = CandySize.touchTarget,
                minHeight = CandySize.touchTarget
            )
            .clip(HitTargetCornerShape)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
            }
            .clickable(
                role = Role.Button,
                onClickLabel = prop?.label ?: fallbackDescription,
                onClick = onClick
            )
    )
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

private fun Modifier.sceneHitTarget(
    sceneWidth: Dp,
    sceneHeight: Dp,
    x: Float,
    y: Float,
    w: Float,
    h: Float
): Modifier = this
    .offset(x = sceneWidth * x, y = sceneHeight * y)
    .size(
        width = (sceneWidth * w).coerceAtLeast(CandySize.touchTarget),
        height = (sceneHeight * h).coerceAtLeast(CandySize.touchTarget)
    )
