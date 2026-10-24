package com.hoabh.chanko.domain.repository

import com.hoabh.chanko.domain.model.PurchaseTransaction
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun getPurchasesByItemId(itemId: Int): Flow<List<PurchaseTransaction>>
    fun getPurchaseById(id: String): Flow<PurchaseTransaction?>
    suspend fun recordPurchase(purchase: PurchaseTransaction)
    suspend fun updatePurchase(purchase: PurchaseTransaction)
    suspend fun deletePurchase(purchaseId: String)
}
