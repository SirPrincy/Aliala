package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Mariage 2027", "Nouveau PC"
    val targetAmount: Long = 20_000_000L,
    val savedAmount: Long = 7_000_000L,
    val deadline: Long = System.currentTimeMillis() + 820 * 86400000L,
    val category: String = "Épargne Projet", // e.g. "Épargne Projet", "Matériel Pro"
    val isDemo: Boolean = false
)
