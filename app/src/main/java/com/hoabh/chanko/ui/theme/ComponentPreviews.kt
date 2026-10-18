package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Components 100%", showBackground = true, widthDp = 360)
@Preview(name = "Components 150%", showBackground = true, widthDp = 360, fontScale = 1.5f)
@Preview(name = "Components 200%", showBackground = true, widthDp = 360, fontScale = 2f)
@Composable
private fun CandyComponentsPreview() {
    ChankoTheme {
        Column(Modifier.padding(CandySpace.lg), verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
            CandyButtonVariant.entries.forEach { variant ->
                CandyButton({}, variant = variant) { Text(variant.name) }
            }
            CandyPrimaryButton({}, loading = true) { Text("Lưu món đồ") }
            CandyPrimaryButton({}, enabled = false) { Text("Chưa đủ thông tin") }
            CandyCard { Text("Tên món đồ dài được phép xuống dòng"); Text("1.234.567 ₫") }
            CandyActionCard({}) { Text("Xem lại nhu cầu của bạn") }
            CandyStatus.entries.forEach { CandyStatusChip(it.name, it) }
            CandyFilterChip("Đang cân nhắc", true, {})
            CandyFilterChip("Tất cả", false, {})
            CandyFilterChip("Bộ lọc chưa khả dụng", false, {}, enabled = false)
        }
    }
}
