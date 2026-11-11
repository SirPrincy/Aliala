package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_payments",
    foreignKeys = [
        ForeignKey(
            entity = BusinessSaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SalePaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val amountAr: Long,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "ESPECES",
    val note: String = ""
)
