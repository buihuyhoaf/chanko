package com.hoabh.chanko.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hoabh.chanko.data.local.chat.dao.ChatDao
import com.hoabh.chanko.data.local.chat.entity.ChatMessageEntity
import com.hoabh.chanko.data.local.chat.entity.ChatThreadEntity
import com.hoabh.chanko.data.local.converter.Converters
import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.data.local.dao.PurchaseDao
import com.hoabh.chanko.data.local.entity.CooldownItemEntity
import com.hoabh.chanko.data.local.entity.PurchaseTransactionEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CooldownItemEntity::class,
        PurchaseTransactionEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun chatDao(): ChatDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE cooldown_items_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            name TEXT NOT NULL, price INTEGER NOT NULL, imageUri TEXT, addedTimestamp INTEGER NOT NULL,
            cooldownDurationMillis INTEGER NOT NULL, reason TEXT NOT NULL, status TEXT NOT NULL)""")
        db.execSQL("""INSERT INTO cooldown_items_new SELECT id, name, CAST(ROUND(price) AS INTEGER),
            imageUri, addedTimestamp, cooldownDurationMillis, reason, status FROM cooldown_items""")
        db.execSQL("DROP TABLE cooldown_items")
        db.execSQL("ALTER TABLE cooldown_items_new RENAME TO cooldown_items")
    }
}
