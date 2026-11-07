package com.hooky.app.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.play.core.review.ReviewManagerFactory
import com.hooky.app.util.AppPrefs
import com.hooky.app.util.ReviewPolicy

/**
 * Shows Google Play's in-app review prompt if [ReviewPolicy] allows it.
 * Call right after a piece is marked Finished. Fails silently: a missing
 * prompt must never get in the user's way.
 */
fun maybeAskForReview(context: Context, pieceCount: Int) {
    val activity = context.findActivity() ?: return
    val prefs = AppPrefs.get(activity)
    val now = System.currentTimeMillis()
    val installedAt = try {
        activity.packageManager.getPackageInfo(activity.packageName, 0).firstInstallTime
    } catch (_: Exception) {
        return
    }
    val lastAsked = prefs.getLong(AppPrefs.REVIEW_LAST_ASKED_AT, 0L).takeIf { it > 0L }

    if (!ReviewPolicy.shouldAsk(now, installedAt, lastAsked, pieceCount)) return

    prefs.edit().putLong(AppPrefs.REVIEW_LAST_ASKED_AT, now).apply()
    val manager = ReviewManagerFactory.create(activity)
    manager.requestReviewFlow().addOnCompleteListener { request ->
        if (request.isSuccessful && !activity.isFinishing) {
            manager.launchReviewFlow(activity, request.result)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
