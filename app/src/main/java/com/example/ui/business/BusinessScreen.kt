package com.example.ui.business

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessOrderEntity
import com.example.data.local.entity.BusinessSaleEntity
import com.example.ui.business.components.AddCatalogueArticleDialog
import com.example.ui.business.components.AddClientDialog
import com.example.ui.business.components.AddFamilyDialog
import com.example.ui.business.components.AddOrderDialog
import com.example.ui.business.components.AddSaleDialog
import com.example.ui.business.components.NewActionMenuSheet
import com.example.ui.business.components.AdjustStockDialog
import com.example.ui.business.components.AiInsightCard
import com.example.ui.business.components.ArticleDetailScreen
import com.example.ui.business.components.BusinessHeroCard
import com.example.ui.business.components.BuyYuansDialog
import com.example.ui.business.components.CashAlertsRow
import com.example.ui.business.components.CatalogueView
import com.example.ui.business.components.ClientDetailDialog
import com.example.ui.business.components.ClientsView
import com.example.ui.business.components.EditCatalogueArticleDialog
import com.example.ui.business.components.FamilyServiceCard
import com.example.ui.business.components.FinancesView
import com.example.ui.business.components.IncomingArrivalsSection
import com.example.ui.business.components.LogisticWizardScreen
import com.example.ui.business.components.OrderDetailDialog
import com.example.ui.business.components.PayPlatformDialog
import com.example.ui.business.components.PipelineSection
import com.example.ui.business.components.PurchasesDonutSection
import com.example.ui.business.components.PurchasesView
import com.example.ui.business.components.RecordSalePaymentDialog
import com.example.ui.business.components.SalesView
import com.example.ui.business.components.SelectOrderToAdjustDialog
import com.example.ui.business.components.StockView
import com.example.ui.business.components.TopSalesSection
import com.example.ui.business.components.WhatsAppRelanceDialog
import com.example.ui.business.components.YuanWalletCard
import com.example.ui.navigation.AppBottomNavBar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PlusJakartaSans

@Composable
fun BusinessScreen(
    viewModel: BusinessViewModel,
    onNavigateBack: () -> Unit,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val businessAccounts by viewModel.businessAccounts.collectAsStateWithLifecycle()
    val businessTransactions by viewModel.businessTransactions.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val subTabsScrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Dialog states
    var showBuyYuansDialog by remember { mutableStateOf(false) }
    var showPayPlatformDialog by remember { mutableStateOf(false) }
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var showNewActionMenuSheet by remember { mutableStateOf(false) }
    var showWhatsAppRelanceDialog by remember { mutableStateOf(false) }
    var orderToAdjust by remember { mutableStateOf<BusinessOrderEntity?>(null) }
    var showSelectOrderToAdjustDialog by remember { mutableStateOf(false) }
    var saleToRecordPayment by remember { mutableStateOf<BusinessSaleEntity?>(null) }
    var showAddSaleDialog by remember { mutableStateOf(false) }
    var articleToEdit by remember { mutableStateOf<BusinessOrderEntity?>(null) }
    var articleToEditInDialog by remember { mutableStateOf<BusinessOrderEntity?>(null) }
    var selectedClientForRelance by remember { mutableStateOf<BusinessClientUi?>(null) }
    var selectedClientForDetail by remember { mutableStateOf<BusinessClientUi?>(null) }
    var showAddCatalogueArticleDialog by remember { mutableStateOf(false) }
    var showAddFamilyDialog by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf<Long?>(null) }
    val liveOrder = uiState.orders.firstOrNull { it.id == selectedOrderId }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // -------------------------------------------------------------
    // VUE DÉTAIL ARTICLE (Catalogue ou Stock)
    // -------------------------------------------------------------
    articleToEdit?.let { article ->
        BackHandler {
            articleToEdit = null
        }
        ArticleDetailScreen(
            article = article,
            onBackClick = { articleToEdit = null },
            onBuyClick = {
                showAddOrderDialog = true
                articleToEdit = null
            },
            onSellClick = {
                showAddSaleDialog = true
                articleToEdit = null
            },
            onAdjustStockClick = {
                orderToAdjust = it
                articleToEdit = null
            },
            onEditArticleClick = {
                articleToEditInDialog = it
            },
            onToggleHideArticle = {
                viewModel.toggleHideStockItem(it)
            },
            onDeleteArticle = {
                viewModel.deleteStockOrder(it)
                articleToEdit = null
            }
        )
        return
    }

    // -------------------------------------------------------------
    // VUE CATALOGUE PLEIN ÉCRAN
    // -------------------------------------------------------------
    if (uiState.isCatalogueOpen) {
        BackHandler {
            viewModel.closeCatalogue()
        }
        CatalogueView(
            uiState = uiState,
            onBackClick = { viewModel.closeCatalogue() },
            onSubTabSelected = { viewModel.onCatalogueSubTabSelected(it) },
            onSearchQueryChanged = { viewModel.onCatalogueSearchQueryChanged(it) },
            onFamilyFilterSelected = { viewModel.onCatalogueFamilyFilterSelected(it) },
            onToggleShowHidden = { viewModel.onToggleCatalogueShowHidden() },
            onSortChanged = { viewModel.onCatalogueSortChanged(it) },
            onViewModeChanged = { viewModel.onCatalogueViewModeChanged(it) },
            onArticleClick = { articleToEdit = it },
            onToggleHideArticle = { viewModel.toggleHideStockItem(it) },
            onNewArticleClick = { showAddCatalogueArticleDialog = true },
            onNewFamilyClick = { showAddFamilyDialog = true },
            onCreateFamily = { name, emoji, color -> viewModel.addCatalogueFamily(name, emoji, color) },
            onTogglePlatformActive = { id, active -> viewModel.togglePlatformActive(id, active) },
            onCreatePlatform = { name, sub, ship, sec, icon, color -> viewModel.addCataloguePlatform(name, sub, ship, sec, icon, color) },
            onDeletePlatform = { id -> viewModel.deleteCataloguePlatform(id) },
            onCreateTransitaire = { name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes ->
                viewModel.addCatalogueTransitaire(name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes)
            },
            onToggleTransitaireArchived = { id -> viewModel.toggleTransitaireArchived(id) },
            onDeleteTransitaire = { id -> viewModel.deleteCatalogueTransitaire(id) },
            onUpdateTransitaireRates = { id, air, sea -> viewModel.updateTransitaireRates(id, air, sea) },
            onRecordTransitairePayment = { id, amount -> viewModel.recordTransitairePayment(id, amount) },
            modifier = modifier
        )

        if (showAddCatalogueArticleDialog) {
            AddCatalogueArticleDialog(
                availableFamilies = uiState.catalogueFamilies.map { it.name },
                onDismiss = { showAddCatalogueArticleDialog = false },
                onConfirm = { prod, fam, sku, vars, pYuan, rAr, sCount, wKg, plat ->
                    viewModel.addCatalogueArticle(prod, fam, sku, vars, pYuan, rAr, sCount, wKg, plat)
                    showAddCatalogueArticleDialog = false
                }
            )
        }

        if (showAddFamilyDialog) {
            AddFamilyDialog(
                onDismiss = { showAddFamilyDialog = false },
                onConfirm = { famName ->
                    viewModel.addCatalogueFamily(famName)
                    viewModel.onCatalogueFamilyFilterSelected(famName)
                    showAddFamilyDialog = false
                }
            )
        }

        return
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavBar(
                selectedTab = 3, // Business tab
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            if (!uiState.isCatalogueOpen) {
                when (uiState.selectedSubTab) {
                    "Achats" -> {
                        ExtendedFloatingActionButton(
                            onClick = { showAddOrderDialog = true },
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
                            onClick = { showAddSaleDialog = true },
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
                            onClick = { showAddClientDialog = true },
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
                            onClick = { showSelectOrderToAdjustDialog = true },
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
                            onClick = { showNewActionMenuSheet = true },
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. TOP HEADER (Module Business / Header avec bouton Catalogue orange 48dp)
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
                            imageVector = if (uiState.selectedSubTab in listOf("Stock", "Ventes")) Icons.Default.Storefront else Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (uiState.selectedSubTab in listOf("Stock", "Ventes")) "MODULE BUSINESS" else "ACCUEIL",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF712AE2),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = when (uiState.selectedSubTab) {
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
                    // Bouton Catalogue 48px touch target avec teinte orange douce
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.openCatalogue() }
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

                    IconButton(
                        onClick = { Toast.makeText(context, "Pas de nouvelle notification", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
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

            // 2. HORIZONTAL SUB-TABS (Aperçu, Achats, Stock, Ventes, Clients, Finances - 6 sous-onglets préservés)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(subTabsScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Aperçu",
                    "Achats",
                    "Stock",
                    "Ventes",
                    "Clients",
                    "Finances"
                ).forEach { tabName ->
                    val isSelected = uiState.selectedSubTab == tabName
                    val isAchatsTab = tabName == "Achats"

                    if (isAchatsTab && isSelected) {
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFFF97316).copy(alpha = 0.15f))
                                .clickable { viewModel.onSelectSubTab(tabName) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Achats",
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFEA580C)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(
                                    if (isSelected) CyanPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { viewModel.onSelectSubTab(tabName) }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabName,
                                fontFamily = PlusJakartaSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. MAIN CONTENT: Switch between "Stock", "Ventes", "Achats", and "Aperçu" view
            when (uiState.selectedSubTab) {
                "Stock" -> {
                    // STOCK DEDICATED VIEW (Avec bouton Gérer le catalogue et redirection masqués)
                    StockView(
                        uiState = uiState,
                        onSegmentSelected = { viewModel.onStockSegmentSelected(it) },
                        onSearchQueryChanged = { viewModel.onStockSearchQueryChanged(it) },
                        onFamilyFilterSelected = { viewModel.onStockFamilyFilterSelected(it) },
                        onToggleShowHidden = { viewModel.onToggleShowHiddenStock() },
                        onAdjustStockClick = { orderToAdjust = it },
                        onToggleHideItem = { viewModel.toggleHideStockItem(it) },
                        onDeleteItem = { viewModel.deleteStockOrder(it) },
                        onCorrectStockFabClick = { showSelectOrderToAdjustDialog = true },
                        onArticleClick = { articleToEdit = it },
                        onOpenCatalogue = { viewModel.openCatalogue() },
                        onOpenCatalogueWithHidden = { viewModel.openCatalogue(showHidden = true) }
                    )
                }
                "Ventes" -> {
                    // VENTES DEDICATED VIEW
                    SalesView(
                        uiState = uiState,
                        onPrevMonth = { viewModel.onPrevMonth() },
                        onNextMonth = { viewModel.onNextMonth() },
                        onDomainSelected = { viewModel.onSalesDomainSelected(it) },
                        onStatusFilterSelected = { viewModel.onSalesStatusFilterSelected(it) },
                        onSearchQueryChanged = { viewModel.onSalesSearchQueryChanged(it) },
                        onSaleClick = { saleToRecordPayment = it },
                        onNewSaleClick = { showAddSaleDialog = true },
                        onViewLinkedOrder = {
                            viewModel.onSelectSubTab("Achats")
                        }
                    )
                }
                "Achats" -> {
                    // ACHATS DEDICATED VIEW
                    PurchasesView(
                        uiState = uiState,
                        onStatusFilterSelected = { viewModel.onSelectPurchasesStatusFilter(it) },
                        onCategoryFilterSelected = { viewModel.onSelectPurchasesCategoryFilter(it) },
                        onSortFilterSelected = { viewModel.onSelectPurchasesSortFilter(it) },
                        onNewPurchaseClick = { showAddOrderDialog = true },
                        onBuyYuansClick = { showBuyYuansDialog = true },
                        onAdvanceQualityControl = { viewModel.onAdvanceQualityControl(it) },
                        onOrderClick = { order ->
                            selectedOrderId = order.id
                        }
                    )
                }
                "Clients" -> {
                    // CLIENTS DEDICATED VIEW
                    ClientsView(
                        uiState = uiState,
                        onFilterSelected = { viewModel.onClientsFilterSelected(it) },
                        onSearchQueryChanged = { viewModel.onClientsSearchQueryChanged(it) },
                        onClientClick = { client ->
                            selectedClientForDetail = client
                        },
                        onNewClientClick = { showAddClientDialog = true },
                        onRecordPayment = { client ->
                            viewModel.recordClientPayment(client.id, client.balanceDueAr)
                        },
                        onWhatsAppRelance = { client ->
                            selectedClientForRelance = client
                        }
                    )
                }
                "Finances" -> {
                    FinancesView(
                        uiState = uiState,
                        accounts = businessAccounts,
                        transactions = businessTransactions,
                        onAddAccount = { name, sub, bal -> viewModel.addBusinessAccount(name, sub, bal) },
                        onAddTransaction = { title, type, amountAr, accountName, ref ->
                            viewModel.addBusinessTransaction(title, type, amountAr, accountName, ref)
                        },
                        onPrevMonth = { viewModel.onPrevMonth() },
                        onNextMonth = { viewModel.onNextMonth() },
                        onBuyYuansClick = { showBuyYuansDialog = true },
                        onPay1688Click = { showPayPlatformDialog = true },
                        onNewOperationClick = { showNewActionMenuSheet = true },
                        onRecordTransitairePayment = {
                            Toast.makeText(context, "Paiement transitaire disponible depuis le Catalogue > Transitaires", Toast.LENGTH_SHORT).show()
                        },
                        onWhatsAppRelance = { _ ->
                            showWhatsAppRelanceDialog = true
                        }
                    )
                }

                else -> {
                    // APERÇU OVERVIEW VIEW
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
                                IconButton(
                                    onClick = { viewModel.onPrevMonth() },
                                    modifier = Modifier.size(28.dp)
                                ) {
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

                                IconButton(
                                    onClick = { viewModel.onNextMonth() },
                                    modifier = Modifier.size(28.dp)
                                ) {
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

                    // Hero Card: Bénéfice net du mois
                    BusinessHeroCard(uiState = uiState)

                    // Yuan Wallet Card
                    YuanWalletCard(
                        uiState = uiState,
                        onBuyYuansClick = { showBuyYuansDialog = true },
                        onPayPlatformClick = { showPayPlatformDialog = true }
                    )

                    // Cash Alerts
                    CashAlertsRow(
                        uiState = uiState,
                        onFreightClick = {
                            Toast.makeText(context, "Détail fret : 85 000 Ar dû sous 3 jours", Toast.LENGTH_SHORT).show()
                        },
                        onClientsClick = {
                            showWhatsAppRelanceDialog = true
                        }
                    )

                    // Pipeline
                    PipelineSection(
                        uiState = uiState,
                        onViewAllClick = {
                            viewModel.onSelectSubTab("Achats")
                        }
                    )

                    // Prochains arrivages
                    IncomingArrivalsSection(
                        parcels = uiState.incomingParcels,
                        onParcelClick = { parcel ->
                            val matched = uiState.visibleCatalogueOrders.find { it.id == parcel.id }
                                ?: uiState.visibleStockOrders.find { it.id == parcel.id }
                                ?: uiState.filteredPurchases.find { it.id == parcel.id }
                            if (matched != null) {
                                selectedOrderId = matched.id
                            } else {
                                Toast.makeText(context, "${parcel.productName} • ${parcel.delayBadge}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // Top Ventes
                    TopSalesSection(topProducts = uiState.topProducts)

                    // Répartition Donut
                    PurchasesDonutSection(uiState = uiState)

                    // Service Proches & Famille
                    FamilyServiceCard(profitAr = uiState.familyServiceProfitAr)

                    // Insight IA Trésorerie
                    AiInsightCard(
                        uiState = uiState,
                        onGenerateWhatsAppClick = { showWhatsAppRelanceDialog = true },
                        onNextInsightClick = { viewModel.onNextInsight() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showBuyYuansDialog) {
        BuyYuansDialog(
            onDismiss = { showBuyYuansDialog = false },
            onConfirm = { amountYuan, rateAr, platform ->
                viewModel.buyYuans(amountYuan, rateAr, platform)
            }
        )
    }

    if (showPayPlatformDialog) {
        PayPlatformDialog(
            cmupRate = uiState.cmupRate,
            availableYuans = uiState.availableYuans,
            onDismiss = { showPayPlatformDialog = false },
            onConfirm = { amountYuan, rateAr, platform, note ->
                viewModel.spendYuans(amountYuan, rateAr, platform, note)
            }
        )
    }

    if (showNewActionMenuSheet) {
        NewActionMenuSheet(
            onDismiss = { showNewActionMenuSheet = false },
            onNewArticleClick = { showAddCatalogueArticleDialog = true },
            onNewPurchaseClick = { showAddOrderDialog = true },
            onNewSaleClick = { showAddSaleDialog = true }
        )
    }

    if (showAddOrderDialog) {
        AddOrderDialog(
            cmupRate = uiState.cmupRate,
            availableYuans = uiState.availableYuans,
            availableArticles = uiState.orders,
            onDismiss = { showAddOrderDialog = false },
            onConfirm = { prod, supp, q, pY, rAr, cat, carr, varStr, fAr ->
                viewModel.addBusinessOrder(prod, supp, q, pY, rAr, cat, carr, varStr, fAr)
            }
        )
    }

    if (showWhatsAppRelanceDialog) {
        WhatsAppRelanceDialog(
            onDismiss = { showWhatsAppRelanceDialog = false }
        )
    }

    selectedClientForRelance?.let { client ->
        WhatsAppRelanceDialog(
            clientName = client.name,
            clientPhone = client.phone,
            balanceDueAr = client.balanceDueAr,
            customMessage = if (client.suggestedRelanceMsg.isNotBlank()) client.suggestedRelanceMsg else null,
            onDismiss = { selectedClientForRelance = null }
        )
    }

    articleToEditInDialog?.let { article ->
        EditCatalogueArticleDialog(
            order = article,
            onDismiss = { articleToEditInDialog = null },
            onConfirm = { newPriceYuan, newResaleAr, newVariants, isHidden ->
                viewModel.updateCatalogueArticle(article, newPriceYuan, newResaleAr, newVariants, isHidden)
                articleToEditInDialog = null
            }
        )
    }

    // Stock Dialogs
    orderToAdjust?.let { order ->
        AdjustStockDialog(
            order = order,
            onDismiss = { orderToAdjust = null },
            onConfirm = { sCount, rCount, dCount, client ->
                viewModel.adjustStock(order, sCount, rCount, dCount, client)
                orderToAdjust = null
            }
        )
    }

    if (showSelectOrderToAdjustDialog) {
        SelectOrderToAdjustDialog(
            orders = uiState.visibleStockOrders,
            onDismiss = { showSelectOrderToAdjustDialog = false },
            onOrderSelected = { selected ->
                orderToAdjust = selected
                showSelectOrderToAdjustDialog = false
            }
        )
    }

    // Sales Dialogs
    saleToRecordPayment?.let { sale ->
        RecordSalePaymentDialog(
            sale = sale,
            onDismiss = { saleToRecordPayment = null },
            onConfirmPayment = { additionalPayment ->
                viewModel.recordSalePayment(sale, additionalPayment)
                saleToRecordPayment = null
            },
            onDeleteSale = {
                viewModel.deleteSale(sale)
                saleToRecordPayment = null
            }
        )
    }

    if (showAddSaleDialog) {
        AddSaleDialog(
            availableStockOrders = uiState.visibleStockOrders,
            onDismiss = { showAddSaleDialog = false },
            onConfirm = { prod, client, phone, q, price, cost, paid, source, dom, linkedOrderId ->
                viewModel.addNewSale(prod, client, phone, q, price, cost, paid, source, dom, linkedOrderId)
                showAddSaleDialog = false
            }
        )
    }

    liveOrder?.let { order ->
        LogisticWizardScreen(
            order = order,
            cmupRate = uiState.cmupRate,
            availableYuans = uiState.availableYuans,
            onSpendYuans = { amount, rate, note ->
                viewModel.spendYuans(amount, rate, "Alipay", note, orderId = order.id)
            },
            onRefundYuans = { amount, rate, note ->
                viewModel.refundYuans(amount, rate, "Alipay", note, orderId = order.id)
            },
            onDismiss = { selectedOrderId = null },
            onDeleteOrder = {
                viewModel.deleteStockOrder(it)
                selectedOrderId = null
            },
            onUpdateOrder = { updated ->
                viewModel.updateBusinessOrder(updated)
            },
            onViewStock = {
                viewModel.onSelectSubTab("Stock")
                selectedOrderId = null
            },
            onCreateSale = {
                viewModel.onSelectSubTab("Ventes")
                selectedOrderId = null
            },
            onReorder = {
                viewModel.onSelectSubTab("Achats")
                selectedOrderId = null
            }
        )
    }

    if (showAddClientDialog) {
        AddClientDialog(
            onDismiss = { showAddClientDialog = false },
            onConfirm = { name, category, phone, initialAr ->
                viewModel.addClient(name, category, phone, initialAr)
                showAddClientDialog = false
            }
        )
    }

    selectedClientForDetail?.let { client ->
        ClientDetailDialog(
            client = client,
            onDismiss = { selectedClientForDetail = null },
            onRecordPayment = { viewModel.recordClientPayment(client.id, client.balanceDueAr) },
            onWhatsAppRelance = { selectedClientForRelance = client }
        )
    }
}
