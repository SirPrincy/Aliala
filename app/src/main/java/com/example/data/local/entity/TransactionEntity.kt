package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val walletId: Long,
    val date: Long, // epoch millis
    val amount: Long, // in Ariary, always positive in storage
    val type: String, // "EXPENSE", "INCOME", "TRANSFER"
    val category: String, // "Nourriture", "Business", "Loisirs", "Transport", "Santé", "Autres"
    val isDemo: Boolean = false
)
