package com.hoabh.chanko.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemStatus {
    COOLING_DOWN,
    BOUGHT,
    PASSED
}

@Entity(tableName = "cooldown_items")
data class CooldownItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Double,
    val imageUri: String? = null,
    val addedTimestamp: Long,
    val cooldownDurationMillis: Long,
    val reason: String,
    val status: ItemStatus = ItemStatus.COOLING_DOWN
)
