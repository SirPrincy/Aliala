package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogue_transitaires")
data class CatalogueTransitaireEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contact: String = "",
    val address: String = "",
    val defaultTariffPerKg: Long = 0L,
    val notes: String = "",
    val airNormalRateAr: Long = 0L,
    val airFastRateAr: Long = 0L,
    val seaRateAr: Long = 0L,
    val airNormalLeadDays: String = "",
    val airFastLeadDays: String = "",
    val seaNormalLeadDays: String = "",
    val goodsCategoryDelays: String = "",
    val rateApplicationDate: String = ""
)
