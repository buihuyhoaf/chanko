package com.hoabh.chanko.di

import android.content.Context
import com.hoabh.chanko.BuildConfig
import com.hoabh.chanko.data.auth.*
import com.hoabh.chanko.dev.DevFakeAuthenticationRepository
import com.hoabh.chanko.dev.DevFakeGoogleAuthRepository
import com.hoabh.chanko.domain.auth.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides @Singleton
    fun legacyRepository(): GoogleAuthRepository = if (BuildConfig.IS_DEV_MOCK) {
        DevFakeGoogleAuthRepository()
    } else {
        object : GoogleAuthRepository {
            override suspend fun createChallenge() = AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
            override suspend fun signIn(challenge: GoogleAuthChallenge, idToken: String) =
                AuthResult.Failure(AuthFailure.NOT_CONFIGURED)
        }
    }

    @Provides @Singleton fun backend(): AuthBackend = UnconfiguredAuthBackend()
    @Provides @Singleton fun store(@ApplicationContext context: Context): SecureSessionStore = KeystoreSessionStore(context)
    @Provides @Singleton fun clock(): AuthClock = AuthClock { System.currentTimeMillis() }

    @Provides @Singleton
    fun repository(backend: AuthBackend, store: SecureSessionStore, clock: AuthClock): AuthenticationRepository =
        if (BuildConfig.IS_DEV_MOCK) {
            DevFakeAuthenticationRepository()
        } else {
            AuthenticationRepositoryImpl(backend, store, clock)
        }

    @Provides fun signIn(repository: AuthenticationRepository) = SignInWithGoogleUseCase(repository)
    @Provides fun restore(repository: AuthenticationRepository) = RestoreAuthSessionUseCase(repository)
    @Provides fun refresh(repository: AuthenticationRepository) = RefreshAuthSessionUseCase(repository)
    @Provides fun logout(repository: AuthenticationRepository) = LogoutUseCase(repository)
}
