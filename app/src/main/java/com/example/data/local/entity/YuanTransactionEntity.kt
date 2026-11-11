package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "yuan_transactions",
    indices = [Index(value = ["orderId", "type"], unique = true)]
)
data class YuanTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "BUY" (Achat Yuans), "SPEND" (Paiement commande), "REFUND" (Remboursement usine)
    val amountYuan: Double, // Montant en ¥
    val exchangeRate: Double, // Taux Ar / ¥
    val amountAr: Long, // Montant total en Ariary
    val platform: String = "Alipay", // "Alipay", "WeChat Pay", "1688", "Taobao"
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val orderId: Long? = null,
    val isDemo: Boolean = false
)
