package com.hoabh.chanko.domain.repository

import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    fun getActiveItems(): Flow<List<CooldownItem>>

    fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>>

    fun getItemById(id: Int): Flow<CooldownItem?>

    suspend fun insertItem(item: CooldownItem)

    suspend fun updateItem(item: CooldownItem)

    suspend fun updateItemStatus(id: Int, status: ItemStatus)

    suspend fun deleteItem(item: CooldownItem)

    fun getTotalSavedAmount(): Flow<Long>
}
