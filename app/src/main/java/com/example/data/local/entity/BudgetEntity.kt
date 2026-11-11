package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val monthKey: String, // "yyyy-MM"
    val amount: Long, // in Ariary
    val isDemo: Boolean = false
)
