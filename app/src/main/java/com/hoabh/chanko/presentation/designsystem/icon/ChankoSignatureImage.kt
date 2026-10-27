package com.hoabh.chanko.presentation.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoabh.chanko.R

/**
 * 8 Artwork 3D Signature của Chanko (C01 - C08).
 * Các hình ảnh WebP 512×512 trong suốt chất lượng cao phục vụ các điểm chạm trải nghiệm cảm xúc lớn.
 */
enum class ChankoSignatureId(
    @get:DrawableRes val drawableRes: Int,
    val titleVi: String,
) {
    C01_QUICK_CHECK(R.drawable.chanko_c01_quick_check, "Đánh giá nhanh"),
    C02_COOLING_OFF(R.drawable.chanko_c02_cooling_off, "Hạ nhiệt mua sắm"),
    C03_READY_FOR_REVIEW(R.drawable.chanko_c03_ready_for_review, "Sẵn sàng xem lại"),
    C04_PAUSE_JAR(R.drawable.chanko_c04_pause_jar, "Hũ tạm dừng"),
    C05_BUDGET_WALLET(R.drawable.chanko_c05_budget_wallet, "Ví ngân sách"),
    C06_MY_ITEMS(R.drawable.chanko_c06_my_items, "Món đồ của tôi"),
    C07_INSIGHTS(R.drawable.chanko_c07_insights, "Thống kê chi tiêu"),
    C08_CHANKO_CHAT(R.drawable.chanko_c08_chanko_chat, "Trò chuyện cùng Chanko"),
}

/**
 * Component hiển thị artwork 3D Signature không áp dụng tint màu.
 */
@Composable
fun ChankoSignatureImage(
    id: ChankoSignatureId,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Image(
        painter = painterResource(id.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = contentScale,
    )
}
