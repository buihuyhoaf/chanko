package com.hoabh.chanko.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hoabh.chanko.data.local.converter.Converters
import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.domain.model.CooldownItem

@Database(
    entities = [CooldownItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
