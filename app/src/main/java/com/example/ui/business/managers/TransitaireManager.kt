package com.example.ui.business.managers

import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.CatalogueTransitaireEntity
import com.example.data.repository.AppRepository
import com.example.util.CurrencyFormatter
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

    suspend fun recordTransitairePayment(id: Long, amountAr: Long): String {
        val list = repository.getAllCatalogueTransitairesSnapshot()
        val transName = list.firstOrNull { it.id == id }?.name ?: "Transitaire"
        repository.insertBusinessTransaction(
            BusinessTransactionEntity(
                title = "Règlement Fret — $transName",
                type = "EXPENSE",
                amountAr = amountAr,
                accountName = "Business",
                referenceTag = "TRANSITAIRE_$id",
                dateMillis = System.currentTimeMillis()
            )
        )
        return "Règlement transitaire de ${CurrencyFormatter.formatAriary(amountAr)} enregistré"
    }
}
