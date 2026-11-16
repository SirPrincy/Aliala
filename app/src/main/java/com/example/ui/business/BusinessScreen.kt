package com.example.ui.business

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.business.catalogue.ArticleDetailScreen
import com.example.ui.business.catalogue.CatalogueView
import com.example.ui.navigation.AppBottomNavBar

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
    val businessInvestments by viewModel.businessInvestments.collectAsStateWithLifecycle()
    val businessDebts by viewModel.businessDebts.collectAsStateWithLifecycle()
    val businessDebtPayments by viewModel.businessDebtPayments.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val modalState = rememberBusinessModalState()

    val selectedArticle = uiState.allCatalogueArticles.firstOrNull {
        it.articleKey == modalState.selectedArticleKey
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    selectedArticle?.let { article ->
        val primaryOrder = article.primaryOrder
        BackHandler {
            if (modalState.selectedOrderId != null) {
                modalState.selectedOrderId = null
            } else {
                modalState.selectedArticleKey = null
            }
        }
        ArticleDetailScreen(
            article = article,
            transitaires = uiState.catalogueTransitaires,
            cmupRate = uiState.cmupRate,
            onBackClick = { modalState.selectedArticleKey = null },
            onBuyClick = { art ->
                modalState.initialArticleForOrder = art
                modalState.showAddOrderDialog = true
            },
            onSellClick = { art ->
                modalState.initialArticleForSale = art
                modalState.showAddSaleDialog = true
            },
            onAdjustStockClick = { art -> modalState.orderToAdjust = art },
            onEditArticleClick = { art -> modalState.articleToEditInDialog = art },
            onToggleHideArticle = { art -> viewModel.toggleHideCatalogueArticle(art) },
            onDuplicateArticle = { art -> viewModel.duplicateBusinessOrder(art) },
            onDeleteArticle = { art ->
                viewModel.deleteCatalogueArticle(art)
                modalState.selectedArticleKey = null
            },
            onUpdateVisual = { newVisual ->
                viewModel.updateCatalogueArticleFull(
                    article = article,
                    newProduct = primaryOrder.product,
                    newReferenceCode = primaryOrder.referenceCode,
                    newFamily = primaryOrder.family,
                    newPlatform = primaryOrder.platform,
                    newPriceYuan = article.latestPriceYuan,
                    newResaleAr = article.resalePriceAr,
                    newWeightKg = primaryOrder.weightKg,
                    newFreightMode = primaryOrder.freightMode,
                    newVariants = primaryOrder.variants,
                    newImageUrl = newVisual,
                    newNotes = primaryOrder.receptionNotes,
                    isHidden = article.isHidden
                )
            },
            onUpdateVariants = { newVariants ->
                viewModel.updateCatalogueArticleFull(
                    article = article,
                    newProduct = primaryOrder.product,
                    newReferenceCode = primaryOrder.referenceCode,
                    newFamily = primaryOrder.family,
                    newPlatform = primaryOrder.platform,
                    newPriceYuan = article.latestPriceYuan,
                    newResaleAr = article.resalePriceAr,
                    newWeightKg = primaryOrder.weightKg,
                    newFreightMode = primaryOrder.freightMode,
                    newVariants = newVariants,
                    newImageUrl = primaryOrder.imageUrl,
                    newNotes = primaryOrder.receptionNotes,
                    isHidden = article.isHidden
                )
            },
            onLotClick = { lot -> modalState.selectedOrderId = lot.id }
        )

        BusinessModalHost(
            modalState = modalState,
            viewModel = viewModel,
            uiState = uiState,
            businessAccounts = businessAccounts,
            primaryOrderFallback = primaryOrder
        )
        return
    }

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
            onStockFilterSelected = { viewModel.onCatalogueStockFilterSelected(it) },
            onToggleShowHidden = { viewModel.onToggleCatalogueShowHidden() },
            onSortChanged = { viewModel.onCatalogueSortChanged(it) },
            onViewModeChanged = { viewModel.onCatalogueViewModeChanged(it) },
            onArticleClick = { modalState.selectedArticleKey = it.articleKey },
            onEditArticleClick = { modalState.articleToEditInDialog = it },
            onQuickRestockArticle = { art ->
                modalState.initialArticleForOrder = art.primaryOrder
                modalState.showAddOrderDialog = true
            },
            onToggleHideArticle = { viewModel.toggleHideCatalogueArticle(it) },
            onDeleteArticle = { viewModel.deleteCatalogueArticle(it) },
            onNewArticleClick = { modalState.showAddCatalogueArticleDialog = true },
            onNewFamilyClick = { modalState.showAddFamilyDialog = true },
            onCreateFamily = { name, emoji, color -> viewModel.addCatalogueFamily(name, emoji, color) },
            onTogglePlatformActive = { id, active -> viewModel.togglePlatformActive(id, active) },
            onUpdatePlatformShippingTag = { id, tag -> viewModel.updatePlatformShippingTag(id, tag) },
            onCreatePlatform = { name, sub, ship, sec, icon, color ->
                viewModel.addCataloguePlatform(name, sub, ship, sec, icon, color)
            },
            onDeletePlatform = { id -> viewModel.deleteCataloguePlatform(id) },
            onCreateTransitaire = { name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays ->
                viewModel.addCatalogueTransitaire(
                    name, hub, airRate, seaRate, contact, address, isActive,
                    dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays
                )
            },
            onUpdateTransitaire = { id, name, hub, airRate, seaRate, contact, address, isActive, dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays ->
                viewModel.updateCatalogueTransitaire(
                    id, name, hub, airRate, seaRate, contact, address, isActive,
                    dateApp, airLead, seaLead, notes, airFastRate, airFastLead, goodsDelays
                )
            },
            onToggleTransitaireArchived = { id -> viewModel.toggleTransitaireArchived(id) },
            onDeleteTransitaire = { id -> viewModel.deleteCatalogueTransitaire(id) },
            onUpdateTransitaireRates = { id, air, sea -> viewModel.updateTransitaireRates(id, air, sea) },
            onRecordTransitairePayment = { id, amount, accName ->
                viewModel.recordTransitairePayment(id, amount, accName)
            },
            accounts = businessAccounts,
            modifier = modifier
        )

        BusinessModalHost(
            modalState = modalState,
            viewModel = viewModel,
            uiState = uiState,
            businessAccounts = businessAccounts
        )
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
            AppBottomNavBar(selectedTab = 3, onTabSelected = onTabSelected)
        },
        floatingActionButton = {
            BusinessScaffoldFab(uiState = uiState, modalState = modalState)
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
            BusinessHeaderBar(
                selectedSubTab = uiState.selectedSubTab,
                notificationsCount = uiState.businessNotifications.size,
                onOpenCatalogue = { viewModel.openCatalogue() },
                onOpenNotifications = { modalState.showNotificationsDialog = true }
            )

            BusinessSubTabsRow(
                selectedSubTab = uiState.selectedSubTab,
                onSelectSubTab = { viewModel.onSelectSubTab(it) }
            )

            BusinessTabContent(
                uiState = uiState,
                viewModel = viewModel,
                modalState = modalState,
                businessAccounts = businessAccounts,
                businessTransactions = businessTransactions,
                businessInvestments = businessInvestments,
                businessDebts = businessDebts,
                businessDebtPayments = businessDebtPayments
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    BusinessModalHost(
        modalState = modalState,
        viewModel = viewModel,
        uiState = uiState,
        businessAccounts = businessAccounts
    )
}
