package com.hoabh.chanko.data.repository

import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.data.local.dao.PurchaseDao
import com.hoabh.chanko.data.local.entity.toDomain
import com.hoabh.chanko.data.local.entity.toEntity
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PurchaseTransaction
import com.hoabh.chanko.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepositoryImpl @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val itemDao: ItemDao
) : PurchaseRepository {

    override fun getPurchasesByItemId(itemId: Int): Flow<List<PurchaseTransaction>> {
        return purchaseDao.getPurchasesByItemId(itemId).map { list -> list.map { it.toDomain() } }
    }

    override fun getPurchaseById(id: String): Flow<PurchaseTransaction?> {
        return purchaseDao.getPurchaseById(id).map { it?.toDomain() }
    }

    override suspend fun recordPurchase(purchase: PurchaseTransaction) {
        require(purchase.actualUnitPrice >= 0 && purchase.quantity >= 1)
        purchaseDao.insertPurchase(purchase.toEntity())
        // Cập nhật trạng thái item sang BOUGHT
        val item = itemDao.getItemById(purchase.itemId)
        // Khi ghi nhận purchase, chuyển item sang trạng thái BOUGHT
        itemDao.updateItemStatus(purchase.itemId, ItemStatus.BOUGHT, System.currentTimeMillis())
    }

    override suspend fun updatePurchase(purchase: PurchaseTransaction) {
        require(purchase.actualUnitPrice >= 0 && purchase.quantity >= 1)
        purchaseDao.updatePurchase(purchase.toEntity())
    }

    override suspend fun deletePurchase(purchaseId: String) {
        purchaseDao.deletePurchaseById(purchaseId)
    }
}
