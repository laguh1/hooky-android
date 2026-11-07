package com.hooky.app.ui.help

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

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

// Adds the Tester ID so the form fills it in (lets the maker gift Pro to people who send ideas)
@Composable
fun feedbackUrl(): String {
    val id = LocalContext.current.getSharedPreferences("hooky_premium", Context.MODE_PRIVATE)
        .getString("installation_id", null)
    val idParam = id?.takeIf { it.isNotBlank() }?.let { "&id=" + Uri.encode(it) } ?: ""
    return "$SITE/feedback.html?lang=${siteLang()}&src=app$idParam"
}
