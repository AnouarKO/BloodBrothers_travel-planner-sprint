package com.example.bbtraveling.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val EURO_SYMBOL = "\u20AC"

fun formatEuro(amount: Double): String {
    val locale = when (Locale.getDefault().language) {
        "es", "ca" -> Locale.forLanguageTag("es-ES")
        else -> Locale.US
    }
    val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(locale))
    return "${formatter.format(amount)} $EURO_SYMBOL"
}
