package com.hooky.app.data.premium

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "hooky_premium"
private const val KEY_IS_PREMIUM = "is_premium"
private const val KEY_INSTALLATION_ID = "installation_id"
private const val RC_KEY_PREMIUM_IDS = "premium_ids"

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig,
    private val firebaseInstallations: FirebaseInstallations
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPremium = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM, false))
    val isPremiumFlow: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _installationId = MutableStateFlow(prefs.getString(KEY_INSTALLATION_ID, "") ?: "")
    val installationIdFlow: StateFlow<String> = _installationId.asStateFlow()

    val isPremium: Boolean get() = _isPremium.value

    suspend fun initialize() {
        try {
            val id = firebaseInstallations.id.await()
            _installationId.value = id
            prefs.edit().putString(KEY_INSTALLATION_ID, id).apply()

            remoteConfig.fetchAndActivate().await()

            val raw = remoteConfig.getString(RC_KEY_PREMIUM_IDS)
            val allowedIds: List<String> = if (raw.isBlank() || raw == "[]") emptyList() else
                try {
                    Json.decodeFromString(ListSerializer(serializer()), raw)
                } catch (_: Exception) {
                    emptyList()
                }

            val premium = id in allowedIds
            _isPremium.value = premium
            prefs.edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
        } catch (_: Exception) {
            // Network unavailable or Firebase not configured — keep cached value
        }
    }
}
