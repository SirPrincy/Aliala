package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transitaire_rate_history")
data class TransitaireRateHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transitaireId: Long,
    val transitaireName: String,
    val mode: String, // "AERIEN", "AERIEN_FAST", "MARITIME"
    val oldRateAr: Long,
    val newRateAr: Long,
    val deltaAr: Long,
    val deltaPercent: Double,
    val changedAtMillis: Long,
    val effectiveDateLabel: String, // Format dd/MM/yyyy
    val note: String = "",
    val isDemo: Boolean = false
)
