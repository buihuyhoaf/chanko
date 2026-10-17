package com.hoabh.chanko

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hoabh.chanko.data.local.AppDatabase
import com.hoabh.chanko.data.local.MIGRATION_1_2
import com.hoabh.chanko.data.local.entity.CooldownItemEntity
import com.hoabh.chanko.domain.model.ItemStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemDatabaseTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun decisionIsAtomicAndSavingsOnlyIncludePassedItems() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.itemDao()
            assertEquals(0L, dao.getTotalSavedAmount().first())
            dao.insertItem(CooldownItemEntity(1, "Tai nghe", 500_000, null, 1000, 2000, "", ItemStatus.COOLING_DOWN))
            assertEquals(0, dao.updateItemStatus(1, ItemStatus.PASSED, 2999))
            assertEquals(1, dao.updateItemStatus(1, ItemStatus.PASSED, 3000))
            assertEquals(0, dao.updateItemStatus(1, ItemStatus.BOUGHT, 4000))
            assertEquals(500_000L, dao.getTotalSavedAmount().first())
            assertTrue(dao.getActiveItems().first().isEmpty())
            assertEquals(ItemStatus.PASSED, dao.getItemById(1).first()?.status)
        } finally { db.close() }
    }

    @Test fun migrationPreservesItemsAndConvertsVndToInteger() = runBlocking {
        val name = "migration-test.db"
        context.deleteDatabase(name)
        context.openOrCreateDatabase(name, 0, null).use { old ->
            old.execSQL("""CREATE TABLE cooldown_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL, price REAL NOT NULL, imageUri TEXT, addedTimestamp INTEGER NOT NULL,
                cooldownDurationMillis INTEGER NOT NULL, reason TEXT NOT NULL, status TEXT NOT NULL)""")
            old.execSQL("INSERT INTO cooldown_items VALUES (7, 'Tai nghe', 500000.4, NULL, 1000, 2000, 'Làm việc', 'PASSED')")
            old.version = 1
        }
        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).addMigrations(MIGRATION_1_2).build()
        try {
            val item = db.itemDao().getItemById(7).first()
            assertEquals(500_000L, item?.price)
            assertEquals("Làm việc", item?.reason)
            assertEquals(ItemStatus.PASSED, item?.status)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
