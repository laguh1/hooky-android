package com.hooky.app.data.premium

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

// Free Pro trial for installs that don't qualify as early adopters: this many days of
// Pro counted from the device's own first launch, no payment method needed. "0" or
// blank turns the trial off. Kept separate from RC_KEY_EARLY_ADOPTER_DAYS on purpose —
// that one is read live, so reusing it would shorten early adopters' free year.
private const val RC_KEY_PRO_TRIAL_DAYS = "pro_trial_days"

// expires = null means lifetime access. expires = "yyyy-MM-dd" grants access through
// the end of that day. An unparseable date fails open (treated as lifetime) so a typo
// in Firebase never accidentally locks out a genuine tester.
@Serializable
private data class PremiumEntry(val id: String, val expires: String? = null)

private fun PremiumEntry.isActiveToday(): Boolean {
    val expiryDate = expires?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return true
    return !LocalDate.now().isAfter(expiryDate)
}

/** Why (or whether) this device currently has Pro — drives the wording on the Pro screen. */
sealed interface ProStatus {
    data object None : ProStatus
    data object Purchased : ProStatus
    data object Gifted : ProStatus
    /** lastDay = null means the early-adopter access has no end date. */
    data class EarlyAdopter(val lastDay: LocalDate?) : ProStatus
    data class Trial(val lastDay: LocalDate) : ProStatus
}

@Singleton
class PremiumManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig,
    private val firebaseInstallations: FirebaseInstallations,
    private val billing: ProBillingManager
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isPremium = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM, false))
    val isPremiumFlow: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _proStatus = MutableStateFlow<ProStatus>(ProStatus.None)
    val proStatusFlow: StateFlow<ProStatus> = _proStatus.asStateFlow()

    private val _installationId = MutableStateFlow(prefs.getString(KEY_INSTALLATION_ID, "") ?: "")
    val installationIdFlow: StateFlow<String> = _installationId.asStateFlow()

    val isPremium: Boolean get() = _isPremium.value

    // Pro granted without a purchase (gift, early adopter, trial). Unknown until initialize().
    private var freeStatus: ProStatus = ProStatus.None
    private var initialized = false
    private var debugOverride: Boolean? = null

    init {
        scope.launch { billing.hasProFlow.collect { publish() } }
    }

    fun forceSetPremium(premium: Boolean) {
        debugOverride = premium
        publish()
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
    private fun earlyAdopterStatus(): ProStatus.EarlyAdopter? {
        val installDate = firstLaunchDate()
        val alreadyQualified = prefs.getBoolean(KEY_QUALIFIED_EARLY_ADOPTER, false)
        if (!alreadyQualified) {
            val cutoff = remoteConfig.getString(RC_KEY_EARLY_ADOPTER_CUTOFF)
                .let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
            if (installDate.isAfter(cutoff)) return null
            prefs.edit().putBoolean(KEY_QUALIFIED_EARLY_ADOPTER, true).apply()
        }

        val days = remoteConfig.getString(RC_KEY_EARLY_ADOPTER_DAYS).toLongOrNull() ?: 0L
        if (days <= 0L) return ProStatus.EarlyAdopter(lastDay = null) // lifetime
        val lastDay = installDate.plusDays(days)
        return if (LocalDate.now().isAfter(lastDay)) null else ProStatus.EarlyAdopter(lastDay)
    }

    private fun trialStatus(): ProStatus.Trial? {
        val days = remoteConfig.getString(RC_KEY_PRO_TRIAL_DAYS).toLongOrNull() ?: 0L
        if (days <= 0L) return null
        val lastDay = firstLaunchDate().plusDays(days - 1)
        return if (LocalDate.now().isAfter(lastDay)) null else ProStatus.Trial(lastDay)
    }

    private fun isGifted(id: String): Boolean {
        val raw = remoteConfig.getString(RC_KEY_PREMIUM_IDS)
        if (id.isBlank() || raw.isBlank() || raw == "[]") return false
        val entries = try {
            Json { ignoreUnknownKeys = true }.decodeFromString(ListSerializer(PremiumEntry.serializer()), raw)
        } catch (_: Exception) {
            emptyList()
        }
        return entries.any { it.id == id && it.isActiveToday() }
    }

    suspend fun initialize() {
        // Each step tolerates being offline: the ID falls back to the cached one and Remote
        // Config to the last activated values (or the defaults in PremiumModule), so the
        // trial and a previously earned early-adopter year still work without a connection.
        attempt { firebaseInstallations.id.await() }?.let { id ->
            _installationId.value = id
            prefs.edit().putString(KEY_INSTALLATION_ID, id).apply()
        }
        attempt { remoteConfig.ensureInitialized().await() }
        attempt { remoteConfig.fetchAndActivate().await() }

        freeStatus = when {
            isGifted(_installationId.value) -> ProStatus.Gifted
            else -> earlyAdopterStatus() ?: trialStatus() ?: ProStatus.None
        }
        initialized = true
        publish()

        refreshPurchases()
    }

    /** Re-checks Google Play for a Pro purchase (new, expired or made on another device). */
    suspend fun refreshPurchases() {
        attempt { billing.refresh() }
    }

    private fun publish() {
        val status = if (billing.hasProFlow.value) ProStatus.Purchased else freeStatus
        val premium = debugOverride ?: (status != ProStatus.None)
        // Before initialize() has run we only know about purchases, so never downgrade the
        // cached value yet — an early adopter must not flash to "locked" on every start.
        if (!initialized && !premium) return
        _proStatus.value = status
        _isPremium.value = premium
        prefs.edit().putBoolean(KEY_IS_PREMIUM, premium).apply()
    }

    private suspend fun <T> attempt(block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }
}
