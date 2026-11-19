package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_categories")
data class BusinessCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String, // "IN" (Revenu / Entrée) ou "OUT" (Dépense / Sortie)
    val iconKey: String = "receipt_long",
    val colorKey: String = "CYAN",
    val isDefault: Boolean = false,
    val displayOrder: Int = 0
)
