package com.example.ui.business

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BusinessAccountEntity
import com.example.data.local.entity.BusinessDebtEntity
import com.example.data.local.entity.BusinessDebtPaymentEntity
import com.example.data.local.entity.BusinessInvestmentEntity
import com.example.data.local.entity.BusinessTransactionEntity
import com.example.ui.business.clients.ClientsView
import com.example.ui.business.finances.FinancesView
import com.example.ui.business.overview.AiInsightCard
import com.example.ui.business.overview.BusinessHeroCard
import com.example.ui.business.overview.CashAlertsRow
import com.example.ui.business.overview.FamilyServiceCard
import com.example.ui.business.overview.IncomingArrivalsSection
import com.example.ui.business.overview.PipelineSection
import com.example.ui.business.overview.PurchasesDonutSection
import com.example.ui.business.overview.TopSalesSection
import com.example.ui.business.overview.YuanWalletCard
import com.example.ui.business.purchases.PurchasesView
import com.example.ui.business.sales.SalesView
import com.example.ui.business.stock.StockView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans
import com.example.util.CurrencyFormatter

@Composable
internal fun BusinessScaffoldFab(
    uiState: BusinessUiState,
    modalState: BusinessModalState
) {
    if (uiState.isCatalogueOpen) return
    when (uiState.selectedSubTab) {
        "Achats" -> {
            ExtendedFloatingActionButton(
                onClick = { modalState.showAddOrderDialog = true },
                containerColor = Color(0xFFF97316),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("btn_new_purchase"),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouvel achat", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) }
            )
        }
        "Ventes" -> {
            ExtendedFloatingActionButton(
                onClick = { modalState.showAddSaleDialog = true },
                containerColor = Color(0xFFF97316),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("fab_new_sale"),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouvelle vente", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) }
            )
        }
        "Clients" -> {
            ExtendedFloatingActionButton(
                onClick = { modalState.showAddClientDialog = true },
                containerColor = Color(0xFF712AE2),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("fab_nouveau_client"),
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Client", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) }
            )
        }
        "Stock" -> {
            ExtendedFloatingActionButton(
                onClick = { modalState.showSelectOrderToAdjustDialog = true },
                containerColor = Color(0xFFF97316),
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("fab_correct_stock"),
                icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                text = { Text("Corriger le stock", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) }
            )
        }
        else -> {
            FloatingActionButton(
                onClick = { modalState.showNewActionMenuSheet = true },
                containerColor = CyanPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .size(56.dp)
                    .testTag("business_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nouvel élément",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
internal fun BusinessHeaderBar(
    selectedSubTab: String,
    notificationsCount: Int,
    onOpenCatalogue: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (selectedSubTab in listOf("Stock", "Ventes")) Icons.Default.Storefront else Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = if (selectedSubTab in listOf("Stock", "Ventes")) "MODULE BUSINESS" else "ACCUEIL",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFF712AE2),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = when (selectedSubTab) {
                        "Stock" -> "Business Stock"
                        "Achats" -> "Business Achats"
                        "Ventes" -> "Business Ventes"
                        else -> "Tableau de Bord"
                    },
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenCatalogue)
                    .testTag("header_btn_catalogue"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = "Catalogue",
                        tint = Color(0xFFEA580C),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("header_btn_notifications")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (notificationsCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 6.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notificationsCount > 9) "9+" else notificationsCount.toString(),
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
internal fun BusinessSubTabsRow(
    selectedSubTab: String,
    onSelectSubTab: (String) -> Unit
) {
    val subTabsScrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(subTabsScrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Aperçu", "Achats", "Stock", "Ventes", "Clients", "Finances").forEach { tabName ->
            val isSelected = selectedSubTab == tabName
            val isAchatsSelected = tabName == "Achats" && isSelected

            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(
                        when {
                            isAchatsSelected -> Color(0xFFF97316).copy(alpha = 0.15f)
                            isSelected -> CyanPrimary
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .clickable { onSelectSubTab(tabName) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabName,
                    fontFamily = PlusJakartaSans,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = when {
                        isAchatsSelected -> Color(0xFFEA580C)
                        isSelected -> Color.White
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
internal fun BusinessTabContent(
    uiState: BusinessUiState,
    viewModel: BusinessViewModel,
    modalState: BusinessModalState,
    businessAccounts: List<BusinessAccountEntity>,
    businessTransactions: List<BusinessTransactionEntity>,
    businessInvestments: List<BusinessInvestmentEntity>,
    businessDebts: List<BusinessDebtEntity>,
    businessDebtPayments: List<BusinessDebtPaymentEntity>
) {
    val context = LocalContext.current

    when (uiState.selectedSubTab) {
        "Stock" -> {
            StockView(
                uiState = uiState,
                onSegmentSelected = { viewModel.onStockSegmentSelected(it) },
                onSearchQueryChanged = { viewModel.onStockSearchQueryChanged(it) },
                onFamilyFilterSelected = { viewModel.onStockFamilyFilterSelected(it) },
                onToggleShowHidden = { viewModel.onToggleShowHiddenStock() },
                onAdjustStockClick = { modalState.orderToAdjust = it },
                onToggleHideItem = { viewModel.toggleHideStockItem(it) },
                onDeleteItem = { viewModel.deleteStockOrder(it) },
                onCorrectStockFabClick = { modalState.showSelectOrderToAdjustDialog = true },
                onArticleClick = { order ->
                    val matched = uiState.allCatalogueArticles.firstOrNull { summary ->
                        summary.primaryOrder.id == order.id || summary.lots.any { it.id == order.id }
                    }
                    modalState.selectedArticleKey = matched?.articleKey
                },
                onOpenCatalogue = { viewModel.openCatalogue() },
                onOpenCatalogueWithHidden = { viewModel.openCatalogue(showHidden = true) },
                onInRouteOrderClick = { order -> modalState.selectedOrderId = order.id },
                onOpenLogistics = { viewModel.onSelectSubTab("Achats") }
            )
        }
        "Ventes" -> {
            SalesView(
                uiState = uiState,
                onPrevMonth = { viewModel.onPrevMonth() },
                onNextMonth = { viewModel.onNextMonth() },
                onDomainSelected = { viewModel.onSalesDomainSelected(it) },
                onStatusFilterSelected = { viewModel.onSalesStatusFilterSelected(it) },
                onSearchQueryChanged = { viewModel.onSalesSearchQueryChanged(it) },
                onSaleClick = { modalState.saleToRecordPayment = it },
                onNewSaleClick = { modalState.showAddSaleDialog = true },
                onViewLinkedOrder = { linkedOrder -> modalState.selectedOrderId = linkedOrder.id },
                onSortFilterSelected = { viewModel.onSalesSortFilterSelected(it) },
                onViewAllPendingOrders = { viewModel.onSelectSubTab("Achats") },
                onWhatsAppRelance = { sale ->
                    modalState.openClientRelanceByName(
                        clients = uiState.clientsList,
                        name = sale.clientName,
                        fallbackDue = (sale.totalPriceAr - sale.paidAmountAr).coerceAtLeast(0L),
                        fallbackPhone = sale.clientPhone
                    )
                }
            )
        }
        "Achats" -> {
            PurchasesView(
                uiState = uiState,
                onStatusFilterSelected = { viewModel.onSelectPurchasesStatusFilter(it) },
                onCategoryFilterSelected = { viewModel.onSelectPurchasesCategoryFilter(it) },
                onSortFilterSelected = { viewModel.onSelectPurchasesSortFilter(it) },
                onSearchQueryChanged = { viewModel.onPurchasesSearchQueryChanged(it) },
                onNewPurchaseClick = { modalState.showAddOrderDialog = true },
                onBuyYuansClick = { modalState.showBuyYuansDialog = true },
                onAdvanceQualityControl = { order -> modalState.selectedOrderId = order.id },
                onOrderClick = { order -> modalState.selectedOrderId = order.id },
                onEditOrder = { order -> modalState.orderToEdit = order },
                onDeleteOrder = { order -> viewModel.deleteStockOrder(order) }
            )
        }
        "Clients" -> {
            ClientsView(
                uiState = uiState,
                onFilterSelected = { viewModel.onClientsFilterSelected(it) },
                onSearchQueryChanged = { viewModel.onClientsSearchQueryChanged(it) },
                onClientClick = { client -> modalState.selectedClientForDetail = client },
                onNewClientClick = { modalState.showAddClientDialog = true },
                onRecordPayment = { client -> modalState.clientToRecordPayment = client },
                onWhatsAppRelance = { client -> modalState.selectedClientForRelance = client }
            )
        }
        "Finances" -> {
            FinancesView(
                uiState = uiState,
                accounts = businessAccounts,
                transactions = businessTransactions,
                investments = businessInvestments,
                debts = businessDebts,
                debtPayments = businessDebtPayments,
                onAddAccount = { name, sub, balAr, isYuan, balYuan ->
                    viewModel.addBusinessAccount(name, sub, balAr, isYuan, balYuan)
                },
                onUpdateAccount = { existing, newName, newSub, newBalAr, newBalYuan ->
                    viewModel.updateBusinessAccount(existing, newName, newSub, newBalAr, newBalYuan)
                },
                onDeleteAccount = { acc -> viewModel.deleteBusinessAccount(acc) },
                onAddTransaction = { title, type, amountAr, accountName, ref, dest ->
                    viewModel.addBusinessTransaction(title, type, amountAr, accountName, ref, dest)
                },
                onBuyYuansFromTx = { amountYuan, rateAr, platform, sourceAccountName, purchaseDateMillis ->
                    viewModel.buyYuans(amountYuan, rateAr, platform, sourceAccountName, purchaseDateMillis)
                },
                onDeleteTransaction = { tx -> viewModel.deleteBusinessTransaction(tx) },
                onRecordCapitalMovement = { title, isContrib, amountAr, accName, note, affectAccount, dateMillis ->
                    viewModel.recordCapitalMovement(title, isContrib, amountAr, accName, note, affectAccount, dateMillis)
                },
                onAddInvestment = { title, cat, amountAr, estValAr, returnAr, accName, notes, dateMillis ->
                    viewModel.addBusinessInvestment(title, cat, amountAr, estValAr, returnAr, accName, notes, dateMillis)
                },
                onUpdateInvestment = { viewModel.updateBusinessInvestment(it) },
                onDeleteInvestment = { viewModel.deleteBusinessInvestment(it) },
                onAddDebt = { title, dir, counterparty, principal, interest, dueMs, createdMs, freq, accName, affectAcc, notes ->
                    viewModel.addBusinessDebt(title, dir, counterparty, principal, interest, dueMs, createdMs, freq, accName, affectAcc, notes)
                },
                onRecordDebtPayment = { debt, amount, accName, affectAcc, dateMs, note ->
                    viewModel.recordBusinessDebtPayment(debt, amount, accName, affectAcc, dateMs, note)
                },
                onUpdateDebt = { viewModel.updateBusinessDebt(it) },
                onDeleteDebt = { viewModel.deleteBusinessDebt(it) },
                onPrevMonth = { viewModel.onPrevMonth() },
                onNextMonth = { viewModel.onNextMonth() },
                onBuyYuansClick = { modalState.showBuyYuansDialog = true },
                onPay1688Click = { modalState.showPayPlatformDialog = true },
                onNewOperationClick = { modalState.showNewActionMenuSheet = true },
                onRecordTransitairePayment = { order, amountAr, accName ->
                    viewModel.payOrderFreight(order, amountAr, accName)
                },
                onWhatsAppRelance = { clientName ->
                    val matchingSales = uiState.sales.filter {
                        it.clientName.equals(clientName, ignoreCase = true) && !it.isFullyPaid
                    }
                    val totalDue = matchingSales.sumOf { (it.totalPriceAr - it.paidAmountAr).coerceAtLeast(0L) }
                    val phone = matchingSales.firstOrNull { it.clientPhone.isNotBlank() }?.clientPhone ?: ""
                    modalState.openClientRelanceByName(uiState.clientsList, clientName, totalDue, phone)
                }
            )
        }
        else -> {
            BusinessOverviewTabBody(
                uiState = uiState,
                viewModel = viewModel,
                modalState = modalState,
                onParcelClick = { parcel ->
                    val matched = uiState.orders.find { it.id == parcel.id }
                        ?: uiState.visibleStockOrders.find { it.id == parcel.id }
                        ?: uiState.filteredPurchases.find { it.id == parcel.id }
                    if (matched != null) {
                        modalState.selectedOrderId = matched.id
                    } else {
                        Toast.makeText(context, "${parcel.productName} • ${parcel.delayBadge}", Toast.LENGTH_SHORT).show()
                    }
                },
                onFreightAlertClick = {
                    viewModel.onSelectSubTab("Finances")
                    val msg = if (uiState.freightDueAmount > 0L) {
                        "Fret transitaire dû : ${CurrencyFormatter.formatAriary(uiState.freightDueAmount)}"
                    } else {
                        "Aucune facture transitaire en attente"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun BusinessOverviewTabBody(
    uiState: BusinessUiState,
    viewModel: BusinessViewModel,
    modalState: BusinessModalState,
    onParcelClick: (IncomingParcelUi) -> Unit,
    onFreightAlertClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("month_selector_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.onResetCurrentMonth() }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mois en cours",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.onPrevMonth() }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mois précédent",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = uiState.selectedMonthLabel,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(onClick = { viewModel.onNextMonth() }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Mois suivant",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    BusinessHeroCard(uiState = uiState)

    YuanWalletCard(
        uiState = uiState,
        onBuyYuansClick = { modalState.showBuyYuansDialog = true },
        onPayPlatformClick = { modalState.showPayPlatformDialog = true }
    )

    CashAlertsRow(
        uiState = uiState,
        onFreightClick = onFreightAlertClick,
        onClientsClick = { modalState.openOverdueClientRelance(uiState) }
    )

    PipelineSection(
        uiState = uiState,
        onViewAllClick = { viewModel.onSelectSubTab("Achats") }
    )

    IncomingArrivalsSection(
        parcels = uiState.incomingParcels,
        onParcelClick = onParcelClick
    )

    TopSalesSection(topProducts = uiState.topProducts)

    PurchasesDonutSection(uiState = uiState)

    FamilyServiceCard(profitAr = uiState.familyServiceProfitAr)

    AiInsightCard(
        uiState = uiState,
        onGenerateWhatsAppClick = { modalState.openOverdueClientRelance(uiState) },
        onNextInsightClick = { viewModel.onNextInsight() }
    )
}
