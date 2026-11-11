package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val category: String = "Business", // "Business" ou "Famille & amis"
    val avatarType: String = "CYAN", // "CYAN", "VIOLET"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)
