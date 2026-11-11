package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_transactions")
data class BusinessTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val type: String, // "IN", "OUT", "TRANSFER", "YUAN"
    val amountAr: Long,
    val accountName: String,
    val referenceTag: String = "",
    val dateMillis: Long = System.currentTimeMillis()
)
