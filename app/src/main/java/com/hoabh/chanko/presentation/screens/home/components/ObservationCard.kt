package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.ObservationUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandyElevation
import com.hoabh.chanko.ui.theme.CandyShape
import com.hoabh.chanko.ui.theme.CandySize
import com.hoabh.chanko.ui.theme.CandySpace

private val BulbTileSize = 40.dp
private val BulbTileShape = RoundedCornerShape(12.dp)
private val BulbIconSize = 22.dp
private val DismissIconSize = 18.dp

/**
 * HOME-06 — Observation Card (`"Góc nhìn hôm nay"`, v2 Image-Aligned):
 * - Icon bóng đèn vàng trong ô vuông bo góc lavender bên trái.
 * - Tiêu đề `"Góc nhìn hôm nay"`, nút đóng `X` góc phải (`onDismissClick`).
 * - Nội dung nhận xét in đậm + dòng bằng chứng (evidence) màu nhạt.
 * - Hai nút pill bên dưới: `"Giải thích thêm"` (`onExplainClick`) và `"Ẩn gợi ý này"` (`onHideClick`).
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CandyShape.card,
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
                // Information / Context Tile
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
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = CandyColors.textSecondary
                    )
                    Text(
                        text = observation.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = CandyColors.textPrimary
                    )
                    if (observation.evidence.isNotBlank()) {
                        Text(
                            text = observation.evidence,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = CandyColors.textSecondary
                        )
                    }
                }

                // Dismiss X button
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

            // Bottom Action Pills: "Giải thích thêm" & "Ẩn gợi ý này"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canExplain) {
                    val explainLabel = stringResource(R.string.home_observation_explain_cta)
                    Surface(
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(CandyShape.chip)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = explainLabel,
                                onClick = { onExplainClick(observation.id) }
                            ),
                        shape = CandyShape.chip,
                        color = CandyColors.primarySoft
                    ) {
                        Box(
                            modifier = Modifier.padding(
                                horizontal = CandySpace.md,
                                vertical = CandySpace.xs
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = explainLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = CandyColors.primaryInk
                            )
                        }
                    }
                }

                if (canHide) {
                    val hideLabel = stringResource(R.string.home_observation_hide_cta)
                    Surface(
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(CandyShape.chip)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = hideLabel,
                                onClick = { onHideClick(observation.id) }
                            ),
                        shape = CandyShape.chip,
                        color = CandyColors.decisionSavedRowBg,
                        border = BorderStroke(1.dp, CandyColors.divider)
                    ) {
                        Box(
                            modifier = Modifier.padding(
                                horizontal = CandySpace.md,
                                vertical = CandySpace.xs
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hideLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CandyColors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
