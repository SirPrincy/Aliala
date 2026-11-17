package com.example.ui.business

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.SettingsDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.repository.AppRepository
import com.example.ui.business.managers.BusinessControlState
import com.example.ui.business.managers.CatalogueFilters
import com.example.ui.business.managers.ClientsFiltersState
import com.example.ui.business.managers.FinancialReporter
import com.example.ui.business.managers.PurchasesFilters
import com.example.ui.business.managers.SalesFilters
import com.example.ui.business.managers.SalesManager
import com.example.ui.business.managers.StockFilters
import com.example.ui.business.managers.StockManager
import com.example.ui.business.managers.TransitaireManager
import com.example.ui.business.managers.buildBusinessRawDataFlow
import com.example.ui.business.managers.buildBusinessUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val purchasesStatusFilterFlow = MutableStateFlow("Tous")
    private val purchasesCategoryFilterFlow = MutableStateFlow("Toutes catégories")
    private val purchasesSortFilterFlow = MutableStateFlow("Date récent")
    private val purchasesSearchQueryFlow = MutableStateFlow("")

    private val stockSegmentFlow = MutableStateFlow("Disponible")
    private val stockSearchQueryFlow = MutableStateFlow("")
    private val stockFamilyFilterFlow = MutableStateFlow("Toutes")
    private val showHiddenStockFlow = MutableStateFlow(false)

    private val salesDomainFlow = MutableStateFlow("BUSINESS")
    private val salesStatusFilterFlow = MutableStateFlow("Toutes")
    private val salesSearchQueryFlow = MutableStateFlow("")
    private val salesSortFilterFlow = MutableStateFlow("Date récent")

    private val isCatalogueOpenFlow = MutableStateFlow(false)
    private val catalogueSubTabFlow = MutableStateFlow("Articles")
    private val catalogueSearchQueryFlow = MutableStateFlow("")
    private val catalogueFamilyFilterFlow = MutableStateFlow("Toutes")
    private val catalogueStockFilterFlow = MutableStateFlow("Tous")
    private val catalogueShowHiddenFlow = MutableStateFlow(false)
    private val catalogueSortFlow = MutableStateFlow("Plus vendus")
    private val catalogueViewModeFlow = MutableStateFlow("LIST")

    private val clientsFilterFlow = MutableStateFlow("all")
    private val clientsSearchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch { repository.seedDefaultCatalogueTablesIfEmpty() }
        viewModelScope.launch { repository.seedDefaultBusinessAccountsIfEmpty() }
    }

    private val purchasesFiltersFlow = combine(
        purchasesStatusFilterFlow, purchasesCategoryFilterFlow, purchasesSortFilterFlow, purchasesSearchQueryFlow
    ) { st, cat, sort, query -> PurchasesFilters(st, cat, sort, query) }

    private val stockFiltersFlow = combine(
        stockSegmentFlow, stockSearchQueryFlow, stockFamilyFilterFlow, showHiddenStockFlow
    ) { seg, query, fam, hidden -> StockFilters(seg, query, fam, hidden) }

    private val salesFiltersFlow = combine(
        salesDomainFlow, salesStatusFilterFlow, salesSearchQueryFlow, salesSortFilterFlow
    ) { domain, status, query, sort -> SalesFilters(domain, status, query, sort) }

    private val catalogueFiltersFlow = combine(
        combine(isCatalogueOpenFlow, catalogueSubTabFlow, catalogueSearchQueryFlow, catalogueFamilyFilterFlow) { a, b, c, d -> listOf(a.toString(), b, c, d) },
        combine(catalogueShowHiddenFlow, catalogueSortFlow, catalogueViewModeFlow, catalogueStockFilterFlow) { e, f, g, h -> listOf(e.toString(), f, g, h) }
    ) { p1, p2 ->
        CatalogueFilters(p1[0].toBoolean(), p1[1], p1[2], p1[3], p2[3], p2[0].toBoolean(), p2[1], p2[2])
    }

    private val secondaryFiltersFlow = combine(
        salesFiltersFlow,
        catalogueFiltersFlow,
        combine(clientsFilterFlow, clientsSearchQueryFlow) { f, q -> ClientsFiltersState(f, q) }
    ) { salesF, catF, clientsF -> Triple(salesF, catF, clientsF) }

    private val controlStateFlow = combine(
        selectedContextFlow, selectedSubTabFlow, monthOffsetFlow, insightIndexFlow, snackbarFlow
    ) { ctx, tab, offset, insightIdx, snackbar -> BusinessControlState(ctx, tab, offset, insightIdx, snackbar) }

    val uiState: StateFlow<BusinessUiState> = combine(
        buildBusinessRawDataFlow(repository), controlStateFlow, purchasesFiltersFlow, stockFiltersFlow, secondaryFiltersFlow
    ) { raw, ctrl, pFilters, sFilters, secondary ->
        buildBusinessUiState(raw, ctrl, pFilters, sFilters, secondary.first, secondary.second, secondary.third)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessUiState())

    val businessAccounts: StateFlow<List<BusinessAccountEntity>> = repository.allBusinessAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessTransactions: StateFlow<List<BusinessTransactionEntity>> = repository.allBusinessTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessInvestments: StateFlow<List<BusinessInvestmentEntity>> = repository.allBusinessInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessDebts: StateFlow<List<BusinessDebtEntity>> = repository.allBusinessDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessDebtPayments: StateFlow<List<BusinessDebtPaymentEntity>> = repository.allBusinessDebtPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSelectContext(choice: String) { selectedContextFlow.value = choice }
    fun onSelectSubTab(tab: String) { selectedSubTabFlow.value = tab }
    fun onPrevMonth() { monthOffsetFlow.value -= 1 }
    fun onNextMonth() { monthOffsetFlow.value += 1 }
    fun onResetCurrentMonth() { monthOffsetFlow.value = 0 }
    fun onNextInsight() { insightIndexFlow.value = if (insightIndexFlow.value >= 3) 1 else insightIndexFlow.value + 1 }

    // Catalogue
    fun openCatalogue(showHidden: Boolean = false) {
        catalogueShowHiddenFlow.value = showHidden
        isCatalogueOpenFlow.value = true
    }

    fun closeCatalogue() { isCatalogueOpenFlow.value = false }
    fun onCatalogueSubTabSelected(tab: String) { catalogueSubTabFlow.value = tab }
    fun onCatalogueSearchQueryChanged(query: String) { catalogueSearchQueryFlow.value = query }
    fun onCatalogueFamilyFilterSelected(family: String) { catalogueFamilyFilterFlow.value = family }
    fun onCatalogueStockFilterSelected(filter: String) { catalogueStockFilterFlow.value = filter }
    fun onToggleCatalogueShowHidden() { catalogueShowHiddenFlow.value = !catalogueShowHiddenFlow.value }
    fun onCatalogueSortChanged(sort: String) { catalogueSortFlow.value = sort }
    fun onCatalogueViewModeChanged(mode: String) { catalogueViewModeFlow.value = mode }

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
            snackbarFlow.value = stockManager.addCatalogueArticle(
                product, family, referenceCode, variants, priceYuan, resaleAr, stockCount, weightKg,
                platform, uiState.value.cmupRate.toDouble(), imageUrl, freightMode, notes, estimatedFreightPerKgAr
            )
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
            snackbarFlow.value = stockManager.updateCatalogueArticle(order, newPriceYuan, newResaleAr, newVariants, isHidden)
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
            snackbarFlow.value = stockManager.updateCatalogueArticleFull(
                article, newProduct, newReferenceCode, newFamily, newPlatform, newPriceYuan, newResaleAr,
                newWeightKg, newFreightMode, newVariants, newImageUrl, newNotes, isHidden,
                uiState.value.cmupRate.toDouble()
            )
        }
    }

    fun toggleHideCatalogueArticle(article: CatalogueArticleSummaryUi) {
        viewModelScope.launch { snackbarFlow.value = stockManager.toggleHideCatalogueArticle(article) }
    }

    fun deleteCatalogueArticle(article: CatalogueArticleSummaryUi) {
        viewModelScope.launch { snackbarFlow.value = stockManager.deleteCatalogueArticle(article) }
    }

    fun addCatalogueFamily(name: String, emoji: String = "✨", colorHex: String = "#06B6D4") {
        viewModelScope.launch {
            stockManager.addCatalogueFamily(name, emoji, colorHex)?.let { snackbarFlow.value = it }
        }
    }

    fun deleteCatalogueFamily(family: CatalogueFamilySummaryUi, targetFamilyName: String = "Divers") {
        viewModelScope.launch { snackbarFlow.value = stockManager.deleteCatalogueFamily(family, targetFamilyName) }
    }

    // Achats
    fun onSelectPurchasesStatusFilter(filter: String) { purchasesStatusFilterFlow.value = filter }
    fun onSelectPurchasesCategoryFilter(cat: String) { purchasesCategoryFilterFlow.value = cat }
    fun onSelectPurchasesSortFilter(sort: String) { purchasesSortFilterFlow.value = sort }
    fun onPurchasesSearchQueryChanged(query: String) { purchasesSearchQueryFlow.value = query }

    // Stock
    fun onStockSegmentSelected(segment: String) { stockSegmentFlow.value = segment }
    fun onStockSearchQueryChanged(query: String) { stockSearchQueryFlow.value = query }
    fun onStockFamilyFilterSelected(family: String) { stockFamilyFilterFlow.value = family }
    fun onToggleShowHiddenStock() { showHiddenStockFlow.value = !showHiddenStockFlow.value }

    fun adjustStock(
        order: BusinessOrderEntity,
        newStockCount: Int,
        newReservedCount: Int,
        newDefectiveCount: Int,
        assignedClient: String
    ) {
        viewModelScope.launch {
            snackbarFlow.value = stockManager.adjustStock(order, newStockCount, newReservedCount, newDefectiveCount, assignedClient)
        }
    }

    fun toggleHideStockItem(order: BusinessOrderEntity) {
        viewModelScope.launch { snackbarFlow.value = stockManager.toggleHideStockItem(order) }
    }

    fun deleteStockOrder(order: BusinessOrderEntity) {
        viewModelScope.launch { snackbarFlow.value = stockManager.deleteStockOrder(order) }
    }

    // Ventes
    fun onSalesDomainSelected(domain: String) { salesDomainFlow.value = domain }
    fun onSalesStatusFilterSelected(filter: String) { salesStatusFilterFlow.value = filter }
    fun onSalesSearchQueryChanged(query: String) { salesSearchQueryFlow.value = query }
    fun onSalesSortFilterSelected(sort: String) { salesSortFilterFlow.value = sort }

    fun recordSalePayment(
        sale: BusinessSaleEntity,
        additionalPaymentAr: Long,
        paymentMethod: String = "ESPECES",
        accountName: String = ""
    ) {
        viewModelScope.launch {
            salesManager.recordSalePayment(sale, additionalPaymentAr, paymentMethod, accountName)?.let { snackbarFlow.value = it }
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
        linkedOrderId: Long? = null,
        accountName: String = "",
        saleDateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            snackbarFlow.value = salesManager.addNewSale(
                productName, clientName, clientPhone, quantity, unitPriceAr, unitCostAr,
                paidAmountAr, saleSource, domain, linkedOrderId, accountName, saleDateMillis
            )
        }
    }

    fun updateSaleDate(sale: BusinessSaleEntity, newSaleDateMillis: Long) {
        viewModelScope.launch {
            snackbarFlow.value = salesManager.updateSaleDate(sale, newSaleDateMillis)
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
            snackbarFlow.value = salesManager.addBusinessSale(
                clientName, clientPhone, productName, quantity, unitResalePriceAr, unitCostAr, paidAmountAr
            )
        }
    }

    fun deleteSale(sale: BusinessSaleEntity) {
        viewModelScope.launch { snackbarFlow.value = salesManager.deleteSale(sale) }
    }

    fun onAdvanceQualityControl(order: BusinessOrderEntity) {
        viewModelScope.launch { snackbarFlow.value = stockManager.onAdvanceQualityControl(order) }
    }

    fun buyYuans(
        amountYuan: Double,
        rateAr: Double,
        platform: String,
        sourceAccountName: String = "",
        purchaseDateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            snackbarFlow.value = financialReporter.buyYuans(
                amountYuan, rateAr, platform, sourceAccountName, purchaseDateMillis
            )
        }
    }

    fun refundYuans(amountYuan: Double, rateAr: Double, platform: String = "Alipay", note: String = "", orderId: Long? = null) {
        viewModelScope.launch {
            val res = financialReporter.refundYuans(amountYuan, rateAr, platform, note, orderId)
            if (res.message.isNotBlank()) snackbarFlow.value = res.message
        }
    }

    fun spendYuans(
        amountYuan: Double,
        rateAr: Double,
        platform: String,
        note: String,
        orderId: Long? = null,
        skipBalanceCheck: Boolean = false,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val res = financialReporter.spendYuans(
                amountYuan, rateAr, platform, note, orderId, uiState.value.availableYuans, skipBalanceCheck
            )
            if (res.message.isNotBlank()) snackbarFlow.value = res.message
            onResult?.invoke(res.success, res.message)
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
        family: String = "Électronique",
        domesticShippingYuan: Double = 0.0,
        orderDateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            snackbarFlow.value = stockManager.addBusinessOrder(
                product, supplier, quantity, priceYuan, resaleAr, category, carrier, variants,
                freightAr, platform, referenceCode, family, uiState.value.cmupRate.toDouble(),
                domesticShippingYuan, orderDateMillis
            )
        }
    }

    fun advanceOrderStatus(order: BusinessOrderEntity) {
        viewModelScope.launch { snackbarFlow.value = stockManager.advanceOrderStatus(order) }
    }

    fun updateBusinessOrder(order: BusinessOrderEntity) {
        viewModelScope.launch { snackbarFlow.value = stockManager.updateBusinessOrder(order) }
    }

    fun duplicateBusinessOrder(order: BusinessOrderEntity) {
        viewModelScope.launch { snackbarFlow.value = stockManager.duplicateBusinessOrder(order) }
    }

    fun togglePlatformActive(platformId: Long, isActive: Boolean) {
        viewModelScope.launch { stockManager.togglePlatformActive(platformId, isActive) }
    }

    fun updatePlatformShippingTag(platformId: Long, newShippingTag: String) {
        viewModelScope.launch {
            stockManager.updatePlatformShippingTag(platformId, newShippingTag)?.let { snackbarFlow.value = it }
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
            snackbarFlow.value = stockManager.addCataloguePlatform(name, subtitle, shippingTag, secondaryTag, iconType, colorHex)
        }
    }

    fun deleteCataloguePlatform(platformId: Long) {
        viewModelScope.launch { snackbarFlow.value = stockManager.deleteCataloguePlatform(platformId) }
    }

    fun addCatalogueTransitaire(
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
    ) {
        viewModelScope.launch {
            snackbarFlow.value = transitaireManager.addCatalogueTransitaire(
                name, hub, airRate, seaRate, contact, chinaWarehouseAddress, isActive,
                rateApplicationDate, airLeadDays, seaLeadDays, internalNotes, airFastRate, airFastLeadDays, goodsCategoryDelays
            )
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
            transitaireManager.updateCatalogueTransitaire(
                id, name, hub, airRate, seaRate, contact, chinaWarehouseAddress, isActive,
                rateApplicationDate, airLeadDays, seaLeadDays, internalNotes, airFastRate, airFastLeadDays, goodsCategoryDelays
            )?.let { snackbarFlow.value = it }
        }
    }

    fun deleteCatalogueTransitaire(id: Long) {
        viewModelScope.launch { snackbarFlow.value = transitaireManager.deleteCatalogueTransitaire(id) }
    }

    fun updateTransitaireRates(id: Long, airRate: Long, seaRate: Long) {
        viewModelScope.launch {
            transitaireManager.updateTransitaireRates(id, airRate, seaRate)?.let { snackbarFlow.value = it }
        }
    }

    fun toggleTransitaireArchived(id: Long) {
        viewModelScope.launch {
            transitaireManager.toggleTransitaireArchived(id)?.let { snackbarFlow.value = it }
        }
    }

    fun recordTransitairePayment(id: Long, amountAr: Long, accountName: String = "") {
        viewModelScope.launch { snackbarFlow.value = transitaireManager.recordTransitairePayment(id, amountAr, accountName) }
    }

    fun payOrderFreight(order: BusinessOrderEntity, amountAr: Long, accountName: String = "") {
        viewModelScope.launch { snackbarFlow.value = transitaireManager.payOrderFreight(order, amountAr, accountName) }
    }

    fun onClientsFilterSelected(filter: String) { clientsFilterFlow.value = filter }
    fun onClientsSearchQueryChanged(query: String) { clientsSearchQueryFlow.value = query }

    fun addClient(name: String, category: String, phone: String, initialPurchaseAr: Long) {
        viewModelScope.launch { snackbarFlow.value = salesManager.addClient(name, category, phone, initialPurchaseAr) }
    }

    fun recordClientPayment(clientId: Long, amountAr: Long) {
        viewModelScope.launch {
            salesManager.recordClientPayment(clientId = clientId, amountAr = amountAr)?.let { snackbarFlow.value = it }
        }
    }

    fun recordClientPayment(client: BusinessClientUi, amountAr: Long, accountName: String = "") {
        viewModelScope.launch {
            salesManager.recordClientPayment(
                clientId = client.id,
                amountAr = amountAr,
                clientName = client.name,
                accountName = accountName,
                clientPhone = client.phone,
                category = client.category
            )?.let { snackbarFlow.value = it }
        }
    }

    fun sendWhatsAppRelance(client: BusinessClientUi) {
        snackbarFlow.value = "Relance WhatsApp générée pour ${client.name}"
    }

    fun clearSnackbar() { snackbarFlow.value = null }

    fun addBusinessAccount(
        name: String,
        subtitle: String,
        initialBalanceAr: Long,
        isYuanAccount: Boolean = false,
        initialBalanceYuan: Double = 0.0
    ) {
        viewModelScope.launch {
            snackbarFlow.value = financialReporter.addBusinessAccount(name, subtitle, initialBalanceAr, isYuanAccount, initialBalanceYuan)
        }
    }

    fun updateBusinessAccount(
        existing: BusinessAccountEntity,
        newName: String,
        newSubtitle: String,
        newBalanceAr: Long,
        newBalanceYuan: Double
    ) {
        viewModelScope.launch {
            snackbarFlow.value = financialReporter.updateBusinessAccount(existing, newName, newSubtitle, newBalanceAr, newBalanceYuan)
        }
    }

    fun deleteBusinessAccount(account: BusinessAccountEntity) {
        viewModelScope.launch { snackbarFlow.value = financialReporter.deleteBusinessAccount(account) }
    }

    fun addBusinessTransaction(
        title: String,
        type: String,
        amountAr: Long,
        accountName: String,
        referenceTag: String = "",
        destinationAccount: String = ""
    ) {
        viewModelScope.launch {
            snackbarFlow.value = financialReporter.addBusinessTransaction(title, type, amountAr, accountName, referenceTag, destinationAccount)
        }
    }

    fun deleteBusinessTransaction(tx: BusinessTransactionEntity) {
        viewModelScope.launch { snackbarFlow.value = financialReporter.deleteBusinessTransaction(tx) }
    }

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
            snackbarFlow.value = financialReporter.recordCapitalMovement(
                title, isContribution, amountAr, accountName, note, affectAccount, dateMillis
            )
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
            snackbarFlow.value = financialReporter.addBusinessInvestment(
                title, category, amountAr, estimatedValueAr, generatedReturnAr, accountName, notes, dateMillis
            )
        }
    }

    fun updateBusinessInvestment(investment: BusinessInvestmentEntity) {
        viewModelScope.launch { snackbarFlow.value = financialReporter.updateBusinessInvestment(investment) }
    }

    fun deleteBusinessInvestment(investment: BusinessInvestmentEntity) {
        viewModelScope.launch { snackbarFlow.value = financialReporter.deleteBusinessInvestment(investment) }
    }

    fun addBusinessDebt(
        title: String,
        direction: String,
        counterparty: String,
        principalAr: Long,
        interestPercent: Double,
        dueDateMillis: Long?,
        createdAtMillis: Long,
        repaymentFrequency: String,
        accountName: String,
        affectAccount: Boolean,
        notes: String = ""
    ) {
        viewModelScope.launch {
            snackbarFlow.value = financialReporter.addBusinessDebt(
                title, direction, counterparty, principalAr, interestPercent,
                dueDateMillis, createdAtMillis, repaymentFrequency, accountName, affectAccount, notes
            )
        }
    }

    fun recordBusinessDebtPayment(
        debt: BusinessDebtEntity,
        amountAr: Long,
        accountName: String,
        affectAccount: Boolean,
        dateMillis: Long = System.currentTimeMillis(),
        note: String = ""
    ) {
        viewModelScope.launch {
            snackbarFlow.value = financialReporter.recordBusinessDebtPayment(
                debt, amountAr, accountName, affectAccount, dateMillis, note
            )
        }
    }

    fun updateBusinessDebt(debt: BusinessDebtEntity) {
        viewModelScope.launch { snackbarFlow.value = financialReporter.updateBusinessDebt(debt) }
    }

    fun deleteBusinessDebt(debt: BusinessDebtEntity) {
        viewModelScope.launch { snackbarFlow.value = financialReporter.deleteBusinessDebt(debt) }
    }
}
