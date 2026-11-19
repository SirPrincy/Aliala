package com.example.ui.business

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.business.detail.LotDetailScreen
import com.example.ui.business.detail.ProductDetailScreen
import com.example.ui.business.detail.ReferentielScreen
import com.example.ui.business.detail.TransitaireDetailScreen
import com.example.ui.business.shared.BusinessResponsiveContainer
import com.example.ui.business.shared.TransitaireColors
import com.example.ui.business.shared.rememberTransitaireColors
import com.example.ui.business.sheets.BusinessSheetHost
import com.example.ui.business.tabs.ClientsTab
import com.example.ui.business.tabs.FinancesTab
import com.example.ui.business.tabs.OverviewTab
import com.example.ui.business.tabs.PurchasesTab
import com.example.ui.business.tabs.SalesTab
import com.example.ui.business.tabs.StockTab
import com.example.ui.navigation.AppBottomNavBar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Inter
import com.example.ui.theme.PlusJakartaSans
import com.example.ui.theme.VioletSecondary

private val BUSINESS_SUB_TABS = listOf("Aperçu", "Achats", "Stock", "Ventes", "Clients", "Finances")

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
    val businessCategories by viewModel.businessCategories.collectAsStateWithLifecycle()

    val colors = rememberTransitaireColors()
    val snackbarHostState = remember { SnackbarHostState() }
    val navState = rememberBusinessNavigationState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Routage plat Level+1 (maximum 1 écran détail à la fois)
    when (val dest = navState.detailDestination) {
        is BusinessDetailDestination.LotDetail -> {
            val order = uiState.orders.firstOrNull { it.id == dest.orderId }
            if (order != null) {
                LotDetailScreen(
                    order = order,
                    cmupRate = uiState.cmupRate,
                    availableYuans = uiState.availableYuans,
                    accounts = businessAccounts,
                    transitaires = uiState.catalogueTransitaires,
                    navState = navState,
                    onBack = { navState.closeDetail() },
                    onUpdateOrder = { viewModel.updateBusinessOrder(it) },
                    onDeleteOrder = { viewModel.deleteStockOrder(it) },
                    onSpendYuans = { amt, rate, plat, note, orderId, skip, debitDateMs, onResult ->
                        viewModel.spendYuans(amt, rate, plat, note, orderId, skip, debitDateMs) { ok, _ ->
                            onResult(ok)
                        }
                    },
                    onRefundYuans = { amt, rate, plat, note, orderId ->
                        viewModel.refundYuans(amt, rate, plat, note, orderId)
                    }
                )
                BusinessSheetHost(
                    navState = navState,
                    viewModel = viewModel,
                    uiState = uiState,
                    businessAccounts = businessAccounts,
                    businessCategories = businessCategories
                )
                return
            } else {
                LaunchedEffect(dest.orderId) { navState.closeDetail() }
            }
        }

        is BusinessDetailDestination.ProductDetail -> {
            val article = uiState.allCatalogueArticles.firstOrNull { it.articleKey == dest.articleKey }
            if (article != null) {
                ProductDetailScreen(
                    article = article,
                    navState = navState,
                    onBack = { navState.closeDetail() },
                    onToggleHide = { viewModel.toggleHideCatalogueArticle(it) },
                    onDeleteArticle = { viewModel.deleteCatalogueArticle(it) },
                    onUpdatePhoto = { newPhoto ->
                        val p = article.primaryOrder
                        viewModel.updateCatalogueArticleFull(
                            article = article,
                            newProduct = p.product,
                            newReferenceCode = p.referenceCode,
                            newFamily = p.family,
                            newPlatform = p.platform,
                            newPriceYuan = article.latestPriceYuan,
                            newResaleAr = article.resalePriceAr,
                            newWeightKg = p.weightKg,
                            newFreightMode = p.freightMode,
                            newVariants = p.variants,
                            newImageUrl = newPhoto,
                            newNotes = p.receptionNotes,
                            isHidden = article.isHidden
                        )
                    },
                    onUpdateVariants = { newVariants ->
                        val p = article.primaryOrder
                        viewModel.updateCatalogueArticleFull(
                            article = article,
                            newProduct = p.product,
                            newReferenceCode = p.referenceCode,
                            newFamily = p.family,
                            newPlatform = p.platform,
                            newPriceYuan = article.latestPriceYuan,
                            newResaleAr = article.resalePriceAr,
                            newWeightKg = p.weightKg,
                            newFreightMode = p.freightMode,
                            newVariants = newVariants,
                            newImageUrl = p.imageUrl,
                            newNotes = p.receptionNotes,
                            isHidden = article.isHidden
                        )
                    }
                )
                BusinessSheetHost(
                    navState = navState,
                    viewModel = viewModel,
                    uiState = uiState,
                    businessAccounts = businessAccounts,
                    businessCategories = businessCategories
                )
                return
            } else {
                LaunchedEffect(dest.articleKey) { navState.closeDetail() }
            }
        }

        is BusinessDetailDestination.Referentiel -> {
            ReferentielScreen(
                uiState = uiState,
                initialTab = dest.initialTab,
                navState = navState,
                onBack = { navState.closeDetail() },
                onTogglePlatformActive = { id, active -> viewModel.togglePlatformActive(id, active) },
                onDeletePlatform = { id -> viewModel.deleteCataloguePlatform(id) },
                onDeleteFamily = { fam -> viewModel.deleteCatalogueFamily(fam) },
                onToggleTransitaireArchived = { id -> viewModel.toggleTransitaireArchived(id) },
                onDeleteTransitaire = { id -> viewModel.deleteCatalogueTransitaire(id) }
            )
            BusinessSheetHost(
                navState = navState,
                viewModel = viewModel,
                uiState = uiState,
                businessAccounts = businessAccounts,
                businessCategories = businessCategories
            )
            return
        }

        is BusinessDetailDestination.TransitaireDetail -> {
            val transitaire = uiState.catalogueTransitaires.firstOrNull { it.id == dest.transitaireId }
            if (transitaire != null) {
                TransitaireDetailScreen(
                    transitaire = transitaire,
                    navState = navState,
                    onBack = { navState.closeDetail() },
                    onSaveGrid = { sync ->
                        viewModel.updateCatalogueTransitaire(
                            id = transitaire.id,
                            name = transitaire.name,
                            hub = transitaire.routeHub,
                            airRate = sync.airRateAr,
                            seaRate = sync.seaRateAr,
                            contact = transitaire.contact,
                            chinaWarehouseAddress = transitaire.chinaWarehouseAddress,
                            isActive = !transitaire.isArchived,
                            rateApplicationDate = sync.rateApplicationDate,
                            airLeadDays = sync.airLeadDays,
                            seaLeadDays = sync.seaLeadDays,
                            internalNotes = transitaire.internalNotes,
                            airFastRate = sync.airFastRateAr,
                            airFastLeadDays = transitaire.airFastLeadDays,
                            goodsCategoryDelays = sync.encodedGrid
                        )
                    },
                    onResetHistory = { viewModel.resetAllTransitaireRateHistories() },
                    onOpenLot = { orderId -> navState.openLot(orderId) }
                )
                BusinessSheetHost(
                    navState = navState,
                    viewModel = viewModel,
                    uiState = uiState,
                    businessAccounts = businessAccounts,
                    businessCategories = businessCategories
                )
                return
            } else {
                LaunchedEffect(dest.transitaireId) { navState.closeDetail() }
            }
        }

        BusinessDetailDestination.None -> Unit
    }

    BackHandler {
        if (navState.activeSheet != BusinessActiveSheet.None) {
            navState.closeSheet()
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavBar(selectedTab = 3, onTabSelected = onTabSelected)
        },
        floatingActionButton = {
            BusinessContextualFab(
                selectedSubTab = uiState.selectedSubTab,
                stockSegment = uiState.stockSegment,
                navState = navState
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(vertical = 8.dp)
        ) {
            BusinessResponsiveContainer { _, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    HumanBusinessHeader(
                        uiState = uiState,
                        colors = colors,
                        onBack = onNavigateBack,
                        onOpenReferentiel = { navState.openReferentiel() },
                        onOpenNotifications = { navState.activeSheet = BusinessActiveSheet.Notifications }
                    )

                    BusinessSubTabsRow(
                        selectedSubTab = uiState.selectedSubTab,
                        colors = colors,
                        onSelectSubTab = { viewModel.onSelectSubTab(it) }
                    )

                    AnimatedContent(
                        targetState = uiState.selectedSubTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "businessTabContent"
                    ) { tab ->
                        when (tab) {
                            "Achats" -> PurchasesTab(
                                uiState = uiState,
                                navState = navState,
                                onSearchQueryChanged = { viewModel.onPurchasesSearchQueryChanged(it) }
                            )

                            "Stock" -> StockTab(
                                uiState = uiState,
                                navState = navState,
                                onSegmentSelected = { viewModel.onStockSegmentSelected(it) },
                                onSearchQueryChanged = { viewModel.onStockSearchQueryChanged(it) },
                                onFamilyFilterSelected = { viewModel.onStockFamilyFilterSelected(it) },
                                onToggleShowHidden = { viewModel.onToggleShowHiddenStock() }
                            )

                            "Ventes" -> SalesTab(
                                uiState = uiState,
                                navState = navState,
                                onPrevMonth = { viewModel.onPrevMonth() },
                                onNextMonth = { viewModel.onNextMonth() },
                                onDomainSelected = { viewModel.onSalesDomainSelected(it) },
                                onSearchQueryChanged = { viewModel.onSalesSearchQueryChanged(it) },
                                onDeleteSale = { viewModel.deleteSale(it) }
                            )

                            "Clients" -> ClientsTab(
                                uiState = uiState,
                                navState = navState,
                                onFilterSelected = { viewModel.onClientsFilterSelected(it) },
                                onSearchQueryChanged = { viewModel.onClientsSearchQueryChanged(it) }
                            )

                            "Finances" -> FinancesTab(
                                uiState = uiState,
                                accounts = businessAccounts,
                                transactions = businessTransactions,
                                investments = businessInvestments,
                                debts = businessDebts,
                                categories = businessCategories,
                                navState = navState,
                                onDeleteInvestment = { viewModel.deleteBusinessInvestment(it) },
                                onDeleteDebt = { viewModel.deleteBusinessDebt(it) }
                            )

                            else -> OverviewTab(
                                uiState = uiState,
                                navState = navState,
                                onPrevMonth = { viewModel.onPrevMonth() },
                                onNextMonth = { viewModel.onNextMonth() },
                                onResetMonth = { viewModel.onResetCurrentMonth() },
                                onSelectSubTab = { viewModel.onSelectSubTab(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(76.dp))
                }
            }
        }
    }

    BusinessSheetHost(
        navState = navState,
        viewModel = viewModel,
        uiState = uiState,
        businessAccounts = businessAccounts,
        businessCategories = businessCategories
    )
}

@Composable
private fun HumanBusinessHeader(
    uiState: BusinessUiState,
    colors: TransitaireColors,
    onBack: () -> Unit,
    onOpenReferentiel: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val notifCount = uiState.businessNotifications.size
    val subtitle = buildString {
        append("${uiState.activeLotsCount} lot${if (uiState.activeLotsCount > 1) "s" else ""} actif${if (uiState.activeLotsCount > 1) "s" else ""}")
        if (uiState.availableStockPieces > 0) {
            append(" · ${uiState.availableStockPieces} pcs en stock")
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_business_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour à l'accueil",
                    tint = colors.textPrimary
                )
            }
            Column {
                Text(
                    text = "Mon Business",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton Référentiel (Transitaires, Familles, Plateformes)
            Surface(
                shape = CircleShape,
                color = colors.cardBg,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenReferentiel)
                    .testTag("btn_open_referentiel")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Référentiel transitaires et familles",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bouton Rappels / Notifications
            Surface(
                shape = CircleShape,
                color = if (notifCount > 0) colors.warningSoftBg else colors.cardBg,
                border = BorderStroke(1.dp, if (notifCount > 0) colors.warningAccent.copy(alpha = 0.35f) else colors.border),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onOpenNotifications)
                    .testTag("btn_business_notifications")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Rappels et alertes",
                        tint = if (notifCount > 0) colors.warningAccent else colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (notifCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 6.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(colors.dangerAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessSubTabsRow(
    selectedSubTab: String,
    colors: TransitaireColors,
    onSelectSubTab: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BUSINESS_SUB_TABS.forEach { tab ->
            val selected = tab == selectedSubTab
            val bg by animateColorAsState(
                targetValue = if (selected) CyanPrimary else colors.cardBg,
                label = "subTabBg"
            )
            Surface(
                shape = CircleShape,
                color = bg,
                border = if (selected) null else BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(CircleShape)
                    .clickable { onSelectSubTab(tab) }
                    .testTag("subtab_$tab")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = tab,
                        fontFamily = PlusJakartaSans,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (selected) Color.White else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessContextualFab(
    selectedSubTab: String,
    stockSegment: String,
    navState: BusinessNavigationState
) {
    val (label, containerColor, targetSheet) = when (selectedSubTab) {
        "Achats" -> Triple("Achat", CyanPrimary, BusinessActiveSheet.OrderForm())
        "Stock" -> if (stockSegment == "Catalogue") {
            Triple("Article", VioletSecondary, BusinessActiveSheet.AddArticle)
        } else {
            Triple("Achat", CyanPrimary, BusinessActiveSheet.OrderForm())
        }
        "Ventes" -> Triple("Vente", VioletSecondary, BusinessActiveSheet.AddSale())
        "Clients" -> Triple("Client", CyanPrimary, BusinessActiveSheet.AddClient)
        "Finances" -> Triple("Opération", VioletSecondary, BusinessActiveSheet.AddFinanceOperation())
        else -> Triple("Vente", CyanPrimary, BusinessActiveSheet.AddSale())
    }

    ExtendedFloatingActionButton(
        onClick = { navState.activeSheet = targetSheet },
        shape = CircleShape,
        containerColor = containerColor,
        contentColor = Color.White,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = {
            Text(
                text = label,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        modifier = Modifier.testTag("fab_business_contextual")
    )
}
