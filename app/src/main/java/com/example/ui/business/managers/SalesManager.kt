package com.example.ui.business.managers

import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.BusinessTransactionEntity
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
        paymentMethod: String = "ESPECES",
        accountName: String = ""
    ): String? {
        if (additionalPaymentAr <= 0L) return null
        val remainingDue = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
        val appliedAmount = additionalPaymentAr.coerceAtMost(remainingDue)
        if (appliedAmount <= 0L) return null
        val nowMs = System.currentTimeMillis()
        val cleanAccount = accountName.trim()
        val resolvedMethod = when {
            cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true) -> cleanAccount
            cleanAccount.equals("Hors caisse", ignoreCase = true) -> "Hors caisse"
            else -> paymentMethod
        }
        val newPaid = sale.paidAmountAr + appliedAmount
        repository.runInTransaction {
            val updated = sale.copy(
                paidAmountAr = newPaid,
                isFullyPaid = newPaid >= sale.totalPriceAr
            )
            repository.updateBusinessSale(updated)
            repository.insertSalePayment(
                SalePaymentEntity(
                    saleId = sale.id,
                    amountAr = appliedAmount,
                    paymentDate = nowMs,
                    paymentMethod = resolvedMethod
                )
            )
            if (cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true)) {
                val matchedAccount = repository.getBusinessAccountsSnapshot()
                    .firstOrNull { !it.isYuanAccount && it.isActive && it.name.equals(cleanAccount, ignoreCase = true) }
                if (matchedAccount != null) {
                    repository.insertBusinessTransaction(
                        BusinessTransactionEntity(
                            title = "Règlement facture ${sale.invoiceNumber.ifBlank { "#${sale.id}" }} (${sale.clientName})",
                            type = "IN",
                            amountAr = appliedAmount,
                            accountName = matchedAccount.name,
                            dateMillis = nowMs,
                            referenceTag = "Vente • ${sale.invoiceNumber.ifBlank { sale.clientName }}"
                        )
                    )
                }
            }
        }
        val suffix = if (cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true)) " sur $cleanAccount" else ""
        return "Règlement de ${CurrencyFormatter.formatAriary(appliedAmount)} enregistré$suffix pour ${sale.clientName}"
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
        linkedOrderId: Long? = null,
        accountName: String = ""
    ): String {
        val totalPrice = unitPriceAr * quantity
        val trimmedClientName = clientName.trim()
        val nowMs = System.currentTimeMillis()
        val cleanAccount = accountName.trim()
        val paymentMethodLabel = when {
            cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true) -> cleanAccount
            cleanAccount.equals("Hors caisse", ignoreCase = true) -> "Hors caisse"
            else -> "ESPECES"
        }
        val actualPaid = paidAmountAr.coerceAtMost(totalPrice)

        val newInvoiceNum = repository.runInTransaction {
            val allSales = repository.businessSales.firstOrNull() ?: emptyList()
            val maxInvoiceSeq = allSales.mapNotNull { s ->
                s.invoiceNumber.filter { it.isDigit() }.toIntOrNull()
            }.maxOrNull() ?: 0
            val invoiceNum = String.format(Locale.US, "FAC-%04d", maxInvoiceSeq + 1)

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
                    invoiceNumber = invoiceNum,
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
                        paymentDate = nowMs,
                        paymentMethod = paymentMethodLabel
                    )
                )
                if (cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true)) {
                    val matchedAccount = repository.getBusinessAccountsSnapshot()
                        .firstOrNull { !it.isYuanAccount && it.isActive && it.name.equals(cleanAccount, ignoreCase = true) }
                    if (matchedAccount != null) {
                        repository.insertBusinessTransaction(
                            BusinessTransactionEntity(
                                title = "Vente $invoiceNum : ${productName.trim()} ($trimmedClientName)",
                                type = "IN",
                                amountAr = actualPaid,
                                accountName = matchedAccount.name,
                                dateMillis = nowMs,
                                referenceTag = "Vente • $invoiceNum"
                            )
                        )
                    }
                }
            }

            if (resolvedOrderId != null && resolvedOrderId > 0L) {
                val targetOrder = allOrders.firstOrNull { it.id == resolvedOrderId }
                if (targetOrder != null) {
                    repository.updateBusinessOrder(
                        targetOrder.copy(
                            stockCount = (targetOrder.stockCount - quantity).coerceAtLeast(0),
                            unitsSold = targetOrder.unitsSold + quantity
                        )
                    )
                }
            }
            invoiceNum
        }

        val caisseSuffix = if (actualPaid > 0L && cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true)) " • $cleanAccount crédité" else ""
        return "Facture $newInvoiceNum créée pour $trimmedClientName$caisseSuffix"
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
        repository.runInTransaction {
            if (sale.linkedOrderId != null && sale.linkedOrderId > 0L) {
                val allOrders = repository.businessOrders.firstOrNull() ?: emptyList()
                val targetOrder = allOrders.firstOrNull { it.id == sale.linkedOrderId }
                if (targetOrder != null) {
                    repository.updateBusinessOrder(
                        targetOrder.copy(
                            stockCount = targetOrder.stockCount + sale.quantity,
                            unitsSold = (targetOrder.unitsSold - sale.quantity).coerceAtLeast(0)
                        )
                    )
                }
            }
            repository.deleteBusinessSale(sale.id)
        }
        return "Facture ${sale.invoiceNumber.ifBlank { "#---" }} de ${sale.clientName} annulée et stock réintégré"
    }

    suspend fun addClient(name: String, category: String, phone: String, initialPurchaseAr: Long): String {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return "Veuillez saisir le nom du client"

        return repository.runInTransaction {
            val existing = repository.findClientByName(trimmedName)
            val avatarType = when (category) {
                "Business" -> "CYAN"
                "Myself" -> "ORANGE"
                else -> "VIOLET"
            }
            val domain = when (category) {
                "Business" -> "BUSINESS"
                "Myself" -> "PERSONAL"
                else -> "FAMILY_FRIENDS"
            }

            if (existing == null) {
                repository.insertClient(
                    ClientEntity(
                        name = trimmedName,
                        phone = phone.trim(),
                        category = category,
                        avatarType = avatarType
                    )
                )
            } else if (existing.phone.isBlank() && phone.isNotBlank()) {
                repository.updateClient(existing.copy(phone = phone.trim()))
            }

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
                    domain = domain,
                    linkedOrderId = null
                )
                if (existing != null) "Achat initial ajouté au client existant « $trimmedName »"
                else "Client « $trimmedName » ajouté avec achat initial"
            } else {
                if (existing != null) "Le client « $trimmedName » existe déjà"
                else "Client « $trimmedName » ajouté"
            }
        }
    }

    suspend fun recordClientPayment(
        clientId: Long,
        amountAr: Long,
        clientName: String = "",
        accountName: String = "",
        clientPhone: String = "",
        category: String = "Business"
    ): String? {
        if (amountAr <= 0L) return "Montant d'encaissement invalide"

        return repository.runInTransaction {
            var client = if (clientId > 0L && clientId < 10000L) repository.getClientById(clientId) else null
            if (client == null && clientName.isNotBlank()) {
                client = repository.findClientByName(clientName.trim())
            }
            if (client == null && clientName.isNotBlank()) {
                val newId = repository.insertClient(
                    ClientEntity(
                        name = clientName.trim(),
                        phone = clientPhone.trim(),
                        category = category,
                        avatarType = when (category) {
                            "Business" -> "CYAN"
                            "Myself" -> "ORANGE"
                            else -> "VIOLET"
                        }
                    )
                )
                client = repository.getClientById(newId)
            }

            val resolvedClientId = client?.id
            val resolvedName = (client?.name ?: clientName).trim()
            val allSales = repository.businessSales.firstOrNull() ?: emptyList()
            val unpaidSales = allSales.filter { sale ->
                val matchesClient = (resolvedClientId != null && sale.clientId == resolvedClientId) ||
                    (clientId in 1L..9999L && sale.clientId == clientId) ||
                    (resolvedName.isNotBlank() && sale.clientName.trim().equals(resolvedName, ignoreCase = true))
                matchesClient && !sale.isFullyPaid && (sale.totalPriceAr - sale.paidAmountAr) > 0L
            }.sortedBy { it.saleDate }

            if (unpaidSales.isEmpty()) {
                return@runInTransaction "Aucun solde impayé pour ${resolvedName.ifBlank { "ce client" }}"
            }

            val cleanAccount = accountName.trim()
            val paymentMethodLabel = if (cleanAccount.isBlank() || cleanAccount.equals("Hors caisse", ignoreCase = true)) {
                "Hors caisse"
            } else cleanAccount

            var remaining = amountAr
            var appliedTotal = 0L
            val nowMs = System.currentTimeMillis()
            for (sale in unpaidSales) {
                if (remaining <= 0L) break
                val dueOnSale = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
                if (dueOnSale > 0L) {
                    val pay = minOf(remaining, dueOnSale)
                    val newPaid = sale.paidAmountAr + pay
                    repository.updateBusinessSale(
                        sale.copy(
                            paidAmountAr = newPaid,
                            isFullyPaid = newPaid >= sale.totalPriceAr,
                            clientId = sale.clientId ?: resolvedClientId
                        )
                    )
                    repository.insertSalePayment(
                        SalePaymentEntity(
                            saleId = sale.id,
                            amountAr = pay,
                            paymentDate = nowMs,
                            paymentMethod = paymentMethodLabel
                        )
                    )
                    remaining -= pay
                    appliedTotal += pay
                }
            }

            if (appliedTotal <= 0L) {
                return@runInTransaction "Aucun règlement appliqué"
            }

            if (cleanAccount.isNotBlank() && !cleanAccount.equals("Hors caisse", ignoreCase = true)) {
                val matchedAccount = repository.getBusinessAccountsSnapshot()
                    .firstOrNull { !it.isYuanAccount && it.isActive && it.name.equals(cleanAccount, ignoreCase = true) }
                if (matchedAccount != null) {
                    repository.insertBusinessTransaction(
                        BusinessTransactionEntity(
                            title = "Encaissement client : ${resolvedName.ifBlank { "Client" }}",
                            type = "IN",
                            amountAr = appliedTotal,
                            accountName = matchedAccount.name,
                            dateMillis = nowMs,
                            referenceTag = "Vente • Client"
                        )
                    )
                }
            }

            val labelSuffix = if (paymentMethodLabel != "Hors caisse") " sur $paymentMethodLabel" else ""
            "Encaissement de ${CurrencyFormatter.formatAriary(appliedTotal)} enregistré$labelSuffix pour ${resolvedName.ifBlank { "le client" }}"
        }
    }
}
