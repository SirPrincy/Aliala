package com.example.ui.business.managers

import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.SalePaymentEntity
import com.example.data.repository.AppRepository
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

class SalesManager(private val repository: AppRepository) {

    suspend fun recordSalePayment(
        sale: BusinessSaleEntity,
        additionalPaymentAr: Long,
        paymentMethod: String = "ESPECES"
    ): String? {
        if (additionalPaymentAr <= 0L) return null
        val remainingDue = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
        val appliedAmount = additionalPaymentAr.coerceAtMost(remainingDue)
        if (appliedAmount <= 0L) return null
        val newPaid = sale.paidAmountAr + appliedAmount
        val updated = sale.copy(
            paidAmountAr = newPaid,
            isFullyPaid = newPaid >= sale.totalPriceAr
        )
        repository.updateBusinessSale(updated)
        repository.insertSalePayment(
            SalePaymentEntity(
                saleId = sale.id,
                amountAr = appliedAmount,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = paymentMethod
            )
        )
        return "Règlement de ${CurrencyFormatter.formatAriary(appliedAmount)} enregistré pour ${sale.clientName}"
    }

    suspend fun addNewSale(
        productName: String,
        clientName: String,
        clientPhone: String,
        quantity: Int,
        unitPriceAr: Long,
        unitCostAr: Long,
        paidAmountAr: Long,
        saleSource: String,
        domain: String,
        linkedOrderId: Long? = null
    ): String {
        val totalPrice = unitPriceAr * quantity
        val allSales = repository.businessSales.firstOrNull() ?: emptyList()

        val maxInvoiceSeq = allSales.mapNotNull { s ->
            val digits = s.invoiceNumber.filter { it.isDigit() }
            digits.toIntOrNull()
        }.maxOrNull() ?: 0
        val nextSeq = maxInvoiceSeq + 1
        val newInvoiceNum = String.format(Locale.US, "FAC-%04d", nextSeq)

        val trimmedClientName = clientName.trim()
        val existingClient = repository.findClientByName(trimmedClientName)
        val finalClientId = existingClient?.id ?: repository.insertClient(
            ClientEntity(
                name = trimmedClientName,
                phone = clientPhone.trim(),
                category = if (domain == "BUSINESS") "Business" else "Famille & amis",
                avatarType = if (domain == "BUSINESS") "CYAN" else "VIOLET"
            )
        )

        val allOrders = repository.businessOrders.firstOrNull() ?: emptyList()
        val resolvedOrderId = linkedOrderId ?: allOrders.firstOrNull {
            it.stockCount > 0 && it.product.trim().equals(productName.trim(), ignoreCase = true)
        }?.id

        val actualPaid = paidAmountAr.coerceAtMost(totalPrice)
        val saleId = repository.insertBusinessSale(
            BusinessSaleEntity(
                productName = productName.trim(),
                clientName = trimmedClientName,
                clientPhone = clientPhone.trim(),
                quantity = quantity,
                unitResalePriceAr = unitPriceAr,
                unitCostAr = unitCostAr,
                totalPriceAr = totalPrice,
                paidAmountAr = actualPaid,
                isFullyPaid = actualPaid >= totalPrice,
                invoiceNumber = newInvoiceNum,
                saleSource = saleSource,
                domain = domain,
                linkedOrderId = resolvedOrderId,
                clientId = finalClientId
            )
        )

        if (actualPaid > 0L) {
            repository.insertSalePayment(
                SalePaymentEntity(
                    saleId = saleId,
                    amountAr = actualPaid,
                    paymentDate = System.currentTimeMillis(),
                    paymentMethod = "ESPECES"
                )
            )
        }

        if (resolvedOrderId != null && resolvedOrderId > 0L) {
            val targetOrder = allOrders.firstOrNull { it.id == resolvedOrderId }
            if (targetOrder != null) {
                val newStock = (targetOrder.stockCount - quantity).coerceAtLeast(0)
                val newUnitsSold = targetOrder.unitsSold + quantity
                repository.updateBusinessOrder(
                    targetOrder.copy(
                        stockCount = newStock,
                        unitsSold = newUnitsSold
                    )
                )
            }
        }

        return "Facture $newInvoiceNum créée pour $trimmedClientName"
    }

    suspend fun addBusinessSale(
        clientName: String,
        clientPhone: String,
        productName: String,
        quantity: Int,
        unitResalePriceAr: Long,
        unitCostAr: Long,
        paidAmountAr: Long
    ): String {
        val totalPrice = unitResalePriceAr * quantity
        repository.insertBusinessSale(
            BusinessSaleEntity(
                productName = productName,
                clientName = clientName,
                clientPhone = clientPhone,
                quantity = quantity,
                unitResalePriceAr = unitResalePriceAr,
                unitCostAr = unitCostAr,
                totalPriceAr = totalPrice,
                paidAmountAr = paidAmountAr,
                isFullyPaid = paidAmountAr >= totalPrice
            )
        )
        return "Vente enregistrée pour $clientName"
    }

    suspend fun deleteSale(sale: BusinessSaleEntity): String {
        if (sale.linkedOrderId != null && sale.linkedOrderId > 0L) {
            val allOrders = repository.businessOrders.firstOrNull() ?: emptyList()
            val targetOrder = allOrders.firstOrNull { it.id == sale.linkedOrderId }
            if (targetOrder != null) {
                val restoredStock = targetOrder.stockCount + sale.quantity
                val restoredUnitsSold = (targetOrder.unitsSold - sale.quantity).coerceAtLeast(0)
                repository.updateBusinessOrder(
                    targetOrder.copy(
                        stockCount = restoredStock,
                        unitsSold = restoredUnitsSold
                    )
                )
            }
        }
        repository.deleteBusinessSale(sale.id)
        return "Facture ${sale.invoiceNumber.ifBlank { "#---" }} de ${sale.clientName} annulée et stock réintégré"
    }

    suspend fun addClient(name: String, category: String, phone: String, initialPurchaseAr: Long): String {
        val trimmedName = name.trim()
        val clientId = repository.insertClient(
            ClientEntity(
                name = trimmedName,
                phone = phone.trim(),
                category = category,
                avatarType = if (category == "Business") "CYAN" else "VIOLET"
            )
        )
        if (initialPurchaseAr > 0L) {
            addNewSale(
                productName = "Achat initial",
                clientName = trimmedName,
                clientPhone = phone.trim(),
                quantity = 1,
                unitPriceAr = initialPurchaseAr,
                unitCostAr = 0L,
                paidAmountAr = initialPurchaseAr,
                saleSource = "STOCK",
                domain = if (category == "Business") "BUSINESS" else "FAMILY_FRIENDS",
                linkedOrderId = null
            )
        }
        return "Client « $trimmedName » ajouté"
    }

    suspend fun recordClientPayment(clientId: Long, amountAr: Long): String? {
        if (amountAr <= 0L) return null
        val client = repository.getClientById(clientId)
        val clientName = client?.name ?: ""
        val allSales = repository.businessSales.firstOrNull() ?: emptyList()
        val unpaidSales = allSales.filter { sale ->
            (sale.clientId == clientId || (clientName.isNotBlank() && sale.clientName.trim().equals(clientName.trim(), ignoreCase = true))) &&
            !sale.isFullyPaid
        }.sortedBy { it.saleDate }

        var remaining = amountAr
        for (sale in unpaidSales) {
            if (remaining <= 0) break
            val dueOnSale = sale.totalPriceAr - sale.paidAmountAr
            if (dueOnSale > 0) {
                val pay = minOf(remaining, dueOnSale)
                val newPaid = sale.paidAmountAr + pay
                val updatedSale = sale.copy(
                    paidAmountAr = newPaid,
                    isFullyPaid = newPaid >= sale.totalPriceAr,
                    clientId = if (sale.clientId == null) clientId else sale.clientId
                )
                repository.updateBusinessSale(updatedSale)
                repository.insertSalePayment(
                    SalePaymentEntity(
                        saleId = sale.id,
                        amountAr = pay,
                        paymentDate = System.currentTimeMillis()
                    )
                )
                remaining -= pay
            }
        }
        return "Encaissement de ${CurrencyFormatter.formatAriary(amountAr)} enregistré pour ${if (clientName.isNotBlank()) clientName else "le client"}"
    }
}
