package com.example.ui.business

import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.YuanTransactionEntity

data class BusinessPipelineStep(
    val stepIndex: Int,
    val count: Int,
    val title: String,
    val subtitle: String
)

data class TopProductUi(
    val rank: Int,
    val name: String,
    val unitsSold: Int,
    val marginPercent: Int,
    val stockCount: Int,
    val stockStatusLabel: String,
    val isRestockNeeded: Boolean
)

data class IncomingParcelUi(
    val id: Long,
    val productName: String,
    val variants: String,
    val carrier: String,
    val delayBadge: String,
    val isCustoms: Boolean,
    val iconCategory: String
)

data class FamilyFilterUi(
    val name: String,
    val count: Int
)

data class CatalogueFamilySummaryUi(
    val id: Long = 0,
    val name: String,
    val emoji: String = "🎧",
    val colorHex: String = "#06B6D4",
    val articleCount: Int = 0,
    val activeCount: Int = 0,
    val hiddenCount: Int = 0,
    val totalStock: Int = 0,
    val stockValueAr: Long = 0L,
    val avgMarginPercent: Int = 40
)

data class CataloguePlatformSummaryUi(
    val id: Long = 0L,
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
    val articleCount: Int = 0,
    val totalLots: Int = 0,
    val totalSpentYuan: Double = 0.0,
    val totalSpentAr: Long = 0L,
    val lastOrderDate: String = "",
    val transitNotes: String = "",
    val hasTransitDetails: Boolean = false
)

data class CatalogueTransitaireSummaryUi(
    val id: Long = 0L,
    val name: String,
    val initials: String,
    val routeHub: String,
    val airRateAr: Long,
    val seaRateAr: Long,
    val airRateTrend: String = "",
    val seaRateTrend: String = "",
    val isAirCheapest: Boolean = false,
    val balanceDueAr: Long = 0L,
    val balanceDueDate: String = "",
    val activeShipmentsCount: Int = 0,
    val lastShipmentDate: String = "",
    val isActive: Boolean = true,
    val avatarType: String = "GRADIENT", // "GRADIENT", "CYAN", "VIOLET", "GRAY"
    val isArchived: Boolean = false,
    val contact: String = "",
    val chinaWarehouseAddress: String = "",
    val rateApplicationDate: String = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date()),
    val airLeadDays: String = "5 à 8 jours",
    val seaLeadDays: String = "30 à 45 jours",
    val internalNotes: String = "",
    val wechat: String = "",
    val whatsapp: String = "",
    val paidAmountAr: Long = 0L,
    val totalInvoicedAr: Long = 0L,
    val effectiveRateAr: Long = 0L,
    val lastShipmentCode: String = ""
)

data class BusinessUiState(
    val isLoading: Boolean = true,
    val businessAccounts: List<com.example.data.local.entity.BusinessAccountEntity> = emptyList(),
    val selectedContext: String = "Business", // "Business" ou "Tout"
    val selectedSubTab: String = "Aperçu", // "Aperçu", "Achats", "Stock", "Ventes", "Clients"
    val purchasesCount: Int = 0,
    val stockCount: Int = 0,
    val selectedMonthLabel: String = "",
    val monthOffset: Int = 0,

    // Navigation & écran Catalogue
    val isCatalogueOpen: Boolean = false,
    val catalogueSubTab: String = "Articles", // "Articles", "Familles", "Plateformes"
    val catalogueSearchQuery: String = "",
    val catalogueFamilyFilter: String = "Toutes",
    val catalogueShowHidden: Boolean = false,
    val catalogueSort: String = "Plus vendus", // "Plus vendus", "Dernier coût", "Nom"
    val catalogueViewMode: String = "LIST", // "LIST", "GRID"
    val activeCatalogueCount: Int = 0,
    val hiddenCatalogueCount: Int = 0,
    val catalogueFamilyPills: List<FamilyFilterUi> = emptyList(),
    val visibleCatalogueOrders: List<BusinessOrderEntity> = emptyList(),
    val catalogueFamilies: List<CatalogueFamilySummaryUi> = emptyList(),
    val cataloguePlatforms: List<CataloguePlatformSummaryUi> = emptyList(),
    val catalogueTransitaires: List<CatalogueTransitaireSummaryUi> = emptyList(),

    // Hero metrics
    val netProfit: Long = 0L,
    val vsPrevMonthPercent: Int = 0,
    val salesTotal: Long = 0L,
    val salesCost: Long = 0L,
    val freightAndExpenses: Long = 0L,
    val marginSharePercent: Int = 0,
    val exchangeSharePercent: Int = 0,
    val freightSharePercent: Int = 0,

    // Portefeuille Yuans
    val availableYuans: Double = 0.0,
    val cmupRate: Long = 650L,
    val estimatedYuanValueAr: Long = 0L,
    val insufficientYuanShortage: Double? = null, // null si pas de manque

    // Alertes trésorerie
    val freightDueAmount: Long = 0L,
    val freightDueDays: Int = 0,
    val clientReceivablesAmount: Long = 0L,
    val pendingClientsCount: Int = 0,

    // Pipeline Chine -> Mada (6 étapes)
    val pipelineSteps: List<BusinessPipelineStep> = emptyList(),
    val totalPipelineCount: Int = 0,

    // Prochains arrivages
    val incomingParcels: List<IncomingParcelUi> = emptyList(),

    // Top ventes du mois
    val topProducts: List<TopProductUi> = emptyList(),

    // Répartition des achats
    val totalPurchasesAr: Long = 0L,
    val businessPurchasesAr: Long = 0L,
    val businessPurchasesPercent: Int = 0,
    val personalPurchasesAr: Long = 0L,
    val personalPurchasesPercent: Int = 0,
    val familyPurchasesAr: Long = 0L,
    val familyPurchasesPercent: Int = 0,

    // Bénéfice sur service proches
    val familyServiceProfitAr: Long = 0L,

    // Insight IA Trésorerie
    val exchangeGainAr: Long = 0L,
    val overdueInvoicesCount: Int = 0,
    val overdueInvoicesAmount: Long = 0L,
    val currentInsightIndex: Int = 1,
    val totalInsightsCount: Int = 3,

    // Filtres & métriques Achats
    val purchasesStatusFilter: String = "Tous",
    val purchasesCategoryFilter: String = "Toutes catégories",
    val purchasesSortFilter: String = "Date récent",
    val activeLotsCount: Int = 0,
    val draftActionsCount: Int = 0,
    val inRouteAmountAr: Long = 0L,
    val inRouteYuans: Double = 0.0,
    val filteredPurchases: List<BusinessOrderEntity> = emptyList(),

    // Métriques & Filtres Stock (Module Business Stock)
    val stockSegment: String = "Disponible", // "Disponible", "En route", "Défectueux"
    val stockSearchQuery: String = "",
    val stockFamilyFilter: String = "Toutes",
    val showHiddenStock: Boolean = false,
    val hiddenStockCount: Int = 0,
    val totalStockValueAr: Long = 0L,
    val availableStockPieces: Int = 0,
    val reservedClientsPieces: Int = 0,
    val inRouteStockUnits: Int = 0,
    val defectiveStockUnits: Int = 0,
    val stockFamiliesCount: Int = 0,
    val stockFamilyPills: List<FamilyFilterUi> = emptyList(),
    val visibleStockOrders: List<BusinessOrderEntity> = emptyList(),
    val inRouteStockOrders: List<BusinessOrderEntity> = emptyList(),
    val defectiveStockOrders: List<BusinessOrderEntity> = emptyList(),

    // Métriques & Filtres Ventes (Module Business Ventes)
    val salesDomain: String = "BUSINESS", // "BUSINESS" ("Business Pro") ou "FAMILY_FRIENDS" ("Famille & Amis")
    val salesStatusFilter: String = "Toutes", // "Toutes", "Impayées", "Partielles", "Réglées", "Sur commande", "Sur stock"
    val salesSearchQuery: String = "",
    val salesTotalCollectedAr: Long = 0L,
    val salesNetProfitAr: Long = 0L,
    val salesGrowthVsPrevMonthPercent: Int = 0,
    val salesRemainingToCollectAr: Long = 0L,
    val salesOpenInvoicesCount: Int = 0,
    val salesPendingOrdersCount: Int = 0,
    val salesProductMarginPercent: Int = 0,
    val salesExchangeGainPercent: Int = 0,
    val salesFreightMarginPercent: Int = 0,
    val visibleSales: List<BusinessSaleEntity> = emptyList(),
    val pendingCustomerOrders: List<BusinessOrderEntity> = emptyList(),

    // Métriques & Filtres Clients (Module Business Clients)
    val clientsFilter: String = "all", // "all", "due", "business", "famille"
    val clientsSearchQuery: String = "",
    val activeClientsCount: Int = 0,
    val totalDueClientsAr: Long = 0L,
    val overdueClientsCount: Int = 0,
    val topClientName: String = "-",
    val topClientTotalAr: Long = 0L,
    val clientsList: List<BusinessClientUi> = emptyList(),

    // Listes brutes
    val orders: List<BusinessOrderEntity> = emptyList(),
    val yuanTransactions: List<YuanTransactionEntity> = emptyList(),
    val sales: List<BusinessSaleEntity> = emptyList(),
    val snackbarMessage: String? = null
)

data class BusinessClientUi(
    val id: Long = 0L,
    val name: String,
    val initials: String,
    val category: String = "Business", // "Business" ou "Famille & amis"
    val totalPurchasesAr: Long = 0L,
    val totalOrdersCount: Int = 1,
    val balanceDueAr: Long = 0L,
    val statusLabel: String = "À jour ✓",
    val statusTag: String = "Dernier achat: Hier",
    val isDueUrgent: Boolean = false,
    val lastPurchaseDate: String = "Hier",
    val phone: String = "",
    val totalPaidAr: Long = 0L,
    val avatarType: String = "VIOLET", // "VIOLET", "CYAN", "GRAY", "RED", "INDIGO"
    val suggestedRelanceMsg: String = ""
)
