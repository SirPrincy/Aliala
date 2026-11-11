package com.example.ui.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entity.HealthEntryEntity
import com.example.data.repository.AppRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val settingsDataStore = SettingsDataStore(application)
    private val repository = AppRepository(application, db, settingsDataStore)

    private val selectedDateFlow = MutableStateFlow(DateUtils.getTodayDateString())
    private val isSpeedDialOpenFlow = MutableStateFlow(false)
    private val showWeightDialogFlow = MutableStateFlow(false)
    private val showSleepDialogFlow = MutableStateFlow(false)
    private val showWorkoutDialogFlow = MutableStateFlow(false)
    private val showMeasurementTypeFlow = MutableStateFlow<String?>(null)
    private val showAddReminderDialogFlow = MutableStateFlow(false)
    private val showHeightDialogFlow = MutableStateFlow(false)
    private val showTargetWeightDialogFlow = MutableStateFlow(false)

    private val waterReminderFlow = MutableStateFlow(true)
    private val sleepReminderFlow = MutableStateFlow(true)
    private val weighReminderFlow = MutableStateFlow(true)

    private data class ProfileState(
        val heightCm: Float?,
        val weightTargetKg: Double
    )

    private data class DialogState(
        val isSpeedDialOpen: Boolean,
        val showWeightDialog: Boolean,
        val showSleepDialog: Boolean,
        val showWorkoutDialog: Boolean,
        val showMeasurementType: String?,
        val showTargetWeightDialog: Boolean
    )

    private data class ReminderState(
        val waterReminder: Boolean,
        val sleepReminder: Boolean,
        val weighReminder: Boolean,
        val showAddReminder: Boolean,
        val showHeightDialog: Boolean
    )

    val uiState: StateFlow<HealthUiState> = combine(
        repository.healthEntries,
        combine(repository.userHeightCm, repository.weightTargetKg) { height, target ->
            ProfileState(height, target)
        },
        selectedDateFlow,
        combine(
            combine(isSpeedDialOpenFlow, showWeightDialogFlow, showSleepDialogFlow) { a, b, c -> Triple(a, b, c) },
            combine(showWorkoutDialogFlow, showMeasurementTypeFlow, showTargetWeightDialogFlow) { d, e, f -> Triple(d, e, f) }
        ) { (a, b, c), (d, e, f) ->
            DialogState(a, b, c, d, e, f)
        },
        combine(
            waterReminderFlow,
            sleepReminderFlow,
            weighReminderFlow,
            showAddReminderDialogFlow,
            showHeightDialogFlow
        ) { wRem: Boolean, sRem: Boolean, wgRem: Boolean, addRem: Boolean, hDiag: Boolean ->
            ReminderState(wRem, sRem, wgRem, addRem, hDiag)
        }
    ) { entries: List<HealthEntryEntity>, profile: ProfileState, selectedDate: String, dialogs: DialogState, reminders: ReminderState ->
        buildUiState(entries, profile.heightCm, profile.weightTargetKg, selectedDate, dialogs, reminders)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HealthUiState()
    )

    private fun formatRelativeDate(dateStr: String?, todayStr: String): String {
        if (dateStr.isNullOrBlank()) return "Aucune mesure"
        if (dateStr == todayStr) return "Aujourd'hui"
        return try {
            val date = LocalDate.parse(dateStr)
            val today = LocalDate.parse(todayStr)
            if (date == today.minusDays(1)) "Hier"
            else {
                val f = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)
                date.format(f)
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun buildUiState(
        entries: List<HealthEntryEntity>,
        userHeightCm: Float?,
        weightTargetKg: Double,
        selectedDate: String,
        dialogs: DialogState,
        reminders: ReminderState
    ): HealthUiState {
        val todayStr = DateUtils.getTodayDateString()
        val currentSelectedDate = try {
            LocalDate.parse(selectedDate)
        } catch (e: Exception) {
            LocalDate.now()
        }

        // Compute Week Days (Monday to Sunday)
        val monday = currentSelectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val frenchDays = listOf("L", "M", "M", "J", "V", "S", "D")
        val weekDayItems = (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val entry = entries.firstOrNull { it.date == dateStr }
            val isCompleted = (entry?.waterGlasses ?: 0) >= 8 || (entry?.steps ?: 0) >= 8000
            HealthDayItem(
                dayLetter = frenchDays[offset],
                dayNumber = date.dayOfMonth,
                dateString = dateStr,
                isSelected = dateStr == selectedDate,
                isCompleted = isCompleted,
                isToday = dateStr == todayStr
            )
        }

        // Header date display formatted in French
        val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
        val capitalizedDate = currentSelectedDate.format(formatter).replaceFirstChar { it.uppercase() }
        val weekNumber = currentSelectedDate.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val dateDisplay = "$capitalizedDate · Semaine $weekNumber"

        // Entry for selected date
        val selectedEntry = entries.firstOrNull { it.date == selectedDate }

        val waterGlasses = selectedEntry?.waterGlasses ?: 0
        val waterMl = waterGlasses * 250
        val sleepHours = selectedEntry?.sleepHours ?: 0.0
        val sleepBed = selectedEntry?.sleepBedTime ?: ""
        val sleepWake = selectedEntry?.sleepWakeTime ?: ""
        val steps = selectedEntry?.steps ?: 0
        val distanceKm = selectedEntry?.distanceKm ?: 0.0
        val workoutMinutes = selectedEntry?.workoutMinutes ?: 0
        val calories = selectedEntry?.caloriesKcal ?: 0
        val mood = selectedEntry?.mood ?: -1
        val moodNote = selectedEntry?.moodNote ?: ""

        // Week entries for Monday..Sunday
        val weekDates = (0..6).map { offset ->
            monday.plusDays(offset.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
        val weekEntries = weekDates.map { d -> entries.firstOrNull { it.date == d } }

        // Weekly water bars (0f to 1.2f)
        val waterWeeklyBars = weekEntries.map { e ->
            val g = e?.waterGlasses ?: 0
            if (g > 0) (g / 8f).coerceIn(0.1f, 1.2f) else 0f
        }

        // Weekly steps bars
        val stepsWeeklyBars = weekEntries.map { e ->
            val s = e?.steps ?: 0
            if (s > 0) (s / 8000f).coerceIn(0.1f, 1.2f) else 0f
        }

        // Sleep trend points (7 days)
        val sleepTrendPoints = weekEntries.map { (it?.sleepHours ?: 0.0).toFloat() }
        val sleepDaysWithData = weekEntries.filterNotNull().filter { it.sleepHours > 0.0 }
        val avgSleepDouble = if (sleepDaysWithData.isNotEmpty()) sleepDaysWithData.map { it.sleepHours }.average() else 0.0
        val sleepWeeklyAverage = if (avgSleepDouble > 0) {
            val h = avgSleepDouble.toInt()
            val m = ((avgSleepDouble % 1) * 60).toInt()
            "${h}h${String.format(Locale.getDefault(), "%02d", m)}"
        } else "—"

        val sleepRegularityPercent = if (sleepDaysWithData.size >= 2) {
            val variance = sleepDaysWithData.map { (it.sleepHours - avgSleepDouble) * (it.sleepHours - avgSleepDouble) }.average()
            val stdDev = Math.sqrt(variance)
            ((1.0 - (stdDev / 4.0).coerceIn(0.0, 0.6)) * 100).toInt()
        } else if (sleepDaysWithData.size == 1) {
            80
        } else {
            0
        }

        val sleepBadge = when {
            sleepHours >= 7.5 -> "Bien dormi 🌙"
            sleepHours in 6.0..7.49 -> "Sommeil correct 😴"
            sleepHours > 0 -> "Nuit courte 🥱"
            else -> "Pas de saisie"
        }

        // Weight calculations
        val weightEntries = entries.filter { it.weightKg != null && (it.weightKg ?: 0.0) > 0.0 }.sortedBy { it.date }
        val latestWeightEntry = weightEntries.lastOrNull()
        val currentWeightKg = selectedEntry?.weightKg ?: latestWeightEntry?.weightKg
        val weightStartKg = weightEntries.firstOrNull()?.weightKg
        val lastWeighDateText = if (latestWeightEntry != null) {
            "Dernière pesée : ${formatRelativeDate(latestWeightEntry.date, todayStr)}"
        } else {
            "Aucune pesée"
        }
        val weightHistoryPoints = weightEntries.takeLast(7).map { it.weightKg!!.toFloat() }
        val weightDeltaMonthKg = if (weightEntries.size >= 2) {
            val recent = weightEntries.last().weightKg!!
            val past = weightEntries.first().weightKg!!
            (recent - past)
        } else 0.0

        // ==========================================
        // RSI / IMC Calculation (Poids et Taille)
        // ==========================================
        val height = userHeightCm ?: 175f
        val (bmiVal, bmiCat, bmiColor, bmiDesc, idealRange, bmiProgress) = if (currentWeightKg != null && height > 50f) {
            val heightM = height / 100.0
            val computedBmi = currentWeightKg / (heightM * heightM)
            val roundedBmi = kotlin.math.round(computedBmi * 10.0) / 10.0

            val cat: String
            val col: String
            val desc: String
            when {
                computedBmi < 18.5 -> {
                    cat = "Poids insuffisant"
                    col = "#06B6D4" // Cyan
                    desc = "Pensez à maintenir une alimentation nutritive et variée."
                }
                computedBmi < 25.0 -> {
                    cat = "Corpulence normale"
                    col = "#22C55E" // Vert Succès
                    desc = "Votre rapport poids/taille est optimal. Continuez sur cette belle lancée !"
                }
                computedBmi < 30.0 -> {
                    cat = "Surpoids modéré"
                    col = "#F59E0B" // Ambre
                    desc = "Une activité physique régulière aide à équilibrer votre composition corporelle."
                }
                else -> {
                    cat = "Obésité"
                    col = "#EF4444" // Rouge
                    desc = "Un accompagnement nutritionnel et un rythme de vie actif sont recommandés."
                }
            }

            val minIdealW = kotlin.math.round(18.5 * heightM * heightM * 10.0) / 10.0
            val maxIdealW = kotlin.math.round(24.9 * heightM * heightM * 10.0) / 10.0
            val idealStr = "${String.format(Locale.FRENCH, "%.1f", minIdealW)} - ${String.format(Locale.FRENCH, "%.1f", maxIdealW)} kg"

            val progressFrac = ((computedBmi - 15.0) / (35.0 - 15.0)).coerceIn(0.0, 1.0).toFloat()

            Tuple6(roundedBmi, cat, col, desc, idealStr, progressFrac)
        } else {
            Tuple6(null, "Non calculé", "#94A3B8", "Renseignez votre taille et une pesée pour calculer votre indice RSI.", "—", 0f)
        }

        // Body Measurements
        val waistEntry = entries.filter { it.waistCm != null && (it.waistCm ?: 0.0) > 0.0 }.maxByOrNull { it.date }
        val bpEntry = entries.filter { !it.bloodPressure.isNullOrBlank() }.maxByOrNull { it.date }
        val tempEntry = entries.filter { it.temperatureC != null && (it.temperatureC ?: 0.0) > 0.0 }.maxByOrNull { it.date }

        val waistCm = selectedEntry?.waistCm ?: waistEntry?.waistCm
        val waistDate = formatRelativeDate(waistEntry?.date, todayStr)
        val bloodPressure = selectedEntry?.bloodPressure ?: bpEntry?.bloodPressure
        val bloodPressureDate = formatRelativeDate(bpEntry?.date, todayStr)
        val temperatureC = selectedEntry?.temperatureC ?: tempEntry?.temperatureC
        val temperatureDate = formatRelativeDate(tempEntry?.date, todayStr)

        // 30-Day Mood dots
        val last30DateStrings = (0..29).map { offset ->
            currentSelectedDate.minusDays(offset.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.reversed()
        val moodHistoryDots = last30DateStrings.map { d ->
            entries.firstOrNull { it.date == d }?.mood ?: -1
        }
        val loggedMoods = entries.filter { it.mood in 0..4 }.map { it.mood }
        val moodAverageEmoji = if (loggedMoods.isNotEmpty()) {
            val avgM = kotlin.math.round(loggedMoods.average()).toInt()
            when (avgM) {
                0 -> "😀"
                1 -> "🙂"
                2 -> "😐"
                3 -> "🙁"
                else -> "😞"
            }
        } else "🙂"

        // Weekly Summary Metrics
        val daysWithWater = weekEntries.filterNotNull().filter { it.waterGlasses > 0 }
        val avgWaterGlasses = if (daysWithWater.isNotEmpty()) daysWithWater.map { it.waterGlasses }.average() else 0.0
        val daysWithSteps = weekEntries.filterNotNull().filter { it.steps > 0 }
        val avgSteps = if (daysWithSteps.isNotEmpty()) daysWithSteps.map { it.steps }.average().toInt() else 0

        val weekMoods = weekEntries.filterNotNull().filter { it.mood in 0..4 }.map { it.mood }
        val (dominantMoodText, dominantMoodEmoji) = if (weekMoods.isNotEmpty()) {
            val dominant = weekMoods.groupBy { it }.maxByOrNull { it.value.size }?.key ?: 1
            when (dominant) {
                0 -> "Très en forme" to "😀"
                1 -> "Serein" to "🙂"
                2 -> "Neutre" to "😐"
                3 -> "Fatigué" to "🙁"
                else -> "Éprouvé" to "😞"
            }
        } else {
            "Non renseigné" to "😐"
        }

        // Best Day of the week
        val bestEntry = weekEntries.filterNotNull().maxByOrNull {
            (it.waterGlasses / 8.0) + (it.steps / 8000.0) + (if (it.sleepHours >= 7.0) 1.0 else 0.0)
        }
        val bestDayHighlight = if (bestEntry != null && (bestEntry.waterGlasses > 0 || bestEntry.steps > 0 || bestEntry.sleepHours > 0)) {
            val d = LocalDate.parse(bestEntry.date)
            val dFmt = DateTimeFormatter.ofPattern("EEEE d", Locale.FRENCH)
            d.format(dFmt).replaceFirstChar { it.uppercase() }
        } else {
            "—"
        }

        // Dynamic Goals Engaged
        val goalsList = listOf(
            waterGlasses >= 8,
            steps >= 8000,
            sleepHours >= 7.0,
            workoutMinutes >= 30,
            mood >= 0,
            currentWeightKg != null,
            (waistCm != null || bloodPressure != null)
        )
        val activeGoalsCount = goalsList.count { it }
        val goalsEngagedText = "$activeGoalsCount objectifs sur 7 bien engagés. ${
            when {
                activeGoalsCount >= 5 -> "Excellente cadence !"
                activeGoalsCount >= 3 -> "Bonne progression !"
                activeGoalsCount >= 1 -> "Bon début de journée !"
                else -> "À toi de jouer !"
            }
        }"

        val globalProgress = (( (waterGlasses / 8f).coerceAtMost(1f) * 0.35f +
                (steps / 8000f).coerceAtMost(1f) * 0.35f +
                (if (sleepHours >= 7.0) 1f else (sleepHours / 7f).toFloat().coerceIn(0f, 1f)) * 0.3f
                ) * 100).toInt().coerceIn(0, 100)
        val vitalityScorePercent = ((activeGoalsCount / 7f) * 100).toInt()

        val motivationTip = when {
            waterGlasses < 8 -> "Plus que ${8 - waterGlasses} verre(s) d'eau pour valider l'objectif !"
            steps < 8000 -> "Plus que ${8000 - steps} pas pour valider tes 8 000 pas quotidiens !"
            sleepHours == 0.0 -> "Pense à enregistrer ta nuit de sommeil !"
            workoutMinutes < 30 -> "Encore un peu de mouvement pour valider les 30 min d'activité !"
            else -> "Tous tes objectifs majeurs sont atteints aujourd'hui ! Bravo !"
        }

        // Dynamic Personalized Insight based on user data
        val highStepDays = entries.filter { it.steps >= 5000 && it.sleepHours > 0 }
        val lowStepDays = entries.filter { it.steps in 1..4999 && it.sleepHours > 0 }
        val insightText = when {
            highStepDays.isNotEmpty() && lowStepDays.isNotEmpty() && highStepDays.map { it.sleepHours }.average() > lowStepDays.map { it.sleepHours }.average() -> {
                val diff = highStepDays.map { it.sleepHours }.average() - lowStepDays.map { it.sleepHours }.average()
                val mins = (diff * 60).toInt()
                "Tu dors nettement mieux (+${mins} min) les jours où tu dépasses 5 000 pas. Pense à marcher après le déjeuner !"
            }
            waterGlasses >= 8 -> "Excellente hydratation ! Boire régulièrement optimise ton niveau d'énergie et ta récupération."
            steps >= 8000 -> "Excellente cadence de marche aujourd'hui ! Tu atteins tes objectifs d'activité."
            sleepHours >= 7.5 -> "Belle nuit réparatrice. Une bonne régularité de sommeil améliore ton bien-être sur toute la journée."
            entries.isEmpty() -> "Commence ton suivi santé en enregistrant tes verres d'eau, ton humeur ou ta pesée."
            else -> "Chaque petit geste compte : vise tes 8 verres d'eau et 8 000 pas aujourd'hui !"
        }

        return HealthUiState(
            selectedDate = selectedDate,
            selectedDateDisplay = dateDisplay,
            vitalityScorePercent = vitalityScorePercent,
            weekDays = weekDayItems,
            globalProgressPercent = globalProgress,
            goalsEngagedText = goalsEngagedText,
            motivationTip = motivationTip,
            waterGlasses = waterGlasses,
            waterTargetGlasses = 8,
            waterMl = waterMl,
            waterTargetMl = 2000,
            waterWeeklyBars = waterWeeklyBars,
            sleepHours = sleepHours,
            sleepBedTime = sleepBed,
            sleepWakeTime = sleepWake,
            sleepBadge = sleepBadge,
            sleepRegularityPercent = sleepRegularityPercent,
            sleepWeeklyAverage = sleepWeeklyAverage,
            sleepTrendPoints = sleepTrendPoints,
            currentWeightKg = currentWeightKg,
            weightDeltaMonthKg = weightDeltaMonthKg,
            weightStartKg = weightStartKg,
            weightTargetKg = weightTargetKg,
            lastWeighDateText = lastWeighDateText,
            weightHistoryPoints = weightHistoryPoints,
            heightCm = height,
            bmiValue = bmiVal,
            bmiCategory = bmiCat,
            bmiColorHex = bmiColor,
            bmiInterpretation = bmiDesc,
            idealWeightRangeText = idealRange,
            bmiProgressFraction = bmiProgress,
            steps = steps,
            stepsTarget = 8000,
            stepsPercent = ((steps / 8000f) * 100).toInt().coerceIn(0, 100),
            distanceKm = distanceKm,
            workoutMinutes = workoutMinutes,
            caloriesKcal = calories,
            stepsWeeklyBars = stepsWeeklyBars,
            mood = mood,
            moodNote = moodNote,
            moodAverageEmoji = moodAverageEmoji,
            moodHistoryDots = moodHistoryDots,
            waistCm = waistCm,
            waistDate = waistDate,
            bloodPressure = bloodPressure,
            bloodPressureDate = bloodPressureDate,
            temperatureC = temperatureC,
            temperatureDate = temperatureDate,
            waterReminderEnabled = reminders.waterReminder,
            sleepReminderEnabled = reminders.sleepReminder,
            weighReminderEnabled = reminders.weighReminder,
            avgWaterGlasses = avgWaterGlasses,
            avgSleepHours = sleepWeeklyAverage,
            avgSteps = avgSteps,
            dominantMoodText = dominantMoodText,
            dominantMoodEmoji = dominantMoodEmoji,
            bestDayHighlight = bestDayHighlight,
            insightText = insightText,
            isSpeedDialOpen = dialogs.isSpeedDialOpen,
            showWeightDialog = dialogs.showWeightDialog,
            showSleepDialog = dialogs.showSleepDialog,
            showWorkoutDialog = dialogs.showWorkoutDialog,
            showMeasurementType = dialogs.showMeasurementType,
            showAddReminderDialog = reminders.showAddReminder,
            showHeightDialog = reminders.showHeightDialog,
            showTargetWeightDialog = dialogs.showTargetWeightDialog
        )
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
    )

    fun selectDate(dateString: String) {
        selectedDateFlow.value = dateString
    }

    fun incrementWater() {
        viewModelScope.launch {
            repository.incrementWater()
        }
    }

    fun decrementWater() {
        viewModelScope.launch {
            repository.decrementWater()
        }
    }

    fun addWaterMl(ml: Int) {
        val extraGlasses = (ml / 250).coerceAtLeast(1)
        val current = uiState.value.waterGlasses
        viewModelScope.launch {
            repository.setWaterGlasses(current + extraGlasses)
        }
    }

    fun setMood(mood: Int) {
        val note = uiState.value.moodNote
        val date = selectedDateFlow.value
        viewModelScope.launch {
            repository.logMood(mood, note, date)
        }
    }

    fun setMoodNote(note: String) {
        val mood = uiState.value.mood
        val date = selectedDateFlow.value
        viewModelScope.launch {
            repository.logMood(mood, note, date)
        }
    }

    fun openSpeedDial() { isSpeedDialOpenFlow.value = true }
    fun closeSpeedDial() { isSpeedDialOpenFlow.value = false }

    fun openWeightDialog() { showWeightDialogFlow.value = true }
    fun closeWeightDialog() { showWeightDialogFlow.value = false }
    fun saveWeight(weightKg: Double) {
        val date = selectedDateFlow.value
        showWeightDialogFlow.value = false
        viewModelScope.launch {
            repository.logWeight(weightKg, date)
        }
    }

    fun openHeightDialog() { showHeightDialogFlow.value = true }
    fun closeHeightDialog() { showHeightDialogFlow.value = false }
    fun saveHeight(heightCm: Float) {
        showHeightDialogFlow.value = false
        viewModelScope.launch {
            repository.setUserHeightCm(heightCm)
        }
    }

    fun openTargetWeightDialog() { showTargetWeightDialogFlow.value = true }
    fun closeTargetWeightDialog() { showTargetWeightDialogFlow.value = false }
    fun saveTargetWeight(targetKg: Double) {
        showTargetWeightDialogFlow.value = false
        viewModelScope.launch {
            repository.setWeightTargetKg(targetKg)
        }
    }

    fun openSleepDialog() { showSleepDialogFlow.value = true }
    fun closeSleepDialog() { showSleepDialogFlow.value = false }
    fun saveSleep(hours: Double, bedTime: String, wakeTime: String) {
        val date = selectedDateFlow.value
        showSleepDialogFlow.value = false
        viewModelScope.launch {
            repository.logSleep(hours, bedTime, wakeTime, date)
        }
    }

    fun openWorkoutDialog() { showWorkoutDialogFlow.value = true }
    fun closeWorkoutDialog() { showWorkoutDialogFlow.value = false }
    fun saveWorkout(workoutMinutes: Int, distanceKm: Double, caloriesKcal: Int, additionalSteps: Int) {
        val date = selectedDateFlow.value
        showWorkoutDialogFlow.value = false
        viewModelScope.launch {
            repository.logActivity(workoutMinutes, distanceKm, caloriesKcal, additionalSteps, date)
        }
    }

    fun openMeasurementDialog(type: String) { showMeasurementTypeFlow.value = type }
    fun closeMeasurementDialog() { showMeasurementTypeFlow.value = null }
    fun saveMeasurement(type: String, value: String) {
        val date = selectedDateFlow.value
        showMeasurementTypeFlow.value = null
        viewModelScope.launch {
            when (type) {
                "waist" -> {
                    val num = value.toDoubleOrNull()
                    repository.logBodyMeasurement(waistCm = num, dateStr = date)
                }
                "bp" -> {
                    repository.logBodyMeasurement(bloodPressure = value, dateStr = date)
                }
                "temp" -> {
                    val num = value.toDoubleOrNull()
                    repository.logBodyMeasurement(temperatureC = num, dateStr = date)
                }
            }
        }
    }

    fun toggleWaterReminder() { waterReminderFlow.value = !waterReminderFlow.value }
    fun toggleSleepReminder() { sleepReminderFlow.value = !sleepReminderFlow.value }
    fun toggleWeighReminder() { weighReminderFlow.value = !weighReminderFlow.value }

    fun openAddReminderDialog() { showAddReminderDialogFlow.value = true }
    fun closeAddReminderDialog() { showAddReminderDialogFlow.value = false }
}
