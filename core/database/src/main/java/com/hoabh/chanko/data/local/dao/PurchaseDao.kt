package com.hoabh.chanko.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hoabh.chanko.data.local.entity.PurchaseTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Query("SELECT * FROM purchase_transactions WHERE itemId = :itemId ORDER BY purchasedTimestamp DESC")
    fun getPurchasesByItemId(itemId: Int): Flow<List<PurchaseTransactionEntity>>

    @Query("SELECT * FROM purchase_transactions WHERE id = :id")
    fun getPurchaseById(id: String): Flow<PurchaseTransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseTransactionEntity)

    @Update
    suspend fun updatePurchase(purchase: PurchaseTransactionEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseTransactionEntity)

    @Query("DELETE FROM purchase_transactions WHERE id = :id")
    suspend fun deletePurchaseById(id: String)
}
