package com.hoabh.chanko.presentation.features.budget.gallery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoabh.chanko.R
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIcon
import com.hoabh.chanko.presentation.designsystem.icon.ChankoIconId
import com.hoabh.chanko.ui.theme.CandyColors
import com.hoabh.chanko.ui.theme.Nunito

/**
 * Header của màn hình Budget Gallery v3 (AC25).
 * - Tiêu đề: "Ngân sách"
 * - Mô tả: "Theo dõi ngân sách mỗi tháng, mua sắm có suy nghĩ hơn. ✨"
 * - Mèo đếm tiền (budget_header_cat_counting_money.png, 88–104dp) ở góc phải
 * - Nút more ba chấm dọc (chanko_act_10_default, touch target 48dp)
 * - Dropdown Menu: "Giới thiệu về ngân sách" và "Đồng bộ dữ liệu"
 */
@Composable
fun BudgetGalleryHeader(
    onAboutClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = "Ngân sách",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = CandyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Theo dõi ngân sách mỗi tháng, mua sắm có suy nghĩ hơn. ✨",
                fontFamily = Nunito,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = CandyColors.textSecondary
            )
        }

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.budget_header_cat_counting_money),
                contentDescription = null,
                modifier = Modifier.size(92.dp),
                contentScale = ContentScale.Fit
            )

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    ChankoIcon(
                        id = ChankoIconId.ACT_10,
                        contentDescription = "Tùy chọn ngân sách",
                        size = 24.dp
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Giới thiệu về ngân sách",
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = CandyColors.textPrimary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onAboutClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Đồng bộ dữ liệu",
                                fontFamily = Nunito,
                                fontSize = 14.sp,
                                color = CandyColors.textPrimary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onSyncClick()
                        }
                    )
                }
            }
        }
    }
}
