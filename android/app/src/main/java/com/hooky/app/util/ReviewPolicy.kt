package com.hooky.app.util

/**
 * When to show Google Play's in-app review prompt.
 *
 * Asked only at a happy moment (the caller checks that a piece was just marked
 * Finished) and only for people who have used the app for a while. Play also
 * applies its own quota, so the prompt may still not appear.
 */
object ReviewPolicy {
    const val MIN_DAYS_SINCE_INSTALL = 3
    const val MIN_PIECES = 2
    const val MIN_DAYS_BETWEEN_ASKS = 90

    private const val DAY_MS = 24L * 60 * 60 * 1000

    fun shouldAsk(
        nowMs: Long,
        installedAtMs: Long,
        lastAskedAtMs: Long?,
        pieceCount: Int
    ): Boolean {
        if (pieceCount < MIN_PIECES) return false
        if (nowMs - installedAtMs < MIN_DAYS_SINCE_INSTALL * DAY_MS) return false
        if (lastAskedAtMs != null && nowMs - lastAskedAtMs < MIN_DAYS_BETWEEN_ASKS * DAY_MS) return false
        return true
    }
}
