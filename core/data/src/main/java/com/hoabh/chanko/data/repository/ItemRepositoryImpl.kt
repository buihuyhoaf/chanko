package com.hoabh.chanko.data.repository

import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.hoabh.chanko.data.local.entity.toDomain
import com.hoabh.chanko.data.local.entity.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getActiveItems(): Flow<List<CooldownItem>> {
        return itemDao.getActiveItems().map { items -> items.map { it.toDomain() } }
    }

    override fun getItemsByStatus(status: ItemStatus): Flow<List<CooldownItem>> {
        return itemDao.getItemsByStatus(status).map { items -> items.map { it.toDomain() } }
    }

    override fun getItemById(id: Int): Flow<CooldownItem?> {
        return itemDao.getItemById(id).map { it?.toDomain() }
    }

    override suspend fun insertItem(item: CooldownItem) {
        require(item.name.isNotBlank() && item.price > 0 && item.cooldownDurationMillis > 0)
        itemDao.insertItem(item.toEntity())
    }

    override suspend fun updateItem(item: CooldownItem) {
        require(item.name.isNotBlank() && item.price > 0 && item.cooldownDurationMillis > 0)
        itemDao.updateItem(item.toEntity())
    }

    override suspend fun updateItemStatus(id: Int, status: ItemStatus) {
        require(status != ItemStatus.COOLING_DOWN)
        check(itemDao.updateItemStatus(id, status, System.currentTimeMillis()) == 1)
    }

    override suspend fun deleteItem(item: CooldownItem) {
        itemDao.deleteItem(item.toEntity())
    }

    override fun getTotalSavedAmount(): Flow<Long> {
        return itemDao.getTotalSavedAmount()
    }
}
