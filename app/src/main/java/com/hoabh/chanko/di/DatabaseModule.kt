package com.hoabh.chanko.di

import android.content.Context
import androidx.room.Room
import com.hoabh.chanko.data.local.AppDatabase
import com.hoabh.chanko.data.local.dao.ItemDao
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
        ).build()
    }

    @Provides
    @Singleton
    fun provideItemDao(database: AppDatabase): ItemDao {
        return database.itemDao()
    }
}
