package com.hooky.app.ui.settings

import android.content.Context
import com.hooky.app.R

internal const val PREFS_CURRENCY_KEY = "currency"

data class CurrencyOption(val code: String, val symbol: String, val labelRes: Int, val sublabel: String)

val CURRENCIES = listOf(
    CurrencyOption("EUR", "€",  R.string.settings_currency_eur, "€ EUR"),
    CurrencyOption("USD", "$",  R.string.settings_currency_usd, "$ USD"),
    CurrencyOption("GBP", "£",  R.string.settings_currency_gbp, "£ GBP"),
    CurrencyOption("BRL", "R$", R.string.settings_currency_brl, "R$ BRL"),
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
