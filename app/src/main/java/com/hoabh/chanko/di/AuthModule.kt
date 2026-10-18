package com.hoabh.chanko.di

import android.content.Context
import com.hoabh.chanko.BuildConfig
import com.hoabh.chanko.core.network.NetworkConfig
import com.hoabh.chanko.data.auth.*
import com.hoabh.chanko.domain.auth.GoogleAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides @Singleton
    fun api(retrofit: Retrofit): GoogleAuthApi = retrofit.create(GoogleAuthApi::class.java)
    @Provides @Singleton
    fun sessionStore(@ApplicationContext context: Context): AuthSessionStore = KeystoreAuthSessionStore(context)
    @Provides @Singleton
    fun repository(api: GoogleAuthApi, json: Json, sessions: AuthSessionStore, config: NetworkConfig): GoogleAuthRepository =
        GoogleAuthRepositoryImpl(api, json, sessions,
            config.baseUrl != null && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank())
}
