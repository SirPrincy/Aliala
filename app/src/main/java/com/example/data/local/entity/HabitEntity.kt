package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "water", // "water", "sport", "sleep"
    val targetDaysPerWeek: Int = 7,
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false,
    val habitType: String = "BOOLEAN",
    val targetCount: Int = 1,
    val unit: String = "fois",
    val moment: String = "Matin",
    val colorHex: String = "#06B6D4",
    val reminderTimes: String = "",
    val note: String = ""
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // "yyyy-MM-dd"
    val isDemo: Boolean = false
)
