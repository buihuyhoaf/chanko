package com.hoabh.chanko.di

import com.hoabh.chanko.data.budget.FakeBudgetRepository
import com.hoabh.chanko.data.repository.ItemRepositoryImpl
import com.hoabh.chanko.data.repository.PurchaseRepositoryImpl
import com.hoabh.chanko.domain.budget.repository.BudgetRepository
import com.hoabh.chanko.domain.repository.ItemRepository
import com.hoabh.chanko.domain.repository.PurchaseRepository
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
    abstract fun bindPurchaseRepository(
        purchaseRepositoryImpl: PurchaseRepositoryImpl
    ): PurchaseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        fakeBudgetRepository: FakeBudgetRepository
    ): BudgetRepository
}
