package com.hooky.app.ui.help

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

// Pages on the Hooky App website. ?lang= makes the page open in the app's language
// (the site otherwise follows the browser), and src=app tags suggestions sent from
// the app so they can be told apart from ones sent from the website.
private const val SITE = "https://hookyapp.netlify.app"

@Composable
private fun siteLang(): String = when (LocalConfiguration.current.locales[0].language) {
    "pt" -> "pt"
    "es" -> "es"
    else -> "en"
}

@Composable
fun guideUrl(): String = "$SITE/guide.html?lang=${siteLang()}"

@Composable
fun feedbackUrl(): String = "$SITE/feedback.html?lang=${siteLang()}&src=app"
