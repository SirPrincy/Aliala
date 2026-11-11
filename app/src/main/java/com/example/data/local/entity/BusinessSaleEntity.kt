package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_sales")
data class BusinessSaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val clientName: String,
    val clientPhone: String = "",
    val quantity: Int = 1,
    val unitResalePriceAr: Long = 0L,
    val unitCostAr: Long = 0L,
    val totalPriceAr: Long = 0L,
    val paidAmountAr: Long = 0L,
    val isFullyPaid: Boolean = true,
    val saleDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 15 * 86400000L,
    val invoiceNumber: String = "", // e.g. "#088", "#087", "#086"
    val saleSource: String = "STOCK", // "STOCK" ("Sur stock") ou "ORDER" ("Sur commande")
    val domain: String = "BUSINESS", // "BUSINESS" ("Business Pro") ou "FAMILY_FRIENDS" ("Famille & Amis")
    val linkedOrderId: Long? = null, // ID optionnel du lot d'achat lié dans business_orders
    val clientId: Long? = null, // ID optionnel du client lié dans clients
    val isDemo: Boolean = false
)
