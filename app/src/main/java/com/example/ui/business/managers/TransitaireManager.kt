package com.example.ui.business.managers

import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.CatalogueTransitaireEntity
import com.example.data.local.entity.TransitaireRateHistoryEntity
import com.example.data.repository.AppRepository
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import kotlinx.coroutines.flow.firstOrNull

class TransitaireManager(private val repository: AppRepository) {

    private fun todayDateStr(): String = DateUtils.getTodayFrenchDate()

    private fun buildHistoryEntry(
        transitaireId: Long,
        transitaireName: String,
        mode: String,
        oldRateAr: Long,
        newRateAr: Long,
        dateLabel: String,
        isInitial: Boolean = false
    ): TransitaireRateHistoryEntity {
        val resolvedDateLabel = dateLabel.trim().ifBlank { todayDateStr() }
        val resolvedMillis = DateUtils.frenchDateToEpochMillis(resolvedDateLabel)
        val deltaAr = if (!isInitial && oldRateAr > 0L && newRateAr > 0L) newRateAr - oldRateAr else 0L
        val deltaPct = if (!isInitial && oldRateAr > 0L && newRateAr > 0L) {
            ((newRateAr - oldRateAr).toDouble() / oldRateAr.toDouble()) * 100.0
        } else 0.0
        val note = when {
            isInitial -> "Tarif initial"
            oldRateAr == 0L && newRateAr > 0L -> "Tarif défini"
            oldRateAr > 0L && newRateAr == 0L -> "Tarif retiré"
            deltaAr > 0L -> "Hausse tarifaire"
            deltaAr < 0L -> "Baisse tarifaire"
            else -> "Révision tarifaire"
        }
        return TransitaireRateHistoryEntity(
            transitaireId = transitaireId,
            transitaireName = transitaireName,
            mode = mode,
            oldRateAr = oldRateAr,
            newRateAr = newRateAr,
            deltaAr = deltaAr,
            deltaPercent = deltaPct,
            changedAtMillis = resolvedMillis,
            effectiveDateLabel = resolvedDateLabel,
            note = note,
            isDemo = false
        )
    }

    suspend fun addCatalogueTransitaire(
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String = "",
        chinaWarehouseAddress: String = "",
        isActive: Boolean = true,
        rateApplicationDate: String = "",
        airLeadDays: String = "",
        seaLeadDays: String = "",
        internalNotes: String = "",
        airFastRate: Long = 0L,
        airFastLeadDays: String = "",
        goodsCategoryDelays: String = ""
    ): String {
        val cleanName = name.trim()
        val hasAnyRate = airRate > 0L || airFastRate > 0L || seaRate > 0L
        val effectiveDate = when {
            !hasAnyRate -> ""
            rateApplicationDate.isNotBlank() -> rateApplicationDate.trim()
            else -> todayDateStr()
        }
        val finalNotes = if (!isActive) "[ARCHIVED] ${internalNotes.trim()}".trim() else internalNotes.trim()

        repository.runInTransaction {
            val newId = repository.insertCatalogueTransitaire(
                CatalogueTransitaireEntity(
                    name = cleanName,
                    contact = contact.trim(),
                    address = chinaWarehouseAddress.trim(),
                    defaultTariffPerKg = airRate.coerceAtLeast(0L),
                    airNormalRateAr = airRate.coerceAtLeast(0L),
                    airFastRateAr = airFastRate.coerceAtLeast(0L),
                    seaRateAr = seaRate.coerceAtLeast(0L),
                    airNormalLeadDays = airLeadDays.trim(),
                    airFastLeadDays = airFastLeadDays.trim(),
                    seaNormalLeadDays = seaLeadDays.trim(),
                    goodsCategoryDelays = goodsCategoryDelays,
                    notes = finalNotes,
                    rateApplicationDate = effectiveDate
                )
            )
            val initialLogs = mutableListOf<TransitaireRateHistoryEntity>()
            if (airRate > 0L) {
                initialLogs.add(buildHistoryEntry(newId, cleanName, "AERIEN", 0L, airRate, effectiveDate, isInitial = true))
            }
            if (airFastRate > 0L) {
                initialLogs.add(buildHistoryEntry(newId, cleanName, "AERIEN_FAST", 0L, airFastRate, effectiveDate, isInitial = true))
            }
            if (seaRate > 0L) {
                initialLogs.add(buildHistoryEntry(newId, cleanName, "MARITIME", 0L, seaRate, effectiveDate, isInitial = true))
            }
            if (initialLogs.isNotEmpty()) {
                repository.insertTransitaireRateHistories(initialLogs)
            }
        }
        return "Transitaire « $cleanName » ajouté"
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
        val cleanName = name.trim()
        var updated = false
        repository.runInTransaction {
            val list = repository.getAllCatalogueTransitairesSnapshot()
            val existing = list.firstOrNull { it.id == id } ?: return@runInTransaction

            val sanitizedAir = airRate.coerceAtLeast(0L)
            val sanitizedAirFast = airFastRate.coerceAtLeast(0L)
            val sanitizedSea = seaRate.coerceAtLeast(0L)

            val airChanged = existing.airNormalRateAr != sanitizedAir
            val fastChanged = existing.airFastRateAr != sanitizedAirFast
            val seaChanged = existing.seaRateAr != sanitizedSea
            val hasAnyNewRate = sanitizedAir > 0L || sanitizedAirFast > 0L || sanitizedSea > 0L
            val today = todayDateStr()

            val effectiveDate = when {
                !hasAnyNewRate -> ""
                rateApplicationDate.isNotBlank() -> rateApplicationDate.trim()
                else -> existing.rateApplicationDate.ifBlank { today }
            }

            val cleanNotes = internalNotes.removePrefix("[ARCHIVED]").trim()
            val finalNotes = if (!isActive) "[ARCHIVED] $cleanNotes".trim() else cleanNotes

            repository.updateCatalogueTransitaire(
                existing.copy(
                    name = cleanName,
                    contact = contact.trim(),
                    address = chinaWarehouseAddress.trim(),
                    defaultTariffPerKg = sanitizedAir,
                    airNormalRateAr = sanitizedAir,
                    airFastRateAr = sanitizedAirFast,
                    seaRateAr = sanitizedSea,
                    airNormalLeadDays = airLeadDays.trim(),
                    airFastLeadDays = airFastLeadDays.trim(),
                    seaNormalLeadDays = seaLeadDays.trim(),
                    goodsCategoryDelays = goodsCategoryDelays,
                    notes = finalNotes,
                    rateApplicationDate = effectiveDate
                )
            )

            val dateLabel = effectiveDate.ifBlank { today }
            val changeLogs = mutableListOf<TransitaireRateHistoryEntity>()
            if (airChanged && (existing.airNormalRateAr > 0L || sanitizedAir > 0L)) {
                changeLogs.add(buildHistoryEntry(id, cleanName, "AERIEN", existing.airNormalRateAr, sanitizedAir, dateLabel))
            }
            if (fastChanged && (existing.airFastRateAr > 0L || sanitizedAirFast > 0L)) {
                changeLogs.add(buildHistoryEntry(id, cleanName, "AERIEN_FAST", existing.airFastRateAr, sanitizedAirFast, dateLabel))
            }
            if (seaChanged && (existing.seaRateAr > 0L || sanitizedSea > 0L)) {
                changeLogs.add(buildHistoryEntry(id, cleanName, "MARITIME", existing.seaRateAr, sanitizedSea, dateLabel))
            }
            if (changeLogs.isNotEmpty()) {
                repository.insertTransitaireRateHistories(changeLogs)
            }
            updated = true
        }
        return if (updated) "Transitaire « $cleanName » modifié" else null
    }

    suspend fun deleteCatalogueTransitaire(id: Long): String {
        repository.runInTransaction {
            repository.deleteTransitaireRateHistoryFor(id)
            repository.deleteCatalogueTransitaire(id)
        }
        return "Transitaire supprimé"
    }

    suspend fun updateTransitaireRates(id: Long, airRate: Long, seaRate: Long, date: String? = null): String? {
        var updated = false
        repository.runInTransaction {
            val list = repository.getAllCatalogueTransitairesSnapshot()
            val existing = list.firstOrNull { it.id == id } ?: return@runInTransaction

            val sanitizedAir = airRate.coerceAtLeast(0L)
            val sanitizedSea = seaRate.coerceAtLeast(0L)
            val hasAnyRate = sanitizedAir > 0L || existing.airFastRateAr > 0L || sanitizedSea > 0L
            val appDate = when {
                !hasAnyRate -> ""
                !date.isNullOrBlank() -> date.trim()
                else -> todayDateStr()
            }

            repository.updateCatalogueTransitaire(
                existing.copy(
                    defaultTariffPerKg = sanitizedAir,
                    airNormalRateAr = sanitizedAir,
                    seaRateAr = sanitizedSea,
                    rateApplicationDate = appDate
                )
            )

            val dateLabel = appDate.ifBlank { todayDateStr() }
            val logs = mutableListOf<TransitaireRateHistoryEntity>()
            if (existing.airNormalRateAr != sanitizedAir && (existing.airNormalRateAr > 0L || sanitizedAir > 0L)) {
                logs.add(buildHistoryEntry(id, existing.name, "AERIEN", existing.airNormalRateAr, sanitizedAir, dateLabel))
            }
            if (existing.seaRateAr != sanitizedSea && (existing.seaRateAr > 0L || sanitizedSea > 0L)) {
                logs.add(buildHistoryEntry(id, existing.name, "MARITIME", existing.seaRateAr, sanitizedSea, dateLabel))
            }
            if (logs.isNotEmpty()) {
                repository.insertTransitaireRateHistories(logs)
            }
            updated = true
        }
        return if (updated) "Tarifs mis à jour" else null
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
