package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class HealthEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val waterGlasses: Int = 0,
    val sleepHours: Double = 0.0,
    val weightKg: Double? = null,
    val steps: Int = 0,
    val mood: Int = -1, // -1: non renseigné, 0: Très bien, 1: Bien, 2: Neutre, 3: Mauvais, 4: Très mauvais
    val isDemo: Boolean = false,
    val waistCm: Double? = null,
    val bloodPressure: String? = null,
    val temperatureC: Double? = null,
    val moodNote: String = "",
    val workoutMinutes: Int = 0,
    val distanceKm: Double = 0.0,
    val caloriesKcal: Int = 0,
    val sleepBedTime: String = "",
    val sleepWakeTime: String = ""
)
