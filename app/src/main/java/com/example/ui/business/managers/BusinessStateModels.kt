package com.example.ui.business.managers

import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.CatalogueFamilyEntity
import com.example.data.local.entity.CataloguePlatformEntity
import com.example.data.local.entity.CatalogueTransitaireEntity
import com.example.data.local.entity.ClientEntity
import com.example.data.local.entity.SalePaymentEntity
import com.example.data.local.entity.TransitaireRateHistoryEntity
import com.example.data.local.entity.YuanTransactionEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal data class BusinessRawData(
    val orders: List<BusinessOrderEntity>,
    val yuanTxs: List<YuanTransactionEntity>,
    val sales: List<BusinessSaleEntity>,
    val families: List<CatalogueFamilyEntity>,
    val platforms: List<CataloguePlatformEntity>,
    val clients: List<ClientEntity>,
    val transitaires: List<CatalogueTransitaireEntity>,
    val businessTransactions: List<BusinessTransactionEntity> = emptyList(),
    val salePayments: List<SalePaymentEntity> = emptyList(),
    val accounts: List<BusinessAccountEntity> = emptyList(),
    val transitaireRateHistory: List<TransitaireRateHistoryEntity> = emptyList()
)

internal data class PurchasesFilters(
    val status: String,
    val category: String,
    val sort: String,
    val searchQuery: String
)

internal data class StockFilters(
    val segment: String,
    val searchQuery: String,
    val family: String,
    val showHidden: Boolean
)

internal data class SalesFilters(
    val domain: String,
    val status: String,
    val searchQuery: String,
    val sort: String
)

internal data class CatalogueFilters(
    val isOpen: Boolean,
    val subTab: String,
    val searchQuery: String,
    val family: String,
    val stockFilter: String,
    val showHidden: Boolean,
    val sort: String,
    val viewMode: String
)

internal data class ClientsFiltersState(
    val filter: String,
    val searchQuery: String
)

internal data class BusinessControlState(
    val subTab: String,
    val monthOffset: Int,
    val insightIndex: Int,
    val snackbarMsg: String?
)

internal fun buildBusinessRawDataFlow(repository: AppRepository): Flow<BusinessRawData> = combine(
    repository.businessOrders,
    repository.yuanTransactions,
    repository.businessSales,
    combine(repository.catalogueFamilies, repository.cataloguePlatforms, repository.salePayments) { f, p, sp -> Triple(f, p, sp) },
    combine(
        repository.clients,
        repository.catalogueTransitaires,
        repository.allBusinessTransactions,
        repository.allBusinessAccounts,
        repository.transitaireRateHistory
    ) { c, t, tx, acc, hist -> listOf(c, t, tx, acc, hist) }
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
        businessTransactions = other[2] as List<BusinessTransactionEntity>,
        salePayments = famPlatPay.third,
        accounts = other[3] as List<BusinessAccountEntity>,
        transitaireRateHistory = other[4] as List<TransitaireRateHistoryEntity>
    )
}
