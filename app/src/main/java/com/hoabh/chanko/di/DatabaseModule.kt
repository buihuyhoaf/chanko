package com.hoabh.chanko.di

import android.content.Context
import androidx.room.Room
import com.hoabh.chanko.data.local.MIGRATION_1_2
import com.hoabh.chanko.data.local.AppDatabase
import com.hoabh.chanko.data.local.chat.dao.ChatDao
import com.hoabh.chanko.data.local.dao.ItemDao
import com.hoabh.chanko.data.local.dao.PurchaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "chanko_database"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    }

    @Provides
    @Singleton
    fun provideItemDao(database: AppDatabase): ItemDao {
        return database.itemDao()
    }

    @Provides
    @Singleton
    fun providePurchaseDao(database: AppDatabase): PurchaseDao {
        return database.purchaseDao()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }
}
