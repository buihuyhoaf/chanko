package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.ObservationUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace
import com.hoabh.chanko.ui.theme.Nunito

private val BulbTileSize = 40.dp
private val BulbTileShape = RoundedCornerShape(12.dp)
private val BulbIconSize = 24.dp
private val DismissIconSize = 18.dp
private val ObservationCardRadius = 24.dp

/**
 * Khu vực 5 — Observation Card ("Góc nhìn từ Chanko" — Candy Pop 3D v1.0, AC-14, AC-20, AC-22):
 * - Chỉ hiển thị khi có insight đủ dữ liệu và chưa bị ẩn (AC-14).
 * - Tiêu đề "Góc nhìn từ Chanko", nút đóng `X` góc phải (>= 48dp).
 * - Nội dung nhận xét ngắn gọn dựa trên dữ liệu thực.
 * - Hai nút pill bên dưới (>= 48dp):
 *   1. "Giải thích thêm" (mở Contextual Sheet giải thích chi tiết ngay tại Home)
 *   2. "Ẩn gợi ý này" (cùng thực hiện hide insight hiện tại với nút X).
 */
@Composable
fun ObservationCard(
    observation: ObservationUi,
    onExplainClick: (String) -> Unit,
    onHideClick: (String) -> Unit,
    onDismissClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    canExplain: Boolean = true,
    canHide: Boolean = true
) {
    val configuration = LocalConfiguration.current
    val isLargeFont = configuration.fontScale >= 1.35f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ObservationCardRadius),
        color = CandyColors.homeCardSurface,
        tonalElevation = CandyElevation.low,
        shadowElevation = CandyElevation.low,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CandySpace.lg),
            verticalArrangement = Arrangement.spacedBy(CandySpace.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(BulbTileSize)
                        .clip(BulbTileShape)
                        .background(CandyColors.observationIconTileBg),
                    contentAlignment = Alignment.Center
                ) {
                    ChankoIcon(
                        id = ChankoIconId.SYS_15,
                        contentDescription = null,
                        size = BulbIconSize
                    )
                }

                Spacer(modifier = Modifier.width(CandySpace.md))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CandySpace.xs)
                ) {
                    Text(
                        text = stringResource(R.string.home_observation_header),
                        style = TextStyle(
                            fontFamily = Nunito,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CandyColors.textPrimary
                    )
                    Text(
                        text = observation.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = CandyColors.textPrimary
                    )
                }

                // Dismiss X button (>= 48dp touch target, AC-14 & AC-20)
                if (canHide) {
                    val dismissCd = stringResource(R.string.home_cd_observation_dismiss)
                    Box(
                        modifier = Modifier
                            .size(CandySize.touchTarget)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = dismissCd,
                                onClick = { onDismissClick(observation.id) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_03,
                            contentDescription = dismissCd,
                            size = DismissIconSize
                        )
                    }
                }
            }

            // Bottom Action Pills: "Giải thích thêm" & "Ẩn gợi ý này"
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stackButtonsVertically = maxWidth < 310.dp || isLargeFont

                if (stackButtonsVertically) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(CandySpace.sm)
                    ) {
                        if (canExplain) {
                            ExplainPillButton(
                                onClick = { onExplainClick(observation.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (canHide) {
                            HidePillButton(
                                onClick = { onHideClick(observation.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CandySpace.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canExplain) {
                            ExplainPillButton(
                                onClick = { onExplainClick(observation.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (canHide) {
                            HidePillButton(
                                onClick = { onHideClick(observation.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplainPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val explainLabel = stringResource(R.string.home_observation_explain_cta)
    Surface(
        modifier = modifier
            .heightIn(min = CandySize.touchTarget)
            .clip(CandyShape.chip)
            .clickable(
                role = Role.Button,
                onClickLabel = explainLabel,
                onClick = onClick
            ),
        shape = CandyShape.chip,
        color = CandyColors.brandPurple
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = CandySpace.lg,
                vertical = CandySpace.sm
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = explainLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun HidePillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hideLabel = stringResource(R.string.home_observation_hide_cta)
    Surface(
        modifier = modifier
            .heightIn(min = CandySize.touchTarget)
            .clip(CandyShape.chip)
            .clickable(
                role = Role.Button,
                onClickLabel = hideLabel,
                onClick = onClick
            ),
        shape = CandyShape.chip,
        color = Color.White,
        border = BorderStroke(1.5.dp, CandyColors.brandPurple.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = CandySpace.lg,
                vertical = CandySpace.sm
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = hideLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = CandyColors.brandPurple
            )
        }
    }
}
