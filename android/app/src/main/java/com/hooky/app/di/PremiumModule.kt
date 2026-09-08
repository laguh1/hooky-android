package com.hooky.app.di

import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig =
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600 // 1 hour
                }
            )
            setDefaultsAsync(
                mapOf(
                    "premium_ids" to "[]",
                    "early_adopter_cutoff" to "",
                    "early_adopter_premium_days" to "0"
                )
            )
        }

    @Provides
    @Singleton
    fun provideFirebaseInstallations(): FirebaseInstallations =
        FirebaseInstallations.getInstance()
}
