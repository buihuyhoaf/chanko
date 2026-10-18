package com.hoabh.chanko.di

import com.hoabh.chanko.core.network.NetworkClient
import com.hoabh.chanko.core.network.NetworkConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun config(): NetworkConfig = NetworkConfig()

    @Provides @Singleton
    fun json(): Json = NetworkClient.json()

    @Provides @Singleton
    fun client(config: NetworkConfig): OkHttpClient = NetworkClient.httpClient(config)

    @Provides @Singleton
    fun retrofit(config: NetworkConfig, client: OkHttpClient, json: Json): Retrofit =
        NetworkClient.retrofit(config, client, json)
}
