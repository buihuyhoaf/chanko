package com.hoabh.chanko.data.budget

import com.hoabh.chanko.domain.budget.repository.SharedItemResolver
import com.hoabh.chanko.domain.repository.ItemRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Resolver dùng ItemRepository hiện có:
 * - Chấp nhận itemId dạng số ("1", "2") hoặc có tiền tố ("item_1", "item_2").
 * - Kiểm tra món đồ thực sự có trong local database (hoặc danh sách active items theo thứ tự seed).
 * - Trả về null nếu không khả dụng để ViewModel phát thông báo "Món đồ chưa có dữ liệu ngoại tuyến".
 */
class SharedItemResolverImpl @Inject constructor(
    private val itemRepository: ItemRepository
) : SharedItemResolver {
    override suspend fun resolveRouteId(itemId: String): Int? {
        val cleaned = itemId.trim().removePrefix("item_")
        val numericId = cleaned.toIntOrNull() ?: return null
        if (numericId <= 0) return null

        val direct = runCatching { itemRepository.getItemById(numericId).first() }
            .getOrNull()
        if (direct != null) return direct.id

        // Fallback cho trường hợp auto-increment ID trong Room lệch với index fixture 1..3
        val activeItems = runCatching { itemRepository.getActiveItems().first() }
            .getOrDefault(emptyList())
        return activeItems.getOrNull(numericId - 1)?.id
    }
}

/**
 * Resolver mặc định dùng trong unit test khi khởi tạo trực tiếp BudgetViewModel(repository).
 */
class InMemorySharedItemResolver(
    private val availableItemIds: MutableSet<String> = mutableSetOf("1", "2", "3", "item_1", "item_2", "item_3")
) : SharedItemResolver {
    fun setItemAvailable(itemId: String, available: Boolean) {
        if (available) availableItemIds.add(itemId) else availableItemIds.remove(itemId)
    }

    override suspend fun resolveRouteId(itemId: String): Int? {
        val trimmed = itemId.trim()
        if (trimmed !in availableItemIds) return null
        return trimmed.removePrefix("item_").toIntOrNull()
    }
}
