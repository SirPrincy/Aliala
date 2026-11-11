package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogue_platforms")
data class CataloguePlatformEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val aliasKey: String = "",
    val subtitle: String = "",
    val badgeText: String = "",
    val isVerified: Boolean = false,
    val iconType: String = "STORE",
    val colorHex: String = "#712ae2",
    val shippingTag: String = "Port Payant (¥ 10 - ¥ 25)",
    val shippingTagIcon: String = "local_shipping",
    val secondaryTag: String = "Min. commande : 2-5 pcs",
    val isActive: Boolean = true,
    val displayOrder: Int = 0,
    val defaultYuanSpent: Double = 0.0,
    val defaultOrdersCount: Int = 0,
    val defaultLastDate: String = "",
    val transitNotes: String = "",
    val isDemo: Boolean = false
)
