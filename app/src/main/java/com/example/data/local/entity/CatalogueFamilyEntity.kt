package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogue_families")
data class CatalogueFamilyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String = "🎧",
    val colorHex: String = "#06B6D4",
    val displayOrder: Int = 0,
    val isDemo: Boolean = false
)
