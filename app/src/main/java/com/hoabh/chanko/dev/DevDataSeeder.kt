package com.hoabh.chanko.dev

import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.data.local.entity.CooldownItemEntity
import com.hoabh.chanko.domain.model.ItemStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tự động nạp dữ liệu mẫu ban đầu cho môi trường dev
 * để lập trình viên có sẵn màn hình với đầy đủ trạng thái dữ liệu.
 */
@Singleton
class DevDataSeeder @Inject constructor(
    private val itemDao: ItemDao
) {
    suspend fun seedIfEmpty() {
        val currentItems = itemDao.getActiveItems().first()
        if (currentItems.isNotEmpty()) return

        val now = System.currentTimeMillis()

        // 1. Món đồ đã hết thời gian hạ nhiệt, sẵn sàng xem lại
        val readyItem = CooldownItemEntity(
            name = "Tai nghe Sony WH-1000XM5",
            price = 6_500_000L,
            imageUri = null,
            addedTimestamp = now - (3 * 86_400_000L),
            cooldownDurationMillis = 2 * 86_400_000L,
            reason = "Cần tai nghe chống ồn tốt để tập trung làm việc",
            status = ItemStatus.COOLING_DOWN
        )

        // 2. Món đồ đang trong thời gian hạ nhiệt
        val coolingItem = CooldownItemEntity(
            name = "Bàn phím cơ NuPhy Air75",
            price = 2_800_000L,
            imageUri = null,
            addedTimestamp = now - (12 * 3600_000L),
            cooldownDurationMillis = 3 * 86_400_000L,
            reason = "Bàn phím hiện tại gõ hơi mỏi tay",
            status = ItemStatus.COOLING_DOWN
        )

        // 3. Món đồ đã bỏ qua (tính vào số tiền đã tiết kiệm)
        val passedItem = CooldownItemEntity(
            name = "Đồng hồ thông minh Smartwatch",
            price = 4_500_000L,
            imageUri = null,
            addedTimestamp = now - (7 * 86_400_000L),
            cooldownDurationMillis = 3 * 86_400_000L,
            reason = "Cân nhắc kỹ thấy chưa thực sự cần thiết lúc này",
            status = ItemStatus.PASSED
        )

        itemDao.insertItem(readyItem)
        itemDao.insertItem(coolingItem)
        itemDao.insertItem(passedItem)
    }
}
