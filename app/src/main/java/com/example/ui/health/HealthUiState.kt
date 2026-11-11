package com.example.ui.health

data class HealthDayItem(
    val dayLetter: String, // "L", "M", "M", "J", "V", "S", "D"
    val dayNumber: Int,    // 20, 21, 22, 23...
    val dateString: String,// "yyyy-MM-dd"
    val isSelected: Boolean = false,
    val isCompleted: Boolean = false,
    val isToday: Boolean = false
)

data class HealthUiState(
    val selectedDate: String = "",
    val selectedDateDisplay: String = "",
    val vitalityScorePercent: Int = 0,
    val weekDays: List<HealthDayItem> = emptyList(),
    val globalProgressPercent: Int = 0,
    val goalsEngagedText: String = "",
    val motivationTip: String = "",

    // Water (Hydratation)
    val waterGlasses: Int = 0,
    val waterTargetGlasses: Int = 8,
    val waterMl: Int = 0,
    val waterTargetMl: Int = 2000,
    val waterWeeklyBars: List<Float> = emptyList(),

    // Sleep (Sommeil)
    val sleepHours: Double = 0.0,
    val sleepBedTime: String = "",
    val sleepWakeTime: String = "",
    val sleepBadge: String = "",
    val sleepRegularityPercent: Int = 0,
    val sleepWeeklyAverage: String = "—",
    val sleepTrendPoints: List<Float> = emptyList(),

    // Weight (Poids)
    val currentWeightKg: Double? = null,
    val weightDeltaMonthKg: Double = 0.0,
    val weightStartKg: Double? = null,
    val weightTargetKg: Double = 66.0,
    val lastWeighDateText: String = "Aucune pesée",
    val weightHistoryPoints: List<Float> = emptyList(),

    // RSI / IMC (Poids & Taille)
    val heightCm: Float? = null,
    val bmiValue: Double? = null,
    val bmiCategory: String = "Non renseigné",
    val bmiColorHex: String = "#06B6D4",
    val bmiInterpretation: String = "",
    val idealWeightRangeText: String = "",
    val bmiProgressFraction: Float = 0f, // 0.0f à 1.0f sur l'intervalle 15.0 - 35.0
    val showHeightDialog: Boolean = false,

    // Activity & Steps
    val steps: Int = 0,
    val stepsTarget: Int = 8000,
    val stepsPercent: Int = 0,
    val distanceKm: Double = 0.0,
    val workoutMinutes: Int = 0,
    val caloriesKcal: Int = 0,
    val stepsWeeklyBars: List<Float> = emptyList(),

    // Mood (Humeur)
    val mood: Int = -1, // -1: non renseigné, 0: 😀, 1: 🙂, 2: 😐, 3: 🙁, 4: 😞
    val moodNote: String = "",
    val moodAverageEmoji: String = "🙂",
    val moodHistoryDots: List<Int> = emptyList(),

    // Body Measurements
    val waistCm: Double? = null,
    val waistDate: String = "",
    val bloodPressure: String? = null,
    val bloodPressureDate: String = "",
    val temperatureC: Double? = null,
    val temperatureDate: String = "",

    // Reminders
    val waterReminderEnabled: Boolean = true,
    val sleepReminderEnabled: Boolean = true,
    val weighReminderEnabled: Boolean = true,

    // Weekly Summary
    val avgWaterGlasses: Double = 0.0,
    val avgSleepHours: String = "—",
    val avgSteps: Int = 0,
    val dominantMoodText: String = "Neutre",
    val dominantMoodEmoji: String = "😐",
    val bestDayHighlight: String = "—",

    // Personalized Insight
    val insightText: String = "",

    // Interactive Dialogs & Drawer
    val isSpeedDialOpen: Boolean = false,
    val showWeightDialog: Boolean = false,
    val showSleepDialog: Boolean = false,
    val showWorkoutDialog: Boolean = false,
    val showMeasurementType: String? = null, // "waist", "bp", "temp"
    val showAddReminderDialog: Boolean = false,
    val showTargetWeightDialog: Boolean = false
)
