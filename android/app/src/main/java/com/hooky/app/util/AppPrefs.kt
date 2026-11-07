package com.hooky.app.util

import android.content.Context
import android.content.SharedPreferences

/** Keys in the app's small settings file (same file the onboarding flag already used). */
object AppPrefs {
    const val FILE = "hooky_settings"

    const val HAS_SEEN_ONBOARDING = "hasSeenOnboarding"

    /** Set when someone goes through onboarding on this version or later (a new user). */
    const val GETTING_STARTED_NEW_USER = "gettingStartedNewUser"
    const val GETTING_STARTED_DISMISSED = "gettingStartedDismissed"

    const val REVIEW_LAST_ASKED_AT = "reviewLastAskedAt"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
