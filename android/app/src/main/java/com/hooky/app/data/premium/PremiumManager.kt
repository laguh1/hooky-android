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
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "hooky_premium"
private const val KEY_IS_PREMIUM = "is_premium"
private const val KEY_INSTALLATION_ID = "installation_id"
private const val KEY_FIRST_LAUNCH_EPOCH_DAY = "first_launch_epoch_day"
private const val KEY_QUALIFIED_EARLY_ADOPTER = "qualified_early_adopter"
private const val RC_KEY_PREMIUM_IDS = "premium_ids"

// Automatic early-adopter reward — no per-person ID collection needed. Anyone whose
// first launch falls on or before RC_KEY_EARLY_ADOPTER_CUTOFF qualifies. Leave the
// cutoff blank in Firebase to disable this entirely.
// RC_KEY_EARLY_ADOPTER_DAYS: how many days of premium they get from their OWN install
// date (e.g. 365 for "1 year"). Blank or "0" means lifetime for anyone who qualifies.
private const val RC_KEY_EARLY_ADOPTER_CUTOFF = "early_adopter_cutoff"
private const val RC_KEY_EARLY_ADOPTER_DAYS = "early_adopter_premium_days"

// expires = null means lifetime access. expires = "yyyy-MM-dd" grants access through
// the end of that day. An unparseable date fails open (treated as lifetime) so a typo
// in Firebase never accidentally locks out a genuine tester.
@Serializable
private data class PremiumEntry(val id: String, val expires: String? = null)

private fun PremiumEntry.isActiveToday(): Boolean {
    val expiryDate = expires?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return true
    return !LocalDate.now().isAfter(expiryDate)
}

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

    fun forceSetPremium(premium: Boolean) {
        _isPremium.value = premium
        prefs.edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
    }

    // Recorded once, on the very first call, and never overwritten afterwards.
    private fun firstLaunchDate(): LocalDate {
        val stored = prefs.getLong(KEY_FIRST_LAUNCH_EPOCH_DAY, -1L)
        if (stored != -1L) return LocalDate.ofEpochDay(stored)
        val today = LocalDate.now()
        prefs.edit().putLong(KEY_FIRST_LAUNCH_EPOCH_DAY, today.toEpochDay()).apply()
        return today
    }

    // Qualification is a one-time gate: once a device qualifies, that fact is cached
    // locally so closing the promo later (blanking or moving the cutoff) can never
    // retroactively strip access already earned — only the duration check below still
    // runs live, so shortening/extending premium_days still works as expected.
    private fun isEarlyAdopter(): Boolean {
        val installDate = firstLaunchDate()
        val alreadyQualified = prefs.getBoolean(KEY_QUALIFIED_EARLY_ADOPTER, false)
        if (!alreadyQualified) {
            val cutoff = remoteConfig.getString(RC_KEY_EARLY_ADOPTER_CUTOFF)
                .let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
            if (installDate.isAfter(cutoff)) return false
            prefs.edit().putBoolean(KEY_QUALIFIED_EARLY_ADOPTER, true).apply()
        }

        val days = remoteConfig.getString(RC_KEY_EARLY_ADOPTER_DAYS).toLongOrNull() ?: 0L
        if (days <= 0L) return true // lifetime
        return !LocalDate.now().isAfter(installDate.plusDays(days))
    }

    suspend fun initialize() {
        try {
            val id = firebaseInstallations.id.await()
            _installationId.value = id
            prefs.edit().putString(KEY_INSTALLATION_ID, id).apply()

            remoteConfig.fetchAndActivate().await()

            val raw = remoteConfig.getString(RC_KEY_PREMIUM_IDS)
            val entries: List<PremiumEntry> = if (raw.isBlank() || raw == "[]") emptyList() else
                try {
                    Json { ignoreUnknownKeys = true }.decodeFromString(ListSerializer(PremiumEntry.serializer()), raw)
                } catch (_: Exception) {
                    emptyList()
                }

            val premium = entries.any { it.id == id && it.isActiveToday() } || isEarlyAdopter()
            _isPremium.value = premium
            prefs.edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
        } catch (_: Exception) {
            // Network unavailable or Firebase not configured — keep cached value
        }
    }
}
