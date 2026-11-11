package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDate: Long, // epoch millis
    val categoryColorHex: String = "#EF4444", // hex for category dot (e.g. red, blue, green, etc.)
    val isCompleted: Boolean = false,
    val isDemo: Boolean = false
)
