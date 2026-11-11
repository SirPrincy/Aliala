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
    val notes: String = ""
)
