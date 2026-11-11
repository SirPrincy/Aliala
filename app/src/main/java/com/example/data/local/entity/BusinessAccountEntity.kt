package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_accounts")
data class BusinessAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val subtitle: String = "Compte",
    val shortCode: String = "CPT",
    val balanceAr: Long = 0L,
    val isYuanAccount: Boolean = false,
    val balanceYuan: Double = 0.0,
    val isActive: Boolean = true
)
