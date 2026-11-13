package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_orders")
data class BusinessOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val product: String, // e.g. "Écouteurs TWS Pro 5"
    val supplier: String = "Guangzhou",
    val quantityBought: Int = 1,
    val priceYuan: Double = 0.0,
    val exchangeRate: Double = 650.0, // 1 Yuan = 650 Ar
    val resalePriceAr: Long = 0L,
    val status: String = "ORDERED", // DRAFT, ORDERED, WAREHOUSE, GUANGZHOU, FLIGHT, CUSTOMS, QUALITY_CONTROL, DELIVERY_READY, SOLD
    val orderDate: Long = System.currentTimeMillis(),
    val estimatedArrival: Long = System.currentTimeMillis() + 6 * 86400000L,
    val soldDate: Long? = null,
    val category: String = "BUSINESS", // "BUSINESS", "PERSONAL", "FAMILY_FRIENDS"
    val carrier: String = "", // e.g. "Vol #AF882 · MadaAir", "Transitaire SinoExpress"
    val variants: String = "", // e.g. "10 Noir, 5 Blanc, 5 Gris"
    val freightCostAr: Long = 0L, // Frais de fret associés
    val isFreightPaid: Boolean = true, // false si frais de fret en attente
    val freightDueDate: Long? = null, // Date d'échéance de paiement fret
    val imageUrl: String = "",
    val stockCount: Int = 0, // Stock restant disponible
    val unitsSold: Int = 0, // Nombre vendu ce mois
    val marginPercent: Int = 40, // Marge commerciale en %
    val restockAlert: Boolean = false, // Vrai si alerte "À racheter"
    val referenceCode: String = "", // e.g. "TWS-P5-PRO"
    val platform: String = "1688", // "1688", "Taobao", "Pinduoduo"
    val weightKg: Double? = null, // e.g. 3.2 kg
    val lotNumber: String = "", // e.g. "Lot #592"
    val assignedClient: String = "", // e.g. "Rina"
    val family: String = "Électronique", // e.g. "Électronique", "Maison", "Mode"
    val reservedCount: Int = 0, // Pièces réservées clients
    val defectiveCount: Int = 0, // Pièces défectueuses / SAV
    val isHidden: Boolean = false, // true si archivé / masqué
    val isDemo: Boolean = false,
    val freightMode: String = "AÉRIEN", // "AÉRIEN" ou "MARITIME"
    val volumeM3: Double? = null, // Volume en m³ (ex: 0.014 m³)
    val receptionNotes: String = "", // Notes d'inspection / livraison
    val trackingNumbers: String = "", // Numéros de suivi séparés par des virgules
    val deliveredTrackingNumbers: String = "", // Numéros de suivi réceptionnés à l'entrepôt
    val receivedParcelsCount: Int = 0, // Nombre de colis reçus à Madagascar
    val receivedUnitsCount: Int = 0, // Nombre d'unités reçues lors de la réception
    val incidentReport: String = "", // Signalement d'incident / retard
    val inspectionResults: String = "", // Résumé du contrôle qualité
    val yuanDebited: Boolean = false, // Vrai si le paiement en Yuans a déjà été effectué
    val yuanDebitedAmount: Double = 0.0, // Montant en Yuans effectivement débité
    val customsTaxAr: Long = 0L, // Frais de douane / TVA à l'importation
    val refundYuan: Double = 0.0, // Remboursement usine en Yuans
    val lostCount: Int = 0, // Nombre de pièces perdues / manquantes
    val unitCostAr: Long = 0L, // Coût unitaire réel FIFO calculé à la clôture/réception
    val domesticShippingYuan: Double = 0.0, // Frais de port interne Chine en Yuans
    val familyId: Long = 0L, // ID unique de la famille dans CatalogueFamilyEntity
    val supplierDispatchDelay: String = "", // Délai d'expédition usine choisi
    val parcelDetailsJson: String = "" // Métadonnées JSON des colis (transporteur, contenu, étapes, horodatage)
)
