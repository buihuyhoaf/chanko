package com.hoabh.chanko.presentation.features.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.features.home.BudgetCardRole
import com.hoabh.chanko.ui.theme.CandyBudgetThemes
import com.hoabh.chanko.ui.theme.CandySpace

private val PlaceholderCornerShape = RoundedCornerShape(CandyBudgetThemes.cardRadius)
private val PlaceholderMinHeight = CandyBudgetThemes.cardMinHeight

/**
 * Thẻ Trung Tính Placeholder cho Chu Kỳ Tiếp Theo Chưa Cấu Hình (Home v2.2 - BC22-05, BC-CARD-LABEL-01, BC-CARD-CONTENT-01):
 * - Nền trắng ngà/xám pastel (#FBF9FD), viền nét đứt native (#82758E, dash = 6dp, gap = 4dp).
 * - Góc trên bên trái có Month Chip pill MM (01..12).
 * - Hình minh họa lịch (next_calendar.png) ở góc phải trên lớp sóng mờ.
 * - Tiêu đề: "Chu kỳ tiếp theo", phụ đề: "Chưa thiết lập ngân sách".
 * - Nút CTA dạng pill "+ Thiết lập ngân sách" nền tím #6326A8, chữ trắng.
 * - Loại bỏ hoàn toàn text trang trí thương hiệu CHANKO / SHOPPING BUDGET.
 */
@Composable
fun BudgetPlaceholderCard(
    monthLabel: String,
    cycleLabel: String = "",
    dateRangeText: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: BudgetCardRole = BudgetCardRole.NextPlaceholder
) {
    val borderColor = CandyBudgetThemes.placeholderBorder
    val bgColor = CandyBudgetThemes.placeholderBackground
    val ctaFill = CandyBudgetThemes.placeholderCtaFill
    val ctaText = CandyBudgetThemes.placeholderCtaText
    val titleText = stringResource(R.string.home_budget_placeholder_title)
    val subtitleText = stringResource(R.string.home_budget_placeholder_unconfigured_subtitle)
    val ctaLabel = stringResource(R.string.home_budget_placeholder_cta)

    val cardCd = buildString {
        append("$titleText, ")
        if (monthLabel.isNotBlank()) append("tháng $monthLabel. ")
        append("$subtitleText. ")
        append("Chạm để $ctaLabel.")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = PlaceholderMinHeight)
            .clip(PlaceholderCornerShape)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics(mergeDescendants = true) {
                contentDescription = cardCd
                this.role = Role.Button
            },
        shape = PlaceholderCornerShape,
        color = bgColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Viền nét đứt native ôm sát mép thẻ (28.dp radius)
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 1.5.dp.toPx()
                val dash = 6.dp.toPx()
                val gap = 4.dp.toPx()
                val radiusPx = CandyBudgetThemes.cardRadius.toPx()
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)
                    )
                )
            }

            // 2. Lớp sóng pastel mờ ở góc dưới bên phải (next-placeholder visual reference)
            Canvas(modifier = Modifier.matchParentSize()) {
                val waveColor = CandyBudgetThemes.placeholderWave
                val wavePath = Path().apply {
                    moveTo(size.width * 0.48f, size.height)
                    cubicTo(
                        size.width * 0.58f, size.height * 0.78f,
                        size.width * 0.72f, size.height * 0.60f,
                        size.width * 1.05f, size.height * 0.62f
                    )
                    lineTo(size.width * 1.05f, size.height)
                    close()
                }
                drawPath(wavePath, waveColor)
            }

            // 3. Hình minh họa lịch với dấu chân (next_calendar.png) căn giữa dọc ở góc phải
            Image(
                painter = painterResource(id = R.drawable.next_calendar),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
                    .size(96.dp)
                    .clearAndSetSemantics {}
            )

            // 4. Cột nội dung chính với padding chuẩn
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(CandyBudgetThemes.contentPadding)
            ) {
                // TopBar: Month Chip pill bên trái, không chứa brand text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (monthLabel.isNotBlank()) {
                        BudgetMonthChip(
                            monthLabel = monthLabel,
                            containerColor = CandyBudgetThemes.placeholderBadgeBg,
                            contentColor = Color.White
                        )
                    }
                }

                // Vùng giữa: Tiêu đề + phụ đề căn giữa theo chiều dọc (Center Vertical)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.68f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = CandyBudgetThemes.textDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = CandyBudgetThemes.placeholderBorder
                        )
                    }
                }

                // Nút CTA dạng pill tím #6326A8, chữ trắng (BC-CARD-CONTENT-01)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ctaFill,
                    shadowElevation = 2.dp,
                    modifier = Modifier.heightIn(min = 40.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ctaLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = ctaText
                        )
                    }
                }
            }
        }
    }
}
