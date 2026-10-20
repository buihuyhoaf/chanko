package com.hoabh.chanko.di

import com.hoabh.chanko.data.repository.ItemRepositoryImpl
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.presentation.screens.home.data.HomeSpendingRepository
import com.hoabh.chanko.presentation.screens.home.data.InMemoryHomeSpendingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        itemRepositoryImpl: ItemRepositoryImpl
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindHomeSpendingRepository(
        homeSpendingRepositoryImpl: InMemoryHomeSpendingRepository
    ): HomeSpendingRepository
}
