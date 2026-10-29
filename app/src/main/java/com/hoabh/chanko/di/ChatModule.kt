package com.hoabh.chanko.di

import com.hoabh.chanko.data.chat.remote.FakeChatDataSource
import com.hoabh.chanko.data.chat.repository.ChatRepositoryImpl
import com.hoabh.chanko.data.local.chat.dao.ChatDao
import com.hoabh.chanko.domain.chat.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideFakeChatDataSource(): FakeChatDataSource {
        return FakeChatDataSource()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        fakeDataSource: FakeChatDataSource
    ): ChatRepository {
        return ChatRepositoryImpl(chatDao, fakeDataSource)
    }
}
