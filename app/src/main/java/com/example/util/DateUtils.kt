package com.example.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    val localeFr = Locale("fr", "MG")

    fun getTodayGreetingDate(): String {
        val now = Date()
        val sdf = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
        val formatted = sdf.format(now)
        return formatted.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }
        }
    }

    fun getCurrentMonthKey(): String {
        return DateTimeFormatter.ofPattern("yyyy-MM").format(LocalDate.now())
    }

    fun formatMonthKeyDisplay(monthKey: String): String {
        return try {
            val ym = YearMonth.parse(monthKey)
            val monthName = ym.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH)
            val capMonth = monthName.replaceFirstChar { it.uppercase(Locale.FRENCH) }
            "$capMonth ${ym.year}"
        } catch (e: Exception) {
            monthKey
        }
    }

    fun shiftMonthKey(monthKey: String, delta: Long): String {
        return try {
            val ym = YearMonth.parse(monthKey).plusMonths(delta)
            DateTimeFormatter.ofPattern("yyyy-MM").format(ym)
        } catch (e: Exception) {
            getCurrentMonthKey()
        }
    }

    fun isCurrentOrFutureMonth(monthKey: String): Boolean {
        return try {
            val current = YearMonth.now()
            val target = YearMonth.parse(monthKey)
            target >= current
        } catch (e: Exception) {
            true
        }
    }

    fun formatTransactionDate(epochMillis: Long): String {
        val txDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
        val today = LocalDate.now()
        val timeSdf = SimpleDateFormat("HH:mm", Locale.FRENCH)
        val timeStr = timeSdf.format(Date(epochMillis))

        return when (txDate) {
            today -> "Aujourd'hui $timeStr"
            today.minusDays(1) -> "Hier $timeStr"
            else -> {
                val dayMonthSdf = SimpleDateFormat("d MMM", Locale.FRENCH)
                dayMonthSdf.format(Date(epochMillis))
            }
        }
    }

    fun formatShortDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
        return sdf.format(Date(epochMillis))
    }

    fun formatRelativeDays(epochMillis: Long): String {
        val target = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
        val today = LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(today, target)
        return when {
            days < 0 -> "En retard"
            days == 0L -> "Aujourd'hui"
            days == 1L -> "Demain"
            else -> "Dans $days jours"
        }
    }

    fun getTodayDateString(): String {
        return LocalDate.now().toString() // "yyyy-MM-dd"
    }

    fun getDayOfWeekIndex(): Int {
        // Monday=0, Tuesday=1, ... Sunday=6
        val dayOfWeek = LocalDate.now().dayOfWeek.value // 1 (Mon) to 7 (Sun)
        return dayOfWeek - 1
    }
}
