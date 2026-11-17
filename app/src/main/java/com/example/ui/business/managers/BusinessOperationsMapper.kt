package com.example.ui.business.managers

import com.example.data.local.entity.BusinessOrderEntity
import com.example.ui.business.BusinessClientUi
import com.example.ui.business.BusinessNotificationUi
import com.example.ui.business.FamilyFilterUi
import com.example.util.BordereauUtils
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal data class PurchasesMappingResult(
    val activeLots: Int,
    val draftActions: Int,
    val inRouteAmountAr: Long,
    val inRouteYuans: Double,
    val exchangeGainAr: Long,
    val filteredPurchasesList: List<BusinessOrderEntity>
)

internal data class StockMappingResult(
    val hiddenStockCount: Int,
    val availableStockPieces: Int,
    val reservedClientsPieces: Int,
    val totalStockValueAr: Long,
    val inRouteStockOrders: List<BusinessOrderEntity>,
    val inRouteStockUnits: Int,
    val defectiveStockOrders: List<BusinessOrderEntity>,
    val defectiveStockUnits: Int,
    val stockFamiliesCount: Int,
    val familyPills: List<FamilyFilterUi>,
    val filteredStockOrders: List<BusinessOrderEntity>
)

internal data class SalesMappingResult(
    val familyServiceProfitAr: Long,
    val salesGrowthVsPrevMonthPercent: Int,
    val salesTotalCollectedAr: Long,
    val salesRemainingToCollectAr: Long,
    val salesOpenInvoicesCount: Int,
    val salesNetProfitAr: Long,
    val salesPendingOrdersCount: Int,
    val pendingCustomerOrders: List<BusinessOrderEntity>,
    val filteredSales: List<com.example.data.local.entity.BusinessSaleEntity>,
    val prodMarginPct: Int,
    val exchGainPct: Int,
    val freightPct: Int
)

internal data class ClientsMappingResult(
    val mergedClients: List<BusinessClientUi>,
    val topClient: BusinessClientUi?,
    val filteredClients: List<BusinessClientUi>
)

internal fun mapPurchasesSection(
    raw: BusinessRawData,
    pFilters: PurchasesFilters,
    overview: OverviewMetricsResult
): PurchasesMappingResult {
    val orders = raw.orders
    val actualPurchases = orders.filter { it.status != "CATALOGUE" }
    val activeLots = orders.count { it.status !in listOf("CATALOGUE", "DRAFT", "DELIVERY_READY", "IN_STOCK", "COMPLETED", "SOLD") }
    val draftActions = orders.count { it.status == "DRAFT" }

    val inRouteOrders = orders.filter { it.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS", "QUALITY_CONTROL") }
    val inRouteAmountAr = inRouteOrders.sumOf(overview.orderCostLambda)
    val inRouteYuans = inRouteOrders.sumOf { (it.priceYuan * it.quantityBought) + it.domesticShippingYuan }

    val latestBuyRate = overview.buyTxs.maxByOrNull { it.date }?.exchangeRate ?: overview.exactCmupRate
    val exchangeGainAr = overview.buyTxs.sumOf { (latestBuyRate - it.exchangeRate) * it.amountYuan }.toLong().coerceAtLeast(0L)

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
        val matchSearch = if (pFilters.searchQuery.isBlank()) true else {
            val q = pFilters.searchQuery.trim()
            order.product.contains(q, ignoreCase = true) ||
                order.referenceCode.contains(q, ignoreCase = true) ||
                order.variants.contains(q, ignoreCase = true) ||
                order.carrier.contains(q, ignoreCase = true) ||
                order.platform.contains(q, ignoreCase = true) ||
                order.lotNumber.contains(q, ignoreCase = true) ||
                order.assignedClient.contains(q, ignoreCase = true) ||
                (BordereauUtils.resolveCodeOrNull(order)?.contains(q, ignoreCase = true) == true)
        }
        matchStatus && matchCategory && matchSearch
    }.let { list ->
        when (pFilters.sort) {
            "Date récent" -> list.sortedByDescending { it.orderDate }
            "Prix ¥ décroissant" -> list.sortedByDescending { it.priceYuan }
            "Prix Ar décroissant" -> list.sortedByDescending { it.resalePriceAr }
            else -> list
        }
    }

    return PurchasesMappingResult(
        activeLots = activeLots,
        draftActions = draftActions,
        inRouteAmountAr = inRouteAmountAr,
        inRouteYuans = inRouteYuans,
        exchangeGainAr = exchangeGainAr,
        filteredPurchasesList = filteredPurchasesList
    )
}

internal fun mapStockSection(
    orders: List<BusinessOrderEntity>,
    sFilters: StockFilters
): StockMappingResult {
    val allStockReadyOrders = orders.filter { it.stockCount > 0 }
    val hiddenStockCount = orders.count { it.isHidden }

    val visibleInStock = allStockReadyOrders.filter { !it.isHidden || sFilters.showHidden }
    val availableStockPieces = visibleInStock.sumOf { it.stockCount }
    val reservedClientsPieces = visibleInStock.sumOf { it.reservedCount }

    val totalStockValueAr = visibleInStock.sumOf { order ->
        val unitFifo = when {
            order.unitCostAr > 0L -> order.unitCostAr
            order.priceYuan > 0.0 -> (order.priceYuan * order.exchangeRate).toLong() +
                (if (order.quantityBought > 0) order.freightCostAr / order.quantityBought else 0L)
            else -> 0L
        }
        unitFifo * order.stockCount
    }

    val inRouteStockOrders = orders.filter { it.status in listOf("ORDERED", "WAREHOUSE", "GUANGZHOU", "FLIGHT", "CUSTOMS") }
    val inRouteStockUnits = inRouteStockOrders.sumOf { it.quantityBought }

    val defectiveStockOrders = orders.filter { it.defectiveCount > 0 && (!it.isHidden || sFilters.showHidden) }
    val defectiveStockUnits = defectiveStockOrders.sumOf { it.defectiveCount }

    val distinctFamilies = visibleInStock.map { it.family.ifBlank { "Électronique" } }.distinct().sorted()
    val familyPills = buildList {
        add(FamilyFilterUi("Toutes", availableStockPieces))
        distinctFamilies.forEach { fam ->
            val countForFam = visibleInStock.filter { it.family.equals(fam, ignoreCase = true) }.sumOf { it.stockCount }
            add(FamilyFilterUi(fam, countForFam))
        }
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

    return StockMappingResult(
        hiddenStockCount = hiddenStockCount,
        availableStockPieces = availableStockPieces,
        reservedClientsPieces = reservedClientsPieces,
        totalStockValueAr = totalStockValueAr,
        inRouteStockOrders = inRouteStockOrders,
        inRouteStockUnits = inRouteStockUnits,
        defectiveStockOrders = defectiveStockOrders,
        defectiveStockUnits = defectiveStockUnits,
        stockFamiliesCount = distinctFamilies.size,
        familyPills = familyPills,
        filteredStockOrders = filteredStockOrders
    )
}

internal fun mapSalesSection(
    raw: BusinessRawData,
    salesFilters: SalesFilters,
    overview: OverviewMetricsResult,
    exchangeGainAr: Long
): SalesMappingResult {
    val sales = raw.sales
    val familyServiceProfitAr = sales.filter { it.domain == "FAMILY_FRIENDS" }.sumOf { sale ->
        val totalProfit = sale.totalPriceAr - (sale.unitCostAr * sale.quantity)
        if (sale.totalPriceAr > 0L) {
            ((totalProfit.toDouble() * sale.paidAmountAr.toDouble()) / sale.totalPriceAr.toDouble()).toLong()
        } else 0L
    }

    fun computeDomainCollectedInPeriod(domain: String, startMs: Long, endMs: Long): Long {
        val domainIds = sales.filter { it.domain == domain }.map { it.id }.toSet()
        val paymentsSum = raw.salePayments
            .filter { it.saleId in domainIds && it.paymentDate in startMs..endMs }
            .sumOf { it.amountAr }
        val legacySum = sales.filter {
            it.domain == domain && it.saleDate in startMs..endMs && it.paidAmountAr > 0L && overview.paymentsBySaleId[it.id].isNullOrEmpty()
        }.sumOf { it.paidAmountAr }
        return paymentsSum + legacySum
    }

    val currentDomainMonthCa = computeDomainCollectedInPeriod(salesFilters.domain, overview.startOfMonthMs, overview.endOfMonthMs)
    val prevDomainMonthCa = computeDomainCollectedInPeriod(salesFilters.domain, overview.startOfPrevMonthMs, overview.endOfPrevMonthMs)
    val salesGrowthVsPrevMonthPercent = when {
        prevDomainMonthCa > 0L -> (((currentDomainMonthCa - prevDomainMonthCa).toDouble() / prevDomainMonthCa.toDouble()) * 100.0).toInt()
        currentDomainMonthCa > 0L -> 100
        else -> 0
    }

    val domainSales = sales.filter { it.domain == salesFilters.domain }
    val salesTotalCollectedAr = domainSales.sumOf { it.paidAmountAr }
    val salesRemainingToCollectAr = domainSales.sumOf { it.totalPriceAr - it.paidAmountAr }
    val salesOpenInvoicesCount = domainSales.count { !it.isFullyPaid }

    val salesNetProfitAr = domainSales.sumOf { sale ->
        val totalProfit = sale.totalPriceAr - (sale.unitCostAr * sale.quantity)
        if (sale.totalPriceAr > 0) {
            ((totalProfit.toDouble() * sale.paidAmountAr.toDouble()) / sale.totalPriceAr.toDouble()).toLong()
        } else 0L
    }

    val pendingCustomerOrders = raw.orders.filter { it.assignedClient.isNotBlank() }

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
    }.let { list ->
        when (salesFilters.sort) {
            "Montant décroissant" -> list.sortedByDescending { it.totalPriceAr }
            "Reste dû" -> list.sortedByDescending { (it.totalPriceAr - it.paidAmountAr).coerceAtLeast(0L) }
            "Marge décroissante" -> list.sortedByDescending { it.totalPriceAr - (it.unitCostAr * it.quantity) }
            else -> list.sortedByDescending { it.saleDate }
        }
    }

    val totalGainsAr = (salesNetProfitAr + exchangeGainAr + overview.freightAndExpenses).coerceAtLeast(1L)
    val prodMarginPct = if (salesNetProfitAr > 0L) (((salesNetProfitAr.toDouble() / totalGainsAr.toDouble()) * 100).toInt()).coerceIn(0, 100) else 0
    val exchGainPct = if (exchangeGainAr > 0L) (((exchangeGainAr.toDouble() / totalGainsAr.toDouble()) * 100).toInt()).coerceIn(0, 100) else 0
    val freightPct = if (totalGainsAr > 1L) (100 - prodMarginPct - exchGainPct).coerceAtLeast(0) else 0

    return SalesMappingResult(
        familyServiceProfitAr = familyServiceProfitAr,
        salesGrowthVsPrevMonthPercent = salesGrowthVsPrevMonthPercent,
        salesTotalCollectedAr = salesTotalCollectedAr,
        salesRemainingToCollectAr = salesRemainingToCollectAr,
        salesOpenInvoicesCount = salesOpenInvoicesCount,
        salesNetProfitAr = salesNetProfitAr,
        salesPendingOrdersCount = pendingCustomerOrders.size,
        pendingCustomerOrders = pendingCustomerOrders,
        filteredSales = filteredSales,
        prodMarginPct = prodMarginPct,
        exchGainPct = exchGainPct,
        freightPct = freightPct
    )
}

internal fun mapClientsSection(
    raw: BusinessRawData,
    clientsFilters: ClientsFiltersState,
    nowMs: Long
): ClientsMappingResult {
    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    val sales = raw.sales

    val clientsFromRoom = raw.clients.map { client ->
        val clientSales = sales.filter {
            it.clientId == client.id || it.clientName.trim().equals(client.name.trim(), ignoreCase = true)
        }
        val totalPurchases = clientSales.sumOf { it.totalPriceAr }
        val totalPaid = clientSales.sumOf { it.paidAmountAr }
        val due = (totalPurchases - totalPaid).coerceAtLeast(0L)
        val phone = client.phone.ifBlank { clientSales.firstOrNull { it.clientPhone.isNotBlank() }?.clientPhone ?: "" }
        val inits = client.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "CL" }
        val latestSaleMs = clientSales.maxOfOrNull { it.saleDate }
        val lastPurchaseDateStr = if (latestSaleMs != null) dateFmt.format(Date(latestSaleMs)) else "Aucun achat"
        val hasOverdue = clientSales.any { !it.isFullyPaid && (it.totalPriceAr - it.paidAmountAr) > 0L && it.dueDate < nowMs }
        val statusTag = when {
            due > 0L && hasOverdue -> "Échéance dépassée"
            due > 0L -> "Solde dû"
            clientSales.isEmpty() -> "Nouveau"
            else -> "À jour"
        }
        BusinessClientUi(
            id = client.id,
            name = client.name,
            initials = inits,
            category = client.category,
            totalPurchasesAr = totalPurchases,
            totalOrdersCount = clientSales.size,
            balanceDueAr = due,
            statusLabel = if (due == 0L) "À jour ✓" else "Reste : ${CurrencyFormatter.formatAriary(due)}",
            statusTag = statusTag,
            isDueUrgent = due > 0L && hasOverdue,
            lastPurchaseDate = lastPurchaseDateStr,
            phone = phone,
            totalPaidAr = totalPaid,
            avatarType = client.avatarType,
            suggestedRelanceMsg = if (due > 0L) "Bonjour ${client.name}, rappel amical de ton solde de ${CurrencyFormatter.formatAriary(due)} sur tes achats." else ""
        )
    }

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
            val category = when (firstSale.domain) {
                "BUSINESS" -> "Business"
                "PERSONAL" -> "Myself"
                else -> "Famille & amis"
            }
            val inits = cName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifBlank { "CL" }
            val latestSaleMs = sGroup.maxOfOrNull { it.saleDate }
            val lastPurchaseDateStr = if (latestSaleMs != null) dateFmt.format(Date(latestSaleMs)) else "Aucun achat"
            val hasOverdue = sGroup.any { !it.isFullyPaid && (it.totalPriceAr - it.paidAmountAr) > 0L && it.dueDate < nowMs }
            val statusTag = when {
                due > 0L && hasOverdue -> "Échéance dépassée"
                due > 0L -> "Solde dû"
                else -> "À jour"
            }
            BusinessClientUi(
                id = (idx + 10000).toLong(),
                name = cName,
                initials = inits,
                category = category,
                totalPurchasesAr = totalPurchases,
                totalOrdersCount = sGroup.size,
                balanceDueAr = due,
                statusLabel = if (due == 0L) "À jour ✓" else "Reste : ${CurrencyFormatter.formatAriary(due)}",
                statusTag = statusTag,
                isDueUrgent = due > 0L && hasOverdue,
                lastPurchaseDate = lastPurchaseDateStr,
                phone = phone,
                totalPaidAr = totalPaid,
                avatarType = when (category) {
                    "Business" -> "CYAN"
                    "Myself" -> "ORANGE"
                    else -> "VIOLET"
                },
                suggestedRelanceMsg = if (due > 0L) "Bonjour $cName, rappel amical de ton solde de ${CurrencyFormatter.formatAriary(due)} sur tes achats." else ""
            )
        }

    val mergedClients = clientsFromRoom + clientsFromSalesOnly
    val topClient = mergedClients.filter { it.totalPurchasesAr > 0L }.maxByOrNull { it.totalPurchasesAr }

    val filteredClients = mergedClients.filter { c ->
        val matchFilter = when (clientsFilters.filter) {
            "due" -> c.balanceDueAr > 0
            "upToDate" -> c.balanceDueAr == 0L
            "business" -> c.category == "Business"
            "famille", "family" -> c.category == "Famille & amis"
            "myself" -> c.category == "Myself"
            else -> true
        }
        val matchSearch = if (clientsFilters.searchQuery.isBlank()) true else {
            c.name.contains(clientsFilters.searchQuery, ignoreCase = true) ||
                c.phone.contains(clientsFilters.searchQuery, ignoreCase = true)
        }
        matchFilter && matchSearch
    }

    return ClientsMappingResult(
        mergedClients = mergedClients,
        topClient = topClient,
        filteredClients = filteredClients
    )
}

internal fun buildDynamicNotifications(
    orders: List<BusinessOrderEntity>,
    overview: OverviewMetricsResult
): List<BusinessNotificationUi> = buildList {
    if (overview.overdue15d.isNotEmpty()) {
        val topOverdueSale = overview.overdue15d.maxByOrNull { it.totalPriceAr - it.paidAmountAr }
        add(
            BusinessNotificationUi(
                id = "notif_overdue_sales",
                title = "${overview.overdue15d.size} facture(s) en retard",
                message = "${CurrencyFormatter.formatAriary(overview.overdueAmount)} à encaisser (+15 jours)",
                targetSubTab = "Ventes",
                clientName = topOverdueSale?.clientName?.takeIf { it.isNotBlank() },
                isUrgent = true
            )
        )
    }
    if (overview.freightDueAmount > 0L) {
        add(
            BusinessNotificationUi(
                id = "notif_freight_due",
                title = "Fret transitaire à régler",
                message = "${CurrencyFormatter.formatAriary(overview.freightDueAmount)} en attente de règlement",
                targetSubTab = "Finances",
                isUrgent = overview.freightDueDays <= 2
            )
        )
    }
    val shortage = overview.insufficientShortage
    if (shortage != null && shortage > 0.0) {
        val firstDraft = overview.draftOrders.firstOrNull()
        add(
            BusinessNotificationUi(
                id = "notif_draft_yuan",
                title = "Yuans insuffisants pour brouillons",
                message = "Il manque ${String.format(Locale.FRANCE, "%.0f", shortage)} ¥ pour valider ${overview.draftOrders.size} commande(s)",
                targetSubTab = "Achats",
                orderId = firstDraft?.id,
                isUrgent = true
            )
        )
    }
    val restockOrders = orders.filter { it.restockAlert && !it.isHidden }
    if (restockOrders.isNotEmpty()) {
        add(
            BusinessNotificationUi(
                id = "notif_restock",
                title = "${restockOrders.size} article(s) à réapprovisionner",
                message = restockOrders.take(2).joinToString(", ") { it.product },
                targetSubTab = "Stock",
                orderId = restockOrders.firstOrNull()?.id,
                isUrgent = false
            )
        )
    }
}
