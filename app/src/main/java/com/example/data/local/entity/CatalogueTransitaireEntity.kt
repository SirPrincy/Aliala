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
    val airNormalRateAr: Long = 45000L,
    val airFastRateAr: Long = 60000L,
    val seaRateAr: Long = 18000L,
    val airNormalLeadDays: String = "7 à 10 jours",
    val airFastLeadDays: String = "3 à 5 jours",
    val seaNormalLeadDays: String = "30 à 45 jours",
    val goodsCategoryDelays: String = "Générale: Normal | Batterie: +2j | Poudre/Liquide: +3j | Téléphone: +2j"
)
