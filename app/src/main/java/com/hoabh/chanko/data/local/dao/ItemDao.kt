package com.hoabh.chanko.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM cooldown_items WHERE status = 'COOLING_DOWN' ORDER BY addedTimestamp DESC")
    fun getActiveItems(): Flow<List<CooldownItem>>

    @Query("SELECT * FROM cooldown_items WHERE status = :status ORDER BY addedTimestamp DESC")
    fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>>

    @Query("SELECT * FROM cooldown_items WHERE id = :id")
    fun getItemById(id: Int): Flow<CooldownItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CooldownItem)

    @Update
    suspend fun updateItem(item: CooldownItem)

    @Query("UPDATE cooldown_items SET status = :status WHERE id = :id")
    suspend fun updateItemStatus(id: Int, status: ItemStatus)

    @Delete
    suspend fun deleteItem(item: CooldownItem)

    @Query("SELECT SUM(price) FROM cooldown_items WHERE status = 'PASSED'")
    fun getTotalSavedAmount(): Flow<Double?>
}
