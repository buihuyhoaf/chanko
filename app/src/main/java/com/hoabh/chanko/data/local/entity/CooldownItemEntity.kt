package com.hoabh.chanko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hoabh.chanko.domain.model.CooldownItem
import com.hoabh.chanko.domain.model.ItemStatus

@Entity(tableName = "cooldown_items")
data class CooldownItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Long,
    val imageUri: String?,
    val addedTimestamp: Long,
    val cooldownDurationMillis: Long,
    val reason: String,
    val status: ItemStatus
)

fun CooldownItemEntity.toDomain() = CooldownItem(
    id, name, price, imageUri, addedTimestamp, cooldownDurationMillis, reason, status
)
fun CooldownItem.toEntity() = CooldownItemEntity(
    id, name, price, imageUri, addedTimestamp, cooldownDurationMillis, reason, status
)
