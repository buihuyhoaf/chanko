package com.hoabh.chanko.data.local.entity

import com.hoabh.chanko.domain.model.CooldownItem

fun CooldownItemEntity.toDomain() = CooldownItem(
    id, name, price, imageUri, addedTimestamp, cooldownDurationMillis, reason, status
)
fun CooldownItem.toEntity() = CooldownItemEntity(
    id, name, price, imageUri, addedTimestamp, cooldownDurationMillis, reason, status
)
