package com.hoabh.chanko.di

import android.content.Context
import androidx.room.Room
import com.hoabh.chanko.data.chat.local.ChatRoomDatabase
import com.hoabh.chanko.data.chat.local.dao.ChatDao
import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.data.chat.repository.ChatRepositoryImpl
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideChatRoomDatabase(
        @ApplicationContext context: Context
    ): ChatRoomDatabase {
        return Room.databaseBuilder(
            context,
            ChatRoomDatabase::class.java,
            "chanko_chat.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: ChatRoomDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    @Singleton
    fun provideFakeChatDataSource(): FakeChatDataSource {
        return FakeChatDataSource()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        fakeDataSource: FakeChatDataSource
    ): ChatRepository {
        return ChatRepositoryImpl(fakeDataSource)
    }
}
