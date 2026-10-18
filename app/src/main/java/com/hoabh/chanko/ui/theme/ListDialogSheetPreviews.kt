@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.hoabh.chanko.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Cells 100%", showBackground = true)
@Preview(name = "Cells 150%", fontScale = 1.5f, showBackground = true)
@Preview(name = "Cells 200% narrow", fontScale = 2f, widthDp = 320, showBackground = true)
@Composable
private fun ListCellsPreview() = ChankoTheme {
    Column(verticalArrangement = Arrangement.spacedBy(CandySpace.sm)) {
        CandyListCell("Tên món đồ dài để kiểm tra xuống dòng", supportingText = "Giá dự kiến", value = "123.456.789 ₫")
        CandyListCell("Xem chi tiết", onClick = {})
        CandyListCell("Chưa thể thao tác", enabled = false, onClick = {})
    }
}

@Preview(name = "Dialog 100%")
@Preview(name = "Dialog 150%", fontScale = 1.5f)
@Preview(name = "Dialog 200% narrow", fontScale = 2f, widthDp = 320)
@Composable
private fun ConfirmationPreview() = ChankoTheme {
    CandyConfirmationDialog("Xác nhận thao tác", "Kiểm tra thông tin trước khi tiếp tục.",
        "Xác nhận", "Quay lại", {}, {}, destructive = true,
        errorMessage = "Chưa thể hoàn tất. Vui lòng thử lại.")
}

@Preview(name = "Dialog pending", fontScale = 2f)
@Composable
private fun PendingPreview() = ChankoTheme {
    CandyConfirmationDialog("Đang xử lý", "Thông tin được giữ trong khi chờ.", "Xác nhận", "Quay lại", {}, {}, loading = true)
}

@Preview(name = "Sheet 100%")
@Preview(name = "Sheet 150%", fontScale = 1.5f)
@Preview(name = "Sheet 200% narrow", fontScale = 2f, widthDp = 320)
@Composable
private fun BottomSheetPreview() = ChankoTheme {
    CandyBottomSheet("Thông tin bổ sung", {}) {
        Text("Nội dung dài có thể xuống dòng và cuộn khi không đủ chiều cao.")
        CandyButton({}, Modifier.fillMaxWidth()) { Text("Đóng bảng thông tin") }
    }
}
