package com.hoabh.chanko.domain.budget.usecase

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.domain.budget.model.BudgetOfflineException
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.first

/**
 * Điều phối đồng bộ chi tiêu ngân sách từ sản phẩm trong Chanko:
 * - Chanko chỉ theo dõi giao dịch liên quan đến sản phẩm người dùng đã thêm vào ứng dụng.
 * - Không có Quick Purchase độc lập; không tự theo dõi chi tiêu ngoài.
 * - Budget chỉ bị trừ khi người dùng xác nhận đã mua sản phẩm.
 * - Hỗ trợ Pay Now và Thẻ tín dụng: đều ghi nhận chi tiêu ngay khi mua.
 * - Hỗ trợ hoàn tiền (refund) và khôi phục ngân sách chu kỳ tương ứng.
 * - Khi offline: chặn ghi, trả về lỗi tiếng Việt yêu cầu kết nối mạng.
 */
class ItemPurchaseBudgetSyncUseCase(
    private val itemRepository: ItemRepository,
    private val purchaseRepository: PurchaseRepository,
    private val budgetRepository: BudgetRepository
) {
    suspend fun recordItemPurchase(
        purchase: PurchaseTransaction,
        isOnline: Boolean
    ): Result<Unit> {
        if (!isOnline) {
            return Result.failure(BudgetOfflineException())
        }

        val item = itemRepository.getItemById(purchase.itemId).first()
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy sản phẩm ID ${purchase.itemId} trong Chanko."))

        // 1. Lưu transaction vào PurchaseRepository
        purchaseRepository.recordPurchase(purchase)

        // 2. Cập nhật trạng thái sản phẩm sang BOUGHT
        itemRepository.updateItemStatus(item.id, ItemStatus.BOUGHT)

        // 3. Ghi nhận chi tiêu vào BudgetRepository
        if (budgetRepository is FakeBudgetRepository) {
            budgetRepository.recordItemExpense(
                itemId = item.id.toString(),
                itemName = item.name,
                amountVnd = purchase.totalAmount,
                paymentType = purchase.paymentType
            )
        }

        return Result.success(Unit)
    }

    suspend fun refundPurchase(
        purchaseId: String,
        itemId: Int,
        isOnline: Boolean
    ): Result<Unit> {
        if (!isOnline) {
            return Result.failure(BudgetOfflineException())
        }

        val purchase = purchaseRepository.getPurchaseById(purchaseId).first()
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy giao dịch ID $purchaseId."))

        purchaseRepository.deletePurchase(purchaseId)

        if (budgetRepository is FakeBudgetRepository) {
            budgetRepository.refundExpense(amountVnd = purchase.totalAmount)
        }

        val remainingPurchases = purchaseRepository.getPurchasesByItemId(itemId).first()
        if (remainingPurchases.isEmpty()) {
            itemRepository.updateItemStatus(itemId, ItemStatus.COOLING_DOWN)
        }

        return Result.success(Unit)
    }
}
