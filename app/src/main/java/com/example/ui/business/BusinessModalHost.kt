package com.example.ui.business

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.catalogue.AddCatalogueArticleDialog
import com.example.ui.business.catalogue.AddFamilyDialog
import com.example.ui.business.catalogue.EditCatalogueArticleDialog
import com.example.ui.business.clients.AddClientDialog
import com.example.ui.business.clients.ClientDetailDialog
import com.example.ui.business.clients.RecordClientPaymentDialog
import com.example.ui.business.clients.WhatsAppRelanceDialog
import com.example.ui.business.finances.AddOrEditBusinessAccountDialog
import com.example.ui.business.finances.BuyYuansDialog
import com.example.ui.business.finances.PayPlatformDialog
import com.example.ui.business.overview.BusinessNotificationsDialog
import com.example.ui.business.overview.NewActionMenuSheet
import com.example.ui.business.purchases.AddOrderDialog
import com.example.ui.business.sales.AddSaleDialog
import com.example.ui.business.sales.RecordSalePaymentDialog
import com.example.ui.business.stock.AdjustStockDialog
import com.example.ui.business.stock.SelectOrderToAdjustDialog
import com.example.ui.business.wizard.LogisticWizardScreen

@Stable
internal class BusinessModalState {
    var showBuyYuansDialog by mutableStateOf(false)
    var showQuickCreateYuanAccountDialog by mutableStateOf(false)
    var showPayPlatformDialog by mutableStateOf(false)
    var showAddOrderDialog by mutableStateOf(false)
    var showNewActionMenuSheet by mutableStateOf(false)
    var showWhatsAppRelanceDialog by mutableStateOf(false)
    var orderToAdjust by mutableStateOf<BusinessOrderEntity?>(null)
    var showSelectOrderToAdjustDialog by mutableStateOf(false)
    var saleToRecordPayment by mutableStateOf<BusinessSaleEntity?>(null)
    var showAddSaleDialog by mutableStateOf(false)
    var selectedArticleKey by mutableStateOf<String?>(null)
    var articleToEditInDialog by mutableStateOf<CatalogueArticleSummaryUi?>(null)
    var initialArticleForOrder by mutableStateOf<BusinessOrderEntity?>(null)
    var initialArticleForSale by mutableStateOf<BusinessOrderEntity?>(null)
    var selectedClientForRelance by mutableStateOf<BusinessClientUi?>(null)
    var selectedClientForDetail by mutableStateOf<BusinessClientUi?>(null)
    var clientToRecordPayment by mutableStateOf<BusinessClientUi?>(null)
    var showAddCatalogueArticleDialog by mutableStateOf(false)
    var showAddFamilyDialog by mutableStateOf(false)
    var showAddClientDialog by mutableStateOf(false)
    var showNotificationsDialog by mutableStateOf(false)
    var selectedOrderId by mutableStateOf<Long?>(null)

    fun openClientRelanceByName(
        clients: List<BusinessClientUi>,
        name: String,
        fallbackDue: Long,
        fallbackPhone: String
    ) {
        val matched = clients.firstOrNull { it.name.equals(name, ignoreCase = true) }
        if (matched != null) {
            selectedClientForRelance = matched
        } else if (name.isNotBlank()) {
            selectedClientForRelance = BusinessClientUi(
                name = name,
                initials = name.trim().take(2).uppercase().ifBlank { "CL" },
                balanceDueAr = fallbackDue,
                phone = fallbackPhone
            )
        } else {
            showWhatsAppRelanceDialog = true
        }
    }

    fun openOverdueClientRelance(uiState: BusinessUiState) {
        val topOverdue = uiState.clientsList
            .filter { it.balanceDueAr > 0L }
            .maxByOrNull { it.balanceDueAr }
        if (topOverdue != null) {
            selectedClientForRelance = topOverdue
        } else {
            val overdueSale = uiState.sales.firstOrNull { !it.isFullyPaid && it.totalPriceAr > it.paidAmountAr }
            if (overdueSale != null) {
                selectedClientForRelance = BusinessClientUi(
                    name = overdueSale.clientName,
                    initials = overdueSale.clientName.trim().take(2).uppercase().ifBlank { "CL" },
                    balanceDueAr = (overdueSale.totalPriceAr - overdueSale.paidAmountAr).coerceAtLeast(0L),
                    phone = overdueSale.clientPhone
                )
            } else {
                showWhatsAppRelanceDialog = true
            }
        }
    }
}

@Composable
internal fun rememberBusinessModalState(): BusinessModalState = remember { BusinessModalState() }

@Composable
internal fun BusinessModalHost(
    modalState: BusinessModalState,
    viewModel: BusinessViewModel,
    uiState: BusinessUiState,
    businessAccounts: List<BusinessAccountEntity>,
    primaryOrderFallback: BusinessOrderEntity? = null
) {
    val liveOrder = uiState.orders.firstOrNull { it.id == modalState.selectedOrderId }

    if (modalState.showBuyYuansDialog) {
        BuyYuansDialog(
            accounts = businessAccounts,
            defaultRate = uiState.cmupRate,
            onDismiss = { modalState.showBuyYuansDialog = false },
            onConfirm = { amountYuan, rateAr, platform, sourceAccountName ->
                viewModel.buyYuans(amountYuan, rateAr, platform, sourceAccountName)
            },
            onCreateAccountClick = { modalState.showQuickCreateYuanAccountDialog = true }
        )
    }

    if (modalState.showQuickCreateYuanAccountDialog) {
        AddOrEditBusinessAccountDialog(
            existingAccounts = businessAccounts,
            defaultIsYuan = true,
            onDismiss = { modalState.showQuickCreateYuanAccountDialog = false },
            onConfirmCreate = { name, subtitle, initialBalanceAr, isYuanAccount, initialBalanceYuan ->
                viewModel.addBusinessAccount(name, subtitle, initialBalanceAr, isYuanAccount, initialBalanceYuan)
                modalState.showQuickCreateYuanAccountDialog = false
                if (isYuanAccount) modalState.showBuyYuansDialog = true
            }
        )
    }

    if (modalState.showPayPlatformDialog) {
        PayPlatformDialog(
            cmupRate = uiState.cmupRate,
            availableYuans = uiState.availableYuans,
            accounts = businessAccounts,
            platforms = uiState.cataloguePlatforms.filter { it.isActive }.map { it.aliasKey.ifBlank { it.name } },
            onDismiss = { modalState.showPayPlatformDialog = false },
            onConfirm = { amountYuan, rateAr, yuanAccountName, note ->
                viewModel.spendYuans(amountYuan, rateAr, yuanAccountName, note)
            }
        )
    }

    if (modalState.showNewActionMenuSheet) {
        NewActionMenuSheet(
            onDismiss = { modalState.showNewActionMenuSheet = false },
            onNewArticleClick = { modalState.showAddCatalogueArticleDialog = true },
            onNewPurchaseClick = { modalState.showAddOrderDialog = true },
            onNewSaleClick = { modalState.showAddSaleDialog = true }
        )
    }

    if (modalState.showAddCatalogueArticleDialog) {
        AddCatalogueArticleDialog(
            availableFamilies = uiState.catalogueFamilies.map { it.name },
            familySummaries = uiState.catalogueFamilies,
            availablePlatforms = uiState.cataloguePlatforms.filter { it.isActive }.map { it.name },
            transitaires = uiState.catalogueTransitaires,
            cmupRate = uiState.cmupRate,
            onDismiss = { modalState.showAddCatalogueArticleDialog = false },
            onConfirm = { prod, fam, sku, vars, pYuan, rAr, sCount, wKg, plat, imgUrl, freightMode, notes, estFreightPerKg ->
                viewModel.addCatalogueArticle(
                    product = prod,
                    family = fam,
                    referenceCode = sku,
                    variants = vars,
                    priceYuan = pYuan,
                    resaleAr = rAr,
                    stockCount = sCount,
                    weightKg = wKg,
                    platform = plat,
                    imageUrl = imgUrl,
                    freightMode = freightMode,
                    notes = notes,
                    estimatedFreightPerKgAr = estFreightPerKg
                )
                modalState.showAddCatalogueArticleDialog = false
            }
        )
    }

    if (modalState.showAddFamilyDialog) {
        AddFamilyDialog(
            onDismiss = { modalState.showAddFamilyDialog = false },
            onConfirm = { famName, emoji, colorHex ->
                viewModel.addCatalogueFamily(famName, emoji, colorHex)
                viewModel.onCatalogueFamilyFilterSelected(famName)
                modalState.showAddFamilyDialog = false
            }
        )
    }

    if (modalState.showAddOrderDialog) {
        AddOrderDialog(
            cmupRate = uiState.cmupRate,
            availableYuans = uiState.availableYuans,
            availableArticles = uiState.orders,
            initialArticle = modalState.initialArticleForOrder ?: primaryOrderFallback,
            transitaires = uiState.catalogueTransitaires.filter { !it.isArchived }.map { it.name },
            onDismiss = {
                modalState.showAddOrderDialog = false
                modalState.initialArticleForOrder = null
            },
            onConfirm = { prod, supp, q, pY, rAr, cat, carr, varStr, fAr, plat, refCode, fam, domYuan ->
                viewModel.addBusinessOrder(prod, supp, q, pY, rAr, cat, carr, varStr, fAr, plat, refCode, fam, domYuan)
                modalState.showAddOrderDialog = false
                modalState.initialArticleForOrder = null
            }
        )
    }

    if (modalState.showWhatsAppRelanceDialog) {
        WhatsAppRelanceDialog(onDismiss = { modalState.showWhatsAppRelanceDialog = false })
    }

    modalState.selectedClientForRelance?.let { client ->
        WhatsAppRelanceDialog(
            clientName = client.name,
            clientPhone = client.phone,
            balanceDueAr = client.balanceDueAr,
            customMessage = client.suggestedRelanceMsg.ifBlank { null },
            onDismiss = { modalState.selectedClientForRelance = null }
        )
    }

    modalState.articleToEditInDialog?.let { articleInDialog ->
        EditCatalogueArticleDialog(
            article = articleInDialog,
            availableFamilies = uiState.catalogueFamilies,
            availablePlatforms = uiState.cataloguePlatforms.filter { it.isActive }.map { it.name },
            transitaires = uiState.catalogueTransitaires,
            cmupRate = uiState.cmupRate,
            onDismiss = { modalState.articleToEditInDialog = null },
            onConfirm = { newProd, newSku, newFam, newPlat, newPriceYuan, newResaleAr, newWeightKg, newFreightMode, newVariants, newImageUrl, newNotes, isHidden ->
                viewModel.updateCatalogueArticleFull(
                    article = articleInDialog,
                    newProduct = newProd,
                    newReferenceCode = newSku,
                    newFamily = newFam,
                    newPlatform = newPlat,
                    newPriceYuan = newPriceYuan,
                    newResaleAr = newResaleAr,
                    newWeightKg = newWeightKg,
                    newFreightMode = newFreightMode,
                    newVariants = newVariants,
                    newImageUrl = newImageUrl,
                    newNotes = newNotes,
                    isHidden = isHidden
                )
                modalState.articleToEditInDialog = null
            }
        )
    }

    modalState.orderToAdjust?.let { order ->
        AdjustStockDialog(
            order = order,
            onDismiss = { modalState.orderToAdjust = null },
            onConfirm = { sCount, rCount, dCount, client ->
                viewModel.adjustStock(order, sCount, rCount, dCount, client)
                modalState.orderToAdjust = null
            }
        )
    }

    if (modalState.showSelectOrderToAdjustDialog) {
        SelectOrderToAdjustDialog(
            orders = uiState.visibleStockOrders,
            onDismiss = { modalState.showSelectOrderToAdjustDialog = false },
            onOrderSelected = { selected ->
                modalState.orderToAdjust = selected
                modalState.showSelectOrderToAdjustDialog = false
            }
        )
    }

    modalState.saleToRecordPayment?.let { sale ->
        RecordSalePaymentDialog(
            sale = sale,
            accounts = businessAccounts,
            onDismiss = { modalState.saleToRecordPayment = null },
            onConfirmPayment = { additionalPayment, accountName ->
                viewModel.recordSalePayment(sale, additionalPayment, accountName = accountName)
                modalState.saleToRecordPayment = null
            },
            onDeleteSale = {
                viewModel.deleteSale(sale)
                modalState.saleToRecordPayment = null
            }
        )
    }

    if (modalState.showAddSaleDialog) {
        AddSaleDialog(
            availableStockOrders = uiState.visibleStockOrders,
            initialArticle = modalState.initialArticleForSale ?: primaryOrderFallback,
            accounts = businessAccounts,
            onDismiss = {
                modalState.showAddSaleDialog = false
                modalState.initialArticleForSale = null
            },
            onConfirm = { prod, client, phone, q, price, cost, paid, source, dom, linkedOrderId, accountName ->
                viewModel.addNewSale(prod, client, phone, q, price, cost, paid, source, dom, linkedOrderId, accountName)
                modalState.showAddSaleDialog = false
                modalState.initialArticleForSale = null
            }
        )
    }

    liveOrder?.let { order ->
        LogisticWizardScreen(
            order = order,
            cmupRate = uiState.cmupRate,
            availableYuans = uiState.availableYuans,
            transitaires = uiState.catalogueTransitaires.filter { !it.isArchived },
            families = uiState.catalogueFamilies,
            accounts = businessAccounts,
            onSpendYuans = { amount, rate, yuanAccountName, note, onResult ->
                viewModel.spendYuans(amount, rate, yuanAccountName, note, orderId = order.id, onResult = onResult)
            },
            onRefundYuans = { amount, rate, yuanAccountName, note ->
                viewModel.refundYuans(amount, rate, yuanAccountName, note, orderId = order.id)
            },
            onBuyYuans = { amount, rate, platform, sourceAccountName ->
                viewModel.buyYuans(amount, rate, platform, sourceAccountName)
            },
            onPayOrderFreight = { ord, amountAr, accName ->
                viewModel.payOrderFreight(ord, amountAr, accName)
            },
            onDismiss = { modalState.selectedOrderId = null },
            onDeleteOrder = {
                viewModel.deleteStockOrder(it)
                modalState.selectedOrderId = null
            },
            onUpdateOrder = { updated -> viewModel.updateBusinessOrder(updated) },
            onViewStock = {
                viewModel.onSelectSubTab("Stock")
                modalState.selectedOrderId = null
                modalState.selectedArticleKey = null
            },
            onCreateSale = {
                viewModel.onSelectSubTab("Ventes")
                modalState.selectedOrderId = null
                modalState.selectedArticleKey = null
            },
            onReorder = {
                viewModel.onSelectSubTab("Achats")
                modalState.selectedOrderId = null
                modalState.selectedArticleKey = null
            }
        )
    }

    if (modalState.showAddClientDialog) {
        AddClientDialog(
            onDismiss = { modalState.showAddClientDialog = false },
            onConfirm = { name, category, phone, initialAr ->
                viewModel.addClient(name, category, phone, initialAr)
                modalState.showAddClientDialog = false
            }
        )
    }

    modalState.selectedClientForDetail?.let { client ->
        ClientDetailDialog(
            client = client,
            onDismiss = { modalState.selectedClientForDetail = null },
            onRecordPayment = { modalState.clientToRecordPayment = client },
            onWhatsAppRelance = { modalState.selectedClientForRelance = client }
        )
    }

    modalState.clientToRecordPayment?.let { client ->
        RecordClientPaymentDialog(
            client = client,
            accounts = businessAccounts,
            onDismiss = { modalState.clientToRecordPayment = null },
            onConfirm = { amountAr, accountName ->
                viewModel.recordClientPayment(client, amountAr, accountName)
                modalState.clientToRecordPayment = null
            }
        )
    }

    if (modalState.showNotificationsDialog) {
        BusinessNotificationsDialog(
            notifications = uiState.businessNotifications,
            onDismiss = { modalState.showNotificationsDialog = false },
            onNotificationClick = { notif ->
                modalState.showNotificationsDialog = false
                viewModel.onSelectSubTab(notif.targetSubTab)
                notif.orderId?.let { modalState.selectedOrderId = it }
                notif.clientName?.let { cName ->
                    modalState.openClientRelanceByName(uiState.clientsList, cName, 0L, "")
                }
            }
        )
    }
}
