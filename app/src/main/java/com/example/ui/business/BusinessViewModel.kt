package com.example.ui.business

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.CatalogueFamilyEntity
import com.example.data.local.entity.CataloguePlatformEntity
import com.example.data.local.entity.CatalogueTransitaireEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.SalePaymentEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.util.CurrencyFormatter

private data class BusinessRawData(
    val orders: List<BusinessOrderEntity>,
    val yuanTxs: List<YuanTransactionEntity>,
    val sales: List<BusinessSaleEntity>,
    val families: List<CatalogueFamilyEntity>,
    val platforms: List<CataloguePlatformEntity>,
    val clients: List<ClientEntity>,
    val transitaires: List<CatalogueTransitaireEntity>,
    val businessTransactions: List<com.example.data.local.entity.BusinessTransactionEntity> = emptyList(),
    val salePayments: List<com.example.data.local.entity.SalePaymentEntity> = emptyList()
)

private data class PurchasesFilters(
    val status: String,
    val category: String,
    val sort: String
)

private data class StockFilters(
    val segment: String,
    val searchQuery: String,
    val family: String,
    val showHidden: Boolean
)

private data class SalesFilters(
    val domain: String,
    val status: String,
    val searchQuery: String
)

private data class CatalogueFilters(
    val isOpen: Boolean,
    val subTab: String,
    val searchQuery: String,
    val family: String,
    val showHidden: Boolean,
    val sort: String,
    val viewMode: String
)

private data class BusinessControlState(
    val contextChoice: String,
    val subTab: String,
    val monthOffset: Int,
    val insightIndex: Int,
    val snackbarMsg: String?
)

class BusinessViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val settingsDataStore = SettingsDataStore(application)
    private val repository = AppRepository(application, db, settingsDataStore)

    private val selectedContextFlow = MutableStateFlow("Business")
    private val selectedSubTabFlow = MutableStateFlow("Aperçu")
    private val monthOffsetFlow = MutableStateFlow(0)
    private val insightIndexFlow = MutableStateFlow(1)
    private val snackbarFlow = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.seedDefaultCatalogueTablesIfEmpty()
        }
    }

    // Filtres Achats
    private val purchasesStatusFilterFlow = MutableStateFlow("Tous")
    private val purchasesCategoryFilterFlow = MutableStateFlow("Toutes catégories")
    private val purchasesSortFilterFlow = MutableStateFlow("Date récent")

    // Filtres Stock
    private val stockSegmentFlow = MutableStateFlow("Disponible")
    private val stockSearchQueryFlow = MutableStateFlow("")
    private val stockFamilyFilterFlow = MutableStateFlow("Toutes")
    private val showHiddenStockFlow = MutableStateFlow(false)

    // Filtres Ventes
    private val salesDomainFlow = MutableStateFlow("BUSINESS") // "BUSINESS" ou "FAMILY_FRIENDS"
    private val salesStatusFilterFlow = MutableStateFlow("Toutes")
    private val salesSearchQueryFlow = MutableStateFlow("")

    // État & Filtres Catalogue
    private val isCatalogueOpenFlow = MutableStateFlow(false)
    private val catalogueSubTabFlow = MutableStateFlow("Articles") // "Articles", "Familles", "Plateformes"
    private val catalogueSearchQueryFlow = MutableStateFlow("")
    private val catalogueFamilyFilterFlow = MutableStateFlow("Toutes")
    private val catalogueShowHiddenFlow = MutableStateFlow(false)
    private val catalogueSortFlow = MutableStateFlow("Plus vendus") // "Plus vendus", "Dernier coût", "Nom"
    private val catalogueViewModeFlow = MutableStateFlow("LIST") // "LIST" ou "GRID"

    private val clientsFilterFlow = MutableStateFlow("all")
    private val clientsSearchQueryFlow = MutableStateFlow("")

    private val rawDataFlow = combine(
        repository.businessOrders,
        repository.yuanTransactions,
        repository.businessSales,
        combine(repository.catalogueFamilies, repository.cataloguePlatforms, repository.salePayments) { f, p, sp -> Triple(f, p, sp) },
        combine(repository.clients, repository.catalogueTransitaires, repository.allBusinessTransactions) { c, t, tx -> Triple(c, t, tx) }
    ) { orders, yuanTxs, sales, famPlatPay, clientTransTx ->
        BusinessRawData(
            orders,
            yuanTxs,
            sales,
            famPlatPay.first,
            famPlatPay.second,
            clientTransTx.first,
            clientTransTx.second,
            clientTransTx.third,
            famPlatPay.third
        )
    }

    private val purchasesFiltersFlow = combine(
        purchasesStatusFilterFlow,
        purchasesCategoryFilterFlow,
        purchasesSortFilterFlow
    ) { st, cat, sort ->
        PurchasesFilters(st, cat, sort)
    }

    private val stockFiltersFlow = combine(
        stockSegmentFlow,
        stockSearchQueryFlow,
        stockFamilyFilterFlow,
        showHiddenStockFlow
    ) { seg, query, fam, hidden ->
        StockFilters(seg, query, fam, hidden)
    }

    private val salesFiltersFlow = combine(
        salesDomainFlow,
        salesStatusFilterFlow,
        salesSearchQueryFlow
    ) { domain, status, query ->
        SalesFilters(domain, status, query)
    }

    private val cataloguePart1Flow = combine(
        isCatalogueOpenFlow,
        catalogueSubTabFlow,
        catalogueSearchQueryFlow,
        catalogueFamilyFilterFlow
    ) { isOpen, subTab, query, fam ->
        listOf(isOpen.toString(), subTab, query, fam)
    }

    private val cataloguePart2Flow = combine(
        catalogueShowHiddenFlow,
        catalogueSortFlow,
        catalogueViewModeFlow
    ) { hidden, sort, mode ->
        listOf(hidden.toString(), sort, mode)
    }

    private val catalogueFiltersFlow = combine(
        cataloguePart1Flow,
        cataloguePart2Flow
    ) { p1, p2 ->
        CatalogueFilters(
            isOpen = p1[0].toBoolean(),
            subTab = p1[1],
            searchQuery = p1[2],
            family = p1[3],
            showHidden = p2[0].toBoolean(),
            sort = p2[1],
            viewMode = p2[2]
        )
    }

    private data class ClientsFiltersState(
        val filter: String,
        val searchQuery: String
    )

    private val clientsFiltersFlow = combine(
        clientsFilterFlow,
        clientsSearchQueryFlow
    ) { filter, query ->
        ClientsFiltersState(filter, query)
    }

    private val secondaryFiltersFlow = combine(
        salesFiltersFlow,
        catalogueFiltersFlow,
        clientsFiltersFlow
    ) { salesF, catF, clientsF ->
        Triple(salesF, catF, clientsF)
    }

    private val controlStateFlow = combine(
        selectedContextFlow,
        selectedSubTabFlow,
        monthOffsetFlow,
        insightIndexFlow,
        snackbarFlow
    ) { ctx, tab, offset, insightIdx, snackbar ->
        BusinessControlState(ctx, tab, offset, insightIdx, snackbar)
    }

    val uiState: StateFlow<BusinessUiState> = combine(
        rawDataFlow,
        controlStateFlow,
        purchasesFiltersFlow,
        stockFiltersFlow,
        secondaryFiltersFlow
    ) { raw, ctrl, pFilters, sFilters, secondary ->
        val salesFilters = secondary.first
        val cFilters = secondary.second
        val clientsFilters = secondary.third

        val orders = raw.orders
        val yuanTxs = raw.yuanTxs
        val sales = raw.sales

        // 1. Date label & Monthly filtering
        val refDate = LocalDate.now().withDayOfMonth(1).plusMonths(ctrl.monthOffset.toLong())
        val monthLabel = refDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRENCH) else it.toString() }

        val startOfMonthMs = refDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonthMs = refDate.plusMonths(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val monthSales = sales.filter { it.saleDate in startOfMonthMs..endOfMonthMs }
        val monthOrders = orders.filter { it.orderDate in startOfMonthMs..endOfMonthMs }

        // 2. Portefeuille Yuans
        val buyTxs = yuanTxs.filter { it.type == "BUY" }
        val refundTxs = yuanTxs.filter { it.type == "REFUND" }
        val spendTxs = yuanTxs.filter { it.type == "SPEND" }
        val totalBoughtYuan = buyTxs.sumOf { it.amountYuan }
        val totalRefundedYuan = refundTxs.sumOf { it.amountYuan }
        val totalSpentYuan = spendTxs.sumOf { it.amountYuan }
        val totalCostAr = buyTxs.sumOf { it.amountAr }

        val availableYuans = totalBoughtYuan + totalRefundedYuan - totalSpentYuan
        val cmupRate = if (totalBoughtYuan > 0.0) (totalCostAr / totalBoughtYuan).toLong() else 650L
        val estimatedYuanValue = (availableYuans.coerceAtLeast(0.0) * cmupRate).toLong()

        // Alerte yuans insuffisants si brouillon présent
        val draftOrders = orders.filter { it.status == "DRAFT" }
        val draftYuansNeeded = draftOrders.sumOf { it.priceYuan * it.quantityBought }
        val insufficientShortage = if (draftYuansNeeded > availableYuans) (draftYuansNeeded - availableYuans) else null

        // 3. Ventes & Bénéfice du mois (Aperçu global filtré par mois réel d'encaissement)
        val salesById = sales.associateBy { it.id }
        val paymentsBySaleId = raw.salePayments.groupBy { it.saleId }

        fun computeCashInAndCost(startMs: Long, endMs: Long): Pair<Long, Long> {
            val monthPayments = raw.salePayments.filter { it.paymentDate in startMs..endMs }
            val legacySales = sales.filter {
                it.saleDate in startMs..endMs && it.paidAmountAr > 0 && paymentsBySaleId[it.id].isNullOrEmpty()
            }
            val total = monthPayments.sumOf { it.amountAr } + legacySales.sumOf { it.paidAmountAr }
            val cost = monthPayments.sumOf { p ->
                val s = salesById[p.saleId]
                if (s != null && s.totalPriceAr > 0) {
                    (s.unitCostAr * s.quantity * (p.amountAr.toDouble() / s.totalPriceAr.toDouble())).toLong()
                } else 0L
            } + legacySales.sumOf {
                (it.unitCostAr * it.quantity * (if (it.totalPriceAr > 0) it.paidAmountAr.toDouble() / it.totalPriceAr.toDouble() else 1.0)).toLong()
            }
            return Pair(total, cost)
        }

        val (salesTotal, salesCost) = computeCashInAndCost(startOfMonthMs, endOfMonthMs)
        val freightAndExpenses = monthOrders.sumOf { it.freightCostAr }
        val netProfit = salesTotal - salesCost - freightAndExpenses

        val prevRefDate = refDate.minusMonths(1)
        val startOfPrevMonthMs = prevRefDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfPrevMonthMs = prevRefDate.plusMonths(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val prevMonthOrders = orders.filter { it.orderDate in startOfPrevMonthMs..endOfPrevMonthMs }
        val (prevSalesTotal, prevSalesCost) = computeCashInAndCost(startOfPrevMonthMs, endOfPrevMonthMs)
        val prevFreight = prevMonthOrders.sumOf { it.freightCostAr }
        val prevNetProfit = prevSalesTotal - prevSalesCost - prevFreight
        val vsPrevMonthPercent = if (prevNetProfit != 0L) {
            (((netProfit - prevNetProfit).toDouble() / kotlin.math.abs(prevNetProfit.toDouble())) * 100).toInt()
        } else 0

        // 4. Alertes trésorerie
        val unpaidFreightOrders = orders.filter { !it.isFreightPaid }
        val freightDueAmount = unpaidFreightOrders.sumOf { it.freightCostAr }
        val unpaidSales = sales.filter { !it.isFullyPaid }
        val clientReceivables = unpaidSales.sumOf { it.totalPriceAr - it.paidAmountAr }
        val pendingClientsCount = unpaidSales.map { it.clientName }.filter { it.isNotBlank() }.distinct().size

        // 5. Pipeline Chine -> Mada (6 étapes)
        val countOrdered = orders.count { it.status == "ORDERED" }
        val countWarehouse = orders.count { it.status == "WAREHOUSE" }
        val countGuangzhou = orders.count { it.status == "GUANGZHOU" }
        val countFlight = orders.count { it.status == "FLIGHT" }
        val countCustoms = orders.count { it.status in listOf("CUSTOMS", "QUALITY_CONTROL") }
        val countControl = orders.count { it.status == "DELIVERY_READY" }

        val pipelineSteps = listOf(
            BusinessPipelineStep(1, countOrdered, "Commandé", "1688 / Taobao"),
            BusinessPipelineStep(2, countWarehouse, "Vers entrepôt", "Colis interne"),
            BusinessPipelineStep(3, countGuangzhou, "Guangzhou", "Pesée faite"),
            BusinessPipelineStep(4, countFlight, "Vol / Fret", "En vol"),
            BusinessPipelineStep(5, countCustoms, "Arrivé Mada", "Dédouanement"),
            BusinessPipelineStep(6, countControl, "Contrôle", "Prêt à livrer")
        )
        val totalPipeline = countOrdered + countWarehouse + countGuangzhou + countFlight + countCustoms + countControl

        // 6. Prochains arrivages
        val transitOrders = orders.filter { it.status in listOf("FLIGHT", "CUSTOMS", "QUALITY_CONTROL", "GUANGZHOU") }
        val incomingList = transitOrders.take(3).map { o ->
            IncomingParcelUi(
                id = o.id,
                productName = o.product,
                variants = if (o.variants.isNotBlank()) o.variants else "Quantité : ${o.quantityBought}",
                carrier = if (o.carrier.isNotBlank()) o.carrier else "Transitaire MadaAir",
                delayBadge = when (o.status) {
                    "CUSTOMS" -> "En douane"
                    "QUALITY_CONTROL" -> "Contrôle qualité"
                    "GUANGZHOU" -> "Pesée faite"
                    else -> "Dans ${((o.estimatedArrival - System.currentTimeMillis()) / 86400000L).coerceIn(1, 14)} jours"
                },
                isCustoms = o.status in listOf("CUSTOMS", "QUALITY_CONTROL"),
                iconCategory = if (o.imageUrl.isNotBlank()) o.imageUrl else "earbuds"
            )
        }

        // 7. Top Ventes du mois sélectionné
        val topProducts = monthSales
            .groupBy { it.productName.trim() }
            .map { (prodName, salesList) ->
                val soldQty = salesList.sumOf { it.quantity }
                val matchingOrder = orders.firstOrNull { it.product.trim().equals(prodName, ignoreCase = true) }
                Triple(prodName, soldQty, matchingOrder)
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(3)
            .mapIndexed { idx, (prodName, soldQty, matchingOrder) ->
                val stock = matchingOrder?.stockCount ?: 0
                val restock = matchingOrder?.restockAlert ?: false
                TopProductUi(
                    rank = idx + 1,
                    name = prodName,
                    unitsSold = soldQty,
                    marginPercent = matchingOrder?.marginPercent ?: 0,
                    stockCount = stock,
                    stockStatusLabel = when {
                        restock -> "À racheter"
                        else -> "$stock en stock"
                    },
                    isRestockNeeded = restock
                )
            }

        // 8. Répartition des achats (excluant les fiches simples du catalogue)
        val actualPurchases = orders.filter { it.status != "CATALOGUE" }
        val totalPurchasesAr = actualPurchases.sumOf { if (it.unitCostAr > 0L) it.unitCostAr * it.quantityBought else ((it.priceYuan * it.exchangeRate).toLong() * it.quantityBought + it.freightCostAr + it.customsTaxAr) }
        val businessPurchasesAr = actualPurchases.filter { it.category == "BUSINESS" }.sumOf { if (it.unitCostAr > 0L) it.unitCostAr * it.quantityBought else ((it.priceYuan * it.exchangeRate).toLong() * it.quantityBought + it.freightCostAr + it.customsTaxAr) }
        val personalPurchasesAr = actualPurchases.filter { it.category == "PERSONAL" }.sumOf { if (it.unitCostAr > 0L) it.unitCostAr * it.quantityBought else ((it.priceYuan * it.exchangeRate).toLong() * it.quantityBought + it.freightCostAr + it.customsTaxAr) }
        val familyPurchasesAr = actualPurchases.filter { it.category == "FAMILY_FRIENDS" }.sumOf { if (it.unitCostAr > 0L) it.unitCostAr * it.quantityBought else ((it.priceYuan * it.exchangeRate).toLong() * it.quantityBought + it.freightCostAr + it.customsTaxAr) }

        val totalPurchasesSafe = totalPurchasesAr.coerceAtLeast(1L)
        val bizPct = ((businessPurchasesAr.toDouble() / totalPurchasesSafe.toDouble()) * 100).toInt()
        val persPct = ((personalPurchasesAr.toDouble() / totalPurchasesSafe.toDouble()) * 100).toInt()
        val famPct = ((familyPurchasesAr.toDouble() / totalPurchasesSafe.toDouble()) * 100).toInt()

        // 9. Insight IA Trésorerie & Créances
        val overdue15d = unpaidSales.filter { (System.currentTimeMillis() - it.saleDate) > 15 * 86400000L }
        val overdueAmount = overdue15d.sumOf { it.totalPriceAr - it.paidAmountAr }
        val overdueCount = overdue15d.size

        // 10. Calculs spécifiques Achats
        val activeLots = orders.count { it.status != "CATALOGUE" && it.status != "DRAFT" && it.status != "SOLD" && it.status != "COMPLETED" }
        val draftActions = orders.count { it.status == "DRAFT" }

        val inRouteOrders = orders.filter { it.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL") }
        val inRouteAmountAr = inRouteOrders.sumOf { if (it.unitCostAr > 0L) it.unitCostAr * it.quantityBought else ((it.priceYuan * it.exchangeRate).toLong() * it.quantityBought + it.freightCostAr) }
        val inRouteYuans = inRouteOrders.sumOf { it.priceYuan * it.quantityBought }

        val exchangeGainAr = buyTxs.sumOf { (650.0 - it.exchangeRate) * it.amountYuan }.toLong().coerceAtLeast(0L)

        // Filtrage des achats (exclut les articles purement catalogue sans achat actif)
        val filteredPurchasesList = actualPurchases.filter { order ->
            val matchStatus = when (pFilters.status) {
                "Tous" -> true
                "Brouillon" -> order.status == "DRAFT"
                "Commandé" -> order.status == "ORDERED"
                "En livraison" -> order.status == "WAREHOUSE"
                "Arrivé entrepôt" -> order.status == "GUANGZHOU"
                "Vers Mada" -> order.status == "FLIGHT"
                "Contrôle qualité" -> order.status == "QUALITY_CONTROL" || order.status == "CUSTOMS"
                "Terminé" -> order.status in listOf("DELIVERY_READY", "IN_STOCK", "COMPLETED", "SOLD")
                else -> true
            }

            val matchCategory = when (pFilters.category) {
                "Toutes catégories" -> true
                "Business" -> order.category == "BUSINESS"
                "Pour moi" -> order.category == "PERSONAL"
                "Famille & amis" -> order.category == "FAMILY_FRIENDS"
                else -> true
            }

            matchStatus && matchCategory
        }.let { list ->
            when (pFilters.sort) {
                "Date récent" -> list.sortedByDescending { it.orderDate }
                "Prix ¥ décroissant" -> list.sortedByDescending { it.priceYuan }
                "Prix Ar décroissant" -> list.sortedByDescending { it.resalePriceAr }
                else -> list
            }
        }

        // 11. CALCULS DYNAMIQUES DU MODULE STOCK
        val allStockReadyOrders = orders.filter { it.stockCount > 0 }
        val hiddenStockCount = orders.count { it.isHidden }

        val visibleInStock = allStockReadyOrders.filter { !it.isHidden || sFilters.showHidden }
        val availableStockPieces = visibleInStock.sumOf { it.stockCount }
        val reservedClientsPieces = visibleInStock.sumOf { it.reservedCount }

        val totalStockValueAr = visibleInStock.sumOf { order ->
            val unitFifo = if (order.unitCostAr > 0L) {
                order.unitCostAr
            } else if (order.priceYuan > 0.0) {
                (order.priceYuan * order.exchangeRate).toLong() + (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
            } else {
                0L
            }
            unitFifo * order.stockCount
        }

        val inRouteStockOrders = orders.filter { it.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS") }
        val inRouteStockUnits = inRouteStockOrders.sumOf { it.quantityBought }

        val defectiveStockOrders = orders.filter { it.defectiveCount > 0 && (!it.isHidden || sFilters.showHidden) }
        val defectiveStockUnits = defectiveStockOrders.sumOf { it.defectiveCount }

        val distinctFamilies = visibleInStock.map { it.family.ifBlank { "Électronique" } }.distinct().sorted()
        val stockFamiliesCount = distinctFamilies.size

        val familyPills = mutableListOf<FamilyFilterUi>()
        familyPills.add(FamilyFilterUi("Toutes", availableStockPieces))
        distinctFamilies.forEach { fam ->
            val countForFam = visibleInStock.filter { it.family.equals(fam, ignoreCase = true) }.sumOf { it.stockCount }
            familyPills.add(FamilyFilterUi(fam, countForFam))
        }

        val currentTabItems = when (sFilters.segment) {
            "Disponible" -> visibleInStock
            "En route" -> inRouteStockOrders
            "Défectueux" -> defectiveStockOrders
            else -> visibleInStock
        }

        val filteredStockOrders = currentTabItems.filter { order ->
            val matchSearch = if (sFilters.searchQuery.isBlank()) true else {
                order.product.contains(sFilters.searchQuery, ignoreCase = true) ||
                order.referenceCode.contains(sFilters.searchQuery, ignoreCase = true) ||
                order.variants.contains(sFilters.searchQuery, ignoreCase = true) ||
                order.carrier.contains(sFilters.searchQuery, ignoreCase = true)
            }

            val orderFam = order.family.ifBlank { "Électronique" }
            val matchFamily = if (sFilters.family == "Toutes" || sFilters.segment != "Disponible") true else {
                orderFam.equals(sFilters.family, ignoreCase = true) ||
                sFilters.family.contains(orderFam, ignoreCase = true) ||
                (orderFam.length >= 4 && sFilters.family.contains(orderFam.take(4), ignoreCase = true))
            }

            matchSearch && matchFamily
        }

        // 12. CALCULS DYNAMIQUES DU MODULE VENTES
        val domainSales = sales.filter { it.domain == salesFilters.domain }
        val salesTotalCollectedAr = domainSales.sumOf { it.paidAmountAr }
        val salesRemainingToCollectAr = domainSales.sumOf { it.totalPriceAr - it.paidAmountAr }
        val salesOpenInvoicesCount = domainSales.count { !it.isFullyPaid }

        val salesNetProfitAr = domainSales.sumOf { sale ->
            val totalProfit = (sale.totalPriceAr - (sale.unitCostAr * sale.quantity))
            if (sale.totalPriceAr > 0) {
                ((totalProfit.toDouble() * sale.paidAmountAr.toDouble()) / sale.totalPriceAr.toDouble()).toLong()
            } else {
                0L
            }
        }

        val pendingCustomerOrders = orders.filter { it.assignedClient.isNotBlank() }
        val salesPendingOrdersCount = pendingCustomerOrders.size

        val filteredSales = domainSales.filter { sale ->
            val matchStatus = when (salesFilters.status) {
                "Toutes" -> true
                "Impayées" -> sale.paidAmountAr == 0L && !sale.isFullyPaid
                "Partielles" -> sale.paidAmountAr > 0L && !sale.isFullyPaid
                "Réglées" -> sale.isFullyPaid
                "Sur commande" -> sale.saleSource == "ORDER"
                "Sur stock" -> sale.saleSource == "STOCK"
                else -> true
            }

            val matchSearch = if (salesFilters.searchQuery.isBlank()) true else {
                sale.clientName.contains(salesFilters.searchQuery, ignoreCase = true) ||
                sale.productName.contains(salesFilters.searchQuery, ignoreCase = true) ||
                sale.invoiceNumber.contains(salesFilters.searchQuery, ignoreCase = true)
            }

            matchStatus && matchSearch
        }

        // ==========================================
        // 13. CALCULS DYNAMIQUES DU MODULE CATALOGUE
        // ==========================================
        val activeCatalogueCount = orders.count { !it.isHidden }
        val hiddenCatalogueCount = orders.count { it.isHidden }

        // Puces de familles pour le catalogue
        val defaultFamilies = listOf(
            CatalogueFamilyEntity(id = 1, name = "Électronique & Audio", emoji = "🎧", colorHex = "#06B6D4", displayOrder = 1),
            CatalogueFamilyEntity(id = 2, name = "Mode & Maroquinerie", emoji = "🎒", colorHex = "#EC4899", displayOrder = 2),
            CatalogueFamilyEntity(id = 3, name = "Maison & Rangement", emoji = "🏠", colorHex = "#F97316", displayOrder = 3),
            CatalogueFamilyEntity(id = 4, name = "Accessoires Mobiles", emoji = "📱", colorHex = "#8B5CF6", displayOrder = 4)
        )
        val availableFamilies = if (raw.families.isNotEmpty()) raw.families else defaultFamilies

        val catalogueFamilyPills = mutableListOf<FamilyFilterUi>()
        catalogueFamilyPills.add(FamilyFilterUi("Toutes", activeCatalogueCount))
        availableFamilies.forEach { fam ->
            val countForFam = orders.filter { o ->
                !o.isHidden && (
                    o.family.equals(fam.name, ignoreCase = true) ||
                    fam.name.contains(o.family, ignoreCase = true) ||
                    (o.family.length >= 4 && fam.name.contains(o.family.take(4), ignoreCase = true))
                )
            }.size
            catalogueFamilyPills.add(FamilyFilterUi(fam.name, countForFam))
        }

        // Filtrage des articles du catalogue
        val catalogueBaseList = if (cFilters.showHidden) orders else orders.filter { !it.isHidden }

        val filteredCatalogueOrders = catalogueBaseList.filter { order ->
            val matchSearch = if (cFilters.searchQuery.isBlank()) true else {
                order.product.contains(cFilters.searchQuery, ignoreCase = true) ||
                order.referenceCode.contains(cFilters.searchQuery, ignoreCase = true) ||
                order.variants.contains(cFilters.searchQuery, ignoreCase = true) ||
                order.family.contains(cFilters.searchQuery, ignoreCase = true)
            }

            val matchFamily = if (cFilters.family == "Toutes") true else {
                order.family.equals(cFilters.family, ignoreCase = true) ||
                cFilters.family.contains(order.family, ignoreCase = true) ||
                (order.family.length >= 4 && cFilters.family.contains(order.family.take(4), ignoreCase = true))
            }

            matchSearch && matchFamily
        }.let { list ->
            when (cFilters.sort) {
                "Plus vendus" -> list.sortedByDescending { it.unitsSold }
                "Dernier coût" -> list.sortedByDescending { if (it.unitCostAr > 0L) it.unitCostAr else (it.priceYuan * it.exchangeRate).toLong() }
                "Nom" -> list.sortedBy { it.product.lowercase() }
                else -> list.sortedByDescending { it.unitsSold }
            }
        }

        // Résumés sous-onglets Familles (dynamiques depuis Room)
        val catalogueFamiliesSummary = availableFamilies.map { fam ->
            val famOrders = orders.filter { o ->
                val of = o.family.trim()
                val fn = fam.name.trim()
                of.equals(fn, ignoreCase = true) ||
                fn.contains(of, ignoreCase = true) ||
                (of.length >= 4 && fn.contains(of.take(4), ignoreCase = true))
            }
            val totalStock = famOrders.sumOf { it.stockCount }
            val activeCount = famOrders.count { !it.isHidden }
            val hiddenCount = famOrders.count { it.isHidden }
            val stockVal = famOrders.sumOf { order ->
                val unitFifo = if (order.unitCostAr > 0L) {
                    order.unitCostAr
                } else if (order.priceYuan > 0.0) {
                    (order.priceYuan * order.exchangeRate).toLong() + (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
                } else {
                    0L
                }
                unitFifo * order.stockCount
            }
            val finalStockVal = stockVal
            val avgMargin = if (famOrders.isNotEmpty()) famOrders.map { it.marginPercent }.average().toInt() else 0

            CatalogueFamilySummaryUi(
                id = fam.id,
                name = fam.name,
                emoji = fam.emoji,
                colorHex = fam.colorHex,
                articleCount = famOrders.size,
                activeCount = activeCount,
                hiddenCount = hiddenCount,
                totalStock = totalStock,
                stockValueAr = finalStockVal,
                avgMarginPercent = avgMargin
            )
        }

        // Résumés sous-onglets Plateformes (dynamiques depuis Room)
        val defaultPlatforms = listOf(
            CataloguePlatformEntity(id = 1, name = "1688.com", aliasKey = "1688", subtitle = "Alibaba Wholesale (Usines B2B)", isVerified = true, iconType = "TEXT_1688", colorHex = "#712ae2", shippingTag = "Port Payant (Sur devis)", shippingTagIcon = "local_shipping", secondaryTag = "Min. commande : 2-5 pcs", isActive = true, displayOrder = 1, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Tarif transitaire aérien: 45 000 Ar/kg. Maritime: 18 000 Ar/kg."),
            CataloguePlatformEntity(id = 2, name = "Taobao", aliasKey = "Taobao", subtitle = "Consommation & Petits lots", badgeText = "C2C / B2C", isVerified = false, iconType = "CART", colorHex = "#8a4cfc", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Livraison rapide état côtier", isActive = true, displayOrder = 2, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Livraison rapide Guangdong en 48h."),
            CataloguePlatformEntity(id = 3, name = "Pinduoduo", aliasKey = "Pinduoduo", subtitle = "Direct Usine / Déstockage & Volume", badgeText = "Usine Direct", isVerified = false, iconType = "PRICE", colorHex = "#ffdad6", shippingTag = "Port Gratuit (包邮)", shippingTagIcon = "check_circle", secondaryTag = "Remises groupées", isActive = true, displayOrder = 3, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Commandes groupées direct usines."),
            CataloguePlatformEntity(id = 4, name = "Fournisseur Direct WeChat", aliasKey = "WeChat", subtitle = "Sino-Contact / Négociations privées", isVerified = false, iconType = "WECHAT", colorHex = "#acedff", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "request_quote", secondaryTag = "Dépôt direct transitaire", isActive = true, displayOrder = 4, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Paiement direct WeChat Pay ou virement agent."),
            CataloguePlatformEntity(id = 5, name = "Xianyu (闲鱼)", aliasKey = "Xianyu", subtitle = "B2C / C2C Occasion & Pièces rares", badgeText = "Seconde main", isVerified = false, iconType = "CART", colorHex = "#FFC107", shippingTag = "Variable (Sur devis usine)", shippingTagIcon = "local_shipping", secondaryTag = "Négociation vendeur", isActive = true, displayOrder = 5, defaultYuanSpent = 0.0, defaultOrdersCount = 0, defaultLastDate = "", transitNotes = "Transactions de seconde main et pièces uniques directes de particuliers ou usines.")
        )
        val availablePlatforms = if (raw.platforms.isNotEmpty()) raw.platforms else defaultPlatforms

        val cataloguePlatformsSummary = availablePlatforms.map { plat ->
            val platOrders = orders.filter { o ->
                val op = o.platform.trim()
                val os = o.supplier.trim()
                val pn = plat.name.trim()
                val pa = plat.aliasKey.trim()
                op.equals(pn, ignoreCase = true) ||
                (pa.isNotBlank() && op.contains(pa, ignoreCase = true)) ||
                (pa.isNotBlank() && os.contains(pa, ignoreCase = true)) ||
                os.contains(pn, ignoreCase = true) ||
                pn.contains(op, ignoreCase = true)
            }
            val hasOrders = platOrders.isNotEmpty()
            val totalLots = platOrders.size
            val totalSpentYuan = if (hasOrders) platOrders.sumOf { it.priceYuan * it.quantityBought } else 0.0
            val totalSpentAr = if (hasOrders) platOrders.sumOf { it.resalePriceAr.coerceAtLeast((it.priceYuan * it.quantityBought * it.exchangeRate).toLong()) } else 0L
            val lastDate = if (hasOrders) {
                val latest = platOrders.maxByOrNull { it.orderDate }
                if (latest != null) {
                    val dt = java.time.Instant.ofEpochMilli(latest.orderDate).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH))
                } else "Aucune commande"
            } else {
                "Aucune commande"
            }

            CataloguePlatformSummaryUi(
                id = plat.id,
                name = plat.name,
                aliasKey = plat.aliasKey,
                subtitle = plat.subtitle,
                badgeText = plat.badgeText,
                isVerified = plat.isVerified,
                iconType = plat.iconType,
                colorHex = plat.colorHex,
                shippingTag = plat.shippingTag,
                shippingTagIcon = plat.shippingTagIcon,
                secondaryTag = plat.secondaryTag,
                isActive = plat.isActive,
                articleCount = platOrders.size,
                totalLots = totalLots,
                totalSpentYuan = totalSpentYuan,
                totalSpentAr = totalSpentAr,
                lastOrderDate = lastDate,
                transitNotes = plat.transitNotes,
                hasTransitDetails = plat.transitNotes.isNotBlank()
            )
        }

        // Transitaires issus de Room
        val dateFmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE)
        val catalogueTransitairesSummary = raw.transitaires.map { t ->
            val transOrders = orders.filter { it.carrier.contains(t.name, ignoreCase = true) }
            val inTransitCount = transOrders.count { it.status in listOf("GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL") }
            val inits = t.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "TR" }

            val totalInvoiced = transOrders.sumOf { it.freightCostAr }
            val paidAmount = raw.businessTransactions
                .filter { it.referenceTag == "TRANSITAIRE_${t.id}" && it.type == "EXPENSE" }
                .sumOf { it.amountAr }
            val balanceDue = (totalInvoiced - paidAmount).coerceAtLeast(0L)
            val totalWeight = transOrders.sumOf { it.weightKg ?: 0.0 }
            val effectiveRate = if (totalWeight > 0.0 && totalInvoiced > 0L) (totalInvoiced / totalWeight).toLong() else 0L
            val latestOrder = transOrders.maxByOrNull { it.orderDate }
            val lastDate = latestOrder?.let { dateFmt.format(java.util.Date(it.orderDate)) } ?: ""
            val lastCode = latestOrder?.referenceCode?.ifBlank { "#CMD-${latestOrder.id}" } ?: ""

            CatalogueTransitaireSummaryUi(
                id = t.id,
                name = t.name,
                initials = inits,
                routeHub = "Hub Chine → Madagascar",
                airRateAr = if (t.airNormalRateAr > 0) t.airNormalRateAr else (if (t.defaultTariffPerKg > 0) t.defaultTariffPerKg else 45000L),
                airFastRateAr = if (t.airFastRateAr > 0) t.airFastRateAr else 60000L,
                seaRateAr = if (t.seaRateAr > 0) t.seaRateAr else 18000L,
                contact = t.contact,
                chinaWarehouseAddress = t.address,
                internalNotes = t.notes.removePrefix("[ARCHIVED]").trim(),
                isArchived = t.notes.startsWith("[ARCHIVED]"),
                activeShipmentsCount = inTransitCount,
                lastShipmentDate = lastDate,
                lastShipmentCode = lastCode,
                totalInvoicedAr = totalInvoiced,
                paidAmountAr = paidAmount,
                balanceDueAr = balanceDue,
                effectiveRateAr = effectiveRate,
                airLeadDays = t.airNormalLeadDays.ifBlank { "7 à 10 jours" },
                airFastLeadDays = t.airFastLeadDays.ifBlank { "3 à 5 jours" },
                seaLeadDays = t.seaNormalLeadDays.ifBlank { "30 à 45 jours" },
                goodsCategoryDelays = t.goodsCategoryDelays.ifBlank { "Générale: Normal | Batterie: +2j | Poudre/Liquide: +3j | Téléphone: +2j" }
            )
        }

        // Clients issus de Room (croisés avec les ventes BusinessSaleEntity)
        val clientsFromRoom = raw.clients.map { client ->
            val clientSales = sales.filter {
                it.clientId == client.id || it.clientName.trim().equals(client.name.trim(), ignoreCase = true)
            }
            val totalPurchases = clientSales.sumOf { it.totalPriceAr }
            val totalPaid = clientSales.sumOf { it.paidAmountAr }
            val due = (totalPurchases - totalPaid).coerceAtLeast(0L)
            val phone = if (client.phone.isNotBlank()) client.phone else (clientSales.firstOrNull { it.clientPhone.isNotBlank() }?.clientPhone ?: "")
            val inits = client.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "CL" }
            BusinessClientUi(
                id = client.id,
                name = client.name,
                initials = inits,
                category = client.category,
                totalPurchasesAr = totalPurchases,
                totalOrdersCount = clientSales.size,
                balanceDueAr = due,
                statusLabel = if (due == 0L) "À jour ✓" else "Reste : ${CurrencyFormatter.formatAriary(due)}",
                statusTag = if (due > 0L) "Relance due" else "À jour",
                isDueUrgent = due > 0L,
                lastPurchaseDate = if (clientSales.isNotEmpty()) "Récent" else "Nouveau",
                phone = phone,
                totalPaidAr = totalPaid,
                avatarType = client.avatarType,
                suggestedRelanceMsg = if (due > 0L) "Bonjour ${client.name}, rappel amical de ton solde de ${CurrencyFormatter.formatAriary(due)} sur tes achats." else ""
            )
        }

        // Clients auto-détectés depuis les ventes n'ayant pas encore de fiche client
        val clientsFromSalesOnly = sales.filter { it.clientName.isNotBlank() }
            .filter { s -> clientsFromRoom.none { it.name.trim().equals(s.clientName.trim(), ignoreCase = true) } }
            .groupBy { it.clientName.trim().lowercase() }
            .values
            .mapIndexed { idx, sGroup ->
                val firstSale = sGroup.first()
                val cName = firstSale.clientName.trim()
                val totalPurchases = sGroup.sumOf { it.totalPriceAr }
                val totalPaid = sGroup.sumOf { it.paidAmountAr }
                val due = (totalPurchases - totalPaid).coerceAtLeast(0L)
                val phone = sGroup.firstOrNull { it.clientPhone.isNotBlank() }?.clientPhone ?: ""
                val category = if (firstSale.domain == "BUSINESS") "Business" else "Famille & amis"
                val inits = cName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "CL" }
                BusinessClientUi(
                    id = (idx + 10000).toLong(),
                    name = cName,
                    initials = inits,
                    category = category,
                    totalPurchasesAr = totalPurchases,
                    totalOrdersCount = sGroup.size,
                    balanceDueAr = due,
                    statusLabel = if (due == 0L) "À jour ✓" else "Reste : ${CurrencyFormatter.formatAriary(due)}",
                    statusTag = if (due > 0L) "Relance due" else "À jour",
                    isDueUrgent = due > 0L,
                    lastPurchaseDate = "Récent",
                    phone = phone,
                    totalPaidAr = totalPaid,
                    avatarType = if (category == "Business") "CYAN" else "VIOLET",
                    suggestedRelanceMsg = if (due > 0L) "Bonjour $cName, rappel amical de ton solde de ${CurrencyFormatter.formatAriary(due)} sur tes achats." else ""
                )
            }

        val mergedClients = clientsFromRoom + clientsFromSalesOnly

        val filteredClients = mergedClients.filter { c ->
            val matchFilter = when (clientsFilters.filter) {
                "due" -> c.balanceDueAr > 0
                "upToDate" -> c.balanceDueAr == 0L
                "business" -> c.category == "Business"
                "family" -> c.category == "Famille & amis"
                else -> true
            }
            val matchSearch = if (clientsFilters.searchQuery.isBlank()) true else {
                c.name.contains(clientsFilters.searchQuery, ignoreCase = true) ||
                c.phone.contains(clientsFilters.searchQuery, ignoreCase = true)
            }
            matchFilter && matchSearch
        }

        val nowMs = System.currentTimeMillis()
        val minFreightDueDate = unpaidFreightOrders.mapNotNull { it.freightDueDate }.minOrNull()
        val freightDueDays = if (minFreightDueDate != null) {
            (((minFreightDueDate - nowMs) / 86400000L).toInt()).coerceAtLeast(0)
        } else if (unpaidFreightOrders.isNotEmpty()) {
            3
        } else {
            0
        }

        val totalGainsAr = (salesNetProfitAr + exchangeGainAr + freightAndExpenses).coerceAtLeast(1L)
        val prodMarginPct = if (salesNetProfitAr > 0L) (((salesNetProfitAr.toDouble() / totalGainsAr.toDouble()) * 100).toInt()).coerceIn(0, 100) else 0
        val exchGainPct = if (exchangeGainAr > 0L) (((exchangeGainAr.toDouble() / totalGainsAr.toDouble()) * 100).toInt()).coerceIn(0, 100) else 0
        val freightPct = if (totalGainsAr > 1L) (100 - prodMarginPct - exchGainPct).coerceAtLeast(0) else 0

        BusinessUiState(
            isLoading = false,
            selectedContext = ctrl.contextChoice,
            selectedSubTab = ctrl.subTab,
            purchasesCount = orders.size,
            stockCount = availableStockPieces,
            selectedMonthLabel = monthLabel,
            monthOffset = ctrl.monthOffset,

            // Catalogue
            isCatalogueOpen = cFilters.isOpen,
            catalogueSubTab = cFilters.subTab,
            catalogueSearchQuery = cFilters.searchQuery,
            catalogueFamilyFilter = cFilters.family,
            catalogueShowHidden = cFilters.showHidden,
            catalogueSort = cFilters.sort,
            catalogueViewMode = cFilters.viewMode,
            activeCatalogueCount = activeCatalogueCount,
            hiddenCatalogueCount = hiddenCatalogueCount,
            catalogueFamilyPills = catalogueFamilyPills,
            visibleCatalogueOrders = filteredCatalogueOrders,
            catalogueFamilies = catalogueFamiliesSummary,
            cataloguePlatforms = cataloguePlatformsSummary,
            catalogueTransitaires = catalogueTransitairesSummary,

            // Hero
            netProfit = netProfit,
            vsPrevMonthPercent = vsPrevMonthPercent,
            salesTotal = salesTotal,
            salesCost = salesCost,
            freightAndExpenses = freightAndExpenses,
            marginSharePercent = prodMarginPct,
            exchangeSharePercent = exchGainPct,
            freightSharePercent = freightPct,
            availableYuans = availableYuans,
            cmupRate = cmupRate,
            estimatedYuanValueAr = estimatedYuanValue,
            insufficientYuanShortage = insufficientShortage,
            freightDueAmount = freightDueAmount,
            freightDueDays = freightDueDays,
            clientReceivablesAmount = clientReceivables,
            pendingClientsCount = pendingClientsCount,
            pipelineSteps = pipelineSteps,
            totalPipelineCount = totalPipeline,
            incomingParcels = incomingList,
            topProducts = topProducts,
            totalPurchasesAr = totalPurchasesAr,
            businessPurchasesAr = businessPurchasesAr,
            businessPurchasesPercent = bizPct,
            personalPurchasesAr = personalPurchasesAr,
            personalPurchasesPercent = persPct,
            familyPurchasesAr = familyPurchasesAr,
            familyPurchasesPercent = famPct,
            familyServiceProfitAr = 0L,
            exchangeGainAr = exchangeGainAr,
            overdueInvoicesCount = overdueCount,
            overdueInvoicesAmount = overdueAmount,
            currentInsightIndex = ctrl.insightIndex,
            totalInsightsCount = 3,

            // Achats view
            purchasesStatusFilter = pFilters.status,
            purchasesCategoryFilter = pFilters.category,
            purchasesSortFilter = pFilters.sort,
            activeLotsCount = activeLots,
            draftActionsCount = draftActions,
            inRouteAmountAr = inRouteAmountAr,
            inRouteYuans = inRouteYuans,
            filteredPurchases = filteredPurchasesList,

            // Stock view
            stockSegment = sFilters.segment,
            stockSearchQuery = sFilters.searchQuery,
            stockFamilyFilter = sFilters.family,
            showHiddenStock = sFilters.showHidden,
            hiddenStockCount = hiddenStockCount,
            totalStockValueAr = totalStockValueAr,
            availableStockPieces = availableStockPieces,
            reservedClientsPieces = reservedClientsPieces,
            inRouteStockUnits = inRouteStockUnits,
            defectiveStockUnits = defectiveStockUnits,
            stockFamiliesCount = stockFamiliesCount,
            stockFamilyPills = familyPills,
            visibleStockOrders = filteredStockOrders,
            inRouteStockOrders = inRouteStockOrders,
            defectiveStockOrders = defectiveStockOrders,

            // Ventes view
            salesDomain = salesFilters.domain,
            salesStatusFilter = salesFilters.status,
            salesSearchQuery = salesFilters.searchQuery,
            salesTotalCollectedAr = salesTotalCollectedAr,
            salesNetProfitAr = salesNetProfitAr,
            salesGrowthVsPrevMonthPercent = 0,
            salesRemainingToCollectAr = salesRemainingToCollectAr,
            salesOpenInvoicesCount = salesOpenInvoicesCount,
            salesPendingOrdersCount = salesPendingOrdersCount,
            salesProductMarginPercent = prodMarginPct,
            salesExchangeGainPercent = exchGainPct,
            salesFreightMarginPercent = freightPct,
            visibleSales = filteredSales,
            pendingCustomerOrders = pendingCustomerOrders,

            // Clients view
            clientsFilter = clientsFilters.filter,
            clientsSearchQuery = clientsFilters.searchQuery,
            activeClientsCount = mergedClients.size,
            totalDueClientsAr = mergedClients.sumOf { it.balanceDueAr },
            overdueClientsCount = mergedClients.count { it.balanceDueAr > 0 },
            topClientName = mergedClients.maxByOrNull { it.totalPurchasesAr }?.name?.split(" ")?.firstOrNull() ?: "-",
            topClientTotalAr = mergedClients.maxByOrNull { it.totalPurchasesAr }?.totalPurchasesAr ?: 0L,
            clientsList = filteredClients,

            orders = orders,
            yuanTransactions = yuanTxs,
            sales = sales,
            snackbarMessage = ctrl.snackbarMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BusinessUiState()
    )

    fun onSelectContext(choice: String) {
        selectedContextFlow.value = choice
    }

    fun onSelectSubTab(tab: String) {
        selectedSubTabFlow.value = tab
    }

    fun onPrevMonth() {
        monthOffsetFlow.value -= 1
    }

    fun onNextMonth() {
        monthOffsetFlow.value += 1
    }

    fun onResetCurrentMonth() {
        monthOffsetFlow.value = 0
    }

    fun onNextInsight() {
        insightIndexFlow.value = if (insightIndexFlow.value >= 3) 1 else insightIndexFlow.value + 1
    }

    // ==========================================
    // GESTION DU CATALOGUE
    // ==========================================
    fun openCatalogue(showHidden: Boolean = false) {
        catalogueShowHiddenFlow.value = showHidden
        isCatalogueOpenFlow.value = true
    }

    fun closeCatalogue() {
        isCatalogueOpenFlow.value = false
    }

    fun onCatalogueSubTabSelected(tab: String) {
        catalogueSubTabFlow.value = tab
    }

    fun onCatalogueSearchQueryChanged(query: String) {
        catalogueSearchQueryFlow.value = query
    }

    fun onCatalogueFamilyFilterSelected(family: String) {
        catalogueFamilyFilterFlow.value = family
    }

    fun onToggleCatalogueShowHidden() {
        catalogueShowHiddenFlow.value = !catalogueShowHiddenFlow.value
    }

    fun onCatalogueSortChanged(sort: String) {
        catalogueSortFlow.value = sort
    }

    fun onCatalogueViewModeChanged(mode: String) {
        catalogueViewModeFlow.value = mode
    }

    fun addCatalogueArticle(
        product: String,
        family: String,
        referenceCode: String,
        variants: String,
        priceYuan: Double,
        resaleAr: Long,
        stockCount: Int,
        weightKg: Double = 0.2,
        platform: String = "1688"
    ) {
        viewModelScope.launch {
            val sku = referenceCode.ifBlank {
                val cleanPrefix = product.take(4).uppercase().replace(" ", "")
                "$cleanPrefix-${(100..999).random()}"
            }
            repository.insertBusinessOrder(
                BusinessOrderEntity(
                    product = product,
                    supplier = platform,
                    platform = platform,
                    referenceCode = sku,
                    quantityBought = stockCount,
                    priceYuan = priceYuan,
                    exchangeRate = uiState.value.cmupRate.toDouble(),
                    resalePriceAr = resaleAr,
                    stockCount = stockCount,
                    weightKg = weightKg,
                    status = if (stockCount > 0) "DELIVERY_READY" else "CATALOGUE",
                    category = "BUSINESS",
                    family = family,
                    carrier = "Transitaire MadaAir",
                    variants = variants,
                    freightCostAr = 0L,
                    isFreightPaid = true,
                    unitsSold = 0,
                    marginPercent = if (priceYuan > 0) (((resaleAr - (priceYuan * 650.0)) / resaleAr.coerceAtLeast(1L)) * 100).toInt() else 45
                )
            )
            snackbarFlow.value = "$product ajouté au catalogue ($sku)"
        }
    }

    fun updateCatalogueArticle(
        order: BusinessOrderEntity,
        newPriceYuan: Double,
        newResaleAr: Long,
        newVariants: String,
        isHidden: Boolean
    ) {
        viewModelScope.launch {
            repository.updateBusinessOrder(
                order.copy(
                    priceYuan = newPriceYuan,
                    resalePriceAr = newResaleAr,
                    variants = newVariants,
                    isHidden = isHidden
                )
            )
            snackbarFlow.value = "${order.product} mis à jour"
        }
    }

    fun addCatalogueFamily(name: String, emoji: String = "✨", colorHex: String = "#06B6D4") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val snapshot = repository.getAllCatalogueFamiliesSnapshot()
            val nextOrder = (snapshot.maxOfOrNull { it.displayOrder } ?: 0) + 1
            repository.insertCatalogueFamily(
                CatalogueFamilyEntity(
                    name = name.trim(),
                    emoji = emoji,
                    colorHex = colorHex,
                    displayOrder = nextOrder,
                    isDemo = false
                )
            )
            snackbarFlow.value = "Famille '${name.trim()}' créée avec succès"
        }
    }

    fun deleteCatalogueFamily(family: CatalogueFamilySummaryUi, targetFamilyName: String = "Divers") {
        viewModelScope.launch {
            // Protection des données : transfert des articles associés vers la famille cible
            val allOrders = repository.businessOrders.firstOrNull() ?: emptyList()
            val toMigrate = allOrders.filter { it.family.equals(family.name, ignoreCase = true) }
            toMigrate.forEach { order ->
                repository.updateBusinessOrder(order.copy(family = targetFamilyName))
            }
            if (family.id > 0) {
                repository.deleteCatalogueFamily(family.id)
            }
            snackbarFlow.value = "Famille '${family.name}' supprimée (articles transférés vers $targetFamilyName)"
        }
    }

    // Gestion Achats
    fun onSelectPurchasesStatusFilter(filter: String) {
        purchasesStatusFilterFlow.value = filter
    }

    fun onSelectPurchasesCategoryFilter(cat: String) {
        purchasesCategoryFilterFlow.value = cat
    }

    fun onSelectPurchasesSortFilter(sort: String) {
        purchasesSortFilterFlow.value = sort
    }

    // Gestion Stock
    fun onStockSegmentSelected(segment: String) {
        stockSegmentFlow.value = segment
    }

    fun onStockSearchQueryChanged(query: String) {
        stockSearchQueryFlow.value = query
    }

    fun onStockFamilyFilterSelected(family: String) {
        stockFamilyFilterFlow.value = family
    }

    fun onToggleShowHiddenStock() {
        showHiddenStockFlow.value = !showHiddenStockFlow.value
    }

    fun adjustStock(
        order: BusinessOrderEntity,
        newStockCount: Int,
        newReservedCount: Int,
        newDefectiveCount: Int,
        assignedClient: String
    ) {
        viewModelScope.launch {
            repository.updateBusinessOrder(
                order.copy(
                    stockCount = newStockCount.coerceAtLeast(0),
                    reservedCount = newReservedCount.coerceAtLeast(0),
                    defectiveCount = newDefectiveCount.coerceAtLeast(0),
                    assignedClient = assignedClient
                )
            )
            snackbarFlow.value = "Stock mis à jour pour ${order.product}"
        }
    }

    fun toggleHideStockItem(order: BusinessOrderEntity) {
        viewModelScope.launch {
            repository.updateBusinessOrder(order.copy(isHidden = !order.isHidden))
            snackbarFlow.value = if (!order.isHidden) "${order.product} masqué" else "${order.product} réaffiché"
        }
    }

    fun deleteStockOrder(order: BusinessOrderEntity) {
        viewModelScope.launch {
            repository.deleteBusinessOrder(order.id)
            snackbarFlow.value = "${order.product} supprimé"
        }
    }

    // Gestion Ventes
    fun onSalesDomainSelected(domain: String) {
        salesDomainFlow.value = domain
    }

    fun onSalesStatusFilterSelected(filter: String) {
        salesStatusFilterFlow.value = filter
    }

    fun onSalesSearchQueryChanged(query: String) {
        salesSearchQueryFlow.value = query
    }

    fun recordSalePayment(sale: BusinessSaleEntity, additionalPaymentAr: Long, paymentMethod: String = "ESPECES") {
        viewModelScope.launch {
            if (additionalPaymentAr <= 0L) return@launch
            val remainingDue = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L)
            val appliedAmount = additionalPaymentAr.coerceAtMost(remainingDue)
            if (appliedAmount <= 0L) return@launch
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
            snackbarFlow.value = "Règlement de ${CurrencyFormatter.formatAriary(appliedAmount)} enregistré pour ${sale.clientName}"
        }
    }

    fun addNewSale(
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
    ) {
        viewModelScope.launch {
            val totalPrice = unitPriceAr * quantity
            val allSales = repository.businessSales.firstOrNull() ?: emptyList()

            // Numérotation séquentielle et unique (ex: FAC-0001, FAC-0002...)
            val maxInvoiceSeq = allSales.mapNotNull { s ->
                val digits = s.invoiceNumber.filter { it.isDigit() }
                digits.toIntOrNull()
            }.maxOrNull() ?: 0
            val nextSeq = maxInvoiceSeq + 1
            val newInvoiceNum = String.format(Locale.US, "FAC-%04d", nextSeq)

            // Trouver ou associer la fiche client
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

            // Résolution de linkedOrderId si non fourni mais produit correspond à un lot en stock
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

            // Historique de règlement
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

            // Décrémenter le stock et incrémenter les unités vendues
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

            snackbarFlow.value = "Facture $newInvoiceNum créée pour $trimmedClientName"
        }
    }

    fun deleteSale(sale: BusinessSaleEntity) {
        viewModelScope.launch {
            // Réintégration du stock si la vente était liée à une commande
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
            snackbarFlow.value = "Facture ${sale.invoiceNumber.ifBlank { "#---" }} de ${sale.clientName} annulée et stock réintégré"
        }
    }

    fun onAdvanceQualityControl(order: BusinessOrderEntity) {
        viewModelScope.launch {
            repository.updateBusinessOrder(order.copy(status = "DELIVERY_READY"))
            snackbarFlow.value = "Contrôle qualité validé pour ${order.product}"
        }
    }

    fun buyYuans(amountYuan: Double, rateAr: Double, platform: String) {
        viewModelScope.launch {
            val amountAr = (amountYuan * rateAr).toLong()
            repository.insertYuanTransaction(
                YuanTransactionEntity(
                    type = "BUY",
                    amountYuan = amountYuan,
                    exchangeRate = rateAr,
                    amountAr = amountAr,
                    platform = platform,
                    note = "Recharge $platform"
                )
            )
            snackbarFlow.value = "¥ ${amountYuan.toInt()} achetés avec succès"
        }
    }

    fun refundYuans(amountYuan: Double, rateAr: Double, platform: String = "Alipay", note: String = "", orderId: Long? = null) {
        viewModelScope.launch {
            if (amountYuan <= 0.0) return@launch
            if (orderId != null && orderId > 0L) {
                val existingTxs = repository.yuanTransactions.firstOrNull() ?: emptyList()
                val alreadyRefunded = existingTxs.any { it.orderId == orderId && it.type == "REFUND" }
                if (alreadyRefunded) {
                    snackbarFlow.value = "Remboursement déjà effectué pour ce lot"
                    return@launch
                }
            }
            val amountAr = (amountYuan * rateAr).toLong()
            val insertedId = repository.insertYuanTransaction(
                YuanTransactionEntity(
                    type = "REFUND",
                    amountYuan = amountYuan,
                    exchangeRate = rateAr,
                    amountAr = amountAr,
                    platform = platform,
                    note = note,
                    orderId = orderId
                )
            )
            if (insertedId == -1L) {
                snackbarFlow.value = "Remboursement déjà enregistré pour ce lot"
            } else {
                snackbarFlow.value = "Remboursement usine de ¥ ${amountYuan.toInt()} enregistré"
            }
        }
    }

    fun spendYuans(amountYuan: Double, rateAr: Double, platform: String, note: String, orderId: Long? = null, skipBalanceCheck: Boolean = false) {
        viewModelScope.launch {
            if (amountYuan <= 0.0) return@launch
            val currentAvailable = uiState.value.availableYuans
            if (!skipBalanceCheck && amountYuan > currentAvailable) {
                snackbarFlow.value = "Solde Alipay insuffisant (¥ ${currentAvailable.toInt()} dispo, ¥ ${amountYuan.toInt()} requis)"
                return@launch
            }
            if (orderId != null && orderId > 0L) {
                val existingTxs = repository.yuanTransactions.firstOrNull() ?: emptyList()
                val alreadyDebited = existingTxs.any { it.orderId == orderId && it.type == "SPEND" }
                if (alreadyDebited) {
                    snackbarFlow.value = "Paiement déjà effectué pour ce lot"
                    return@launch
                }
            }
            val amountAr = (amountYuan * rateAr).toLong()
            val insertedId = repository.insertYuanTransaction(
                YuanTransactionEntity(
                    type = "SPEND",
                    amountYuan = amountYuan,
                    exchangeRate = rateAr,
                    amountAr = amountAr,
                    platform = platform,
                    note = note,
                    orderId = orderId
                )
            )
            if (insertedId == -1L) {
                snackbarFlow.value = "Paiement déjà effectué pour ce lot"
            } else {
                snackbarFlow.value = "Paiement de ¥ ${amountYuan.toInt()} enregistré"
            }
        }
    }

    fun addBusinessOrder(
        product: String,
        supplier: String,
        quantity: Int,
        priceYuan: Double,
        resaleAr: Long,
        category: String,
        carrier: String,
        variants: String,
        freightAr: Long,
        platform: String = "1688",
        referenceCode: String = "",
        family: String = "Électronique"
    ) {
        viewModelScope.launch {
            repository.insertBusinessOrder(
                BusinessOrderEntity(
                    product = product,
                    supplier = supplier,
                    platform = platform,
                    referenceCode = referenceCode.ifBlank { "LOT-${System.currentTimeMillis() % 10000}" },
                    quantityBought = quantity,
                    priceYuan = priceYuan,
                    exchangeRate = uiState.value.cmupRate.toDouble(),
                    resalePriceAr = resaleAr,
                    status = "ORDERED",
                    category = category,
                    family = family,
                    carrier = carrier,
                    variants = variants,
                    freightCostAr = freightAr,
                    isFreightPaid = false
                )
            )
            snackbarFlow.value = "Nouvelle commande enregistrée"
        }
    }

    fun addBusinessSale(
        clientName: String,
        clientPhone: String,
        productName: String,
        quantity: Int,
        unitResalePriceAr: Long,
        unitCostAr: Long,
        paidAmountAr: Long
    ) {
        viewModelScope.launch {
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
            snackbarFlow.value = "Vente enregistrée pour $clientName"
        }
    }

    fun advanceOrderStatus(order: BusinessOrderEntity) {
        val nextStatus = when (order.status) {
            "DRAFT" -> "ORDERED"
            "ORDERED" -> "WAREHOUSE"
            "WAREHOUSE" -> "GUANGZHOU"
            "GUANGZHOU" -> "FLIGHT"
            "FLIGHT" -> "CUSTOMS"
            "CUSTOMS" -> "QUALITY_CONTROL"
            "QUALITY_CONTROL" -> "DELIVERY_READY"
            "DELIVERY_READY" -> "SOLD"
            else -> "SOLD"
        }
        val statusLabelFr = when (nextStatus) {
            "DRAFT" -> "Brouillon"
            "ORDERED" -> "Commandé 1688"
            "WAREHOUSE" -> "Reçu entrepôt Chine"
            "GUANGZHOU" -> "Hub Guangzhou"
            "FLIGHT" -> "En vol / Transit"
            "CUSTOMS" -> "Dédouanement Mada"
            "QUALITY_CONTROL" -> "Contrôle qualité"
            "DELIVERY_READY" -> "Prêt à livrer / En stock"
            "SOLD" -> "Vendu"
            else -> nextStatus
        }
        viewModelScope.launch {
            repository.updateBusinessOrder(order.copy(status = nextStatus))
            snackbarFlow.value = "Statut mis à jour : $statusLabelFr"
        }
    }

    fun updateBusinessOrder(order: BusinessOrderEntity) {
        viewModelScope.launch {
            repository.updateBusinessOrder(order)
            snackbarFlow.value = "${order.product} mis à jour"
        }
    }

    fun duplicateBusinessOrder(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val duplicate = order.copy(
                id = 0,
                lotNumber = "${order.lotNumber} (Copie)",
                orderDate = System.currentTimeMillis()
            )
            repository.insertBusinessOrder(duplicate)
            snackbarFlow.value = "Commande dupliquée : ${order.product}"
        }
    }

    fun togglePlatformActive(platformId: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.updatePlatformActive(platformId, isActive)
        }
    }

    fun updatePlatformShippingTag(platformId: Long, newShippingTag: String) {
        viewModelScope.launch {
            val snapshot = repository.getAllCataloguePlatformsSnapshot()
            val target = snapshot.find { it.id == platformId }
            if (target != null) {
                repository.updateCataloguePlatform(target.copy(shippingTag = newShippingTag))
                snackbarFlow.value = "Mode de port mis à jour : $newShippingTag"
            }
        }
    }

    fun addCataloguePlatform(
        name: String,
        subtitle: String,
        shippingTag: String,
        secondaryTag: String,
        iconType: String = "STORE",
        colorHex: String = "#712ae2"
    ) {
        viewModelScope.launch {
            val count = repository.getAllCataloguePlatformsSnapshot().size
            val isVerified = name.contains("1688", ignoreCase = true)
            val badge = if (name.contains("taobao", ignoreCase = true)) "C2C / B2C"
                        else if (name.contains("pinduoduo", ignoreCase = true)) "Usine Direct"
                        else ""
            val shipIcon = if (shippingTag.contains("gratuit", ignoreCase = true)) "check_circle"
                           else if (shippingTag.contains("devis", ignoreCase = true)) "request_quote"
                           else "local_shipping"
            val alias = name.split(" ").firstOrNull() ?: name

            val newPlat = CataloguePlatformEntity(
                name = name.trim(),
                aliasKey = alias,
                subtitle = subtitle.trim(),
                badgeText = badge,
                isVerified = isVerified,
                iconType = iconType,
                colorHex = colorHex,
                shippingTag = shippingTag.trim(),
                shippingTagIcon = shipIcon,
                secondaryTag = secondaryTag.trim(),
                isActive = true,
                displayOrder = count + 1,
                defaultYuanSpent = 0.0,
                defaultOrdersCount = 0,
                defaultLastDate = "Nouvelle plateforme",
                transitNotes = "Frais de port intérieurs et logistique configurable."
            )
            repository.insertCataloguePlatform(newPlat)
            snackbarFlow.value = "Plateforme « $name » ajoutée au catalogue"
        }
    }

    fun deleteCataloguePlatform(platformId: Long) {
        viewModelScope.launch {
            repository.deleteCataloguePlatform(platformId)
            snackbarFlow.value = "Plateforme supprimée"
        }
    }

    fun addCatalogueTransitaire(
        name: String,
        hub: String,
        airRate: Long,
        seaRate: Long,
        contact: String = "",
        chinaWarehouseAddress: String = "",
        isActive: Boolean = true,
        rateApplicationDate: String = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.FRANCE).format(java.util.Date()),
        airLeadDays: String = "7 à 10 jours",
        seaLeadDays: String = "30 à 45 jours",
        internalNotes: String = "",
        airFastRate: Long = 60000L,
        airFastLeadDays: String = "3 à 5 jours",
        goodsCategoryDelays: String = "Générale: Normal | Batterie: +2j | Poudre/Liquide: +3j | Téléphone: +2j"
    ) {
        viewModelScope.launch {
            repository.insertCatalogueTransitaire(
                CatalogueTransitaireEntity(
                    name = name.trim(),
                    contact = contact.trim(),
                    address = chinaWarehouseAddress.ifBlank { hub }.trim(),
                    defaultTariffPerKg = airRate,
                    airNormalRateAr = airRate,
                    airFastRateAr = airFastRate,
                    seaRateAr = seaRate,
                    airNormalLeadDays = airLeadDays.ifBlank { "7 à 10 jours" },
                    airFastLeadDays = airFastLeadDays.ifBlank { "3 à 5 jours" },
                    seaNormalLeadDays = seaLeadDays.ifBlank { "30 à 45 jours" },
                    goodsCategoryDelays = goodsCategoryDelays,
                    notes = internalNotes.trim()
                )
            )
            snackbarFlow.value = "Transitaire « $name » ajouté"
        }
    }

    fun updateCatalogueTransitaire(
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
    ) {
        viewModelScope.launch {
            val list = repository.getAllCatalogueTransitairesSnapshot()
            val existing = list.firstOrNull { it.id == id }
            if (existing != null) {
                repository.updateCatalogueTransitaire(
                    existing.copy(
                        name = name.trim(),
                        contact = contact.trim(),
                        address = chinaWarehouseAddress.ifBlank { hub }.trim(),
                        defaultTariffPerKg = airRate,
                        airNormalRateAr = airRate,
                        airFastRateAr = airFastRate,
                        seaRateAr = seaRate,
                        airNormalLeadDays = airLeadDays.ifBlank { "7 à 10 jours" },
                        airFastLeadDays = airFastLeadDays.ifBlank { "3 à 5 jours" },
                        seaNormalLeadDays = seaLeadDays.ifBlank { "30 à 45 jours" },
                        goodsCategoryDelays = goodsCategoryDelays,
                        notes = internalNotes.trim()
                    )
                )
                snackbarFlow.value = "Transitaire « $name » modifié"
            }
        }
    }

    fun deleteCatalogueTransitaire(id: Long) {
        viewModelScope.launch {
            repository.deleteCatalogueTransitaire(id)
            snackbarFlow.value = "Transitaire supprimé"
        }
    }

    fun updateTransitaireRates(id: Long, airRate: Long, seaRate: Long) {
        viewModelScope.launch {
            val list = repository.getAllCatalogueTransitairesSnapshot()
            val existing = list.firstOrNull { it.id == id }
            if (existing != null) {
                repository.updateCatalogueTransitaire(
                    existing.copy(
                        defaultTariffPerKg = airRate,
                        airNormalRateAr = airRate,
                        seaRateAr = seaRate
                    )
                )
                snackbarFlow.value = "Tarifs mis à jour"
            }
        }
    }

    fun toggleTransitaireArchived(id: Long) {
        viewModelScope.launch {
            val list = repository.getAllCatalogueTransitairesSnapshot()
            val existing = list.firstOrNull { it.id == id }
            if (existing != null) {
                val isCurrentlyArchived = existing.notes.startsWith("[ARCHIVED]")
                val newNotes = if (isCurrentlyArchived) {
                    existing.notes.removePrefix("[ARCHIVED]").trim()
                } else {
                    "[ARCHIVED] ${existing.notes}".trim()
                }
                repository.updateCatalogueTransitaire(existing.copy(notes = newNotes))
                snackbarFlow.value = if (isCurrentlyArchived) "Transitaire réactivé" else "Transitaire archivé"
            }
        }
    }

    fun recordTransitairePayment(id: Long, amountAr: Long) {
        viewModelScope.launch {
            val list = repository.getAllCatalogueTransitairesSnapshot()
            val transName = list.firstOrNull { it.id == id }?.name ?: "Transitaire"
            repository.insertBusinessTransaction(
                com.example.data.local.entity.BusinessTransactionEntity(
                    title = "Règlement Fret — $transName",
                    type = "EXPENSE",
                    amountAr = amountAr,
                    accountName = "Business",
                    referenceTag = "TRANSITAIRE_$id",
                    dateMillis = System.currentTimeMillis()
                )
            )
            snackbarFlow.value = "Règlement transitaire de ${CurrencyFormatter.formatAriary(amountAr)} enregistré"
        }
    }

    fun onClientsFilterSelected(filter: String) {
        clientsFilterFlow.value = filter
    }

    fun onClientsSearchQueryChanged(query: String) {
        clientsSearchQueryFlow.value = query
    }

    fun addClient(name: String, category: String, phone: String, initialPurchaseAr: Long) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            repository.insertClient(
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
            snackbarFlow.value = "Client « $trimmedName » ajouté"
        }
    }

    fun recordClientPayment(clientId: Long, amountAr: Long) {
        viewModelScope.launch {
            if (amountAr <= 0L) return@launch
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
            snackbarFlow.value = "Encaissement de ${CurrencyFormatter.formatAriary(amountAr)} enregistré pour ${if (clientName.isNotBlank()) clientName else "le client"}"
        }
    }

    fun sendWhatsAppRelance(client: BusinessClientUi) {
        snackbarFlow.value = "Relance WhatsApp générée pour ${client.name}"
    }

    fun clearSnackbar() {
        snackbarFlow.value = null
    }

    val businessAccounts: StateFlow<List<com.example.data.local.entity.BusinessAccountEntity>> =
        repository.allBusinessAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedDefaultBusinessAccountsIfEmpty()
        }
    }

    fun addBusinessAccount(name: String, subtitle: String, initialBalanceAr: Long) {
        viewModelScope.launch {
            val shortCode = name.trim().take(2).uppercase().ifBlank { "CP" }
            repository.insertBusinessAccount(
                com.example.data.local.entity.BusinessAccountEntity(
                    name = name.trim(),
                    subtitle = subtitle.trim().ifBlank { "Compte" },
                    shortCode = shortCode,
                    balanceAr = initialBalanceAr
                )
            )
            snackbarFlow.value = "Compte « $name » ajouté"
        }
    }

    val businessTransactions: StateFlow<List<com.example.data.local.entity.BusinessTransactionEntity>> =
        repository.allBusinessTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBusinessTransaction(
        title: String,
        type: String,
        amountAr: Long,
        accountName: String,
        referenceTag: String = ""
    ) {
        viewModelScope.launch {
            repository.insertBusinessTransaction(
                com.example.data.local.entity.BusinessTransactionEntity(
                    title = title.trim(),
                    type = type,
                    amountAr = amountAr,
                    accountName = accountName.trim().ifBlank { "Espèces" },
                    referenceTag = referenceTag.trim()
                )
            )
            snackbarFlow.value = "Opération « $title » enregistrée"
        }
    }
}
