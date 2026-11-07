package com.hoabh.chanko.presentation.features.budget.gallery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.domain.budget.model.MoneyVnd
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Thẻ tóm tắt tổng ngân sách năm (Annual Summary Section) cho Budget Gallery (AC05).
 * - Nền kính trắng lavender, viền mỏng
 * - Tổng hạn mức đã thiết lập trong năm (không tính nháp/gợi ý)
 * - Số tháng đã thiết lập
 * - Ảnh minh họa lịch hồng (budget_summary_calendar_pink.png, 48–64dp) ở góc phải
 * - Cảnh báo ngoại tuyến nếu mất kết nối mạng
 */
@Composable
fun AnnualSummarySection(
    annualTotalMinor: Long,
    configuredMonthsCount: Int,
    totalMonthsInYear: Int,
    isOnline: Boolean,
    lastSyncedAt: Long?,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = cardShape)
            .clip(cardShape)
            .background(Color.White)
            .border(1.dp, CandyColors.homeCardBorder, cardShape)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tổng ngân sách năm",
                        fontFamily = Nunito,
                        fontSize = 13.sp,
                        color = CandyColors.textSecondary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(CandyColors.backgroundLavender)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$configuredMonthsCount/$totalMonthsInYear tháng",
                            fontFamily = Nunito,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CandyColors.brandPurple
                        )
                    }
                }

                Text(
                    text = MoneyVnd.format(annualTotalMinor),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = CandyColors.textPrimary
                )

                if (!isOnline) {
                    Text(
                        text = "Đang xem dữ liệu ngoại tuyến",
                        fontFamily = Nunito,
                        fontSize = 12.sp,
                        color = CandyColors.overBudget
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.budget_summary_calendar_pink),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
