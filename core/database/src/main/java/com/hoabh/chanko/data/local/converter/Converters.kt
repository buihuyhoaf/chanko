package com.hoabh.chanko.data.local.converter

import androidx.room.TypeConverter
import com.hoabh.chanko.domain.model.ItemStatus
import com.hoabh.chanko.domain.model.PaymentType

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

    @TypeConverter
    @JvmStatic
    fun fromPaymentType(type: PaymentType): String {
        return type.name
    }

    @TypeConverter
    @JvmStatic
    fun toPaymentType(value: String): PaymentType {
        return PaymentType.valueOf(value)
    }
}
