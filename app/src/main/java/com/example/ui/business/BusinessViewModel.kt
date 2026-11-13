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
import com.example.ui.business.managers.FinancialReporter
import com.example.ui.business.managers.SalesManager
import com.example.ui.business.managers.StockManager
import com.example.ui.business.managers.TransitaireManager
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
    val salePayments: List<com.example.data.local.entity.SalePaymentEntity> = emptyList(),
    val accounts: List<com.example.data.local.entity.BusinessAccountEntity> = emptyList()
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
    val stockFilter: String,
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

    val salesManager = SalesManager(repository)
    val stockManager = StockManager(repository)
    val transitaireManager = TransitaireManager(repository)
    val financialReporter = FinancialReporter(repository)

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
    private val catalogueStockFilterFlow = MutableStateFlow("Tous")
    private val catalogueShowHiddenFlow = MutableStateFlow(false)
    private val catalogueSortFlow = MutableStateFlow("Plus vendus") // "Plus vendus", "Marge élevée", "Stock bas", "Dernier coût", "Nom", "Plus récents"
    private val catalogueViewModeFlow = MutableStateFlow("LIST") // "LIST" ou "GRID"

    private val clientsFilterFlow = MutableStateFlow("all")
    private val clientsSearchQueryFlow = MutableStateFlow("")

    private val rawDataFlow = combine(
        repository.businessOrders,
        repository.yuanTransactions,
        repository.businessSales,
        combine(repository.catalogueFamilies, repository.cataloguePlatforms, repository.salePayments) { f, p, sp -> Triple(f, p, sp) },
        combine(
            repository.clients,
            repository.catalogueTransitaires,
            repository.allBusinessTransactions,
            repository.allBusinessAccounts
        ) { c, t, tx, acc ->
            listOf(c, t, tx, acc)
        }
    ) { orders, yuanTxs, sales, famPlatPay, other ->
        @Suppress("UNCHECKED_CAST")
        BusinessRawData(
            orders = orders,
            yuanTxs = yuanTxs,
            sales = sales,
            families = famPlatPay.first,
            platforms = famPlatPay.second,
            clients = other[0] as List<ClientEntity>,
            transitaires = other[1] as List<CatalogueTransitaireEntity>,
            businessTransactions = other[2] as List<com.example.data.local.entity.BusinessTransactionEntity>,
            salePayments = famPlatPay.third,
            accounts = other[3] as List<com.example.data.local.entity.BusinessAccountEntity>
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
        catalogueViewModeFlow,
        catalogueStockFilterFlow
    ) { hidden, sort, mode, stockFilter ->
        listOf(hidden.toString(), sort, mode, stockFilter)
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
            viewMode = p2[2],
            stockFilter = p2[3]
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

        // 2. Portefeuille Yuans (source réelle : comptes Yuan actifs + CMUP exact sans troncature)
        val buyTxs = yuanTxs.filter { it.type == "BUY" }
        val refundTxs = yuanTxs.filter { it.type == "REFUND" }
        val spendTxs = yuanTxs.filter { it.type == "SPEND" }
        val totalBoughtYuan = buyTxs.sumOf { it.amountYuan }
        val totalRefundedYuan = refundTxs.sumOf { it.amountYuan }
        val totalSpentYuan = spendTxs.sumOf { it.amountYuan }
        val totalCostAr = buyTxs.sumOf { it.amountAr }

        val activeYuanAccounts = raw.accounts.filter { it.isYuanAccount && it.isActive }
        val txNetYuans = (totalBoughtYuan + totalRefundedYuan - totalSpentYuan).coerceAtLeast(0.0)
        val accountsYuanSum = activeYuanAccounts.sumOf { it.balanceYuan }
        val availableYuans = when {
            activeYuanAccounts.isEmpty() -> 0.0
            accountsYuanSum == 0.0 && txNetYuans > 0.0 -> txNetYuans
            else -> accountsYuanSum.coerceAtLeast(0.0)
        }
        val exactCmupRate = if (totalBoughtYuan > 0.0) totalCostAr.toDouble() / totalBoughtYuan else 650.0
        val cmupRate = kotlin.math.round(exactCmupRate).toLong()
        val estimatedYuanValue = kotlin.math.round(availableYuans * exactCmupRate).toLong()

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

        val (allTimeSalesTotal, allTimeSalesCost) = computeCashInAndCost(0L, Long.MAX_VALUE)
        val allTimeFreightCost = orders.sumOf { it.freightCostAr }
        val cumulativeBusinessProfitAr = allTimeSalesTotal - allTimeSalesCost - allTimeFreightCost

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
        val totalTransitairePaidRecorded = raw.businessTransactions
            .filter { it.referenceTag.startsWith("TRANSITAIRE_") && it.type == "EXPENSE" }
            .sumOf { it.amountAr }
        val totalFreightInvoiced = orders.sumOf { it.freightCostAr }
        val freightDueAmount = (totalFreightInvoiced - totalTransitairePaidRecorded).coerceAtLeast(0L)
        val unpaidFreightOrders = orders.filter { !it.isFreightPaid }
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
                carrier = if (o.carrier.isNotBlank()) o.carrier else "Non spécifié",
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
        // Puces de familles pour le catalogue
        val defaultFamilies = listOf(
            CatalogueFamilyEntity(id = 1, name = "Électronique & Audio", emoji = "🎧", colorHex = "#06B6D4", displayOrder = 1),
            CatalogueFamilyEntity(id = 2, name = "Mode & Maroquinerie", emoji = "🎒", colorHex = "#EC4899", displayOrder = 2),
            CatalogueFamilyEntity(id = 3, name = "Maison & Rangement", emoji = "🏠", colorHex = "#F97316", displayOrder = 3),
            CatalogueFamilyEntity(id = 4, name = "Accessoires Mobiles", emoji = "📱", colorHex = "#8B5CF6", displayOrder = 4)
        )
        val availableFamilies = if (raw.families.isNotEmpty()) raw.families else defaultFamilies

        // Regroupement des lots par article (SKU ou nom normalisé) pour une fiche produit consolidée
        val inTransitStatuses = setOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL")
        val allCatalogueArticles = orders
            .groupBy { order ->
                val skuKey = order.referenceCode.trim().lowercase()
                val nameKey = order.product.trim().lowercase()
                if (skuKey.isNotBlank()) "sku:$skuKey" else "name:$nameKey"
            }
            .map { (articleKey, groupLots) ->
                val sortedLots = groupLots.sortedByDescending { it.orderDate }
                val primary = sortedLots.firstOrNull { !it.isHidden } ?: sortedLots.first()

                // Hérite les métadonnées riches si absentes du lot le plus récent
                val resolvedImage = sortedLots.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: ""
                val resolvedFamily = sortedLots.firstOrNull { it.family.isNotBlank() }?.family ?: "Divers"
                val resolvedPlatform = sortedLots.firstOrNull { it.platform.isNotBlank() }?.platform ?: primary.supplier
                val resolvedWeight = sortedLots.firstOrNull { (it.weightKg ?: 0.0) > 0.0 }?.weightKg
                val resolvedResale = sortedLots.firstOrNull { it.resalePriceAr > 0L }?.resalePriceAr ?: primary.resalePriceAr
                val resolvedSku = sortedLots.firstOrNull { it.referenceCode.isNotBlank() }?.referenceCode ?: primary.referenceCode
                val resolvedNotes = sortedLots.firstOrNull { it.receptionNotes.isNotBlank() }?.receptionNotes ?: ""

                val hydratedPrimary = primary.copy(
                    imageUrl = resolvedImage,
                    family = resolvedFamily,
                    platform = resolvedPlatform,
                    weightKg = resolvedWeight,
                    resalePriceAr = resolvedResale,
                    referenceCode = resolvedSku,
                    receptionNotes = resolvedNotes
                )

                val totalStock = sortedLots.sumOf { it.stockCount.coerceAtLeast(0) }
                val totalTransit = sortedLots.filter { it.status in inTransitStatuses }.sumOf { it.quantityBought.coerceAtLeast(0) }
                val totalReserved = sortedLots.sumOf { it.reservedCount.coerceAtLeast(0) }
                val totalDefective = sortedLots.sumOf { it.defectiveCount.coerceAtLeast(0) }
                val totalBought = sortedLots.sumOf { if (it.status == "CATALOGUE") 0 else it.quantityBought.coerceAtLeast(0) }

                val lotIds = sortedLots.map { it.id }.toSet()
                val normalizedProduct = hydratedPrimary.product.trim().lowercase()
                val matchingSales = sales.filter { sale ->
                    (sale.linkedOrderId != null && sale.linkedOrderId in lotIds) ||
                    (normalizedProduct.isNotBlank() && sale.productName.trim().lowercase() == normalizedProduct)
                }.sortedByDescending { it.saleDate }

                val salesQty = matchingSales.sumOf { it.quantity }
                val lotSoldSum = sortedLots.sumOf { it.unitsSold.coerceAtLeast(0) }
                val totalSold = maxOf(lotSoldSum, salesQty)

                val latestCostLot = sortedLots.firstOrNull {
                    it.unitCostAr > 0L || it.priceYuan > 0.0
                } ?: hydratedPrimary
                val latestPriceYuan = latestCostLot.priceYuan
                val latestUnitCostAr = when {
                    latestCostLot.unitCostAr > 0L -> latestCostLot.unitCostAr
                    latestCostLot.priceYuan > 0.0 -> {
                        val effRate = if (latestCostLot.exchangeRate > 0.0) latestCostLot.exchangeRate else cmupRate.toDouble()
                        val unitFreight = if (latestCostLot.freightCostAr > 0L && latestCostLot.quantityBought > 0) {
                            latestCostLot.freightCostAr / latestCostLot.quantityBought
                        } else 0L
                        kotlin.math.round(latestCostLot.priceYuan * effRate).toLong() + unitFreight
                    }
                    else -> 0L
                }

                val computedMarginPct = when {
                    resolvedResale > 0L && latestUnitCostAr > 0L ->
                        (((resolvedResale - latestUnitCostAr).toDouble() / resolvedResale.toDouble()) * 100).toInt()
                    hydratedPrimary.marginPercent != 0 -> hydratedPrimary.marginPercent
                    else -> 0
                }

                val totalRevenueAr = if (matchingSales.isNotEmpty()) {
                    matchingSales.sumOf { it.totalPriceAr }
                } else {
                    totalSold.toLong() * resolvedResale
                }
                val realProfitAr = if (matchingSales.isNotEmpty()) {
                    matchingSales.sumOf { sale ->
                        val c = if (sale.unitCostAr > 0L) sale.unitCostAr else latestUnitCostAr
                        (sale.totalPriceAr - (c * sale.quantity))
                    }
                } else {
                    (resolvedResale - latestUnitCostAr).coerceAtLeast(0L) * totalSold
                }

                val matchedFam = availableFamilies.firstOrNull { fam ->
                    resolvedFamily.equals(fam.name, ignoreCase = true) ||
                    fam.name.contains(resolvedFamily, ignoreCase = true) ||
                    (resolvedFamily.length >= 4 && fam.name.contains(resolvedFamily.take(4), ignoreCase = true))
                }

                val isHiddenArticle = sortedLots.all { it.isHidden }
                val isRefOnly = sortedLots.all { it.status == "CATALOGUE" && it.quantityBought == 0 && it.stockCount == 0 }
                val statusTag = when {
                    isHiddenArticle -> "Masqué"
                    totalStock > 5 -> "En stock"
                    totalStock in 1..5 -> "Stock faible"
                    totalTransit > 0 -> "En transit"
                    isRefOnly -> "Référentiel"
                    else -> "Rupture"
                }

                val combinedVariants = sortedLots
                    .flatMap { com.example.ui.business.components.VariantColorUtils.parseVariants(it.variants) }
                    .distinct()

                CatalogueArticleSummaryUi(
                    articleKey = articleKey,
                    primaryOrder = hydratedPrimary,
                    lots = sortedLots,
                    lotsCount = sortedLots.size,
                    totalStockCount = totalStock,
                    totalInTransitCount = totalTransit,
                    totalReservedCount = totalReserved,
                    totalDefectiveCount = totalDefective,
                    totalBoughtCount = totalBought,
                    totalSoldCount = totalSold,
                    totalRevenueAr = totalRevenueAr,
                    realProfitAr = realProfitAr,
                    latestPriceYuan = latestPriceYuan,
                    latestUnitCostAr = latestUnitCostAr,
                    resalePriceAr = resolvedResale,
                    marginPercent = computedMarginPct,
                    familyEmoji = matchedFam?.emoji ?: "📦",
                    familyColorHex = matchedFam?.colorHex ?: "#06B6D4",
                    statusTag = statusTag,
                    isHidden = isHiddenArticle,
                    isReferenceOnly = isRefOnly,
                    allVariants = combinedVariants,
                    salesHistory = matchingSales
                )
            }

        val activeCatalogueCount = allCatalogueArticles.count { !it.isHidden }
        val hiddenCatalogueCount = allCatalogueArticles.count { it.isHidden }

        val catalogueFamilyPills = mutableListOf<FamilyFilterUi>()
        catalogueFamilyPills.add(FamilyFilterUi("Toutes", activeCatalogueCount))
        availableFamilies.forEach { fam ->
            val countForFam = allCatalogueArticles.count { art ->
                !art.isHidden && (
                    art.primaryOrder.family.equals(fam.name, ignoreCase = true) ||
                    fam.name.contains(art.primaryOrder.family, ignoreCase = true) ||
                    (art.primaryOrder.family.length >= 4 && fam.name.contains(art.primaryOrder.family.take(4), ignoreCase = true))
                )
            }
            catalogueFamilyPills.add(FamilyFilterUi(fam.name, countForFam))
        }

        val visibleCatalogueArticles = allCatalogueArticles
            .filter { art ->
                val matchHidden = if (cFilters.showHidden) true else !art.isHidden
                val matchSearch = if (cFilters.searchQuery.isBlank()) true else {
                    val q = cFilters.searchQuery
                    art.primaryOrder.product.contains(q, ignoreCase = true) ||
                    art.primaryOrder.referenceCode.contains(q, ignoreCase = true) ||
                    art.allVariants.any { it.contains(q, ignoreCase = true) } ||
                    art.primaryOrder.family.contains(q, ignoreCase = true) ||
                    art.primaryOrder.platform.contains(q, ignoreCase = true)
                }
                val matchFamily = if (cFilters.family == "Toutes") true else {
                    val f = art.primaryOrder.family
                    f.equals(cFilters.family, ignoreCase = true) ||
                    cFilters.family.contains(f, ignoreCase = true) ||
                    (f.length >= 4 && cFilters.family.contains(f.take(4), ignoreCase = true))
                }
                val matchStock = when (cFilters.stockFilter) {
                    "En stock" -> art.totalStockCount > 0
                    "En transit" -> art.totalInTransitCount > 0
                    "Rupture" -> art.totalStockCount == 0 && art.totalInTransitCount == 0 && !art.isReferenceOnly
                    "Référentiel" -> art.isReferenceOnly
                    else -> true
                }
                matchHidden && matchSearch && matchFamily && matchStock
            }
            .let { list ->
                when (cFilters.sort) {
                    "Plus vendus" -> list.sortedByDescending { it.totalSoldCount }
                    "Marge élevée" -> list.sortedByDescending { it.marginPercent }
                    "Stock bas" -> list.sortedBy { it.totalStockCount }
                    "Dernier coût" -> list.sortedByDescending { it.latestUnitCostAr }
                    "Nom" -> list.sortedBy { it.primaryOrder.product.lowercase() }
                    "Plus récents" -> list.sortedByDescending { it.primaryOrder.orderDate }
                    else -> list.sortedByDescending { it.totalSoldCount }
                }
            }

        // Compatibilité descendante pour visibleCatalogueOrders
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
            val sortedOrders = transOrders.sortedByDescending { it.orderDate }
            val latestOrder = sortedOrders.firstOrNull()
            val lastDate = latestOrder?.let { dateFmt.format(java.util.Date(it.orderDate)) } ?: ""
            val lastCode = latestOrder?.referenceCode?.ifBlank { "#CMD-${latestOrder.id}" } ?: ""
            val recentShipments = sortedOrders.take(6).map { ord ->
                val isDelivered = ord.status in listOf("STOCK", "DELIVERED", "SOLD", "ARRIVED")
                val statusLabel = when (ord.status) {
                    "ORDERED" -> "Commandé"
                    "GUANGZHOU" -> "Entrepôt Chine"
                    "FLIGHT" -> "En transit"
                    "CUSTOMS" -> "Douane Mada"
                    "QUALITY_CONTROL" -> "Contrôle qualité"
                    "STOCK", "ARRIVED" -> "Arrivé Mada"
                    "DELIVERED", "SOLD" -> "Livré / Vendu"
                    else -> ord.status
                }
                val modeStr = if (ord.freightMode.equals("SEA", ignoreCase = true) || ord.freightMode.equals("MARITIME", ignoreCase = true)) {
                    "SEA"
                } else {
                    "AIR"
                }
                TransitaireShipmentSummaryUi(
                    id = ord.id,
                    code = ord.referenceCode.ifBlank { "#CMD-${ord.id}" },
                    title = ord.product.ifBlank { "Colis #${ord.id}" },
                    transportMode = modeStr,
                    weightKg = ord.weightKg ?: 0.0,
                    volumeCbm = ord.volumeM3 ?: 0.0,
                    freightCostAr = ord.freightCostAr,
                    statusLabel = statusLabel,
                    isDelivered = isDelivered,
                    dateLabel = dateFmt.format(java.util.Date(ord.orderDate))
                )
            }
            val dynamicHub = t.address.lines().firstOrNull()?.trim()?.take(38)?.ifBlank { null }
                ?: "Fret Chine → Madagascar"

            CatalogueTransitaireSummaryUi(
                id = t.id,
                name = t.name,
                initials = inits,
                routeHub = dynamicHub,
                airRateAr = if (t.airNormalRateAr > 0) t.airNormalRateAr else (if (t.defaultTariffPerKg > 0) t.defaultTariffPerKg else 45000L),
                airFastRateAr = if (t.airFastRateAr > 0) t.airFastRateAr else 60000L,
                seaRateAr = if (t.seaRateAr > 0) t.seaRateAr else 18000L,
                contact = t.contact,
                chinaWarehouseAddress = t.address,
                rateApplicationDate = t.rateApplicationDate,
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
                goodsCategoryDelays = t.goodsCategoryDelays,
                recentShipments = recentShipments
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
            catalogueStockFilter = cFilters.stockFilter,
            catalogueShowHidden = cFilters.showHidden,
            catalogueSort = cFilters.sort,
            catalogueViewMode = cFilters.viewMode,
            activeCatalogueCount = activeCatalogueCount,
            hiddenCatalogueCount = hiddenCatalogueCount,
            catalogueFamilyPills = catalogueFamilyPills,
            visibleCatalogueOrders = filteredCatalogueOrders,
            allCatalogueArticles = allCatalogueArticles,
            visibleCatalogueArticles = visibleCatalogueArticles,
            catalogueFamilies = catalogueFamiliesSummary,
            cataloguePlatforms = cataloguePlatformsSummary,
            catalogueTransitaires = catalogueTransitairesSummary,

            // Hero
            netProfit = netProfit,
            cumulativeBusinessProfitAr = cumulativeBusinessProfitAr,
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
            businessAccounts = raw.accounts,
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

    fun onCatalogueStockFilterSelected(filter: String) {
        catalogueStockFilterFlow.value = filter
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
        platform: String = "1688",
        imageUrl: String = "",
        freightMode: String = "AÉRIEN",
        notes: String = "",
        estimatedFreightPerKgAr: Long = 45000L
    ) {
        viewModelScope.launch {
            val msg = stockManager.addCatalogueArticle(
                product = product,
                family = family,
                referenceCode = referenceCode,
                variants = variants,
                priceYuan = priceYuan,
                resaleAr = resaleAr,
                stockCount = stockCount,
                weightKg = weightKg,
                platform = platform,
                cmupRate = uiState.value.cmupRate.toDouble(),
                imageUrl = imageUrl,
                freightMode = freightMode,
                notes = notes,
                estimatedFreightPerKgAr = estimatedFreightPerKgAr
            )
            snackbarFlow.value = msg
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
            val msg = stockManager.updateCatalogueArticle(order, newPriceYuan, newResaleAr, newVariants, isHidden)
            snackbarFlow.value = msg
        }
    }

    fun updateCatalogueArticleFull(
        article: CatalogueArticleSummaryUi,
        newProduct: String,
        newReferenceCode: String,
        newFamily: String,
        newPlatform: String,
        newPriceYuan: Double,
        newResaleAr: Long,
        newWeightKg: Double?,
        newFreightMode: String,
        newVariants: String,
        newImageUrl: String,
        newNotes: String,
        isHidden: Boolean
    ) {
        viewModelScope.launch {
            val msg = stockManager.updateCatalogueArticleFull(
                article = article,
                newProduct = newProduct,
                newReferenceCode = newReferenceCode,
                newFamily = newFamily,
                newPlatform = newPlatform,
                newPriceYuan = newPriceYuan,
                newResaleAr = newResaleAr,
                newWeightKg = newWeightKg,
                newFreightMode = newFreightMode,
                newVariants = newVariants,
                newImageUrl = newImageUrl,
                newNotes = newNotes,
                isHidden = isHidden,
                cmupRate = uiState.value.cmupRate.toDouble()
            )
            snackbarFlow.value = msg
        }
    }

    fun toggleHideCatalogueArticle(article: CatalogueArticleSummaryUi) {
        viewModelScope.launch {
            val msg = stockManager.toggleHideCatalogueArticle(article)
            snackbarFlow.value = msg
        }
    }

    fun deleteCatalogueArticle(article: CatalogueArticleSummaryUi) {
        viewModelScope.launch {
            val msg = stockManager.deleteCatalogueArticle(article)
            snackbarFlow.value = msg
        }
    }

    fun addCatalogueFamily(name: String, emoji: String = "✨", colorHex: String = "#06B6D4") {
        viewModelScope.launch {
            val msg = stockManager.addCatalogueFamily(name, emoji, colorHex)
            if (msg != null) snackbarFlow.value = msg
        }
    }

    fun deleteCatalogueFamily(family: CatalogueFamilySummaryUi, targetFamilyName: String = "Divers") {
        viewModelScope.launch {
            val msg = stockManager.deleteCatalogueFamily(family, targetFamilyName)
            snackbarFlow.value = msg
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
            val msg = stockManager.adjustStock(order, newStockCount, newReservedCount, newDefectiveCount, assignedClient)
            snackbarFlow.value = msg
        }
    }

    fun toggleHideStockItem(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val msg = stockManager.toggleHideStockItem(order)
            snackbarFlow.value = msg
        }
    }

    fun deleteStockOrder(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val msg = stockManager.deleteStockOrder(order)
            snackbarFlow.value = msg
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
            val msg = salesManager.recordSalePayment(sale, additionalPaymentAr, paymentMethod)
            if (msg != null) snackbarFlow.value = msg
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
            val msg = salesManager.addNewSale(
                productName, clientName, clientPhone, quantity, unitPriceAr, unitCostAr,
                paidAmountAr, saleSource, domain, linkedOrderId
            )
            snackbarFlow.value = msg
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
            val msg = salesManager.addBusinessSale(
                clientName, clientPhone, productName, quantity, unitResalePriceAr, unitCostAr, paidAmountAr
            )
            snackbarFlow.value = msg
        }
    }

    fun deleteSale(sale: BusinessSaleEntity) {
        viewModelScope.launch {
            val msg = salesManager.deleteSale(sale)
            snackbarFlow.value = msg
        }
    }

    fun onAdvanceQualityControl(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val msg = stockManager.onAdvanceQualityControl(order)
            snackbarFlow.value = msg
        }
    }

    fun buyYuans(amountYuan: Double, rateAr: Double, platform: String, sourceAccountName: String = "") {
        viewModelScope.launch {
            val msg = financialReporter.buyYuans(amountYuan, rateAr, platform, sourceAccountName)
            snackbarFlow.value = msg
        }
    }

    fun refundYuans(amountYuan: Double, rateAr: Double, platform: String = "Alipay", note: String = "", orderId: Long? = null) {
        viewModelScope.launch {
            val msg = financialReporter.refundYuans(amountYuan, rateAr, platform, note, orderId)
            if (msg.isNotBlank()) snackbarFlow.value = msg
        }
    }

    fun spendYuans(amountYuan: Double, rateAr: Double, platform: String, note: String, orderId: Long? = null, skipBalanceCheck: Boolean = false) {
        viewModelScope.launch {
            val msg = financialReporter.spendYuans(
                amountYuan, rateAr, platform, note, orderId, uiState.value.availableYuans, skipBalanceCheck
            )
            if (msg.isNotBlank()) snackbarFlow.value = msg
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
            val msg = stockManager.addBusinessOrder(
                product, supplier, quantity, priceYuan, resaleAr, category, carrier, variants,
                freightAr, platform, referenceCode, family, uiState.value.cmupRate.toDouble()
            )
            snackbarFlow.value = msg
        }
    }

    fun advanceOrderStatus(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val msg = stockManager.advanceOrderStatus(order)
            snackbarFlow.value = msg
        }
    }

    fun updateBusinessOrder(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val msg = stockManager.updateBusinessOrder(order)
            snackbarFlow.value = msg
        }
    }

    fun duplicateBusinessOrder(order: BusinessOrderEntity) {
        viewModelScope.launch {
            val msg = stockManager.duplicateBusinessOrder(order)
            snackbarFlow.value = msg
        }
    }

    fun togglePlatformActive(platformId: Long, isActive: Boolean) {
        viewModelScope.launch {
            stockManager.togglePlatformActive(platformId, isActive)
        }
    }

    fun updatePlatformShippingTag(platformId: Long, newShippingTag: String) {
        viewModelScope.launch {
            val msg = stockManager.updatePlatformShippingTag(platformId, newShippingTag)
            if (msg != null) snackbarFlow.value = msg
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
            val msg = stockManager.addCataloguePlatform(name, subtitle, shippingTag, secondaryTag, iconType, colorHex)
            snackbarFlow.value = msg
        }
    }

    fun deleteCataloguePlatform(platformId: Long) {
        viewModelScope.launch {
            val msg = stockManager.deleteCataloguePlatform(platformId)
            snackbarFlow.value = msg
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
            val msg = transitaireManager.addCatalogueTransitaire(
                name, hub, airRate, seaRate, contact, chinaWarehouseAddress, isActive,
                rateApplicationDate, airLeadDays, seaLeadDays, internalNotes, airFastRate, airFastLeadDays, goodsCategoryDelays
            )
            snackbarFlow.value = msg
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
            val msg = transitaireManager.updateCatalogueTransitaire(
                id, name, hub, airRate, seaRate, contact, chinaWarehouseAddress, isActive,
                rateApplicationDate, airLeadDays, seaLeadDays, internalNotes, airFastRate, airFastLeadDays, goodsCategoryDelays
            )
            if (msg != null) snackbarFlow.value = msg
        }
    }

    fun deleteCatalogueTransitaire(id: Long) {
        viewModelScope.launch {
            val msg = transitaireManager.deleteCatalogueTransitaire(id)
            snackbarFlow.value = msg
        }
    }

    fun updateTransitaireRates(id: Long, airRate: Long, seaRate: Long) {
        viewModelScope.launch {
            val msg = transitaireManager.updateTransitaireRates(id, airRate, seaRate)
            if (msg != null) snackbarFlow.value = msg
        }
    }

    fun toggleTransitaireArchived(id: Long) {
        viewModelScope.launch {
            val msg = transitaireManager.toggleTransitaireArchived(id)
            if (msg != null) snackbarFlow.value = msg
        }
    }

    fun recordTransitairePayment(id: Long, amountAr: Long) {
        viewModelScope.launch {
            val msg = transitaireManager.recordTransitairePayment(id, amountAr)
            snackbarFlow.value = msg
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
            val msg = salesManager.addClient(name, category, phone, initialPurchaseAr)
            snackbarFlow.value = msg
        }
    }

    fun recordClientPayment(clientId: Long, amountAr: Long) {
        viewModelScope.launch {
            val msg = salesManager.recordClientPayment(clientId, amountAr)
            if (msg != null) snackbarFlow.value = msg
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

    fun addBusinessAccount(
        name: String,
        subtitle: String,
        initialBalanceAr: Long,
        isYuanAccount: Boolean = false,
        initialBalanceYuan: Double = 0.0
    ) {
        viewModelScope.launch {
            val msg = financialReporter.addBusinessAccount(
                name = name,
                subtitle = subtitle,
                initialBalanceAr = initialBalanceAr,
                isYuanAccount = isYuanAccount,
                initialBalanceYuan = initialBalanceYuan
            )
            snackbarFlow.value = msg
        }
    }

    fun updateBusinessAccount(
        existing: com.example.data.local.entity.BusinessAccountEntity,
        newName: String,
        newSubtitle: String,
        newBalanceAr: Long,
        newBalanceYuan: Double
    ) {
        viewModelScope.launch {
            val msg = financialReporter.updateBusinessAccount(
                existing = existing,
                newName = newName,
                newSubtitle = newSubtitle,
                newBalanceAr = newBalanceAr,
                newBalanceYuan = newBalanceYuan
            )
            snackbarFlow.value = msg
        }
    }

    fun deleteBusinessAccount(account: com.example.data.local.entity.BusinessAccountEntity) {
        viewModelScope.launch {
            val msg = financialReporter.deleteBusinessAccount(account)
            snackbarFlow.value = msg
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
        referenceTag: String = "",
        destinationAccount: String = ""
    ) {
        viewModelScope.launch {
            val msg = financialReporter.addBusinessTransaction(title, type, amountAr, accountName, referenceTag, destinationAccount)
            snackbarFlow.value = msg
        }
    }

    fun deleteBusinessTransaction(tx: com.example.data.local.entity.BusinessTransactionEntity) {
        viewModelScope.launch {
            val msg = financialReporter.deleteBusinessTransaction(tx)
            snackbarFlow.value = msg
        }
    }

    val businessInvestments: StateFlow<List<com.example.data.local.entity.BusinessInvestmentEntity>> =
        repository.allBusinessInvestments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun recordCapitalMovement(
        title: String,
        isContribution: Boolean,
        amountAr: Long,
        accountName: String,
        note: String = "",
        affectAccount: Boolean = true,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val msg = financialReporter.recordCapitalMovement(
                title = title,
                isContribution = isContribution,
                amountAr = amountAr,
                accountName = accountName,
                note = note,
                affectAccount = affectAccount,
                dateMillis = dateMillis
            )
            snackbarFlow.value = msg
        }
    }

    fun addBusinessInvestment(
        title: String,
        category: String,
        amountAr: Long,
        estimatedValueAr: Long,
        generatedReturnAr: Long,
        accountName: String,
        notes: String = "",
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val msg = financialReporter.addBusinessInvestment(
                title = title,
                category = category,
                amountAr = amountAr,
                estimatedValueAr = estimatedValueAr,
                generatedReturnAr = generatedReturnAr,
                accountName = accountName,
                notes = notes,
                dateMillis = dateMillis
            )
            snackbarFlow.value = msg
        }
    }

    fun updateBusinessInvestment(investment: com.example.data.local.entity.BusinessInvestmentEntity) {
        viewModelScope.launch {
            val msg = financialReporter.updateBusinessInvestment(investment)
            snackbarFlow.value = msg
        }
    }

    fun deleteBusinessInvestment(investment: com.example.data.local.entity.BusinessInvestmentEntity) {
        viewModelScope.launch {
            val msg = financialReporter.deleteBusinessInvestment(investment)
            snackbarFlow.value = msg
        }
    }
}
