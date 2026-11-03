package com.hoabh.chanko.presentation.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.presentation.screens.home.ObservationUi
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.CandySpace

private val ObservationCornerShape = RoundedCornerShape(24.dp)

/**
 * Khu vực 4 — Observation ("Góc nhìn từ Chanko" — Home v2.0 — AC-26, AC-27, AC-31):
 * - Thiết kế theo `reference/home-v2-approved.png` và `VISUAL-RECONCILIATION.md`:
 *   - Mèo Chanko trang trí ló đầu góc trái dưới (không nhận focus TalkBack).
 *   - Icon bóng đèn + tiêu đề "Góc nhìn từ Chanko".
 *   - Nội dung nhận xét dựa trên dữ liệu thực.
 *   - Nút mũi tên tròn `>` (vùng chạm >= 48dp) mở giải thích chi tiết.
 *   - Nút ẩn gợi ý (`X` / "Ẩn gợi ý này", vùng chạm >= 48dp) lưu bền vững theo `insightId`.
 */
@Composable
fun ObservationCard(
    observation: ObservationUi,
    onExplainClick: () -> Unit,
    onHideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val explainCd = stringResource(R.string.home_cd_observation_explain)
    val dismissCd = stringResource(R.string.home_cd_observation_dismiss)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = ObservationCornerShape,
                ambientColor = CandyColors.primarySoft,
                spotColor = CandyColors.primarySoft
            )
            .clip(ObservationCornerShape)
            .clickable(
                role = Role.Button,
                onClickLabel = explainCd,
                onClick = onExplainClick
            ),
        shape = ObservationCornerShape,
        color = CandyColors.dialogueBubbleSurface,
        border = BorderStroke(1.dp, CandyColors.homeCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = CandySpace.md,
                    end = CandySpace.sm,
                    top = CandySpace.md,
                    bottom = CandySpace.md
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CandySpace.sm)
            ) {
                // Mèo Chanko trang trí bên trái (chỉ trang trí — AC-31)
                Image(
                    painter = painterResource(id = R.drawable.cat_mascot),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(64.dp)
                        .clearAndSetSemantics {}
                )

                // Nội dung chính ở giữa
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.BUD_05,
                            contentDescription = null,
                            size = 18.dp
                        )
                        Text(
                            text = stringResource(R.string.home_observation_header),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            ),
                            color = CandyColors.primaryInk
                        )
                    }

                    Text(
                        text = observation.text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = CandyColors.HomeV2.textPrimary
                    )
                }

                // Cột hành động bên phải: Nút ẩn (X) + Nút mũi tên mở giải thích (>)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Control ẩn có nhãn accessible rõ ràng (VISUAL-RECONCILIATION.md, AC-26, AC-31)
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = dismissCd,
                                onClick = onHideClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ChankoIcon(
                            id = ChankoIconId.ACT_07,
                            contentDescription = dismissCd,
                            size = 16.dp
                        )
                    }

                    // Nút mũi tên tròn `>` mở giải thích chi tiết (AC-27)
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = explainCd,
                                onClick = onExplainClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CandyColors.primarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "›",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                ),
                                color = CandyColors.brandPurple
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hộp thoại giải thích chi tiết cho "Góc nhìn từ Chanko" (AC-27):
 * Hiển thị phân tích chi tiết + dẫn chứng dữ liệu thực + tuỳ chọn "Đã hiểu" hoặc "Ẩn gợi ý này".
 */
@Composable
fun InsightExplanationDialog(
    observation: ObservationUi,
    onDismiss: () -> Unit,
    onHideInsight: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ObservationCornerShape,
        containerColor = CandyColors.homeCardSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CandySpace.xs)
            ) {
                ChankoIcon(
                    id = ChankoIconId.BUD_05,
                    contentDescription = null,
                    size = 22.dp
                )
                Text(
                    text = stringResource(R.string.home_observation_header),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CandyColors.HomeV2.textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
                Text(
                    text = observation.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CandyColors.HomeV2.textPrimary
                )
                if (observation.detailedExplanation.isNotBlank()) {
                    Text(
                        text = observation.detailedExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CandyColors.HomeV2.textSecondary
                    )
                }
                if (observation.evidence.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CandyColors.primarySoft
                    ) {
                        Text(
                            text = observation.evidence,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = CandyColors.primaryInk,
                            modifier = Modifier.padding(CandySpace.sm)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandyColors.brandPurple,
                    contentColor = Color.White
                ),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_observation_dialog_understood),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onHideInsight(observation.id) },
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_observation_hide_cta),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = CandyColors.HomeV2.textSecondary
                )
            }
        }
    )
}
