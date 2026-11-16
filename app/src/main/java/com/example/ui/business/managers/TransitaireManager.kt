package com.example.ui.business.managers

import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.CatalogueTransitaireEntity
import com.example.data.repository.AppRepository
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransitaireManager(private val repository: AppRepository) {

    suspend fun addCatalogueTransitaire(
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String = "",
        chinaWarehouseAddress: String = "",
        isActive: Boolean = true,
        rateApplicationDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date()),
        airLeadDays: String = "7 à 10 jours",
        seaLeadDays: String = "30 à 45 jours",
        internalNotes: String = "",
        airFastRate: Long = 60000L,
        airFastLeadDays: String = "3 à 5 jours",
        goodsCategoryDelays: String = ""
    ): String {
        val effectiveDate = rateApplicationDate.ifBlank {
            SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
        }
        repository.insertCatalogueTransitaire(
            CatalogueTransitaireEntity(
                name = name.trim(),
                contact = contact.trim(),
                address = chinaWarehouseAddress.trim(),
                defaultTariffPerKg = airRate,
                airNormalRateAr = airRate,
                airFastRateAr = airFastRate,
                seaRateAr = seaRate,
                airNormalLeadDays = airLeadDays.ifBlank { "7 à 10 jours" },
                airFastLeadDays = airFastLeadDays.ifBlank { "3 à 5 jours" },
                seaNormalLeadDays = seaLeadDays.ifBlank { "30 à 45 jours" },
                goodsCategoryDelays = goodsCategoryDelays,
                notes = internalNotes.trim(),
                rateApplicationDate = effectiveDate
            )
        )
        return "Transitaire « $name » ajouté"
    }

    suspend fun updateCatalogueTransitaire(
        id: Long,
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String,
        chinaWarehouseAddress: String,
        isActive: Boolean,
        rateApplicationDate: String,
        airLeadDays: String,
        seaLeadDays: String,
        internalNotes: String,
        airFastRate: Long,
        airFastLeadDays: String,
        goodsCategoryDelays: String
    ): String? {
        val list = repository.getAllCatalogueTransitairesSnapshot()
        val existing = list.firstOrNull { it.id == id }
        return if (existing != null) {
            val effectiveDate = rateApplicationDate.ifBlank {
                existing.rateApplicationDate.ifBlank {
                    SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
                }
            }
            repository.updateCatalogueTransitaire(
                existing.copy(
                    name = name.trim(),
                    contact = contact.trim(),
                    address = chinaWarehouseAddress.trim(),
                    defaultTariffPerKg = airRate,
                    airNormalRateAr = airRate,
                    airFastRateAr = airFastRate,
                    seaRateAr = seaRate,
                    airNormalLeadDays = airLeadDays.ifBlank { "7 à 10 jours" },
                    airFastLeadDays = airFastLeadDays.ifBlank { "3 à 5 jours" },
                    seaNormalLeadDays = seaLeadDays.ifBlank { "30 à 45 jours" },
                    goodsCategoryDelays = goodsCategoryDelays,
                    notes = internalNotes.trim(),
                    rateApplicationDate = effectiveDate
                )
            )
            "Transitaire « $name » modifié"
        } else null
    }

    suspend fun deleteCatalogueTransitaire(id: Long): String {
        repository.deleteCatalogueTransitaire(id)
        return "Transitaire supprimé"
    }

    suspend fun updateTransitaireRates(id: Long, airRate: Long, seaRate: Long, date: String? = null): String? {
        val list = repository.getAllCatalogueTransitairesSnapshot()
        val existing = list.firstOrNull { it.id == id }
        return if (existing != null) {
            val appDate = if (!date.isNullOrBlank()) date else SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date())
            repository.updateCatalogueTransitaire(
                existing.copy(
                    defaultTariffPerKg = airRate,
                    airNormalRateAr = airRate,
                    seaRateAr = seaRate,
                    rateApplicationDate = appDate
                )
            )
            "Tarifs mis à jour"
        } else null
    }

    suspend fun toggleTransitaireArchived(id: Long): String? {
        val list = repository.getAllCatalogueTransitairesSnapshot()
        val existing = list.firstOrNull { it.id == id }
        return if (existing != null) {
            val isCurrentlyArchived = existing.notes.startsWith("[ARCHIVED]")
            val newNotes = if (isCurrentlyArchived) {
                existing.notes.removePrefix("[ARCHIVED]").trim()
            } else {
                "[ARCHIVED] ${existing.notes}".trim()
            }
            repository.updateCatalogueTransitaire(existing.copy(notes = newNotes))
            if (isCurrentlyArchived) "Transitaire réactivé" else "Transitaire archivé"
        } else null
    }

    suspend fun recordTransitairePayment(
        id: Long,
        amountAr: Long,
        accountName: String = ""
    ): String {
        if (amountAr <= 0L) return "Montant de règlement invalide"
        val ariaryAccounts = repository.getBusinessAccountsSnapshot()
            .filter { !it.isYuanAccount && it.isActive }
        if (ariaryAccounts.isEmpty()) {
            return "Paiement bloqué : veuillez d'abord créer un compte Ariary (Espèces, Mvola...)."
        }
        val cleanAccount = accountName.trim()
        val matchedAccount = if (cleanAccount.isNotBlank()) {
            ariaryAccounts.firstOrNull { it.name.equals(cleanAccount, ignoreCase = true) }
                ?: return "Compte « $cleanAccount » introuvable. Choisissez un compte Ariary actif."
        } else {
            ariaryAccounts.first()
        }

        val list = repository.getAllCatalogueTransitairesSnapshot()
        val transName = list.firstOrNull { it.id == id }?.name ?: "Transitaire"

        repository.runInTransaction {
            val existingTxs = repository.allBusinessTransactions.firstOrNull() ?: emptyList()
            val paidBeforeViaTx = existingTxs
                .filter { it.referenceTag == "TRANSITAIRE_$id" && it.type == "EXPENSE" }
                .sumOf { it.amountAr }

            val allOrders = repository.businessOrders.firstOrNull() ?: emptyList()
            val transOrders = allOrders
                .filter { it.carrier.contains(transName, ignoreCase = true) && it.freightCostAr > 0L }
                .sortedBy { it.orderDate }
            val alreadyPaidOrdersFreight = transOrders.filter { it.isFreightPaid }.sumOf { it.freightCostAr }

            repository.insertBusinessTransaction(
                BusinessTransactionEntity(
                    title = "Règlement Fret — $transName",
                    type = "EXPENSE",
                    amountAr = amountAr,
                    accountName = matchedAccount.name,
                    referenceTag = "TRANSITAIRE_$id",
                    dateMillis = System.currentTimeMillis()
                )
            )

            var surplusToAllocate = if (paidBeforeViaTx >= alreadyPaidOrdersFreight) {
                (paidBeforeViaTx - alreadyPaidOrdersFreight) + amountAr
            } else {
                amountAr
            }
            for (ord in transOrders.filter { !it.isFreightPaid }) {
                if (surplusToAllocate >= ord.freightCostAr) {
                    repository.updateBusinessOrder(ord.copy(isFreightPaid = true))
                    surplusToAllocate -= ord.freightCostAr
                } else {
                    break
                }
            }
        }

        return "Règlement transitaire de ${CurrencyFormatter.formatAriary(amountAr)} débité de ${matchedAccount.name}"
    }

    suspend fun payOrderFreight(
        order: com.example.data.local.entity.BusinessOrderEntity,
        amountAr: Long,
        accountName: String = ""
    ): String {
        val effectiveAmount = if (amountAr > 0L) amountAr else order.freightCostAr
        if (order.isFreightPaid) return "Cette facture fret est déjà réglée"
        if (effectiveAmount <= 0L) return "Montant de fret invalide"

        val ariaryAccounts = repository.getBusinessAccountsSnapshot()
            .filter { !it.isYuanAccount && it.isActive }
        if (ariaryAccounts.isEmpty()) {
            return "Paiement bloqué : veuillez d'abord créer un compte Ariary (Espèces, Mvola...)."
        }
        val cleanAccount = accountName.trim()
        val matchedAccount = if (cleanAccount.isNotBlank()) {
            ariaryAccounts.firstOrNull { it.name.equals(cleanAccount, ignoreCase = true) }
                ?: return "Compte « $cleanAccount » introuvable."
        } else {
            ariaryAccounts.first()
        }

        val transitaires = repository.getAllCatalogueTransitairesSnapshot()
        val matchedTransitaire = if (order.carrier.isNotBlank()) {
            transitaires.firstOrNull {
                it.name.equals(order.carrier, ignoreCase = true) ||
                    order.carrier.contains(it.name, ignoreCase = true)
            }
        } else null

        val refTag = if (matchedTransitaire != null) {
            "TRANSITAIRE_${matchedTransitaire.id}"
        } else {
            "TRANSITAIRE_LOT_${order.id}"
        }
        val carrierLabel = order.carrier.ifBlank { "Transitaire" }
        val lotRef = order.referenceCode.ifBlank { "Lot #${order.id}" }

        repository.runInTransaction {
            repository.insertBusinessTransaction(
                BusinessTransactionEntity(
                    title = "Fret $carrierLabel — ${order.product.ifBlank { lotRef }}",
                    type = "EXPENSE",
                    amountAr = effectiveAmount,
                    accountName = matchedAccount.name,
                    referenceTag = refTag,
                    dateMillis = System.currentTimeMillis()
                )
            )
            repository.updateBusinessOrder(
                order.copy(
                    freightCostAr = effectiveAmount,
                    isFreightPaid = true
                )
            )
        }
        return "Fret de ${CurrencyFormatter.formatAriary(effectiveAmount)} réglé via ${matchedAccount.name}"
    }
}
