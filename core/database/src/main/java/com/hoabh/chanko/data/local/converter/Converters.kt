package com.hoabh.chanko.data.local.converter

import androidx.room.TypeConverter
import com.hoabh.chanko.domain.model.ItemStatus

object Converters {

    @TypeConverter
    @JvmStatic
    fun fromItemStatus(status: ItemStatus): String {
        return status.name
    }

    @TypeConverter
    @JvmStatic
    fun toItemStatus(value: String): ItemStatus {
        return ItemStatus.valueOf(value)
    }
}
