package com.hoabh.chanko.presentation.designsystem.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Component vẽ icon chuẩn mực của Chanko.
 *
 * @param id Định danh icon trong bộ 66 icon Candy Duotone.
 * @param contentDescription Nhãn mô tả tiếng Việt cho trợ năng (TalkBack).
 *   Đặt null khi icon mang tính trang trí và đã có text/nhãn cạnh bên.
 * @param modifier Modifier tùy biến. Kích thước mặc định là 24.dp.
 * @param selected Trạng thái kích hoạt/chọn (thường dùng cho Tab Navigation hoặc Toggle).
 * @param enabled Trạng thái khả dụng. Khi false, icon tự động chuyển sang biến thể disabled.
 * @param size Kích thước icon (mặc định 24.dp).
 */
@Composable
fun ChankoIcon(
    id: ChankoIconId,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 24.dp,
) {
    val resource = when {
        !enabled -> id.disabled
        selected -> id.selected
        else -> id.normal
    }

    Icon(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = Color.Unspecified, // Bảo toàn cấu trúc màu đa lớp của Candy Duotone
    )
}
