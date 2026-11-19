package com.example.ui.business

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.data.local.entity.YuanTransactionEntity

internal sealed interface BusinessDetailDestination {
    data object None : BusinessDetailDestination
    data class LotDetail(val orderId: Long) : BusinessDetailDestination
    data class ProductDetail(val articleKey: String) : BusinessDetailDestination
    data class Referentiel(val initialTab: String = "Transitaires") : BusinessDetailDestination
    data class TransitaireDetail(val transitaireId: Long, val returnToReferentiel: Boolean = true) : BusinessDetailDestination
}

internal sealed interface BusinessActiveSheet {
    data object None : BusinessActiveSheet
    data class OrderForm(
        val editingOrder: BusinessOrderEntity? = null,
        val initialArticle: BusinessOrderEntity? = null
    ) : BusinessActiveSheet
    data class AddSale(val initialArticle: BusinessOrderEntity? = null) : BusinessActiveSheet
    data class RecordSalePayment(val sale: BusinessSaleEntity) : BusinessActiveSheet
    data object BuyYuans : BusinessActiveSheet
    data object PayPlatform : BusinessActiveSheet
    data class AccountForm(
        val existing: BusinessAccountEntity? = null,
        val defaultIsYuan: Boolean = false
    ) : BusinessActiveSheet
    data class AddFinanceOperation(
        val presetTitle: String = "",
        val presetType: String = "IN",
        val presetTag: String = ""
    ) : BusinessActiveSheet
    data class TransactionDetail(val tx: BusinessTransactionEntity) : BusinessActiveSheet
    data class YuanTransactionDetail(val ytx: YuanTransactionEntity) : BusinessActiveSheet
    data class PayFreightOrder(val order: BusinessOrderEntity) : BusinessActiveSheet
    data class AdjustStock(val order: BusinessOrderEntity) : BusinessActiveSheet
    data class ClientDetail(val client: BusinessClientUi) : BusinessActiveSheet
    data class RecordClientPayment(val client: BusinessClientUi) : BusinessActiveSheet
    data object AddClient : BusinessActiveSheet
    data class WhatsAppRelance(
        val clientName: String = "",
        val phone: String = "",
        val balanceDueAr: Long = 0L,
        val customMessage: String = ""
    ) : BusinessActiveSheet
    data object Notifications : BusinessActiveSheet
    data object AddArticle : BusinessActiveSheet
    data class EditArticle(val article: CatalogueArticleSummaryUi) : BusinessActiveSheet
    data object AddFamily : BusinessActiveSheet
    data object AddPlatform : BusinessActiveSheet
    data class TransitaireForm(val existing: CatalogueTransitaireSummaryUi? = null) : BusinessActiveSheet
    data class TransitaireTariffLine(
        val transitaire: CatalogueTransitaireSummaryUi,
        val existingItem: com.example.ui.business.model.TariffItem? = null
    ) : BusinessActiveSheet
    data class PayTransitaire(val transitaire: CatalogueTransitaireSummaryUi) : BusinessActiveSheet
    data class CapitalMovement(
        val isContribution: Boolean = true,
        val existing: BusinessTransactionEntity? = null
    ) : BusinessActiveSheet
    data class ManageCategories(val initialType: String = "OUT") : BusinessActiveSheet
    data class InvestmentForm(val existing: BusinessInvestmentEntity? = null) : BusinessActiveSheet
    data class DebtForm(val existing: BusinessDebtEntity? = null) : BusinessActiveSheet
    data class DebtPayment(val debt: BusinessDebtEntity) : BusinessActiveSheet
}

@Stable
internal class BusinessNavigationState {
    var detailDestination by mutableStateOf<BusinessDetailDestination>(BusinessDetailDestination.None)
    var activeSheet by mutableStateOf<BusinessActiveSheet>(BusinessActiveSheet.None)

    fun closeSheet() {
        activeSheet = BusinessActiveSheet.None
    }

    fun openLot(orderId: Long) {
        activeSheet = BusinessActiveSheet.None
        detailDestination = BusinessDetailDestination.LotDetail(orderId)
    }

    fun openProduct(articleKey: String) {
        activeSheet = BusinessActiveSheet.None
        detailDestination = BusinessDetailDestination.ProductDetail(articleKey)
    }

    fun openReferentiel(tab: String = "Transitaires") {
        activeSheet = BusinessActiveSheet.None
        detailDestination = BusinessDetailDestination.Referentiel(tab)
    }

    fun openTransitaire(transitaireId: Long, fromReferentiel: Boolean = true) {
        activeSheet = BusinessActiveSheet.None
        detailDestination = BusinessDetailDestination.TransitaireDetail(transitaireId, fromReferentiel)
    }

    fun closeDetail() {
        val current = detailDestination
        detailDestination = if (current is BusinessDetailDestination.TransitaireDetail && current.returnToReferentiel) {
            BusinessDetailDestination.Referentiel("Transitaires")
        } else {
            BusinessDetailDestination.None
        }
    }

    fun openClientRelanceByName(
        clients: List<BusinessClientUi>,
        name: String,
        fallbackDue: Long,
        fallbackPhone: String
    ) {
        val matched = clients.firstOrNull { it.name.equals(name, ignoreCase = true) }
        activeSheet = if (matched != null) {
            BusinessActiveSheet.WhatsAppRelance(
                clientName = matched.name,
                phone = matched.phone,
                balanceDueAr = matched.balanceDueAr,
                customMessage = matched.suggestedRelanceMsg
            )
        } else {
            BusinessActiveSheet.WhatsAppRelance(
                clientName = name,
                phone = fallbackPhone,
                balanceDueAr = fallbackDue
            )
        }
    }

    fun openTopOverdueRelance(uiState: BusinessUiState) {
        val topClient = uiState.clientsList
            .filter { it.balanceDueAr > 0L }
            .maxByOrNull { it.balanceDueAr }
        if (topClient != null) {
            activeSheet = BusinessActiveSheet.WhatsAppRelance(
                clientName = topClient.name,
                phone = topClient.phone,
                balanceDueAr = topClient.balanceDueAr,
                customMessage = topClient.suggestedRelanceMsg
            )
            return
        }
        val overdueSale = uiState.sales.firstOrNull { !it.isFullyPaid && it.totalPriceAr > it.paidAmountAr }
        activeSheet = if (overdueSale != null) {
            BusinessActiveSheet.WhatsAppRelance(
                clientName = overdueSale.clientName,
                phone = overdueSale.clientPhone,
                balanceDueAr = (overdueSale.totalPriceAr - overdueSale.paidAmountAr).coerceAtLeast(0L)
            )
        } else {
            BusinessActiveSheet.WhatsAppRelance()
        }
    }
}

@Composable
internal fun rememberBusinessNavigationState(): BusinessNavigationState = remember {
    BusinessNavigationState()
}
