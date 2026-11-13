package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_investments")
data class BusinessInvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val category: String = "MATERIEL", // "PLACEMENT", "MATERIEL", "LOGISTIQUE", "STOCK_STRATEGIQUE", "MARKETING", "INFRASTRUCTURE", "AUTRE"
    val amountAr: Long,
    val estimatedValueAr: Long = amountAr,
    val generatedReturnAr: Long = 0L,
    val status: String = "ACTIF", // "ACTIF", "AMORTI", "CEDE"
    val accountName: String = "Externe",
    val linkedTransactionId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
