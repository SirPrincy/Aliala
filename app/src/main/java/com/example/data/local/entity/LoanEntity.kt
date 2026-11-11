package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val direction: String, // "BORROWED" (Je dois) | "LENT" (On me doit)
    val counterpartyType: String, // "PERSON" | "BANK"
    val name: String, // e.g., "Prêt BNI Express", "Faly"
    val amount: Long, // principal amount in Ariary
    val interestPercent: Double? = null, // e.g. 5.0
    val dueDate: Long, // epoch millis
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)

@Entity(tableName = "loan_payments")
data class LoanPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val amount: Long,
    val date: Long,
    val isDemo: Boolean = false
)
