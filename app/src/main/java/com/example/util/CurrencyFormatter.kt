package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale.FRENCH).apply {
        groupingSeparator = ' '
    }
    private val decimalFormat = DecimalFormat("#,###", symbols)

    fun formatAriary(amount: Long, showSign: Boolean = false): String {
        val formatted = decimalFormat.format(abs(amount))
        return when {
            amount > 0 && showSign -> "+$formatted Ar"
            amount < 0 -> "-$formatted Ar"
            else -> "$formatted Ar"
        }
    }

    fun formatCompactAriary(amount: Long): String {
        return when {
            abs(amount) >= 1_000_000 -> {
                val millions = amount.toDouble() / 1_000_000.0
                if (millions % 1.0 == 0.0) "${millions.toLong()}M Ar" else String.format(Locale.FRENCH, "%.1fM Ar", millions)
            }
            abs(amount) >= 1_000 -> "${amount / 1000}k Ar"
            else -> "$amount Ar"
        }
    }
}
