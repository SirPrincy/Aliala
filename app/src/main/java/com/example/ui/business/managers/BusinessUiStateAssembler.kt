package com.example.ui.business.managers

import com.example.ui.business.BusinessUiState

internal fun buildBusinessUiState(
    raw: BusinessRawData,
    ctrl: BusinessControlState,
    pFilters: PurchasesFilters,
    sFilters: StockFilters,
    salesFilters: SalesFilters,
    cFilters: CatalogueFilters,
    clientsFilters: ClientsFiltersState
): BusinessUiState {
    val nowMs = System.currentTimeMillis()
    val overview = mapBusinessOverviewMetrics(raw = raw, monthOffset = ctrl.monthOffset, nowMs = nowMs)
    val purchases = mapPurchasesSection(raw = raw, pFilters = pFilters, overview = overview)
    val stock = mapStockSection(orders = raw.orders, sFilters = sFilters)
    val sales = mapSalesSection(
        raw = raw,
        salesFilters = salesFilters,
        overview = overview,
        exchangeGainAr = purchases.exchangeGainAr
    )
    val catalogue = mapCatalogueSection(
        raw = raw,
        cFilters = cFilters,
        cmupRate = overview.cmupRate,
        transitaireDueMap = overview.transitaireDueMap
    )
    val clients = mapClientsSection(raw = raw, clientsFilters = clientsFilters, nowMs = nowMs)
    val dynamicNotifications = buildDynamicNotifications(orders = raw.orders, overview = overview)

    return BusinessUiState(
        isLoading = false,
        selectedContext = ctrl.contextChoice,
        selectedSubTab = ctrl.subTab,
        purchasesCount = raw.orders.size,
        stockCount = stock.availableStockPieces,
        selectedMonthLabel = overview.monthLabel,
        monthOffset = ctrl.monthOffset,

        isCatalogueOpen = cFilters.isOpen,
        catalogueSubTab = cFilters.subTab,
        catalogueSearchQuery = cFilters.searchQuery,
        catalogueFamilyFilter = cFilters.family,
        catalogueStockFilter = cFilters.stockFilter,
        catalogueShowHidden = cFilters.showHidden,
        catalogueSort = cFilters.sort,
        catalogueViewMode = cFilters.viewMode,
        activeCatalogueCount = catalogue.activeCatalogueCount,
        hiddenCatalogueCount = catalogue.hiddenCatalogueCount,
        catalogueFamilyPills = catalogue.catalogueFamilyPills,
        visibleCatalogueOrders = catalogue.filteredCatalogueOrders,
        allCatalogueArticles = catalogue.allCatalogueArticles,
        visibleCatalogueArticles = catalogue.visibleCatalogueArticles,
        catalogueFamilies = catalogue.catalogueFamiliesSummary,
        cataloguePlatforms = catalogue.cataloguePlatformsSummary,
        catalogueTransitaires = catalogue.catalogueTransitairesSummary,

        netProfit = overview.netProfit,
        cumulativeBusinessProfitAr = overview.cumulativeBusinessProfitAr,
        vsPrevMonthPercent = overview.vsPrevMonthPercent,
        salesTotal = overview.salesTotal,
        salesCost = overview.salesCost,
        freightAndExpenses = overview.freightAndExpenses,
        marginSharePercent = sales.prodMarginPct,
        exchangeSharePercent = sales.exchGainPct,
        freightSharePercent = sales.freightPct,
        availableYuans = overview.availableYuans,
        cmupRate = overview.cmupRate,
        estimatedYuanValueAr = overview.estimatedYuanValue,
        businessAccounts = raw.accounts,
        insufficientYuanShortage = overview.insufficientShortage,
        freightDueAmount = overview.freightDueAmount,
        freightDueDays = overview.freightDueDays,
        clientReceivablesAmount = overview.clientReceivables,
        pendingClientsCount = overview.pendingClientsCount,
        pipelineSteps = overview.pipelineSteps,
        totalPipelineCount = overview.totalPipeline,
        incomingParcels = overview.incomingList,
        topProducts = overview.topProducts,
        totalPurchasesAr = overview.totalPurchasesAr,
        businessPurchasesAr = overview.businessPurchasesAr,
        businessPurchasesPercent = overview.bizPct,
        personalPurchasesAr = overview.personalPurchasesAr,
        personalPurchasesPercent = overview.persPct,
        familyPurchasesAr = overview.familyPurchasesAr,
        familyPurchasesPercent = overview.famPct,
        familyServiceProfitAr = sales.familyServiceProfitAr,
        exchangeGainAr = purchases.exchangeGainAr,
        overdueInvoicesCount = overview.overdueCount,
        overdueInvoicesAmount = overview.overdueAmount,
        currentInsightIndex = ctrl.insightIndex,
        totalInsightsCount = 3,

        purchasesStatusFilter = pFilters.status,
        purchasesCategoryFilter = pFilters.category,
        purchasesSortFilter = pFilters.sort,
        purchasesSearchQuery = pFilters.searchQuery,
        activeLotsCount = purchases.activeLots,
        draftActionsCount = purchases.draftActions,
        inRouteAmountAr = purchases.inRouteAmountAr,
        inRouteYuans = purchases.inRouteYuans,
        filteredPurchases = purchases.filteredPurchasesList,

        stockSegment = sFilters.segment,
        stockSearchQuery = sFilters.searchQuery,
        stockFamilyFilter = sFilters.family,
        showHiddenStock = sFilters.showHidden,
        hiddenStockCount = stock.hiddenStockCount,
        totalStockValueAr = stock.totalStockValueAr,
        availableStockPieces = stock.availableStockPieces,
        reservedClientsPieces = stock.reservedClientsPieces,
        inRouteStockUnits = stock.inRouteStockUnits,
        defectiveStockUnits = stock.defectiveStockUnits,
        stockFamiliesCount = stock.stockFamiliesCount,
        stockFamilyPills = stock.familyPills,
        visibleStockOrders = stock.filteredStockOrders,
        inRouteStockOrders = stock.inRouteStockOrders,
        defectiveStockOrders = stock.defectiveStockOrders,

        salesDomain = salesFilters.domain,
        salesStatusFilter = salesFilters.status,
        salesSearchQuery = salesFilters.searchQuery,
        salesSortFilter = salesFilters.sort,
        prevMonthShortLabel = overview.prevMonthShortLabel,
        salesTotalCollectedAr = sales.salesTotalCollectedAr,
        salesNetProfitAr = sales.salesNetProfitAr,
        salesGrowthVsPrevMonthPercent = sales.salesGrowthVsPrevMonthPercent,
        salesRemainingToCollectAr = sales.salesRemainingToCollectAr,
        salesOpenInvoicesCount = sales.salesOpenInvoicesCount,
        salesPendingOrdersCount = sales.salesPendingOrdersCount,
        salesProductMarginPercent = sales.prodMarginPct,
        salesExchangeGainPercent = sales.exchGainPct,
        salesFreightMarginPercent = sales.freightPct,
        visibleSales = sales.filteredSales,
        pendingCustomerOrders = sales.pendingCustomerOrders,

        clientsFilter = clientsFilters.filter,
        clientsSearchQuery = clientsFilters.searchQuery,
        activeClientsCount = clients.mergedClients.size,
        totalDueClientsAr = clients.mergedClients.sumOf { it.balanceDueAr },
        overdueClientsCount = clients.mergedClients.count { it.balanceDueAr > 0 },
        topClientName = clients.topClient?.name ?: "—",
        topClientTotalAr = clients.topClient?.totalPurchasesAr ?: 0L,
        clientsList = clients.filteredClients,

        orders = raw.orders,
        yuanTransactions = raw.yuanTxs,
        sales = raw.sales,
        businessNotifications = dynamicNotifications,
        snackbarMessage = ctrl.snackbarMsg
    )
}
