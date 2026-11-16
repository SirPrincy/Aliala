package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_debts")
data class BusinessDebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val direction: String = "BORROWED", // "BORROWED" (Je dois) | "LENT" (On me doit)
    val counterparty: String = "",
    val principalAr: Long,
    val paidAr: Long = 0L,
    val interestPercent: Double = 0.0,
    val dueDateMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val repaymentFrequency: String = "Ponctuel", // "Ponctuel", "Mensuel", "Hebdomadaire"
    val accountName: String = "Hors caisse",
    val linkedTransactionId: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "business_debt_payments")
data class BusinessDebtPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val debtId: Long,
    val amountAr: Long,
    val accountName: String = "Hors caisse",
    val linkedTransactionId: Long? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
