package com.example.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.repository.AppRepository
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsDataStore = SettingsDataStore(application)
    val repository = AppRepository(application, db, settingsDataStore)

    private val selectedDateFlow = MutableStateFlow(LocalDate.now())
    private val activeFilterFlow = MutableStateFlow(HabitFilter.ALL)
    private val heatmapMonthFlow = MutableStateFlow(YearMonth.now())

    // In-memory interactive states for rich card widgets
    private val waterCountFlow = MutableStateFlow(6)
    private val readingMinutesFlow = MutableStateFlow(12)
    private val isReadingTimerRunningFlow = MutableStateFlow(false)
    private val sportDoneTimeFlow = MutableStateFlow("Fait à 08:30")
    private val swipedHabitIdsFlow = MutableStateFlow<Set<Long>>(emptySet())

    // Dialog & Sheet states
    private val isStudyDialogOpenFlow = MutableStateFlow(false)
    private val isAddHabitSheetOpenFlow = MutableStateFlow(false)
    private val habitToEditFlow = MutableStateFlow<HabitUiItem?>(null)
    private val habitToDeleteFlow = MutableStateFlow<HabitUiItem?>(null)
    private val tipIndexFlow = MutableStateFlow(0)

    private var timerJob: Job? = null

    val uiState: StateFlow<HabitsUiState> = combine(
        combine(
            repository.habits,
            repository.habitLogs,
            repository.healthEntries
        ) { habits, logs, health ->
            Triple(habits, logs, health)
        },
        combine(
            selectedDateFlow,
            activeFilterFlow,
            heatmapMonthFlow
        ) { date, filter, hmMonth ->
            Triple(date, filter, hmMonth)
        },
        combine(
            combine(waterCountFlow, readingMinutesFlow, isReadingTimerRunningFlow) { w, r, run -> Triple(w, r, run) },
            combine(swipedHabitIdsFlow, repository.totalXp, repository.recordStreak) { swiped, xp, record -> Triple(swiped, xp, record) }
        ) { (w, r, run), (swiped, xp, record) ->
            StateCardStates(w, r, run, swiped, xp, record)
        },
        combine(
            isStudyDialogOpenFlow,
            isAddHabitSheetOpenFlow,
            habitToEditFlow,
            habitToDeleteFlow,
            tipIndexFlow
        ) { studyOpen, addOpen, editItem, deleteItem, tipIdx ->
            StateDialogs(studyOpen, addOpen, editItem, deleteItem, tipIdx)
        }
    ) { (habits, logs, health), (selectedDate, filter, heatmapMonth), cardStates, dialogs ->
        buildUiState(
            habits = habits,
            logs = logs,
            healthWater = health.firstOrNull { it.date == DateUtils.getTodayDateString() }?.waterGlasses ?: cardStates.wCount,
            selectedDate = selectedDate,
            filter = filter,
            heatmapMonth = heatmapMonth,
            cardStates = cardStates,
            dialogs = dialogs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    private data class StateCardStates(
        val wCount: Int,
        val rMinutes: Int,
        val rRunning: Boolean,
        val swiped: Set<Long>,
        val totalXp: Long,
        val recordStreak: Int
    )

    private data class StateDialogs(
        val studyOpen: Boolean,
        val addOpen: Boolean,
        val editItem: HabitUiItem?,
        val deleteItem: HabitUiItem?,
        val tipIdx: Int
    )

    private fun buildUiState(
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        healthWater: Int,
        selectedDate: LocalDate,
        filter: HabitFilter,
        heatmapMonth: YearMonth,
        cardStates: StateCardStates,
        dialogs: StateDialogs
    ): HabitsUiState {
        val today = LocalDate.now()
        val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Week strip items (7 days)
        val dayLetters = listOf("L", "M", "M", "J", "V", "S", "D")
        val weekDays = (0..6).map { i ->
            val d = monday.plusDays(i.toLong())
            val dateStr = d.toString()
            val isSelected = d == selectedDate
            val isToday = d == today
            val isFuture = d.isAfter(today)
            val doneCount = logs.count { it.date == dateStr }
            val isCompleted = !isFuture && doneCount > 0
            WeekDayItem(
                dayLetter = dayLetters[i],
                dayNumber = d.dayOfMonth,
                dateStr = dateStr,
                isSelected = isSelected,
                isToday = isToday,
                isCompleted = isCompleted,
                isFuture = isFuture
            )
        }

        // Format selected date label
        val frenchLocale = Locale.FRENCH
        val dayOfWeekName = selectedDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, frenchLocale)
            .replaceFirstChar { it.uppercase(frenchLocale) }
        val monthName = selectedDate.month.getDisplayName(java.time.format.TextStyle.FULL, frenchLocale)
        val weekNumber = selectedDate.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        val selectedDateLabel = "$dayOfWeekName ${selectedDate.dayOfMonth} $monthName · Semaine $weekNumber"

        // Dynamic XP & Level calculation from stored totalXp
        val totalXp = cardStates.totalXp
        val level = ((totalXp / 500) + 1).toInt()
        val xpCurrent = (totalXp % 500).toInt()
        val xpMax = 500

        // Dynamic overall streak and record streak calculation from habit_logs
        val overallStreak = computeOverallStreak(logs, today)
        val effectiveRecordStreak = maxOf(cardStates.recordStreak, overallStreak)
        if (overallStreak > cardStates.recordStreak) {
            viewModelScope.launch {
                repository.updateRecordStreak(overallStreak)
            }
        }

        // Build Habit Items dynamically from Room database (repository.habits)
        val items = habits.map { h ->
            val isDone = logs.any { it.habitId == h.id && it.date == selectedDate.toString() }
            val habitStreak = computeHabitStreak(h.id, logs, today)
            val history7Days = compute7DayHistory(h.id, logs, monday, today, isDone)
            val isSwiped = cardStates.swiped.contains(h.id)

            val lowerName = h.name.lowercase(Locale.ROOT)
            val em = when {
                h.iconName.isNotBlank() && h.iconName.length <= 4 -> h.iconName
                h.iconName == "water" || lowerName.contains("eau") -> "💧"
                h.iconName == "sport" || lowerName.contains("sport") -> "🏃"
                h.iconName == "book" || lowerName.contains("lecture") -> "📖"
                h.iconName == "sleep" || lowerName.contains("sommeil") -> "🌙"
                h.iconName == "german" || lowerName.contains("allemand") -> "🇩🇪"
                h.iconName == "meditation" || lowerName.contains("méditer") -> "🧘"
                h.iconName == "food" -> "🥗"
                h.iconName == "code" -> "💻"
                else -> "✨"
            }

            val cardType = when (h.habitType) {
                "COUNTER" -> HabitCardType.COUNTER
                "DURATION" -> HabitCardType.TIMER
                "AVOID" -> HabitCardType.SWIPABLE
                "BOOLEAN" -> HabitCardType.CHECK
                else -> when {
                    h.iconName == "water" || lowerName.contains("eau") || lowerName.contains("hydratation") -> HabitCardType.COUNTER
                    h.iconName == "book" || lowerName.contains("lecture") -> HabitCardType.TIMER
                    h.iconName == "translate" || h.iconName == "german" || lowerName.contains("allemand") -> HabitCardType.STUDY
                    h.iconName == "sleep" || lowerName.contains("sommeil") || lowerName.contains("23h") -> HabitCardType.SWIPABLE
                    h.iconName == "sport" || lowerName.contains("sport") -> HabitCardType.CHECK
                    else -> HabitCardType.SWIPABLE
                }
            }

            val category = if (h.moment.isNotBlank()) h.moment else if (h.targetDaysPerWeek <= 3) "Soir" else "Matin"

            val subtitle = when {
                h.habitType == "COUNTER" -> "Objectif : ${h.targetCount} ${h.unit} / jour"
                h.habitType == "DURATION" -> "Objectif : ${h.targetCount} ${h.unit} chronométré"
                h.habitType == "AVOID" -> "Abstinence · 0 ${h.unit}"
                h.habitType == "BOOLEAN" -> "Objectif : ${h.targetDaysPerWeek} jours / semaine"
                h.iconName == "water" || lowerName.contains("eau") -> "Objectif : 8 verres (2,0 L)"
                h.iconName == "book" || lowerName.contains("lecture") -> "Objectif : 20 min par jour"
                h.iconName == "translate" || h.iconName == "german" || lowerName.contains("allemand") -> "15 nouveaux mots (Kapitel 5)"
                h.iconName == "sleep" || lowerName.contains("sommeil") -> "Préparation extinction écrans à 22h30"
                h.iconName == "sport" || lowerName.contains("sport") -> "Cardio & Renforcement"
                else -> "Objectif : ${h.targetDaysPerWeek} jours/semaine"
            }

            val currentWater = if (cardType == HabitCardType.COUNTER) {
                if (cardStates.wCount > 0) cardStates.wCount else if (isDone) h.targetCount else healthWater
            } else 0
            val isWaterDone = currentWater >= (if (h.targetCount > 0) h.targetCount else 8) || isDone
            val isLectureDone = (cardStates.rMinutes >= (if (h.targetCount > 0) h.targetCount else 20)) || isDone

            val finalDone = when (cardType) {
                HabitCardType.COUNTER -> isWaterDone
                HabitCardType.TIMER -> isLectureDone
                else -> isDone
            }

            HabitUiItem(
                id = h.id,
                name = h.name,
                subtitle = subtitle,
                iconEmoji = em,
                streak = habitStreak,
                category = category,
                type = cardType,
                isCompletedToday = finalDone,
                counterValue = currentWater,
                counterTarget = if (h.targetCount > 0) h.targetCount else 8,
                counterUnit = h.unit.ifBlank { "verres" },
                timerElapsedMinutes = if (cardStates.rMinutes > 0) cardStates.rMinutes else if (isDone) h.targetCount else 0,
                timerTargetMinutes = if (h.targetCount > 0) h.targetCount else 20,
                isTimerRunning = cardStates.rRunning,
                completedTimeTag = if (finalDone) sportDoneTimeFlow.value ?: "Terminé" else null,
                history7Days = history7Days,
                isSwipedOpen = isSwiped,
                iconName = h.iconName,
                targetDaysPerWeek = h.targetDaysPerWeek,
                colorHex = h.colorHex.ifBlank { "#06B6D4" },
                reminderTimes = h.reminderTimes,
                note = h.note
            )
        }

        // Filter calculation
        val allCount = items.size
        val doneCount = items.count { it.isCompletedToday }
        val todoCount = allCount - doneCount

        val filteredItems = when (filter) {
            HabitFilter.ALL -> items
            HabitFilter.TODO -> items.filter { !it.isCompletedToday }
            HabitFilter.DONE -> items.filter { it.isCompletedToday }
            HabitFilter.MORNING -> items.filter { it.category == "Matin" }
            HabitFilter.EVENING -> items.filter { it.category == "Soir" }
        }

        // Hero Card stats
        val dayPercent = if (allCount > 0) ((doneCount.toDouble() / allCount.toDouble()) * 100).toInt() else 0
        val remainingToPerfect = (allCount - doneCount).coerceAtLeast(0)
        val motivationMsg = when {
            allCount == 0 -> "Ajoutez vos premières habitudes pour démarrer !"
            remainingToPerfect == 0 -> "Journée parfaite accomplie ! Bravo !"
            else -> "Plus que $remainingToPerfect ${if (remainingToPerfect > 1) "habitudes" else "habitude"} pour une journée parfaite !"
        }

        // Weekly performance bar chart (7 columns L, M, M, J, V, S, D)
        val weeklyStats = computeWeeklyBars(logs, allCount, monday, today)

        // Consistency Heatmap (days 1..N for heatmapMonth)
        val (heatmapDays, perfectDays, avgRate) = computeHeatmapDays(logs, allCount, heatmapMonth, today)
        val monthDisplay = heatmapMonth.month.getDisplayName(java.time.format.TextStyle.FULL, frenchLocale)
            .replaceFirstChar { it.uppercase(frenchLocale) } + " " + heatmapMonth.year

        // 30 Days Habit Ranking
        val rankings = computeHabitRankings(logs, items, today)

        // Badges dynamically unlocked based on streaks
        val badges = computeBadges(overallStreak, effectiveRecordStreak)

        val morningHabits = items.filter { it.category == "Matin" }
        val eveningHabits = items.filter { it.category == "Soir" }
        val morningDone = morningHabits.count { it.isCompletedToday }
        val eveningDone = eveningHabits.count { it.isCompletedToday }
        val morningRate = if (morningHabits.isNotEmpty()) (morningDone * 100) / morningHabits.size else 0
        val eveningRate = if (eveningHabits.isNotEmpty()) (eveningDone * 100) / eveningHabits.size else 0

        val tips = listOf(
            if (morningRate >= eveningRate && morningHabits.isNotEmpty()) {
                "Tu réussis mieux tes rituels le matin (${morningRate}% aujourd'hui). Caler tes tâches prioritaires avant midi maximise ta régularité !"
            } else if (eveningHabits.isNotEmpty()) {
                "Tes rituels du soir sont bien suivis (${eveningRate}%). Un rituel apaisant avant le coucher prépare un sommeil réparateur."
            } else {
                "Créer des rituels réguliers renforce votre discipline et sécurise vos séries jour après jour !"
            },
            "Boire un grand verre d'eau dès le réveil relance votre métabolisme et facilite votre concentration.",
            "Visualiser votre journée la veille ou dès le matin augmente la probabilité de réussite de plus de 40%."
        )

        return HabitsUiState(
            selectedDate = selectedDate,
            selectedDateLabel = selectedDateLabel,
            regularityPercent = avgRate.coerceAtLeast(0),
            weekDays = weekDays,
            dayPercent = dayPercent,
            completedTodayCount = doneCount,
            totalHabitsCount = allCount,
            streakDays = overallStreak,
            recordStreak = effectiveRecordStreak,
            level = level,
            xpCurrent = xpCurrent,
            xpMax = xpMax,
            motivationalMessage = motivationMsg,
            activeFilter = filter,
            allCount = allCount,
            todoCount = todoCount,
            doneCount = doneCount,
            habits = filteredItems,
            weeklyBars = weeklyStats.bars,
            weeklyAverage = weeklyStats.average,
            weeklyBestDay = weeklyStats.bestDay,
            weeklyDelta = weeklyStats.delta,
            heatmapMonth = heatmapMonth,
            heatmapMonthDisplay = monthDisplay,
            heatmapDays = heatmapDays,
            perfectDaysCount = perfectDays,
            averageSuccessRate = avgRate,
            activeStreakDays = overallStreak,
            rankings = rankings,
            badges = badges,
            insightTip = tips[dialogs.tipIdx.coerceIn(0, tips.lastIndex)],
            insightTipIndex = dialogs.tipIdx,
            activeBottomNavTab = 0,
            isStudyDialogOpen = dialogs.studyOpen,
            isAddHabitSheetOpen = dialogs.addOpen,
            habitToEdit = dialogs.editItem,
            habitToDelete = dialogs.deleteItem
        )
    }

    private data class HabitMeta(
        val type: HabitCardType,
        val category: String,
        val emoji: String,
        val subtitle: String
    )

    // ==========================================
    // Dynamic Streak & Metric Computation Helpers
    // ==========================================

    private fun computeHabitStreak(habitId: Long, logs: List<HabitLogEntity>, today: LocalDate): Int {
        val hDates = logs.filter { it.habitId == habitId }.map { it.date }.toSet()
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()
        var currDate = if (hDates.contains(todayStr)) today else if (hDates.contains(yesterdayStr)) today.minusDays(1) else null
        var streak = 0
        while (currDate != null && hDates.contains(currDate.toString())) {
            streak++
            currDate = currDate.minusDays(1)
        }
        return streak
    }

    private fun computeOverallStreak(logs: List<HabitLogEntity>, today: LocalDate): Int {
        val activeDates = logs.map { it.date }.toSet()
        val todayStr = today.toString()
        val yesterdayStr = today.minusDays(1).toString()
        var currDate = if (activeDates.contains(todayStr)) today else if (activeDates.contains(yesterdayStr)) today.minusDays(1) else null
        if (currDate == null) return 0
        var streak = 0
        while (currDate != null && activeDates.contains(currDate.toString())) {
            streak++
            currDate = currDate.minusDays(1)
        }
        return streak
    }

    private fun compute7DayHistory(
        habitId: Long,
        logs: List<HabitLogEntity>,
        monday: LocalDate,
        today: LocalDate,
        isCompletedToday: Boolean
    ): List<Int> {
        val hDates = logs.filter { it.habitId == habitId }.map { it.date }.toSet()
        return (0..6).map { i ->
            val d = monday.plusDays(i.toLong())
            when {
                d.isAfter(today) -> 0
                d == today -> if (isCompletedToday) 1 else 2 // 2 = in progress / pulsing
                hDates.contains(d.toString()) -> 1
                else -> 0
            }
        }
    }

    private data class WeeklyPerformanceStats(
        val bars: List<WeeklyBar>,
        val average: Int,
        val bestDay: String,
        val delta: String
    )

    private fun computeWeeklyBars(
        logs: List<HabitLogEntity>,
        totalHabits: Int,
        monday: LocalDate,
        today: LocalDate
    ): WeeklyPerformanceStats {
        val dayLetters = listOf("L", "M", "M", "J", "V", "S", "D")
        val frenchDayNames = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
        var totalPastPct = 0
        var pastDaysCount = 0

        val pcts = (0..6).map { i ->
            val d = monday.plusDays(i.toLong())
            val isFuture = d.isAfter(today)
            val doneCount = logs.count { it.date == d.toString() }
            if (isFuture || totalHabits == 0) 0 else ((doneCount.toDouble() / totalHabits.toDouble()) * 100).toInt().coerceIn(0, 100)
        }

        var maxPct = -1
        var bestDayIdx = -1
        (0..6).forEach { i ->
            val d = monday.plusDays(i.toLong())
            if (!d.isAfter(today)) {
                totalPastPct += pcts[i]
                pastDaysCount++
                if (pcts[i] > maxPct) {
                    maxPct = pcts[i]
                    bestDayIdx = i
                }
            }
        }

        val bars = (0..6).map { i ->
            val d = monday.plusDays(i.toLong())
            WeeklyBar(
                letter = dayLetters[i],
                percentage = pcts[i],
                isHighlight = (maxPct > 0 && i == bestDayIdx),
                isToday = d == today,
                isFuture = d.isAfter(today)
            )
        }

        val weeklyAvg = if (pastDaysCount > 0 && totalHabits > 0) totalPastPct / pastDaysCount else 0
        val bestDayStr = if (bestDayIdx >= 0 && maxPct > 0) "${frenchDayNames[bestDayIdx]} ⭐" else "—"

        val prevMonday = monday.minusWeeks(1)
        var prevTotalPct = 0
        (0..6).forEach { i ->
            val d = prevMonday.plusDays(i.toLong())
            val doneCount = logs.count { it.date == d.toString() }
            val p = if (totalHabits == 0) 0 else ((doneCount.toDouble() / totalHabits.toDouble()) * 100).toInt().coerceIn(0, 100)
            prevTotalPct += p
        }
        val prevWeeklyAvg = if (totalHabits > 0) prevTotalPct / 7 else 0
        val diff = weeklyAvg - prevWeeklyAvg
        val prevWeekNum = prevMonday.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        val deltaStr = if (diff >= 0) "+$diff% vs S$prevWeekNum" else "$diff% vs S$prevWeekNum"

        return WeeklyPerformanceStats(
            bars = bars,
            average = weeklyAvg,
            bestDay = bestDayStr,
            delta = deltaStr
        )
    }

    private fun computeHeatmapDays(
        logs: List<HabitLogEntity>,
        totalHabits: Int,
        heatmapMonth: YearMonth,
        today: LocalDate
    ): Triple<List<HeatmapDay>, Int, Int> {
        val daysInMonth = heatmapMonth.lengthOfMonth()
        val isCurrentMonth = heatmapMonth == YearMonth.from(today)
        var perfectDays = 0
        var totalPct = 0
        var pastDays = 0

        val firstDayOfMonth = heatmapMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
        val paddingDaysBefore = (firstDayOfWeek - 1)

        val paddingDays = (0 until paddingDaysBefore).map {
            HeatmapDay(
                dayNumber = 0,
                level = 0,
                isToday = false,
                isFuture = false
            )
        }

        val monthDays = (1..daysInMonth).map { dayNum ->
            val date = heatmapMonth.atDay(dayNum)
            val isToday = isCurrentMonth && dayNum == today.dayOfMonth
            val isFuture = date.isAfter(today)
            val doneCount = logs.count { it.date == date.toString() }

            val ratio = if (totalHabits > 0) doneCount.toDouble() / totalHabits.toDouble() else 0.0
            val level = when {
                isFuture || totalHabits == 0 -> 0
                ratio >= 1.0 -> 4
                ratio >= 0.70 -> 3
                ratio >= 0.40 -> 2
                ratio > 0.0 -> 1
                else -> 0
            }

            if (!isFuture) {
                pastDays++
                if (totalHabits > 0 && doneCount >= totalHabits) {
                    perfectDays++
                }
                totalPct += (ratio * 100).toInt().coerceIn(0, 100)
            }

            HeatmapDay(
                dayNumber = dayNum,
                level = level,
                isToday = isToday,
                isFuture = isFuture
            )
        }

        val allHeatmapDays = paddingDays + monthDays
        val avgRate = if (pastDays > 0 && totalHabits > 0) totalPct / pastDays else 0
        return Triple(allHeatmapDays, perfectDays, avgRate)
    }

    private fun computeHabitRankings(
        logs: List<HabitLogEntity>,
        items: List<HabitUiItem>,
        today: LocalDate
    ): List<HabitRankingItem> {
        if (items.isEmpty()) return emptyList()
        val last30Days = (0 until 30).map { today.minusDays(it.toLong()).toString() }.toSet()

        val vibrantColors = listOf(
            "#22C55E",
            "#06B6D4",
            "#14B8A6",
            "#8B5CF6",
            "#F59E0B",
            "#EC4899",
            "#3B82F6"
        )

        val ranked = items.map { item ->
            val count = logs.count { it.habitId == item.id && it.date in last30Days }
            val pct = ((count.toDouble() / 30.0) * 100).toInt().coerceIn(0, 100)
            Triple(item, pct, count)
        }.sortedByDescending { it.second }

        return ranked.mapIndexed { index, (item, pct, _) ->
            val badge = when {
                index == 0 && pct > 0 -> "🏆 N°1"
                pct < 50 -> "⚠️ À stimuler"
                else -> null
            }
            val color = vibrantColors[index % vibrantColors.size]
            HabitRankingItem(
                name = item.name,
                emoji = item.iconEmoji,
                badge = badge,
                percentage = pct,
                barColorHex = color
            )
        }
    }

    private fun computeBadges(currentStreak: Int, recordStreak: Int): List<HabitBadgeItem> {
        val maxStreak = maxOf(currentStreak, recordStreak)
        return listOf(
            HabitBadgeItem(
                title = "7 jours",
                iconEmoji = "🏅",
                statusText = if (maxStreak >= 7) "Débloqué" else "$currentStreak/7 j",
                isUnlocked = maxStreak >= 7,
                progress = (currentStreak / 7f).coerceIn(0f, 1f),
                accentColorHex = "#F59E0B"
            ),
            HabitBadgeItem(
                title = "14 jours",
                iconEmoji = "⚡",
                statusText = if (maxStreak >= 14) "Débloqué" else "$currentStreak/14 j",
                isUnlocked = maxStreak >= 14,
                progress = (currentStreak / 14f).coerceIn(0f, 1f),
                accentColorHex = "#7C3AED"
            ),
            HabitBadgeItem(
                title = "30 jours",
                iconEmoji = "🔒",
                statusText = if (maxStreak >= 30) "Débloqué" else "$currentStreak/30 j",
                isUnlocked = maxStreak >= 30,
                progress = (currentStreak / 30f).coerceIn(0f, 1f),
                accentColorHex = "#06B6D4"
            ),
            HabitBadgeItem(
                title = "100 jours",
                iconEmoji = "👑",
                statusText = if (maxStreak >= 100) "Légende" else "Légende",
                isUnlocked = maxStreak >= 100,
                progress = (currentStreak / 100f).coerceIn(0f, 1f),
                accentColorHex = "#64748B"
            )
        )
    }

    // ==========================================
    // Interactive User Actions
    // ==========================================

    fun onSelectDate(dateStr: String) {
        try {
            selectedDateFlow.value = LocalDate.parse(dateStr)
        } catch (_: Exception) {}
    }

    fun onSelectFilter(filter: HabitFilter) {
        activeFilterFlow.value = filter
    }

    fun incrementWater() {
        val current = waterCountFlow.value
        val next = (current + 1).coerceAtMost(16)
        waterCountFlow.value = next
        viewModelScope.launch {
            repository.setWaterGlasses(next)
            repository.addXp(10L)
            if (next >= 8) {
                val dateStr = selectedDateFlow.value.toString()
                val allHabits = repository.getAllHabitsSnapshot()
                val waterHabit = allHabits.firstOrNull {
                    it.iconName == "water" || it.name.contains("Eau", ignoreCase = true) || it.name.contains("Hydratation", ignoreCase = true)
                }
                if (waterHabit != null) {
                    val logs = repository.getAllHabitLogsSnapshot()
                    if (!logs.any { it.habitId == waterHabit.id && it.date == dateStr }) {
                        repository.toggleHabitForDate(waterHabit.id, dateStr)
                    }
                }
            }
        }
    }

    fun decrementWater() {
        val current = waterCountFlow.value
        val next = (current - 1).coerceAtLeast(0)
        waterCountFlow.value = next
        viewModelScope.launch {
            repository.setWaterGlasses(next)
            repository.addXp(-10L)
            if (next < 8) {
                val dateStr = selectedDateFlow.value.toString()
                val allHabits = repository.getAllHabitsSnapshot()
                val waterHabit = allHabits.firstOrNull {
                    it.iconName == "water" || it.name.contains("Eau", ignoreCase = true) || it.name.contains("Hydratation", ignoreCase = true)
                }
                if (waterHabit != null) {
                    val logs = repository.getAllHabitLogsSnapshot()
                    if (logs.any { it.habitId == waterHabit.id && it.date == dateStr }) {
                        repository.toggleHabitForDate(waterHabit.id, dateStr)
                    }
                }
            }
        }
    }

    fun toggleHabit(habitId: Long) {
        val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        sportDoneTimeFlow.value = "Fait à $timeNow"
        val dateStr = selectedDateFlow.value.toString()
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, dateStr)
        }
    }

    fun toggleReadingTimer() {
        val currentlyRunning = isReadingTimerRunningFlow.value
        if (currentlyRunning) {
            timerJob?.cancel()
            isReadingTimerRunningFlow.value = false
        } else {
            isReadingTimerRunningFlow.value = true
            timerJob = viewModelScope.launch {
                while (isReadingTimerRunningFlow.value && readingMinutesFlow.value < 20) {
                    delay(3000) // simulated 1 minute per 3 seconds for demonstration
                    readingMinutesFlow.value = (readingMinutesFlow.value + 1).coerceAtMost(20)
                    repository.addXp(1L)
                }
                if (readingMinutesFlow.value >= 20) {
                    isReadingTimerRunningFlow.value = false
                    repository.addXp(15L) // Bonus completion XP
                    val dateStr = selectedDateFlow.value.toString()
                    val allHabits = repository.getAllHabitsSnapshot()
                    val readingHabit = allHabits.firstOrNull {
                        it.iconName == "book" || it.name.contains("Lecture", ignoreCase = true)
                    }
                    if (readingHabit != null) {
                        val logs = repository.getAllHabitLogsSnapshot()
                        if (!logs.any { it.habitId == readingHabit.id && it.date == dateStr }) {
                            repository.toggleHabitForDate(readingHabit.id, dateStr)
                        }
                    }
                }
            }
        }
    }

    fun openStudyDialog() {
        isStudyDialogOpenFlow.value = true
    }

    fun closeStudyDialog() {
        isStudyDialogOpenFlow.value = false
    }

    fun completeGermanStudy() {
        isStudyDialogOpenFlow.value = false
        viewModelScope.launch {
            val dateStr = selectedDateFlow.value.toString()
            val allHabits = repository.getAllHabitsSnapshot()
            val germanHabit = allHabits.firstOrNull {
                it.iconName == "translate" || it.iconName == "german" || it.name.contains("Allemand", ignoreCase = true)
            }
            if (germanHabit != null) {
                val logs = repository.getAllHabitLogsSnapshot()
                if (!logs.any { it.habitId == germanHabit.id && it.date == dateStr }) {
                    repository.toggleHabitForDate(germanHabit.id, dateStr)
                }
            }
            repository.addXp(25L)
        }
    }

    fun toggleSwipe(habitId: Long) {
        val current = swipedHabitIdsFlow.value
        if (current.contains(habitId)) {
            swipedHabitIdsFlow.value = current - habitId
        } else {
            swipedHabitIdsFlow.value = current + habitId
        }
    }

    fun previousHeatmapMonth() {
        heatmapMonthFlow.value = heatmapMonthFlow.value.minusMonths(1)
    }

    fun nextHeatmapMonth() {
        heatmapMonthFlow.value = heatmapMonthFlow.value.plusMonths(1)
    }

    fun openAddHabitSheet() {
        habitToEditFlow.value = null
        isAddHabitSheetOpenFlow.value = true
    }

    fun closeAddHabitSheet() {
        isAddHabitSheetOpenFlow.value = false
    }

    fun openEditHabitSheet(habit: HabitUiItem) {
        isAddHabitSheetOpenFlow.value = false
        habitToEditFlow.value = habit
    }

    fun closeEditHabitSheet() {
        habitToEditFlow.value = null
    }

    fun confirmDeleteHabit(habit: HabitUiItem) {
        habitToDeleteFlow.value = habit
    }

    fun dismissDeleteDialog() {
        habitToDeleteFlow.value = null
    }

    fun executeDeleteHabit() {
        val habit = habitToDeleteFlow.value ?: return
        habitToDeleteFlow.value = null
        viewModelScope.launch {
            repository.deleteHabit(habit.id)
        }
    }

    fun createHabit(
        name: String,
        category: String,
        iconName: String,
        targetDays: Int,
        habitType: String = "BOOLEAN",
        targetCount: Int = 1,
        unit: String = "fois",
        colorHex: String = "#06B6D4",
        reminderTimes: String = "",
        note: String = ""
    ) {
        if (name.isBlank()) return
        val clampedDays = targetDays.coerceIn(1, 7)
        isAddHabitSheetOpenFlow.value = false
        viewModelScope.launch {
            val newId = repository.insertHabit(
                HabitEntity(
                    name = name.trim(),
                    iconName = iconName,
                    targetDaysPerWeek = clampedDays,
                    isDemo = false,
                    habitType = habitType,
                    targetCount = targetCount,
                    unit = unit,
                    moment = category,
                    colorHex = colorHex,
                    reminderTimes = reminderTimes,
                    note = note
                )
            )
            repository.addXp(20L)
            val todayStr = DateUtils.getTodayDateString()
            if (name.contains("eau", ignoreCase = true) || unit == "verres" || iconName == "💧" || iconName == "water") {
                val healthEntries = repository.getAllHealthEntriesSnapshot()
                val todayHealth = healthEntries.firstOrNull { it.date == todayStr }
                if (todayHealth != null && todayHealth.waterGlasses >= targetCount) {
                    repository.toggleHabitForDate(newId, todayStr)
                }
            }
        }
    }

    fun updateHabit(
        id: Long,
        name: String,
        category: String,
        iconName: String,
        targetDays: Int,
        habitType: String = "BOOLEAN",
        targetCount: Int = 1,
        unit: String = "fois",
        colorHex: String = "#06B6D4",
        reminderTimes: String = "",
        note: String = ""
    ) {
        if (name.isBlank()) return
        val clampedDays = targetDays.coerceIn(1, 7)
        habitToEditFlow.value = null
        viewModelScope.launch {
            val allHabits = repository.getAllHabitsSnapshot()
            val existing = allHabits.firstOrNull { it.id == id }
            if (existing != null) {
                repository.updateHabit(
                    existing.copy(
                        name = name.trim(),
                        iconName = iconName,
                        targetDaysPerWeek = clampedDays,
                        habitType = habitType,
                        targetCount = targetCount,
                        unit = unit,
                        moment = category,
                        colorHex = colorHex,
                        reminderTimes = reminderTimes,
                        note = note
                    )
                )
            }
        }
    }

    fun nextTip() {
        tipIndexFlow.value = (tipIndexFlow.value + 1) % 3
    }
}
