package com.hoabh.chanko.di

import com.hoabh.chanko.data.chat.fake.FakeChatRepository
import com.hoabh.chanko.data.chat.repository.ChatRepositoryImpl
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
    fun provideFakeChatRepository(): FakeChatRepository {
        return FakeChatRepository()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        fakeChatRepository: FakeChatRepository
    ): ChatRepository {
        return ChatRepositoryImpl(fakeChatRepository)
    }
}
