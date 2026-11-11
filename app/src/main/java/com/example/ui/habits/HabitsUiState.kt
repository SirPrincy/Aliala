package com.example.ui.habits

import java.time.LocalDate
import java.time.YearMonth

enum class HabitCardType {
    COUNTER,
    CHECK,
    TIMER,
    STUDY,
    SWIPABLE,
    STANDARD
}

enum class HabitFilter {
    ALL,
    TODO,
    DONE,
    MORNING,
    EVENING
}

data class WeekDayItem(
    val dayLetter: String,
    val dayNumber: Int,
    val dateStr: String,
    val isSelected: Boolean,
    val isToday: Boolean,
    val isCompleted: Boolean,
    val isFuture: Boolean
)

data class HabitUiItem(
    val id: Long,
    val name: String,
    val subtitle: String,
    val iconEmoji: String,
    val streak: Int,
    val category: String, // "Matin", "Après-midi", "Soir"
    val type: HabitCardType,
    val isCompletedToday: Boolean,
    val counterValue: Int = 0,
    val counterTarget: Int = 8,
    val counterUnit: String = "verres (2,0 L)",
    val timerElapsedMinutes: Int = 12,
    val timerTargetMinutes: Int = 20,
    val isTimerRunning: Boolean = false,
    val completedTimeTag: String? = null,
    val history7Days: List<Int> = listOf(1, 1, 1, 2, 0, 0, 0), // 0: gray, 1: green, 2: active
    val isSwipedOpen: Boolean = false,
    val iconName: String = "water",
    val targetDaysPerWeek: Int = 7,
    val colorHex: String = "#06B6D4",
    val reminderTimes: String = "",
    val note: String = ""
)

data class WeeklyBar(
    val letter: String,
    val percentage: Int,
    val isHighlight: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class HeatmapDay(
    val dayNumber: Int,
    val level: Int, // 0 = empty/future, 1 = light, 2 = medium, 3 = high, 4 = max
    val isToday: Boolean,
    val isFuture: Boolean
)

data class HabitRankingItem(
    val name: String,
    val emoji: String,
    val badge: String? = null,
    val percentage: Int,
    val barColorHex: String
)

data class HabitBadgeItem(
    val title: String,
    val iconEmoji: String,
    val statusText: String,
    val isUnlocked: Boolean,
    val progress: Float = 1.0f,
    val accentColorHex: String = "#F59E0B"
)

data class HabitsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateLabel: String = "Jeudi 23 octobre · Semaine 43",
    val regularityPercent: Int = 86,
    val weekDays: List<WeekDayItem> = emptyList(),
    val dayPercent: Int = 60,
    val completedTodayCount: Int = 3,
    val totalHabitsCount: Int = 5,
    val streakDays: Int = 12,
    val recordStreak: Int = 21,
    val level: Int = 4,
    val xpCurrent: Int = 340,
    val xpMax: Int = 500,
    val motivationalMessage: String = "Plus que 2 habitudes pour une journée parfaite !",
    val activeFilter: HabitFilter = HabitFilter.ALL,
    val allCount: Int = 5,
    val todoCount: Int = 2,
    val doneCount: Int = 3,
    val habits: List<HabitUiItem> = emptyList(),
    val weeklyBars: List<WeeklyBar> = emptyList(),
    val weeklyAverage: Int = 72,
    val weeklyBestDay: String = "Mercredi ⭐",
    val weeklyDelta: String = "+14% vs S42",
    val heatmapMonth: YearMonth = YearMonth.now(),
    val heatmapMonthDisplay: String = "Octobre 2024",
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val perfectDaysCount: Int = 9,
    val averageSuccessRate: Int = 78,
    val activeStreakDays: Int = 12,
    val rankings: List<HabitRankingItem> = emptyList(),
    val badges: List<HabitBadgeItem> = emptyList(),
    val insightTip: String = "Tu réussis +30% mieux tes habitudes le matin. Essaye de caler la séance de lecture avant midi pour sécuriser ta série !",
    val insightTipIndex: Int = 0,
    val activeBottomNavTab: Int = 0,
    val isStudyDialogOpen: Boolean = false,
    val isAddHabitSheetOpen: Boolean = false,
    val habitToEdit: HabitUiItem? = null,
    val habitToDelete: HabitUiItem? = null
)
