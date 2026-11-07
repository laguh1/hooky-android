package com.hooky.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPolicyTest {

    private val day = 24L * 60 * 60 * 1000
    private val installed = 1_000_000_000_000L

    @Test
    fun asksAfterThreeDaysWithTwoPieces() {
        assertTrue(ReviewPolicy.shouldAsk(installed + 3 * day, installed, null, 2))
    }

    @Test
    fun doesNotAskTooSoonAfterInstall() {
        assertFalse(ReviewPolicy.shouldAsk(installed + 2 * day, installed, null, 5))
    }

    @Test
    fun doesNotAskWithOnlyOnePiece() {
        assertFalse(ReviewPolicy.shouldAsk(installed + 30 * day, installed, null, 1))
    }

    @Test
    fun waitsNinetyDaysBetweenAsks() {
        val asked = installed + 10 * day
        assertFalse(ReviewPolicy.shouldAsk(asked + 89 * day, installed, asked, 4))
        assertTrue(ReviewPolicy.shouldAsk(asked + 90 * day, installed, asked, 4))
    }
}
