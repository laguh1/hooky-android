package com.hooky.app.ui.settings

import android.content.Context

internal const val PREFS_CURRENCY_KEY = "currency"

data class CurrencyOption(val code: String, val symbol: String, val label: String, val sublabel: String)

val CURRENCIES = listOf(
    CurrencyOption("EUR", "€",  "Euro",            "€ EUR"),
    CurrencyOption("USD", "$",  "US Dollar",        "$ USD"),
    CurrencyOption("GBP", "£",  "British Pound",    "£ GBP"),
    CurrencyOption("BRL", "R$", "Brazilian Real",   "R$ BRL"),
)

fun getCurrencySymbol(context: Context): String {
    val code = context.getSharedPreferences("hooky_settings", Context.MODE_PRIVATE)
        .getString(PREFS_CURRENCY_KEY, "EUR") ?: "EUR"
    return CURRENCIES.find { it.code == code }?.symbol ?: "€"
}

fun saveCurrency(context: Context, code: String) {
    context.getSharedPreferences("hooky_settings", Context.MODE_PRIVATE)
        .edit().putString(PREFS_CURRENCY_KEY, code).apply()
}

fun getCurrentCurrencyCode(context: Context): String =
    context.getSharedPreferences("hooky_settings", Context.MODE_PRIVATE)
        .getString(PREFS_CURRENCY_KEY, "EUR") ?: "EUR"

fun Float.formatCurrency(symbol: String): String = "$symbol%.2f".format(this)
