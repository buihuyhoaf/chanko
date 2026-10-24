package com.hoabh.chanko.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hoabh.chanko.data.local.entity.CooldownItemEntity
import com.hoabh.chanko.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM cooldown_items WHERE isArchived = 1 ORDER BY addedTimestamp DESC")
    fun getArchivedItems(): Flow<List<CooldownItemEntity>>

    @Query("UPDATE cooldown_items SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Int, isArchived: Boolean): Int

    @Query("SELECT * FROM cooldown_items WHERE status = 'COOLING_DOWN' AND isArchived = 0 ORDER BY addedTimestamp DESC")
    fun getActiveItems(): Flow<List<CooldownItemEntity>>

    @Query("SELECT * FROM cooldown_items WHERE status = :status AND isArchived = 0 ORDER BY addedTimestamp DESC")
    fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItemEntity>>

    @Query("SELECT * FROM cooldown_items WHERE id = :id")
    fun getItemById(id: Int): Flow<CooldownItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CooldownItemEntity)

    @Update
    suspend fun updateItem(item: CooldownItemEntity)

    @Query("UPDATE cooldown_items SET status = :status WHERE id = :id AND status = 'COOLING_DOWN' AND addedTimestamp + cooldownDurationMillis <= :now")
    suspend fun updateItemStatus(id: Int, status: ItemStatus, now: Long): Int

    @Delete
    suspend fun deleteItem(item: CooldownItemEntity)

    @Query("SELECT COALESCE(SUM(price), 0) FROM cooldown_items WHERE status = 'PASSED'")
    fun getTotalSavedAmount(): Flow<Long>
}
