package com.hoabh.chanko.di

import com.hoabh.chanko.domain.additem.*
import com.hoabh.chanko.domain.additem.UrlExtractionService
import com.hoabh.chanko.domain.additem.UnavailableUrlExtractionService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AddItemModule {
    @Provides fun extraction(): UrlExtractionService = UnavailableUrlExtractionService()
    @Provides fun evaluation(): QuickCheckEvaluationContract = UnavailableQuickCheckEngine()
}
